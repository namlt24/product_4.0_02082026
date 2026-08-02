package com.viettel.bccs.policy.action.dto.response;

import java.util.Date;

public record ActionResponse(
        String actionCode,
        String name,
        String description,
        String status,
        String createUser,
        Date createDatetime,
        String updateUser,
        Date updateDatetime,
        String type,
        String reasonType
) {
}