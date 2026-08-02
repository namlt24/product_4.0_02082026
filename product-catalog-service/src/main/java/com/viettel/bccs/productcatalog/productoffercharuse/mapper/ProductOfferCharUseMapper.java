package com.viettel.bccs.productcatalog.productoffercharuse.mapper;

import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductOfferCharUseDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.entity.ProductOfferCharUseEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductOfferCharUseMapper {

    public ProductOfferCharUseDTO toDto(ProductOfferCharUseEntity entity) {
        if (entity == null) return null;
        return ProductOfferCharUseDTO.builder()
                .productOfferCharUseId(entity.getProductOfferCharUseId())
                .orderChar(entity.getOrderChar())
                .type(entity.getType())
                .productOfferingId(entity.getProductOfferingId())
                .productSpecCharValueId(entity.getProductSpecCharValueId())
                .productSpecCharId(entity.getProductSpecCharId())
                .createUser(entity.getCreateUser())
                .createDatetime(entity.getCreateDatetime())
                .updateUser(entity.getUpdateUser())
                .updateDatetime(entity.getUpdateDatetime())
                .status(entity.getStatus())
                .specificValue(entity.getSpecificValue())
                .effectDatetime(entity.getEffectDatetime())
                .expireDatetime(entity.getExpireDatetime())
                .limited(entity.getLimited())
                .description(entity.getDescription())
                .build();
    }
}