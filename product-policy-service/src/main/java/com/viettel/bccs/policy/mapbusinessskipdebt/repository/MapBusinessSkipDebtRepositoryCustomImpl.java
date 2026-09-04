package com.viettel.bccs.policy.mapbusinessskipdebt.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.viettel.bccs.policy.mapbusinessskipdebt.entity.MapBusinessSkipDebtEntity;
import com.viettel.bccs.policy.utils.DataUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class MapBusinessSkipDebtRepositoryCustomImpl implements MapBusinessSkipDebtRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<MapBusinessSkipDebtEntity> findActiveByActionCodeAndTelecomServiceId(
            String actionCode, Long telecomServiceId) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT * FROM MAP_BUSINESS_SKIP_DEBT ");
        sql.append(" WHERE STATUS = 1 ");
        sql.append(" AND TRUNC(SYSDATE) >= TRUNC(EFFECT_DATETIME) ");
        sql.append(" AND (EXPIRE_DATETIME IS NULL OR TRUNC(EXPIRE_DATETIME) >= TRUNC(SYSDATE)) ");

        if (DataUtil.notNullObject(actionCode)) {
            sql.append(" AND ACTION_CODE = :actionCode ");
        }
        if (DataUtil.notNullObject(telecomServiceId)) {
            sql.append(" AND TELECOM_SERVICE_ID = :telecomServiceId ");
        }

        sql.append(" ORDER BY CREATE_DATETIME DESC");

        Query query = em.createNativeQuery(sql.toString(), MapBusinessSkipDebtEntity.class);

        if (DataUtil.notNullObject(actionCode)) {
            query.setParameter("actionCode", actionCode);
        }
        if (DataUtil.notNullObject(telecomServiceId)) {
            query.setParameter("telecomServiceId", telecomServiceId);
        }

        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MapBusinessSkipDebtEntity> findActiveByShopId(Long shopId) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT * FROM MAP_BUSINESS_SKIP_DEBT ");
        sql.append(" WHERE STATUS = 1 ");
        sql.append(" AND SHOP_ID = :shopId ");
        sql.append(" AND TRUNC(SYSDATE) >= TRUNC(EFFECT_DATETIME) ");
        sql.append(" AND (EXPIRE_DATETIME IS NULL OR TRUNC(EXPIRE_DATETIME) >= TRUNC(SYSDATE)) ");
        sql.append(" ORDER BY CREATE_DATETIME DESC");

        Query query = em.createNativeQuery(sql.toString(), MapBusinessSkipDebtEntity.class);
        query.setParameter("shopId", shopId);
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MapBusinessSkipDebtEntity> findActiveByStaffId(Long staffId) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT * FROM MAP_BUSINESS_SKIP_DEBT ");
        sql.append(" WHERE STATUS = 1 ");
        sql.append(" AND STAFF_ID = :staffId ");
        sql.append(" AND TRUNC(SYSDATE) >= TRUNC(EFFECT_DATETIME) ");
        sql.append(" AND (EXPIRE_DATETIME IS NULL OR TRUNC(EXPIRE_DATETIME) >= TRUNC(SYSDATE)) ");
        sql.append(" ORDER BY CREATE_DATETIME DESC");

        Query query = em.createNativeQuery(sql.toString(), MapBusinessSkipDebtEntity.class);
        query.setParameter("staffId", staffId);
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MapBusinessSkipDebtEntity> searchForApi(String actionCode, Long telecomServiceId,
                                                        String effectDatetime, String shopCode,
                                                        String staffCode, String businessNo, String contractNo) {
        StringBuilder strQuery = new StringBuilder();
        java.util.concurrent.ConcurrentHashMap<String, Object> parameters = new java.util.concurrent.ConcurrentHashMap<>();
        strQuery.append(" SELECT * FROM MAP_BUSINESS_SKIP_DEBT msbd ");
        strQuery.append(" WHERE 1=1 AND msbd.STATUS = 1 \n ");

        if (DataUtil.notNullOrEmpty(actionCode)) {
            strQuery.append(" AND msbd.ACTION_CODE = TRIM(:actionCode)\n ");
            parameters.put("actionCode", DataUtil.trim(actionCode));
        }
        if (DataUtil.notNullObject(telecomServiceId)) {
            strQuery.append(" AND msbd.TELECOM_SERVICE_ID = :telecomServiceId\n ");
            parameters.put("telecomServiceId", telecomServiceId);
        }
        if (DataUtil.notNullOrEmpty(effectDatetime)) {
            strQuery.append(" AND TRUNC(msbd.EFFECT_DATETIME) <= TRUNC(TO_DATE(:effectDatetime,'dd/mm/yyyy'))\n ");
            parameters.put("effectDatetime", effectDatetime);

            strQuery.append(" AND (msbd.EXPIRE_DATETIME IS NULL OR TRUNC(msbd.EXPIRE_DATETIME) >= TRUNC(TO_DATE(:expireDatetime,'dd/mm/yyyy')))\n ");
            parameters.put("expireDatetime", effectDatetime);
        }
        if (DataUtil.notNullOrEmpty(shopCode)) {
            strQuery.append(" AND msbd.SHOP_ID IN (SELECT s.SHOP_ID FROM SHOP s WHERE s.STATUS = 1 AND TRIM(s.SHOP_CODE) = TRIM(:shopCode))\n ");
            parameters.put("shopCode", DataUtil.trim(shopCode));
        }
        if (DataUtil.notNullOrEmpty(staffCode)) {
            strQuery.append(" AND msbd.STAFF_ID IN (SELECT st.STAFF_ID FROM STAFF st WHERE st.STATUS = 1 AND TRIM(st.STAFF_CODE) = TRIM(:staffCode))\n ");
            parameters.put("staffCode", DataUtil.trim(staffCode));
        }
        if (DataUtil.notNullOrEmpty(businessNo)) {
            strQuery.append(" AND msbd.BUSINESS_NO = TRIM(:businessNo)");
            parameters.put("businessNo", DataUtil.trim(businessNo));
        }
        if (!DataUtil.isNullOrEmpty(contractNo)) {
            strQuery.append(" AND msbd.CONTRACT_NO = TRIM(:contractNo)");
            parameters.put("contractNo", DataUtil.trim(contractNo));
        }

        Query query = em.createNativeQuery(strQuery.toString(), MapBusinessSkipDebtEntity.class);
        parameters.forEach(query::setParameter);

        return query.getResultList();
    }

}