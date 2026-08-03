package com.viettel.bccs.policy.discountpromotion.mapper;

import com.viettel.bccs.policy.discountpromotion.dto.response.DiscountPromotionDTO;
import com.viettel.bccs.policy.discountpromotion.dto.response.DiscountPromotionResponse;
import com.viettel.bccs.policy.discountpromotion.entity.DiscountPromotionEntity;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
public class DiscountPromotionMapper {

    public DiscountPromotionDTO toDTO(DiscountPromotionEntity entity) {
        if (entity == null) {
            return null;
        }
        return DiscountPromotionDTO.builder()
                .discountPromotionId(entity.getDiscountPromotionId())
                .telecomServiceId(entity.getTelecomServiceId())
                .code(entity.getCode())
                .name(entity.getName())
                .type(entity.getType())
                .systemType(entity.getSystemType())
                .discountMethod(entity.getDiscountMethod())
                .discountPolicy(entity.getDiscountPolicy())
                .subType(entity.getSubType())
                .monthCommitment(entity.getMonthCommitment())
                .pricePlan(entity.getPricePlan())
                .monthAmount(entity.getMonthAmount())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .content(entity.getContent())
                .areaCode(entity.getAreaCode())
                .effectDatetime(toDate(entity.getEffectDatetime()))
                .expireDatetime(toDate(entity.getExpireDatetime()))
                .createUser(entity.getCreateUser())
                .createDatetime(toDate(entity.getCreateDatetime()))
                .updateUser(entity.getUpdateUser())
                .updateDatetime(toDate(entity.getUpdateDatetime()))
                .cycle(entity.getCycle())
                .listType(entity.getListType())
                .subListId(entity.getSubListId())
                .note(entity.getNote())
                .build();
    }

    private static Date toDate(java.time.LocalDateTime localDateTime) {
        return localDateTime == null ? null : Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public List<DiscountPromotionDTO> toDTOList(List<DiscountPromotionEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(this::toDTO).toList();
    }

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