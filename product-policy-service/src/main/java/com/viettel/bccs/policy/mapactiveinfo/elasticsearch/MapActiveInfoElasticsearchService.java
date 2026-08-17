package com.viettel.bccs.policy.mapactiveinfo.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.JsonData;
import com.viettel.bccs.common.error.code.CommonErrorCode;
import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.common.error.exception.IntegrationException;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
import com.viettel.bccs.policy.mapactiveinfo.entity.MapActiveInfoEntity;
import com.viettel.bccs.policy.mapactiveinfo.repository.MapActiveInfoRepository;
import com.viettel.bccs.policy.utils.Const;
import com.viettel.bccs.policy.utils.DataUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "bccs.policy.mapactiveinfo.elasticsearch", name = "enabled", havingValue = "true")
public class MapActiveInfoElasticsearchService {

    private static final int REINDEX_PAGE_SIZE = 2000;

    private final ElasticsearchClient client;
    private final MapActiveInfoRepository repository;

    public void ensureIndex() {
        boolean exists = execute("indices.exists", () -> client.indices()
                .exists(ExistsRequest.of(e -> e.index(MapActiveInfoEsDocument.INDEX_NAME)))
                .value());
        if (exists) {
            return;
        }
        execute("indices.create", () -> client.indices().create(c -> c
                .index(MapActiveInfoEsDocument.INDEX_NAME)
                .mappings(m -> m
                        .properties("id", p -> p.long_(l -> l))
                        .properties("telServiceId", p -> p.long_(l -> l))
                        .properties("productCode", p -> p.keyword(k -> k))
                        .properties("regReasonId", p -> p.long_(l -> l))
                        .properties("promCode", p -> p.keyword(k -> k))
                        .properties("channelTypeId", p -> p.long_(l -> l))
                        .properties("provinceCode", p -> p.keyword(k -> k))
                        .properties("districtCode", p -> p.keyword(k -> k))
                        .properties("effectDate", p -> p.date(d -> d))
                        .properties("endDate", p -> p.date(d -> d))
                        .properties("offerId", p -> p.long_(l -> l))
                        .properties("status", p -> p.long_(l -> l))
                        .properties("precinctCode", p -> p.keyword(k -> k))
                        .properties("shopCode", p -> p.keyword(k -> k))
                        .properties("staffCode", p -> p.keyword(k -> k))
                        .properties("actionCode", p -> p.keyword(k -> k))
                        .properties("customerGroup", p -> p.keyword(k -> k))
                        .properties("customerType", p -> p.keyword(k -> k))
                        .properties("subType", p -> p.keyword(k -> k))
                        .properties("subGroup", p -> p.keyword(k -> k))
                        .properties("stationCodes", p -> p.keyword(k -> k))
                        .properties("stationCodesList", p -> p.keyword(k -> k))
                        .properties("technology", p -> p.keyword(k -> k))
                        .properties("nodeCode", p -> p.keyword(k -> k))
                        .properties("groupNodeCode", p -> p.keyword(k -> k))
                        .properties("payType", p -> p.keyword(k -> k))
                        .properties("issueDatetime", p -> p.date(d -> d))
                        .properties("updateDatetime", p -> p.date(d -> d))
                )
        ));
        log.info("Đã tạo index Elasticsearch '{}'", MapActiveInfoEsDocument.INDEX_NAME);
    }

    /**
     * Đọc toàn bộ MAP_ACTIVE_INFO từ Oracle (phân trang) và bulk-index
     * sang Elasticsearch. Trả về tổng số document đã index.
     */
    public long reindexAll() {
        ensureIndex();
        long total = 0;
        int page = 0;
        while (true) {
            var pageResult = repository.findAll(PageRequest.of(page, REINDEX_PAGE_SIZE, Sort.by("id")));
            if (pageResult.isEmpty()) {
                break;
            }
            List<BulkOperation> operations = pageResult.stream()
                    .map(entity -> BulkOperation.of(op -> op.index(idx -> idx
                            .index(MapActiveInfoEsDocument.INDEX_NAME)
                            .id(String.valueOf(entity.getId()))
                            .document(toDocument(entity)))))
                    .toList();
            execute("bulk", () -> client.bulk(BulkRequest.of(b -> b.operations(operations))));
            total += pageResult.getNumberOfElements();
            log.info("Đã reindex {} document (page {})", total, page);
            if (!pageResult.hasNext()) {
                break;
            }
            page++;
        }
        execute("indices.refresh", () -> client.indices().refresh(r -> r.index(MapActiveInfoEsDocument.INDEX_NAME)));
        log.info("Reindex Elasticsearch xong: {} document", total);
        return total;
    }


