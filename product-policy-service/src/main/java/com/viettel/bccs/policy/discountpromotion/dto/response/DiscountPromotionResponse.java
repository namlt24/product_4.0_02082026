package com.viettel.bccs.policy.discountpromotion.dto.response;

import java.util.Date;

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
        Date effectDatetime,
        Date expireDatetime,
        String createUser,
        Date createDatetime,
        String updateUser,
        Date updateDatetime,
        Long cycle,
        String listType,
        Long subListId,
        String note
) {}