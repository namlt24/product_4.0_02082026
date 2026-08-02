package com.viettel.bccs.organization.custtype.repository;

import com.viettel.bccs.organization.custtype.entity.CustTypeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CustTypeRepositoryCustomImpl implements CustTypeRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<CustTypeEntity> getMappingChannelCustType(Long channelTypeId, String groupType) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.* FROM CUST_CHANNEL_TYPE_MAPPING a, CUST_TYPE b ");
        sql.append("WHERE a.CUST_TYPE = b.CUST_TYPE ");
        if (channelTypeId != null) {
            sql.append("AND a.CHANNEL_TYPE_ID = :channelTypeId ");
        }
        sql.append("AND a.STATUS = 1 AND b.GROUP_TYPE = :groupType ");
        sql.append("ORDER BY b.NAME");

        Query query = em.createNativeQuery(sql.toString(), CustTypeEntity.class);
        if (channelTypeId != null) {
            query.setParameter("channelTypeId", channelTypeId);
        }
        query.setParameter("groupType", groupType);

        @SuppressWarnings("unchecked")
        List<CustTypeEntity> resultList = query.getResultList();
        return resultList;
    }
}