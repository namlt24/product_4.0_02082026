package com.viettel.bccs.productcatalog.productspeccharuse.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.productcatalog.productspeccharuse.dto.response.ProductSpecCharUseResponse;
import com.viettel.bccs.productcatalog.productspeccharuse.mapper.ProductSpecCharUseMapper;
import com.viettel.bccs.productcatalog.productspeccharuse.repository.ProductSpecCharUseRepository;
import com.viettel.bccs.productcatalog.utils.Const;
import com.viettel.bccs.productcatalog.utils.RequestValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductSpecCharUseService {

    private final ProductSpecCharUseRepository productSpecCharUseRepository;
    private final ProductSpecCharUseMapper productSpecCharUseMapper;

    @Transactional(readOnly = true)
    public List<ProductSpecCharUseResponse> findByIds(List<Long> ids) {
        RequestValidator.requireNotEmpty(ids, "ids", "BCCS-PRODUCT-VALIDATE-0000");
        return productSpecCharUseRepository.findAllById(ids).stream()
                .filter(entity -> Const.Status.ACTIVE.equals(entity.getStatus()))
                .map(productSpecCharUseMapper::toResponse)
                .toList();
    }
}
