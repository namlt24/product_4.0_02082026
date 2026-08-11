package com.viettel.bccs.policy.mapactiveinfo.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.policy.client.OptionSetClient;
import com.viettel.bccs.policy.client.StaffExtClient;
import com.viettel.bccs.policy.client.TelecomServiceClient;
import com.viettel.bccs.policy.client.dto.OptionSetValueResponse;
import com.viettel.bccs.policy.client.dto.StaffDTO;
import com.viettel.bccs.policy.client.dto.StaffExtResponse;
import com.viettel.bccs.policy.common.dto.FilterRequest;
import com.viettel.bccs.policy.common.helper.StaffResolveHelper;
import com.viettel.bccs.policy.discountpromotioncharuse.mapper.MapActiveInfoMapper;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.ChanelTypeIdRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.IsCheckMapActiveInfoRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.entity.MapActiveInfoEntity;
import com.viettel.bccs.policy.mapactiveinfo.repository.MapActiveInfoRepository;
import com.viettel.bccs.policy.reason.dto.response.ReasonDTO;
import com.viettel.bccs.policy.reason.service.ReasonService;
import com.viettel.bccs.policy.reasonpause.dto.response.ReasonPauseDTO;
import com.viettel.bccs.policy.utils.Const;
import com.viettel.bccs.policy.utils.DataUtil;
import com.viettel.bccs.policy.utils.RequiredRoleMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class MapActiveInfoQuerryService {

    private final MapActiveInfoRepository repository;
    private final MapActiveInfoMapper mapper;
    private final OptionSetClient optionSetClient;
    private final TelecomServiceClient telecomServiceClient;
    @Lazy
    private final MapActiveInfoValidateService mapActiveInfoValidateService;
    private final StaffExtClient staffExtClient;
    @Lazy
    private final ReasonService reasonService;
    private final StaffResolveHelper staffResolveHelper;

    public MapActiveInfoQuerryService(MapActiveInfoRepository repository, MapActiveInfoMapper mapper,
                                      OptionSetClient optionSetClient, TelecomServiceClient telecomServiceClient,
                                      @Lazy MapActiveInfoValidateService mapActiveInfoValidateService,
                                      StaffExtClient staffExtClient,
                                      @Lazy ReasonService reasonService,
                                      StaffResolveHelper staffResolveHelper) {
        this.repository = repository;
        this.mapper = mapper;
        this.optionSetClient = optionSetClient;
        this.telecomServiceClient = telecomServiceClient;
        this.mapActiveInfoValidateService = mapActiveInfoValidateService;
        this.staffExtClient = staffExtClient;
        this.reasonService = reasonService;
        this.staffResolveHelper = staffResolveHelper;
    }


    public MapActiveInfoResponse findById(Long id) {
        Optional<MapActiveInfoEntity> entity = repository.findById(id);
        if (entity.isEmpty()) {
            throw new BusinessException("BCCS-POLICY-MAPACTIVE-0020", "Map active info not found with id: " + id);
        }
        return mapper.toResponse(entity.get());
    }

    public List<MapActiveInfoDTO> findByExampleWithOfferType(MapActiveInfoDTO exampleMapActiveInfo, int mode, String offerType) {
        return findByExample(exampleMapActiveInfo, mode, false, offerType, false);
    }

    private List<MapActiveInfoDTO> findByExample(MapActiveInfoDTO exampleMapActiveInfo, int mode, boolean searchCache, String offerType, boolean isTgdd) {
        if (Const.PRODUCT_OFFER_TYPE.VAS.equals(offerType)) {
            return findByExampleForVas(exampleMapActiveInfo, mode, searchCache);

        } else {
            return findByExampleForProduct(exampleMapActiveInfo, mode, searchCache, isTgdd);
        }
    }

    private List<MapActiveInfoDTO> findByExampleForProduct(MapActiveInfoDTO exampleMapActiveInfo, int mode, boolean searchCache, boolean isTgdd) {
        List<MapActiveInfoDTO> mapActiveInfos = new ArrayList<>();
        if (DataUtil.isNullOrEmpty(exampleMapActiveInfo.getProvinceCode())) {
            log.warn("sale.wired.connect.mapActiveInfo.provinceCode.not.set");
        }
        if (DataUtil.isNullOrEmpty(exampleMapActiveInfo.getDistrictCode())) {
            log.warn("sale.wired.connect.mapActiveInfo.districtCode.not.set");
        }
        Long startTime = System.currentTimeMillis();
        try {
            if (isTgdd) {
                mapActiveInfos = mapper.toDtoBean(repository.findByExample(exampleMapActiveInfo, true, isTgdd));
            } else {
                if (searchCache) {
                    mapActiveInfos = new ArrayList<>(); // todo cache elasticsearch
                    List<Long> elasticListId = new ArrayList<>();
                    for (MapActiveInfoDTO map : mapActiveInfos) {
                        elasticListId.add(map.getId());
                    }
                    log.info("lstMapId from ElasticSearch: " + elasticListId);
                    if (!DataUtil.isNullOrEmpty(mapActiveInfos)) {
                        mapActiveInfos.stream().forEach(x -> {
                            if (DataUtil.safeEqual(x.getSubGroupCode(), Const.DEFAULT_ALL)) {
                                x.setSubGroupCode(null);
                            }
                        });
                    }
                    if (!DataUtil.isNullOrEmpty(exampleMapActiveInfo.getNodeCode())) {
                        mapActiveInfos = mapper.toDtoBean(repository.getListMapActiveInfoByNode(exampleMapActiveInfo.getNodeCode(), elasticListId));
                        List<Long> mapIds = mapActiveInfos.stream().map(MapActiveInfoDTO::getId).collect(Collectors.toList());
                        List<Long> minusIds = elasticListId.stream().filter(id -> !mapIds.contains(id)).collect(Collectors.toList());
                        log.info("minus lstMapId: " + minusIds);
                    }

                    List<Long> mapIds = mapActiveInfos.stream().map(MapActiveInfoDTO::getId).collect(Collectors.toList());
                    List<Long> minusIds = elasticListId.stream().filter(id -> !mapIds.contains(id)).collect(Collectors.toList());
                    log.info("minus lstMapId: " + minusIds);

                } else {
                    mapActiveInfos = mapper.toDtoBean(repository.findByExample(exampleMapActiveInfo, true, isTgdd));
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            try {
                if (searchCache) {
                    mapActiveInfos = mapper.toDtoBean(repository.findByExample(exampleMapActiveInfo, false, isTgdd));
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        Long duration = System.currentTimeMillis() - startTime;
        log.info("Namlt: duration = " + duration);
        log.info("Lay dc " + mapActiveInfos.size() + " ban ghi map active info sau khi query ElasticSearch");
        if (!exampleMapActiveInfo.isCheckVasCode() && !DataUtil.isNullOrEmpty(mapActiveInfos)) {
            mapActiveInfos = mapActiveInfos.stream().filter(x ->
                    DataUtil.isNullOrEmpty(x.getVasCode())
                            && !DataUtil.isNullOrEmpty(x.getProductCode())).collect(Collectors.toList());
        }


        String[] mapFilelds = Const.MAP_ACTIVE_INFO.nonElasticSearchMapFields_1();
        int[] filterModes = Const.MAP_ACTIVE_INFO.nonElasticSearchFilterModes_1();
        if (Const.MAP_ACTIVE_INFO.MODE_1 == mode) {
            mapFilelds = Const.MAP_ACTIVE_INFO.nonElasticSearchMapFields_1();
            filterModes = Const.MAP_ACTIVE_INFO.nonElasticSearchFilterModes_1();
        } else if (Const.MAP_ACTIVE_INFO.MODE_2 == mode) {
            mapFilelds = Const.MAP_ACTIVE_INFO.nonElasticSearchMapFields_2();
            filterModes = Const.MAP_ACTIVE_INFO.nonElasticSearchFilterModes_2();
        } else if (Const.MAP_ACTIVE_INFO.MODE_3 == mode) {
            mapFilelds = Const.MAP_ACTIVE_INFO.nonElasticSearchMapFields_3();
            filterModes = Const.MAP_ACTIVE_INFO.nonElasticSearchFilterModes_3();
        }

        for (int i = 0; i < mapFilelds.length; i++) {
            startTime = System.currentTimeMillis();
            String mapField = mapFilelds[i];
            int filterMode = filterModes[i];
            String filterValue = DataUtil.safeToString(exampleMapActiveInfo.getByProperty(mapField));
            log.info("gia tri filter: " + filterValue + " filter mode: " + filterMode);
            List<MapActiveInfoDTO> tempMapActiveInfoList = new ArrayList<>();

            boolean isIncludeAll = true;
            if (filterMode == Const.MAP_ACTIVE_INFO.FILTER_MODE_ONLY_INDIVIDUAL
                    && (!Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(filterValue))) {
                for (MapActiveInfoDTO curentMapActiveInfo : mapActiveInfos) {
                    String recordValue = DataUtil.safeToString(curentMapActiveInfo.getByProperty(mapField));
                    if (!Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(recordValue)) {
                        isIncludeAll = false;
                        break;
                    }
                }
            }

            log.info("lay tat ca map_active_info " + isIncludeAll);

            for (MapActiveInfoDTO currentMapActiveInfo : mapActiveInfos) {

                String recordValue = DataUtil.safeToString(currentMapActiveInfo.getByProperty(mapField));
                if (Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(filterValue)) {
                    if (isIncludeAll || !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(recordValue)) {
                        tempMapActiveInfoList.add(currentMapActiveInfo);
                    }
                } else {
                    if (recordValue != null && recordValue.equals(filterValue)) {
                        if (isIncludeAll || !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(filterValue)) {
                            tempMapActiveInfoList.add(currentMapActiveInfo);
                        }
                    }
                    if (isIncludeAll) {
                        if (Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(recordValue)) {
                            tempMapActiveInfoList.add(currentMapActiveInfo);
                        }
                    }
                }
            }
            mapActiveInfos = tempMapActiveInfoList;
            log.info("Loc qua " + mapField + " thi con " + mapActiveInfos.size() + " ban ghi");
            duration = System.currentTimeMillis() - startTime;
            log.info("Thuannx: duration_in_for = " + duration);
        }

        if (!DataUtil.isNullOrEmpty(mapActiveInfos)) {
            if (mapActiveInfos.size() < 100) {
                log.info("list mapId: " + mapActiveInfos.stream().map(MapActiveInfoDTO::getId).collect(Collectors.toList()));
            }
        }

        return mapActiveInfos;
    }

    private List<MapActiveInfoDTO> findByExampleForVas(MapActiveInfoDTO exampleMapActiveInfo, int mode, boolean searchCache) {
        String[] keys = new String[]{
                MapActiveInfoDTO.COLUMNS.ID.name(),
                MapActiveInfoDTO.COLUMNS.PAY_TYPE.name(),
                MapActiveInfoDTO.COLUMNS.ACTION_CODE.name(),
                MapActiveInfoDTO.COLUMNS.TEL_SERVICE_ID.name(),
                MapActiveInfoDTO.COLUMNS.STAFF_CODE.name(),
                MapActiveInfoDTO.COLUMNS.SHOP_CODE.name(),
                MapActiveInfoDTO.COLUMNS.CHANNEL_TYPE_ID.name(),
                MapActiveInfoDTO.COLUMNS.DISTRICT_CODE.name(),
                MapActiveInfoDTO.COLUMNS.PROVINCE_CODE.name(),
                MapActiveInfoDTO.COLUMNS.PRECINCT_CODE.name(),
                MapActiveInfoDTO.COLUMNS.TECHNOLOGY.name(),
                MapActiveInfoDTO.COLUMNS.CUSTOMER_GROUP.name(),
                MapActiveInfoDTO.COLUMNS.CUSTOMER_TYPE.name(),
                MapActiveInfoDTO.COLUMNS.SUB_GROUP.name(),
                MapActiveInfoDTO.COLUMNS.SUB_TYPE.name(),
                MapActiveInfoDTO.COLUMNS.STATION_ID.name(),
                MapActiveInfoDTO.COLUMNS.HISTORY_DATE.name(),
                MapActiveInfoDTO.COLUMNS.REG_REASON_ID.name()
        };
        return findByExampleForVas(exampleMapActiveInfo, mode, searchCache, keys, keys.length);
    }

    private List<MapActiveInfoDTO> findByExampleForVas(MapActiveInfoDTO exampleMapActiveInfo, int mode, boolean searchCache, String[] keys, int index) {
        List<MapActiveInfoDTO> mapActiveInfos;
        if (DataUtil.isNullOrEmpty(exampleMapActiveInfo.getProvinceCode())) {
            log.warn("sale.wired.connect.mapActiveInfo.provinceCode.not.set");
        }
        if (DataUtil.isNullOrEmpty(exampleMapActiveInfo.getDistrictCode())) {
            log.warn("sale.wired.connect.mapActiveInfo.districtCode.not.set");
        }
        try {
            mapActiveInfos = new ArrayList<>();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
        log.info("Lay dc " + mapActiveInfos.size() + " ban ghi map active info sau khi query elasticsearch");
        if (!DataUtil.isNullOrEmpty(mapActiveInfos)) {
            mapActiveInfos = mapActiveInfos.stream().filter(x ->
                    !DataUtil.isNullOrEmpty(x.getVasCode())
                            && DataUtil.isNullOrEmpty(x.getProductCode())).collect(Collectors.toList());
        } else {
            return mapActiveInfos;
        }

        String[] mapFilelds = Const.MAP_ACTIVE_INFO.nonElasticSearchMapFields_1();
        int[] filterModes = Const.MAP_ACTIVE_INFO.nonElasticSearchFilterModes_1();
        if (Const.MAP_ACTIVE_INFO.MODE_1 == mode) {
            mapFilelds = Const.MAP_ACTIVE_INFO.nonElasticSearchMapFields_1();
            filterModes = Const.MAP_ACTIVE_INFO.nonElasticSearchFilterModes_1();
        } else if (Const.MAP_ACTIVE_INFO.MODE_2 == mode) {
            mapFilelds = Const.MAP_ACTIVE_INFO.nonElasticSearchMapFields_2();
            filterModes = Const.MAP_ACTIVE_INFO.nonElasticSearchFilterModes_2();
        }
        for (int i = 0; i < mapFilelds.length; i++) {
            String mapField = mapFilelds[i];
            int filterMode = filterModes[i];
            String filterValue = DataUtil.safeToString(exampleMapActiveInfo.getByProperty(mapField));
            log.info("gia tri filter: " + filterValue + " filter mode: " + filterMode);
            List<MapActiveInfoDTO> tempMapActiveInfoList = new ArrayList<>();
            boolean isIncludeAll = true;
            if (filterMode == Const.MAP_ACTIVE_INFO.FILTER_MODE_ONLY_INDIVIDUAL
                    && (!Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(filterValue))) {
                for (MapActiveInfoDTO curentMapActiveInfo : mapActiveInfos) {
                    String recordValue = DataUtil.safeToString(curentMapActiveInfo.getByProperty(mapField));
                    if (!Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(recordValue)) {
                        isIncludeAll = false;
                        break;
                    }
                }
            }
            log.info("lay tat ca map_active_info " + isIncludeAll);
            for (MapActiveInfoDTO currentMapActiveInfo : mapActiveInfos) {
                String recordValue = DataUtil.safeToString(currentMapActiveInfo.getByProperty(mapField));
                if (Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(filterValue)) {
                    if (isIncludeAll || !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(recordValue)) {
                        tempMapActiveInfoList.add(currentMapActiveInfo);
                    }
                } else {

                    if (recordValue != null && recordValue.equals(filterValue)) {
                        if (isIncludeAll || !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(filterValue)) {
                            tempMapActiveInfoList.add(currentMapActiveInfo);
                        }
                    }

                    if (isIncludeAll) {
                        if (Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(recordValue)) {
                            tempMapActiveInfoList.add(currentMapActiveInfo);
                        }
                    }
                }
            }
            mapActiveInfos = tempMapActiveInfoList;
            log.info("Loc qua " + mapField + " thi con " + mapActiveInfos.size() + " ban ghi");
        }

        return mapActiveInfos;
    }

    public boolean isCheckMapActiveInfo(IsCheckMapActiveInfoRequest request) {
        if (Const.PRODUCT_OFFER_TYPE.VAS.equals(request.getProductOfferType())) {
            return checkMapActiveInfoForVas(request.getActionCode(), request.getTelServiceId());
        }
        return checkMapActiveInfo(request.getActionCode(), request.getTelServiceId());
    }

    public boolean checkMapActiveInfo(String actionCode, Long telServiceId) {
        List<String> actionCodeList = getActionCodeList(Const.OPTION_SET.ACTION_REQUIRE_MAP_ACTIVE_INFO);
        List<Long> telecomServiceIds = getTelecomServiceIds();

        boolean checkCustomActionWithService = false;
        var customActionWithService = optionSetClient.findValueByOptionSetCode(Const.OPTION_SET.CUSTOM_ACTION_WITH_SERVICE);
        if (!customActionWithService.isEmpty()) {
            for (var osvDTO : customActionWithService) {
                List<String> lstService = Arrays.stream(
                        Optional.ofNullable(osvDTO.getName()).orElse("").split(";")
                ).toList();
                if (Objects.equals(actionCode, osvDTO.getValue()) && lstService.contains(String.valueOf(telServiceId))) {
                    checkCustomActionWithService = true;
                    break;
                }
            }
        }

        return actionCodeList.contains(actionCode) || telecomServiceIds.contains(telServiceId) || checkCustomActionWithService;
    }

    public boolean checkMapActiveInfoForVas(String actionCode, Long telServiceId) {
        List<String> actionCodeList = getActionCodeList(Const.OPTION_SET.ACTION_REQUIRE_MAP_ACTIVE_INFO_FOR_VAS);
        List<Long> telecomServiceIds = getTelecomServiceIds();
        return actionCodeList.contains(actionCode) || telecomServiceIds.contains(telServiceId);
    }

    private List<String> getActionCodeList(String optionSetCode) {
        var optionSetValues = optionSetClient.findValueByOptionSetCode(optionSetCode);
        String actionCodeStr = optionSetValues.isEmpty() ? "" : optionSetValues.get(0).getValue();
        if (actionCodeStr == null || actionCodeStr.isBlank()) {
            return List.of();
        }
        return Arrays.stream(actionCodeStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private List<Long> getTelecomServiceIds() {
        return Arrays.asList(
                Const.TELECOM_SERVICE_ID.V_TRACKING,
                Const.TELECOM_SERVICE_ID.SMAS,
                Const.TELECOM_SERVICE_ID.SMSPARENT,
                Const.TELECOM_SERVICE_ID.MOBILE,
                Const.TELECOM_SERVICE_ID.HOMEPHONE,
                Const.TELECOM_SERVICE_ID.DEFAULT_VALUE_MAP_SELECT_ALL
        );
    }

    public Long getChanelTypeIdMapActiveInfo(ChanelTypeIdRequest request) {
        Long staffId = request.getStaffId();
        StaffDTO staffDTO = staffResolveHelper.resolveStaffDTO(staffId);
        return getChanelTypeIdMapActiveInfo(staffDTO);
    }

    public Long getChanelTypeIdMapActiveInfo(StaffDTO staffDTO) {
        Long chanelTypeId = staffDTO.getShopChanelTypeId();

        if (chanelTypeId == null) {
            return chanelTypeId;
        }

        if (DataUtil.safeEqual(staffDTO.getChannelTypeId(), Const.CHANNEL_TYPE.CHANNEL_TYPE_NV)) {
            return chanelTypeId;
        }

        if (!DataUtil.safeEqual(staffDTO.getChannelTypeId(), Const.CHANNEL_TYPE.CHANNEL_TYPE_NVDB)) {
            return staffDTO.getChannelTypeId();
        }

        String pointOfSale = staffDTO.getPointOfSale();
        if (DataUtil.safeEqual(pointOfSale, Const.CHANNEL_TYPE.POINT_OF_SALE_DB)) {
            return Const.CHANNEL_TYPE.CHANNEL_TYPE_DECODE_DB_POINT_OF_SALE;
        }

        if (DataUtil.safeEqual(pointOfSale, Const.CHANNEL_TYPE.POINT_OF_SALE_NVDB)) {
            return Const.CHANNEL_TYPE.CHANNEL_TYPE_DECODE_NVDB_POINT_OF_SALE;
        }
        return null;
    }

    public List<MapActiveInfoDTO> getMapActiveInfosByLevel(List<MapActiveInfoDTO> mapActiveInfos, String levelName, int mode) {
        List<MapActiveInfoDTO> subMapActiveInfos = new ArrayList<>();
        if (mapActiveInfos == null || mapActiveInfos.isEmpty()) {
            return subMapActiveInfos;
        }
        String[] mapFilelds = Const.MAP_ACTIVE_INFO.orderFields_1();
        int[] filterModes = Const.MAP_ACTIVE_INFO.filterModes_1();
        if (mode == 1) {
            mapFilelds = Const.MAP_ACTIVE_INFO.orderFields_1();
            filterModes = Const.MAP_ACTIVE_INFO.filterModes_1();
        } else if (mode == 2) {
            mapFilelds = Const.MAP_ACTIVE_INFO.orderFields_2();
            filterModes = Const.MAP_ACTIVE_INFO.filterModes_2();
        } else if (mode == 6) {
            mapFilelds = Const.MAP_ACTIVE_INFO.orderFields_6();
            filterModes = Const.MAP_ACTIVE_INFO.filterModes_6();
        }

        int maxLevel;
        for (maxLevel = 0; maxLevel < mapFilelds.length; maxLevel++) {
            if (mapFilelds[maxLevel].equals(levelName)) {
                break;
            }
        }
        subMapActiveInfos = mapActiveInfos;
        for (int j = 0; j < maxLevel + 1; j++) {

            String fieldName = mapFilelds[j];
            int fieldMode = filterModes[j];

            if (fieldMode == Const.MAP_ACTIVE_INFO.FILTER_MODE_ONLY_INDIVIDUAL) {
                List<MapActiveInfoDTO> tempListAll = new ArrayList<>();
                List<MapActiveInfoDTO> tempListIndi = new ArrayList<>();

                for (int i = 0; i < subMapActiveInfos.size(); i++) {

                    MapActiveInfoDTO mapActiveInfo = subMapActiveInfos.get(i);
                    String fieldValue = DataUtil.safeToString(mapActiveInfo.getByProperty(fieldName));

                    if (Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(fieldValue)) {
                        tempListAll.add(mapActiveInfo);
                    } else {
                        tempListIndi.add(mapActiveInfo);
                    }
                }

                if (tempListIndi.isEmpty()) {
                    subMapActiveInfos = tempListAll;
                } else {
                    subMapActiveInfos = tempListIndi;
                }

            }
        }

        if (!DataUtil.isNullOrEmpty(subMapActiveInfos)) {
            if (mapActiveInfos.size() < 100) {
                log.info("list mapId: " + subMapActiveInfos.stream().map(MapActiveInfoDTO::getId).collect(Collectors.toList()));
            }
        }
        return subMapActiveInfos;
    }

    public List<ReasonDTO> getReasonFullWithBusinessNo(StaffDTO staffDTO, String payType, Long offerId, String actionCode, String serviceType, String province, String district, String precint, String customerGroup, String customerType, String subType, String subGroup, String stationCodes, String promotionCode, String technology, Integer mode, Boolean getReasonCharUse, RequiredRoleMap roleMap, String nodeCode, Long singleOrCombo, List<FilterRequest> listProductSpec, boolean isTdd, List<String> lstBusinessNo) {
        return getReasonFullCheckOfferAndHistory(staffDTO,
                payType,
                offerId,
                actionCode,
                serviceType,
                province,
                district,
                precint,
                customerGroup,
                customerType,
                subType,
                subGroup,
                stationCodes,
                promotionCode,
                technology,
                mode,
                getReasonCharUse,
                roleMap,
                null,
                null,
                true,
                nodeCode,
                singleOrCombo,
                listProductSpec,
                isTdd,
                lstBusinessNo
        );
    }

    private List<ReasonDTO> getReasonFullCheckOfferAndHistory(StaffDTO staffDTO, String payType, Long offerId, String actionCode, String serviceType, String province, String district, String precint, String customerGroup, String customerType, String subType, String subGroup, String stationCodes, String promotionCode, String technology, Integer mode, Boolean getReasonCharUse, RequiredRoleMap roleMap, Long numOffer, Date historyDate, boolean checkStatus, String nodeCode, Long singleOrCombo, List<FilterRequest> listProductSpec, boolean isTgdd, List<String> lstBusinessNo) {
        var optionSetMap = optionSetClient.findByOptionSetCodes(
                Arrays.asList(
                        Const.OPTION_SET.CHECK_MAPPING_BY_USER_AREA,
                        "SERVICE_CHECK_PRECINT_REASON",
                        "CHECK_MAP_BUSINESS",
                        "CONFIG_MAPPING_BY_USER_AREA"
                )
        );

        if (DataUtil.notNullOrEmpty(lstBusinessNo)) {
            lstBusinessNo = lstBusinessNo.stream().map(x -> x.trim()).collect(Collectors.toList());
            for (String businessNo : lstBusinessNo) {
                if (businessNo.length() > 3500) {
                    throw new BusinessException("BCCS-POLICY-MAPACTIVE-0015", "Tham số businessNo vượt quá độ dài cho phép (3500 ký tự)");
                }
            }
        }
        String tPromotionCode = DataUtil.defaultIfNull(promotionCode, Const.DEFAULT_VALUE_MAP_SELECT_ALL);
        Long telServiceId = DataUtil.defaultIfNull(telecomServiceClient.getServiceIdByAlias(serviceType), Long.valueOf(Const.DEFAULT_VALUE_MAP_SELECT_ALL));

        List<ReasonDTO> lstReason;
        MapActiveInfoDTO mapActiveInfoExample;

        if (!checkMapActiveInfo(actionCode, telServiceId)) {
            lstReason = reasonService.getListReasonByActionCodeAndTelServiceForAuditWithMappingChecking(actionCode, telServiceId, payType, numOffer, checkStatus);
        } else {
            String custTypeId = convertCustTypeCode2Id(customerType);
            mapActiveInfoExample = getMapActiveInfoExample(staffDTO, payType, offerId, actionCode, telServiceId,
                    Long.valueOf(Const.DEFAULT_VALUE_MAP_SELECT_ALL), tPromotionCode,
                    customerGroup, custTypeId, subType, subGroup, stationCodes, technology, historyDate, nodeCode, isTgdd);

            boolean isActiveCD = (!Const.TELECOM_SERVICE_ID.MOBILE.equals(telServiceId)
                    && !Const.TELECOM_SERVICE_ID.HOMEPHONE.equals(telServiceId)
                    && !Const.TELECOM_SERVICE_ID.SMAS.equals(telServiceId)
                    && !Const.TELECOM_SERVICE_ID.SMSPARENT.equals(telServiceId));

            if (isActiveCD) {
                mapActiveInfoExample.setProvinceCode(DataUtil.toUpper(province));
                mapActiveInfoExample.setDistrictCode(DataUtil.toUpper(district));

                List<OptionSetValueResponse> lstOption = optionSetMap.getOrDefault("SERVICE_CHECK_PRECINT_REASON", Collections.emptyList());
                if (!DataUtil.isNullOrEmpty(lstOption)) {
                    List<String> lstValidService = lstOption.stream().map(OptionSetValueResponse::getValue).collect(Collectors.toList());
                    if (lstValidService.contains(DataUtil.safeToString(telServiceId))) {
                        mapActiveInfoExample.setPrecinctCode(DataUtil.toUpper(precint));
                    } else {
                        mapActiveInfoExample.setPrecinctCode(Const.DEFAULT_VALUE_MAP_SELECT_ALL);
                    }
                } else {
                    if (Const.TELECOM_SERVICE_ID.CABLE_TV.equals(telServiceId)) {
                        mapActiveInfoExample.setPrecinctCode(DataUtil.toUpper(precint));
                    } else {
                        mapActiveInfoExample.setPrecinctCode(Const.DEFAULT_VALUE_MAP_SELECT_ALL);
                    }
                }

            }

            List<OptionSetValueResponse> checkMapBusinessOptions = optionSetMap.getOrDefault("CHECK_MAP_BUSINESS", Collections.emptyList());
            if (!DataUtil.isNullOrEmpty(checkMapBusinessOptions) && DataUtil.safeEqual(checkMapBusinessOptions.get(0).getValue(), Const.STATUS.ACTIVE)) {
                if (!DataUtil.isNullOrEmpty(lstBusinessNo)) {
                    mapActiveInfoExample.setLstBusinessNo(lstBusinessNo);
                } else {
                    mapActiveInfoExample.setLstBusinessNo(Arrays.asList("-1"));
                }

            }
            //techasians: dau noi dia ban theo user
            List<OptionSetValueResponse> configMappingUserArea = optionSetMap.getOrDefault(Const.OPTION_SET.CONFIG_MAPPING_BY_USER_AREA, Collections.emptyList());

            if (DataUtil.safeEqual(mapActiveInfoExample.getChannelTypeId(), Const.CHANNEL_TYPE.CHUOI_TOAN_QUOC) && !DataUtil.isNullObject(staffDTO.getStaffId())) {
                List<OptionSetValueResponse> checkMappingByUserAreaOptions = optionSetMap.getOrDefault(Const.OPTION_SET.CHECK_MAPPING_BY_USER_AREA, Collections.emptyList());
                if (!DataUtil.isNullOrEmpty(checkMappingByUserAreaOptions) && DataUtil.safeEqual(checkMappingByUserAreaOptions.get(0).getValue(), Const.STATUS.ACTIVE)) {
                    if (mapActiveInfoValidateService.checkMappingByUser(mapActiveInfoExample, configMappingUserArea)) {
                        StaffExtResponse staffExtResponse = staffExtClient.getStaffExtByStaffIDAndKey(staffDTO.getStaffId(), Const.STAFF_EXT_KEY.MAP_AREA_CHAIN_CHANNEL);
                        if (!DataUtil.isNullObject(staffExtResponse)) {
                            //voi shop chuoi toan quoc chi tim theo dia ban tinh
                            mapActiveInfoExample.setProvinceCode(staffExtResponse.getValue());
                            mapActiveInfoExample.setDistrictCode(null);
                            mapActiveInfoExample.setPrecinctCode(null);
                        }
                    }
                }
            }

            List<MapActiveInfoDTO> mapActiveInfos = findByExample(mapActiveInfoExample, mode, false, Const.PRODUCT_OFFER_TYPE.PRODUCT_CODE, false);

            List<MapActiveInfoDTO> lstMapRemoveCheckBusinessNo = new ArrayList<>();
            List<Long> lstRemoveReasonId = new ArrayList<>();

            log.info("list reasonId from MapActiveInfo: " + mapActiveInfos.stream().map(MapActiveInfoDTO::getRegReasonId).collect(Collectors.toList()));


            lstReason = reasonService.getReasonFromMapActiveInfos(mapActiveInfos, mode, numOffer);

            if (!DataUtil.isNullOrEmpty(lstRemoveReasonId) && !DataUtil.isNullOrEmpty(lstReason)) {
                List<Long> finalLstRemoveReasonId = lstRemoveReasonId;
                lstReason = lstReason.stream().filter(x -> !finalLstRemoveReasonId.contains(x.getReasonId())).collect(Collectors.toList());
            }

        }
        log.info("list reasonId before check spec : " + lstReason.stream().map(ReasonDTO::getReasonId).collect(Collectors.toList()));
        // Loc thuoc tinh single_or_combo
        if (DataUtil.isNullObject(listProductSpec)) {
            listProductSpec = new ArrayList<>();
        }
        if (!DataUtil.isNullObject(singleOrCombo)) {
            listProductSpec.add(FilterRequest.builder()
                    .property(Const.PRODUCT_SPEC_CHAR.SINGLE_OR_COMBO)
                    .operator(FilterRequest.Operator.EQ)
                    .valueText(DataUtil.safeToString(singleOrCombo))
                    .build());
        }
        if (!DataUtil.isNullOrEmpty(listProductSpec)) {
            lstReason = reasonService.findByLstIdWithSpec(lstReason.stream().map(ReasonDTO::getReasonId).collect(Collectors.toList()), listProductSpec, null);
        }
        if (getReasonCharUse) {
            lstReason = reasonService.getReasonCharUse(lstReason);
        }
        log.info("list reasonId before return : " + lstReason.stream().map(ReasonDTO::getReasonId).toList());
        Map<Long, List<ReasonPauseDTO>> reasonPauseByReasonId = reasonService.getReasonPauseByReasonIds(
                lstReason.stream().map(ReasonDTO::getReasonId).collect(Collectors.toList()));
        for (ReasonDTO dto : lstReason) {
            List<ReasonPauseDTO> reasonPauseDTOS = reasonPauseByReasonId.getOrDefault(dto.getReasonId(), Collections.emptyList());
            dto.setListReasonPause(reasonPauseDTOS);
        }

        return lstReason;
    }

    public MapActiveInfoDTO getMapActiveInfoExample(StaffDTO staffDTO, String payType, Long offerId, String actionCode,
                                                    Long telServiceId, Long regReasonId, String promotionCode, String customerGroup, String custTypeId,
                                                    String subType, String subGroup, String stationCodes, String technology, Date historyDate, String nodeCode, boolean isTgdd) {

        MapActiveInfoDTO mapActiveInfoExample;

        // staffDTO đã được caller (ReasonService.getReasonFull) resolve đầy đủ thông tin
        // shop trước khi gọi tới đây -> không cần gọi lại staffShopClient (tránh 1 lần
        // cross-service call thừa cho mỗi request).
        if (DataUtil.isNullObject(staffDTO)) {
            throw new BusinessException("BCCS-POLICY-MAPACTIVE-0006");
        }

        Long channelTypeId = getChanelTypeIdMapActiveInfo(staffDTO);
        mapActiveInfoExample = new MapActiveInfoDTO();
        String tPayType = DataUtil.isNullOrEmpty(payType) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : payType;
        mapActiveInfoExample.setPayType(tPayType);
        mapActiveInfoExample.setActionCode(actionCode);
        mapActiveInfoExample.setStaffCode(DataUtil.toUpper(staffDTO.getStaffCode()));
        mapActiveInfoExample.setShopCode(DataUtil.toUpper(staffDTO.getShopCode()));
        mapActiveInfoExample.setProvinceCode(staffDTO.getShopProvince());
        mapActiveInfoExample.setDistrictCode(staffDTO.getShopDistrict());
        mapActiveInfoExample.setPrecinctCode(staffDTO.getShopPrecinct());
        mapActiveInfoExample.setTelServiceId(telServiceId);
        mapActiveInfoExample.setOfferId(offerId);
        mapActiveInfoExample.setChannelTypeId(channelTypeId);
        mapActiveInfoExample.setRegReasonId(regReasonId);
        mapActiveInfoExample.setPromCode(promotionCode);
        mapActiveInfoExample.setCustomerGroup(customerGroup);
        mapActiveInfoExample.setCustomerType(custTypeId);
        mapActiveInfoExample.setSubType(subType);
        mapActiveInfoExample.setSubGroup(subGroup);
        mapActiveInfoExample.setStationCodes(stationCodes);
        mapActiveInfoExample.setTechnology(technology);
        if (!isTgdd) {
            mapActiveInfoExample.setHistoryDate(historyDate);
        }
        mapActiveInfoExample.setNodeCode(nodeCode);
        return mapActiveInfoExample;
    }

    private String convertCustTypeCode2Id(String customerType) {
        return customerType == null ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : customerType;
    }
}
