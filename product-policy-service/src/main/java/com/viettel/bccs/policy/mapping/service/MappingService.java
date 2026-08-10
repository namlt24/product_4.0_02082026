package com.viettel.bccs.policy.mapping.service;

import com.viettel.bccs.policy.mapping.dto.response.MappingResponse;
import com.viettel.bccs.policy.mapping.mapper.MappingMapper;
import com.viettel.bccs.policy.mapping.repository.MappingRepository;
import com.viettel.bccs.policy.reason.dto.response.ReasonResponse;
import com.viettel.bccs.policy.reason.mapper.ReasonMapper;
import com.viettel.bccs.policy.utils.Const;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MappingService {

    private final MappingRepository repository;
    private final MappingMapper mapper;
    private final ReasonMapper reasonMapper;

    public List<String> findSaleServiceCodeByReason(Long reasonId) {
        return repository.findSaleServiceCodeByReason(reasonId);
    }

    /**
     * Migrate từ mono: ProductOfferPriceServiceImpl.getPriceInServices gọi
     * mappingService.getMappingReasonProductOfferPrice(temp) (temp = productPackageId).
     */
    public List<ReasonResponse> getMappingReasonProductOfferPrice(Long productPackageId) {
        return repository.getMappingReasonProductOfferPrice(productPackageId)
                .stream()
                .map(reasonMapper::toResponse)
                .toList();
    }

    public List<MappingResponse> getMappingByMultiParams(Long reasonId, String actionCode, Long telServiceId) {
        return repository.findByReasonIdAndActionCodeAndTelServiceIdAndStatus(reasonId, actionCode, telServiceId, Const.STATUS.ACTIVE)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}