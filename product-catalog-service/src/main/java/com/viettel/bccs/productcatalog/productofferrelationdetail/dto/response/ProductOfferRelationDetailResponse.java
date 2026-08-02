package com.viettel.bccs.productcatalog.productofferrelationdetail.dto.response;

import java.util.Date;

public record ProductOfferRelationDetailResponse(
        Long productOfferRelationDetail,
        Long productOfferRelationId,
        Long productSpecCharId,
        Long productSpecCharValueId,
        String status,
        String createUser,
        Date createDatetime,
        String updateUser,
        Date updateDatetime,
        String specificValue,
        String description,
        Date effectDatetime,
        Date expireDatetime
) {
}