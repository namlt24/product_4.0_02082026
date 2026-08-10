package com.viettel.bccs.productcatalog.product.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.productcatalog.common.dto.FilterRequest;
import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetValueResponse;
import com.viettel.bccs.productcatalog.optionset.service.OptionSetValueService;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingDTO;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingResponse;
import com.viettel.bccs.productcatalog.product.mapper.ProductOfferingMapper;
import com.viettel.bccs.productcatalog.product.repository.ProductOfferingRepository;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.service.ProductOfferCharUseService;
import com.viettel.bccs.productcatalog.productofferrelation.dto.response.ProductOfferRelationResponse;
import com.viettel.bccs.productcatalog.productofferrelation.service.ProductOfferRelationService;
import com.viettel.bccs.productcatalog.utils.Const;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductOfferingService {

    private final ProductOfferingRepository productOfferingRepository;
    private final ProductOfferingMapper productOfferingMapper;
    private final ProductOfferRelationService productOfferRelationService;
    private final ProductOfferCharUseService productOfferCharUseService;
    private final OptionSetValueService optionSetValueService;

    public ProductOfferingResponse getByProductCode(String productCode) {
        return productOfferingRepository.findByCode(productCode)
                .map(productOfferingMapper::toResponse)
                .orElseThrow(() -> new BusinessException("BCCS-CATALOG-PRODUCT-0001", "Product not found with code: " + productCode));
    }

    @Cacheable(value = "productOfferingCache", key = "'OFFER_ALTER:' + #offerId + ':' + '' + #changeChannel + ':' + #checkStatus")
    public List<ProductOfferingDTO> getListOfferAlterStatus(Long offerId, String changeChannel, boolean checkStatus) {
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

    public List<ProductOfferingDTO> findByPayTypeWithSpec(String telecomServiceId, String payType, String productOfferTypeId, List<FilterRequest> listProductSpec) {
        if (DataUtil.isAnyNull(payType, productOfferTypeId)) {
            throw new BusinessException("BCCS-CATALOG-PRODUCT-0002", "payType and productOfferTypeId are required");
        }
        return productOfferingRepository.findByPayTypeWithSpec(telecomServiceId, payType, productOfferTypeId, listProductSpec).stream()
                .map(productOfferingMapper::toDto)
                .toList();
    }

    @Cacheable(value = "productOfferingCache", key = "'CODE_OR_ID:' + #proOfferId + ':' + #prodOfferCode + ':' + #status")
    public List<ProductOfferingDTO> findByCodeOrId(Long proOfferId, String prodOfferCode, String status) {
        if (DataUtil.isAnyNull(proOfferId, prodOfferCode)) {
            return List.of();
        }
        return productOfferingRepository.findByCodeOrId(proOfferId, prodOfferCode, status).stream()
                .map(productOfferingMapper::toDto)
                .toList();
    }

    @Cacheable(value = "productOfferingCache", key = "'CODES_OFFER_TYPE:' + T(String).join(',', #codes.stream().sorted().toList()) + ':' + #productOfferTypeId")
    public List<ProductOfferingDTO> findByCodesAndProductOfferType(List<String> codes, Long productOfferTypeId) {
        return productOfferingRepository.findByCodesAndProductOfferType(codes, productOfferTypeId).stream()
                .map(productOfferingMapper::toDto)
                .toList();
    }

    public List<ProductOfferingDTO> findByIds(List<Long> offerIds) {
        if (DataUtil.isNullOrEmpty(offerIds)) {
            return List.of();
        }
        return productOfferingRepository.findAllById(offerIds).stream()
                .map(productOfferingMapper::toDto)
                .toList();
    }

    public boolean checkAttProductOrVasByCode(String productCode, String productType, String attributeCode) {
        if (DataUtil.isAnyNull(productCode, productType, attributeCode)) {
            throw new BusinessException("BCCS-CATALOG-PRODUCT-0003", "productCode, productType and attributeCode are required");
        }
        return productOfferingRepository.checkAttProductOrVasByCode(productCode, Long.valueOf(productType.trim()), attributeCode);
    }

    /**
     * Map 1:1 tu ProductOfferingServiceImpl.getListVas (code mono cu, khong con trong repo hien
     * tai). Code cu dung cache 2 tang thu cong (RAM HashMap + raw Jedis) — day la vi pham
     * CLAUDE.md ("khong dung Redis client tho") va vi pham thang rule ArchUnit
     * NO_DIRECT_REDIS_CLIENT_USAGE dang bat trong repo nay, nen CHI RIENG PHAN CACHE duoc thay
     * bang @Cacheable (BCCS starter). Toan bo logic nghiep vu con lai giu nguyen trong
     * getListVasCore ben duoi, map tung dong voi code cu.
     */
    @Cacheable(value = "productOfferingCache", key = "'LIST_VAS:' + #offerId")
    public List<ProductOfferingDTO> getListVas(Long offerId) {
        return getListVasCore(offerId);
    }

    /**
     * Map 1:1 tu ProductOfferingServiceImpl.getListVasCore (dong 3618-3727 code mono cu).
     * Duy nhat 1 doan duoc thay the: "Loc danh sach vas theo cac nhom co san" (comment goc cua
     * tac gia "MinhNH - 20160213") — code cu doc 8 danh sach tu file vascode_config.properties
     * (qua getVasExclude), code nay doc tu OPTION_SET.CODE=VAS_EXCLUSIVE_GROUP (qua
     * getVasExcludeGroup). Cau truc chuoi if-else-if va cach gan typeIndex giu nguyen y het.
     */
    private List<ProductOfferingDTO> getListVasCore(Long offerId) {
        // productOfferingServiceImpl.java:3621 — repository.getListVas(offerId) khong co source
        // that trong bo code duoc cung cap, da viet tam theo suy doan trong
        // ProductOfferingRepositoryCustomImpl.getListVas(...).
        List<ProductOfferingDTO> lst = productOfferingRepository.getListVas(offerId).stream()
                .map(productOfferingMapper::toDto)
                .collect(Collectors.toCollection(ArrayList::new));
        if (DataUtil.isNullOrEmpty(lst)) {
            return null;
        }

        List<ProductOfferRelationResponse> mainOfferRelations = productOfferRelationService.findByMainOfferId(offerId);

        for (ProductOfferingDTO productOfferingDTO : lst) {
            // (a) lay thuoc tinh cua VAS. Code cu goi productOfferCharUseService.getProductOfferCharacter(id)
            // rieng le cho tung VAS trong vong lap (N+1) — method nay khong con trong repo hien tai,
            // dung tam getProductSpecCharByOfferingIds(List.of(id)) goi tung VAS 1 de giu dung cau truc N+1 cua code cu.
            List<ProductSpecCharDTO> lstAtt = productOfferCharUseService
                    .getProductSpecCharByOfferingIds(List.of(String.valueOf(productOfferingDTO.getProductOfferingId())))
                    .getOrDefault(productOfferingDTO.getProductOfferingId(), List.of());
            if (!DataUtil.isNullOrEmpty(lstAtt)) {
                productOfferingDTO.setLstProductSpecChars(lstAtt);
            }

            // (b) lay thong tin quan he — loc lai tu mainOfferRelations da lay 1 lan o tren,
            // dung 2 dieu kien y het code cu: relationOfferId == VAS dang xet, va relationTypeId == VAS
            List<ProductOfferRelationResponse> lstTemp = mainOfferRelations.stream()
                    .filter(x -> DataUtil.safeEqual(x.relationOfferId(), productOfferingDTO.getProductOfferingId()))
                    .filter(x -> DataUtil.safeEqual(x.relationTypeId(), Const.RELATION_TYPE.VAS))
                    .toList();
            if (!DataUtil.isNullOrEmpty(lstTemp)) {
                productOfferingDTO.setLstProductOfferRelations(lstTemp);
            }
        }

        // MinhNH - 20160213 (giu nguyen comment goc): Loc danh sach vas theo cac nhom co san,
        // dua theo Product 1. THAY THE nguon du lieu: truoc doc file vascode_config.properties
        // (getVasExclude), nay lay tu OptionSet.CODE=VAS_EXCLUSIVE_GROUP (getVasExcludeGroup).
        if (!DataUtil.isNullOrEmpty(lst)) {
            List<String> PRE_GPRS = getVasExcludeGroup("PRE_GPRS");
            List<String> POS_GPRS = getVasExcludeGroup("POS_GPRS");
            List<String> PRE_G1 = getVasExcludeGroup("PRE_G1");
            List<String> POS_G1 = getVasExcludeGroup("POS_G1");
            List<String> POS_AP_BH = getVasExcludeGroup("POS_AP_BH");
            List<String> POS_BB = getVasExcludeGroup("POS_BB");
            List<String> PRE_BB = getVasExcludeGroup("PRE_BB");
            // code cu dat ten bien la "PRE_IPP" nhung thuc chat goi getVasExclude("IPP", "POS")
            // -> doc du lieu tu property key POS_IPP_* (da xac nhan qua doi chieu source that),
            // nen o day dat dung ten nhom theo ban chat du lieu la POS_IPP.
            List<String> POS_IPP = getVasExcludeGroup("POS_IPP");

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

    /**
     * Thay the ProductOfferingServiceImpl.getVasExclude(type, serviceType) (doc file
     * vascode_config.properties). Lay danh sach ma VAS thuoc 1 nhom loai tru duy nhat,
     * tu OPTION_SET.CODE=VAS_EXCLUSIVE_GROUP, OPTION_SET_VALUE.NAME=groupName,
     * OPTION_SET_VALUE.VALUE=ma VAS.
     */
    private List<String> getVasExcludeGroup(String groupName) {
        return optionSetValueService.findByOptionSetCode(Const.OPTION_SET.VAS_EXCLUSIVE_GROUP).stream()
                .filter(v -> groupName.equals(v.name()) && v.value() != null)
                .map(OptionSetValueResponse::value)
                .toList();
    }
}