    public List<MapActiveInfoEntity> search(MapActiveInfoDTO dto, boolean searchInvidualField) {
        BoolQuery.Builder root = new BoolQuery.Builder();

        // STATUS != 0
        root.mustNot(mn -> mn.term(t -> t.field("status").value(0)));
        // sysdate > trunc(EFFECT_DATE)  =>  effectDate < now
        root.filter(f -> f.range(r -> r.field("effectDate").lt(JsonData.of("now/d"))));
        // END_DATE is null OR END_DATE >= trunc(sysdate)
        root.filter(f -> f.bool(b -> b
                .should(sh -> sh.bool(bb -> bb.mustNot(mn -> mn.exists(e -> e.field("endDate")))))
                .should(sh -> sh.range(r -> r.field("endDate").gte(JsonData.of("now/d"))))
                .minimumShouldMatch("1")));

        if (dto.getFromDate() != null) {
            root.filter(f -> f.range(r -> r.field("issueDatetime").gte(JsonData.of(toEsDate(dto.getFromDate())))));
        }
        if (dto.getToDate() != null) {
            root.filter(f -> f.range(r -> r.field("updateDatetime").lte(JsonData.of(toEsDate(dto.getToDate())))));
        }

        addSelectAllCondition(root, "id", dto.getId());
        addSelectAllCondition(root, "payType", dto.getPayType());
        addSelectAllCondition(root, "actionCode", dto.getActionCode());
        addSelectAllCondition(root, "telServiceId", dto.getTelServiceId());
        addSelectAllCondition(root, "channelTypeId", dto.getChannelTypeId());
        addSelectAllCondition(root, "provinceCode", dto.getProvinceCode());
        addSelectAllCondition(root, "districtCode", dto.getDistrictCode());
        addSelectAllCondition(root, "precinctCode", dto.getPrecinctCode());
        addSelectAllCondition(root, "shopCode", dto.getShopCode());
        addSelectAllCondition(root, "staffCode", dto.getStaffCode());
        addSelectAllCondition(root, "customerGroup", dto.getCustomerGroup());
        addSelectAllCondition(root, "customerType", dto.getCustomerType());
        addSelectAllCondition(root, "subGroup", dto.getSubGroup());
        addSelectAllCondition(root, "subType", dto.getSubType());
        addSelectAllCondition(root, "technology", dto.getTechnology());

        if (!DataUtil.isNullOrEmpty(dto.getStationCodes()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getStationCodes())) {
            String stationCode = dto.getStationCodes();
            root.filter(f -> f.bool(b -> b
                    .should(sh -> sh.terms(t -> t.field("stationCodesList")
                            .terms(tt -> tt.value(List.of(FieldValue.of(stationCode))))))
                    .should(sh -> sh.term(t -> t.field("stationCodes").value(Const.DEFAULT_VALUE_MAP_SELECT_ALL)))
                    .should(sh -> sh.bool(bb -> bb.mustNot(mn -> mn.exists(e -> e.field("stationCodes")))))
                    .minimumShouldMatch("1")));
        }

        // NODE_CODE: chỉ dịch phần match/null/sentinel — nhánh EXISTS join group_node không dịch được (xem javadoc)
        if (!DataUtil.isNullOrEmpty(dto.getNodeCode()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getNodeCode())) {
            String nodeCode = dto.getNodeCode();
            root.filter(f -> f.bool(b -> b
                    .should(sh -> sh.wildcard(w -> w.field("nodeCode").value("*" + nodeCode + "*").caseInsensitive(true)))
                    .should(sh -> sh.bool(bb -> bb.mustNot(mn -> mn.exists(e -> e.field("nodeCode")))))
                    .should(sh -> sh.terms(t -> t.field("nodeCode")
                            .terms(tt -> tt.value(List.of(FieldValue.of("-1"), FieldValue.of("-1;"))))))
                    .minimumShouldMatch("1")));
        }

        if (searchInvidualField) {
            addSelectAllCondition(root, "productCode", dto.getProductCode());
            addSelectAllCondition(root, "regReasonId", dto.getRegReasonId());
            addSelectAllCondition(root, "promCode", dto.getPromCode());
        }

        Query query = Query.of(q -> q.bool(root.build()));

        SearchResponse<MapActiveInfoEsDocument> response = execute("search", () -> client.search(s -> s
                        .index(MapActiveInfoEsDocument.INDEX_NAME)
                        .size(10000)
                        .query(query)
                        .sort(so -> so.field(fs -> fs.field("regReasonId").order(SortOrder.Asc))),
                MapActiveInfoEsDocument.class));

        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .filter(Objects::nonNull)
                .map(this::toEntity)
                .toList();
    }

