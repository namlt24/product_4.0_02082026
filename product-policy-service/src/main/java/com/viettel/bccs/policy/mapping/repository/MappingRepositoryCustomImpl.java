package com.viettel.bccs.policy.mapping.repository;

import com.viettel.bccs.policy.reason.entity.ReasonEntity;
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

    @Override
    @SuppressWarnings("unchecked")
    public List<ReasonEntity> getMappingReasonProductOfferPrice(Long productPackageId) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT c.* FROM MAPPING a, REASON c ");
        sql.append(" WHERE a.REASON_ID = c.REASON_ID ");
        sql.append(" AND a.STATUS = 1 AND c.STATUS = 1 ");
        sql.append(" AND (a.END_EFFECT_DATE IS NULL OR a.END_EFFECT_DATE >= TRUNC(SYSDATE)) ");
        sql.append(" AND a.SALE_SERVICE_ID = :productPackageId");

        Query query = em.createNativeQuery(sql.toString(), ReasonEntity.class);
        query.setParameter("productPackageId", productPackageId);
        return query.getResultList();
    }
}