package com.viettel.bccs.policy.ref.refprodpackpotype.mapper;

import org.springframework.stereotype.Component;

import com.viettel.bccs.policy.ref.refprodpackpotype.dto.RefProdPackPoTypeDTO;
import com.viettel.bccs.policy.ref.refprodpackpotype.entity.RefProdPackPoTypeEntity;

@Component
public class RefProdPackPoTypeMapper {

    public RefProdPackPoTypeDTO toDTO(RefProdPackPoTypeEntity entity) {
        if (entity == null) {
            return null;
        }
        return RefProdPackPoTypeDTO.builder()
                .prodPackTypeId(entity.getProdPackTypeId())
                .productPackageId(entity.getProductPackageId())
                .productOfferTypeId(entity.getProductOfferTypeId())
                .status(entity.getStatus())
                .updateStock(entity.getUpdateStock())
                .checkStaffStock(entity.getCheckStaffStock())
                .checkShopStock(entity.getCheckShopStock())
                .updateDatetime(entity.getUpdateDatetime())
                .limitGoods(entity.getLimitGoods())
                .transferIm(entity.getTransferIm())
                .build();
    }
}