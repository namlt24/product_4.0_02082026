package com.viettel.bccs.policy.mapactiveinfo.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.common.error.exception.SystemException;
import com.viettel.bccs.policy.client.StaffExtClient;
import com.viettel.bccs.policy.client.dto.OptionSetValueResponse;
import com.viettel.bccs.policy.client.dto.StaffExtResponse;
import com.viettel.bccs.policy.client.dto.StaffResponse;
import com.viettel.bccs.policy.client.OptionSetClient;
import com.viettel.bccs.policy.client.ProductOfferingClient;
import com.viettel.bccs.policy.client.ProductOfferCharUseClient;
import com.viettel.bccs.policy.client.StaffShopClient;
import com.viettel.bccs.policy.discountpromotion.dto.response.DiscountPromotionResponse;
import com.viettel.bccs.policy.discountpromotion.service.DiscountPromotionService;
import com.viettel.bccs.policy.exception.LogicException;
import com.viettel.bccs.policy.client.dto.StaffDTO;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.*;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.ShopResponse;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.ValidateInputMapActiveInfoResponse;
import com.viettel.bccs.policy.discountpromotioncharuse.mapper.MapActiveInfoMapper;
import com.viettel.bccs.policy.mapactiveinfo.repository.MapActiveInfoRepository;
import com.viettel.bccs.policy.reason.dto.response.ReasonResponse;
import com.viettel.bccs.policy.reason.service.ReasonService;
import com.viettel.bccs.policy.utils.Const;
import com.viettel.bccs.policy.utils.Const.OPTION_SET;
import com.viettel.bccs.policy.utils.Const.PRODUCT_OFFER_TYPE;
import com.viettel.bccs.policy.utils.Const.TELECOM_SERVICE_ID;
import com.viettel.bccs.policy.utils.DataUtil;
import com.viettel.bccs.policy.utils.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class MapActiveInfoValidateService {

    private final MapActiveInfoRepository repository;
    private final MapActiveInfoMapper mapper;
    private final OptionSetClient optionSetClient;
    private final StaffShopClient staffShopClient;
    private final StaffExtClient staffExtClient;
    private final ProductOfferingClient productOfferingClient;
    private final ProductOfferCharUseClient productOfferCharUseClient;
    private final ReasonService reasonService;
    private final DiscountPromotionService discountPromotionService;
    private final TransactionTemplate transactionTemplate;
    private final Executor asyncExecutor;
    private final MessageUtil messageUtil;
    private final MapActiveInfoQuerryService mapActiveInfoQuerryService;

    public MapActiveInfoValidateService(MapActiveInfoRepository repository, MapActiveInfoMapper mapper,
                                        OptionSetClient optionSetClient, StaffShopClient staffShopClient,
                                        StaffExtClient staffExtClient, ProductOfferingClient productOfferingClient,
                                        ProductOfferCharUseClient productOfferCharUseClient,
                                        ReasonService reasonService,
                                        DiscountPromotionService discountPromotionService,
                                        TransactionTemplate transactionTemplate,
                                        @Qualifier("asyncExecutor") Executor asyncExecutor,
                                        MessageUtil messageUtil,
                                        MapActiveInfoQuerryService mapActiveInfoQuerryService) {
        this.repository = repository;
        this.mapper = mapper;
        this.optionSetClient = optionSetClient;
        this.staffShopClient = staffShopClient;
        this.staffExtClient = staffExtClient;
        this.productOfferingClient = productOfferingClient;
        this.productOfferCharUseClient = productOfferCharUseClient;
        this.reasonService = reasonService;
        this.discountPromotionService = discountPromotionService;
        this.transactionTemplate = transactionTemplate;
        this.asyncExecutor = asyncExecutor;
        this.messageUtil = messageUtil;
        this.mapActiveInfoQuerryService = mapActiveInfoQuerryService;
    }

    @Value("${app.async.task-timeout-ms:5000}")
    private long taskTimeoutMs;

    @Value("${app.async.total-timeout-ms:10000}")
    private long totalTimeoutMs;

    public ValidateInputMapActiveInfoResponse validateInputMapActiveInfo(ValidateInputMapActiveInfoRequest request) {
        List<String> lstBusinessNo = request.getLstBusinessNo();
        boolean isDefaultValue = false;

        if (lstBusinessNo != null && !lstBusinessNo.isEmpty()) {
            lstBusinessNo = lstBusinessNo.stream()
                    .map(String::trim)
                    .toList();
            for (String businessNo : lstBusinessNo) {
                if (businessNo.length() > 3500) {
                    throw new BusinessException("BCCS-POLICY-002", "Business number length must not exceed 3500 characters");
                }
            }
        } else {
            lstBusinessNo = Collections.singletonList("-1");
            isDefaultValue = true;
        }

        if (request.getRegReasonId() == null) {
            throw new BusinessException("BCCS-POLICY-003", "regReasonId is required");
        }

        if (request.getStaffDTO() == null) {
            throw new BusinessException("BCCS-POLICY-007", "Staff info is required");
        }

        if (!DataUtil.isNullOrEmpty(request.getStaffDTO().getStaffCode())) {
            StaffResponse staffResponse = staffShopClient.getStaffShopFullInfo(request.getStaffDTO().getStaffCode());
            if (staffResponse != null) {
                if (staffResponse.getShop() != null) {
                    ShopResponse shop = staffResponse.getShop();
                    request.getStaffDTO().setShopId(shop.getShopId());
                    request.getStaffDTO().setShopName(shop.getName());
                    request.getStaffDTO().setShopCode(shop.getShopCode());
                    request.getStaffDTO().setShopChanelTypeId(shop.getChannelTypeId());
                    request.getStaffDTO().setShopProvince(shop.getProvince());
                    request.getStaffDTO().setShopDistrict(shop.getDistrict());
                    request.getStaffDTO().setShopPrecinct(shop.getPrecinct());
                    request.getStaffDTO().setShopPath(shop.getShopPath());
                    request.getStaffDTO().setAreaCode(shop.getAreaCode());
                }
            } else {
                throw new BusinessException("ERROR-STAFF-SERVICE", "Error call service staff");
            }
        }

        request.setLstBusinessNo(lstBusinessNo);

        return new ValidateInputMapActiveInfoResponse(request);
    }

    public boolean isCheckMapActiveInfo(IsCheckMapActiveInfoRequest request) {
        return mapActiveInfoQuerryService.isCheckMapActiveInfo(request);
    }

    public void validateWithOutMapActiveInfo(ValidateInputMapActiveInfoRequest request) {
        log.info("[validateWithOutMapActiveInfo] START - actionCode={}, telServiceId={}, payType={}",
                request.getActionCode(), request.getTelServiceId(), request.getPayType());

        // 1. Task lấy Reason
        CompletableFuture<List<ReasonResponse>> futureReason = CompletableFuture
                .supplyAsync(() -> {
                    log.debug("[AsyncTask] getListReason START - actionCode={}, telServiceId={}",
                            request.getActionCode(), request.getTelServiceId());
                    try {
                        List<ReasonResponse> result = transactionTemplate.execute(status ->
                                reasonService.getListReasonByActionCodeAndTelServiceForAudit(
                                        request.getActionCode(), request.getTelServiceId(), request.getPayType()));
                        log.debug("[AsyncTask] getListReason END - {} results", result != null ? result.size() : 0);
                        return result;
                    } catch (Exception ex) {
                        log.error("[AsyncTask] getListReason EXCEPTION: {}", ex.getClass().getName() + ": " + ex.getMessage(), ex);
                        throw ex;
                    }
                }, asyncExecutor)
                .orTimeout(taskTimeoutMs, TimeUnit.MILLISECONDS);

        // 2. Task lấy Discount Promotion
        CompletableFuture<List<DiscountPromotionResponse>> futurePromotion = CompletableFuture
                .supplyAsync(() -> {
                    log.debug("[AsyncTask] getPromotionList START - telServiceId={}", request.getTelServiceId());
                    try {
                        List<DiscountPromotionResponse> result = transactionTemplate.execute(status ->
                                discountPromotionService.getPromotionList(request.getTelServiceId(), true, true, null));
                        log.debug("[AsyncTask] getPromotionList END - {} results", result != null ? result.size() : 0);
                        return result;
                    } catch (Exception ex) {
                        log.error("[AsyncTask] getPromotionList EXCEPTION: {}", ex.getClass().getName() + ": " + ex.getMessage(), ex);
                        throw ex;
                    }
                }, asyncExecutor)
                .orTimeout(taskTimeoutMs, TimeUnit.MILLISECONDS);

        // 3. Chờ cả 2 task hoàn thành với Timeout tổng
        try {
            CompletableFuture.allOf(futureReason, futurePromotion).join();

            List<ReasonResponse> reasons = futureReason.join();
            List<DiscountPromotionResponse> promotions = futurePromotion.join();

            log.info("[validateWithOutMapActiveInfo] Both tasks completed - reasons={}, promotions={}",
                    reasons != null ? reasons.size() : 0, promotions != null ? promotions.size() : 0);

            validateMapActiveInfoCommon(request, reasons, promotions);
            log.info("[validateWithOutMapActiveInfo] END OK");

        } catch (java.util.concurrent.CompletionException e) {
            Throwable cause = e.getCause();
            log.error("[validateWithOutMapActiveInfo] CompletionException - cause={}: {}",
                    cause != null ? cause.getClass().getName() : "null", cause != null ? cause.getMessage() : "null", e);
            throw new SystemException("Lỗi hệ thống trong quá trình kiểm tra thông tin", e);

        } catch (Exception e) {
            log.error("[validateWithOutMapActiveInfo] Unexpected exception: {}: {}",
                    e.getClass().getName(), e.getMessage(), e);
            throw e;
        }
    }

    public void validateMapActiveInfo(ValidateInputMapActiveInfoRequest request) throws LogicException, Exception{
        log.info("[validateMapActiveInfo] START - actionCode={}, telServiceId={}, payType={}",
                request.getActionCode(), request.getTelServiceId(), request.getPayType());

        MapActiveInfoDTO mapActiveInfo = new MapActiveInfoDTO();

        if (!DataUtil.isNullOrEmpty(request.getOfferIds())) {
            List<ReasonDTO> lstReason = new ArrayList<>();
            List<DiscountPromotionDTO> lstPromotions = new ArrayList<>();
            String custTypeId = convertCustTypeCode2Id(request.getCustomerType());
            StringBuilder errMsgNonMapField = new StringBuilder();
            mapActiveInfo = getUniqueMapActiveInfo(request.getStaffDTO(), request.getActionCode(),
                    request.getOfferIds() != null && !request.getOfferIds().isEmpty() ? request.getOfferIds().get(0) : null,
                    DataUtil.isNullOrEmpty(request.getPromotionCode()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : request.getPromotionCode(),
                    request.getRegReasonId(), request.getTelServiceId(),
                    request.getProvince(), request.getDistrict(), request.getPrecinct(),
                    request.getCustomerGroup(), custTypeId, request.getSubType(),
                    request.getSubGroup(), request.getStationCodes(), errMsgNonMapField,
                    request.getPayType(), request.getTechnology(), request.getMode(), request.getProductOfferType(),
                    request.getLstBusinessNo());
        } else {
            log.info("offerId=null");
            throw new BusinessException("BCCS-POLICY-006",
                    "sale.wired.connect.mapActiveInfo.reason.not.map.offerId");
        }
    }

    private MapActiveInfoDTO getUniqueMapActiveInfo(StaffDTO staffDTO, String actionCode, Long offerId,
                                                    String promotionCode, Long regReasonId, Long telServiceId,
                                                    String province, String district, String precinct,
                                                    String customerGroup, String custTypeId, String subType,
                                                    String subGroup, String stationCodes, StringBuilder errMsgNonMapField,
                                                    String payType, String technology, int mode,
                                                    String productOfferType, List<String> lstBusinessNo) throws LogicException {
        var optionSetMap = optionSetClient.findByOptionSetCodes(
                Arrays.asList(
                        OPTION_SET.CHECK_MAP_BUSINESS_PRODUCT,
                        OPTION_SET.CONFIG_MAPPING_BY_USER_AREA,
                        OPTION_SET.CHECK_MAPPING_BY_USER_AREA,
                        OPTION_SET.OFFER_FILTER_MODE,
                        OPTION_SET.ACTION_CODE_ALLOW_OFFER_FILTER_MODE
                )
        );
        List<OptionSetValueResponse> checkMapBusiness = optionSetMap.getOrDefault(OPTION_SET.CHECK_MAP_BUSINESS_PRODUCT, Collections.emptyList());
        List<OptionSetValueResponse> configMappingUserArea = optionSetMap.getOrDefault(OPTION_SET.CONFIG_MAPPING_BY_USER_AREA, Collections.emptyList());
        List<OptionSetValueResponse> checkMapArea = optionSetMap.getOrDefault(OPTION_SET.CHECK_MAPPING_BY_USER_AREA, Collections.emptyList());

        String provinceID;
        String districtID;
        String precintId = Const.DEFAULT_VALUE_MAP_SELECT_ALL;
        Long chanelTypeId;

        if (staffDTO.getShopId() == null) {
            return null;
        }

        boolean isActiveCD = true
                && (!Const.TELECOM_SERVICE_ID.MOBILE.equals(telServiceId)
                && !Const.TELECOM_SERVICE_ID.HOMEPHONE.equals(telServiceId)
                && !Const.TELECOM_SERVICE_ID.SMAS.equals(telServiceId)
                && !Const.TELECOM_SERVICE_ID.SMSPARENT.equals(telServiceId));

        if (!isActiveCD) {
            provinceID = staffDTO.getShopProvince();
            districtID = staffDTO.getShopDistrict();
        } else {
            provinceID = province;
            districtID = district;
            precintId = precinct;
        }

        chanelTypeId = getChanelTypeIdMapActiveInfo(staffDTO);

        MapActiveInfoDTO mapActiveInfoExample = new MapActiveInfoDTO();
        mapActiveInfoExample.setActionCode(actionCode);
        mapActiveInfoExample.setStaffCode(DataUtil.toUpper(staffDTO.getStaffCode()));
        mapActiveInfoExample.setShopCode(staffDTO.getShopCode());
        mapActiveInfoExample.setProvinceCode(provinceID);
        mapActiveInfoExample.setDistrictCode(districtID);
        if (Const.TELECOM_SERVICE_ID.CABLE_TV.equals(telServiceId)
                || Const.TELECOM_SERVICE_ID.INTERNET_EOC.equals(telServiceId)) {
            mapActiveInfoExample.setPrecinctCode(precintId);
        } else {
            mapActiveInfoExample.setPrecinctCode(Const.DEFAULT_VALUE_MAP_SELECT_ALL);
        }

        mapActiveInfoExample.setOfferId(offerId);
        mapActiveInfoExample.setChannelTypeId(chanelTypeId);
        mapActiveInfoExample.setRegReasonId(regReasonId);
        mapActiveInfoExample.setTelServiceId(telServiceId);
        mapActiveInfoExample.setPromCode(promotionCode);

        mapActiveInfoExample.setCustomerGroup(customerGroup);
        mapActiveInfoExample.setCustomerType(custTypeId);
        mapActiveInfoExample.setSubType(subType);
        mapActiveInfoExample.setSubGroup(subGroup);
        mapActiveInfoExample.setStationCodes(stationCodes);
        mapActiveInfoExample.setPayType(payType);
        mapActiveInfoExample.setTechnology(technology);

        if (DataUtil.safeEqual(checkMapBusiness.get(0).getValue(), Const.STATUS.ACTIVE)) {
            mapActiveInfoExample.setLstBusinessNo(lstBusinessNo);
        }

        if (DataUtil.safeEqual(mapActiveInfoExample.getChannelTypeId(), Const.CHANNEL_TYPE.CHUOI_TOAN_QUOC) && !DataUtil.isNullObject(staffDTO.getStaffId())) {
            boolean isCheckMapAreaActive = !DataUtil.isNullOrEmpty(checkMapArea) && DataUtil.safeEqual(checkMapArea.get(0).getValue(), Const.STATUS.ACTIVE);
            if (isCheckMapAreaActive) {
                if (checkMappingByUser(mapActiveInfoExample, configMappingUserArea)) {
                    StaffExtResponse staffExtResponse = staffExtClient.getStaffExtByStaffIDAndKey(staffDTO.getStaffId(), Const.STAFF_EXT_KEY.MAP_AREA_CHAIN_CHANNEL);
                    if (!DataUtil.isNullObject(staffExtResponse)) {
                        mapActiveInfoExample.setProvinceCode(staffExtResponse.getValue());
                        mapActiveInfoExample.setDistrictCode(null);
                        mapActiveInfoExample.setPrecinctCode(null);
                    }
                }
            }
        }

        List<MapActiveInfoDTO> mapActiveInfos = findByExampleWithOfferType(mapActiveInfoExample, mode, productOfferType);

        if ((DataUtil.safeEqual(mapActiveInfos.size(), 0))
                && (!DataUtil.isNullOrEmpty(lstBusinessNo) && (lstBusinessNo.size() != 1
                || (!DataUtil.safeEqual(lstBusinessNo.get(0), Const.DEFAULT_VALUE_MAP_SELECT_ALL))))) {
            if (!DataUtil.isNullOrEmpty(checkMapBusiness) && DataUtil.safeEqual(checkMapBusiness.get(0).getValue(), Const.STATUS.ACTIVE)) {
                String businessNos = String.join(" , ", lstBusinessNo);
                String textParam = messageUtil.getTextParam("BCCS-POLICY-MAPACTIVE-001", businessNos);
                throw new LogicException("", textParam);
            }
        }

        if (Const.PRODUCT_OFFER_TYPE.VAS.equals(productOfferType)) {
            mapActiveInfos = getMapActiveInfosByLevelForVas(mapActiveInfos, "promCode", mode);
        } else {
            mapActiveInfos = getMapActiveInfosByLevel(mapActiveInfos, "promCode", mode, optionSetMap);
        }
        mapActiveInfos = filterNonMapFields(errMsgNonMapField, mapActiveInfos, mapActiveInfoExample);

        if (!DataUtil.isNullOrEmpty(mapActiveInfos)) {
            Collections.sort(mapActiveInfos, (s1, s2) -> compareMapActiveInfoDTO(s1, s2, mode));
            MapActiveInfoDTO mapActiveInfo = mapActiveInfos.get(0);
            log.info("getUniqueMapActiveInfo: id=" + mapActiveInfo.getId());
            return mapActiveInfo;
        }
        log.info("getUniqueMapActiveInfo: khong tim thay mapping");

        if (Const.PRODUCT_OFFER_TYPE.VAS.equals(productOfferType)) {
            //kiem tra xem VAS khong khai bao hoac khai bao sai
            String[] keys = new String[]{MapActiveInfoDTO.COLUMNS.ID.name(),
                    MapActiveInfoDTO.COLUMNS.PAY_TYPE.name(),
                    MapActiveInfoDTO.COLUMNS.ACTION_CODE.name(),
                    MapActiveInfoDTO.COLUMNS.TEL_SERVICE_ID.name(),
                    MapActiveInfoDTO.COLUMNS.OFFER_ID.name()};
//            mapActiveInfos = ElasticSearchController.doSearch(Const.ElasticSearch_CORE.MAP_ACTIVE_INFO, buildQueryElasticSearchForVas(mapActiveInfoExample, keys, keys.length), MapActiveInfoDTO.class, Const.UNLIMIT, "REG_REASON_ID", "ASC");
            mapActiveInfos = new ArrayList<>();
            log.info("Lay dc " + mapActiveInfos.size() + " ban ghi map active info sau khi thuc hien kiem tra lai");
            if (!DataUtil.isNullOrEmpty(mapActiveInfos)) {
                mapActiveInfos = mapActiveInfos.stream().filter(x ->
                        !DataUtil.isNullOrEmpty(x.getVasCode())
                                && DataUtil.isNullOrEmpty(x.getProductCode())).collect(Collectors.toList());
            }
            if (!DataUtil.isNullOrEmpty(mapActiveInfos)) {
                log.info("vas co khai bao thong tin, nhung khong phu hop voi dieu kien gui len, khong cho phep thuc hien");
//                ProductOfferingDTO productOfferingDTO = null;
//                try {
//                    productOfferingDTO = productOfferingService.findOne(offerId);
//                } catch (Exception e) {
//                    log.error(e.getMessage(), e);
//                    throw new LogicException(SALE_WIRED_CONNECT_MAP_ACTIVE_INFO_REASON_NOT_MAP_ACTION, messageUtil.getTextParam("BCCS-POLICY-MAPACTIVE-002", offerId));
//                }
                //kiem tra lai cac thong tin truoc khi tra thong bao ve cho nguoi dung
                //lay thong tin ly do
//                String reasonCode = "";
//                if (regReasonId != null && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(String.valueOf(regReasonId))) {
//                    ReasonDTO reasonDTO = reasonService.findOne(regReasonId);
//                    if (reasonDTO != null) {
//                        reasonCode = reasonDTO.getReasonCode();
//                    }
//                }
                //lay thong tin dia ban
                String areaName = "";
                StringBuilder areaCode = new StringBuilder();
                if (provinceID != null) {
                    areaCode.append(provinceID);
                    if (districtID != null) {
                        areaCode.append(districtID);
                        if (precintId != null) {
                            areaCode.append(precintId);
                        }
                    }
                }
//                if (areaCode.length() > 0) {
//                    AreaDTO areaDTO = areaService.findByAreaCode(areaCode.toString());
//                    if (areaDTO != null) {
//                        areaName = areaDTO.getName();
//                    }
//                }
//                String textParam;
//                if (!DataUtil.isNullOrEmpty(promotionCode) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(promotionCode)) {
//                    textParam = messageUtil.getTextParam("BCCS-POLICY-MAPACTIVE-003", areaName, reasonCode, promotionCode, productOfferingDTO.getCode());
//                } else {
//                    textParam = messageUtil.getTextParam("BCCS-POLICY-MAPACTIVE-004", areaName, reasonCode, productOfferingDTO.getCode());
//                }
//                throw new LogicException(VAS_MAPPING_INVALID, textParam);
            } else {
                log.info("vas khong khai bao thong tin, cho phep thuc hien");
                throw new LogicException("", null);
            }
        }
        return null;
    }

    public void validateMapActiveInfoCommon(ValidateInputMapActiveInfoRequest request,
                                            List<ReasonResponse> lstReason,
                                            List<DiscountPromotionResponse> lstPromotions) {
        log.debug("[validateMapActiveInfoCommon] regReasonId={}, isActiveCD={}, promotionCode={}",
                request.getRegReasonId(), !TELECOM_SERVICE_ID.MOBILE.equals(request.getTelServiceId())
                        && !TELECOM_SERVICE_ID.HOMEPHONE.equals(request.getTelServiceId())
                        && !TELECOM_SERVICE_ID.SMAS.equals(request.getTelServiceId())
                        && !TELECOM_SERVICE_ID.SMSPARENT.equals(request.getTelServiceId()),
                request.getPromotionCode());

        if (lstReason == null || lstReason.isEmpty()) {
            throw new BusinessException("BCCS-POLICY-004",
                    "regReasonId is not mapped with action");
        }
        boolean wrongReason = lstReason.stream()
                .noneMatch(r -> Objects.equals(request.getRegReasonId(), r.reasonId()));
        if (wrongReason) {
            throw new BusinessException("BCCS-POLICY-004",
                    "regReasonId is not mapped with action");
        }

        boolean isActiveCD = !TELECOM_SERVICE_ID.MOBILE.equals(request.getTelServiceId())
                && !TELECOM_SERVICE_ID.HOMEPHONE.equals(request.getTelServiceId())
                && !TELECOM_SERVICE_ID.SMAS.equals(request.getTelServiceId())
                && !TELECOM_SERVICE_ID.SMSPARENT.equals(request.getTelServiceId());

        boolean wrongPromotions;
        if (isActiveCD && request.getPromotionCode() != null
                && !request.getPromotionCode().isBlank()
                && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(request.getPromotionCode())) {
            if (lstPromotions == null || lstPromotions.isEmpty()) {
                wrongPromotions = true;
            } else {
                wrongPromotions = lstPromotions.stream()
                        .noneMatch(p -> request.getPromotionCode().equals(p.code()));
            }
        } else {
            wrongPromotions = false;
        }

        if (wrongPromotions) {
            throw new BusinessException("BCCS-POLICY-005",
                    "sale.wired.connect.custOrderDetail.promotion.not.map.action");
        }
    }

    private String convertCustTypeCode2Id(String customerType) {
        return customerType == null ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : customerType;
    }

    public Long getChanelTypeIdMapActiveInfo(StaffDTO staffDTO) {
        return mapActiveInfoQuerryService.getChanelTypeIdMapActiveInfo(staffDTO);
    }

    private boolean checkMappingByUser(MapActiveInfoDTO mapActiveInfoDTO, List<OptionSetValueResponse> options) {
        if (DataUtil.isNullOrEmpty(options)) {
            return true;
        }
        for (OptionSetValueResponse valueDTO : options) {
            List<String> lstValue = Arrays.asList(valueDTO.getValue().split(";"));
            if (lstValue.size() != 4) {
                return false;
            }
            String actionCode = lstValue.get(0);
            String telecomServiceId = lstValue.get(1);
            String payType = lstValue.get(2);
            String shopCode = lstValue.get(3);
            if (checkMappingContainCofig(actionCode, mapActiveInfoDTO.getActionCode())
                    && checkMappingContainCofig(telecomServiceId, mapActiveInfoDTO.getTelServiceId())
                    && checkMappingContainCofig(payType, mapActiveInfoDTO.getPayType())
                    && checkMappingContainCofig(shopCode, mapActiveInfoDTO.getShopCode())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkMappingContainCofig(Object valueDb, Object valueParam) {
        if (DataUtil.isNullObject(valueParam)) {
            return true;
        }
        if (DataUtil.safeEqual(valueDb, Const.DEFAULT_ALL) || DataUtil.safeEqual(valueParam, Const.DEFAULT_ALL)) {
            return true;
        }
        if (DataUtil.safeEqual(valueDb, valueParam)) {
            return true;
        }
        return false;
    }

    private List<MapActiveInfoDTO> getMapActiveInfosByLevelForVas(List<MapActiveInfoDTO> mapActiveInfos, String levelName, int mode) {
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
        return subMapActiveInfos;
    }

    public List<MapActiveInfoDTO> getMapActiveInfosByLevel(List<MapActiveInfoDTO> mapActiveInfos, String levelName, int mode, Map<String, List<OptionSetValueResponse>> optionSetMap) {
        List<OptionSetValueResponse> offerfiltermode = optionSetMap.getOrDefault(OPTION_SET.OFFER_FILTER_MODE, Collections.emptyList());
        List<OptionSetValueResponse> actionCodeAllowOfferFilterMode = optionSetMap.getOrDefault(OPTION_SET.ACTION_CODE_ALLOW_OFFER_FILTER_MODE, Collections.emptyList());

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

        if (!DataUtil.isNullOrEmpty(offerfiltermode) && DataUtil.safeEqual(offerfiltermode.get(0).getValue(), Const.STATUS.ACTIVE)) {
            if (mapActiveInfos.get(0) != null
                    && !DataUtil.isNullOrEmpty(mapActiveInfos.get(0).getActionCode())
                    && actionCodeAllowOfferFilterMode.stream().anyMatch(v -> Objects.equals(v.getValue(), mapActiveInfos.get(0).getActionCode()))) {
                for (int i = 0; i < mapFilelds.length; i++) {
                    if (mapFilelds[i].equals("offerId") || mapFilelds[i].equals("channelTypeId") || mapFilelds[i].equals("provinceCode") || mapFilelds[i].equals("districtCode") || mapFilelds[i].equals("precinctCode")) {
                        filterModes[i] = 0;
                    }
                }
            }
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

    private List<MapActiveInfoDTO> filterNonMapFields(StringBuilder errMsg, List<MapActiveInfoDTO> mapActiveInfos, MapActiveInfoDTO mapActiveInfoExample) {
        List<MapActiveInfoDTO> subMapActiveInfos = new ArrayList<>();
        if (DataUtil.isNullOrEmpty(mapActiveInfos)) {
            return subMapActiveInfos;
        }
        subMapActiveInfos = mapActiveInfos;
        for (int i = 0; i < Const.MAP_ACTIVE_INFO.nonMapFields().length; i++) {

            //Thuc hien loc theo tung field
            String nonMapField = Const.MAP_ACTIVE_INFO.nonMapFields()[i];
            int filterMode = Const.MAP_ACTIVE_INFO.filterModes3()[i];
            String searchFieldValue = DataUtil.safeToString(mapActiveInfoExample.getByProperty(nonMapField));

            if (filterMode == Const.MAP_ACTIVE_INFO.FILTER_MODE_ONLY_INDIVIDUAL && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(searchFieldValue)) {
                List<MapActiveInfoDTO> tempListAll = new ArrayList<>();
                List<MapActiveInfoDTO> tempListIndi = new ArrayList<>();

                for (MapActiveInfoDTO mapActiveInfo : subMapActiveInfos) {

                    String fieldValue = DataUtil.safeToString(mapActiveInfo.getByProperty(nonMapField));
                    fieldValue = StringUtils.removeEnd(fieldValue, Const.ATTRIBUTE_PARAM_SPLIT);

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

            if (!Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(searchFieldValue)) {
                List<MapActiveInfoDTO> tempList = new ArrayList<>();
                for (MapActiveInfoDTO mapActiveInfo : subMapActiveInfos) {
                    String fieldValue = DataUtil.safeToString(mapActiveInfo.getByProperty(nonMapField));
                    fieldValue = StringUtils.removeEnd(fieldValue, Const.ATTRIBUTE_PARAM_SPLIT);

                    if (Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(fieldValue)) {
                        tempList.add(mapActiveInfo);
                    } else {
                        if ((Const.ATTRIBUTE_PARAM_SPLIT + fieldValue + Const.ATTRIBUTE_PARAM_SPLIT)
                                .contains(Const.ATTRIBUTE_PARAM_SPLIT + searchFieldValue + Const.ATTRIBUTE_PARAM_SPLIT)) {
                            tempList.add(mapActiveInfo);
                        }
                    }
                }
                subMapActiveInfos = tempList;
                if (tempList.isEmpty() && Const.MAP_ACTIVE_INFO.STATION_CODES.equals(nonMapField)) {
                    errMsg.append(messageUtil.getText("BCCS-POLICY-MAPACTIVE-005"));
                    break;
                }

            }
        }
        return subMapActiveInfos;
    }

    public static int compareMapActiveInfoDTO(MapActiveInfoDTO mapActiveInfo1, MapActiveInfoDTO mapActiveInfo2, int mode) {
        String[] mapFilelds = Const.MAP_ACTIVE_INFO.orderFields_1();

        if (mode == 1) {
            mapFilelds = Const.MAP_ACTIVE_INFO.orderFields_1();

        } else if (mode == 2) {
            mapFilelds = Const.MAP_ACTIVE_INFO.orderFields_2();

        }
        for (String mapField : mapFilelds) {

            Object fieldValue1 = mapActiveInfo1.getByProperty(mapField);
            Object fieldValue2 = mapActiveInfo2.getByProperty(mapField);

            int compareResult = 0;
            //Sua compareTo
            if (fieldValue1 instanceof String) compareResult = ((String) fieldValue2).compareTo((String) fieldValue1);
            else if (fieldValue1 instanceof Long) compareResult = ((Long) fieldValue2).compareTo((Long) fieldValue1);

            if (compareResult != 0) {
                return compareResult;
            }
        }

        return 0;
    }

    public List<MapActiveInfoDTO> findByExampleWithOfferType(MapActiveInfoDTO exampleMapActiveInfo, int mode, String offerType) {
        return mapActiveInfoQuerryService.findByExampleWithOfferType(exampleMapActiveInfo, mode, offerType);
    }

}