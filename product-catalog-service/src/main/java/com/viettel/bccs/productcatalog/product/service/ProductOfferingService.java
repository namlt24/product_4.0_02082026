package com.viettel.bccs.productcatalog.product.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.productcatalog.common.dto.FilterRequest;
import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetValueResponse;
import com.viettel.bccs.productcatalog.optionset.service.OptionSetValueService;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingDTO;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingResponse;
import com.viettel.bccs.productcatalog.product.dto.response.StockOfferingRow;
import com.viettel.bccs.productcatalog.product.mapper.ProductOfferingMapper;
import com.viettel.bccs.productcatalog.product.repository.ProductOfferingRepository;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.mapper.ProductSpecCharUseMapper;
import com.viettel.bccs.productcatalog.productoffercharuse.service.ProductOfferCharUseService;
import com.viettel.bccs.productcatalog.productofferrelation.dto.response.ProductOfferRelationResponse;
import com.viettel.bccs.productcatalog.productofferrelation.service.ProductOfferRelationService;
import com.viettel.bccs.productcatalog.productspecchar.entity.ProductSpecCharEntity;
import com.viettel.bccs.productcatalog.productspecchar.service.ProductSpecCharService;
import com.viettel.bccs.productcatalog.utils.Const;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final ProductSpecCharService productSpecCharService;
    private final ProductSpecCharUseMapper productSpecCharUseMapper;

    @Cacheable(value = "productOfferingCache", key = "'ID:' + #productOfferingId")
    public ProductOfferingDTO findById(Long productOfferingId) {
        return productOfferingRepository.findById(productOfferingId)
                .map(productOfferingMapper::toDto)
                .orElse(null);
    }

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

    @Cacheable(value = "productOfferingCache", key = "'TELECOM_SUB_TYPE_ACTIVE:' + #telecomServiceId + ':' + '' + #subType + ':' + #offerTypeId")
    public List<ProductOfferingDTO> findByTelecomSubTypeOfferType(Long telecomServiceId, String subType, Long offerTypeId) {
        return findByTelecomSubTypeOfferTypeCheckProductStatus(telecomServiceId, subType, offerTypeId, true);
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

    public boolean hasProductAtt(Long offerId, String attributeCode) {
        if (DataUtil.isAnyNull(offerId, attributeCode)) {
            throw new BusinessException("BCCS-CATALOG-PRODUCT-0004", "offerId and attributeCode are required");
        }
        return productOfferingRepository.hasProductAtt(offerId, attributeCode);
    }

    /**
     * Lấy danh sách mặt hàng (product_offering) có gán các đặc tính (product_spec_char.code) trong
     * specCodes, theo điều kiện AND (phải có TẤT CẢ) hoặc OR (chỉ cần có ÍT NHẤT 1). Không truyền
     * condition thì mặc định AND.
     */
    @Cacheable(value = "productOfferingCache", key = "'SPEC_CHARS:' + T(String).join(',', #specCodes.stream().sorted().toList()) + ':' + #condition")
    public List<ProductOfferingDTO> getListProductOfferingBySpecChars(List<String> specCodes, String condition) {
        if (DataUtil.isNullOrEmpty(specCodes)) {
            throw new BusinessException("BCCS-CATALOG-PRODUCT-0005", "specCodes must not be empty");
        }
        String normalizedCondition = DataUtil.isNullOrEmpty(condition) ? Const.CONDITION.AND : condition.trim().toUpperCase();
        if (!Const.CONDITION.AND.equals(normalizedCondition) && !Const.CONDITION.OR.equals(normalizedCondition)) {
            throw new BusinessException("BCCS-CATALOG-PRODUCT-0005", "condition must be AND or OR");
        }
        return productOfferingRepository.findBySpecCharCodes(specCodes, normalizedCondition).stream()
                .map(productOfferingMapper::toDto)
                .toList();
    }

    @Cacheable(value = "productOfferingCache", key = "'LIST_VAS:' + #offerId")
    public List<ProductOfferingDTO> getListVas(Long offerId) {
        return getListVasCore(offerId);
    }

    /**
     * Migrate từ mono: ProductOfferingServiceImpl.getListStockModelBySaleServiceCode
     * (nhánh containNumber=true — xem ghi chú tại ProductOfferingRepositoryCustom). Trả về shape
     * nội bộ {@link StockOfferingRow} (chưa có giá — giá được tính riêng ở bước 8, do phụ thuộc
     * ProductOfferPriceService, không thể inject ngược lại đây vì ProductOfferPriceService đã
     * phụ thuộc ProductOfferingService, tránh circular dependency).
     */
    public List<StockOfferingRow> getListStockModelBySaleServiceCode(String saleServiceCode) {
        return productOfferingRepository.getListStockModelBySaleServiceCode(saleServiceCode).stream()
                .map(ProductOfferingService::toStockOfferingRow)
                .toList();
    }

    private static StockOfferingRow toStockOfferingRow(Object[] row) {
        return new StockOfferingRow(
                toLong(row[0]),
                toLong(row[1]),
                row[2] != null ? row[2].toString() : null,
                row[3] != null ? ((Number) row[3]).shortValue() : null,
                toLong(row[4]),
                row[5] != null ? row[5].toString() : null,
                row[6] != null ? row[6].toString() : null,
                toLong(row[7])
        );
    }

    private static Long toLong(Object value) {
        return value != null ? ((Number) value).longValue() : null;
    }


    private List<ProductOfferingDTO> getListVasCore(Long offerId) {
        // productOfferingServiceImpl.java:3621 — repository.getListVas(offerId) da doi chieu voi
        // source that va cap nhat trong ProductOfferingRepositoryCustomImpl.getListVas(...).
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

    // Chi so cot trong Object[] tra ve tu ProductSpecCharService.findByLstSpecCodeAndLstProductCode
    // (xem ProductSpecCharRepositoryCustomImpl - 24 cot product_spec_char + 3 cot product_offering).
    private static final int ROW_IDX_OFFERING_ID = 24;
    private static final int ROW_IDX_OFFERING_CODE = 25;
    private static final int ROW_IDX_OFFERING_NAME = 26;

    /**
     * Migrate từ mono: ExternalServiceForMbccsImpl.findProductOfferingByListCodeListSpecCode. Tìm
     * các product_offering (mặc định productOfferType=200/PRODUCT_CODE nếu không truyền) có gán ít
     * nhất 1 đặc tính trong lstSpecCode (bắt buộc), tuỳ chọn lọc thêm theo lstProductOfferCode. Gom
     * nhóm kết quả theo productOfferingId — mỗi dòng DB trả về là 1 spec char; DTO offering dựng
     * mới CHỈ gồm id/code/name/status (ACTIVE), không load đầy đủ như {@link #findById}, đúng hành
     * vi gốc. Trả về {@code null} nếu không có kết quả nào (không phải list rỗng — đúng hành vi mono).
     */
    public List<ProductOfferingDTO> findProductOfferingByListCodeListSpecCode(
            List<String> lstProductOfferCode, List<String> lstSpecCode, String productOfferType) {
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

            ProductSpecCharDTO specCharDto = productSpecCharUseMapper.toDto(buildSpecCharEntity(row));
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

    private static ProductSpecCharEntity buildSpecCharEntity(Object[] row) {
        return ProductSpecCharEntity.builder()
                .productSpecCharId(row[0] != null ? ((Number) row[0]).longValue() : null)
                .name(rowStr(row[1]))
                .description(rowStr(row[2]))
                .valueType(rowStr(row[3]))
                .charType(rowStr(row[4]))
                .minCardinality(row[5] != null ? ((Number) row[5]).longValue() : null)
                .maxCardinality(row[6] != null ? ((Number) row[6]).longValue() : null)
                .status(rowStr(row[7]))
                .code(rowStr(row[8]))
                .productSpecCharTypeId(rowStr(row[9]))
                .valueSetType(row[10] != null ? ((Number) row[10]).longValue() : null)
                .responseClass(rowStr(row[11]))
                .sqlQuery(rowStr(row[12]))
                .displayObject(rowStr(row[13]))
                .valueObject(rowStr(row[14]))
                .solrQuery(rowStr(row[15]))
                .solrCore(rowStr(row[16]))
                .solrSchema(rowStr(row[17]))
                .dataType(rowStr(row[18]))
                .wsWsdl(rowStr(row[19]))
                .templateRequest(rowStr(row[20]))
                .validatePattern(rowStr(row[21]))
                .extData(rowStr(row[22]))
                .note(rowStr(row[23]))
                .build();
    }
}
