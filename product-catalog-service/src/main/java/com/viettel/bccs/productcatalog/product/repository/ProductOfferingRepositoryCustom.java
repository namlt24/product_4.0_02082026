package com.viettel.bccs.productcatalog.product.repository;

import java.util.List;

import com.viettel.bccs.productcatalog.common.dto.FilterRequest;
import com.viettel.bccs.productcatalog.product.entity.ProductOfferingEntity;

public interface ProductOfferingRepositoryCustom {

    List<ProductOfferingEntity> getListOfferAlterStatus(Long offerId, String changeChannel, boolean checkStatus);

    List<ProductOfferingEntity> findByTelecomSubTypeOfferTypeCheckProductStatus(Long telecomServiceId, String subType,
        Long offerTypeId, boolean getActiveProduct);

    List<ProductOfferingEntity> findByPayTypeWithSpec(String telecomServiceId, String payType,
        String productOfferTypeId, List<FilterRequest> listProductSpec);

    List<ProductOfferingEntity> findByCodeOrId(Long proOfferId, String prodOfferCode, String status);

    List<ProductOfferingEntity> findByCodesAndProductOfferType(List<String> codes, Long productOfferTypeId);

    boolean checkAttProductOrVasByCode(String productCode, Long productType, String attributeCode);

    boolean hasProductAtt(Long offerId, String attributeCode);

    List<ProductOfferingEntity> findBySpecCharCodes(List<String> specCodes, Long productOfferTypeId);

    List<ProductOfferingEntity> getListVas(Long offerId, Integer type);
}