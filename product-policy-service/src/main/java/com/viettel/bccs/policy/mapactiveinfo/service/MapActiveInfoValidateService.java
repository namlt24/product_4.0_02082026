package com.viettel.bccs.policy.mapactiveinfo.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.common.error.exception.SystemException;
import com.viettel.bccs.policy.client.StaffExtClient;
import com.viettel.bccs.policy.client.dto.*;
import com.viettel.bccs.policy.client.OptionSetClient;
import com.viettel.bccs.policy.client.ProductOfferingClient;
import com.viettel.bccs.policy.client.ProductOfferCharUseClient;
import com.viettel.bccs.policy.client.StaffShopClient;
import com.viettel.bccs.policy.discountpromotion.dto.response.DiscountPromotionDTO;
import com.viettel.bccs.policy.discountpromotion.service.DiscountPromotionService;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.*;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.ShopResponse;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.ValidateInputMapActiveInfoResponse;
import com.viettel.bccs.policy.discountpromotioncharuse.mapper.MapActiveInfoMapper;
import com.viettel.bccs.policy.mapactiveinfo.model.ValidationContext;
import com.viettel.bccs.policy.mapactiveinfo.repository.MapActiveInfoRepository;
import com.viettel.bccs.policy.reason.dto.response.ReasonDTO;
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
import org.springframework.context.annotation.Lazy;
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
                                        @Lazy ReasonService reasonService,
                                        @Lazy DiscountPromotionService discountPromotionService,
                                        TransactionTemplate transactionTemplate,
                                        @Qualifier("asyncExecutor") Executor asyncExecutor,
                                        MessageUtil messageUtil,
                                        @Lazy MapActiveInfoQuerryService mapActiveInfoQuerryService) {
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

    public boolean isCheckMapActiveInfo(IsCheckMapActiveInfoRequest request) {
        return mapActiveInfoQuerryService.isCheckMapActiveInfo(request);
    }

    public List<MapActiveInfoDTO> validateMapActiveInfo(String staffCode, String actionCode, List<Long> offerIds,
                                                        String promotionCode, Long regReasonId, String captchaAnswer, Long telServiceId, Date nowDate,
                                                        boolean isNeedCheckCaptcha, String province, String district, String precinct, String customerGroup,
                                                        String customerType, String subType, String subGroup, String stationCodes, String payType,
                                                        String technology, int mode, String productOfferType, List<String> lstBusinessNo) {
        // Client chỉ gửi staffCode dạng chuỗi (không phải object StaffDTO lồng nhau) -- dựng StaffDTO
        // tối thiểu ở đây; các bước bên dưới (resolveValidationContext/getUniqueMapActiveInfo) tự
        // enrich đầy đủ thông tin shop qua staffShopClient.getStaffShopFullInfo(staffCode) rồi mới dùng,
        // nên object StaffDTO đầu vào chỉ cần chứa staffCode là đủ.
        StaffDTO staffDTO = DataUtil.isNullOrEmpty(staffCode) ? null : new StaffDTO(staffCode);
        List<MapActiveInfoDTO> mapActiveInfoDTOs = new ArrayList<>();
        MapActiveInfoDTO mapActiveInfo;
        List<ReasonDTO> lstReason;
        List<DiscountPromotionDTO> lstPromotions;
        if (regReasonId == null) {
            log.info("regReasonId=null");
            throw new BusinessException("BCCS-POLICY-MAPACTIVE-0016");
        }
        boolean isCheckMapActiveInfo = mapActiveInfoQuerryService.checkMapActiveInfo(actionCode, telServiceId);


        if (!isCheckMapActiveInfo) {
            // 1. Task lấy Reason
            CompletableFuture<List<ReasonDTO>> futureReason = CompletableFuture
                    .supplyAsync(() -> {
                        log.debug("[AsyncTask] getListReason START - actionCode={}, telServiceId={}",
                                actionCode, telServiceId);
                        try {
                            List<ReasonDTO> result = transactionTemplate.execute(status ->
                                    reasonService.getListReasonByActionCodeAndTelServiceForAudit(
                                            actionCode, telServiceId, payType));
                            log.debug("[AsyncTask] getListReason END - {} results", result != null ? result.size() : 0);
                            return result;
                        } catch (Exception ex) {
                            log.error("[AsyncTask] getListReason EXCEPTION: {}", ex.getClass().getName() + ": " + ex.getMessage(), ex);
                            throw ex;
                        }
                    }, asyncExecutor)
                    .orTimeout(taskTimeoutMs, TimeUnit.MILLISECONDS);

            // 2. Task lấy Discount Promotion
            CompletableFuture<List<DiscountPromotionDTO>> futurePromotion = CompletableFuture
                    .supplyAsync(() -> {
                        log.debug("[AsyncTask] getPromotionList START - telServiceId={}", telServiceId);
                        try {
                            List<DiscountPromotionDTO> result = transactionTemplate.execute(status ->
                                    discountPromotionService.getPromotionList(telServiceId, true, true, null));
                            log.debug("[AsyncTask] getPromotionList END - {} results", result != null ? result.size() : 0);
                            return result;
                        } catch (Exception ex) {
                            log.error("[AsyncTask] getPromotionList EXCEPTION: {}", ex.getClass().getName() + ": " + ex.getMessage(), ex);
                            throw ex;
                        }
                    }, asyncExecutor)
                    .orTimeout(taskTimeoutMs, TimeUnit.MILLISECONDS);

            // 3. Chờ cả 2 task hoàn thành với Timeout tổng
            CompletableFuture.allOf(futureReason, futurePromotion).join();

            lstReason = futureReason.join();
            lstPromotions = futurePromotion.join();

            log.info("[validateWithOutMapActiveInfo] Both tasks completed - reasons={}, promotions={}",
                    lstReason != null ? lstReason.size() : 0, lstPromotions != null ? lstPromotions.size() : 0);

            validateMapActiveInfoCommon(actionCode, promotionCode, regReasonId,
                    telServiceId, lstReason, lstPromotions);
        } else {
            /*
            Từ offerIds lấy ra List<ProductOfferingDTO> gọi cross service sang productcatalog
             */
            if (!DataUtil.isNullOrEmpty(offerIds)) {
                List<ProductOfferingDTO> lstProductOffering = productOfferingClient.findByIds(offerIds);
                Map<Long, ProductOfferingDTO> mapProductOfferingById = lstProductOffering.stream()
                        .collect(Collectors.toMap(ProductOfferingDTO::getProductOfferingId, p -> p, (a, b) -> a));
                // Các thông tin này không đổi theo từng offerId trong 1 request (staff/shop, option set,
                // channelType, tỉnh/huyện/phường suy ra từ shop) -> resolve 1 lần duy nhất thay vì gọi lại
                // cross-service cho mỗi offerId trong vòng lặp bên dưới.
                ValidationContext context = staffDTO != null
                        ? resolveValidationContext(staffDTO, actionCode, payType, telServiceId, province, district, precinct)
                        : new ValidationContext(Collections.emptyMap(), null, null, null, Const.DEFAULT_VALUE_MAP_SELECT_ALL);
                for (Long offerId : offerIds) {
                    lstReason = new ArrayList<>();
                    lstPromotions = new ArrayList<>();
                    String custTypeId = convertCustTypeCode2Id(customerType);
                    StringBuilder errMsgNonMapField = new StringBuilder();
                    mapActiveInfo = getUniqueMapActiveInfo(staffDTO, actionCode, offerId,
                            DataUtil.isNullOrEmpty(promotionCode) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : promotionCode,
                            regReasonId, telServiceId,
                            customerGroup, custTypeId, subType, subGroup, stationCodes, errMsgNonMapField, payType, technology, mode, productOfferType,
                            lstBusinessNo, mapProductOfferingById.get(offerId), context);
                    if (mapActiveInfo != null) {
                        List<MapActiveInfoDTO> lstMapActiveInfo = new ArrayList<>();
                        lstMapActiveInfo.add(mapActiveInfo);

                        lstReason.addAll(reasonService.getReasonFromMapActiveInfos(lstMapActiveInfo, mode, null));

                        lstPromotions.addAll(discountPromotionService.getPromFromMapActiveInfos(lstMapActiveInfo, mode, false));
                        mapActiveInfoDTOs.add(mapActiveInfo);

                    } else {
                        if (!errMsgNonMapField.toString().isEmpty()) {
                            throw new BusinessException("BCCS-POLICY-MAPACTIVE-0005");
                        }
                        ProductOfferingDTO productOfferingDTO = mapProductOfferingById.get(offerId);
                        String offerCodeForMsg = productOfferingDTO != null ? productOfferingDTO.getCode() : String.valueOf(offerId);
                        String textParam = messageUtil.getTextParam("BCCS-POLICY-MAPACTIVE-0002", offerCodeForMsg);
                        throw new BusinessException("BCCS-POLICY-MAPACTIVE-0002", textParam);
                    }
                    if (mapActiveInfo != null) {
                        validateMapActiveInfoCommon(actionCode,
                                promotionCode,
                                regReasonId,
                                telServiceId, lstReason, lstPromotions);
                    }
                }
            } else {
                log.info("offerId=null");
                throw new BusinessException("BCCS-POLICY-MAPACTIVE-0017");
            }
        }
        return mapActiveInfoDTOs;
    }


    private ValidationContext resolveValidationContext(StaffDTO staffDTO, String actionCode, String payType,
                                                       Long telServiceId, String province, String district, String precinct) {
        StaffResponse staffResponse = staffShopClient.getStaffShopFullInfo(staffDTO.getStaffCode());
        if (staffResponse != null) {
            if (staffResponse.getShop() != null) {
                ShopResponse shop = staffResponse.getShop();
                staffDTO.setShopId(shop.getShopId());
                staffDTO.setShopName(shop.getName());
                staffDTO.setShopCode(shop.getShopCode());
                staffDTO.setShopChanelTypeId(shop.getChannelTypeId());
                staffDTO.setShopProvince(shop.getProvince());
                staffDTO.setShopDistrict(shop.getDistrict());
                staffDTO.setShopPrecinct(shop.getPrecinct());
                staffDTO.setShopPath(shop.getShopPath());
                staffDTO.setAreaCode(shop.getAreaCode());
            }
        }
        var optionSetMap = optionSetClient.findByOptionSetCodes(
                Arrays.asList(
                        OPTION_SET.CHECK_MAP_BUSINESS_PRODUCT,
                        OPTION_SET.CONFIG_MAPPING_BY_USER_AREA,
                        OPTION_SET.CHECK_MAPPING_BY_USER_AREA,
                        OPTION_SET.OFFER_FILTER_MODE,
                        OPTION_SET.ACTION_CODE_ALLOW_OFFER_FILTER_MODE
                )
        );

        if (staffDTO.getShopId() == null) {
            return new ValidationContext(optionSetMap, null, null, null, Const.DEFAULT_VALUE_MAP_SELECT_ALL);
        }

        List<OptionSetValueResponse> configMappingUserArea = optionSetMap.getOrDefault(OPTION_SET.CONFIG_MAPPING_BY_USER_AREA, Collections.emptyList());
        List<OptionSetValueResponse> checkMapArea = optionSetMap.getOrDefault(OPTION_SET.CHECK_MAPPING_BY_USER_AREA, Collections.emptyList());

        boolean isActiveCD = true
                && (!Const.TELECOM_SERVICE_ID.MOBILE.equals(telServiceId)
                && !Const.TELECOM_SERVICE_ID.HOMEPHONE.equals(telServiceId)
                && !Const.TELECOM_SERVICE_ID.SMAS.equals(telServiceId)
                && !Const.TELECOM_SERVICE_ID.SMSPARENT.equals(telServiceId));

        String provinceID;
        String districtID;
        String precintId = Const.DEFAULT_VALUE_MAP_SELECT_ALL;
        if (!isActiveCD) {
            provinceID = staffDTO.getShopProvince();
            districtID = staffDTO.getShopDistrict();
        } else {
            provinceID = province;
            districtID = district;
            precintId = precinct;
        }

        Long chanelTypeId = getChanelTypeIdMapActiveInfo(staffDTO);

        if (DataUtil.safeEqual(chanelTypeId, Const.CHANNEL_TYPE.CHUOI_TOAN_QUOC) && !DataUtil.isNullObject(staffDTO.getStaffId())) {
            boolean isCheckMapAreaActive = !DataUtil.isNullOrEmpty(checkMapArea) && DataUtil.safeEqual(checkMapArea.get(0).getValue(), Const.STATUS.ACTIVE);
            if (isCheckMapAreaActive) {
                MapActiveInfoDTO userAreaCheckContext = new MapActiveInfoDTO();
                userAreaCheckContext.setActionCode(actionCode);
                userAreaCheckContext.setTelServiceId(telServiceId);
                userAreaCheckContext.setPayType(payType);
                userAreaCheckContext.setShopCode(staffDTO.getShopCode());
                if (checkMappingByUser(userAreaCheckContext, configMappingUserArea)) {
                    StaffExtResponse staffExtResponse = staffExtClient.getStaffExtByStaffIDAndKey(staffDTO.getStaffId(), Const.STAFF_EXT_KEY.MAP_AREA_CHAIN_CHANNEL);
                    if (!DataUtil.isNullObject(staffExtResponse)) {
                        provinceID = staffExtResponse.getValue();
                        districtID = null;
                        precintId = null;
                    }
                }
            }
        }

        return new ValidationContext(optionSetMap, chanelTypeId, provinceID, districtID, precintId);
    }

    private MapActiveInfoDTO getUniqueMapActiveInfo(StaffDTO staffDTO, String actionCode, Long offerId,
                                                    String promotionCode, Long regReasonId, Long telServiceId,
                                                    String customerGroup, String custTypeId, String subType,
                                                    String subGroup, String stationCodes, StringBuilder errMsgNonMapField,
                                                    String payType, String technology, int mode,
                                                    String productOfferType, List<String> lstBusinessNo, ProductOfferingDTO productOfferingDTO,
                                                    ValidationContext context) {
        if (staffDTO == null || staffDTO.getShopId() == null) {
            return null;
        }

        Map<String, List<OptionSetValueResponse>> optionSetMap = context.optionSetMap();
        List<OptionSetValueResponse> checkMapBusiness = optionSetMap.getOrDefault(OPTION_SET.CHECK_MAP_BUSINESS_PRODUCT, Collections.emptyList());

        String provinceID = context.provinceID();
        String districtID = context.districtID();
        String precintId = context.precintId();
        Long chanelTypeId = context.chanelTypeId();

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

        mapActiveInfoExample.setChannelTypeId(chanelTypeId);
        mapActiveInfoExample.setRegReasonId(regReasonId);
        mapActiveInfoExample.setTelServiceId(telServiceId);
        mapActiveInfoExample.setPromCode(promotionCode);
        mapActiveInfoExample.setOfferId(offerId);

        mapActiveInfoExample.setCustomerGroup(customerGroup);
        mapActiveInfoExample.setCustomerType(custTypeId);
        mapActiveInfoExample.setSubType(subType);
        mapActiveInfoExample.setSubGroup(subGroup);
        mapActiveInfoExample.setStationCodes(stationCodes);
        mapActiveInfoExample.setPayType(payType);
        mapActiveInfoExample.setTechnology(technology);

        if (!DataUtil.isNullOrEmpty(checkMapBusiness) && DataUtil.safeEqual(checkMapBusiness.get(0).getValue(), Const.STATUS.ACTIVE)) {
            mapActiveInfoExample.setLstBusinessNo(lstBusinessNo);
        }

        List<MapActiveInfoDTO> mapActiveInfos = findByExampleWithOfferType(mapActiveInfoExample, mode, productOfferType);

        if ((DataUtil.safeEqual(mapActiveInfos.size(), 0))
                && (!DataUtil.isNullOrEmpty(lstBusinessNo) && (lstBusinessNo.size() != 1
                || (!DataUtil.safeEqual(lstBusinessNo.get(0), Const.DEFAULT_VALUE_MAP_SELECT_ALL))))) {
            if (!DataUtil.isNullOrEmpty(checkMapBusiness) && DataUtil.safeEqual(checkMapBusiness.get(0).getValue(), Const.STATUS.ACTIVE)) {
                String businessNos = String.join(" , ", lstBusinessNo);
                String textParam = messageUtil.getTextParam("BCCS-POLICY-MAPACTIVE-0001", businessNos);
                throw new BusinessException("BCCS-POLICY-MAPACTIVE-0001", textParam);
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

                //lay thong tin ly do
                String reasonCode = "";
                if (regReasonId != null && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(String.valueOf(regReasonId))) {
                    try {
                        ReasonResponse reasonResponse = reasonService.findById(regReasonId);
                        reasonCode = reasonResponse != null ? reasonResponse.reasonCode() : "";
                    } catch (BusinessException e) {
                        log.warn("Khong tim thay reason voi regReasonId={}", regReasonId);
                    }
                }
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
                String offerCodeForMsg = productOfferingDTO != null ? productOfferingDTO.getCode() : null;
                String textParam;
                String errorCodeForVas;
                if (!DataUtil.isNullOrEmpty(promotionCode) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(promotionCode)) {
                    errorCodeForVas = "BCCS-POLICY-MAPACTIVE-0003";
                    textParam = messageUtil.getTextParam(errorCodeForVas, areaName, reasonCode, promotionCode, offerCodeForMsg);
                } else {
                    errorCodeForVas = "BCCS-POLICY-MAPACTIVE-0004";
                    textParam = messageUtil.getTextParam(errorCodeForVas, areaName, reasonCode, offerCodeForMsg);
                }
                throw new BusinessException(errorCodeForVas, textParam);
            } else {
                log.info("vas khong khai bao thong tin, cho phep thuc hien");
                return null;
            }
        }
        return null;
    }

    public void validateMapActiveInfoCommon(String actionCode, String promotionCode, Long regReasonId,
                                            Long telServiceId, List<ReasonDTO> lstReason,
                                            List<DiscountPromotionDTO> lstPromotions) {
        log.debug("[validateMapActiveInfoCommon] regReasonId={}, isActiveCD={}, promotionCode={}",
                regReasonId, !TELECOM_SERVICE_ID.MOBILE.equals(telServiceId)
                        && !TELECOM_SERVICE_ID.HOMEPHONE.equals(telServiceId)
                        && !TELECOM_SERVICE_ID.SMAS.equals(telServiceId)
                        && !TELECOM_SERVICE_ID.SMSPARENT.equals(telServiceId),
                promotionCode);

        if (lstReason == null || lstReason.isEmpty()) {
            throw new BusinessException("BCCS-POLICY-MAPACTIVE-0018");
        }
        boolean wrongReason = lstReason.stream()
                .noneMatch(r -> Objects.equals(regReasonId, r.getReasonId()));
        if (wrongReason) {
            throw new BusinessException("BCCS-POLICY-MAPACTIVE-0018");
        }

        boolean isActiveCD = !TELECOM_SERVICE_ID.MOBILE.equals(telServiceId)
                && !TELECOM_SERVICE_ID.HOMEPHONE.equals(telServiceId)
                && !TELECOM_SERVICE_ID.SMAS.equals(telServiceId)
                && !TELECOM_SERVICE_ID.SMSPARENT.equals(telServiceId);

        boolean wrongPromotions;
        if (isActiveCD && promotionCode != null
                && !promotionCode.isBlank()
                && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(promotionCode)) {
            if (lstPromotions == null || lstPromotions.isEmpty()) {
                wrongPromotions = true;
            } else {
                wrongPromotions = lstPromotions.stream()
                        .noneMatch(p -> promotionCode.equals(p.getCode()));
            }
        } else {
            wrongPromotions = false;
        }

        if (wrongPromotions) {
            throw new BusinessException("BCCS-POLICY-MAPACTIVE-0019");
        }
    }

    private String convertCustTypeCode2Id(String customerType) {
        return customerType == null ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : customerType;
    }

    public Long getChanelTypeIdMapActiveInfo(StaffDTO staffDTO) {
        return mapActiveInfoQuerryService.getChanelTypeIdMapActiveInfo(staffDTO);
    }

    public boolean checkMappingByUser(MapActiveInfoDTO mapActiveInfoDTO, List<OptionSetValueResponse> options) {
        if (DataUtil.isNullOrEmpty(options)) {
            return true;
        }
        for (OptionSetValueResponse valueDTO : options) {
            if (DataUtil.isNullOrEmpty(valueDTO.getValue())) {
                return false;
            }
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
                    errMsg.append(messageUtil.getText("BCCS-POLICY-MAPACTIVE-0005"));
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
            //Sua compareTo - neu 1 trong 2 gia tri null hoac khac kieu, coi nhu bang nhau o field nay, chuyen sang field uu tien tiep theo
            if (fieldValue1 instanceof String && fieldValue2 instanceof String)
                compareResult = ((String) fieldValue2).compareTo((String) fieldValue1);
            else if (fieldValue1 instanceof Long && fieldValue2 instanceof Long)
                compareResult = ((Long) fieldValue2).compareTo((Long) fieldValue1);

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