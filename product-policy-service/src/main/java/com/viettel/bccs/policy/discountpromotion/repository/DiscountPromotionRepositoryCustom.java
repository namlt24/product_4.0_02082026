package com.viettel.bccs.policy.discountpromotion.repository;

import com.viettel.bccs.policy.discountpromotion.dto.response.DiscountPromotionDTO;
import com.viettel.bccs.policy.discountpromotion.entity.DiscountPromotionEntity;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface DiscountPromotionRepositoryCustom {

    List<DiscountPromotionEntity> getPromotionList(
            Long telecomServiceId,
            boolean checkStatus,
            boolean checkEffectDate,
            LocalDateTime endDate);

    List<DiscountPromotionDTO> getPromFromMapActiveInfosCheckDuplicate(List<MapActiveInfoDTO> tMapActiveInfos, boolean getDuplicateProm);
}