package com.viettel.bccs.productcatalog.productpackage.repository;

import com.viettel.bccs.productcatalog.productpackage.dto.response.ProductPackageDTO;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Optional;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductPackageRepositoryCustomImpl implements ProductPackageRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public Optional<ProductPackageDTO> findActiveByCode(String code) {
        String sql = """
            SELECT TO_CHAR(prp.PRODUCT_PACKAGE_ID) AS PRODUCT_PACKAGE_ID,
                   prp.NAME,
                   prp.CODE,
                   prp.DESCRIPTION,
                   prp.STATUS,
                   prp.EFFECT_DATETIME,
                   prp.EXPIRE_DATETIME,
                   prp.CREATE_USER,
                   prp.CREATE_DATETIME,
                   prp.UPDATE_USER,
                   prp.UPDATE_DATETIME,
                   prp.VERSION,
                   prp.TYPE,
                   TO_CHAR(prp.ACCOUNTING_ID) AS ACCOUNTING_ID,
                   prp.FEE_TYPE,
                   TO_CHAR(prp.TELECOM_SERVICE_ID) AS TELECOM_SERVICE_ID,
                   prp.UNIT,
                   prp.NOTE,
                   TO_CHAR(prp.AREA_GROUP_ID) AS AREA_GROUP_ID,
                   TO_CHAR(prp.OWNER_SHOP_ID) AS OWNER_SHOP_ID,
                   TO_CHAR(prp.SAP_MATERIAL_NUMBER) AS SAP_MATERIAL_NUMBER,
                   prp.IT_TELCOL,
                   (SELECT pcu.SPECIFIC_VALUE
                    FROM BCCS_PRODUCT.PRODUCT_PACKAGE_CHAR_USE pcu
                    WHERE pcu.PRODUCT_SPEC_CHAR_ID IN (
                        SELECT psc.PRODUCT_SPEC_CHAR_ID
                        FROM BCCS_PRODUCT.PRODUCT_SPEC_CHAR psc
                        WHERE psc.CODE = 'GENERALNAME'
                    )
                      AND pcu.PRODUCT_PACKAGE_ID = prp.PRODUCT_PACKAGE_ID
                      AND pcu.STATUS = 1
                      AND ROWNUM = 1) AS defaultName
            FROM BCCS_PRODUCT.PRODUCT_PACKAGE prp
            WHERE prp.STATUS = '1'
              AND UPPER(prp.CODE) = UPPER(:code)
              AND (prp.EFFECT_DATETIME IS NULL OR prp.EFFECT_DATETIME < SYSDATE)
              AND (prp.EXPIRE_DATETIME IS NULL OR prp.EXPIRE_DATETIME >= TRUNC(SYSDATE))
            """;

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        query.setParameter("code", code);
        var results = query.getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapTupleToDTO((Tuple) results.get(0)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProductPackageDTO> getProductPackageExtra(String code, String productPackageType, boolean isActive, boolean isUpperCode, boolean checkStatus) {
        StringBuilder sql = new StringBuilder();
        sql.append("""
                SELECT TO_CHAR(prp.PRODUCT_PACKAGE_ID) AS PRODUCT_PACKAGE_ID,
                       prp.NAME,
                       prp.CODE,
                       prp.DESCRIPTION,
                       prp.STATUS,
                       prp.EFFECT_DATETIME,
                       prp.EXPIRE_DATETIME,
                       prp.CREATE_USER,
                       prp.CREATE_DATETIME,
                       prp.UPDATE_USER,
                       prp.UPDATE_DATETIME,
                       prp.VERSION,
                       prp.TYPE,
                       TO_CHAR(prp.ACCOUNTING_ID) AS ACCOUNTING_ID,
                       prp.FEE_TYPE,
                       TO_CHAR(prp.TELECOM_SERVICE_ID) AS TELECOM_SERVICE_ID,
                       prp.UNIT,
                       prp.NOTE,
                       TO_CHAR(prp.AREA_GROUP_ID) AS AREA_GROUP_ID,
                       TO_CHAR(prp.OWNER_SHOP_ID) AS OWNER_SHOP_ID,
                       TO_CHAR(prp.SAP_MATERIAL_NUMBER) AS SAP_MATERIAL_NUMBER,
                       prp.IT_TELCOL,
                       (SELECT pcu.SPECIFIC_VALUE
                        FROM BCCS_PRODUCT.PRODUCT_PACKAGE_CHAR_USE pcu
                        WHERE pcu.PRODUCT_SPEC_CHAR_ID IN (
                            SELECT psc.PRODUCT_SPEC_CHAR_ID
                            FROM BCCS_PRODUCT.PRODUCT_SPEC_CHAR psc
                            WHERE psc.CODE = 'GENERALNAME'
                        )
                          AND pcu.PRODUCT_PACKAGE_ID = prp.PRODUCT_PACKAGE_ID
                          AND pcu.STATUS = 1
                          AND ROWNUM = 1) AS defaultName
                FROM BCCS_PRODUCT.PRODUCT_PACKAGE prp
                WHERE 1 = 1
                """);

        if (checkStatus) {
            sql.append(" AND prp.STATUS = :p_status");
        }
        if (isActive) {
            sql.append(" AND (prp.EFFECT_DATETIME IS NULL OR prp.EFFECT_DATETIME < SYSDATE) AND (prp.EXPIRE_DATETIME IS NULL OR prp.EXPIRE_DATETIME >= TRUNC(SYSDATE))");
        }
        if (isUpperCode) {
            sql.append(" AND UPPER(prp.CODE) = UPPER(:p_code)");
        } else {
            sql.append(" AND prp.CODE = :p_code");
        }
        if (!org.springframework.util.StringUtils.isEmpty(productPackageType)) {
            sql.append(" AND prp.TYPE = :p_type");
        }

        sql.append(" ORDER BY prp.PRODUCT_PACKAGE_ID");

        Query query = entityManager.createNativeQuery(sql.toString(), Tuple.class);

        if (checkStatus) {
            query.setParameter("p_status", "1");
        }
        if (!org.springframework.util.StringUtils.isEmpty(code)) {
            query.setParameter("p_code", code);
        }
        if (!org.springframework.util.StringUtils.isEmpty(productPackageType)) {
            query.setParameter("p_type", productPackageType);
        }

        List<Tuple> results = query.getResultList();
        if (results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream().map(this::mapTupleToDTO).toList();
    }

    private ProductPackageDTO mapTupleToDTO(Tuple tuple) {
        return ProductPackageDTO.builder()
                .productPackageId(DataUtil.safeToLong(tuple.get("PRODUCT_PACKAGE_ID"), null))
                .name(DataUtil.safeToString(tuple.get("NAME"), null))
                .code(DataUtil.safeToString(tuple.get("CODE"), null))
                .description(DataUtil.safeToString(tuple.get("DESCRIPTION"), null))
                .status(DataUtil.safeToString(tuple.get("STATUS"), null))
                .effectDatetime(DataUtil.toDate(tuple.get("EFFECT_DATETIME")))
                .expireDatetime(DataUtil.toDate(tuple.get("EXPIRE_DATETIME")))
                .createUser(DataUtil.safeToString(tuple.get("CREATE_USER"), null))
                .createDatetime(DataUtil.toDate(tuple.get("CREATE_DATETIME")))
                .updateUser(DataUtil.safeToString(tuple.get("UPDATE_USER"), null))
                .updateDatetime(DataUtil.toDate(tuple.get("UPDATE_DATETIME")))
                .version(DataUtil.safeToString(tuple.get("VERSION"), null))
                .type(DataUtil.safeToString(tuple.get("TYPE"), null))
                .accountingId(DataUtil.safeToLong(tuple.get("ACCOUNTING_ID"), null))
                .feeType(DataUtil.safeToString(tuple.get("FEE_TYPE"), null))
                .telecomServiceId(DataUtil.safeToLong(tuple.get("TELECOM_SERVICE_ID"), null))
                .unit(DataUtil.safeToString(tuple.get("UNIT"), null))
                .note(DataUtil.safeToString(tuple.get("NOTE"), null))
                .areaGroupId(DataUtil.safeToLong(tuple.get("AREA_GROUP_ID"), null))
                .ownerShopId(DataUtil.safeToLong(tuple.get("OWNER_SHOP_ID"), null))
                .sapMaterialNumber(DataUtil.safeToLong(tuple.get("SAP_MATERIAL_NUMBER"), null))
                .itTelcol(DataUtil.safeToString(tuple.get("IT_TELCOL"), null))
                .defaultName(DataUtil.safeToString(tuple.get("defaultName"), null))
                .build();
    }
}