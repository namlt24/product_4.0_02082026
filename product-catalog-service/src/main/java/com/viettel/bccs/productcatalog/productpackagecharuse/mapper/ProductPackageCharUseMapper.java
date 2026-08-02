package com.viettel.bccs.productcatalog.productpackagecharuse.mapper;

import com.viettel.bccs.productcatalog.productpackagecharuse.dto.response.ProductPackageCharUseDTO;
import com.viettel.bccs.productcatalog.productpackagecharuse.entity.ProductPackageCharUseEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductPackageCharUseMapper {

    public ProductPackageCharUseDTO toDTO(ProductPackageCharUseEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProductPackageCharUseDTO.builder()
                .productPackageCharUseId(entity.getProductPackageCharUseId())
                .productPackageId(entity.getProductPackageId())
                .productSpecCharId(entity.getProductSpecCharId())
                .productSpecCharValueId(entity.getProductSpecCharValueId())
                .status(entity.getStatus())
                .specificValue(entity.getSpecificValue())
                .limited(entity.getLimited())
                .description(entity.getDescription())
                .effectDatetime(entity.getEffectDatetime())
                .expireDatetime(entity.getExpireDatetime())
                .createDatetime(entity.getCreateDatetime())
                .updateDatetime(entity.getUpdateDatetime())
                .createUser(entity.getCreateUser())
                .updateUser(entity.getUpdateUser())
                .build();
    }
}