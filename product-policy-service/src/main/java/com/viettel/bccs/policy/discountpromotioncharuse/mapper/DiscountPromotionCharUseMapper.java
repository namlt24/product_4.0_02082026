package com.viettel.bccs.policy.discountpromotioncharuse.mapper;

import org.springframework.stereotype.Component;

import com.viettel.bccs.policy.discountpromotioncharuse.dto.response.DiscountPromotionCharUseResponse;
import com.viettel.bccs.policy.discountpromotioncharuse.entity.DiscountPromotionCharUseEntity;

@Component
public class DiscountPromotionCharUseMapper {

    public DiscountPromotionCharUseResponse toResponse(DiscountPromotionCharUseEntity entity) {
        if (entity == null) {
            return null;
        }
        return new DiscountPromotionCharUseResponse(
                entity.getDiscountPromotionCharUseId(),
                entity.getDiscountPromotionId(),
                entity.getProductSpecCharValueId(),
                entity.getProductSpecCharId(),
                entity.getCreateUser(),
                entity.getCreateDatetime(),
                entity.getUpdateUser(),
                entity.getUpdateDatetime(),
                entity.getStatus(),
                entity.getSpecificValue(),
                entity.getEffectDatetime(),
                entity.getExpireDatetime(),
                entity.getLimited(),
                entity.getNote()
        );
    }
}