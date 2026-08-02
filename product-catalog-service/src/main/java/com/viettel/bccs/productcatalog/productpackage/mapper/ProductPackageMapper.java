package com.viettel.bccs.productcatalog.productpackage.mapper;

import com.viettel.bccs.productcatalog.productpackage.dto.response.ProductPackageResponse;
import com.viettel.bccs.productcatalog.productpackage.entity.ProductPackageEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductPackageMapper {

    public ProductPackageResponse toResponse(ProductPackageEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProductPackageResponse.builder()
                .productPackageId(entity.getProductPackageId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .type(entity.getType())
                .saleType(entity.getSaleType())
                .unit(entity.getUnit())
                .effectDatetime(entity.getEffectDatetime())
                .expireDatetime(entity.getExpireDatetime())
                .createDatetime(entity.getCreateDatetime())
                .updateDatetime(entity.getUpdateDatetime())
                .createUser(entity.getCreateUser())
                .updateUser(entity.getUpdateUser())
                .version(entity.getVersion())
                .accountingId(entity.getAccountingId())
                .feeType(entity.getFeeType())
                .telecomServiceId(entity.getTelecomServiceId())
                .note(entity.getNote())
                .note1(entity.getNote1())
                .note2(entity.getNote2())
                .areaGroupId(entity.getAreaGroupId())
                .ownerShopId(entity.getOwnerShopId())
                .sapMaterialNumber(entity.getSapMaterialNumber())
                .itTelcol(entity.getItTelcol())
                .build();
    }
}