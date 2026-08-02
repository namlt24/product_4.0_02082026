package com.viettel.bccs.productcatalog.productpackagefee.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.productcatalog.productpackagefee.dto.response.ProductPackageFeeDTO;
import com.viettel.bccs.productcatalog.productpackagefee.dto.response.ProductPackageFeeResponse;
import com.viettel.bccs.productcatalog.productpackagefee.entity.ProductPackageFeeEntity;
import com.viettel.bccs.productcatalog.productpackagefee.mapper.ProductPackageFeeMapper;
import com.viettel.bccs.productcatalog.productpackagefee.repository.ProductPackageFeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductPackageFeeService {

    private final ProductPackageFeeRepository repository;
    private final ProductPackageFeeMapper mapper;

    public ProductPackageFeeResponse findById(Long id) {
        Optional<ProductPackageFeeEntity> entity = repository.findById(id);
        if (entity.isEmpty()) {
            throw new BusinessException("BCCS-CATALOG-002", "Product package fee not found with id: " + id);
        }
        return mapper.toResponse(entity.get());
    }

    public List<ProductPackageFeeResponse> findByProductPackageId(Long productPackageId) {
        return findByProductPackageIdCached(productPackageId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<ProductPackageFeeDTO> findByProductPackageIdForPackage(Long productPackageId) {
        return findByProductPackageIdCached(productPackageId).stream()
                .map(mapper::toDTOPackage)
                .toList();
    }

    @Cacheable(value = "productPackageFeeCache", key = "'PKG_FEE:' + #productPackageId")
    public List<ProductPackageFeeEntity> findByProductPackageIdCached(Long productPackageId) {
        return repository.findByProductPackageId(productPackageId);
    }

    public List<ProductPackageFeeResponse> findByStatus(String status) {
        return repository.findByStatus(status).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<ProductPackageFeeResponse> findByProductPackageIdAndStatus(Long productPackageId, String status) {
        return repository.findByProductPackageIdAndStatus(productPackageId, status).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<ProductPackageFeeResponse> findByPricePolicyId(Long pricePolicyId) {
        return repository.findByPricePolicyId(pricePolicyId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<ProductPackageFeeResponse> findByPriceTypeId(Long priceTypeId) {
        return repository.findByPriceTypeId(priceTypeId).stream()
                .map(mapper::toResponse)
                .toList();
    }
}