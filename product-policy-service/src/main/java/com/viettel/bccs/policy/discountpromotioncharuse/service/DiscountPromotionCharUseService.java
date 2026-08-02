package com.viettel.bccs.policy.discountpromotioncharuse.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.policy.discountpromotioncharuse.dto.response.DiscountPromotionCharUseResponse;
import com.viettel.bccs.policy.discountpromotioncharuse.entity.DiscountPromotionCharUseEntity;
import com.viettel.bccs.policy.discountpromotioncharuse.mapper.DiscountPromotionCharUseMapper;
import com.viettel.bccs.policy.discountpromotioncharuse.repository.DiscountPromotionCharUseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscountPromotionCharUseService {

    private final DiscountPromotionCharUseRepository repository;
    private final DiscountPromotionCharUseMapper mapper;

    public DiscountPromotionCharUseResponse findById(Long id) {
        Optional<DiscountPromotionCharUseEntity> entity = repository.findById(id);
        if (entity.isEmpty()) {
            throw new BusinessException("BCCS-POLICY-001", "Discount promotion char use not found with id: " + id);
        }
        return mapper.toResponse(entity.get());
    }
}