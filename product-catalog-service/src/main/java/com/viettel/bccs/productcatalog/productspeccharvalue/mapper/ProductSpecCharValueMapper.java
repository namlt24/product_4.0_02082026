package com.viettel.bccs.productcatalog.productspeccharvalue.mapper;

import com.viettel.bccs.productcatalog.productspeccharvalue.dto.response.ProductSpecCharValueResponse;
import com.viettel.bccs.productcatalog.productspeccharvalue.entity.ProductSpecCharValueEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductSpecCharValueMapper {

    public ProductSpecCharValueResponse toResponse(ProductSpecCharValueEntity entity) {
        return new ProductSpecCharValueResponse(
                entity.getProductSpecCharValueId(),
                entity.getProductSpecCharId(),
                entity.getValueType(),
                entity.getIsDefault(),
                entity.getValue(),
                entity.getUnitOfMeasure(),
                entity.getValueFrom(),
                entity.getValueTo(),
                entity.getRangeInterval(),
                entity.getStatus(),
                entity.getCreateUser(),
                entity.getCreateDatetime(),
                entity.getUpdateUser(),
                entity.getUpdateDatetime(),
                entity.getName(),
                entity.getSpecificValue(),
                entity.getNote());
    }
}