package com.viettel.bccs.productcatalog.prodpackshop.mapper;

import com.viettel.bccs.productcatalog.prodpackshop.dto.response.ProdPackShopDTO;
import com.viettel.bccs.productcatalog.prodpackshop.entity.ProdPackShopEntity;
import org.springframework.stereotype.Component;

@Component
public class ProdPackShopMapper {

    public ProdPackShopDTO toDTO(ProdPackShopEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProdPackShopDTO.builder()
                .prodPackShopId(entity.getProdPackShopId())
                .shopId(entity.getShopId())
                .prodPackTypeId(entity.getProdPackTypeId())
                .status(entity.getStatus())
                .createUser(entity.getCreateUser())
                .createDatetime(entity.getCreateDatetime())
                .updateUser(entity.getUpdateUser())
                .updateDatetime(entity.getUpdateDatetime())
                .build();
    }
}