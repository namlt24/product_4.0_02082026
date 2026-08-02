package com.viettel.bccs.policy.reason.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.policy.client.ProductPackageClient;
import com.viettel.bccs.policy.reason.dto.response.ReasonResponse;
import com.viettel.bccs.policy.utils.Const;
import com.viettel.bccs.policy.reason.entity.ReasonEntity;
import com.viettel.bccs.policy.reason.mapper.ReasonMapper;
import com.viettel.bccs.policy.reason.repository.ReasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReasonService {

    private final ReasonRepository repository;
    private final ReasonMapper mapper;
    private final ProductPackageClient productPackageClient;

    public ReasonResponse findById(Long id) {
        Optional<ReasonEntity> entity = repository.findById(id);
        if (entity.isEmpty()) {
            throw new BusinessException("BCCS-POLICY-001", "Reason not found with id: " + id);
        }
        return mapper.toResponse(entity.get());
    }

    public List<ReasonResponse> getListReasonByActionCodeAndTelServiceForAudit(
            String actionCode, Long telServiceId, String payType) {
        return getListReasonByActionCodeAndTelServiceForAuditWithMappingChecking(actionCode, telServiceId, payType, null, true);
    }

    public List<ReasonResponse> getListReasonByActionCodeAndTelServiceForAuditWithMappingChecking(
            String actionCode, Long telServiceId, String payType, Integer numProduct, boolean checkStatus) {

        List<ReasonEntity> entities = repository.getListReasonByActionCodeAndTelServiceForAuditWithMappingChecking(actionCode, telServiceId, payType, numProduct, checkStatus);

        return entities.stream()
                .map(mapper::toResponse)
                .toList();
    }
}