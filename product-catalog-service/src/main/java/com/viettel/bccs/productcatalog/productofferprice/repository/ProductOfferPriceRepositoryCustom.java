package com.viettel.bccs.productcatalog.productofferprice.repository;

import java.util.List;

import com.viettel.bccs.productcatalog.productofferprice.entity.ProductOfferPriceEntity;

public interface ProductOfferPriceRepositoryCustom {

    List<ProductOfferPriceEntity> getPriceInServices(Long productPackageId, Long productOfferType,
        Long productOfferId, Long pricePolicy);

    List<ProductOfferPriceEntity> getPriceEquipment(Long productPackageId, Long productOfferType, Long productOfferId);

    List<ProductOfferPriceEntity> getPriceByTypePolicy(Long productOfferId, Long priceTypeId, Long pricePolicy);

    List<ProductOfferPriceEntity> getPledgePriceInfoByOfferId(Long productOfferingId);

}