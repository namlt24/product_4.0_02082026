package com.viettel.bccs.productcatalog.prodpackproductoffertype.mapper;

import org.springframework.stereotype.Component;

import com.viettel.bccs.productcatalog.prodpackproductoffertype.dto.response.ProdPackProductOfferTypeDTO;
import com.viettel.bccs.productcatalog.prodpackproductoffertype.entity.ProdPackProductOfferTypeEntity;

@Component
public class ProdPackProductOfferTypeMapper {

    public ProdPackProductOfferTypeDTO toDto(ProdPackProductOfferTypeEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProdPackProductOfferTypeDTO.builder()
                .prodPackTypeId(entity.getProdPackTypeId())
                .productPackageId(entity.getProductPackageId())
                .productOfferTypeId(entity.getProductOfferTypeId())
                .status(entity.getStatus())
                .updateStock(entity.getUpdateStock())
                .checkStaffStock(entity.getCheckStaffStock())
                .checkShopStock(entity.getCheckShopStock())
                .createUser(entity.getCreateUser())
                .createDatetime(entity.getCreateDatetime())
                .updateUser(entity.getUpdateUser())
                .updateDatetime(entity.getUpdateDatetime())
                .require(entity.getRequire())
                .description(entity.getDescription())
                .limitGoods(entity.getLimitGoods())
                .distribute(entity.getDistribute())
                .transferIm(entity.getTransferIm())
                .build();
    }
}