package com.viettel.bccs.organization.custchanneltype.mapper;

import org.springframework.stereotype.Component;

import com.viettel.bccs.organization.custchanneltype.dto.CustChannelTypeMappingDTO;
import com.viettel.bccs.organization.custchanneltype.entity.CustChannelTypeMappingEntity;

@Component
public class CustChannelTypeMappingMapper {

    public CustChannelTypeMappingDTO toDTO(CustChannelTypeMappingEntity entity) {
        CustChannelTypeMappingDTO dto = new CustChannelTypeMappingDTO();
        dto.setCustChannelTypeMapId(entity.getCustChannelTypeMapId());
        dto.setCustType(entity.getCustType());
        dto.setChannelTypeId(entity.getChannelTypeId());
        dto.setStatus(entity.getStatus());
        dto.setCreateUser(entity.getCreateUser());
        dto.setCreateDatetime(entity.getCreateDatetime());
        dto.setUpdateUser(entity.getUpdateUser());
        dto.setUpdateDatetime(entity.getUpdateDatetime());
        return dto;
    }
}
