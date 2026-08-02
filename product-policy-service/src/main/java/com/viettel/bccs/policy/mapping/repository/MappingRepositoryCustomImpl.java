package com.viettel.bccs.policy.mapping.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MappingRepositoryCustomImpl implements MappingRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<String> findSaleServiceCodeByReason(Long reasonId) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT SALE_SERVICE_CODE FROM MAPPING ");
        sql.append(" WHERE REASON_ID = :reasonId AND STATUS = 1");

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("reasonId", reasonId);
        return query.getResultList();
    }
}