package com.viettel.bccs.productcatalog.productoffercharuse.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingCharacterFullDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecExtensionDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.mapper.ProductSpecCharUseMapper;
import com.viettel.bccs.productcatalog.productoffercharuse.mapper.ProductSpecCharValueUseMapper;
import com.viettel.bccs.productcatalog.productspecchar.entity.ProductSpecCharEntity;
import com.viettel.bccs.productcatalog.productspeccharvalue.entity.ProductSpecCharValueEntity;
import com.viettel.bccs.productcatalog.utils.Const;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductOfferCharUseRepositoryCustomImpl implements ProductOfferCharUseRepositoryCustom {

    private static final int BATCH_SIZE = 100;

    private final EntityManager entityManager;
    private final ProductSpecCharUseMapper productSpecCharUseMapper;
    private final ProductSpecCharValueUseMapper productSpecCharValueUseMapper;
    private final ObjectMapper objectMapper;

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
              CAST(a.PRODUCT_OFFERING_ID AS NUMBER(19)) AS PRODUCT_OFFERING_ID,
              CAST(a.PRODUCT_OFFER_CHAR_USE_ID AS NUMBER(19)) AS PRODUCT_OFFER_CHAR_USE_ID,
              a.TYPE,
              CAST(c.PRODUCT_SPEC_CHAR_ID AS NUMBER(19)) AS PRODUCT_SPEC_CHAR_ID,
              c.NAME,
              c.DESCRIPTION,
              c.VALUE_TYPE,
              c.CHAR_TYPE,
              CAST(c.MIN_CARDINALITY AS NUMBER(19)) AS MIN_CARDINALITY,
              CAST(c.MAX_CARDINALITY AS NUMBER(19)) AS MAX_CARDINALITY,
              c.STATUS AS char_status,
              c.CODE,
              c.PRODUCT_SPEC_CHAR_TYPE_ID,
              CAST(c.VALUE_SET_TYPE AS NUMBER(19)) AS VALUE_SET_TYPE,
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
              CAST(d.PRODUCT_SPEC_CHAR_VALUE_ID AS NUMBER(19)) AS PRODUCT_SPEC_CHAR_VALUE_ID,
              CAST(d.PRODUCT_SPEC_CHAR_ID AS NUMBER(19)) AS value_spec_char_id,
              d.VALUE_TYPE AS value_value_type,
              CAST(d.IS_DEFAULT AS NUMBER(19)) AS IS_DEFAULT,
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
    public List<Object[]> findCharsByOfferingIdAndCharType(Long offeringId, String charType) {
        if (offeringId == null || charType == null) {
            return new ArrayList<>();
        }

        String sql = """
            SELECT
              CAST(a.PRODUCT_OFFERING_ID AS NUMBER(19)) AS PRODUCT_OFFERING_ID,
              CAST(a.PRODUCT_OFFER_CHAR_USE_ID AS NUMBER(19)) AS PRODUCT_OFFER_CHAR_USE_ID,
              a.TYPE,
              CAST(c.PRODUCT_SPEC_CHAR_ID AS NUMBER(19)) AS PRODUCT_SPEC_CHAR_ID,
              c.NAME,
              c.DESCRIPTION,
              c.VALUE_TYPE,
              c.CHAR_TYPE,
              CAST(c.MIN_CARDINALITY AS NUMBER(19)) AS MIN_CARDINALITY,
              CAST(c.MAX_CARDINALITY AS NUMBER(19)) AS MAX_CARDINALITY,
              c.STATUS AS char_status,
              c.CODE,
              c.PRODUCT_SPEC_CHAR_TYPE_ID,
              CAST(c.VALUE_SET_TYPE AS NUMBER(19)) AS VALUE_SET_TYPE,
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
              CAST(d.PRODUCT_SPEC_CHAR_VALUE_ID AS NUMBER(19)) AS PRODUCT_SPEC_CHAR_VALUE_ID,
              CAST(d.PRODUCT_SPEC_CHAR_ID AS NUMBER(19)) AS value_spec_char_id,
              d.VALUE_TYPE AS value_value_type,
              CAST(d.IS_DEFAULT AS NUMBER(19)) AS IS_DEFAULT,
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
              AND a.PRODUCT_OFFERING_ID = :offeringId
              AND c.CHAR_TYPE = :charType
              AND a.TYPE IN ('1', '2')
            ORDER BY c.CODE
            """.formatted(
                Const.DEFAULT_PRODUCT_SCHEMA,
                Const.DEFAULT_PRODUCT_SCHEMA,
                Const.DEFAULT_PRODUCT_SCHEMA);

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("offeringId", offeringId);
        query.setParameter("charType", charType);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return results;
    }

    @Override
    public List<Object[]> findProductOfferCharacter(Long productOfferingId) {
        if (productOfferingId == null) {
            return new ArrayList<>();
        }

        // Column layout giống findSpecCharsByOfferingIds (index 0..39), thêm b.CODE (product_offering.code) ở index 40.
        String sql = """
            SELECT
              CAST(b.PRODUCT_OFFERING_ID AS NUMBER(19)) AS PRODUCT_OFFERING_ID,
              CAST(a.PRODUCT_OFFER_CHAR_USE_ID AS NUMBER(19)) AS PRODUCT_OFFER_CHAR_USE_ID,
              a.TYPE,
              CAST(c.PRODUCT_SPEC_CHAR_ID AS NUMBER(19)) AS PRODUCT_SPEC_CHAR_ID,
              c.NAME,
              c.DESCRIPTION,
              c.VALUE_TYPE,
              c.CHAR_TYPE,
              CAST(c.MIN_CARDINALITY AS NUMBER(19)) AS MIN_CARDINALITY,
              CAST(c.MAX_CARDINALITY AS NUMBER(19)) AS MAX_CARDINALITY,
              c.STATUS AS char_status,
              c.CODE,
              c.PRODUCT_SPEC_CHAR_TYPE_ID,
              CAST(c.VALUE_SET_TYPE AS NUMBER(19)) AS VALUE_SET_TYPE,
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
              CAST(d.PRODUCT_SPEC_CHAR_VALUE_ID AS NUMBER(19)) AS PRODUCT_SPEC_CHAR_VALUE_ID,
              CAST(d.PRODUCT_SPEC_CHAR_ID AS NUMBER(19)) AS value_spec_char_id,
              d.VALUE_TYPE AS value_value_type,
              CAST(d.IS_DEFAULT AS NUMBER(19)) AS IS_DEFAULT,
              d.VALUE,
              d.UNIT_OF_MEASURE,
              d.VALUE_FROM,
              d.VALUE_TO,
              d.RANGE_INTERVAL,
              d.STATUS AS value_status,
              d.NAME AS value_name,
              d.SPECIFIC_VALUE,
              d.NOTE AS value_note,
              b.CODE AS product_code
            FROM %sproduct_offering b
            JOIN %sproduct_offer_char_use a ON a.PRODUCT_OFFERING_ID = b.PRODUCT_OFFERING_ID AND a.STATUS = '1'
            JOIN %sproduct_spec_char c ON a.PRODUCT_SPEC_CHAR_ID = c.PRODUCT_SPEC_CHAR_ID AND c.STATUS = '1'
            JOIN %sproduct_spec_char_value d ON a.PRODUCT_SPEC_CHAR_VALUE_ID = d.PRODUCT_SPEC_CHAR_VALUE_ID AND d.STATUS = '1'
            WHERE b.PRODUCT_OFFERING_ID = :offeringId
              AND b.STATUS = '1'
            ORDER BY c.CODE
            """.formatted(
                Const.DEFAULT_PRODUCT_SCHEMA,
                Const.DEFAULT_PRODUCT_SCHEMA,
                Const.DEFAULT_PRODUCT_SCHEMA,
                Const.DEFAULT_PRODUCT_SCHEMA);

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("offeringId", productOfferingId);

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
    @Override
    public List<ProductOfferingCharacterFullDTO> getListPricePlanByOfferId(Long productOfferingId) {
        StringBuilder strQuery = new StringBuilder();
        List<ProductOfferingCharacterFullDTO> lst = new ArrayList<>();
        strQuery.append(" SELECT "
                + " c.PRODUCT_SPEC_CHAR_ID, c.NAME, c.DESCRIPTION, c.VALUE_TYPE, c.CHAR_TYPE, "
                + " c.MIN_CARDINALITY, c.MAX_CARDINALITY, c.STATUS, c.CODE, c.PRODUCT_SPEC_CHAR_TYPE_ID, "
                + " c.VALUE_SET_TYPE, c.RESPONSE_CLASS, c.SQL_QUERY, c.DISPLAY_OBJECT, c.VALUE_OBJECT, "
                + " c.SOLR_QUERY, c.SOLR_CORE, c.SOLR_SCHEMA, c.DATA_TYPE, c.WS_WSDL, "
                + " c.TEMPLATE_REQUEST, c.VALIDATE_PATTERN, c.EXT_DATA, c.NOTE, "
                + " d.PRODUCT_SPEC_CHAR_VALUE_ID, d.PRODUCT_SPEC_CHAR_ID, d.VALUE_TYPE, d.IS_DEFAULT, d.VALUE, "
                + " d.UNIT_OF_MEASURE, d.VALUE_FROM, d.VALUE_TO, d.RANGE_INTERVAL, d.STATUS, "
                + " d.NAME, d.SPECIFIC_VALUE, d.NOTE ");
        strQuery.append(" FROM   product_spec_char_use a, product_offering b, product_spec_char c,  product_spec_Char_value d ");
        strQuery.append(" WHERE      1= 1  ");
        strQuery.append(" AND b.product_offering_id = :productOfferingId    ");
        strQuery.append(" AND a.product_spec_id = b.product_spec_id    ");
        strQuery.append(" AND a.status ='1'   ");
        strQuery.append(" AND b.status ='1'   ");
        strQuery.append(" AND c.status ='1'    ");
        strQuery.append(" AND d.status ='1'    ");
        strQuery.append(" AND a.product_spec_char_id = c.product_spec_char_id    ");
        strQuery.append(" AND a.product_spec_Char_value_id = d.product_spec_char_value_id  (+)  ");
        strQuery.append(" AND c.char_type = :charType   ");
        strQuery.append(" and a.product_spec_char_id not in (   ");
        strQuery.append(" SELECT   a.product_spec_char_id ");
        strQuery.append(" FROM   product_offer_char_use a ");
        strQuery.append(" WHERE 1 = 1 ");
        strQuery.append(" AND a.product_offering_id = :productOfferingId ");
        strQuery.append(" AND a.status = '1') ");


        Query query = entityManager.createNativeQuery(strQuery.toString());
        query.setParameter("productOfferingId", productOfferingId);
        query.setParameter("charType", Const.CHAR_TYPE.PRICE_PLAN);

        List<Object[]> result = query.getResultList();
        for (Object[] temp : result) {
            ProductSpecCharEntity productSpecChar = buildCharEntity(temp);
            ProductSpecCharValueEntity productSpecCharValue = buildValueEntity(temp);
            ProductOfferingCharacterFullDTO productOfferingCharacterFullDTO = new ProductOfferingCharacterFullDTO();
            productOfferingCharacterFullDTO.setProductOfferingId(productOfferingId);
            productOfferingCharacterFullDTO.setProductSpecCharDTO(productSpecCharUseMapper.toDto(productSpecChar));
            productOfferingCharacterFullDTO.setProductSpecCharValueDTO(productSpecCharValueUseMapper.toDto(productSpecCharValue));
            lst.add(productOfferingCharacterFullDTO);
        }

        strQuery.setLength(0);
        strQuery.append(" SELECT   c, d ,"
                + " ( select offer.productOfferTypeId from ProductOfferingEntity offer where offer.productOfferingId = a.productOfferingId ) as offerType "
                + "  , a.specificValue ");
        strQuery.append(" FROM   ProductOfferCharUseEntity a,ProductSpecCharEntity c,  ProductSpecCharValueEntity d ");
        strQuery.append(" WHERE    1=1 ");
        strQuery.append(" AND a.productOfferingId = :productOfferingId ");
        strQuery.append(" AND a.status = '1' ");

        strQuery.append(" AND c.status = '1' ");
        strQuery.append(" AND d.status = '1' ");
        strQuery.append(" AND a.type IN ('1', '2') ");
        strQuery.append(" AND a.productSpecCharId = c.productSpecCharId    ");
        strQuery.append(" AND a.productSpecCharValueId = d.productSpecCharValueId    ");
        strQuery.append(" AND c.productSpecCharId = d.productSpecCharId    ");
        strQuery.append(" AND c.charType = :charType   ");
        query = entityManager.createQuery(strQuery.toString());
        query.setParameter("productOfferingId", productOfferingId);
        query.setParameter("charType", Const.CHAR_TYPE.PRICE_PLAN);
        result = query.getResultList();
        for (Object[] temp : result) {
            ProductSpecCharValueEntity productSpecCharValue = (ProductSpecCharValueEntity) temp[1];
            if (!(productSpecCharValue != null && Const.STATUS.INACTIVE.equals(productSpecCharValue.getStatus()))) {
                ProductSpecCharEntity productSpecChar = (ProductSpecCharEntity) temp[0];
                ProductOfferingCharacterFullDTO productOfferingCharacterFullDTO = new ProductOfferingCharacterFullDTO();
                productOfferingCharacterFullDTO.setProductOfferingId(productOfferingId);
                productOfferingCharacterFullDTO.setProductSpecCharDTO(productSpecCharUseMapper.toDto(productSpecChar));
                productOfferingCharacterFullDTO.setProductSpecCharValueDTO(productSpecCharValueUseMapper.toDto(productSpecCharValue));
                Long productOfferTypeId = DataUtil.safeToLong(temp[2], null);
                if (DataUtil.safeEqual(productOfferTypeId, Const.SPEC_CHAR_TYPE.OFFERING)) {
                    String specificValue = DataUtil.safeToString(temp[3]);
                    if (!DataUtil.isNullOrEmpty(specificValue)) {
                        try {
                            ProductSpecExtensionDTO productSpecExtensionDTO = objectMapper.readValue(specificValue, ProductSpecExtensionDTO.class);
                            if (productSpecExtensionDTO != null) {
                                productOfferingCharacterFullDTO.setExtensionType(productSpecExtensionDTO.getExtensionType());
                                productOfferingCharacterFullDTO.setExtensionValue(productSpecExtensionDTO.getExtensionValue());
                            }
                        } catch (Exception e) {
                            log.error("Parse specific_value failed for productOfferingId {}: {}", productOfferingId, specificValue, e);
                        }

                    }

                }
                lst.add(productOfferingCharacterFullDTO);
            }

        }
        return lst;
    }

    private ProductSpecCharEntity buildCharEntity(Object[] row) {
        return ProductSpecCharEntity.builder()
                .productSpecCharId(row[0] != null ? ((Number) row[0]).longValue() : null)
                .name(str(row[1]))
                .description(str(row[2]))
                .valueType(str(row[3]))
                .charType(str(row[4]))
                .minCardinality(row[5] != null ? ((Number) row[5]).longValue() : null)
                .maxCardinality(row[6] != null ? ((Number) row[6]).longValue() : null)
                .status(str(row[7]))
                .code(str(row[8]))
                .productSpecCharTypeId(str(row[9]))
                .valueSetType(row[10] != null ? ((Number) row[10]).longValue() : null)
                .responseClass(str(row[11]))
                .sqlQuery(str(row[12]))
                .displayObject(str(row[13]))
                .valueObject(str(row[14]))
                .solrQuery(str(row[15]))
                .solrCore(str(row[16]))
                .solrSchema(str(row[17]))
                .dataType(str(row[18]))
                .wsWsdl(str(row[19]))
                .templateRequest(str(row[20]))
                .validatePattern(str(row[21]))
                .extData(str(row[22]))
                .note(str(row[23]))
                .build();
    }

    private ProductSpecCharValueEntity buildValueEntity(Object[] row) {
        // Outer join d.product_spec_char_value_id (+) — khi miss (value_id null) trả về null entity
        if (row.length <= 24 || row[24] == null) {
            return null;
        }
        return ProductSpecCharValueEntity.builder()
                .productSpecCharValueId(((Number) row[24]).longValue())
                .productSpecCharId(row[25] != null ? ((Number) row[25]).longValue() : null)
                .valueType(str(row[26]))
                .isDefault(row[27] != null ? ((Number) row[27]).longValue() : null)
                .value(str(row[28]))
                .unitOfMeasure(str(row[29]))
                .valueFrom(str(row[30]))
                .valueTo(str(row[31]))
                .rangeInterval(str(row[32]))
                .status(str(row[33]))
                .name(str(row[34]))
                .specificValue(str(row[35]))
                .note(str(row[36]))
                .build();
    }

    private String str(Object val) {
        return val != null ? val.toString() : null;
    }
}