    private void addSelectAllCondition(BoolQuery.Builder root, String field, String value) {
        if (DataUtil.isNullOrEmpty(value) || Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(value)) {
            return;
        }
        root.filter(f -> f.bool(b -> b
                .should(sh -> sh.term(t -> t.field(field).value(value)))
                .should(sh -> sh.term(t -> t.field(field).value(Const.DEFAULT_VALUE_MAP_SELECT_ALL)))
                .should(sh -> sh.bool(bb -> bb.mustNot(mn -> mn.exists(e -> e.field(field)))))
                .minimumShouldMatch("1")));
    }

    private void addSelectAllCondition(BoolQuery.Builder root, String field, Long value) {
        if (DataUtil.isNullOrZero(value) || Long.valueOf(Const.DEFAULT_VALUE_MAP_SELECT_ALL).equals(value)) {
            return;
        }
        long sentinel = Long.parseLong(Const.DEFAULT_VALUE_MAP_SELECT_ALL);
        root.filter(f -> f.bool(b -> b
                .should(sh -> sh.term(t -> t.field(field).value(value)))
                .should(sh -> sh.term(t -> t.field(field).value(sentinel)))
                .should(sh -> sh.bool(bb -> bb.mustNot(mn -> mn.exists(e -> e.field(field)))))
                .minimumShouldMatch("1")));
    }

    private static String toEsDate(java.util.Date date) {
        return Instant.ofEpochMilli(date.getTime()).toString();
    }

    private MapActiveInfoEsDocument toDocument(MapActiveInfoEntity e) {
        return MapActiveInfoEsDocument.builder()
                .id(e.getId())
                .telServiceId(e.getTelServiceId())
                .productCode(e.getProductCode())
                .productName(e.getProductName())
                .regReasonId(e.getRegReasonId())
                .reasonName(e.getReasonName())
                .promCode(e.getPromCode())
                .promName(e.getPromName())
                .channelTypeId(e.getChannelTypeId())
                .channelName(e.getChannelName())
                .provinceCode(e.getProvinceCode())
                .districtCode(e.getDistrictCode())
                .effectDate(e.getEffectDate())
                .endDate(e.getEndDate())
                .provinceName(e.getProvinceName())
                .districtName(e.getDistrictName())
                .offerId(e.getOfferId())
                .offerName(e.getOfferName())
                .status(e.getStatus())
                .precinctName(e.getPrecinctName())
                .precinctCode(e.getPrecinctCode())
                .shopCode(e.getShopCode())
                .staffCode(e.getStaffCode())
                .actionCode(e.getActionCode())
                .actionName(e.getActionName())
                .limitNumber(e.getLimitNumber())
                .captcharRequire(e.getCaptcharRequire())
                .unit(e.getUnit())
                .customerGroup(e.getCustomerGroup())
                .customerType(e.getCustomerType())
                .subType(e.getSubType())
                .subGroup(e.getSubGroup())
                .policyDoc(e.getPolicyDoc())
                .actionGroup(e.getActionGroup())
                .actionGroupName(e.getActionGroupName())
                .fileName(e.getFileName())
                .stationId(e.getStationId())
                .shopId(e.getShopId())
                .createUser(e.getCreateUser())
                .issueDatetime(e.getIssueDatetime())
                .stationCodes(e.getStationCodes())
                .stationCodesList(splitStationCodes(e.getStationCodes()))
                .payType(e.getPayType())
                .technology(e.getTechnology())
                .updateDatetime(e.getUpdateDatetime())
                .updateUser(e.getUpdateUser())
                .areaGroupCode(e.getAreaGroupCode())
                .vasCode(e.getVasCode())
                .vasName(e.getVasName())
                .nodeCode(e.getNodeCode())
                .note(e.getNote())
                .groupNodeCode(e.getGroupNodeCode())
                .staffType(e.getStaffType())
                .businessNo(e.getBusinessNo())
                .subGroupCode(e.getSubGroupCode())
                .smeCustomerGroup(e.getSmeCustomerGroup())
                .importOfflineId(e.getImportOfflineId())
                .build();
    }

