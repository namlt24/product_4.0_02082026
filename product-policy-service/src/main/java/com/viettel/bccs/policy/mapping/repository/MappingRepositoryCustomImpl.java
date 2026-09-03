package com.viettel.bccs.policy.mapping.repository;

import com.viettel.bccs.policy.reason.entity.ReasonEntity;
import com.viettel.bccs.policy.utils.DataUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    @SuppressWarnings("unchecked")
    public String getSaleServiceCode(Long telecomServiceId, Long reasonId, String productCode, String actionCode) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT m.SALE_SERVICE_CODE FROM MAPPING m, REASON r, ACTION a ");
        sql.append(" WHERE m.REASON_ID = r.REASON_ID ");
        sql.append("   AND a.REASON_TYPE = m.ACTION_CODE ");
        sql.append("   AND r.STATUS = '1' AND m.STATUS = '1' AND a.STATUS = '1' ");

        if (DataUtil.notNullOrEmpty(actionCode)) {
            sql.append("   AND a.ACTION_CODE = :actionCode ");
            params.put("actionCode", actionCode);
        }

        sql.append("   AND m.REASON_ID = :reasonId ");
        params.put("reasonId", reasonId);

        if (telecomServiceId != null && telecomServiceId != 0L) {
            sql.append("   AND m.TEL_SERVICE_ID = :telecomServiceId ");
            params.put("telecomServiceId", telecomServiceId);
        } else {
            sql.append("   AND m.TEL_SERVICE_ID IS NULL ");
        }

        if (DataUtil.notNullOrEmpty(productCode)) {
            sql.append("   AND (m.PRODUCT_CODE = :productCode OR m.PRODUCT_CODE IS NULL) ");
            params.put("productCode", productCode);
        } else {
            sql.append("   AND m.PRODUCT_CODE IS NULL ");
        }

        sql.append(" ORDER BY m.PRODUCT_CODE");

        Query query = em.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);
        query.setMaxResults(1);

        List<Object> result = query.getResultList();
        return result.isEmpty() ? null : (String) result.get(0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> getLstMapPackageByActionCodeAndReasonCodes(List<String> reasonCodes, String actionCode) {

        if (DataUtil.isNullOrEmpty(reasonCodes) || DataUtil.isNullOrEmpty(actionCode)) {
            return Collections.emptyMap();
        }

        StringBuilder sqlQuery = new StringBuilder();

        sqlQuery.append(" SELECT r.REASON_CODE, m.SALE_SERVICE_CODE ");
        sqlQuery.append(" FROM MAPPING m, REASON r, ACTION a ");

        sqlQuery.append(" WHERE m.REASON_ID    = r.REASON_ID ");
        sqlQuery.append(" AND a.REASON_TYPE      = m.ACTION_CODE ");
        sqlQuery.append(" AND m.STATUS      = 1 ");
        sqlQuery.append(" AND r.STATUS      = 1 ");
        sqlQuery.append(" AND a.STATUS      = 1 ");
        sqlQuery.append(" AND m.TEL_SERVICE_ID IS NOT NULL ");


        sqlQuery.append(" AND r.REASON_CODE IN (:reasonCodes) ");
        sqlQuery.append(" AND a.ACTION_CODE = :actionCode  ");



        Query query = em.createNativeQuery(sqlQuery.toString());

        query.setParameter("reasonCodes", reasonCodes);
        query.setParameter("actionCode", actionCode);


        List<Object[]> resultList = query.getResultList();

        return resultList.stream()
                .collect(Collectors.toMap(
                        row -> DataUtil.safeToString(row[0]),
                        row -> DataUtil.safeToString(row[1]),
                        (existing, replacement) -> existing
                ));
    }
}