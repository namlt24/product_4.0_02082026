package com.viettel.bccs.productcatalog.productofferprice.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.viettel.bccs.productcatalog.productofferprice.entity.ProductOfferPriceEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductOfferPriceRepositoryCustomImpl implements ProductOfferPriceRepositoryCustom {
    private static final Long EQUIPMENT_PRICE_TYPE_ID = 289L;

    private final EntityManager entityManager;

    @Override
    public List<ProductOfferPriceEntity> getPriceInServices(Long productPackageId, Long productOfferType,
        Long productOfferId, Long pricePolicy) {
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

    @Override
    @SuppressWarnings("unchecked")
    public List<ProductOfferPriceEntity> getPriceEquipment(Long productPackageId, Long productOfferType,
        Long productOfferId) {
        String sql = """
            SELECT a.*
            FROM BCCS_PRODUCT.PRODUCT_OFFER_PRICE a
            WHERE 1 = 1
              AND a.STATUS = '1'
              AND a.PRICE_TYPE_ID = :equipmentPriceTypeId
              AND a.PRODUCT_OFFERING_ID = :productOfferId
              AND a.PRODUCT_OFFER_PRICE_ID IN (
                  SELECT b.PRODUCT_OFFER_PRICE_ID
                  FROM BCCS_PRODUCT.PACKAGE_OFFER b
                  WHERE b.STATUS = '1'
                    AND b.PRODUCT_OFFERING_ID = :productOfferId
                    AND b.PROD_PACK_TYPE_ID IN (
                        SELECT c.PROD_PACK_TYPE_ID
                        FROM BCCS_PRODUCT.PROD_PACK_PRODUCT_OFFER_TYPE c
                        WHERE c.STATUS = '1'
                          AND c.PRODUCT_PACKAGE_ID = :productPackageId
                          AND c.PRODUCT_OFFER_TYPE_ID = :productOfferType
                    )
              )
            """;

        Query query = entityManager.createNativeQuery(sql, ProductOfferPriceEntity.class);
        query.setParameter("productPackageId", productPackageId);
        query.setParameter("productOfferType", productOfferType);
        query.setParameter("productOfferId", productOfferId);
        query.setParameter("equipmentPriceTypeId", EQUIPMENT_PRICE_TYPE_ID);

        return query.getResultList();
    }

    /**
     * Nguồn thật (hệ mono cũ) viết bằng JPQL (em.createQuery, entity "ProductOfferPrice", dùng
     * hàm Oracle sysdate/trunc trực tiếp trong JPQL). Codebase này KHÔNG có tiền lệ dùng JPQL ở bất
     * kỳ repository custom nào (100% dùng native SQL) và Hibernate không nhận sysdate/trunc như
     * hàm JPQL chuẩn — giữ nguyên dạng JPQL như bản gốc sẽ lỗi lúc parse. Viết lại thành native SQL
     * tương đương 1:1 về nghiệp vụ (cùng bảng, cùng điều kiện lọc, cùng thứ tự sắp xếp), theo đúng
     * convention native SQL đã dùng cho 2 hàm phía trên trong cùng file.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<ProductOfferPriceEntity> getPriceByTypePolicy(Long productOfferId, Long priceTypeId, Long pricePolicy) {
        String sql = """
            SELECT a.*
            FROM BCCS_PRODUCT.PRODUCT_OFFER_PRICE a
            WHERE 1 = 1
              AND a.PRODUCT_OFFERING_ID = :productOfferingId
              AND a.PRICE_TYPE_ID = :priceTypeId
              AND a.PRICE_POLICY_ID = :pricePolicy
              AND a.STATUS = '1'
              AND (a.EFFECT_DATETIME IS NULL OR a.EFFECT_DATETIME <= TRUNC(SYSDATE))
              AND (a.EXPIRE_DATETIME IS NULL OR a.EXPIRE_DATETIME >= TRUNC(SYSDATE))
            ORDER BY a.PRICE
            """;

        Query query = entityManager.createNativeQuery(sql, ProductOfferPriceEntity.class);
        query.setParameter("productOfferingId", productOfferId);
        query.setParameter("priceTypeId", priceTypeId);
        query.setParameter("pricePolicy", pricePolicy);

        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProductOfferPriceEntity> getPledgePriceInfoByOfferId(Long productOfferingId) {
        String sql = """
            SELECT pop.*
            FROM BCCS_PRODUCT.PRODUCT_OFFER_PRICE pop
            WHERE pop.STATUS = '1'
              AND pop.PRICE_TYPE_ID = 2
              AND (pop.EFFECT_DATETIME IS NULL OR pop.EFFECT_DATETIME < SYSDATE)
              AND (pop.EXPIRE_DATETIME IS NULL OR pop.EXPIRE_DATETIME > SYSDATE)
              AND pop.PRODUCT_OFFERING_ID = :productOfferingId
            """;

        Query query = entityManager.createNativeQuery(sql, ProductOfferPriceEntity.class);
        query.setParameter("productOfferingId", productOfferingId);
        return query.getResultList();
    }

}