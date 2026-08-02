package com.viettel.bccs.productcatalog.optionset.dto.response;

import java.util.Date;

public record OptionSetValueResponse(
        Long optionSetValueId,
        Long optionSetId,
        String optionSetCode,
        String name,
        String value,
        String status,
        String description,
        String createUser,
        Date createDatetime,
        String updateUser,
        Date updateDatetime,
        Long parentId
) {
}