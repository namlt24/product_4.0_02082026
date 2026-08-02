package com.viettel.bccs.productcatalog.optionset.dto.response;

import java.util.Date;

public record OptionSetResponse(
        Long optionSetId,
        String code,
        String name,
        String status,
        String createUser,
        Date createDatetime,
        String updateUser,
        Date updateDatetime,
        String description
) {
}