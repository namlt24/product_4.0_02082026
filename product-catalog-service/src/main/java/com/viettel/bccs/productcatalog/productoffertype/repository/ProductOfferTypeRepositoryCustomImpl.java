package com.viettel.bccs.productcatalog.productoffertype.repository;

import com.viettel.bccs.productcatalog.productoffertype.entity.ProductOfferTypeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductOfferTypeRepositoryCustomImpl implements ProductOfferTypeRepositoryCustom {

    private static final int BATCH_SIZE = 100;
    private final EntityManager entityManager;

    @Override
    public List<ProductOfferTypeEntity> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<ProductOfferTypeEntity> result = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += BATCH_SIZE) {
            List<Long> batch = ids.subList(i, Math.min(i + BATCH_SIZE, ids.size()));
            result.addAll(executeQuery(batch));
        }
        return result;
    }

    private List<ProductOfferTypeEntity> executeQuery(List<Long> ids) {
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            inClause.append(i == 0 ? ":id" + i : ", :id" + i);
        }

        String sql = """
            SELECT a.PRODUCT_OFFER_TYPE_ID, a.PARENT_ID, a.NAME, a.DESCRIPTION,
                   a.STATUS, a.CREATE_USER, a.CREATE_DATETIME, a.UPDATE_USER, a.UPDATE_DATETIME
            FROM PRODUCT_OFFER_TYPE a
            WHERE a.PRODUCT_OFFER_TYPE_ID IN (%s)
            """.formatted(inClause);

        Query query = entityManager.createNativeQuery(sql, ProductOfferTypeEntity.class);
        for (int i = 0; i < ids.size(); i++) {
            query.setParameter("id" + i, ids.get(i));
        }

        @SuppressWarnings("unchecked")
        List<ProductOfferTypeEntity> list = query.getResultList();
        return list;
    }
}