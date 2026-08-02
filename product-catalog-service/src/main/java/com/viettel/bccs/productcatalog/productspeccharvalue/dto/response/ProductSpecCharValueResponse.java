package com.viettel.bccs.productcatalog.productspeccharvalue.dto.response;

import java.util.Date;

public record ProductSpecCharValueResponse(
        Long productSpecCharValueId,
        Long productSpecCharId,
        String valueType,
        Long isDefault,
        String value,
        String unitOfMeasure,
        String valueFrom,
        String valueTo,
        String rangeInterval,
        String status,
        String createUser,
        Date createDatetime,
        String updateUser,
        Date updateDatetime,
        String name,
        String specificValue,
        String note
) {
}