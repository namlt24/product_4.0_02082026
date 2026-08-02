package com.viettel.bccs.productcatalog.productoffercharuse.mapper;

import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharValueDTO;
import com.viettel.bccs.productcatalog.productspeccharvalue.entity.ProductSpecCharValueEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductSpecCharValueUseMapper {

    public ProductSpecCharValueDTO toDto(ProductSpecCharValueEntity entity) {
        if (entity == null) return null;
        return ProductSpecCharValueDTO.builder()
                .productSpecCharValueId(entity.getProductSpecCharValueId())
                .productSpecCharId(entity.getProductSpecCharId())
                .valueType(entity.getValueType())
                .isDefault(entity.getIsDefault())
                .value(entity.getValue())
                .unitOfMeasure(entity.getUnitOfMeasure())
                .valueFrom(entity.getValueFrom())
                .valueTo(entity.getValueTo())
                .rangeInterval(entity.getRangeInterval())
                .status(entity.getStatus())
                .name(entity.getName())
                .specificValue(entity.getSpecificValue())
                .note(entity.getNote())
                .build();
    }
}