package com.viettel.bccs.productcatalog.productspecchar.dto.response;

import java.util.Date;

public record ProductSpecCharResponse(
        Long productSpecCharId,
        String name,
        String description,
        String valueType,
        String charType,
        Long minCardinality,
        Long maxCardinality,
        String status,
        String createUser,
        Date createDatetime,
        String updateUser,
        Date updateDatetime,
        String code,
        String productSpecCharTypeId,
        Long valueSetType,
        String responseClass,
        String sqlQuery,
        String displayObject,
        String valueObject,
        String solrQuery,
        String solrCore,
        String solrSchema,
        String dataType,
        String wsWsdl,
        String templateRequest,
        String validatePattern,
        String extData,
        String note
) {
}