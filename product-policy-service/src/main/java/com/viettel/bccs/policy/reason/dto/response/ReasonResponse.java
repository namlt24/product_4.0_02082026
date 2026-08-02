package com.viettel.bccs.policy.reason.dto.response;

import java.time.LocalDateTime;

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
        LocalDateTime createDatetime,
        String updateUser,
        LocalDateTime updateDatetime,
        Long limitNumberIsdn,
        Long limitNumberUser,
        String type,
        LocalDateTime effectDatetime,
        LocalDateTime expireDatetime,
        Long priority,
        String note
) {}