package com.viettel.bccs.productcatalog.productoffertype.repository;

import com.viettel.bccs.productcatalog.productoffertype.entity.ProductOfferTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOfferTypeRepository extends JpaRepository<ProductOfferTypeEntity, Long>, ProductOfferTypeRepositoryCustom {
}