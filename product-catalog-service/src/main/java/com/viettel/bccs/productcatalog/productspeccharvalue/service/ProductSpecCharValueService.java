package com.viettel.bccs.productcatalog.productspeccharvalue.service;

import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharValueDTO;
import com.viettel.bccs.productcatalog.productspeccharvalue.dto.response.ProductSpecCharValueResponse;
import com.viettel.bccs.productcatalog.productspeccharvalue.entity.ProductSpecCharValueEntity;
import com.viettel.bccs.productcatalog.productspeccharvalue.mapper.ProductSpecCharValueMapper;
import com.viettel.bccs.productcatalog.productspeccharvalue.repository.ProductSpecCharValueRepository;
import com.viettel.bccs.productcatalog.utils.Const;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSpecCharValueService {

    private final ProductSpecCharValueRepository productSpecCharValueRepository;
    private final ProductSpecCharValueMapper productSpecCharValueMapper;

    @Transactional(readOnly = true)
    public List<ProductSpecCharValueResponse> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return productSpecCharValueRepository.findAllById(ids).stream()
                .filter(entity -> Const.STATUS.ACTIVE.equals(entity.getStatus()))
                .map(productSpecCharValueMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "productSpecCharValueCache",
            key = "'SPEC_CHAR_VALUE:' + #characterCode + ':' + #productOfferingId")
    public List<ProductSpecCharValueEntity> getByProductSpecCharCodeAndProductOfferingId(
            String characterCode, Long productOfferingId) {
        if (DataUtil.isNullOrEmpty(characterCode) || DataUtil.isNullOrZero(productOfferingId)) {
            return Collections.emptyList();
        }
        return productSpecCharValueRepository
                .getByProductSpecCharCodeAndProductOfferingId(characterCode, productOfferingId);
    }

}