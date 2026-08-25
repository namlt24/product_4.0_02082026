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

    /**
     * Map 1 dong ket qua tho (Object[]) tu native query
     * {@code ProductOfferCharUseRepositoryCustom.findSpecCharsByOfferingIds}/
     * {@code findProductOfferCharacter} sang {@link ProductSpecCharValueEntity} (offset cot 27-39).
     * Chuyen tu ProductOfferCharUseService sang day - dung dung tang trach nhiem cua Mapper.
     */
    public ProductSpecCharValueEntity buildValueEntity(Object[] row) {
        return ProductSpecCharValueEntity.builder()
                .productSpecCharValueId(row[27] != null ? ((Number) row[27]).longValue() : null)
                .productSpecCharId(row[28] != null ? ((Number) row[28]).longValue() : null)
                .valueType(str(row[29]))
                .isDefault(row[30] != null ? ((Number) row[30]).longValue() : null)
                .value(str(row[31]))
                .unitOfMeasure(str(row[32]))
                .valueFrom(str(row[33]))
                .valueTo(str(row[34]))
                .rangeInterval(str(row[35]))
                .status(str(row[36]))
                .name(str(row[37]))
                .specificValue(str(row[38]))
                .note(str(row[39]))
                .build();
    }

    private static String str(Object val) {
        return val != null ? val.toString() : null;
    }
}