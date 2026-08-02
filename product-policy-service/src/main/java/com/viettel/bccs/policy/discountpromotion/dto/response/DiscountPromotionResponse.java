package com.viettel.bccs.policy.discountpromotion.dto.response;

import java.time.LocalDateTime;

public record DiscountPromotionResponse(
        Long discountPromotionId,
        String telecomServiceId,
        String code,
        String name,
        String type,
        String systemType,
        String discountMethod,
        String discountPolicy,
        String subType,
        Long monthCommitment,
        String pricePlan,
        Long monthAmount,
        String status,
        String description,
        String content,
        String areaCode,
        LocalDateTime effectDatetime,
        LocalDateTime expireDatetime,
        String createUser,
        LocalDateTime createDatetime,
        String updateUser,
        LocalDateTime updateDatetime,
        Long cycle,
        String listType,
        Long subListId,
        String note
) {}