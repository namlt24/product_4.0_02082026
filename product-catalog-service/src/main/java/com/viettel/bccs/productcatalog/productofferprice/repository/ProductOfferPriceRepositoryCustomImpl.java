package com.viettel.bccs.productcatalog.productofferprice.repository;

import com.viettel.bccs.productcatalog.productofferprice.entity.ProductOfferPriceEntity;
import com.viettel.bccs.productcatalog.utils.Const;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductOfferPriceRepositoryCustomImpl implements ProductOfferPriceRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<ProductOfferPriceEntity> findForPccc(Long productPackageId, String productPackageCode,
            Long productOfferType, Long productOfferId, Long pricePolicy) {
        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT DISTINCT pp.* FROM %sproduct_offer_price pp
            INNER JOIN %sproduct_offering po ON pp.PRODUCT_OFFERING_ID = po.PRODUCT_OFFERING_ID
            INNER JOIN %sproduct_package pkg ON pkg.CODE = po.CODE AND pkg.STATUS = '1'
            WHERE pp.STATUS = '1'
              AND po.STATUS = '1'
              AND (pp.EXPIRE_DATETIME IS NULL OR pp.EXPIRE_DATETIME >= SYSDATE)
              AND (pp.EFFECT_DATETIME IS NULL OR pp.EFFECT_DATETIME <= SYSDATE)
            """.formatted(
                Const.DEFAULT_PRODUCT_SCHEMA,
                Const.DEFAULT_PRODUCT_SCHEMA,
                Const.DEFAULT_PRODUCT_SCHEMA));

        List<Object> params = new ArrayList<>();

        if (DataUtil.notNullOrEmpty(productPackageId)) {
            sql.append(" AND pkg.PRODUCT_PACKAGE_ID = ?").append(params.size() + 1);
            params.add(productPackageId);
        }
        if (DataUtil.notNullOrEmpty(productPackageCode)) {
            sql.append(" AND pkg.CODE = ?").append(params.size() + 1);
            params.add(productPackageCode);
        }
        if (DataUtil.notNullOrEmpty(productOfferType)) {
            sql.append(" AND po.PRODUCT_OFFER_TYPE_ID = ?").append(params.size() + 1);
            params.add(productOfferType);
        }
        if (DataUtil.notNullOrEmpty(productOfferId)) {
            sql.append(" AND pp.PRODUCT_OFFERING_ID = ?").append(params.size() + 1);
            params.add(productOfferId);
        }
        if (DataUtil.notNullOrEmpty(pricePolicy)) {
            sql.append(" AND pp.PRICE_POLICY_ID = ?").append(params.size() + 1);
            params.add(pricePolicy);
        }

        sql.append(" ORDER BY pp.PRIORITY ASC NULLS LAST, pp.EFFECT_DATETIME DESC");

        Query query = entityManager.createNativeQuery(sql.toString(), ProductOfferPriceEntity.class);
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }

        @SuppressWarnings("unchecked")
        List<ProductOfferPriceEntity> results = query.getResultList();
        return results;
    }

    @Override
    public List<ProductOfferPriceEntity> getPriceInServices(Long productPackageId, Long productOfferType, Long productOfferId, Long pricePolicy) {
        String sql = """
            SELECT a.*
            FROM BCCS_PRODUCT.PRODUCT_OFFER_PRICE a
            WHERE 1 = 1
              AND a.STATUS = '1'
              AND a.PRICE_POLICY_ID = :pricePolicy
              AND (a.EFFECT_DATETIME IS NULL OR a.EFFECT_DATETIME <= TRUNC(SYSDATE))
              AND (a.EXPIRE_DATETIME IS NULL OR a.EXPIRE_DATETIME >= TRUNC(SYSDATE))
              AND a.PRODUCT_OFFER_PRICE_ID IN (
                  SELECT b.PRODUCT_OFFER_PRICE_ID
                  FROM BCCS_PRODUCT.PACKAGE_OFFER b
                  WHERE 1 = 1
                    AND b.STATUS = '1'
                    AND b.PRODUCT_OFFERING_ID = :productOfferId
                    AND b.PROD_PACK_TYPE_ID IN (
                        SELECT c.PROD_PACK_TYPE_ID
                        FROM BCCS_PRODUCT.PROD_PACK_PRODUCT_OFFER_TYPE c
                        WHERE 1 = 1 AND c.STATUS = '1'
                              AND c.PRODUCT_PACKAGE_ID = :productPackageId
                              AND c.PRODUCT_OFFER_TYPE_ID = :productOfferType
                    )
              )
            """;

        Query query = entityManager.createNativeQuery(sql, ProductOfferPriceEntity.class);
        query.setParameter("productOfferId", productOfferId);
        query.setParameter("pricePolicy", pricePolicy);
        query.setParameter("productPackageId", productPackageId);
        query.setParameter("productOfferType", productOfferType);

        @SuppressWarnings("unchecked")
        List<ProductOfferPriceEntity> list = query.getResultList();
        return list;
    }
}