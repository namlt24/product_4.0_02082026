package com.viettel.bccs.policy.ref.refproductpackagefee.mapper;

import com.viettel.bccs.policy.ref.refproductpackagefee.dto.RefProductPackageFeeDTO;
import com.viettel.bccs.policy.ref.refproductpackagefee.entity.RefProductPackageFeeEntity;
import org.springframework.stereotype.Component;

@Component
public class RefProductPackageFeeMapper {

    public RefProductPackageFeeDTO toDTO(RefProductPackageFeeEntity entity) {
        if (entity == null) {
            return null;
        }
        return RefProductPackageFeeDTO.builder()
                .productPackageFeeId(entity.getProductPackageFeeId())
                .productPackageId(entity.getProductPackageId())
                .pricePolicyId(entity.getPricePolicyId())
                .priceTypeId(entity.getPriceTypeId())
                .name(entity.getName())
                .price(entity.getPrice())
                .vat(entity.getVat())
                .effectDatetime(entity.getEffectDatetime())
                .expireDatetime(entity.getExpireDatetime())
                .status(entity.getStatus())
                .build();
    }
}