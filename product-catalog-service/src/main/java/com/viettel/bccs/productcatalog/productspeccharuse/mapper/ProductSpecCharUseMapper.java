package com.viettel.bccs.productcatalog.productspeccharuse.mapper;

import org.springframework.stereotype.Component;

import com.viettel.bccs.productcatalog.productspeccharuse.dto.response.ProductSpecCharUseResponse;
import com.viettel.bccs.productcatalog.productspeccharuse.entity.ProductSpecCharUseEntity;

@Component("productSpecCharUseMapper")
public class ProductSpecCharUseMapper {

    public ProductSpecCharUseResponse toResponse(ProductSpecCharUseEntity entity) {
        return new ProductSpecCharUseResponse(
                entity.getProdSpecCharUseId(),
                entity.getOrderChar(),
                entity.getProductSpecId(),
                entity.getProductSpecCharId(),
                entity.getProductSpecCharValueId(),
                entity.getStatus(),
                entity.getCreateUser(),
                entity.getCreateDatetime(),
                entity.getUpdateUser(),
                entity.getUpdateDatetime(),
                entity.getSystemType(),
                entity.getSpecificValue(),
                entity.getConfigPhase(),
                entity.getMin(),
                entity.getMax(),
                entity.getIsRequired(),
                entity.getNote());
    }
}
