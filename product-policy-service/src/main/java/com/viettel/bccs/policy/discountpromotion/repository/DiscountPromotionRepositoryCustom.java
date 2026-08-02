package com.viettel.bccs.policy.discountpromotion.repository;

import com.viettel.bccs.policy.discountpromotion.entity.DiscountPromotionEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface DiscountPromotionRepositoryCustom {

    List<DiscountPromotionEntity> getPromotionList(
            Long telecomServiceId,
            boolean checkStatus,
            boolean checkEffectDate,
            LocalDateTime endDate);
}