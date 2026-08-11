package com.viettel.bccs.policy.mapactiveinfo.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.policy.client.*;
import com.viettel.bccs.policy.client.dto.*;
import com.viettel.bccs.policy.common.dto.FilterRequest;
import com.viettel.bccs.policy.common.helper.StaffResolveHelper;
import com.viettel.bccs.policy.discountpromotion.service.DiscountPromotionService;
import com.viettel.bccs.policy.discountpromotioncharuse.mapper.MapActiveInfoMapper;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.RequestMbccs;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoProductVsaleRoles;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.ShopResponse;
import com.viettel.bccs.policy.mapactiveinfo.repository.MapActiveInfoRepository;
import com.viettel.bccs.policy.reason.service.ReasonService;
import com.viettel.bccs.policy.utils.Const;
import com.viettel.bccs.policy.utils.DataUtil;
import com.viettel.bccs.policy.utils.MessageUtil;
import com.viettel.bccs.policy.utils.RequiredRoleMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class MapActiveInfoProductService {

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
    private final MessageUtil messageUtil;
    private final MapActiveInfoQuerryService mapActiveInfoQuerryService;
    private final MapActiveInfoValidateService mapActiveInfoValidateService;
    private final StaffResolveHelper staffResolveHelper;

    public MapActiveInfoProductService(MapActiveInfoRepository repository, MapActiveInfoMapper mapper,
                                       OptionSetClient optionSetClient, StaffShopClient staffShopClient,
                                       StaffExtClient staffExtClient, ProductOfferingClient productOfferingClient,
                                       ProductOfferCharUseClient productOfferCharUseClient,
                                       ReasonService reasonService,
                                       DiscountPromotionService discountPromotionService,
                                       TransactionTemplate transactionTemplate,
                                       MessageUtil messageUtil,
                                       MapActiveInfoQuerryService mapActiveInfoQuerryService,
                                       MapActiveInfoValidateService mapActiveInfoValidateService,
                                       StaffResolveHelper staffResolveHelper) {
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
        this.messageUtil = messageUtil;
        this.mapActiveInfoQuerryService = mapActiveInfoQuerryService;
        this.mapActiveInfoValidateService = mapActiveInfoValidateService;
        this.staffResolveHelper = staffResolveHelper;
    }
    // ============== getProductCodeByMapActiveInfo ==============


    public List<ProductOfferingDTO> getProductCodeByMapActiveInfo(RequestMbccs request) {
        log.info("[getProductCodeByMapActiveInfo] START - staffCode={}, payType={}, actionCode={}, telecomServiceId={}",
                request.getStaffCode(), request.getPayType(), request.getActionCode(), request.getTelecomServiceId());

        validateRequest(request);

        List<ProductOfferingDTO> products = fetchOfferingsWithSpec(request);
        MapActiveInfoProductVsaleRoles vsaleRoles = loadMapActiveInfoProductVsaleRoles();

        List<ProductOfferingDTO> lstRemove = new ArrayList<>();
        List<ProductOfferingDTO> lstAdd = new ArrayList<>();

        filterProductsByRole(products, vsaleRoles, request.getRoleMap(), lstRemove, lstAdd);

        applyRoleFilter(products, vsaleRoles, request.getRoleMap(), lstRemove, lstAdd);

        log.info("[getProductCodeByMapActiveInfo] END - {} products returned", products.size());
        return products;
    }

    private void validateRequest(RequestMbccs request) {
        DataUtil.trimValue(request);
        validateCommonProductCodeParams(request.getStaffCode(), request.getPayType(), request.getActionCode(),
                request.getTelecomServiceId(), request.getRoleMap());
    }

    /**
     * Validate các tham số bắt buộc dùng chung giữa getProductCodeByMapActiveInfo và getProductCodeNew:
     * staffCode/payType/actionCode/telecomServiceId không rỗng, roleMap có ít nhất 1 quyền hợp lệ,
     * và độ dài tối đa của từng trường.
     */
    private void validateCommonProductCodeParams(String staffCode, String payType, String actionCode,
                                                  String telecomServiceId, RequiredRoleMap roleMap) {
        validateMissingParam(staffCode, "BCCS-POLICY-MAPACTIVE-0006");
        validateMissingParam(payType, "BCCS-POLICY-MAPACTIVE-0007");
        validateMissingParam(actionCode, "BCCS-POLICY-MAPACTIVE-0008");
        validateMissingParam(telecomServiceId, "BCCS-POLICY-MAPACTIVE-0009");

        RequiredRoleMap validRoleMap = validateMissingParam(roleMap, "BCCS-POLICY-MAPACTIVE-0010");
        validateMissingParam(validRoleMap.getValues(), "BCCS-POLICY-MAPACTIVE-0010");
        validRoleMap.setValues(validRoleMap.getValues().stream().map(String::trim).collect(Collectors.toList()));
        validRoleMap.getValues().removeIf(DataUtil::isNullOrEmpty);
        validateMissingParam(validRoleMap.getValues(), "BCCS-POLICY-MAPACTIVE-0010");

        validateMaxLengthParam(staffCode, 40, "BCCS-POLICY-MAPACTIVE-0011");
        validateMaxLengthParam(payType, 1, "BCCS-POLICY-MAPACTIVE-0012");
        validateMaxLengthParam(actionCode, 10, "BCCS-POLICY-MAPACTIVE-0013");
        validateMaxLengthParam(telecomServiceId, 10, "BCCS-POLICY-MAPACTIVE-0014");
    }

    private List<ProductOfferingDTO> fetchOfferingsWithSpec(RequestMbccs request) {
        List<ProductOfferingDTO> products = getProductCodeByMapActiveInfoWithSpec(
                request.getStaffCode(),
                request.getPayType(),
                request.getActionCode(),
                request.getTelecomServiceId(),
                null,
                null,
                null,
                1,
                request.getRoleMap(),
                true,
                "200",
                request.getListProductSpec(),
                null,
                null);

        List<String> offeringFields = Arrays.asList(
                "productOfferingId", "productSpecId", "productOfferTypeId", "name", "code",
                "telecomServiceId", "description", "status", "effectDatetime", "expireDatetime",
                "checkSerial", "checkDeposit", "unit", "accountingModelCode", "accountingModelName",
                "accountingName", "accountingCode", "deviceType", "stockModelType", "ownerShopId",
                "returnStockWhenCancelled", "sapMaterialNumber", "usageId", "isBundle", "subType", "demoDuration",
                "createUser", "updateUser", "createDatetime", "updateDatetime", "lstProductSpecCharDTOs"
        );
        List<String> specCharFields = Arrays.asList(
                "productSpecCharId", "code", "name", "status", "description", "value",
                "createUser", "updateUser", "createDatetime", "updateDatetime"
        );

        for (ProductOfferingDTO product : products) {
            DataUtil.setObjectFieldsNonnull(product, offeringFields);
            if (!DataUtil.isNullOrEmpty(product.getLstProductSpecCharDTOs())) {
                for (ProductSpecCharDTO psc : product.getLstProductSpecCharDTOs()) {
                    if (psc.getProductSpecCharValueDTO() != null && psc.getProductSpecCharValueDTO().getValue() != null) {
                        psc.setValue(psc.getProductSpecCharValueDTO().getValue());
                    }
                    DataUtil.setObjectFieldsNonnull(psc, specCharFields);
                }
            }
        }
        return products;
    }

    public MapActiveInfoProductVsaleRoles loadMapActiveInfoProductVsaleRoles() {
        Map<String, List<OptionSetValueResponse>> allRoles = optionSetClient.findByOptionSetCodes(
                Arrays.asList("ON_VSALE_ROLE", "VSALE_DIDONG_M2M", "VSALE_DIDONG_GOIDACBIET", "VSALE_DIDONG_GOITHUONG"));

        OptionSetValueResponse onVsaleRole = allRoles.getOrDefault("ON_VSALE_ROLE", Collections.emptyList()).stream()
                .filter(v -> Const.STATUS.ACTIVE.equals(v.getValue()))
                .findFirst()
                .orElse(null);

        List<String> roleM2Ms = toValueList(allRoles.get("VSALE_DIDONG_M2M"));
        List<String> roleDBs = toValueList(allRoles.get("VSALE_DIDONG_GOIDACBIET"));
        List<String> roleGOITHUONGs = toValueList(allRoles.get("VSALE_DIDONG_GOITHUONG"));

        return new MapActiveInfoProductVsaleRoles(onVsaleRole, roleM2Ms, roleDBs, roleGOITHUONGs);
    }

    public List<String> toValueList(List<OptionSetValueResponse> list) {
        if (list == null) return List.of();
        return list.stream().map(OptionSetValueResponse::getValue).filter(Objects::nonNull).toList();
    }

    public void filterProductsByRole(List<ProductOfferingDTO> products, MapActiveInfoProductVsaleRoles roles,
                                     RequiredRoleMap roleMap, List<ProductOfferingDTO> lstRemove, List<ProductOfferingDTO> lstAdd) {
        if (roles.onVsaleRole() == null) return;

        for (ProductOfferingDTO product : products) {
            if (DataUtil.isNullOrEmpty(product.getLstProductSpecCharDTOs())) {
                if (!roleMap.hasListRole(roles.roleM2Ms(), Const.VSALE_ROLE.VSALE_DAUNOI_DIDONG_GOIM2M)
                        && !roleMap.hasListRole(roles.roleDBs(), Const.VSALE_ROLE.VSALE_DAUNOI_DIDONG_GOIDACBIET)
                        && !roleMap.hasListRole(roles.roleGOITHUONGs(), Const.VSALE_ROLE.VSALE_DAUNOI_DIDONG_GOITHUONG)) {
                    lstRemove.add(product);
                }
                continue;
            }

            for (ProductSpecCharDTO psc : product.getLstProductSpecCharDTOs()) {
                if (!roleMap.hasListRole(roles.roleM2Ms(), Const.VSALE_ROLE.VSALE_DAUNOI_DIDONG_GOIM2M)) {
                    if (DataUtil.safeEqual(psc.getCode(),
                            Const.PRODUCT_SPEC_CHAR.GOI_CUOC_DAC_THU,
                            Const.PRODUCT_SPEC_CHAR.SPECIFIC_PACKAGE)) {
                        lstRemove.add(product);
                        break;
                    }
                }
                if (!roleMap.hasListRole(roles.roleDBs(), Const.VSALE_ROLE.VSALE_DAUNOI_DIDONG_GOIDACBIET)) {
                    if (DataUtil.safeEqual(psc.getCode(),
                            Const.PRODUCT_SPEC_CHAR.PRODUCT_STUDENT,
                            Const.PRODUCT_SPEC_CHAR.STU_PRO,
                            Const.PRODUCT_SPEC_CHAR.SPEC_HISCL)) {
                        lstRemove.add(product);
                        break;
                    }
                }
                if (!roleMap.hasListRole(roles.roleGOITHUONGs(), Const.VSALE_ROLE.VSALE_DAUNOI_DIDONG_GOITHUONG)) {
                    if (DataUtil.safeEqual(psc.getCode(),
                            Const.PRODUCT_SPEC_CHAR.PRODUCT_STUDENT,
                            Const.PRODUCT_SPEC_CHAR.STU_PRO,
                            Const.PRODUCT_SPEC_CHAR.SPEC_HISCL,
                            Const.PRODUCT_SPEC_CHAR.GOI_CUOC_DAC_THU,
                            Const.PRODUCT_SPEC_CHAR.SPECIFIC_PACKAGE)) {
                        lstAdd.add(product);
                        break;
                    }
                }
            }
        }
    }

    private void applyRoleFilter(List<ProductOfferingDTO> products, MapActiveInfoProductVsaleRoles roles,
                                 RequiredRoleMap roleMap, List<ProductOfferingDTO> lstRemove, List<ProductOfferingDTO> lstAdd) {
        if (roles.onVsaleRole() != null) {
            if (!roleMap.hasListRole(roles.roleGOITHUONGs(), Const.VSALE_ROLE.VSALE_DAUNOI_DIDONG_GOITHUONG)) {
                products.retainAll(lstAdd);
            }
        } else {
            if (!roleMap.hasRole(Const.MDEALER_ROLE.MDEALER_DAUNOI_DIDONG_GOITHUONG)) {
                products.retainAll(lstAdd);
            }
        }
        products.removeAll(new HashSet<>(lstRemove));
    }


    public List<ProductOfferingDTO> getProductCodeByMapActiveInfoWithSpec(String staffCode, String payType, String actionCode,
                                                                          String telecomServiceId, String offerId, String changeMethod,
                                                                          String technology, int mode, RequiredRoleMap roleMap, boolean checkProductStatus,
                                                                          String productOfferTypeId, List<FilterRequest> listProductSpec,
                                                                          List<String> lstBusinessNo, Boolean mustGic) {

        StaffDTO staffDTO = staffResolveHelper.resolveStaffDTO(staffCode);

        if (!DataUtil.isNullObject(lstBusinessNo)) {
            List<String> lstBusinessNoNew = lstBusinessNo.stream()
                    .map(String::trim)
                    .collect(Collectors.toList());
            lstBusinessNo = lstBusinessNoNew;
            for (String businessNo : lstBusinessNo) {
                if (businessNo.length() > 3500) {
                    throw new BusinessException("BCCS-POLICY-MAPACTIVE-0015");
                }
            }
        }
        List<ProductOfferingDTO> productOfferingDTOS = getProductOfferingCheckStatus(staffDTO, payType, actionCode, telecomServiceId, offerId, changeMethod, technology, mode,
                roleMap, checkProductStatus, productOfferTypeId, listProductSpec, null, lstBusinessNo, mustGic);

        List<OptionSetValueResponse> listPriorityOffer = optionSetClient.findValueByOptionSetCode(Const.OPTION_SET.LIST_PRIORITY_OFFER);
        if (DataUtil.isNullOrEmpty(listPriorityOffer)) {
            return productOfferingDTOS;
        }
        Map<String, Integer> orderMap = new HashMap<>();
        String regex = "-?\\d+";
        for (OptionSetValueResponse priority : listPriorityOffer) {
            if (!DataUtil.isNullOrEmpty(priority.getValue()) && priority.getName() != null && priority.getName().matches(regex)) {
                orderMap.put(priority.getValue(), Integer.parseInt(priority.getName()));
            }
        }
        Collections.sort(productOfferingDTOS, (po1, po2) -> {
            Integer order1 = orderMap.getOrDefault(po1.getCode(), Integer.MAX_VALUE);
            Integer order2 = orderMap.getOrDefault(po2.getCode(), Integer.MAX_VALUE);
            return Integer.compare(order1, order2);
        });
        return productOfferingDTOS;
    }

    public List<ProductOfferingDTO> getProductOfferingCheckStatus(StaffDTO staffDTO,
                                                                  String payType,
                                                                  String actionCode,
                                                                  String telecomServiceId,
                                                                  String offerId,
                                                                  String changeMethod,
                                                                  String technology,
                                                                  int mode,
                                                                  RequiredRoleMap roleMap,
                                                                  boolean checkProductStatus,
                                                                  String productOfferingType,
                                                                  List<FilterRequest> listProductSpec, String infraType,
                                                                  List<String> lstBusinessNo, Boolean mustGic) {
        if (Const.PRODUCT_OFFER_TYPE.VAS.equals(productOfferingType)) {
            return getProductOfferingCheckStatusForVas(
                    staffDTO,
                    payType,
                    actionCode,
                    telecomServiceId,
                    offerId,
                    changeMethod,
                    technology,
                    mode,
                    roleMap,
                    true,
                    Const.PRODUCT_OFFER_TYPE.PRODUCT_CODE,
                    null,
                    listProductSpec
            );
        } else {
            return getProductOfferingCheckStatus(
                    staffDTO,
                    payType,
                    actionCode,
                    telecomServiceId,
                    offerId,
                    changeMethod,
                    Const.DEFAULT_VALUE_MAP_SELECT_ALL,
                    Const.DEFAULT_VALUE_MAP_SELECT_ALL,
                    Const.DEFAULT_VALUE_MAP_SELECT_ALL,
                    Const.DEFAULT_VALUE_MAP_SELECT_ALL,
                    Const.DEFAULT_VALUE_MAP_SELECT_ALL,
                    null,
                    technology,
                    mode,
                    roleMap,
                    true,
                    Const.PRODUCT_OFFER_TYPE.PRODUCT_CODE,
                    null,
                    null,
                    listProductSpec,
                    false, infraType, lstBusinessNo, mustGic
            );

        }
    }

    // ============== getProductCodeNew ==============

    public List<ProductOfferingDTO> getProductCode(RequestMbccs request) {
        log.info("[getProductCodeNew] START - staffCode={}, payType={}, actionCode={}, telecomServiceId={}",
                request.getStaffCode(), request.getPayType(), request.getActionCode(), request.getTelecomServiceId());

        validateRequest(request);

        int mode = request.getMode() != null ? request.getMode() : 0;
        String offerId = request.getOfferId() != null ? String.valueOf(request.getOfferId()) : null;
        List<ProductOfferingDTO> products = getProductCodeCheckStatus(
                request.getStaffCode(), request.getPayType(), request.getActionCode(), request.getTelecomServiceId(),
                offerId, request.getChangeMethod(), request.getTechnology(), mode, request.getRoleMap(),
                true, request.getListProductSpec(), request.getInfraType(), null);

        log.info("[getProductCodeNew] END - {} products returned", products.size());
        return products;
    }

    public List<ProductOfferingDTO> getProductCodeCheckStatus(String staffCode, String payType, String actionCode,
                                                               String telecomServiceId, String offerId, String changeMethod,
                                                               String technology, int mode, RequiredRoleMap roleMap,
                                                               boolean checkProductStatus, List<FilterRequest> listProductSpec,
                                                               String infraType, List<String> lstBusinessNo) {
        StaffDTO staffDTO = staffResolveHelper.resolveStaffDTO(staffCode);
        return getProductOfferingCheckStatus(staffDTO, payType, actionCode, telecomServiceId, offerId, changeMethod,
                technology, mode, roleMap, checkProductStatus, Const.PRODUCT_OFFER_TYPE.PRODUCT_CODE,
                listProductSpec, infraType, lstBusinessNo, null);
    }

    private List<ProductOfferingDTO> getProductOfferingCheckStatus(StaffDTO staffDTO,
                                                                   String payType,
                                                                   String actionCode,
                                                                   String telecomServiceId,
                                                                   String offerId,
                                                                   String changeMethod,
                                                                   String customerGroup,
                                                                   String customerType,
                                                                   String subType,
                                                                   String subGroup,
                                                                   String stationCodes,
                                                                   String promotionCode,
                                                                   String technology,
                                                                   int mode,
                                                                   RequiredRoleMap roleMap,
                                                                   boolean checkProductStatus,
                                                                   String productOfferingType,
                                                                   Date historyDate,
                                                                   String nodeCode,
                                                                   List<FilterRequest> listProductSpec,
                                                                   boolean isTgdd, String infraType, List<String> lstBusinessNo, Boolean mustGic) {

        if (staffDTO.getMyViettelOrder() != null && staffDTO.getMyViettelOrder() == true) {
            String defaultStaffCode = "MY_VIETTEL_CD";
            StaffResponse expStaff = null;
            expStaff = staffShopClient.getStaffShopFullInfo(staffDTO.getStaffCode());
            staffDTO = expStaff.toDTO();
            staffDTO.setMyViettelOrder(true);
        }
        List<ProductOfferingDTO> lsProductOfferCode;
        if (!DataUtil.isNullOrEmpty(offerId) && !DataUtil.isNullOrEmpty(changeMethod)) {
            lsProductOfferCode = DataUtil.defaultIfNull(productOfferingClient.getListOfferAlterStatus(DataUtil.safeToLong(offerId), changeMethod, checkProductStatus), new ArrayList<>());
        } else {
            lsProductOfferCode = DataUtil.defaultIfNull(productOfferingClient.findByTelecomSubTypeOfferTypeCheckProductStatus(DataUtil.safeToLong(telecomServiceId), payType, DataUtil.safeToLong(productOfferingType), checkProductStatus), new ArrayList<>());
        }

        List<ProductOfferingDTO> lstRemove = new ArrayList<>();
        String xgsponValue = "1";
        if (DataUtil.safeEqual(infraType, "1")) {
            xgsponValue = "0";
        }
        Map<Long, List<ProductSpecCharDTO>> mapOfferingSpecChar = new HashMap<>();

        List<Long> offeringIds = lsProductOfferCode.stream()
                .map(ProductOfferingDTO::getProductOfferingId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<Long, List<ProductSpecCharDTO>> allSpecChars =
                productOfferCharUseClient.getProductSpecCharByOfferingIds(
                        offeringIds.stream().map(String::valueOf).toList());


        for (ProductOfferingDTO x : lsProductOfferCode) {
            List<ProductSpecCharDTO> lst =
                    allSpecChars.getOrDefault(x.getProductOfferingId(), List.of());
            if (!lst.isEmpty()) {
                for (ProductSpecCharDTO psc : lst) {
                    if (DataUtil.safeEqual(psc.getCode(), Const.PRODUCT_SPEC_CHAR.IS_XGSPON)
                            && psc.getProductSpecCharValueDTO() != null
                            && DataUtil.safeEqual(psc.getProductSpecCharValueDTO().getValue(), xgsponValue)) {
                        lstRemove.add(x);
                    }
                }
                mapOfferingSpecChar.put(x.getProductOfferingId(), lst);
            }
        }

        lsProductOfferCode.removeAll(lstRemove);
        List<ProductOfferingDTO> lsProduct = new ArrayList<>();
        MapActiveInfoDTO mapActiveInfoExample;
        if (!mapActiveInfoQuerryService.checkMapActiveInfo(actionCode, Long.parseLong(telecomServiceId))) {
            lsProduct.addAll(lsProductOfferCode);
        } else {
            mapActiveInfoExample = getMapActiveInfoExample(staffDTO, payType, Long.parseLong(Const.DEFAULT_VALUE_MAP_SELECT_ALL), actionCode,
                    DataUtil.safeToLong(telecomServiceId),
                    Long.parseLong(Const.DEFAULT_VALUE_MAP_SELECT_ALL),
                    Const.DEFAULT_VALUE_MAP_SELECT_ALL,
                    customerGroup, customerType,
                    subType, subGroup, stationCodes, technology, historyDate, nodeCode, false);
            boolean isActiveCD = (!Const.TELECOM_SERVICE_ID.MOBILE.toString().equals(telecomServiceId)
                    && !Const.TELECOM_SERVICE_ID.HOMEPHONE.toString().equals(telecomServiceId)
                    && !Const.TELECOM_SERVICE_ID.SMAS.toString().equals(telecomServiceId)
                    && !Const.TELECOM_SERVICE_ID.SMSPARENT.toString().equals(telecomServiceId));

            if (isActiveCD) {
                mapActiveInfoExample.setProvinceCode(DataUtil.toUpper(staffDTO.getShopProvince()));
                mapActiveInfoExample.setDistrictCode(DataUtil.toUpper(staffDTO.getShopDistrict()));
                if (Const.TELECOM_SERVICE_ID.CABLE_TV.toString().equals(telecomServiceId)) {
                    mapActiveInfoExample.setPrecinctCode(DataUtil.toUpper(staffDTO.getShopPrecinct()));
                } else {
                    mapActiveInfoExample.setPrecinctCode(Const.DEFAULT_VALUE_MAP_SELECT_ALL);
                }
            }

            List<MapActiveInfoDTO> mapActiveInfos = mapActiveInfoValidateService.findByExampleWithOfferType(mapActiveInfoExample, mode, Const.PRODUCT_OFFER_TYPE.PRODUCT_CODE);

            lsProduct = getProductFromMapActiveInfos(mapActiveInfos, lsProductOfferCode, mode);

            if (!DataUtil.isNullObject(offerId) && DataUtil.isNullOrEmpty(changeMethod)) {
                if (!DataUtil.isNullOrEmpty(lsProduct)) {
                    lsProduct = lsProduct.stream().filter(x -> DataUtil.safeEqual(x.getProductOfferingId(), offerId)).collect(Collectors.toList());
                }
            }
        }
        String disableKey = "";
        if (Const.PAY_TYPE.PREPAID.equals(payType)) {
            disableKey = "DISABLED_PRODUCT_CODE_PRE";
        } else if (Const.PAY_TYPE.POSTPAID.equals(payType)) {
            disableKey = "DISABLED_PRODUCT_CODE_POS";
        }
        String disableProductOffer = optionSetClient.getValueByTwoCodeOption("DISABLED_OBJECTS", disableKey);
        List<String> disableProductOfferListByCode = Arrays.asList(disableProductOffer.split(",")).stream().filter(x -> !DataUtil.isNullOrEmpty(x)).map(x -> x.trim()).collect(Collectors.toList());
        if (!DataUtil.isNullOrEmpty(disableProductOfferListByCode)) {
            List<ProductOfferingDTO> productOfferingDTOs = productOfferingClient.findByCodesAndProductOfferType(disableProductOfferListByCode, Long.valueOf(Const.PRODUCT_OFFER_TYPE.PRODUCT_CODE));
            if (!DataUtil.isNullOrEmpty(productOfferingDTOs)) {
                lsProduct = filterProductOffer(lsProduct, productOfferingDTOs.stream().map(x -> String.valueOf(x.getProductOfferingId())).collect(Collectors.toList()), false);
            }

        }
        if (!DataUtil.isNullOrEmpty(listProductSpec)) {
            lsProduct = filterBySpecChars(lsProduct, allSpecChars, listProductSpec);
        }

        if (!DataUtil.isNullObject(mustGic)) {
            FilterRequest filter = new FilterRequest();
            filter.setProperty("DF_GIC");
            filter.setValueText("1");
            if (!mustGic) {
                filter.setOperator(FilterRequest.Operator.NE);
            }
            lsProduct = filterBySpecChars(lsProduct, allSpecChars, List.of(filter));
        }

        if (!DataUtil.isNullOrEmpty(lsProduct)) {
            for (ProductOfferingDTO product : lsProduct) {
                product.setDownloadSpeed(DataUtil.safeToLong(getSpecCharValue(allSpecChars, product.getProductOfferingId(), Const.PRODUCT_SPEC_CHAR.DOWNLOAD_SPEED), null));
                product.setUploadSpeed(DataUtil.safeToLong(getSpecCharValue(allSpecChars, product.getProductOfferingId(), Const.PRODUCT_SPEC_CHAR.UPLOAD_SPEED), null));
                product.setListingPrice(DataUtil.safeToLong(getSpecCharValue(allSpecChars, product.getProductOfferingId(), Const.PRODUCT_SPEC_CHAR.LISTING_PRICE), null));
                product.setLstProductSpecCharDTOs(mapOfferingSpecChar.get(product.getProductOfferingId()));
            }
        }

        if (DataUtil.safeEqual(telecomServiceId, 1L) && DataUtil.safeEqual(payType, 2)) {
            reorderList(lsProduct, Const.PRIORITIZE_CODE.TOM690_12);
        }
        if (DataUtil.safeEqual(telecomServiceId, 1L) && DataUtil.safeEqual(payType, 1)) {
            reorderList(lsProduct, Const.PRIORITIZE_CODE.POBAS_BASIC);
        }

        return lsProduct;
    }

    public List<ProductOfferingDTO> getProductOfferingCheckStatusForVas(StaffDTO staffDTO,
                                                                        String payType,
                                                                        String actionCode,
                                                                        String telecomServiceId,
                                                                        String offerId,
                                                                        String changeMethod,
                                                                        String technology,
                                                                        int mode,
                                                                        RequiredRoleMap roleMap,
                                                                        boolean checkProductStatus,
                                                                        String productOfferingType,
                                                                        Date historyDate,
                                                                        List<FilterRequest> listProductSpec){
        List<ProductOfferingDTO> lsProductOfferCode = DataUtil.defaultIfNull(productOfferingClient.findByTelecomSubTypeOfferTypeCheckProductStatus(Long.parseLong(telecomServiceId), payType, Long.parseLong(productOfferingType), checkProductStatus), new ArrayList<>());

        Map<Long, List<ProductSpecCharDTO>> mapOfferingSpecChar = new HashMap<>();

        List<Long> offeringIds = lsProductOfferCode.stream()
                .map(ProductOfferingDTO::getProductOfferingId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<Long, List<ProductSpecCharDTO>> allSpecChars =
                productOfferCharUseClient.getProductSpecCharByOfferingIds(
                        offeringIds.stream().map(String::valueOf).toList());

        for (ProductOfferingDTO x : lsProductOfferCode) {
            List<ProductSpecCharDTO> lst =
                    allSpecChars.getOrDefault(x.getProductOfferingId(), List.of());
            if (!lst.isEmpty()) {
                mapOfferingSpecChar.put(x.getProductOfferingId(), lst);
            }
        }

        List<ProductOfferingDTO> lsProduct = new ArrayList<>();
        MapActiveInfoDTO mapActiveInfoExample;
        if (!mapActiveInfoQuerryService.checkMapActiveInfo(actionCode, Long.parseLong(telecomServiceId))) {
            lsProduct.addAll(lsProductOfferCode);
        } else {
            mapActiveInfoExample = getMapActiveInfoExample(staffDTO, payType, Long.parseLong(Const.DEFAULT_VALUE_MAP_SELECT_ALL), actionCode, DataUtil.safeToLong(telecomServiceId),
                    Long.parseLong(Const.DEFAULT_VALUE_MAP_SELECT_ALL), Const.DEFAULT_VALUE_MAP_SELECT_ALL, Const.DEFAULT_VALUE_MAP_SELECT_ALL, Const.DEFAULT_VALUE_MAP_SELECT_ALL,
                    Const.DEFAULT_VALUE_MAP_SELECT_ALL, Const.DEFAULT_VALUE_MAP_SELECT_ALL, Const.DEFAULT_VALUE_MAP_SELECT_ALL, technology, historyDate, null, false);
            boolean isActiveCD = (!Const.TELECOM_SERVICE_ID.MOBILE.toString().equals(telecomServiceId)
                    && !Const.TELECOM_SERVICE_ID.HOMEPHONE.toString().equals(telecomServiceId)
                    && !Const.TELECOM_SERVICE_ID.SMAS.toString().equals(telecomServiceId)
                    && !Const.TELECOM_SERVICE_ID.SMSPARENT.toString().equals(telecomServiceId));

            if (isActiveCD) {
                mapActiveInfoExample.setProvinceCode(DataUtil.toUpper(staffDTO.getShopProvince()));
                mapActiveInfoExample.setDistrictCode(DataUtil.toUpper(staffDTO.getShopDistrict()));
                if (Const.TELECOM_SERVICE_ID.CABLE_TV.toString().equals(telecomServiceId)) {
                    mapActiveInfoExample.setPrecinctCode(DataUtil.toUpper(staffDTO.getShopPrecinct()));
                } else {
                    mapActiveInfoExample.setPrecinctCode(Const.DEFAULT_VALUE_MAP_SELECT_ALL);
                }
            }

            List<MapActiveInfoDTO> mapActiveInfos = mapActiveInfoValidateService.findByExampleWithOfferType(mapActiveInfoExample, mode, Const.PRODUCT_OFFER_TYPE.PRODUCT_CODE);

            lsProduct = getProductFromMapActiveInfos(mapActiveInfos, lsProductOfferCode, mode);
        }

        if (!DataUtil.isNullOrEmpty(listProductSpec)) {
            lsProduct = filterBySpecChars(lsProduct, allSpecChars, listProductSpec);
        }

        if (!DataUtil.isNullOrEmpty(lsProduct)) {
            for (ProductOfferingDTO product : lsProduct) {
                product.setDownloadSpeed(DataUtil.safeToLong(getSpecCharValue(allSpecChars, product.getProductOfferingId(), Const.PRODUCT_SPEC_CHAR.DOWNLOAD_SPEED), null));
                product.setUploadSpeed(DataUtil.safeToLong(getSpecCharValue(allSpecChars, product.getProductOfferingId(), Const.PRODUCT_SPEC_CHAR.UPLOAD_SPEED), null));
                product.setListingPrice(DataUtil.safeToLong(getSpecCharValue(allSpecChars, product.getProductOfferingId(), Const.PRODUCT_SPEC_CHAR.LISTING_PRICE), null));
                product.setLstProductSpecCharDTOs(mapOfferingSpecChar.get(product.getProductOfferingId()));
            }
        }

        return lsProduct;
    }

    public MapActiveInfoDTO getMapActiveInfoExample(StaffDTO staffDTO, String payType, Long offerId, String actionCode,
                                                    Long telServiceId, Long regReasonId, String promotionCode, String customerGroup, String custTypeId,
                                                    String subType, String subGroup, String stationCodes, String technology, Date historyDate, String nodeCode, boolean isTgdd) {

        if (staffDTO == null) {
            throw new BusinessException("BCCS-POLICY-MAPACTIVE-0007", "Staff info is required");
        }

        StaffResponse staffResponse = staffShopClient.getStaffShopFullInfo(staffDTO.getStaffCode());
        if (DataUtil.isNullObject(staffResponse)) {
            throw new BusinessException("BCCS-POLICY-MAPACTIVE-0006");
        }

        ShopResponse shop = staffResponse.getShop();
        if (shop == null) {
            throw new BusinessException("BCCS-POLICY-MAPACTIVE-0006", "Shop info not found for staff: " + staffDTO.getStaffCode());
        }

        Long channelTypeId = mapActiveInfoQuerryService.getChanelTypeIdMapActiveInfo(staffDTO);
        MapActiveInfoDTO mapActiveInfoExample = new MapActiveInfoDTO();
        String tPayType = DataUtil.isNullOrEmpty(payType) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : payType;
        mapActiveInfoExample.setPayType(tPayType);
        mapActiveInfoExample.setActionCode(actionCode);
        mapActiveInfoExample.setStaffCode(DataUtil.toUpper(staffDTO.getStaffCode()));
        mapActiveInfoExample.setShopCode(DataUtil.toUpper(shop.getShopCode()));
        mapActiveInfoExample.setProvinceCode(shop.getProvince());
        mapActiveInfoExample.setDistrictCode(shop.getDistrict());
        mapActiveInfoExample.setPrecinctCode(shop.getPrecinct());
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

    private List<ProductOfferingDTO> getProductFromMapActiveInfos(List<MapActiveInfoDTO> mapActiveInfos, List<ProductOfferingDTO> lsProductOfferCode, int mode) {
        List<ProductOfferingDTO> lsResult = new ArrayList<>();
        if (DataUtil.isNullOrEmpty(mapActiveInfos) || DataUtil.isNullOrEmpty(lsProductOfferCode)) {
            return lsResult;
        }
        List<MapActiveInfoDTO> mapActiveInfosByLevel = mapActiveInfoQuerryService.getMapActiveInfosByLevel(mapActiveInfos, "offerId", mode);
        if (!DataUtil.isNullOrEmpty(mapActiveInfosByLevel)) {
            Long offerIdTem;
            Long offerIdTemFull;
            if (mapActiveInfosByLevel.size() == 1 && mapActiveInfosByLevel.get(0) != null && mapActiveInfosByLevel.get(0).getOfferId() != null &&
                    mapActiveInfosByLevel.get(0).getOfferId().equals(Long.parseLong(Const.DEFAULT_VALUE_MAP_SELECT_ALL))) {
                lsProductOfferCode.forEach(productOfferingDTO -> productOfferingDTO.setAttachPromCode(mapActiveInfosByLevel.get(0).getPromCode()));
                return lsProductOfferCode;
            }

            for (ProductOfferingDTO productOfferCodeWsDTO : lsProductOfferCode) {
                offerIdTemFull = productOfferCodeWsDTO.getProductOfferingId();
                for (MapActiveInfoDTO mapActiveInfoTemp : mapActiveInfosByLevel) {
                    offerIdTem = mapActiveInfoTemp.getOfferId();
                    //Dang lay map theo level OfferId nen co ban ghi chi tiet thi khong lay ban ghi tat ca -1
                    if (offerIdTem != null && !offerIdTem.equals(Long.parseLong(Const.DEFAULT_VALUE_MAP_SELECT_ALL))
                            && offerIdTem.equals(offerIdTemFull)) {
                        //hoanglm: 290319: dinh kem them ma KM vao doi tuong goi cuoc
                        productOfferCodeWsDTO.setAttachPromCode(mapActiveInfoTemp.getPromCode());
                        productOfferCodeWsDTO.setReasonCode(mapActiveInfoTemp.getReasonName());
                        lsResult.add(productOfferCodeWsDTO);
                        break;
                    }
                }
            }
        }
        return lsResult;
    }

    private <T> T validateMissingParam(T input, String errorCode) {
        if (DataUtil.isNullOrEmpty(input)) {
            throw new BusinessException(errorCode);
        }
        return input;
    }

    private void validateMaxLengthParam(String input, int maxLength, String errorCode) {
        if (!DataUtil.isNullOrEmpty(input) && input.length() > maxLength) {
            throw new BusinessException(errorCode);
        }
    }

    private List<ProductOfferingDTO> filterBySpecChars(List<ProductOfferingDTO> products, Map<Long, List<ProductSpecCharDTO>> specCharMap, List<FilterRequest> filters) {
        if (DataUtil.isNullOrEmpty(products) || DataUtil.isNullOrEmpty(filters)) {
            return products;
        }
        return products.stream()
                .filter(product -> matchesAllSpecCharFilters(product.getProductOfferingId(), specCharMap, filters))
                .collect(Collectors.toList());
    }

    private List<String> getSpecCharValue(Map<Long, List<ProductSpecCharDTO>> specCharMap, Long offeringId, String specCharCode) {
        if (offeringId == null || DataUtil.isNullOrEmpty(specCharCode)) {
            return List.of(null);
        }
        List<ProductSpecCharDTO> specChars = specCharMap.getOrDefault(offeringId, List.of());
        List<String> values = specChars.stream()
                .filter(sc -> DataUtil.safeEqual(sc.getCode(), specCharCode))
                .map(sc -> sc.getProductSpecCharValueDTO() != null
                        ? DataUtil.safeToString(sc.getProductSpecCharValueDTO().getValue())
                        : null)
                .collect(Collectors.toList());
        if (values.isEmpty()) {
            values = new ArrayList<>();
            values.add(null);
        }
        return values;
    }

    private boolean matchesAllSpecCharFilters(Long offeringId, Map<Long, List<ProductSpecCharDTO>> specCharMap, List<FilterRequest> filters) {
        List<ProductSpecCharDTO> specChars = specCharMap.getOrDefault(offeringId, List.of());
        for (FilterRequest filter : filters) {
            ProductSpecCharDTO matched = specChars.stream()
                    .filter(sc -> DataUtil.safeEqual(sc.getCode(), filter.getProperty()))
                    .findFirst()
                    .orElse(null);
            boolean hasSpec = matched != null;
            if (!hasSpec) {
                if (filter.getOperator() == FilterRequest.Operator.NE) {
                    continue;
                }
                return false;
            }
            if (filter.getOperator() == FilterRequest.Operator.NE) {
                String filterValue = DataUtil.safeToString(filter.getValueText());
                String actualValue = matched.getProductSpecCharValueDTO() != null
                        ? DataUtil.safeToString(matched.getProductSpecCharValueDTO().getValue())
                        : null;
                if (DataUtil.safeEqual(actualValue, filterValue)) {
                    return false;
                }
            } else if (DataUtil.notNullOrEmpty(filter.getValueText())) {
                String filterValue = DataUtil.safeToString(filter.getValueText());
                String actualValue = matched.getProductSpecCharValueDTO() != null
                        ? DataUtil.safeToString(matched.getProductSpecCharValueDTO().getValue())
                        : null;
                if (!DataUtil.safeEqual(actualValue, filterValue)) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<ProductOfferingDTO> filterProductOffer(List<ProductOfferingDTO> lsProduct, List<String> lsProductFilter, boolean isAllowOffer) {
        List<ProductOfferingDTO> allowList = new ArrayList<>();
        List<ProductOfferingDTO> removeList = new ArrayList<>();
        for (ProductOfferingDTO productOffer : lsProduct) {
            if (productOffer == null) {
                break;
            }
            for (String offerId : lsProductFilter) {
                if (productOffer.getProductOfferingId() != null && offerId.equals(productOffer.getProductOfferingId().toString())) {
                    if (isAllowOffer) {
                        allowList.add(productOffer);
                    } else {
                        removeList.add(productOffer);
                    }
                    break;
                }
            }
        }
        if (isAllowOffer) {
            return allowList;
        }

        lsProduct.removeAll(removeList);
        return lsProduct;
    }

    public static void reorderList(List<ProductOfferingDTO> list, String productCode) {
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            if (DataUtil.safeEqual(list.get(i).getCode(), productCode)) {
                ProductOfferingDTO temp = list.get(i);
                list.set(i, list.get(index));
                list.set(index, temp);
                index++;
            }
        }
    }
}
