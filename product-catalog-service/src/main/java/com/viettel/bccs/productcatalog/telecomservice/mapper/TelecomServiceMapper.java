package com.viettel.bccs.productcatalog.telecomservice.mapper;

import com.viettel.bccs.productcatalog.telecomservice.dto.response.TelecomServiceDTO;
import com.viettel.bccs.productcatalog.telecomservice.entity.TelecomServiceEntity;
import org.springframework.stereotype.Component;

@Component
public class TelecomServiceMapper {

    public TelecomServiceDTO toDto(TelecomServiceEntity entity) {
        if (entity == null) {
            return null;
        }
        return TelecomServiceDTO.builder()
                .telecomServiceId(entity.getTelecomServiceId())
                .name(entity.getName())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .serviceAlias(entity.getServiceAlias())
                .createUser(entity.getCreateUser())
                .createDatetime(entity.getCreateDatetime())
                .updateUser(entity.getUpdateUser())
                .updateDatetime(entity.getUpdateDatetime())
                .build();
    }
}
