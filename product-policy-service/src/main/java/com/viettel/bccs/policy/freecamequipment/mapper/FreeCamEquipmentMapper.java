package com.viettel.bccs.policy.freecamequipment.mapper;

import com.viettel.bccs.policy.freecamequipment.dto.response.FreeCamEquipmentDTO;
import com.viettel.bccs.policy.freecamequipment.entity.FreeCamEquipmentEntity;
import org.springframework.stereotype.Component;

@Component
public class FreeCamEquipmentMapper {

    public FreeCamEquipmentDTO toResponse(FreeCamEquipmentEntity entity) {
        if (entity == null) {
            return null;
        }
        return FreeCamEquipmentDTO.builder()
                .freeCamEquipmentId(entity.getFreeCamEquipmentId())
                .actionCode(entity.getActionCode())
                .reasonId(entity.getReasonId())
                .areaCode(entity.getAreaCode())
                .status(entity.getStatus())
                .camInsideNumber(entity.getCamInsideNumber())
                .camOutsideNumber(entity.getCamOutsideNumber())
                .camMaxNumber(entity.getCamMaxNumber())
                .camInsidePrice(entity.getCamInsidePrice())
                .camOutsidePrice(entity.getCamOutsidePrice())
                .effectDatetime(entity.getEffectDatetime())
                .expireDatetime(entity.getExpireDatetime())
                .createUser(entity.getCreateUser())
                .updateUser(entity.getUpdateUser())
                .description(entity.getDescription())
                .shopCode(entity.getShopCode())
                .staffCode(entity.getStaffCode())
                .createDatetime(entity.getCreateDatetime())
                .updateDatetime(entity.getUpdateDatetime())
                .customerGroup(entity.getCustomerGroup())
                .customerType(entity.getCustomerType())
                .build();
    }
}
