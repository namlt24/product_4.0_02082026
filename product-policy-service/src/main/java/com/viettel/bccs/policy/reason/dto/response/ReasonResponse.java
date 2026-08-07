package com.viettel.bccs.policy.reason.dto.response;

import java.util.Date;

public record ReasonResponse(
        Long reasonId,
        String reasonCode,
        String reasonType,
        String name,
        String payType,
        String telService,
        String description,
        String status,
        String createUser,
        Date createDatetime,
        String updateUser,
        Date updateDatetime,
        Long limitNumberIsdn,
        Long limitNumberUser,
        String type,
        Date effectDatetime,
        Date expireDatetime,
        Long priority,
        String note
) {}
