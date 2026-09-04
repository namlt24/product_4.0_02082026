package com.viettel.bccs.organization.channeltype.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.viettel.bccs.organization.utils.Const;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChannelTypeRepositoryCustomImpl implements ChannelTypeRepositoryCustom {

    private final EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<Long> findActiveChannelTypeIds() {
        String sql = "SELECT CAST(CHANNEL_TYPE_ID AS NUMBER(19)) FROM CHANNEL_TYPE "
                + "WHERE STATUS = :status ORDER BY CHANNEL_TYPE_ID";

        Query query = em.createNativeQuery(sql, Long.class);
        query.setParameter("status", Const.Status.ACTIVE);

        return query.getResultList();
    }
}
