package com.viettel.bccs.policy.ref.refproductpackage.mapper;

import org.springframework.stereotype.Component;

import com.viettel.bccs.policy.ref.refproductpackage.dto.RefProductPackageDTO;
import com.viettel.bccs.policy.ref.refproductpackage.entity.RefProductPackageEntity;

@Component
public class RefProductPackageMapper {

    public RefProductPackageDTO toDTO(RefProductPackageEntity entity) {
        if (entity == null) {
            return null;
        }
        return RefProductPackageDTO.builder()
                .productPackageId(entity.getProductPackageId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .effectDatetime(entity.getEffectDatetime())
                .expireDatetime(entity.getExpireDatetime())
                .updateDatetime(entity.getUpdateDatetime())
                .type(entity.getType())
                .accountingId(entity.getAccountingId())
                .feeType(entity.getFeeType())
                .telecomServiceId(entity.getTelecomServiceId())
                .build();
    }
}