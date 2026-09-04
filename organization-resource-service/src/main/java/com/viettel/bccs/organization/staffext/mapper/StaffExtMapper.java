package com.viettel.bccs.organization.staffext.mapper;

import org.springframework.stereotype.Component;

import com.viettel.bccs.organization.staffext.dto.response.StaffExtResponse;
import com.viettel.bccs.organization.staffext.entity.StaffExtEntity;

@Component
public class StaffExtMapper {

    public StaffExtResponse toResponse(StaffExtEntity entity) {
        return new StaffExtResponse(
                entity.getStaffExtId(),
                entity.getStaffId(),
                entity.getStaffExtKey(),
                entity.getValue(),
                entity.getStatus(),
                entity.getCreateUser(),
                entity.getCreateDatetime(),
                entity.getUpdateUser(),
                entity.getUpdateDatetime());
    }
}
