package com.viettel.bccs.productcatalog.productspecchar.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.productcatalog.productspecchar.dto.response.ProductSpecCharResponse;
import com.viettel.bccs.productcatalog.productspecchar.mapper.ProductSpecCharMapper;
import com.viettel.bccs.productcatalog.productspecchar.repository.ProductSpecCharRepository;
import com.viettel.bccs.productcatalog.utils.Const;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSpecCharService {

    private final ProductSpecCharRepository productSpecCharRepository;
    private final ProductSpecCharMapper productSpecCharMapper;

    @Transactional(readOnly = true)
    public ProductSpecCharResponse getByCode(String code) {
        return productSpecCharRepository.findByCode(code)
                .map(productSpecCharMapper::toResponse)
                .orElseThrow(() -> new BusinessException("BCCS-CATALOG-CHAR-0001", "Product spec char not found with code: " + code));
    }

    @Transactional(readOnly = true)
    public ProductSpecCharResponse getById(Long id) {
        return productSpecCharRepository.findById(id)
                .map(productSpecCharMapper::toResponse)
                .orElseThrow(() -> new BusinessException("BCCS-CATALOG-CHAR-0001", "Product spec char not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<ProductSpecCharResponse> getAll() {
        return productSpecCharRepository.findAll().stream()
                .map(productSpecCharMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductSpecCharResponse> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return productSpecCharRepository.findAllById(ids).stream()
                .filter(entity -> Const.STATUS.ACTIVE.equals(entity.getStatus()))
                .map(productSpecCharMapper::toResponse)
                .toList();
    }

}