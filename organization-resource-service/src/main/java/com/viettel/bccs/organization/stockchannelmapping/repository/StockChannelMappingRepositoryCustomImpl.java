package com.viettel.bccs.organization.stockchannelmapping.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.NativeQuery;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StockChannelMappingRepositoryCustomImpl implements StockChannelMappingRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<Long> findActiveFunctionalStockIds(Long telServiceId, Long channelTypeId, Long shopId,
                                                    Long staffId, Date today, String status) {

        String sql = """
                SELECT DISTINCT scm.STOCK_SHOP_ID
                FROM BCCS_PRODUCT.STOCK_CHANNEL_MAPPING scm
                WHERE scm.STATUS = :status
                  AND scm.EFFECT_DATE <= :today
                  AND (scm.EXPIRE_DATE IS NULL OR scm.EXPIRE_DATE >= :today)
                  AND scm.TELECOM_SERVICE_ID = :telServiceId
                  AND scm.CHANNEL_TYPE_ID = :channelTypeId
                  AND (
                      (scm.SHOP_ID = :shopId AND scm.STAFF_ID = :staffId)
                   OR (scm.SHOP_ID = :shopId AND scm.STAFF_ID = -1)
                   OR (scm.SHOP_ID = -1      AND scm.STAFF_ID = -1)
                  )
                """;

        NativeQuery<Tuple> query = (NativeQuery<Tuple>) entityManager.createNativeQuery(sql, Tuple.class);
        query.addScalar("STOCK_SHOP_ID", BigDecimal.class);
        query.setParameter("status", status);
        query.setParameter("telServiceId", telServiceId);
        query.setParameter("channelTypeId", channelTypeId);
        query.setParameter("shopId", shopId);
        query.setParameter("staffId", staffId);
        query.setParameter("today", today);

        return query.getResultList().stream()
                .map(t -> t.get("STOCK_SHOP_ID", BigDecimal.class).longValue())
                .toList();
    }

}
