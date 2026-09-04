package com.viettel.bccs.productcatalog.productpackagefee.repository;

import java.util.List;

import com.viettel.bccs.productcatalog.productpackagefee.entity.ProductPackageFeeEntity;

public interface ProductPackageFeeRepositoryCustom {

    List<ProductPackageFeeEntity> findByProductPackageId(Long productPackageId);
}