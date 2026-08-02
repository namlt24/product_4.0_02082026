package com.viettel.bccs.productcatalog.productoffercharuse.repository;

import com.viettel.bccs.productcatalog.utils.Const;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductOfferCharUseRepositoryCustomImpl implements ProductOfferCharUseRepositoryCustom {

    private static final int BATCH_SIZE = 100;

    private final EntityManager entityManager;

    @Override
    public List<Object[]> findSpecCharsByOfferingIds(List<String> offeringIds) {
        if (offeringIds == null || offeringIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Object[]> allResults = new ArrayList<>();
        for (int i = 0; i < offeringIds.size(); i += BATCH_SIZE) {
            List<String> batch = offeringIds.subList(i, Math.min(i + BATCH_SIZE, offeringIds.size()));
            List<Object[]> batchResults = executeQuery(batch);
            allResults.addAll(batchResults);
        }
        return allResults;
    }

    private List<Object[]> executeQuery(List<String> offeringIds) {
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < offeringIds.size(); i++) {
            inClause.append(i == 0 ? ":id0" : ", :id" + i);
        }

        String sql = """
            SELECT
              a.PRODUCT_OFFERING_ID,
              a.PRODUCT_OFFER_CHAR_USE_ID,
              a.TYPE,
              c.PRODUCT_SPEC_CHAR_ID,
              c.NAME,
              c.DESCRIPTION,
              c.VALUE_TYPE,
              c.CHAR_TYPE,
              c.MIN_CARDINALITY,
              c.MAX_CARDINALITY,
              c.STATUS AS char_status,
              c.CODE,
              c.PRODUCT_SPEC_CHAR_TYPE_ID,
              c.VALUE_SET_TYPE,
              c.RESPONSE_CLASS,
              c.SQL_QUERY,
              c.DISPLAY_OBJECT,
              c.VALUE_OBJECT,
              c.SOLR_QUERY,
              c.SOLR_CORE,
              c.SOLR_SCHEMA,
              c.DATA_TYPE,
              c.WS_WSDL,
              c.TEMPLATE_REQUEST,
              c.VALIDATE_PATTERN,
              c.EXT_DATA,
              c.NOTE AS char_note,
              d.PRODUCT_SPEC_CHAR_VALUE_ID,
              d.PRODUCT_SPEC_CHAR_ID AS value_spec_char_id,
              d.VALUE_TYPE AS value_value_type,
              d.IS_DEFAULT,
              d.VALUE,
              d.UNIT_OF_MEASURE,
              d.VALUE_FROM,
              d.VALUE_TO,
              d.RANGE_INTERVAL,
              d.STATUS AS value_status,
              d.NAME AS value_name,
              d.SPECIFIC_VALUE,
              d.NOTE AS value_note,
              d.NAME AS value_name_from
            FROM %sproduct_offer_char_use a
            JOIN %sproduct_spec_char c ON a.PRODUCT_SPEC_CHAR_ID = c.PRODUCT_SPEC_CHAR_ID AND c.STATUS = '1'
            JOIN %sproduct_spec_char_value d ON a.PRODUCT_SPEC_CHAR_VALUE_ID = d.PRODUCT_SPEC_CHAR_VALUE_ID AND d.STATUS = '1'
            WHERE a.STATUS = '1'
              AND a.PRODUCT_OFFERING_ID IN (%s)
            ORDER BY a.PRODUCT_OFFERING_ID, c.CODE
            """.formatted(
                Const.DEFAULT_PRODUCT_SCHEMA,
                Const.DEFAULT_PRODUCT_SCHEMA,
                Const.DEFAULT_PRODUCT_SCHEMA,
                inClause);

        Query query = entityManager.createNativeQuery(sql);
        for (int i = 0; i < offeringIds.size(); i++) {
            query.setParameter("id" + i, offeringIds.get(i));
        }

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return results;
    }

    @Override
    public Optional<String> findAttributeValueByOfferingIdAndCharCode(Long offeringId, String charCode) {
        String sql = """
            SELECT COALESCE(a.SPECIFIC_VALUE, d.VALUE) AS attribute_value
            FROM %sproduct_offer_char_use a
            JOIN %sproduct_spec_char c ON a.PRODUCT_SPEC_CHAR_ID = c.PRODUCT_SPEC_CHAR_ID AND c.STATUS = '1'
            LEFT JOIN %sproduct_spec_char_value d ON a.PRODUCT_SPEC_CHAR_VALUE_ID = d.PRODUCT_SPEC_CHAR_VALUE_ID AND d.STATUS = '1'
            WHERE a.STATUS = '1'
              AND a.PRODUCT_OFFERING_ID = :offeringId
              AND c.CODE = :charCode
            FETCH FIRST 1 ROWS ONLY
            """.formatted(
                Const.DEFAULT_PRODUCT_SCHEMA,
                Const.DEFAULT_PRODUCT_SCHEMA,
                Const.DEFAULT_PRODUCT_SCHEMA);

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("offeringId", offeringId);
        query.setParameter("charCode", charCode);

        List<?> results = query.getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        Object val = results.get(0);
        return Optional.ofNullable(val != null ? val.toString() : null);
    }
}