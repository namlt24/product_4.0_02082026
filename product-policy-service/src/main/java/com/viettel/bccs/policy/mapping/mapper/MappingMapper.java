package com.viettel.bccs.policy.mapping.mapper;

import com.viettel.bccs.policy.mapping.dto.response.MappingResponse;
import com.viettel.bccs.policy.mapping.entity.MappingEntity;
import org.springframework.stereotype.Component;

@Component
public class MappingMapper {

    public MappingResponse toResponse(MappingEntity entity) {
        if (entity == null) {
            return null;
        }
        return new MappingResponse(
                entity.getId(),
                entity.getVas(),
                entity.getVasName(),
                entity.getProductName(),
                entity.getProductCode(),
                entity.getActionName(),
                entity.getActionCode(),
                entity.getReasonId(),
                entity.getReasonName(),
                entity.getTelServiceId(),
                entity.getSaleServiceId(),
                entity.getSaleServiceName(),
                entity.getSaleServiceCode(),
                entity.getChannel(),
                entity.getStatus(),
                entity.getUserCreate(),
                entity.getUserUpdate(),
                entity.getCreateDatetime(),
                entity.getChangeDatetime(),
                entity.getIp(),
                entity.getEndEffectDate(),
                entity.getTypeMapping(),
                entity.getActionId()
        );
    }
}