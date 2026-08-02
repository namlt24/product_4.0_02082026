package com.viettel.bccs.productcatalog.prodpackshop.repository;

import com.viettel.bccs.productcatalog.utils.Const;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.NativeQuery;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ProdPackShopRepositoryCustomImpl implements ProdPackShopRepositoryCustom {

    private static final int BATCH_SIZE = 100;
    private final EntityManager entityManager;

    @Override
    public Map<Long, List<Long>> findShopIdsByProdPackTypeIds(List<Long> prodPackTypeIds) {
        if (prodPackTypeIds == null || prodPackTypeIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<Long>> result = new HashMap<>();
        for (int i = 0; i < prodPackTypeIds.size(); i += BATCH_SIZE) {
            List<Long> batch = prodPackTypeIds.subList(i, Math.min(i + BATCH_SIZE, prodPackTypeIds.size()));
            executeQuery(batch, result);
        }

        return result;
    }

    private void executeQuery(List<Long> prodPackTypeIds, Map<Long, List<Long>> result) {
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < prodPackTypeIds.size(); i++) {
            inClause.append(i == 0 ? ":id" + i : ", :id" + i);
        }

        String sql = """
            SELECT a.PROD_PACK_TYPE_ID, a.SHOP_ID
            FROM %sPROD_PACK_SHOP a
            WHERE a.STATUS = '1'
              AND a.PROD_PACK_TYPE_ID IN (%s)
            """.formatted(Const.DEFAULT_PRODUCT_SCHEMA, inClause);

        Query query = entityManager.createNativeQuery(sql);
        ((NativeQuery<?>) query)
                .addScalar("PROD_PACK_TYPE_ID", BigDecimal.class)
                .addScalar("SHOP_ID", BigDecimal.class);
        for (int i = 0; i < prodPackTypeIds.size(); i++) {
            query.setParameter("id" + i, prodPackTypeIds.get(i));
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        for (Object[] row : rows) {
            BigDecimal bdProdPackTypeId = (BigDecimal) row[0];
            BigDecimal bdShopId = (BigDecimal) row[1];
            if (bdProdPackTypeId == null || bdShopId == null) continue;
            result.computeIfAbsent(bdProdPackTypeId.longValue(), k -> new ArrayList<>()).add(bdShopId.longValue());
        }
    }
}