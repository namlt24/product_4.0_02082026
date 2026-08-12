package com.viettel.bccs.productcatalog.productoffertype.repository;

import com.viettel.bccs.productcatalog.productoffertype.entity.ProductOfferTypeEntity;
import com.viettel.bccs.productcatalog.utils.Const;
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

    @Override
    public List<ProductOfferTypeEntity> findBySaleServiceCodeWithProductOffering(String saleServiceCode) {
        String sql = """
            SELECT DISTINCT a.* FROM PRODUCT_OFFER_TYPE a, PROD_PACK_PRODUCT_OFFER_TYPE b, PRODUCT_PACKAGE c
            WHERE 1 = 1
              AND a.PRODUCT_OFFER_TYPE_ID = b.PRODUCT_OFFER_TYPE_ID
              AND b.PRODUCT_PACKAGE_ID = c.PRODUCT_PACKAGE_ID
              AND a.STATUS = '1'
              AND b.STATUS = '1'
              AND c.STATUS = '1'
              AND c.CODE = :saleServiceCode
              AND c.TYPE = '%s'
            """.formatted(Const.PRODUCT_PACKAGE_TYPE.SALE_SERVICE);

        Query query = entityManager.createNativeQuery(sql, ProductOfferTypeEntity.class);
        query.setParameter("saleServiceCode", saleServiceCode);

        @SuppressWarnings("unchecked")
        List<ProductOfferTypeEntity> list = query.getResultList();
        return list;
    }
}