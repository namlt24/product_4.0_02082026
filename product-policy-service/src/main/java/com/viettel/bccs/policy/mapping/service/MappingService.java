package com.viettel.bccs.policy.mapping.service;

import com.viettel.bccs.policy.mapping.mapper.MappingMapper;
import com.viettel.bccs.policy.mapping.repository.MappingRepository;
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
}