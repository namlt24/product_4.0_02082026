package com.viettel.bccs.productcatalog.productofferrelation.mapper;

import com.viettel.bccs.productcatalog.productofferrelation.dto.response.ProductOfferRelationResponse;
import com.viettel.bccs.productcatalog.productofferrelation.entity.ProductOfferRelationEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductOfferRelationMapper {

    public ProductOfferRelationResponse toResponse(ProductOfferRelationEntity entity) {
        return new ProductOfferRelationResponse(
                entity.getProductOfferRelationId(),
                entity.getRelationTypeId(),
                entity.getMainOfferId(),
                entity.getRelationOfferId(),
                entity.getStatus(),
                entity.getCreateUser(),
                entity.getCreateDatetime(),
                entity.getUpdateUser(),
                entity.getUpdateDatetime(),
                entity.getConfigPhase(),
                entity.getDescription(),
                entity.getEffectDatetime(),
                entity.getExpireDatetime());
    }
}