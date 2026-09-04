package com.viettel.bccs.productcatalog.productspeccharvalue.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.productcatalog.productspeccharvalue.entity.ProductSpecCharValueEntity;
import com.viettel.bccs.productcatalog.utils.Const;

@Repository
public interface ProductSpecCharValueRepository extends JpaRepository<ProductSpecCharValueEntity, Long> {

    List<ProductSpecCharValueEntity> findByProductSpecCharId(Long productSpecCharId);

    Optional<ProductSpecCharValueEntity> findByProductSpecCharIdAndIsDefault(Long productSpecCharId, Long isDefault);

    List<ProductSpecCharValueEntity> findByProductSpecCharIdAndStatus(Long productSpecCharId, String status);

    boolean existsByProductSpecCharId(Long productSpecCharId);

    @Query(value =
            "SELECT d.* " +
                    "FROM " + Const.DEFAULT_PRODUCT_SCHEMA + "product_offer_char_use a " +
                    "JOIN " + Const.DEFAULT_PRODUCT_SCHEMA
                            + "product_spec_char c ON a.PRODUCT_SPEC_CHAR_ID = c.PRODUCT_SPEC_CHAR_ID"
                            + " AND c.STATUS = '1' " +
                    "JOIN " + Const.DEFAULT_PRODUCT_SCHEMA
                            + "product_spec_char_value d ON a.PRODUCT_SPEC_CHAR_VALUE_ID = d.PRODUCT_SPEC_CHAR_VALUE_ID"
                            + " AND d.STATUS = '1' " +
                    "WHERE a.STATUS = '1' " +
                    "  AND a.PRODUCT_OFFERING_ID = :productOfferingId " +
                    "  AND c.CODE = :characterCode " +
                    "  AND (a.EFFECT_DATETIME IS NULL OR a.EFFECT_DATETIME <= TRUNC(SYSDATE)) " +
                    "  AND (a.EXPIRE_DATETIME IS NULL OR a.EXPIRE_DATETIME >= TRUNC(SYSDATE))",
            nativeQuery = true)
    List<ProductSpecCharValueEntity> getByProductSpecCharCodeAndProductOfferingId(
            @Param("characterCode") String characterCode,
            @Param("productOfferingId") Long productOfferingId);

}