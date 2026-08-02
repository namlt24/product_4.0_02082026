package com.viettel.bccs.productcatalog.productofferprice.repository;

import com.viettel.bccs.productcatalog.productofferprice.entity.ProductOfferPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOfferPriceRepository extends JpaRepository<ProductOfferPriceEntity, Long>, ProductOfferPriceRepositoryCustom {

}