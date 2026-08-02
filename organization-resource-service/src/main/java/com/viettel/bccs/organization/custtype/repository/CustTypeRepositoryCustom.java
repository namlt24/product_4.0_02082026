package com.viettel.bccs.organization.custtype.repository;

import com.viettel.bccs.organization.custtype.entity.CustTypeEntity;

import java.util.List;

public interface CustTypeRepositoryCustom {

    List<CustTypeEntity> getMappingChannelCustType(Long channelTypeId, String groupType);
}