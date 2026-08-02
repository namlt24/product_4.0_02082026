package com.viettel.bccs.productcatalog.productpackage.repository;

import com.viettel.bccs.productcatalog.productpackage.dto.response.ProductPackageDTO;

import java.util.List;
import java.util.Optional;

public interface ProductPackageRepositoryCustom {

    List<ProductPackageDTO> getProductPackageExtra(String code, String productPackageType, boolean isActive, boolean isUpperCode, boolean checkStatus);

    Optional<ProductPackageDTO> findActiveByCode(String code);
}