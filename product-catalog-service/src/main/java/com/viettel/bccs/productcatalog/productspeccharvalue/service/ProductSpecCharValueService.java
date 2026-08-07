package com.viettel.bccs.productcatalog.productspeccharvalue.service;

import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharValueDTO;
import com.viettel.bccs.productcatalog.productspeccharvalue.dto.response.ProductSpecCharValueResponse;
import com.viettel.bccs.productcatalog.productspeccharvalue.mapper.ProductSpecCharValueMapper;
import com.viettel.bccs.productcatalog.productspeccharvalue.repository.ProductSpecCharValueRepository;
import com.viettel.bccs.productcatalog.utils.Const;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<ProductSpecCharValueDTO> getByProductSpecCharCodeAndProductOfferingId(String deviceTypeCamCharCode, Long productOfferId) {
        // TODO: stub tạm để compile được cho việc test local — chưa có logic thật, cần hoàn thiện lại
        return List.of();
    }
}