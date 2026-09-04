package com.viettel.bccs.policy.reason.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.policy.reason.dto.response.ReasonDTO;
import com.viettel.bccs.policy.reason.mapper.ReasonMapper;
import com.viettel.bccs.policy.reason.repository.ReasonRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReasonMappingLookupService {

    private final ReasonRepository repository;
    private final ReasonMapper mapper;

    @Cacheable(value = "reasonMappingLookupCache",
            key = "#actionCode + ':' + #telServiceId + ':' + #numProduct + ':' + #productOfferType"
                    + " + ':' + T(String).join(',', #excludeProdOfferTypeIds.stream().sorted().toList())")
    public List<ReasonDTO> getByActionCodeOrderByIdWithMappingChecking(String actionCode, Long telServiceId,
            Long numProduct, String productOfferType, List<String> excludeProdOfferTypeIds) {
        return mapper.toDTO(repository.getByActionCodeOrderByIdWithMappingChecking(
                actionCode, telServiceId, numProduct, productOfferType, excludeProdOfferTypeIds));
    }
}
