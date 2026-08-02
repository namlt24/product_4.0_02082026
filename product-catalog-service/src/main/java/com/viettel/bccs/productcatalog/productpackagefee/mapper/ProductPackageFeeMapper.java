package com.viettel.bccs.productcatalog.productpackagefee.mapper;

import com.viettel.bccs.productcatalog.productpackagefee.dto.response.ProductPackageFeeDTO;
import com.viettel.bccs.productcatalog.productpackagefee.dto.response.ProductPackageFeeResponse;
import com.viettel.bccs.productcatalog.productpackagefee.entity.ProductPackageFeeEntity;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import org.springframework.stereotype.Component;

@Component
public class ProductPackageFeeMapper {

    public ProductPackageFeeResponse toResponse(ProductPackageFeeEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProductPackageFeeResponse.builder()
                .productPackageFeeId(entity.getProductPackageFeeId())
                .productPackageId(entity.getProductPackageId())
                .pricePolicyId(entity.getPricePolicyId())
                .priceTypeId(entity.getPriceTypeId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .price(entity.getPrice())
                .vat(entity.getVat())
                .effectDatetime(entity.getEffectDatetime())
                .expireDatetime(entity.getExpireDatetime())
                .createDatetime(entity.getCreateDatetime())
                .updateDatetime(entity.getUpdateDatetime())
                .priority(entity.getPriority() != null ? entity.getPriority().longValue() : null)
                .effectType(entity.getEffectType())
                .cronExpression(entity.getCronExpression())
                .realStep(entity.getRealStep())
                .revenueObj(entity.getRevenueObj())
                .createUser(entity.getCreateUser())
                .updateUser(entity.getUpdateUser())
                .fileAttachmentId(entity.getFileAttachmentId())
                .distribute(entity.getDistribute())
                .sapMaterialNumber(entity.getSapMaterialNumber())
                .build();
    }

    public ProductPackageFeeDTO toDTOPackage(ProductPackageFeeEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProductPackageFeeDTO.builder()
                .productPackageFeeId(entity.getProductPackageFeeId())
                .productPackageId(entity.getProductPackageId())
                .pricePolicyId(entity.getPricePolicyId())
                .priceTypeId(entity.getPriceTypeId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .price(DataUtil.safeToLong(entity.getPrice(), null))
                .vat(DataUtil.safeToLong(entity.getVat(), null))
                .effectDatetime(entity.getEffectDatetime())
                .expireDatetime(entity.getExpireDatetime())
                .createDatetime(entity.getCreateDatetime())
                .updateDatetime(entity.getUpdateDatetime())
                .priority(entity.getPriority() != null ? entity.getPriority().shortValue() : null)
                .effectType(entity.getEffectType())
                .cronExpression(entity.getCronExpression())
                .realStep(entity.getRealStep())
                .revenueObj(entity.getRevenueObj())
                .createUser(entity.getCreateUser())
                .updateUser(entity.getUpdateUser())
                .fileAttachmentId(entity.getFileAttachmentId())
                .distribute(entity.getDistribute())
                .sapMaterialNumber(entity.getSapMaterialNumber())
                .build();
    }
}