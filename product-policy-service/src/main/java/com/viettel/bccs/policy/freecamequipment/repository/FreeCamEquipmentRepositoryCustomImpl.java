package com.viettel.bccs.policy.freecamequipment.repository;

import com.viettel.bccs.policy.freecamequipment.entity.FreeCamEquipmentEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FreeCamEquipmentRepositoryCustomImpl implements FreeCamEquipmentRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<FreeCamEquipmentEntity> checkReasonFreeCam(Long productPackageId) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT * FROM FREE_CAM_EQUIPMENT ");
        sql.append(" WHERE STATUS = 1 ");
        sql.append(" AND EFFECT_DATETIME <= TRUNC(SYSDATE) ");
        sql.append(" AND (EXPIRE_DATETIME >= TRUNC(SYSDATE) OR EXPIRE_DATETIME IS NULL) ");
        sql.append(" AND REASON_ID IN ");
        sql.append("   (SELECT c.REASON_ID FROM MAPPING a, REASON c ");
        sql.append("    WHERE a.REASON_ID = c.REASON_ID AND a.STATUS = 1 AND c.STATUS = 1 ");
        sql.append("    AND (a.END_EFFECT_DATE IS NULL OR a.END_EFFECT_DATE >= TRUNC(SYSDATE)) ");
        sql.append("    AND a.SALE_SERVICE_ID = :productPackageId)");

        Query query = em.createNativeQuery(sql.toString(), FreeCamEquipmentEntity.class);
        query.setParameter("productPackageId", productPackageId);
        return query.getResultList();
    }
}
