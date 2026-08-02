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