    private MapActiveInfoEntity toEntity(MapActiveInfoEsDocument d) {
        MapActiveInfoEntity e = new MapActiveInfoEntity();
        e.setId(d.getId());
        e.setTelServiceId(d.getTelServiceId());
        e.setProductCode(d.getProductCode());
        e.setProductName(d.getProductName());
        e.setRegReasonId(d.getRegReasonId());
        e.setReasonName(d.getReasonName());
        e.setPromCode(d.getPromCode());
        e.setPromName(d.getPromName());
        e.setChannelTypeId(d.getChannelTypeId());
        e.setChannelName(d.getChannelName());
        e.setProvinceCode(d.getProvinceCode());
        e.setDistrictCode(d.getDistrictCode());
        e.setEffectDate(d.getEffectDate());
        e.setEndDate(d.getEndDate());
        e.setProvinceName(d.getProvinceName());
        e.setDistrictName(d.getDistrictName());
        e.setOfferId(d.getOfferId());
        e.setOfferName(d.getOfferName());
        e.setStatus(d.getStatus());
        e.setPrecinctName(d.getPrecinctName());
        e.setPrecinctCode(d.getPrecinctCode());
        e.setShopCode(d.getShopCode());
        e.setStaffCode(d.getStaffCode());
        e.setActionCode(d.getActionCode());
        e.setActionName(d.getActionName());
        e.setLimitNumber(d.getLimitNumber());
        e.setCaptcharRequire(d.getCaptcharRequire());
        e.setUnit(d.getUnit());
        e.setCustomerGroup(d.getCustomerGroup());
        e.setCustomerType(d.getCustomerType());
        e.setSubType(d.getSubType());
        e.setSubGroup(d.getSubGroup());
        e.setPolicyDoc(d.getPolicyDoc());
        e.setActionGroup(d.getActionGroup());
        e.setActionGroupName(d.getActionGroupName());
        e.setFileName(d.getFileName());
        e.setStationId(d.getStationId());
        e.setShopId(d.getShopId());
        e.setCreateUser(d.getCreateUser());
        e.setIssueDatetime(d.getIssueDatetime());
        e.setStationCodes(d.getStationCodes());
        e.setPayType(d.getPayType());
        e.setTechnology(d.getTechnology());
        e.setUpdateDatetime(d.getUpdateDatetime());
        e.setUpdateUser(d.getUpdateUser());
        e.setAreaGroupCode(d.getAreaGroupCode());
        e.setVasCode(d.getVasCode());
        e.setVasName(d.getVasName());
        e.setNodeCode(d.getNodeCode());
        e.setNote(d.getNote());
        e.setGroupNodeCode(d.getGroupNodeCode());
        e.setStaffType(d.getStaffType());
        e.setBusinessNo(d.getBusinessNo());
        e.setSubGroupCode(d.getSubGroupCode());
        e.setSmeCustomerGroup(d.getSmeCustomerGroup());
        e.setImportOfflineId(d.getImportOfflineId());
        return e;
    }

    private static List<String> splitStationCodes(String stationCodes) {
        if (DataUtil.isNullOrEmpty(stationCodes)) {
            return List.of();
        }
        return Arrays.stream(stationCodes.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private <T> T execute(String operation, ElasticsearchCall<T> call) {
        try {
            return call.execute();
        } catch (BusinessException exception) {
            throw exception;
        } catch (IOException | ElasticsearchException exception) {
            throw new IntegrationException(
                    CommonErrorCode.INTEGRATION_ERROR,
                    "Elasticsearch " + operation + " operation failed",
                    exception);
        }
    }

    @FunctionalInterface
    private interface ElasticsearchCall<T> {
        T execute() throws IOException;
    }
}
