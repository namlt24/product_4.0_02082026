package com.viettel.bccs.productcatalog.productpackagecharuse.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.productcatalog.productpackagecharuse.entity.ProductPackageCharUseEntity;

@Repository
public interface ProductPackageCharUseRepository extends JpaRepository<ProductPackageCharUseEntity, Long>,
    ProductPackageCharUseRepositoryCustom {

}