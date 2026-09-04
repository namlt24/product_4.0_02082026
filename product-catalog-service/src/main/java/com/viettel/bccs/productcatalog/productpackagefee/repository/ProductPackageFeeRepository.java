package com.viettel.bccs.productcatalog.productpackagefee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.productcatalog.productpackagefee.entity.ProductPackageFeeEntity;

@Repository
public interface ProductPackageFeeRepository extends JpaRepository<ProductPackageFeeEntity, Long>,
    ProductPackageFeeRepositoryCustom {
    List<ProductPackageFeeEntity> findByStatus(String status);

    List<ProductPackageFeeEntity> findByProductPackageIdAndStatus(Long productPackageId, String status);

    List<ProductPackageFeeEntity> findByPricePolicyId(Long pricePolicyId);

    List<ProductPackageFeeEntity> findByPriceTypeId(Long priceTypeId);
}