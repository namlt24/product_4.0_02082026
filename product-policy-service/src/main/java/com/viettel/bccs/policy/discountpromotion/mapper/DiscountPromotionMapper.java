package com.viettel.bccs.policy.discountpromotion.mapper;

import com.viettel.bccs.policy.discountpromotion.dto.response.DiscountPromotionResponse;
import com.viettel.bccs.policy.discountpromotion.entity.DiscountPromotionEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DiscountPromotionMapper {

    public DiscountPromotionResponse toResponse(DiscountPromotionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new DiscountPromotionResponse(
                entity.getDiscountPromotionId(),
                entity.getTelecomServiceId(),
                entity.getCode(),
                entity.getName(),
                entity.getType(),
                entity.getSystemType(),
                entity.getDiscountMethod(),
                entity.getDiscountPolicy(),
                entity.getSubType(),
                entity.getMonthCommitment(),
                entity.getPricePlan(),
                entity.getMonthAmount(),
                entity.getStatus(),
                entity.getDescription(),
                entity.getContent(),
                entity.getAreaCode(),
                entity.getEffectDatetime(),
                entity.getExpireDatetime(),
                entity.getCreateUser(),
                entity.getCreateDatetime(),
                entity.getUpdateUser(),
                entity.getUpdateDatetime(),
                entity.getCycle(),
                entity.getListType(),
                entity.getSubListId(),
                entity.getNote()
        );
    }

    public List<DiscountPromotionResponse> toResponseList(List<DiscountPromotionEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(this::toResponse).toList();
    }
}