package com.viettel.bccs.productcatalog.product.repository;

import com.viettel.bccs.productcatalog.common.dto.FilterRequest;
import com.viettel.bccs.productcatalog.product.entity.ProductOfferingEntity;
import com.viettel.bccs.productcatalog.utils.Const;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// entityManager.createNativeQuery(sql, Class) tra ve Query tho theo dung dac ta JPA (khong co
// TypedQuery cho native query) - query.getResultList() vi vay tra ve List tho, khong the tranh
// unchecked conversion khi return ve List<ProductOfferingEntity> o hau het method cua class nay.
@SuppressWarnings("unchecked")
@Repository
@RequiredArgsConstructor
public class ProductOfferingRepositoryCustomImpl implements ProductOfferingRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<ProductOfferingEntity> findByTelecomSubTypeOfferTypeCheckProductStatus(Long telecomServiceId, String subType, Long offerTypeId, boolean getActiveProduct) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT a.* FROM ").append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_offering a WHERE 1=1");

        if (getActiveProduct) {
            sql.append(" AND a.status = '1'");
        }
        appendCondition(sql, "a.telecom_service_id = :telecomServiceId", telecomServiceId != null && telecomServiceId > 0);
        appendCondition(sql, "a.sub_type = :subType", subType != null && !subType.isEmpty());
        appendCondition(sql, "a.product_offer_type_id = :offerTypeId", offerTypeId != null && offerTypeId > 0);

        sql.append(" ORDER BY a.code");

        Query query = entityManager.createNativeQuery(sql.toString(), ProductOfferingEntity.class);
        if (telecomServiceId != null && telecomServiceId > 0) {
            query.setParameter("telecomServiceId", telecomServiceId);
        }
        if (subType != null && !subType.isEmpty()) {
            query.setParameter("subType", subType);
        }
        if (offerTypeId != null && offerTypeId > 0) {
            query.setParameter("offerTypeId", offerTypeId);
        }
        return query.getResultList();
    }

    private void appendCondition(StringBuilder sql, String condition, boolean check) {
        if (check) {
            sql.append(" AND ").append(condition);
        }
    }

    @Override
