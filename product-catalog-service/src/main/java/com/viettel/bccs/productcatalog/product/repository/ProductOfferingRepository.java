package com.viettel.bccs.productcatalog.product.repository;

import com.viettel.bccs.productcatalog.product.entity.ProductOfferingEntity;
import com.viettel.bccs.productcatalog.utils.Const;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOfferingRepository extends JpaRepository<ProductOfferingEntity, Long>, ProductOfferingRepositoryCustom {

    Optional<ProductOfferingEntity> findFirstByCode(String code);


    @Query(value = "select CAST(b.prod_pack_type_id AS NUMBER(19)) prod_pack_type_id, " +
            "       CAST(c.product_offer_type_id AS NUMBER(19)) product_offer_type_id, " +
            "       c.name as typeName, CAST(e.check_serial AS NUMBER(19)) check_serial, " +
            "       CAST(e.product_offering_id AS NUMBER(19)) product_offering_id, e.code, e.name, " +
            "       CAST(e.telecom_service_id AS NUMBER(19)) telecom_service_id " +
            "from BCCS_PRODUCT.product_package a, BCCS_PRODUCT.prod_pack_product_offer_type b, " +
            "     BCCS_PRODUCT.product_offer_type c, BCCS_PRODUCT.package_offer d, " +
            "     BCCS_PRODUCT.product_offering e, BCCS_PRODUCT.product_offer_price f " +
            "where a.product_package_id = b.product_package_id " +
            "  and b.product_offer_type_id = c.product_offer_type_id " +
            "  and b.prod_pack_type_id = d.prod_pack_type_id " +
            "  and d.product_offering_id = e.product_offering_id " +
            "  and e.product_offering_id = f.product_offering_id " +
            "  and d.product_offer_price_id = f.product_offer_price_id " +
            "  and a.status = '1' and b.status = '1' and c.status = '1' and d.status = '1' " +
            "  and e.status = '1' and f.status = '1' " +
            "  and f.effect_datetime <= sysdate " +
            "  and (f.expire_datetime >= sysdate or f.expire_datetime is null) " +
            "  and LOWER(a.code) = LOWER(:saleServiceCode) " +
            "  and a.TYPE = '" + Const.PRODUCT_PACKAGE_TYPE.SALE_SERVICE + "' " +
            "order by c.name, e.name",
            nativeQuery = true)
    List<Object[]> getListStockModelBySaleServiceCode(@Param("saleServiceCode") String saleServiceCode);
}