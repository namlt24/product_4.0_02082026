package com.viettel.bccs.policy.discountpromotioncharuse.dto.response;

import java.util.Date;

public record DiscountPromotionCharUseResponse(
        Long discountPromotionCharUseId,
        Long discountPromotionId,
        Long productSpecCharValueId,
        Long productSpecCharId,
        String createUser,
        Date createDatetime,
        String updateUser,
        Date updateDatetime,
        String status,
        String specificValue,
        Date effectDatetime,
        Date expireDatetime,
        Long limited,
        String note
) {}