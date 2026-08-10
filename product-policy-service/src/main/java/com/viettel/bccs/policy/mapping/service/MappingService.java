package com.viettel.bccs.policy.mapping.service;

import com.viettel.bccs.policy.mapping.dto.response.MappingResponse;
import com.viettel.bccs.policy.mapping.mapper.MappingMapper;
import com.viettel.bccs.policy.mapping.repository.MappingRepository;
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

    public List<String> findSaleServiceCodeByReason(Long reasonId) {
        return repository.findSaleServiceCodeByReason(reasonId);
    }

    public List<MappingResponse> getMappingByMultiParams(Long reasonId, String actionCode, Long telServiceId) {
        return repository.findByReasonIdAndActionCodeAndTelServiceIdAndStatus(reasonId, actionCode, telServiceId, Const.STATUS.ACTIVE)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}