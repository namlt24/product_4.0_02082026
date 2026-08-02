package com.viettel.bccs.policy.reason.mapper;

import com.viettel.bccs.policy.reason.dto.response.ReasonResponse;
import com.viettel.bccs.policy.reason.entity.ReasonEntity;
import org.springframework.stereotype.Component;

@Component
public class ReasonMapper {

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