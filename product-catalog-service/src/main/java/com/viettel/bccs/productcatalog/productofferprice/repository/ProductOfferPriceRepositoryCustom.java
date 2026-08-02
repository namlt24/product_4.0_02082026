package com.viettel.bccs.productcatalog.productofferprice.repository;

import com.viettel.bccs.productcatalog.productofferprice.entity.ProductOfferPriceEntity;

import java.util.List;

public interface ProductOfferPriceRepositoryCustom {

    List<ProductOfferPriceEntity> findForPccc(Long productPackageId, String productPackageCode,
            Long productOfferType, Long productOfferId, Long pricePolicy);

    List<ProductOfferPriceEntity> getPriceInServices(Long productPackageId, Long productOfferType, Long productOfferId, Long pricePolicy);
}