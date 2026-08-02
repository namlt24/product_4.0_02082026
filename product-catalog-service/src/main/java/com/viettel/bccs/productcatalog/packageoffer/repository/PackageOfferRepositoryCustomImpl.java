package com.viettel.bccs.productcatalog.packageoffer.repository;

import com.viettel.bccs.productcatalog.packageoffer.entity.PackageOfferEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.NativeQuery;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PackageOfferRepositoryCustomImpl implements PackageOfferRepositoryCustom {

    private static final int BATCH_SIZE = 100;
    private final EntityManager entityManager;

    @Override
    public Map<Long, List<PackageOfferEntity>> getPackageOfferByListProdPackTypeIds(List<Long> prodPackTypeIds) {
        if (prodPackTypeIds == null || prodPackTypeIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<PackageOfferEntity>> result = new HashMap<>();
        for (int i = 0; i < prodPackTypeIds.size(); i += BATCH_SIZE) {
            List<Long> batch = prodPackTypeIds.subList(i, Math.min(i + BATCH_SIZE, prodPackTypeIds.size()));
            executeQuery(batch, result);
        }
        return result;
    }

    private void executeQuery(List<Long> prodPackTypeIds, Map<Long, List<PackageOfferEntity>> result) {
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < prodPackTypeIds.size(); i++) {
            inClause.append(i == 0 ? ":id" + i : ", :id" + i);
        }

        String sql = """
            SELECT TO_CHAR(a.PROD_PACK_OFFER_ID) AS PROD_PACK_OFFER_ID,
                   TO_CHAR(a.PRODUCT_OFFERING_ID) AS PRODUCT_OFFERING_ID,
                   TO_CHAR(a.PROD_PACK_TYPE_ID) AS PROD_PACK_TYPE_ID,
                   TO_CHAR(a.PRODUCT_OFFER_PRICE_ID) AS PRODUCT_OFFER_PRICE_ID,
                   a.STATUS,
                   a.SUPPLY_METHOD,
                   a.IS_MANDATORY,
                   a.CREATE_DATETIME,
                   a.CREATE_USER,
                   a.UPDATE_USER,
                   a.UPDATE_DATETIME,
                   TO_CHAR(a.NUM_OFFER) AS NUM_OFFER,
                   a.DESCRIPTION,
                   a.NEW_OR_SOLD,
                   a.EFFECT_DATETIME,
                   a.EXPIRE_DATETIME,
                   a.SHOW_OR_HIDE,
                   TO_CHAR(a.SAP_MATERIAL_NUMBER) AS SAP_MATERIAL_NUMBER
            FROM BCCS_PRODUCT.PACKAGE_OFFER a
            WHERE a.PROD_PACK_TYPE_ID IN (%s)
            """.formatted(inClause);

        NativeQuery<Tuple> query = (NativeQuery<Tuple>) entityManager.createNativeQuery(sql, Tuple.class);
        for (int i = 0; i < prodPackTypeIds.size(); i++) {
            query.setParameter("id" + i, prodPackTypeIds.get(i));
        }

        List<Tuple> rows = query.getResultList();
        for (Tuple row : rows) {
            PackageOfferEntity entity = mapRowToEntity(row);
            result.computeIfAbsent(entity.getProdPackTypeId(), k -> new ArrayList<>()).add(entity);
        }
    }

    private PackageOfferEntity mapRowToEntity(Tuple row) {
        PackageOfferEntity e = new PackageOfferEntity();
        e.setProdPackOfferId(toLong(row.get("PROD_PACK_OFFER_ID")));
        e.setProductOfferingId(toLong(row.get("PRODUCT_OFFERING_ID")));
        e.setProdPackTypeId(toLong(row.get("PROD_PACK_TYPE_ID")));
        e.setProductOfferPriceId(toLong(row.get("PRODUCT_OFFER_PRICE_ID")));
        e.setStatus(toString(row.get("STATUS")));
        e.setSupplyMethod(toString(row.get("SUPPLY_METHOD")));
        e.setIsMandatory(toString(row.get("IS_MANDATORY")));
        e.setCreateDatetime(toDate(row.get("CREATE_DATETIME")));
        e.setCreateUser(toString(row.get("CREATE_USER")));
        e.setUpdateUser(toString(row.get("UPDATE_USER")));
        e.setUpdateDatetime(toDate(row.get("UPDATE_DATETIME")));
        e.setNumOffer(toLong(row.get("NUM_OFFER")));
        e.setDescription(toString(row.get("DESCRIPTION")));
        e.setNewOrSold(toString(row.get("NEW_OR_SOLD")));
        e.setEffectDatetime(toDate(row.get("EFFECT_DATETIME")));
        e.setExpireDatetime(toDate(row.get("EXPIRE_DATETIME")));
        e.setShowOrHide(toString(row.get("SHOW_OR_HIDE")));
        e.setSapMaterialNumber(toLong(row.get("SAP_MATERIAL_NUMBER")));
        return e;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd.longValue();
        String str = value.toString().trim();
        return str.isEmpty() ? null : Long.parseLong(str);
    }

    private java.util.Date toDate(Object value) {
        if (value == null) return null;
        if (value instanceof java.util.Date) return (java.util.Date) value;
        if (value instanceof java.sql.Date) return new java.util.Date(((java.sql.Date) value).getTime());
        if (value instanceof java.sql.Timestamp) return new java.util.Date(((java.sql.Timestamp) value).getTime());
        return null;
    }

    private String toString(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        if (value instanceof Clob) {
            try { return ((Clob) value).getSubString(1, (int) Math.min(((Clob) value).length(), 4000)); }
            catch (Exception ex) { return null; }
        }
        return value.toString();
    }
}