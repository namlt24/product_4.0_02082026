package com.viettel.bccs.organization.custtype.repository;

import java.util.List;

import com.viettel.bccs.organization.custtype.entity.CustTypeEntity;

public interface CustTypeRepositoryCustom {

    List<CustTypeEntity> getMappingChannelCustType(Long channelTypeId, String groupType);
}
