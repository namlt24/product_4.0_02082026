package com.viettel.bccs.policy.discountpromotion.repository;

import com.viettel.bccs.policy.discountpromotion.entity.DiscountPromotionEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DiscountPromotionRepositoryCustomImpl implements DiscountPromotionRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<DiscountPromotionEntity> getPromotionList(
            Long telecomServiceId,
            boolean checkStatus,
            boolean checkEffectDate,
            LocalDateTime endDate) {

        StringBuilder queryString = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();

        if (telecomServiceId != null) {
            queryString.append("SELECT dp FROM DiscountPromotionEntity dp WHERE dp.type = '1' AND dp.systemType = '2' "
                    + "AND ',' || dp.telecomServiceId || ',' LIKE :telService ");
            parameters.put("telService", "%," + telecomServiceId + ",%");
        } else {
            queryString.append("SELECT dp FROM DiscountPromotionEntity dp WHERE dp.status = '1' "
                    + "AND dp.type = '1' AND dp.systemType = '2' ");
        }

        if (checkEffectDate) {
            queryString.append("AND (dp.effectDatetime IS NULL OR dp.effectDatetime <= CURRENT_DATE) ");
        }

        if (checkStatus) {
            queryString.append("AND dp.status = '1' ");
        }

        if (endDate != null && checkEffectDate) {
            queryString.append("AND (dp.expireDatetime IS NULL OR dp.expireDatetime >= :endDate) ");
            parameters.put("endDate", endDate);
        } else if (checkEffectDate) {
            queryString.append("AND (dp.expireDatetime IS NULL OR dp.expireDatetime >= CURRENT_DATE) ");
        }

        queryString.append("ORDER BY FUNCTION('NLSSORT', dp.code, 'NLS_SORT=vietnamese')");

        Query query = em.createQuery(queryString.toString(), DiscountPromotionEntity.class);

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        return query.getResultList();
    }
}