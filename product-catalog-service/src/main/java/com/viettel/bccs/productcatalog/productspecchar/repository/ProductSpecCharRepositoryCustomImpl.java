package com.viettel.bccs.productcatalog.productspecchar.repository;

import com.viettel.bccs.productcatalog.utils.Const;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Migrate từ mono: ExternalServiceForMbccsImpl.findProductOfferingByListCodeListSpecCode
 * (ProductSpecCharRepoImpl gốc). JOIN PRODUCT_SPEC_CHAR (a) - PRODUCT_OFFER_CHAR_USE (b) -
 * PRODUCT_OFFERING (c): tìm đặc tính đang active, gán cho sản phẩm đang active cùng loại
 * (productOfferTypeId), khớp ít nhất 1 mã trong lstSpecCode, tuỳ chọn lọc thêm theo lstProductCode
 * (chỉ thêm điều kiện khi danh sách không rỗng — đúng logic gốc).
 */
@Repository
@RequiredArgsConstructor
public class ProductSpecCharRepositoryCustomImpl implements ProductSpecCharRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<Object[]> findByListSpecCodeAndListProductCode(List<String> lstSpecCode, List<String> lstProductCode, Long productOfferTypeId) {
        if (lstSpecCode == null || lstSpecCode.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder specInClause = new StringBuilder();
        for (int i = 0; i < lstSpecCode.size(); i++) {
            specInClause.append(i == 0 ? ":specCode0" : ", :specCode" + i);
        }

        boolean filterByProductCode = lstProductCode != null && !lstProductCode.isEmpty();
        StringBuilder productInClause = new StringBuilder();
        if (filterByProductCode) {
            for (int i = 0; i < lstProductCode.size(); i++) {
                productInClause.append(i == 0 ? ":productCode0" : ", :productCode" + i);
            }
        }

        // CAST(... AS NUMBER(19)): tranh Hibernate suy luan nham cot NUMBER(10) sang "int"
        // (ORA-17026 khi id thuc te vuot Integer.MAX_VALUE) - dung pattern da xac nhan o
        // ProductOfferCharUseRepositoryCustomImpl/ProductOfferingRepository.
        String sql = "SELECT" +
                " CAST(a.PRODUCT_SPEC_CHAR_ID AS NUMBER(19)) AS PRODUCT_SPEC_CHAR_ID," +
                " a.NAME, a.DESCRIPTION, a.VALUE_TYPE, a.CHAR_TYPE," +
                " CAST(a.MIN_CARDINALITY AS NUMBER(19)) AS MIN_CARDINALITY," +
                " CAST(a.MAX_CARDINALITY AS NUMBER(19)) AS MAX_CARDINALITY," +
                " a.STATUS, a.CODE, a.PRODUCT_SPEC_CHAR_TYPE_ID," +
                " CAST(a.VALUE_SET_TYPE AS NUMBER(19)) AS VALUE_SET_TYPE," +
                " a.RESPONSE_CLASS, a.SQL_QUERY, a.DISPLAY_OBJECT, a.VALUE_OBJECT," +
                " a.SOLR_QUERY, a.SOLR_CORE, a.SOLR_SCHEMA, a.DATA_TYPE, a.WS_WSDL," +
                " a.TEMPLATE_REQUEST, a.VALIDATE_PATTERN, a.EXT_DATA, a.NOTE," +
                " CAST(c.PRODUCT_OFFERING_ID AS NUMBER(19)) AS PRODUCT_OFFERING_ID," +
                " c.CODE AS PRODUCT_OFFERING_CODE, c.NAME AS PRODUCT_OFFERING_NAME" +
                " FROM " + Const.DEFAULT_PRODUCT_SCHEMA + "product_spec_char a, " +
                Const.DEFAULT_PRODUCT_SCHEMA + "product_offer_char_use b, " +
                Const.DEFAULT_PRODUCT_SCHEMA + "product_offering c" +
                " WHERE 1 = 1" +
                " AND a.PRODUCT_SPEC_CHAR_ID = b.PRODUCT_SPEC_CHAR_ID" +
                " AND b.product_offering_id = c.product_offering_id" +
                " AND a.status = :status" +
                " AND b.status = :status" +
                " AND c.status = :status" +
                " AND c.product_offer_type_id = :productOfferType" +
                " AND a.code IN (" + specInClause + ")" +
                (filterByProductCode ? " AND c.code IN (" + productInClause + ")" : "") +
                " ORDER BY c.code";

        Query query = entityManager.createNativeQuery(sql);
        for (int i = 0; i < lstSpecCode.size(); i++) {
            query.setParameter("specCode" + i, lstSpecCode.get(i));
        }
        if (filterByProductCode) {
            for (int i = 0; i < lstProductCode.size(); i++) {
                query.setParameter("productCode" + i, lstProductCode.get(i));
            }
        }
        query.setParameter("status", Const.STATUS.ACTIVE);
        query.setParameter("productOfferType", productOfferTypeId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return results;
    }
}
