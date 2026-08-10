package com.viettel.bccs.policy.freecamequipment.dto.response;

import java.math.BigDecimal;
import java.util.Date;

public record FreeCamEquipmentResponse(
        Long freeCamEquipmentId,
        String actionCode,
        Long reasonId,
        String areaCode,
        String status,
        Long camInsideNumber,
        Long camOutsideNumber,
        Long camMaxNumber,
        BigDecimal camInsidePrice,
        BigDecimal camOutsidePrice,
        Date effectDatetime,
        Date expireDatetime,
        String createUser,
        String updateUser,
        String description,
        String shopCode,
        String staffCode,
        Date createDatetime,
        Date updateDatetime,
        String customerGroup,
        String customerType
) {}
