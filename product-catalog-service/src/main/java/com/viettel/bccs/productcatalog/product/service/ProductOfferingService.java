package com.viettel.bccs.productcatalog.product.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.productcatalog.utils.RequestValidator;
import com.viettel.bccs.productcatalog.common.dto.FilterRequest;
import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetValueResponse;
import com.viettel.bccs.productcatalog.optionset.service.OptionSetValueService;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingDTO;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingResponse;
import com.viettel.bccs.productcatalog.product.dto.response.StockOfferingRow;
import com.viettel.bccs.productcatalog.product.dto.response.SubTypeDTO;
import com.viettel.bccs.productcatalog.product.entity.ProductOfferingEntity;
import com.viettel.bccs.productcatalog.product.mapper.ProductOfferingMapper;
import com.viettel.bccs.productcatalog.product.repository.ProductOfferingRepository;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.mapper.ProductSpecCharUseMapper;
import com.viettel.bccs.productcatalog.productoffercharuse.service.ProductOfferCharUseService;
import com.viettel.bccs.productcatalog.product.dto.request.CheckProductAttByRuleTypeRequest;
import com.viettel.bccs.productcatalog.product.dto.response.CheckProductAttByRuleTypeResponse;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productofferrelation.dto.response.ProductOfferRelationResponse;
import com.viettel.bccs.productcatalog.productofferrelation.service.ProductOfferRelationService;
import com.viettel.bccs.productcatalog.productspecchar.service.ProductSpecCharService;
import com.viettel.bccs.productcatalog.utils.Const;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductOfferingService {

    private static final String RULE_CHECK_PACKAGE_ELIGIBILITY = "CHECK_PACKAGE_ELIGIBILITY";

    private final ProductOfferingRepository productOfferingRepository;
    private final ProductOfferingMapper productOfferingMapper;
    private final ProductOfferRelationService productOfferRelationService;
    private final ProductOfferCharUseService productOfferCharUseService;
    private final OptionSetValueService optionSetValueService;
    private final ProductSpecCharService productSpecCharService;
    private final ProductSpecCharUseMapper productSpecCharUseMapper;

    @Cacheable(value = "productOfferingCache", key = "'ID:' + #productOfferingId")
    public ProductOfferingDTO findById(Long productOfferingId) {
        return productOfferingRepository.findById(productOfferingId)
                .map(productOfferingMapper::toDto)
                .orElse(null);
    }

    public ProductOfferingResponse getByProductCode(String productCode) {
        RequestValidator.requireNotBlank(productCode, "productCode", "BCCS-PRODUCT-VALIDATE-0000");
        return productOfferingRepository.findFirstByCode(productCode)
                .map(productOfferingMapper::toResponse)
                .orElseThrow(() -> new BusinessException("BCCS-CATALOG-PRODUCT-0001", "Product not found with code: " + productCode));
    }

    public SubTypeDTO getSubTypeByProductCode(String productCode) {
        if (DataUtil.isNullOrEmpty(productCode)) {
            throw new BusinessException("BCCS-CATALOG-PRODUCT-0008", "Mã mặt hàng không được để trống");
        }
        ProductOfferingResponse response = productOfferingRepository.findFirstByCode(productCode)
                .map(productOfferingMapper::toResponse)
                .orElseThrow(() -> new BusinessException("BCCS-CATALOG-PRODUCT-0009",
                        "Mặt hàng " + productCode + " không hợp lệ"));
        String subType = response.subType();
        return new SubTypeDTO(resolveSubTypeName(subType), subType);
    }

    private String resolveSubTypeName(String subType) {
        if (Const.SUB_TYPE.POST.equals(subType)) {
            return "Trả sau";
        }
        if (Const.SUB_TYPE.PRE.equals(subType)) {
            return "Trả trước";
        }
        return null;
    }

    @Cacheable(value = "productOfferingCache", key = "'OFFER_ALTER:' + #offerId + ':' + '' + #changeChannel + ':' + #checkStatus")
    public List<ProductOfferingDTO> getListOfferAlterStatus(Long offerId, String changeChannel, boolean checkStatus) {
        RequestValidator.requireNotNull(offerId, "offerId", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotBlank(changeChannel, "changeChannel", "BCCS-PRODUCT-VALIDATE-0000");
        return productOfferingRepository.getListOfferAlterStatus(offerId, changeChannel, checkStatus).stream()
                .map(productOfferingMapper::toDto)
                .toList();
    }

    @Cacheable(value = "productOfferingCache", key = "'TELECOM_SUB_TYPE:' + #telecomServiceId + ':' + '' + #subType + ':' + #offerTypeId + ':' + #getActiveProduct")
    public List<ProductOfferingDTO> findByTelecomSubTypeOfferTypeCheckProductStatus(Long telecomServiceId, String subType, Long offerTypeId, boolean getActiveProduct) {
        return productOfferingRepository.findByTelecomSubTypeOfferTypeCheckProductStatus(telecomServiceId, subType, offerTypeId, getActiveProduct).stream()
                .map(productOfferingMapper::toDto)
                .toList();
    }

    @Cacheable(value = "productOfferingCache", key = "'TELECOM_SUB_TYPE_ACTIVE:' + #telecomServiceId + ':' + '' + #subType + ':' + #offerTypeId")
    public List<ProductOfferingDTO> findByTelecomSubTypeOfferType(Long telecomServiceId, String subType, Long offerTypeId) {
        return findByTelecomSubTypeOfferTypeCheckProductStatus(telecomServiceId, subType, offerTypeId, true);
    }

    public List<ProductOfferingDTO> findByPayTypeWithSpec(String telecomServiceId, String payType, String productOfferTypeId, List<FilterRequest> listProductSpec) {
        RequestValidator.requireNotBlank(payType, "payType", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotBlank(productOfferTypeId, "productOfferTypeId", "BCCS-PRODUCT-VALIDATE-0000");
        if (DataUtil.isAnyNull(payType, productOfferTypeId)) {
            throw new BusinessException("BCCS-CATALOG-PRODUCT-0002", "payType and productOfferTypeId are required");
        }
        validateOperators(listProductSpec);
        return productOfferingRepository.findByPayTypeWithSpec(telecomServiceId, payType, productOfferTypeId, listProductSpec).stream()
                .map(productOfferingMapper::toDto)
                .toList();
    }

    private void validateOperators(List<FilterRequest> listProductSpec) {
        if (DataUtil.isNullOrEmpty(listProductSpec)) {
            return;
        }
        for (FilterRequest filterRequest : listProductSpec) {
            validateProperty(filterRequest.getProperty());
            FilterRequest.Operator operator = filterRequest.getOperator();
            if (operator == null) {
                continue;
            }
            boolean supported = false;
            for (FilterRequest.Operator candidate : FilterRequest.Operator.values()) {
                if (candidate == operator) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                throw new BusinessException("BCCS-CATALOG-PRODUCT-0006",
                        "operator " + operator + " không hợp lệ, chỉ chấp nhận một trong: "
                                + DataUtil.safeToString(FilterRequest.Operator.values()));
            }
        }
    }

    private void validateProperty(String property) {
        if (DataUtil.isNullOrEmpty(property)) {
            return;
        }
        if (!property.matches("^[A-Za-z0-9_-]+$")) {
            throw new BusinessException("BCCS-CATALOG-PRODUCT-0007",
                    "property không hợp lệ, chỉ gồm chữ, số, '_' hoặc '-': " + property);
        }
    }

    @Cacheable(value = "productOfferingCache", key = "'CODE_OR_ID:' + #proOfferId + ':' + #prodOfferCode + ':' + #status")
    public List<ProductOfferingDTO> findByCodeOrId(Long proOfferId, String prodOfferCode, String status) {
        if (DataUtil.isNullOrEmpty(proOfferId) && DataUtil.isNullOrEmpty(prodOfferCode)) {
            return List.of();
        }
        return productOfferingRepository.findByCodeOrId(proOfferId, prodOfferCode, status).stream()
                .map(productOfferingMapper::toDto)
                .toList();
    }

    @Cacheable(value = "productOfferingCache", key = "'CODES_OFFER_TYPE:' + T(String).join(',', #codes.stream().sorted().toList()) + ':' + #productOfferTypeId")
    public List<ProductOfferingDTO> findByCodesAndProductOfferType(List<String> codes, Long productOfferTypeId) {
        RequestValidator.requireNotEmpty(codes, "codes", "BCCS-PRODUCT-VALIDATE-0000");
        return productOfferingRepository.findByCodesAndProductOfferType(codes, productOfferTypeId).stream()
                .map(productOfferingMapper::toDto)
                .toList();
    }

    public List<ProductOfferingDTO> findByIds(List<Long> offerIds) {
        RequestValidator.requireNotEmpty(offerIds, "offerIds", "BCCS-PRODUCT-VALIDATE-0000");
        if (DataUtil.isNullOrEmpty(offerIds)) {
            return List.of();
        }
        return productOfferingRepository.findAllById(offerIds).stream()
                .map(productOfferingMapper::toDto)
                .toList();
    }

    public boolean checkAttProductOrVasByCode(String productCode, String productType, String attributeCode) {
        RequestValidator.requireNotBlank(productCode, "productCode", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotBlank(productType, "productType", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotBlank(attributeCode, "attributeCode", "BCCS-PRODUCT-VALIDATE-0000");
        if (DataUtil.isAnyNull(productCode, productType, attributeCode)) {
            throw new BusinessException("BCCS-CATALOG-PRODUCT-0003", "productCode, productType and attributeCode are required");
        }
        return productOfferingRepository.checkAttProductOrVasByCode(productCode, Long.valueOf(productType.trim()), attributeCode);
    }

    public boolean hasProductAtt(Long offerId, String attributeCode) {
        RequestValidator.requireNotBlank(attributeCode, "attributeCode", "BCCS-PRODUCT-VALIDATE-0000");
        if (DataUtil.isAnyNull(offerId, attributeCode)) {
            throw new BusinessException("BCCS-CATALOG-PRODUCT-0004", "offerId and attributeCode are required");
        }
        return productOfferingRepository.hasProductAtt(offerId, attributeCode);
    }


    @Cacheable(value = "productOfferingCache", key = "'SPEC_CHARS:' + T(String).join(',', #specCodes.stream().sorted().toList()) + ':' + #productOfferTypeId")
    public List<ProductOfferingDTO> getListProductOfferingBySpecChars(List<String> specCodes, Long productOfferTypeId) {
        RequestValidator.requireNotEmpty(specCodes, "specCodes", "BCCS-PRODUCT-VALIDATE-0000");
        if (DataUtil.isNullOrEmpty(specCodes)) {
            throw new BusinessException("BCCS-CATALOG-PRODUCT-0005", "specCodes must not be empty");
        }
        return productOfferingRepository.findBySpecCharCodes(specCodes, productOfferTypeId).stream()
                .map(productOfferingMapper::toDto)
                .toList();
    }

    public CheckProductAttByRuleTypeResponse checkProductAttByRuleType(CheckProductAttByRuleTypeRequest request) {
        RequestValidator.requireNotNull(request, "request", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotBlank(request.getProductCode(), "productCode", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotBlank(request.getRuleType(), "ruleType", "BCCS-PRODUCT-VALIDATE-0000");
        if (request == null || DataUtil.isNullOrEmpty(request.getProductCode())
                || DataUtil.isNullOrEmpty(request.getRuleType())) {
            throw new BusinessException("BCCS-CATALOG-PRODUCT-0005", "productCode and ruleType are required");
        }
        if (!RULE_CHECK_PACKAGE_ELIGIBILITY.equalsIgnoreCase(request.getRuleType().trim())) {
            throw new BusinessException("BCCS-CATALOG-PRODUCT-0005", "Unsupported rule type: " + request.getRuleType());
        }

        ProductOfferingEntity offering = productOfferingRepository.findFirstByCode(request.getProductCode())
                .orElseThrow(() -> new BusinessException("BCCS-CATALOG-PRODUCT-0001",
                        "Package not found with code: " + request.getProductCode()));

        Set<String> specCharCodes = resolveSpecCharCodes(offering.getProductOfferingId());

        return CheckProductAttByRuleTypeResponse.evaluate(offering.getTelecomServiceId(), specCharCodes);
    }

    private Set<String> resolveSpecCharCodes(Long productOfferingId) {
        if (productOfferingId == null) {
            return Collections.emptySet();
        }
        Map<Long, List<ProductSpecCharDTO>> byOffering =
                productOfferCharUseService.getProductSpecCharByOfferingIds(List.of(String.valueOf(productOfferingId)));
        List<ProductSpecCharDTO> chars = byOffering.get(productOfferingId);
        if (DataUtil.isNullOrEmpty(chars)) {
            return Collections.emptySet();
        }
        Set<String> result = new HashSet<>();
        for (ProductSpecCharDTO dto : chars) {
            if (dto != null && dto.getCode() != null) {
                result.add(dto.getCode().toUpperCase());
            }
        }
        return result;
    }

    @Cacheable(value = "productOfferingCache", key = "'LIST_VAS:' + #offerId + ':' + #type")
    public List<ProductOfferingDTO> getListVas(Long offerId, Integer type) {
        RequestValidator.requireNotNull(offerId, "offerId", "BCCS-PRODUCT-VALIDATE-0000");
        return getListVasCore(offerId, type);
    }

    public List<StockOfferingRow> getListStockModelBySaleServiceCode(String saleServiceCode) {
        return productOfferingRepository.getListStockModelBySaleServiceCode(saleServiceCode).stream()
                .map(productOfferingMapper::toStockOfferingRow)
                .toList();
    }


    private List<ProductOfferingDTO> getListVasCore(Long offerId, Integer type) {

        List<ProductOfferingDTO> lst = productOfferingRepository.getListVas(offerId, type).stream()
                .map(productOfferingMapper::toDto)
                .collect(Collectors.toCollection(ArrayList::new));
        if (DataUtil.isNullOrEmpty(lst)) {
            return null;
        }

        List<ProductOfferRelationResponse> mainOfferRelations = productOfferRelationService.findByMainOfferId(offerId);
        // Gom relation theo relationOfferId 1 lan duy nhat (loc VAS truoc khi group, giu dung
        // dieu kien relationTypeId==VAS nhu code cu) - tranh quet lai toan bo mainOfferRelations
        // cho MOI phan tu cua lst (O(n*m) -> O(n+m)).
        Map<Long, List<ProductOfferRelationResponse>> relationsByOfferId = mainOfferRelations.stream()
                .filter(x -> DataUtil.safeEqual(x.relationTypeId(), Const.RELATION_TYPE.VAS))
                .collect(Collectors.groupingBy(ProductOfferRelationResponse::relationOfferId));

        // Batch 1 lan duy nhat cho ca lst (method nay da duoc thiet ke de nhan ca list va tu
        // chia batch 100 id/query - truoc day bi goi rieng le tung id trong vong lap ben duoi,
        // thanh N+1 query khong can thiet).
        List<String> offeringIds = lst.stream()
                .map(dto -> String.valueOf(dto.getProductOfferingId()))
                .toList();
        Map<Long, List<ProductSpecCharDTO>> specCharsByOfferingId = productOfferCharUseService.getProductSpecCharByOfferingIds(offeringIds);

        for (ProductOfferingDTO productOfferingDTO : lst) {

            List<ProductSpecCharDTO> lstAtt = specCharsByOfferingId.getOrDefault(productOfferingDTO.getProductOfferingId(), List.of());
            if (!DataUtil.isNullOrEmpty(lstAtt)) {
                productOfferingDTO.setLstProductSpecChars(lstAtt);
            }

            List<ProductOfferRelationResponse> lstTemp = relationsByOfferId.getOrDefault(productOfferingDTO.getProductOfferingId(), List.of());
            if (!DataUtil.isNullOrEmpty(lstTemp)) {
                productOfferingDTO.setLstProductOfferRelations(lstTemp);
            }
        }

        // MinhNH - 20160213 (giu nguyen comment goc): Loc danh sach vas theo cac nhom co san,
        // dua theo Product 1. THAY THE nguon du lieu: truoc doc file vascode_config.properties
        // (getVasExclude), nay lay tu OptionSet.CODE=VAS_EXCLUSIVE_GROUP (getVasExcludeGroup).
        if (!DataUtil.isNullOrEmpty(lst)) {
            // Fetch 1 lan duy nhat - truoc day getVasExcludeGroup tu query lai findByOptionSetCode
            // (khong cache) cho MOI 1 trong 8 nhom ben duoi, ra 8 query DB giong het nhau.
            List<OptionSetValueResponse> vasExclusiveGroups = optionSetValueService.findByOptionSetCode(Const.OPTION_SET.VAS_EXCLUSIVE_GROUP);
            List<String> PRE_GPRS = getVasExcludeGroup(vasExclusiveGroups, "PRE_GPRS");
            List<String> POS_GPRS = getVasExcludeGroup(vasExclusiveGroups, "POS_GPRS");
            List<String> PRE_G1 = getVasExcludeGroup(vasExclusiveGroups, "PRE_G1");
            List<String> POS_G1 = getVasExcludeGroup(vasExclusiveGroups, "POS_G1");
            List<String> POS_AP_BH = getVasExcludeGroup(vasExclusiveGroups, "POS_AP_BH");
            List<String> POS_BB = getVasExcludeGroup(vasExclusiveGroups, "POS_BB");
            List<String> PRE_BB = getVasExcludeGroup(vasExclusiveGroups, "PRE_BB");
            // code cu dat ten bien la "PRE_IPP" nhung thuc chat goi getVasExclude("IPP", "POS")
            // -> doc du lieu tu property key POS_IPP_* (da xac nhan qua doi chieu source that),
            // nen o day dat dung ten nhom theo ban chat du lieu la POS_IPP.
            List<String> POS_IPP = getVasExcludeGroup(vasExclusiveGroups, "POS_IPP");

            List<List<ProductOfferingDTO>> standList = new ArrayList<>();
            List<ProductOfferingDTO> checkPreGPRS = new ArrayList<>();
            List<ProductOfferingDTO> checkPosGPRS = new ArrayList<>();
            List<ProductOfferingDTO> checkPreG1 = new ArrayList<>();
            List<ProductOfferingDTO> checkPosG1 = new ArrayList<>();
            List<ProductOfferingDTO> checkPosAP = new ArrayList<>();
            List<ProductOfferingDTO> checkPosBB = new ArrayList<>();
            List<ProductOfferingDTO> checkPreBB = new ArrayList<>();
            List<ProductOfferingDTO> checkPosIPP = new ArrayList<>();

            for (ProductOfferingDTO aLst : lst) {
                if (checkVasExclude(PRE_GPRS, aLst.getCode())) {
                    checkPreGPRS.add(aLst);
                } else if (checkVasExclude(POS_GPRS, aLst.getCode())) {
                    checkPosGPRS.add(aLst);
                } else if (checkVasExclude(PRE_G1, aLst.getCode())) {
                    checkPreG1.add(aLst);
                } else if (checkVasExclude(POS_G1, aLst.getCode())) {
                    checkPosG1.add(aLst);
                } else if (checkVasExclude(POS_AP_BH, aLst.getCode())) {
                    checkPosAP.add(aLst);
                } else if (checkVasExclude(PRE_BB, aLst.getCode()) && DataUtil.safeEqual(aLst.getSubType(), Const.SUB_TYPE.PRE)) {
                    checkPreBB.add(aLst);
                } else if (checkVasExclude(POS_BB, aLst.getCode()) && DataUtil.safeEqual(aLst.getSubType(), Const.SUB_TYPE.POST)) {
                    checkPosBB.add(aLst);
                } else if (checkVasExclude(POS_IPP, aLst.getCode())) {
                    checkPosIPP.add(aLst);
                } else {
                    List<ProductOfferingDTO> temp = new ArrayList<>();
                    temp.add(aLst);
                    standList.add(temp);
                }
            }
            if (checkPreGPRS.size() > 0) {
                standList.add(checkPreGPRS);
            }
            if (checkPosGPRS.size() > 0) {
                standList.add(checkPosGPRS);
            }
            if (checkPreG1.size() > 0) {
                standList.add(checkPreG1);
            }
            if (checkPosG1.size() > 0) {
                standList.add(checkPosG1);
            }
            if (checkPosAP.size() > 0) {
                standList.add(checkPosAP);
            }
            if (checkPreBB.size() > 0) {
                standList.add(checkPreBB);
            }
            if (checkPosBB.size() > 0) {
                standList.add(checkPosBB);
            }
            if (checkPosIPP.size() > 0) {
                standList.add(checkPosIPP);
            }

            lst = new ArrayList<>(); // reset list tra ve
            if (!DataUtil.isNullOrEmpty(standList)) {
                for (int i = 0; i < standList.size(); i++) {
                    List<ProductOfferingDTO> lstTemp = standList.get(i);
                    if (!DataUtil.isNullOrEmpty(lstTemp)) {
                        for (ProductOfferingDTO productOfferingDTO : lstTemp) {
                            productOfferingDTO.setTypeIndex(i + 1); // TH value = 0 se ko check rang buoc vas nua
                        }
                        lst.addAll(lstTemp);
                    }
                }
            }
            return lst;
        }
        return lst;
    }

    /**
     * Map 1:1 tu ProductOfferingServiceImpl.checkVasExclude (dong 227-239 code mono cu).
     */
    private boolean checkVasExclude(List<String> lstCheck, String vasCode) {
        if (lstCheck == null) {
            return false;
        }
        for (String aLstCheck : lstCheck) {
            if (aLstCheck.equals(vasCode)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getVasExcludeGroup(List<OptionSetValueResponse> allGroups, String groupName) {
        return allGroups.stream()
                .filter(v -> groupName.equals(v.name()) && v.value() != null)
                .map(OptionSetValueResponse::value)
                .toList();
    }

    // Chi so cot trong Object[] tra ve tu ProductSpecCharService.findByLstSpecCodeAndLstProductCode
    // (xem ProductSpecCharRepositoryCustomImpl - 24 cot product_spec_char + 3 cot product_offering).
    private static final int ROW_IDX_OFFERING_ID = 24;
    private static final int ROW_IDX_OFFERING_CODE = 25;
    private static final int ROW_IDX_OFFERING_NAME = 26;


    public List<ProductOfferingDTO> findProductOfferingByListCodeListSpecCode(
            List<String> lstProductOfferCode, List<String> lstSpecCode, String productOfferType) {
        RequestValidator.requireNotEmpty(lstSpecCode, "lstSpecCode", "BCCS-PRODUCT-VALIDATE-0000");
        Long productOfferTypeId = DataUtil.isNullOrEmpty(productOfferType)
                ? Const.PRODUCT_OFFER_TYPE.PRODUCT_CODE
                : Long.valueOf(productOfferType.trim());

        List<Object[]> rows = productSpecCharService.findByLstSpecCodeAndLstProductCode(lstSpecCode, lstProductOfferCode, productOfferTypeId);
        if (DataUtil.isNullOrEmpty(rows)) {
            return null;
        }

        Map<Long, ProductOfferingDTO> resultByOfferingId = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Long productOfferingId = ((Number) row[ROW_IDX_OFFERING_ID]).longValue();

            ProductSpecCharDTO specCharDto = productSpecCharUseMapper.toDto(productSpecCharUseMapper.buildSpecCharEntity(row));
            specCharDto.setProductOfferingId(productOfferingId);

            ProductOfferingDTO offeringDto = resultByOfferingId.get(productOfferingId);
            if (offeringDto == null) {
                List<ProductSpecCharDTO> lstProductSpecChars = new ArrayList<>();
                lstProductSpecChars.add(specCharDto);
                offeringDto = ProductOfferingDTO.builder()
                        .productOfferingId(productOfferingId)
                        .code(rowStr(row[ROW_IDX_OFFERING_CODE]))
                        .name(rowStr(row[ROW_IDX_OFFERING_NAME]))
                        .status(Const.STATUS.ACTIVE)
                        .lstProductSpecChars(lstProductSpecChars)
                        .build();
                resultByOfferingId.put(productOfferingId, offeringDto);
            } else {
                offeringDto.getLstProductSpecChars().add(specCharDto);
            }
        }
        return new ArrayList<>(resultByOfferingId.values());
    }

    private static String rowStr(Object val) {
        return val != null ? val.toString() : null;
    }
}
