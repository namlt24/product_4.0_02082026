package com.viettel.bccs.productcatalog.product.mapper;

import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingDTO;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingResponse;
import com.viettel.bccs.productcatalog.product.entity.ProductOfferingEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductOfferingMapper {

    public ProductOfferingResponse toResponse(ProductOfferingEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ProductOfferingResponse(
            entity.getProductOfferingId(),
            entity.getProductSpecId(),
            entity.getProductOfferTypeId(),
            entity.getName(),
            entity.getCode(),
            entity.getSubType(),
            entity.getTelecomServiceId(),
            entity.getDescription(),
            entity.getStatus(),
            entity.getEffectDatetime(),
            entity.getExpireDatetime(),
            entity.getCreateUser(),
            entity.getCreateDatetime(),
            entity.getUpdateUser(),
            entity.getUpdateDatetime(),
            entity.getVersion(),
            entity.getCheckSerial(),
            entity.getCheckDeposit(),
            entity.getUnit(),
            entity.getAccountingModelCode(),
            entity.getAccountingModelName(),
            entity.getAccountingName(),
            entity.getAccountingCode(),
            entity.getDemoDuration(),
            entity.getIsDemo(),
            entity.getDeviceType(),
            entity.getTransceiver(),
            entity.getStockModelType(),
            entity.getOwnerShopId(),
            entity.getReturnStockWhenCancelled(),
            entity.getReturnStockWhenCancelled1(),
            entity.getSapMaterialNumber(),
            entity.getUsageId(),
            entity.getDistribute(),
            entity.getNumMonth(),
            entity.getIsBundle(),
            entity.getSapProductType()
        );
    }

    public ProductOfferingDTO toDto(ProductOfferingEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProductOfferingDTO.builder()
            .productOfferingId(entity.getProductOfferingId())
            .productSpecId(entity.getProductSpecId())
            .productOfferTypeId(entity.getProductOfferTypeId())
            .name(entity.getName())
            .code(entity.getCode())
            .subType(entity.getSubType())
            .telecomServiceId(entity.getTelecomServiceId())
            .description(entity.getDescription())
            .status(entity.getStatus())
            .effectDatetime(entity.getEffectDatetime())
            .expireDatetime(entity.getExpireDatetime())
            .createUser(entity.getCreateUser())
            .createDatetime(entity.getCreateDatetime())
            .updateUser(entity.getUpdateUser())
            .updateDatetime(entity.getUpdateDatetime())
            .version(entity.getVersion())
            .checkSerial(entity.getCheckSerial() != null ? entity.getCheckSerial().shortValue() : null)
            .checkDeposit(entity.getCheckDeposit() != null ? entity.getCheckDeposit().shortValue() : null)
            .unit(entity.getUnit())
            .accountingModelCode(entity.getAccountingModelCode())
            .accountingModelName(entity.getAccountingModelName())
            .accountingName(entity.getAccountingName())
            .accountingCode(entity.getAccountingCode())
            .demoDuration(entity.getDemoDuration())
            .isDemo(entity.getIsDemo() != null ? entity.getIsDemo().shortValue() : null)
            .deviceType(entity.getDeviceType())
            .transceiver(entity.getTransceiver() != null ? entity.getTransceiver().shortValue() : null)
            .stockModelType(entity.getStockModelType() != null ? entity.getStockModelType().shortValue() : null)
            .ownerShopId(entity.getOwnerShopId())
            .sapMaterialNumber(entity.getSapMaterialNumber())
            .usageId(entity.getUsageId())
            .isBundle(entity.getIsBundle())
            .sapProductType(entity.getSapProductType())
            .build();
    }
}