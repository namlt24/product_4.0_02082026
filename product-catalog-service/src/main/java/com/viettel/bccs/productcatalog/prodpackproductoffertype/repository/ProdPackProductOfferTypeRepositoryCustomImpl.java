package com.viettel.bccs.productcatalog.prodpackproductoffertype.repository;

import com.viettel.bccs.productcatalog.prodpackproductoffertype.dto.response.ProdPackProductOfferTypeDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProdPackProductOfferTypeRepositoryCustomImpl implements ProdPackProductOfferTypeRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<ProdPackProductOfferTypeDTO> getByProductPackageIdAndStatus(Long productPackageId, String status) {
        // Dùng TO_CHAR cho các cột ID/Số lớn để ép Oracle JDBC không convert về Integer
        String sql = """
            SELECT TO_CHAR(PROD_PACK_TYPE_ID) AS PROD_PACK_TYPE_ID, 
                   TO_CHAR(PRODUCT_PACKAGE_ID) AS PRODUCT_PACKAGE_ID, 
                   TO_CHAR(PRODUCT_OFFER_TYPE_ID) AS PRODUCT_OFFER_TYPE_ID, 
                   STATUS,
                   TO_CHAR(UPDATE_STOCK) AS UPDATE_STOCK, 
                   TO_CHAR(CHECK_STAFF_STOCK) AS CHECK_STAFF_STOCK, 
                   TO_CHAR(CHECK_SHOP_STOCK) AS CHECK_SHOP_STOCK, 
                   CREATE_USER,
                   CREATE_DATETIME, 
                   UPDATE_USER, 
                   UPDATE_DATETIME, 
                   REQUIRE,
                   DESCRIPTION, 
                   TO_CHAR(LIMIT_GOODS) AS LIMIT_GOODS, 
                   TO_CHAR(DISTRIBUTE) AS DISTRIBUTE, 
                   TRANSFER_IM
            FROM BCCS_PRODUCT.PROD_PACK_PRODUCT_OFFER_TYPE
            WHERE PRODUCT_PACKAGE_ID = :productPackageId AND STATUS = :status
            ORDER BY PROD_PACK_TYPE_ID
            """;

        var query = entityManager.createNativeQuery(sql, Tuple.class);
        query.setParameter("productPackageId", productPackageId);
        query.setParameter("status", status);

        @SuppressWarnings("unchecked")
        List<Tuple> results = query.getResultList();
        if (results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream().map(this::mapRowToDTO).toList();
    }

    private ProdPackProductOfferTypeDTO mapRowToDTO(Tuple row) {
        return ProdPackProductOfferTypeDTO.builder()
                .prodPackTypeId(toLong(row.get("PROD_PACK_TYPE_ID")))
                .productPackageId(toLong(row.get("PRODUCT_PACKAGE_ID")))
                .productOfferTypeId(toLong(row.get("PRODUCT_OFFER_TYPE_ID")))
                .status(toString(row.get("STATUS")))
                .updateStock(toString(row.get("UPDATE_STOCK")))
                .checkStaffStock(toString(row.get("CHECK_STAFF_STOCK")))
                .checkShopStock(toString(row.get("CHECK_SHOP_STOCK")))
                .createUser(toString(row.get("CREATE_USER")))
                .createDatetime(toDate(row.get("CREATE_DATETIME")))
                .updateUser(toString(row.get("UPDATE_USER")))
                .updateDatetime(toDate(row.get("UPDATE_DATETIME")))
                .require(toString(row.get("REQUIRE")))
                .description(toString(row.get("DESCRIPTION")))
                .limitGoods(toLong(row.get("LIMIT_GOODS")))
                .distribute(toLong(row.get("DISTRIBUTE")))
                .transferIm(toString(row.get("TRANSFER_IM")))
                .build();
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        String str = obj.toString().trim();
        return str.isEmpty() ? null : Long.parseLong(str);
    }

    private String toString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    private java.util.Date toDate(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.util.Date date) return date;
        if (obj instanceof java.sql.Timestamp ts) return new java.util.Date(ts.getTime());
        return null;
    }

    @Override
    public List<ProdPackProductOfferTypeDTO> getListByProductPackageIdAndOfferTypeIds(Long productPackageId, List<Long> offerTypeIds) {
        String sql = """
            SELECT TO_CHAR(PROD_PACK_TYPE_ID) AS PROD_PACK_TYPE_ID,
                   TO_CHAR(PRODUCT_PACKAGE_ID) AS PRODUCT_PACKAGE_ID,
                   TO_CHAR(PRODUCT_OFFER_TYPE_ID) AS PRODUCT_OFFER_TYPE_ID,
                   STATUS,
                   TO_CHAR(UPDATE_STOCK) AS UPDATE_STOCK,
                   TO_CHAR(CHECK_STAFF_STOCK) AS CHECK_STAFF_STOCK,
                   TO_CHAR(CHECK_SHOP_STOCK) AS CHECK_SHOP_STOCK,
                   CREATE_USER,
                   CREATE_DATETIME,
                   UPDATE_USER,
                   UPDATE_DATETIME,
                   REQUIRE,
                   DESCRIPTION,
                   TO_CHAR(LIMIT_GOODS) AS LIMIT_GOODS,
                   TO_CHAR(DISTRIBUTE) AS DISTRIBUTE,
                   TRANSFER_IM
            FROM BCCS_PRODUCT.PROD_PACK_PRODUCT_OFFER_TYPE
            WHERE PRODUCT_PACKAGE_ID = :productPackageId AND STATUS = '1'
            """;
        var queryBuilder = new StringBuilder(sql);
        if (offerTypeIds != null && !offerTypeIds.isEmpty()) {
            queryBuilder.append(" AND PRODUCT_OFFER_TYPE_ID IN (:offerTypeIds)");
        }
        queryBuilder.append(" ORDER BY PROD_PACK_TYPE_ID");

        var query = entityManager.createNativeQuery(queryBuilder.toString(), Tuple.class);
        query.setParameter("productPackageId", productPackageId);
        if (offerTypeIds != null && !offerTypeIds.isEmpty()) {
            query.setParameter("offerTypeIds", offerTypeIds);
        }

        @SuppressWarnings("unchecked")
        List<Tuple> results = query.getResultList();
        if (results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream().map(this::mapRowToDTO).toList();
    }
}