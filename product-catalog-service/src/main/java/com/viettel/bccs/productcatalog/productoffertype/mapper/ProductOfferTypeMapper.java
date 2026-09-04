package com.viettel.bccs.productcatalog.productoffertype.mapper;

import org.springframework.stereotype.Component;

import com.viettel.bccs.productcatalog.productoffertype.dto.response.ProductOfferTypeDTO;
import com.viettel.bccs.productcatalog.productoffertype.entity.ProductOfferTypeEntity;

@Component
public class ProductOfferTypeMapper {

    public ProductOfferTypeDTO toDto(ProductOfferTypeEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProductOfferTypeDTO.builder()
                .productOfferTypeId(entity.getProductOfferTypeId())
                .parentId(entity.getParentId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createUser(entity.getCreateUser())
                .createDatetime(entity.getCreateDatetime())
                .updateUser(entity.getUpdateUser())
                .updateDatetime(entity.getUpdateDatetime())
                .build();
    }
}