package com.viettel.bccs.policy.reason.mapper;

import com.viettel.bccs.policy.reason.dto.response.ReasonDTO;
import com.viettel.bccs.policy.reason.dto.response.ReasonResponse;
import com.viettel.bccs.policy.reason.entity.ReasonEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class ReasonMapper {

    public ReasonDTO toDTO(ReasonEntity entity) {
        if (entity == null) {
            return null;
        }
        return ReasonDTO.builder()
                .reasonId(entity.getReasonId())
                .reasonCode(entity.getReasonCode())
                .reasonType(entity.getReasonType())
                .name(entity.getName())
                .payType(entity.getPayType())
                .telService(entity.getTelService())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createUser(entity.getCreateUser())
                .createDatetime(entity.getCreateDatetime())
                .updateUser(entity.getUpdateUser())
                .updateDatetime(entity.getUpdateDatetime())
                .limitNumberIsdn(entity.getLimitNumberIsdn())
                .limitNumberUser(entity.getLimitNumberUser())
                .type(entity.getType())
                .effectDatetime(entity.getEffectDatetime())
                .expireDatetime(entity.getExpireDatetime())
                .priority(entity.getPriority())
                .note(entity.getNote())
                .build();
    }

    public List<ReasonDTO> toDTO(List<ReasonEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        List<ReasonDTO> result = new ArrayList<>(entities.size());
        for (ReasonEntity entity : entities) {
            result.add(toDTO(entity));
        }
        return result;
    }

    public ReasonResponse toResponse(ReasonEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ReasonResponse(
                entity.getReasonId(),
                entity.getReasonCode(),
                entity.getReasonType(),
                entity.getName(),
                entity.getPayType(),
                entity.getTelService(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreateUser(),
                entity.getCreateDatetime(),
                entity.getUpdateUser(),
                entity.getUpdateDatetime(),
                entity.getLimitNumberIsdn(),
                entity.getLimitNumberUser(),
                entity.getType(),
                entity.getEffectDatetime(),
                entity.getExpireDatetime(),
                entity.getPriority(),
                entity.getNote()
        );
    }
}