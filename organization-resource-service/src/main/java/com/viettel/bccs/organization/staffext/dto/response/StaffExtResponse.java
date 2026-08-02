package com.viettel.bccs.organization.staffext.dto.response;

import java.util.Date;

public record StaffExtResponse(
        Long staffExtId,
        Long staffId,
        String key,
        String value,
        String status,
        String createUser,
        Date createDatetime,
        String updateUser,
        Date updateDatetime
) {
}