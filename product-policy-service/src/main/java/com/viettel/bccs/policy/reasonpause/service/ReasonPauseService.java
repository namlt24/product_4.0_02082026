package com.viettel.bccs.policy.reasonpause.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.policy.reasonpause.dto.response.ReasonPauseDTO;
import com.viettel.bccs.policy.reasonpause.entity.ReasonPauseEntity;
import com.viettel.bccs.policy.reasonpause.mapper.ReasonPauseMapper;
import com.viettel.bccs.policy.reasonpause.repository.ReasonPauseRepository;
import com.viettel.bccs.policy.utils.Const;
import com.viettel.bccs.policy.utils.DataUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReasonPauseService {

    private final ReasonPauseRepository repository;
    private final ReasonPauseMapper mapper;

    public ReasonPauseDTO findById(Long id) {
        Optional<ReasonPauseEntity> entity = repository.findById(id);
        if (entity.isEmpty()) {
            throw new BusinessException("BCCS-POLICY-001", "Reason pause not found with id: " + id);
        }
        return mapper.toDTO(entity.get());
    }

    public List<ReasonPauseDTO> getReasonPauseByReasonId(Long reasonId) {
        List<ReasonPauseEntity> entities = repository.findByReasonIdAndStatus(reasonId, Const.STATUS.ACTIVE);
        return mapper.toDTO(entities);
    }

    public Map<Long, List<ReasonPauseDTO>> getReasonPauseByReasonIds(List<Long> reasonIds) {
        if (DataUtil.isNullOrEmpty(reasonIds)) {
            return Map.of();
        }
        List<ReasonPauseDTO> dtos = mapper.toDTO(repository.findByReasonIdInAndStatus(reasonIds, Const.STATUS.ACTIVE));
        Map<Long, List<ReasonPauseDTO>> result = new HashMap<>();
        for (ReasonPauseDTO dto : dtos) {
            result.computeIfAbsent(dto.getReasonId(), k -> new ArrayList<>())
                    .add(dto);
        }
        return result;
    }
}
