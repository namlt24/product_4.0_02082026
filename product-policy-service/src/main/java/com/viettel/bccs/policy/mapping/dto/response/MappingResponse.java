package com.viettel.bccs.policy.mapping.dto.response;

import java.util.Date;

public record MappingResponse(
        Long id,
        String vas,
        String vasName,
        String productName,
        String productCode,
        String actionName,
        String actionCode,
        Long reasonId,
        String reasonName,
        Long telServiceId,
        Long saleServiceId,
        String saleServiceName,
        String saleServiceCode,
        String channel,
        String status,
        String userCreate,
        String userUpdate,
        Date createDatetime,
        Date changeDatetime,
        String ip,
        Date endEffectDate,
        String typeMapping,
        String actionId
) {
}