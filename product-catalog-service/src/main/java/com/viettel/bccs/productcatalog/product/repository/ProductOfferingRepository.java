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

    Optional<ProductOfferingEntity> findByCode(String code);

    /**
     * Migrate từ mono: ProductOfferingServiceImpl.getListStockModelBySaleServiceCode (nhánh
     * containNumber=true — xem ghi chú tại ProductOfferingRepositoryCustom). Dùng @Query
     * (nativeQuery=true) khai báo trên interface thay vì EntityManager.createNativeQuery thủ công
     * trong RepositoryCustomImpl: query này KHÔNG cần dựng điều kiện động (không cần
     * StringBuilder/Map params như các query trong ProductOfferingRepositoryCustomImpl) nên theo
     * đúng convention sẵn có của project cho query cố định (xem OptionSetValueRepository).
     * QUAN TRỌNG — CAST(... AS NUMBER(19)): với native query trả Object[] (không gắn entity/
     * result-class), Hibernate 7 tự suy luận kiểu Java cho cột NUMBER dựa trên precision do JDBC
     * driver báo cáo; cột NUMBER(10) như PROD_PACK_TYPE_ID đôi khi bị suy luận nhầm sang phạm vi
     * "int" (chỉ tới ~2.1 tỷ) dù giá trị thật vượt Integer.MAX_VALUE (đã xác nhận qua test thực tế:
     * PROD_PACK_TYPE_ID=3000022662 gây ORA-17026 "Numeric overflow", trong khi cùng câu SQL đọc
     * qua raw JDBC/getObject() hoàn toàn bình thường). CAST sang NUMBER(19) buộc Hibernate coi cột
     * là BigDecimal/Long, tránh nhầm sang int.
     */
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