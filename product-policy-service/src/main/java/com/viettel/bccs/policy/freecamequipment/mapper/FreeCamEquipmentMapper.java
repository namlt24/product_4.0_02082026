package com.viettel.bccs.policy.freecamequipment.mapper;

import com.viettel.bccs.policy.freecamequipment.dto.response.FreeCamEquipmentResponse;
import com.viettel.bccs.policy.freecamequipment.entity.FreeCamEquipmentEntity;
import org.springframework.stereotype.Component;

@Component
public class FreeCamEquipmentMapper {

    public FreeCamEquipmentResponse toResponse(FreeCamEquipmentEntity entity) {
        if (entity == null) {
            return null;
        }
        return new FreeCamEquipmentResponse(
                entity.getFreeCamEquipmentId(),
                entity.getActionCode(),
                entity.getReasonId(),
                entity.getAreaCode(),
                entity.getStatus(),
                entity.getCamInsideNumber(),
                entity.getCamOutsideNumber(),
                entity.getCamMaxNumber(),
                entity.getCamInsidePrice(),
                entity.getCamOutsidePrice(),
                entity.getEffectDatetime(),
                entity.getExpireDatetime(),
                entity.getCreateUser(),
                entity.getUpdateUser(),
                entity.getDescription(),
                entity.getShopCode(),
                entity.getStaffCode(),
                entity.getCreateDatetime(),
                entity.getUpdateDatetime(),
                entity.getCustomerGroup(),
                entity.getCustomerType()
        );
    }
}
