package com.viettel.bccs.organization.custtype.mapper;

import com.viettel.bccs.organization.custtype.dto.CustTypeDTO;
import com.viettel.bccs.organization.custtype.entity.CustTypeEntity;
import org.springframework.stereotype.Component;

@Component
public class CustTypeMapper {

    public CustTypeDTO toDTO(CustTypeEntity entity) {
        CustTypeDTO dto = new CustTypeDTO();
        dto.setCustType(entity.getCustType());
        dto.setName(entity.getName());
        dto.setCreateUser(entity.getCreateUser());
        dto.setCreateDatetime(entity.getCreateDatetime());
        dto.setUpdateUser(entity.getUpdateUser());
        dto.setUpdateDatetime(entity.getUpdateDatetime());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setGroupType(entity.getGroupType());
        dto.setTax(entity.getTax());
        dto.setPlan(entity.getPlan());
        dto.setRepresentCust(entity.getRepresentCust());
        dto.setCustTypeId(entity.getCustTypeId());
        return dto;
    }
}