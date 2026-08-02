package com.viettel.bccs.organization.channeltype.mapper;

import com.viettel.bccs.organization.channeltype.dto.ChannelTypeDTO;
import com.viettel.bccs.organization.channeltype.entity.ChannelTypeEntity;
import org.springframework.stereotype.Component;

@Component
public class ChannelTypeMapper {

    public ChannelTypeDTO toDTO(ChannelTypeEntity entity) {
        ChannelTypeDTO dto = new ChannelTypeDTO();
        dto.setChannelTypeId(entity.getChannelTypeId());
        dto.setName(entity.getName());
        dto.setStatus(entity.getStatus());
        dto.setObjectType(entity.getObjectType());
        dto.setIsVtUnit(entity.getIsVtUnit());
        dto.setCheckComm(entity.getCheckComm());
        dto.setStockType(entity.getStockType());
        dto.setStockReportTemplate(entity.getStockReportTemplate());
        dto.setTotalDebit(entity.getTotalDebit());
        dto.setAllowAddBatch(entity.getAllowAddBatch());
        dto.setSuffixObjectCode(entity.getSuffixObjectCode());
        dto.setUpdateStaffOwnerRole(entity.getUpdateStaffOwnerRole());
        dto.setDiscountPolicyDefaut(entity.getDiscountPolicyDefaut());
        dto.setPricePolicyDefaut(entity.getPricePolicyDefaut());
        dto.setUpdateBlankCodeRole(entity.getUpdateBlankCodeRole());
        dto.setUpdateObjectInfoRole(entity.getUpdateObjectInfoRole());
        dto.setUpdateShopRole(entity.getUpdateShopRole());
        dto.setGroupChannelTypeId(entity.getGroupChannelTypeId());
        dto.setGroupChannelId(entity.getGroupChannelId());
        dto.setIsVhrChannel(entity.getIsVhrChannel());
        dto.setIsCollChannel(entity.getIsCollChannel());
        dto.setIsNotBlankCode(entity.getIsNotBlankCode());
        dto.setCode(entity.getCode());
        dto.setCreateDatetime(entity.getCreateDatetime());
        dto.setCreateUser(entity.getCreateUser());
        dto.setUpdateUser(entity.getUpdateUser());
        dto.setUpdateDatetime(entity.getUpdateDatetime());
        dto.setPaymentCode(entity.getPaymentCode());
        dto.setPaymentTail(entity.getPaymentTail());
        dto.setAssignCustStatus(entity.getAssignCustStatus());
        dto.setDescription(entity.getDescription());
        return dto;
    }
}