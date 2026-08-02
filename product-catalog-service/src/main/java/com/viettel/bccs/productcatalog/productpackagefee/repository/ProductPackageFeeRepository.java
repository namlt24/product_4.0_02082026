package com.viettel.bccs.productcatalog.productpackagefee.repository;

import com.viettel.bccs.productcatalog.productpackagefee.entity.ProductPackageFeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductPackageFeeRepository extends JpaRepository<ProductPackageFeeEntity, Long>, ProductPackageFeeRepositoryCustom {

    List<ProductPackageFeeEntity> findByStatus(String status);

    List<ProductPackageFeeEntity> findByProductPackageIdAndStatus(Long productPackageId, String status);

    List<ProductPackageFeeEntity> findByPricePolicyId(Long pricePolicyId);

    List<ProductPackageFeeEntity> findByPriceTypeId(Long priceTypeId);
}