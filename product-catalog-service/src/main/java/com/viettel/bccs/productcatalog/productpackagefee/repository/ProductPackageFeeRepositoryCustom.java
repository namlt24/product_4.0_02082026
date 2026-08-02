package com.viettel.bccs.productcatalog.productpackagefee.repository;

import com.viettel.bccs.productcatalog.productpackagefee.entity.ProductPackageFeeEntity;

import java.util.List;

public interface ProductPackageFeeRepositoryCustom {

    List<ProductPackageFeeEntity> findByProductPackageId(Long productPackageId);
}