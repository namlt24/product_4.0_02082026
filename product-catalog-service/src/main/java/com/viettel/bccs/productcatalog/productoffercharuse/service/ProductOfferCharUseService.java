package com.viettel.bccs.productcatalog.productoffercharuse.service;

import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharValueDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.mapper.ProductSpecCharUseMapper;
import com.viettel.bccs.productcatalog.productoffercharuse.mapper.ProductSpecCharValueUseMapper;
import com.viettel.bccs.productcatalog.productoffercharuse.repository.ProductOfferCharUseRepositoryCustom;
import com.viettel.bccs.productcatalog.productspecchar.entity.ProductSpecCharEntity;
import com.viettel.bccs.productcatalog.productspeccharvalue.entity.ProductSpecCharValueEntity;
import com.viettel.bccs.productcatalog.utils.Const;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductOfferCharUseService {

    private final ProductOfferCharUseRepositoryCustom repository;
    private final ProductSpecCharUseMapper productSpecCharUseMapper;
    private final ProductSpecCharValueUseMapper productSpecCharValueUseMapper;

    @Cacheable(value = "productOfferCharUseCache", key = "'OFFERING_SPEC_CHAR_BATCH:' + T(String).join(',', #offeringIds.stream().sorted().toList())")
    public Map<Long, List<ProductSpecCharDTO>> getProductSpecCharByOfferingIds(List<String> offeringIds) {
        if (DataUtil.isNullOrEmpty(offeringIds)) {
            return Collections.emptyMap();
        }

        Map<Long, List<ProductSpecCharDTO>> resultMap = new LinkedHashMap<>();

        List<String> validIds = offeringIds.stream()
                .filter(id -> {
                    try { return Long.parseLong(id) <= Integer.MAX_VALUE; }
                    catch (NumberFormatException e) { return false; }
                })
                .toList();

        if (!validIds.isEmpty()) {
            List<Object[]> results = repository.findSpecCharsByOfferingIds(validIds);
            if (!DataUtil.isNullOrEmpty(results)) {
                for (Object[] row : results) {
                    Long productOfferingId = ((Number) row[0]).longValue();
                    Long offerCharUseId = ((Number) row[1]).longValue();
                    String offerCharUseType = row[2] != null ? row[2].toString() : null;

                    ProductSpecCharEntity charEntity = buildCharEntity(row);
                    ProductSpecCharValueEntity valueEntity = buildValueEntity(row);
                    String valueName = row[37] != null ? row[37].toString() : null;

                    ProductSpecCharValueDTO valueDto = productSpecCharValueUseMapper.toDto(valueEntity);
                    ProductSpecCharDTO dto = productSpecCharUseMapper.toDtoWithValue(
                            charEntity, valueDto, valueName, productOfferingId, offerCharUseId, offerCharUseType);

                    resultMap.computeIfAbsent(productOfferingId, k -> new ArrayList<>()).add(dto);
                }
            }
        }

        return resultMap;
    }

    @Cacheable(value = "productOfferCharUseCache", key = "'ATTR_VALUE:' + #offerId + ':' + #attributeName")
    public Optional<String> getAttributeValue(Long offerId, String attributeName) {
        if (offerId == null || DataUtil.isNullOrEmpty(attributeName)) {
            return Optional.empty();
        }
        return repository.findAttributeValueByOfferingIdAndCharCode(offerId, attributeName);
    }

    @Cacheable(value = "productOfferCharUseCache", key = "'PRICE_PLAN:' + #productOfferingId")
    public List<ProductSpecCharDTO> getListPricePlanByOfferId(Long productOfferingId) {
        if (productOfferingId == null) {
            return Collections.emptyList();
        }

        List<Object[]> results = repository.findCharsByOfferingIdAndCharType(productOfferingId, Const.CHAR_TYPE.PRICE_PLAN);
        if (DataUtil.isNullOrEmpty(results)) {
            return Collections.emptyList();
        }

        List<ProductSpecCharDTO> lst = new ArrayList<>();
        for (Object[] row : results) {
            Long offeringId = ((Number) row[0]).longValue();
            Long offerCharUseId = ((Number) row[1]).longValue();
            String offerCharUseType = row[2] != null ? row[2].toString() : null;

            ProductSpecCharEntity charEntity = buildCharEntity(row);
            ProductSpecCharValueEntity valueEntity = buildValueEntity(row);
            String valueName = row[37] != null ? row[37].toString() : null;

            ProductSpecCharValueDTO valueDto = productSpecCharValueUseMapper.toDto(valueEntity);
            ProductSpecCharDTO dto = productSpecCharUseMapper.toDtoWithValue(
                    charEntity, valueDto, valueName, offeringId, offerCharUseId, offerCharUseType);

            lst.add(dto);
        }
        return lst;
    }

    private String str(Object val) {
        return val != null ? val.toString() : null;
    }

    private ProductSpecCharEntity buildCharEntity(Object[] row) {
        return ProductSpecCharEntity.builder()
                .productSpecCharId(row[3] != null ? ((Number) row[3]).longValue() : null)
                .name(str(row[4]))
                .description(str(row[5]))
                .valueType(str(row[6]))
                .charType(str(row[7]))
                .minCardinality(row[8] != null ? ((Number) row[8]).longValue() : null)
                .maxCardinality(row[9] != null ? ((Number) row[9]).longValue() : null)
                .status(str(row[10]))
                .code(str(row[11]))
                .productSpecCharTypeId(str(row[12]))
                .valueSetType(row[13] != null ? ((Number) row[13]).longValue() : null)
                .responseClass(str(row[14]))
                .sqlQuery(str(row[15]))
                .displayObject(str(row[16]))
                .valueObject(str(row[17]))
                .solrQuery(str(row[18]))
                .solrCore(str(row[19]))
                .solrSchema(str(row[20]))
                .dataType(str(row[21]))
                .wsWsdl(str(row[22]))
                .templateRequest(str(row[23]))
                .validatePattern(str(row[24]))
                .extData(str(row[25]))
                .note(str(row[26]))
                .build();
    }

    private ProductSpecCharValueEntity buildValueEntity(Object[] row) {
        return ProductSpecCharValueEntity.builder()
                .productSpecCharValueId(row[27] != null ? ((Number) row[27]).longValue() : null)
                .productSpecCharId(row[28] != null ? ((Number) row[28]).longValue() : null)
                .valueType(str(row[29]))
                .isDefault(row[30] != null ? ((Number) row[30]).longValue() : null)
                .value(str(row[31]))
                .unitOfMeasure(str(row[32]))
                .valueFrom(str(row[33]))
                .valueTo(str(row[34]))
                .rangeInterval(str(row[35]))
                .status(str(row[36]))
                .name(str(row[37]))
                .specificValue(str(row[38]))
                .note(str(row[39]))
                .build();
    }
}