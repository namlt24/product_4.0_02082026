package com.viettel.bccs.productcatalog.productpackage.repository;

import java.util.List;
import java.util.Optional;

import com.viettel.bccs.productcatalog.productpackage.dto.response.ProductPackageDTO;

public interface ProductPackageRepositoryCustom {

    List<ProductPackageDTO> getProductPackageExtra(String code, String productPackageType, boolean isActive,
        boolean isUpperCode, boolean checkStatus);

    Optional<ProductPackageDTO> findActiveByCode(String code);
}