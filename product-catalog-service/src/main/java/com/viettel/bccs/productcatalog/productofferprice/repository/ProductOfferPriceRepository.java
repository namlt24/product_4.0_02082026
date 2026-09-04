package com.viettel.bccs.productcatalog.productofferprice.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.productcatalog.productofferprice.entity.ProductOfferPriceEntity;

@Repository
public interface ProductOfferPriceRepository extends JpaRepository<ProductOfferPriceEntity, Long>,
    ProductOfferPriceRepositoryCustom {

}