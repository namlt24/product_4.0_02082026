package com.viettel.bccs.productcatalog.product.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.productcatalog.common.dto.FilterRequest;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingDTO;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingResponse;
import com.viettel.bccs.productcatalog.product.mapper.ProductOfferingMapper;
import com.viettel.bccs.productcatalog.product.repository.ProductOfferingRepository;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductOfferingService {

    private final ProductOfferingRepository productOfferingRepository;
    private final ProductOfferingMapper productOfferingMapper;

    public ProductOfferingResponse getByProductCode(String productCode) {
        return productOfferingRepository.findByCode(productCode)
                .map(productOfferingMapper::toResponse)
                .orElseThrow(() -> new BusinessException("CATALOG-PRODUCT-001", "Product not found with code: " + productCode));
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
            throw new BusinessException("CATALOG-PRODUCT-002", "payType and productOfferTypeId are required");
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
}