package com.viettel.bccs.policy.reasonpause.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.viettel.bccs.policy.reasonpause.dto.response.ReasonPauseDTO;
import com.viettel.bccs.policy.reasonpause.entity.ReasonPauseEntity;

@Component
public class ReasonPauseMapper {

    public ReasonPauseDTO toDTO(ReasonPauseEntity entity) {
        if (entity == null) {
            return null;
        }
        return ReasonPauseDTO.builder()
                .reasonPauseId(entity.getReasonPauseId())
                .numMonth(entity.getNumMonth())
                .price(entity.getPrice())
                .reasonId(entity.getReasonId())
                .status(entity.getStatus())
                .createDatetime(entity.getCreateDatetime())
                .createUser(entity.getCreateUser())
                .updateDatetime(entity.getUpdateDatetime())
                .updateUser(entity.getUpdateUser())
                .build();
    }

    public List<ReasonPauseDTO> toDTO(List<ReasonPauseEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        List<ReasonPauseDTO> result = new ArrayList<>(entities.size());
        for (ReasonPauseEntity entity : entities) {
            result.add(toDTO(entity));
        }
        return result;
    }
}
