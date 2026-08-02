package com.viettel.bccs.policy.discountpromotioncharuse.dto.response;

import java.time.LocalDateTime;

public record DiscountPromotionCharUseResponse(
        Long discountPromotionCharUseId,
        Long discountPromotionId,
        Long productSpecCharValueId,
        Long productSpecCharId,
        String createUser,
        LocalDateTime createDatetime,
        String updateUser,
        LocalDateTime updateDatetime,
        String status,
        String specificValue,
        LocalDateTime effectDatetime,
        LocalDateTime expireDatetime,
        Long limited,
        String note
) {}