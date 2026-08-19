package com.viettel.bccs.organization.stockchannelmapping.mapper;

import com.viettel.bccs.organization.stockchannelmapping.dto.response.StockChannelMappingResponse;
import com.viettel.bccs.organization.stockchannelmapping.entity.StockChannelMappingEntity;
import com.viettel.bccs.organization.utils.Const;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class StockChannelMappingMapper {

    public StockChannelMappingResponse toResponse(StockChannelMappingEntity entity) {
        return StockChannelMappingResponse.builder()
                .stockChannelMappingId(entity.getStockChannelMappingId())
                .telecomServiceId(entity.getTelecomServiceId())
                .channelTypeId(entity.getChannelTypeId())
                .stockShopId(entity.getStockShopId())
                .shopId(entity.getShopId())
                .staffId(entity.getStaffId())
                .effectDate(entity.getEffectDate())
                .expireDate(entity.getExpireDate())
                .status(entity.getStatus())
                .createUser(entity.getCreateUser())
                .createDatetime(entity.getCreateDatetime())
                .updateUser(entity.getUpdateUser())
                .updateDatetime(entity.getUpdateDatetime())
                .build();
    }

}
