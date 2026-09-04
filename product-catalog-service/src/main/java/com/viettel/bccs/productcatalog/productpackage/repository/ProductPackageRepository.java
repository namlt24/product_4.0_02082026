package com.viettel.bccs.productcatalog.productpackage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.productcatalog.productpackage.entity.ProductPackageEntity;

@Repository
public interface ProductPackageRepository extends JpaRepository<ProductPackageEntity, Long>,
    ProductPackageRepositoryCustom {    Optional<ProductPackageEntity> findByCode(String code);

    @Query(value = """
            SELECT pp.code
            FROM product_package pp
            WHERE pp.status = '1'
              AND (pp.expire_datetime IS NULL OR pp.expire_datetime >= TRUNC(SYSDATE))
              AND EXISTS (
                  SELECT 1
                  FROM product_package_fee ppf
                  WHERE ppf.product_package_id = pp.product_package_id
                    AND ppf.status = 1
                    AND (ppf.code IS NULL OR ppf.code = 'PHM')
                    AND (ppf.expire_datetime IS NULL OR ppf.expire_datetime >= TRUNC(SYSDATE))
              )
            """, nativeQuery = true)
    List<String> findPackageCodes();

    @Query(value = """
            SELECT pp.code
            FROM product_package pp
            WHERE pp.status = '1'
              AND (pp.expire_datetime IS NULL OR pp.expire_datetime >= TRUNC(SYSDATE))
              AND EXISTS (
                  SELECT 1
                  FROM product_package_fee ppf
                  WHERE ppf.product_package_id = pp.product_package_id
                    AND ppf.status = 1
                    AND (ppf.code IS NULL OR ppf.code = 'PHM')
                    AND (ppf.expire_datetime >= TRUNC(SYSDATE))
              )
              AND (
                  SELECT COUNT(1)
                  FROM prod_pack_product_offer_type ppp
                  WHERE ppp.product_package_id = pp.product_package_id
                    AND TO_CHAR(ppp.product_offer_type_id) IN (
                        SELECT value FROM option_set_value
                        WHERE option_set_id = (SELECT option_set_id FROM option_set
                            WHERE code = :excludeProdOfferType AND status = '1')
                    )
              ) = :packageNumber
            """, nativeQuery = true)
    List<String> findPackageCodesByProductOfferTypeCount(
            @Param("excludeProdOfferType") String excludeProdOfferType,
            @Param("packageNumber") Integer packageNumber);

    List<ProductPackageEntity> findByStatus(String status);

    List<ProductPackageEntity> findByType(String type);

    List<ProductPackageEntity> findByTelecomServiceId(Long telecomServiceId);
}