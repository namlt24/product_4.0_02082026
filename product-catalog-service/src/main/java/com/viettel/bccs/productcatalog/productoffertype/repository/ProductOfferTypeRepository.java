package com.viettel.bccs.productcatalog.productoffertype.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.productcatalog.productoffertype.entity.ProductOfferTypeEntity;

@Repository
public interface ProductOfferTypeRepository extends JpaRepository<ProductOfferTypeEntity, Long>,
    ProductOfferTypeRepositoryCustom {
    }