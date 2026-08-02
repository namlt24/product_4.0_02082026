package com.viettel.bccs.productcatalog.product.repository;

import com.viettel.bccs.productcatalog.product.entity.ProductOfferingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductOfferingRepository extends JpaRepository<ProductOfferingEntity, Long>, ProductOfferingRepositoryCustom {

    Optional<ProductOfferingEntity> findByCode(String code);
}