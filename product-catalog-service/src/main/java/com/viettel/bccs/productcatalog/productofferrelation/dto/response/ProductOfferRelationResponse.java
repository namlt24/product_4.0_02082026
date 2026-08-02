package com.viettel.bccs.productcatalog.productofferrelation.dto.response;

import java.util.Date;

public record ProductOfferRelationResponse(
        Long productOfferRelationId,
        Long relationTypeId,
        Long mainOfferId,
        Long relationOfferId,
        String status,
        String createUser,
        Date createDatetime,
        String updateUser,
        Date updateDatetime,
        String configPhase,
        String description,
        Date effectDatetime,
        Date expireDatetime
) {
}