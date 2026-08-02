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
        appendCondition(sql, "a.product_offering_id = :proOfferId", proOfferId != null && proOfferId > 0);
        appendCondition(sql, "a.code = :prodOfferCode", prodOfferCode != null && !prodOfferCode.isEmpty());

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