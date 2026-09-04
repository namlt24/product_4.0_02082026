package com.viettel.bccs.productcatalog.productofferrelationdetail.mapper;

import org.springframework.stereotype.Component;

import com.viettel.bccs.productcatalog.productofferrelationdetail.dto.response.ProductOfferRelationDetailResponse;
import com.viettel.bccs.productcatalog.productofferrelationdetail.entity.ProductOfferRelationDetailEntity;

@Component
public class ProductOfferRelationDetailMapper {

    public ProductOfferRelationDetailResponse toResponse(ProductOfferRelationDetailEntity entity) {
        return new ProductOfferRelationDetailResponse(
                entity.getProductOfferRelationDetail(),
                entity.getProductOfferRelationId(),
                entity.getProductSpecCharId(),
                entity.getProductSpecCharValueId(),
                entity.getStatus(),
                entity.getCreateUser(),
                entity.getCreateDatetime(),
                entity.getUpdateUser(),
                entity.getUpdateDatetime(),
                entity.getSpecificValue(),
                entity.getDescription(),
                entity.getEffectDatetime(),
                entity.getExpireDatetime());
    }
}