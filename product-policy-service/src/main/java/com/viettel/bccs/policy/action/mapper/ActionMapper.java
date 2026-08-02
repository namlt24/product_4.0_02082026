package com.viettel.bccs.policy.action.mapper;

import com.viettel.bccs.policy.action.dto.response.ActionResponse;
import com.viettel.bccs.policy.action.entity.ActionEntity;
import org.springframework.stereotype.Component;

@Component
public class ActionMapper {

    public ActionResponse toResponse(ActionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ActionResponse(
                entity.getActionCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreateUser(),
                entity.getCreateDatetime(),
                entity.getUpdateUser(),
                entity.getUpdateDatetime(),
                entity.getType(),
                entity.getReasonType()
        );
    }
}