public List<ProductOfferingEntity> findByCodeOrId(Long proOfferId, String prodOfferCode, String status) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT a.* FROM ").append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_offering a WHERE 1=1");

        if (status != null && !status.isEmpty()) {
            sql.append(" AND a.status = :status");
        }
        if (proOfferId != null && proOfferId > 0){
            sql.append(" AND a.PRODUCT_OFFERING_ID = :proOfferId");
        }
        if (prodOfferCode != null && !prodOfferCode.isEmpty()) {
            sql.append(" AND a.CODE = :prodOfferCode");
        }

        sql.append(" ORDER BY a.code");

        Query query = entityManager.createNativeQuery(sql.toString(), ProductOfferingEntity.class);
        if (status != null && !status.isEmpty()) {
            query.setParameter("status", status);
        }
        if (proOfferId != null && proOfferId > 0) {
            query.setParameter("proOfferId", proOfferId);
        }
        if (prodOfferCode != null && !prodOfferCode.isEmpty()) {
            query.setParameter("prodOfferCode", prodOfferCode);
        }
        return query.getResultList();
    }

    @Override
    public List<ProductOfferingEntity> getListOfferAlterStatus(Long offerId, String changeChannel, boolean checkStatus) {
        String strQuery = " SELECT * FROM " + Const.DEFAULT_PRODUCT_SCHEMA + "product_offering WHERE status='1' AND product_offering_id IN ( " +
                " SELECT a.relation_offer_id FROM " +
                Const.DEFAULT_PRODUCT_SCHEMA + "product_offer_relation a, " +
                Const.DEFAULT_PRODUCT_SCHEMA + "product_offer_relation_detail b, " +
                Const.DEFAULT_PRODUCT_SCHEMA + "product_spec_char c, " +
                Const.DEFAULT_PRODUCT_SCHEMA + "product_spec_char_value d " +
                " WHERE 1=1 AND a.main_offer_id = :offerId AND a.relation_type_id = 3 " +
                " AND a.product_offer_relation_id = b.product_offer_relation_id " +
                " AND b.product_spec_char_id = c.product_spec_char_id " +
                " AND b.product_spec_char_value_id = d.product_spec_char_value_id " +
                " AND c.code = 'CHANGE_METHOD' " +
                " AND d.value = :changeChannel";

        if (checkStatus) {
            strQuery += " AND a.status = '1' AND b.status = '1' AND c.status = '1' AND d.status = '1' ) ";
        } else {
            strQuery += " AND b.status = '1' AND c.status = '1' AND d.status = '1' ) ";
        }

        Query query = entityManager.createNativeQuery(strQuery, ProductOfferingEntity.class);
        query.setParameter("offerId", offerId);
        query.setParameter("changeChannel", changeChannel);

        return query.getResultList();
    }

    @Override
    public List<ProductOfferingEntity> findByPayTypeWithSpec(String telecomServiceId, String payType, String productOfferTypeId, List<FilterRequest> listProductSpec) {
        StringBuilder strQuery = new StringBuilder();
        strQuery.append(" SELECT a.* FROM ").append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_offering a WHERE a.status = '1' ");

        Map<String, Object> params = new HashMap<>();

        if (DataUtil.notNullOrEmpty(telecomServiceId)) {
            strQuery.append(" AND a.telecom_service_id = :telecomServiceId ");
            params.put("telecomServiceId", Long.valueOf(telecomServiceId.trim()));
        }
        if (DataUtil.notNullOrEmpty(payType)) {
            strQuery.append(" AND a.sub_type = :payType ");
            params.put("payType", payType.trim());
        }
        if (DataUtil.notNullOrEmpty(productOfferTypeId)) {
            strQuery.append(" AND a.product_offer_type_id = :productOfferTypeId ");
            params.put("productOfferTypeId", Long.valueOf(productOfferTypeId.trim()));
        }

        if (DataUtil.notNullOrEmpty(listProductSpec)) {
            int i = 0;
            for (FilterRequest filterRequest : listProductSpec) {
                String clause = filterRequest.isNotEqual() ? " AND NOT EXISTS " : " AND EXISTS ";
                strQuery.append(clause)
                        .append(" (SELECT 1 FROM ")
                        .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_offer_char_use b, ")
                        .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_spec_char c, ")
                        .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_spec_char_value d ")
                        .append(" WHERE b.product_spec_char_id = c.product_spec_char_id ")
                        .append(" AND b.product_offering_id = a.product_offering_id ")
                        .append(" AND b.status = '1' ")
                        .append(" AND c.status = '1' ")
                        .append(" AND d.status = '1' ")
                        .append(" AND d.product_spec_char_value_id = b.product_spec_char_value_id ")
                        .append(" AND c.code = :p_").append(filterRequest.getProperty()).append(i);

                params.put("p_" + filterRequest.getProperty() + i, filterRequest.getProperty());

                if (DataUtil.notNullOrEmpty(filterRequest.getValueText())) {
                    String valueStr = DataUtil.safeToString(filterRequest.getValueText());
                    if ("LONG".equalsIgnoreCase(DataUtil.safeToString(filterRequest.getValueType()))) {
                        strQuery.append(" AND to_number(d.value) ").append(operatorToOracle(filterRequest)).append(" to_number(:p_value_").append(filterRequest.getProperty()).append(i).append("))");
                    } else {
                        strQuery.append(" AND d.value ").append(operatorToOracle(filterRequest)).append(" :p_value_").append(filterRequest.getProperty()).append(i).append(")");
                    }
                    params.put("p_value_" + filterRequest.getProperty() + i, valueStr);
                } else {
                    strQuery.append(") ");
                }
                i++;
            }
        }

        Query query = entityManager.createNativeQuery(strQuery.toString(), ProductOfferingEntity.class);
        params.forEach(query::setParameter);
        return query.getResultList();
    }

    @Override
    public List<ProductOfferingEntity> findByCodesAndProductOfferType(List<String> codes, Long productOfferTypeId) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }

        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < codes.size(); i++) {
            inClause.append(i == 0 ? ":code0" : ", :code" + i);
        }

        String sql = "SELECT a.* FROM " + Const.DEFAULT_PRODUCT_SCHEMA + "product_offering a" +
                " WHERE a.code IN (" + inClause + ")" +
                " AND a.product_offer_type_id = :productOfferTypeId" +
                " AND a.status = '1'" +
                " ORDER BY a.code";

        Query query = entityManager.createNativeQuery(sql, ProductOfferingEntity.class);
        for (int i = 0; i < codes.size(); i++) {
            query.setParameter("code" + i, codes.get(i));
        }
        query.setParameter("productOfferTypeId", productOfferTypeId);
        return query.getResultList();
    }

    @Override
    public boolean checkAttProductOrVasByCode(String productCode, Long productType, String attributeCode) {
        return hasProductAttInternal(productCode, null, productType, attributeCode);
    }

    @Override
    public boolean hasProductAtt(Long offerId, String attributeCode) {
        return hasProductAttInternal(null, offerId, null, attributeCode);
    }


    private boolean hasProductAttInternal(String productCode, Long offerId, Long productType, String attributeCode) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(1) FROM ")
                .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_offering a, ")
                .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_offer_char_use b, ")
                .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_spec_char c, ")
                .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_spec_char_value d")
                .append(" WHERE a.product_offering_id = b.product_offering_id")
                .append(" AND b.product_spec_char_id = c.product_spec_char_id")
                .append(" AND d.product_spec_char_value_id = b.product_spec_char_value_id")
                .append(" AND c.code = :attributeCode")
                .append(" AND a.status = '1' AND b.status = '1' AND c.status = '1' AND d.status = '1'");
        if (productCode != null) {
            sql.append(" AND a.code = :productCode");
        }
        if (offerId != null) {
            sql.append(" AND a.product_offering_id = :offerId");
        }
        if (productType != null) {
            sql.append(" AND a.product_offer_type_id = :productType");
        }

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("attributeCode", attributeCode);
        if (productCode != null) {
            query.setParameter("productCode", productCode);
        }
        if (offerId != null) {
            query.setParameter("offerId", offerId);
        }
        if (productType != null) {
            query.setParameter("productType", productType);
        }

        Number count = (Number) query.getSingleResult();
        return count.longValue() > 0;
    }


    @Override
    public List<ProductOfferingEntity> findBySpecCharCodes(List<String> specCodes, Long productOfferTypeId) {
        StringBuilder sql = new StringBuilder("SELECT a.* FROM ")
                .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_offering a")
                .append(" WHERE a.status = '1'");

        Map<String, Object> params = new HashMap<>();

        if (productOfferTypeId != null) {
            sql.append(" AND a.product_offer_type_id = :productOfferTypeId");
            params.put("productOfferTypeId", productOfferTypeId);
        }

        for (int i = 0; i < specCodes.size(); i++) {
            sql.append(" AND EXISTS (SELECT 1 FROM ")
                    .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_offer_char_use b, ")
                    .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_spec_char c, ")
                    .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_spec_char_value d")
                    .append(" WHERE b.product_offering_id = a.product_offering_id")
                    .append(" AND b.product_spec_char_id = c.product_spec_char_id")
                    .append(" AND d.product_spec_char_value_id = b.product_spec_char_value_id")
                    .append(" AND b.status = '1' AND c.status = '1' AND d.status = '1'")
                    .append(" AND c.code = :specCode").append(i).append(")");
            params.put("specCode" + i, specCodes.get(i));
        }
        sql.append(" ORDER BY a.code");

        Query query = entityManager.createNativeQuery(sql.toString(), ProductOfferingEntity.class);
        params.forEach(query::setParameter);
        return query.getResultList();
    }

    @Override
    public List<ProductOfferingEntity> getListVas(Long offerId, Integer type) {
        StringBuilder strQuery = new StringBuilder();
        strQuery.append(" SELECT * FROM ").append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_offering ")
                .append(" WHERE status = '1' AND product_offering_id IN ( ");
        strQuery.append(" SELECT a.relation_offer_id FROM ")
                .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_offer_relation a, ")
                .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_offer_relation_detail b, ")
                .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_spec_char c, ")
                .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_spec_char_value d ");
        strQuery.append(" WHERE 1=1 AND a.main_offer_id = :offerId AND a.relation_type_id = ")
                .append(Const.RELATION_TYPE.VAS).append(" ");
        strQuery.append(" AND a.product_offer_relation_id = b.product_offer_relation_id ");
        strQuery.append(" AND b.product_spec_char_id = c.product_spec_char_id ");
        strQuery.append(" AND b.product_spec_char_value_id = d.product_spec_char_value_id ");

        strQuery.append(" AND a.status = '1' AND b.status = '1' AND c.status = '1' AND d.status = '1' ");

        // type=1: chi lay VAS ma quan he (product_offer_relation) co thuoc tinh quan he
        // (product_offer_relation_detail) code=IS_CONNECTED gia tri 1, VA khong co thuoc tinh
        // code=VAS_DATA. Thuoc tinh nay gan tren BAN GHI QUAN HE, khong phai tren san pham VAS.
        if (Integer.valueOf(1).equals(type)) {
            strQuery.append(" AND EXISTS (SELECT 1 FROM ")
                    .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_offer_relation_detail rb, ")
                    .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_spec_char rc, ")
                    .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_spec_char_value rd ")
                    .append(" WHERE rb.product_offer_relation_id = a.product_offer_relation_id ")
                    .append(" AND rb.product_spec_char_id = rc.product_spec_char_id ")
                    .append(" AND rb.product_spec_char_value_id = rd.product_spec_char_value_id ")
                    .append(" AND rb.status = '1' AND rc.status = '1' AND rd.status = '1' ")
                    .append(" AND rc.code = 'IS_CONNECTED' AND rd.value = '1') ");
            strQuery.append(" AND NOT EXISTS (SELECT 1 FROM ")
                    .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_offer_relation_detail nb, ")
                    .append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_spec_char nc ")
                    .append(" WHERE nb.product_offer_relation_id = a.product_offer_relation_id ")
                    .append(" AND nb.product_spec_char_id = nc.product_spec_char_id ")
                    .append(" AND nb.status = '1' AND nc.status = '1' ")
                    .append(" AND nc.code = 'VAS_DATA') ");
        }

        strQuery.append(" ) ");

        Query query = entityManager.createNativeQuery(strQuery.toString(), ProductOfferingEntity.class);
        query.setParameter("offerId", offerId);
        return query.getResultList();
    }

    public static String operatorToOracle(FilterRequest filter) {
        FilterRequest.Operator operator = filter.getOperator();
        if (operator == null) {
            operator = FilterRequest.Operator.EQ;
        }
        if (operator == FilterRequest.Operator.GT) {
            return ">";
        }
        if (operator == FilterRequest.Operator.LT) {
            return "<";
        }
        if (operator == FilterRequest.Operator.NE) {
            return "<>";
        }
        if (operator == FilterRequest.Operator.GOE) {
            return ">=";
        }
        if (operator == FilterRequest.Operator.LOE) {
            return "<=";
        }
        if (operator == FilterRequest.Operator.IN) {
            return "IN";
        }
        if (operator == FilterRequest.Operator.LIKE) {
            return "LIKE";
        }
        return "=";
    }
}