package com.viettel.bccs.organization.identitytype.mapper;

import org.springframework.stereotype.Component;

import com.viettel.bccs.organization.identitytype.dto.IdentityTypeDTO;
import com.viettel.bccs.organization.identitytype.entity.IdentityTypeEntity;

@Component
public class IdentityTypeMapper {

    public IdentityTypeDTO toDTO(IdentityTypeEntity entity) {
        IdentityTypeDTO dto = new IdentityTypeDTO();
        dto.setIdType(entity.getIdType());
        dto.setName(entity.getName());
        dto.setStatus(entity.getStatus());
        dto.setDescription(entity.getDescription());
        dto.setMinLength(entity.getMinLength());
        dto.setMaxLength(entity.getMaxLength());
        dto.setValuePattern(entity.getValuePattern());
        return dto;
    }
}
