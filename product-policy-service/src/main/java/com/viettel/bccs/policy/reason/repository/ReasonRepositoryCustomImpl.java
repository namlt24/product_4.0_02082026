package com.viettel.bccs.policy.reason.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.policy.common.dto.FilterRequest;
import com.viettel.bccs.policy.reason.dto.response.ReasonDTO;
import com.viettel.bccs.policy.reason.entity.ReasonEntity;
import com.viettel.bccs.policy.reason.mapper.ReasonMapper;
import com.viettel.bccs.policy.utils.Const;
import com.viettel.bccs.policy.utils.DataUtil;
import com.viettel.bccs.policy.utils.DateUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ReasonRepositoryCustomImpl implements ReasonRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ReasonMapper mapper;

    @Override
    @SuppressWarnings("unchecked")
    public List<ReasonEntity> getListReasonByActionCodeAndTelServiceForAuditWithMappingChecking(
            String actionCode, Long telServiceId, String payType, Long numProduct, boolean checkStatus,
            List<String> excludeProdOfferTypeIds) {
        StringBuilder strQuery = new StringBuilder("");
        if (Const.ActionCode.SUB_CONNECTION.equals(actionCode)) {
            strQuery.append(" select distinct r.* from " + Const.DEFAULT_PRODUCT_SCHEMA + "reason r, " + Const.DEFAULT_PRODUCT_SCHEMA + "action a, " + Const.DEFAULT_PRODUCT_SCHEMA + "mapping m, " + Const.DEFAULT_PRODUCT_SCHEMA + "ref_product_package pp ");
            strQuery.append(" where 1=1 ");
            strQuery.append(" and m.reason_id = r.reason_id and m.sale_service_code = pp.code  " +
                    "and m.status = '1' and pp.status = '1' " +
                    "and (pp.EXPIRE_DATETIME is null or pp.EXPIRE_DATETIME >= trunc(sysdate))  " +
                    " ");
            if (checkStatus) {
                strQuery.append(" and r.status = :status   ");
                strQuery.append(" AND (r.effect_datetime IS NULL OR r.effect_datetime < TRUNC(sysdate) + 1) ");
                strQuery.append(" AND (r.expire_datetime IS NULL OR r.expire_datetime >= TRUNC(sysdate)) ");
            }
            strQuery.append(" and (r.type is null or r.type <> '0') ");
            strQuery.append(" and r.reason_type = a.reason_type   ");
            strQuery.append(" and a.action_code = :actionCode ");
            strQuery.append(" and (r.description not like :description or r.description is null) ");
            strQuery.append(" " +
                    " AND EXISTS " +
                    " (SELECT  1 FROM  bccs_product.ref_product_package_fee ppf " +
                    " WHERE ppf.product_package_id = pp.product_package_id AND status = 1 " +
                    " AND (ppf.code is null or ppf.code = 'PHM') " +
                    " AND (ppf.expire_datetime IS NULL OR ppf.expire_datetime >= TRUNC (SYSDATE))) ");

            if (numProduct != null) {
                strQuery.append(" AND ( SELECT COUNT (1)" +
                        " from REF_PROD_PACK_PO_TYPE ppp" +
                        " WHERE     ppp.product_package_id = pp.product_package_id " +
                        "AND to_char(ppp.product_offer_type_id)  in (:excludeProdOfferTypeIds)) = :pNumber ");

            }
        } else {
            if (numProduct != null) {
                strQuery.append(" select distinct r.* from " + Const.DEFAULT_PRODUCT_SCHEMA + "reason r, " + Const.DEFAULT_PRODUCT_SCHEMA + "action a, " + Const.DEFAULT_PRODUCT_SCHEMA + "mapping m, " + Const.DEFAULT_PRODUCT_SCHEMA + "product_package pp ");
                strQuery.append(" where 1=1 ");
                strQuery.append(" and m.reason_id = r.reason_id and m.sale_service_code = pp.code  " +
                        "and m.status = '1' and pp.status = '1' " +
                        "and (pp.EXPIRE_DATETIME is null or pp.EXPIRE_DATETIME >= trunc(sysdate))  " +
                        " ");
                if (checkStatus) {
                    strQuery.append(" and r.status = :status   ");
                    strQuery.append(" AND (r.effect_datetime IS NULL OR r.effect_datetime < TRUNC(sysdate) + 1) ");
                    strQuery.append(" AND (r.expire_datetime IS NULL OR r.expire_datetime >= TRUNC(sysdate)) ");
                }
                strQuery.append(" and (r.type is null or r.type <> '0') ");
                strQuery.append(" and r.reason_type = a.reason_type   ");
                strQuery.append(" and a.action_code = :actionCode ");
                strQuery.append(" and (r.description not like :description or r.description is null) ");
                strQuery.append(" AND ( SELECT COUNT (1)" +
                        " from REF_PROD_PACK_PO_TYPE ppp" +
                        " WHERE     ppp.product_package_id = pp.product_package_id " +
                        "AND to_char(ppp.product_offer_type_id)  in (:excludeProdOfferTypeIds)) = :pNumber ");

            } else {
                strQuery.append(" select distinct r.* from " + Const.DEFAULT_PRODUCT_SCHEMA + "reason r, " + Const.DEFAULT_PRODUCT_SCHEMA + "action a ");
                strQuery.append(" where 1=1 ");
                if (checkStatus) {
                    strQuery.append(" and r.status = :status   ");
                    strQuery.append(" AND (r.effect_datetime IS NULL OR r.effect_datetime < TRUNC(sysdate) + 1) ");
                    strQuery.append(" AND (r.expire_datetime IS NULL OR r.expire_datetime >= TRUNC(sysdate)) ");
                }
                strQuery.append(" and (r.type is null or r.type <> '0') ");
                strQuery.append(" and r.reason_type = a.reason_type   ");
                strQuery.append(" and a.action_code = :actionCode ");
                strQuery.append(" and (r.description not like :description or r.description is null) ");
            }

        }

        if (!DataUtil.isNullOrEmpty(payType)) {
            strQuery.append(" and r.pay_type = :payType ");
        }
        if (!DataUtil.isNullObject(telServiceId) && !DataUtil.safeEqual(Const.DEFAULT_VALUE_MAP_SELECT_ALL, telServiceId)) {
            strQuery.append(" and ','||r.tel_service||',' like :telService ");
        }
        if (checkStatus) {
            strQuery.append(" and (r.effect_datetime is null or r.effect_datetime < TRUNC(sysdate) + 1) ");
            strQuery.append(" and (r.expire_datetime is null or r.expire_datetime >= TRUNC(sysdate)) ");
        }
        strQuery.append(" order by NLSSORT(r.name,'NLS_SORT=vietnamese') ");
        Query query = em.createNativeQuery(strQuery.toString(), ReasonEntity.class);
        query.setParameter("status", Const.Status.ACTIVE);
        query.setParameter("actionCode", actionCode);
        query.setParameter("description", "%" + Const.STRING_DESCRIPTION_REASON_COMMITMENT + "%");
        if (!DataUtil.isNullOrEmpty(payType)) {
            query.setParameter("payType", payType);
        }
        if (!DataUtil.isNullObject(telServiceId) && !DataUtil.safeEqual(Const.DEFAULT_VALUE_MAP_SELECT_ALL, telServiceId)) {
            query.setParameter("telService", "%," + String.valueOf(telServiceId) + ",%");
        }
        if (numProduct != null) {
            query.setParameter("pNumber", numProduct);
            query.setParameter("excludeProdOfferTypeIds", safeList(excludeProdOfferTypeIds));

        }
        List<ReasonEntity> listResult = query.getResultList();
            return listResult;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ReasonEntity> getByActionCodeOrderByIdWithMappingChecking(String actionCode, Long telServiceId, Long numProduct, String productOfferType,
                                                                          List<String> excludeProdOfferTypeIds) {
        String strQuery;
        boolean setTelService = false;
        if (Const.ActionCode.SUB_CONNECTION.equals(actionCode) && !Const.ProductOfferType.VAS.equals(productOfferType)) {
            strQuery = ("SELECT distinct a.* FROM " + Const.DEFAULT_PRODUCT_SCHEMA + "reason a, " + Const.DEFAULT_PRODUCT_SCHEMA + "mapping m, " + Const.DEFAULT_PRODUCT_SCHEMA + "ref_product_package pp ") +
                    "WHERE 1 = 1 and m.reason_id = a.reason_id and m.sale_service_code = pp.code and m.status = '1' and pp.status = '1' and (pp.EXPIRE_DATETIME is null or pp.EXPIRE_DATETIME >= trunc(sysdate)) ";
            if (telServiceId != null) {
                strQuery = strQuery + " and m.TEL_SERVICE_ID = :telServiceId ";
                setTelService = true;
            }
            if (numProduct != null) {
                strQuery = strQuery + " AND ( SELECT COUNT (1)" +
                        " from bccs_product.REF_PROD_PACK_PO_TYPE ppp" +
                        " WHERE     ppp.product_package_id = pp.product_package_id AND " +
                        " to_char(ppp.product_offer_type_id)  in " +
                        "(:excludeProdOfferTypeIds)) = :numProduct ";

            }

            strQuery = strQuery + " AND EXISTS (SELECT  1 FROM ref_product_package_fee ppf WHERE ppf.product_package_id = pp.product_package_id AND status = 1  AND (ppf.code is null or ppf.code = 'PHM')  AND (ppf.expire_datetime IS NULL OR ppf.expire_datetime >= TRUNC (SYSDATE))) " +
                    " and a.reason_type IN ( SELECT reason_type FROM " + Const.DEFAULT_PRODUCT_SCHEMA + "action WHERE status = '1' AND action_code = :actionCode )  " +
                    " AND a.status = '1'  " +
                    " AND (a.effect_datetime IS NULL OR a.effect_datetime < TRUNC(sysdate) + 1) " +
                    " AND (a.expire_datetime IS NULL OR a.expire_datetime >= TRUNC(sysdate)) " +
                    " AND (a.type IS NULL OR a.type <> '0') " +
                    " ORDER BY a.reason_id ASC ";

        } else {
            if (numProduct != null) {
                strQuery = ("SELECT distinct a.* FROM " + Const.DEFAULT_PRODUCT_SCHEMA + "reason a, " + Const.DEFAULT_PRODUCT_SCHEMA + "mapping m, " + Const.DEFAULT_PRODUCT_SCHEMA + "ref_product_package pp ") +
                        "WHERE 1 = 1 and m.reason_id = a.reason_id and m.sale_service_code = pp.code and m.status = '1' and pp.status = '1' and (pp.EXPIRE_DATETIME is null or pp.EXPIRE_DATETIME >= trunc(sysdate)) ";
                if (telServiceId != null) {
                    strQuery = strQuery + " and m.TEL_SERVICE_ID = :telServiceId ";
                    setTelService = true;
                }

                strQuery = strQuery + " AND ( SELECT COUNT (1)" +
                        " from bccs_product.REF_PROD_PACK_PO_TYPE ppp" +
                        " WHERE     ppp.product_package_id = pp.product_package_id AND " +
                        " to_char(ppp.product_offer_type_id)  in " +
                        "(:excludeProdOfferTypeIds)) = :numProduct ";


                strQuery = strQuery + " and a.reason_type IN ( SELECT reason_type FROM " + Const.DEFAULT_PRODUCT_SCHEMA + "action WHERE status = '1' AND action_code = :actionCode )  " +
                        " AND a.status = '1'  " +
                        " AND (a.effect_datetime IS NULL OR a.effect_datetime < TRUNC(sysdate) + 1) " +
                        " AND (a.expire_datetime IS NULL OR a.expire_datetime >= TRUNC(sysdate)) " +
                        " AND (a.type IS NULL OR a.type <> '0') " +
                        " ORDER BY a.reason_id ASC ";
            } else {
                strQuery = ("SELECT a.* FROM " + Const.DEFAULT_PRODUCT_SCHEMA + "reason a ") +
                        "WHERE 1 = 1  " +
                        " and a.reason_type IN ( SELECT reason_type FROM " + Const.DEFAULT_PRODUCT_SCHEMA + "action WHERE status = '1' AND action_code = :actionCode )  " +
                        " AND a.status = '1'  " +
                        " AND (a.effect_datetime IS NULL OR a.effect_datetime < TRUNC(sysdate) + 1) " +
                        " AND (a.expire_datetime IS NULL OR a.expire_datetime >= TRUNC(sysdate)) " +
                        " AND (a.type IS NULL OR a.type <> '0') " +
                        " ORDER BY a.reason_id ASC ";
            }

        }
        Query query = em.createNativeQuery(strQuery, ReasonEntity.class);
        query.setParameter("actionCode", actionCode);
        if (setTelService) {
            query.setParameter("telServiceId", telServiceId);
        }
        if (numProduct != null) {
            query.setParameter("numProduct", numProduct);
            query.setParameter("excludeProdOfferTypeIds", safeList(excludeProdOfferTypeIds));

        }
        return query.getResultList();
    }

    private static List<String> safeList(List<String> ids) {
        return DataUtil.isNullOrEmpty(ids) ? List.of("-1") : ids;
    }

    @Override
    public List<ReasonDTO> findByLstIdWithSpec(List<Long> lstReasonId, List<FilterRequest> listProductSpec, String productCode) {
        if (DataUtil.isNullOrEmpty(lstReasonId)) {
            return new ArrayList<>();
        }
        try {
            ConcurrentHashMap<String, Object> params = new ConcurrentHashMap<>();
            String sqlQuery = "SELECT rs.* FROM BCCS_PRODUCT.REASON  rs where rs.REASON_ID in( :reasonId ) and rs.status = 1 ";
            if (!DataUtil.isNullObject(listProductSpec)) {
                for (FilterRequest filterRequest : listProductSpec) {
                    if (Boolean.TRUE.equals(filterRequest.getNotEqual())) {
                        sqlQuery += "       AND NOT EXISTS " +
                                "               (SELECT 1 " +
                                "                  FROM bccs_product.reason_char_use b, " +
                                "                       bccs_product.product_spec_char c, " +
                                "                       bccs_product.product_spec_char_value d " +
                                "                 WHERE     b.product_spec_char_id = c.product_spec_char_id " +
                                "                       AND b.reason_id = rs.reason_id " +
                                "                       AND b.status = 1 " +
                                "                       AND c.status = 1 " +
                                "                       AND d.status = 1 " +
                                "                       AND d.product_spec_char_value_id = " +
                                "                               b.product_spec_char_value_id " +
                                "                       AND c.code =  " + ":p_" + filterRequest.getProperty();
                        params.put("p_" + filterRequest.getProperty(), filterRequest.getProperty());
                        if (!DataUtil.isNullOrEmpty(filterRequest.getValueText())) {
                            if (filterRequest.getProperty().equals(Const.ProductSpecChar.SINGLE_OR_COMBO) || filterRequest.getProperty().equals(Const.ProductSpecChar.IS_PROJECT)) {
                                if ("1".equals(filterRequest.getValueText())) {
                                    sqlQuery += " AND d.VALUE IN ('1','3') )";
                                } else {
                                    sqlQuery += " AND d.VALUE IN ('2','3') )";
                                }
                            }else if (filterRequest.getProperty().equals(Const.ProductSpecChar.FTTH_PRODUCT_CODE) || filterRequest.getProperty().equals(Const.ProductSpecChar.FTTH_OLD_CODE)) {
                                if (!DataUtil.isNullOrEmpty(filterRequest.getProperty())) {
                                    sqlQuery += " AND (',' || d.VALUE || ',') LIKE ('%,' || :p_value_" + filterRequest.getProperty() + " || ',%')) ";
                                }
                                if (DataUtil.safeEqual(DataUtil.toUpper(filterRequest.getValueType()), "LONG")) {
                                    sqlQuery += "               AND to_number(d.VALUE) " + DataUtil.operatorToOracle(filterRequest) + " to_number(:p_value_" + filterRequest.getProperty() + "))";
                                } else if(DataUtil.safeEqual(DataUtil.toUpper(filterRequest.getValueType()), "DATE")) {
                                    String datePattern = "dd/MM/yyyy";
                                    if (!DateUtil.isValidDateFormat(DataUtil.safeToString(filterRequest.getValueText()), "dd/MM/yyyy")) {
                                        throw new BusinessException("BCCS-POLICY-REASON-0002", "product.lb.productoffer.validateDate");
                                    } else {
                                        if (Boolean.TRUE.equals(filterRequest.getValueInRange())) {
                                            sqlQuery += " AND (d.VALUE_FROM is null or TRUNC(TO_DATE(d.VALUE_FROM, '" + datePattern + "')) <= TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "'))) " +
                                                    " AND (d.VALUE_TO is null or TRUNC(TO_DATE(d.VALUE_TO, '" + datePattern + "')) >= TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "'))))";
                                        } else {
                                            sqlQuery += " AND TRUNC(TO_DATE(d.VALUE, '" + datePattern + "')) " +
                                                    DataUtil.operatorToOracle(filterRequest) +
                                                    " TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "')))";
                                        }
                                    }
                                }
                                params.put("p_value_" + filterRequest.getProperty(), DataUtil.safeToString(filterRequest.getValueText()));
                            } else {
                                if (DataUtil.safeEqual(DataUtil.toUpper(filterRequest.getValueType()), "LONG")) {
                                    // neu truyen valueInRange = true thi check theo khoang gia tri
                                    if(Boolean.TRUE.equals(filterRequest.getValueInRange())){
                                        sqlQuery += " AND (d.VALUE_FROM is null or to_number(d.VALUE_FROM) <= to_number(:p_value_" + filterRequest.getProperty() +  ")) AND (d.VALUE_TO is null or to_number(d.VALUE_TO) >= to_number(:p_value_" + filterRequest.getProperty() +  ")))";
                                    }else{
                                        sqlQuery += "               AND to_number(d.VALUE) " + DataUtil.operatorToOracle(filterRequest) + " to_number(:p_value_" + filterRequest.getProperty() + "))";
                                    }
                                } else if(DataUtil.safeEqual(DataUtil.toUpper(filterRequest.getValueType()), "DATE")) {
                                    String datePattern = "dd/MM/yyyy";
                                    if (!DateUtil.isValidDateFormat(DataUtil.safeToString(filterRequest.getValueText()), "dd/MM/yyyy")) {
                                        throw new BusinessException("BCCS-POLICY-REASON-0002", "product.lb.productoffer.validateDate");
                                    } else {
                                        if (Boolean.TRUE.equals(filterRequest.getValueInRange())) {
                                            sqlQuery += " AND (d.VALUE_FROM is null or TRUNC(TO_DATE(d.VALUE_FROM, '" + datePattern + "')) <= TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "'))) " +
                                                    " AND (d.VALUE_TO is null or TRUNC(TO_DATE(d.VALUE_TO, '" + datePattern + "')) >= TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "'))))";
                                        } else {
                                            sqlQuery += " AND TRUNC(TO_DATE(d.VALUE, '" + datePattern + "')) " +
                                                    DataUtil.operatorToOracle(filterRequest) +
                                                    " TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "')))";
                                        }
                                    }
                                }else {
                                    sqlQuery += "               AND d.VALUE " + DataUtil.operatorToOracle(filterRequest) + " :p_value_" + filterRequest.getProperty() + ")";
                                }
                                params.put("p_value_" + filterRequest.getProperty(), DataUtil.safeToString(filterRequest.getValueText()));
                            }
                        } else {
                            sqlQuery += ") ";
                        }
                    } else {
                        if (Boolean.TRUE.equals(filterRequest.getExtract())) {
                            sqlQuery += "       AND EXISTS " +
                                    "               (SELECT 1 " +
                                    "                  FROM bccs_product.reason_char_use b, " +
                                    "                       bccs_product.product_spec_char c, " +
                                    "                       bccs_product.product_spec_char_value d " +
                                    "                 WHERE     b.product_spec_char_id = c.product_spec_char_id " +
                                    "                       AND b.reason_id = rs.reason_id " +
                                    "                       AND b.status = 1 " +
                                    "                       AND c.status = 1 " +
                                    "                       AND d.status = 1 " +
                                    "                       AND d.product_spec_char_value_id = " +
                                    "                               b.product_spec_char_value_id " +
                                    "                       AND c.code =  " + ":p_" + filterRequest.getProperty();
                            params.put("p_" + filterRequest.getProperty(), filterRequest.getProperty());
                            if (!DataUtil.isNullOrEmpty(filterRequest.getValueText())) {
                                if (filterRequest.getProperty().equals(Const.ProductSpecChar.SINGLE_OR_COMBO)) {
                                    if ("1".equals(filterRequest.getValueText())) {
                                        sqlQuery += " AND d.VALUE IN ('1','3') )";
                                    } else {
                                        sqlQuery += " AND d.VALUE IN ('2','3') )";
                                    }
                                } else if (filterRequest.getProperty().equals(Const.ProductSpecChar.ARPU_SINGLE)) {
                                    if (!DataUtil.isNullOrEmpty(productCode)) {
                                        sqlQuery += "               AND upper(b.specific_value) = upper(:productCode) ";
                                        params.put("productCode", productCode);
                                    }
                                    if (DataUtil.safeEqual(DataUtil.toUpper(filterRequest.getValueType()), "LONG")) {
                                        sqlQuery += "               AND to_number(d.VALUE) " + DataUtil.operatorToOracle(filterRequest) + " to_number(:p_value_" + filterRequest.getProperty() + "))";
                                    } else if(DataUtil.safeEqual(DataUtil.toUpper(filterRequest.getValueType()), "DATE")) {
                                        String datePattern = "dd/MM/yyyy";
                                        if (!DateUtil.isValidDateFormat(DataUtil.safeToString(filterRequest.getValueText()), "dd/MM/yyyy")) {
                                            throw new BusinessException("BCCS-POLICY-REASON-0002", "product.lb.productoffer.validateDate");
                                        } else {
                                            if (Boolean.TRUE.equals(filterRequest.getValueInRange())) {
                                                sqlQuery += " AND (d.VALUE_FROM is null or TRUNC(TO_DATE(d.VALUE_FROM, '" + datePattern + "')) <= TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "')))) " +
                                                        " AND (d.VALUE_TO is null or TRUNC(TO_DATE(d.VALUE_TO, '" + datePattern + "')) >= TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "')))))";
                                            } else {
                                                sqlQuery += " AND TRUNC(TO_DATE(d.VALUE, '" + datePattern + "')) " +
                                                        DataUtil.operatorToOracle(filterRequest) +
                                                        " TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "')))";
                                            }
                                        }
                                    }
                                }else if (filterRequest.getProperty().equals(Const.ProductSpecChar.FTTH_PRODUCT_CODE) || filterRequest.getProperty().equals(Const.ProductSpecChar.FTTH_OLD_CODE)) {
                                    if (!DataUtil.isNullOrEmpty(filterRequest.getProperty())) {
                                        sqlQuery += " AND (',' || d.VALUE || ',') LIKE ('%,' || :p_value_" + filterRequest.getProperty() + " || ',%')) ";
                                    }
                                    if (DataUtil.safeEqual(DataUtil.toUpper(filterRequest.getValueType()), "LONG")) {
                                        sqlQuery += "               AND to_number(d.VALUE) " + DataUtil.operatorToOracle(filterRequest) + " to_number(:p_value_" + filterRequest.getProperty() + "))";
                                    } else if(DataUtil.safeEqual(DataUtil.toUpper(filterRequest.getValueType()), "DATE")) {
                                        String datePattern = "dd/MM/yyyy";
                                        if (!DateUtil.isValidDateFormat(DataUtil.safeToString(filterRequest.getValueText()), "dd/MM/yyyy")) {
                                            throw new BusinessException("BCCS-POLICY-REASON-0002", "product.lb.productoffer.validateDate");
                                        } else {
                                            if (Boolean.TRUE.equals(filterRequest.getValueInRange())) {
                                                sqlQuery += " AND (d.VALUE_FROM is null or TRUNC(TO_DATE(d.VALUE_FROM, '" + datePattern + "')) <= TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "'))) " +
                                                        " AND (d.VALUE_TO is null or TRUNC(TO_DATE(d.VALUE_TO, '" + datePattern + "')) >= TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "'))))";
                                            } else {
                                                sqlQuery += " AND TRUNC(TO_DATE(d.VALUE, '" + datePattern + "')) " +
                                                        DataUtil.operatorToOracle(filterRequest) +
                                                        " TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "')))";
                                            }
                                        }
                                    }
//                                    sqlQuery += "               AND d.VALUE " + DataUtil.operatorToOracle(filterRequest) + " :p_value_" + filterRequest.getProperty() + ")";
                                    params.put("p_value_" + filterRequest.getProperty(), DataUtil.safeToString(filterRequest.getValueText()));
                                }else {
                                    if (DataUtil.safeEqual(DataUtil.toUpper(filterRequest.getValueType()), "LONG")) {
                                        sqlQuery += "               AND to_number(d.VALUE) " + DataUtil.operatorToOracle(filterRequest) + " to_number(:p_value_" + filterRequest.getProperty() + "))";
                                    } else if(DataUtil.safeEqual(DataUtil.toUpper(filterRequest.getValueType()), "DATE")) {
                                        String datePattern = "dd/MM/yyyy";
                                        if (!DateUtil.isValidDateFormat(DataUtil.safeToString(filterRequest.getValueText()), "dd/MM/yyyy")) {
                                            throw new BusinessException("BCCS-POLICY-REASON-0002", "product.lb.productoffer.validateDate");
                                        } else {
                                            if (Boolean.TRUE.equals(filterRequest.getValueInRange())) {
                                                sqlQuery += " AND (d.VALUE_FROM is null or TRUNC(TO_DATE(d.VALUE_FROM, '" + datePattern + "')) <= TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "'))) " +
                                                        " AND (d.VALUE_TO is null or TRUNC(TO_DATE(d.VALUE_TO, '" + datePattern + "')) >= TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "'))))";
                                            } else {
                                                sqlQuery += " AND TRUNC(TO_DATE(d.VALUE, '" + datePattern + "'))) " +
                                                        DataUtil.operatorToOracle(filterRequest) +
                                                        " TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "'))";
                                            }
                                        }
                                    } else {
                                        sqlQuery += "               AND d.VALUE " + DataUtil.operatorToOracle(filterRequest) + " :p_value_" + filterRequest.getProperty() + ")";
                                    }
                                    params.put("p_value_" + filterRequest.getProperty(), DataUtil.safeToString(filterRequest.getValueText()));
                                }
                            } else {
                                sqlQuery += ") ";
                            }
                        } else {
                            sqlQuery += "       AND (EXISTS " +
                                    "               (SELECT 1 " +
                                    "                  FROM bccs_product.reason_char_use b, " +
                                    "                       bccs_product.product_spec_char c, " +
                                    "                       bccs_product.product_spec_char_value d " +
                                    "                 WHERE     b.product_spec_char_id = c.product_spec_char_id " +
                                    "                       AND b.reason_id = rs.reason_id " +
                                    "                       AND b.status = 1 " +
                                    "                       AND c.status = 1 " +
                                    "                       AND d.status = 1 " +
                                    "                       AND d.product_spec_char_value_id = " +
                                    "                               b.product_spec_char_value_id " +
                                    "                       AND c.code =  " + ":p_" + filterRequest.getProperty();
                            params.put("p_" + filterRequest.getProperty(), filterRequest.getProperty());
                            if (!DataUtil.isNullOrEmpty(filterRequest.getValueText())) {
                                if (filterRequest.getProperty().equals(Const.ProductSpecChar.SINGLE_OR_COMBO)) {
                                    if ("1".equals(filterRequest.getValueText())) {
                                        sqlQuery += " AND d.VALUE IN ('1','3') )";
                                    } else {
                                        sqlQuery += " AND d.VALUE IN ('2','3') )";
                                    }
                                } else if (filterRequest.getProperty().equals(Const.ProductSpecChar.ARPU_SINGLE)) {
                                    if (!DataUtil.isNullOrEmpty(productCode)) {
                                        sqlQuery += "               AND (upper(b.specific_value) = upper(:productCode) OR b.specific_value is null) ";
                                        params.put("productCode", productCode);
                                    }
                                    if (DataUtil.safeEqual(DataUtil.toUpper(filterRequest.getValueType()), "LONG")) {
                                        sqlQuery += "               AND to_number(d.VALUE) " + DataUtil.operatorToOracle(filterRequest) + " to_number(:p_value_" + filterRequest.getProperty() + "))";
                                    } else if(DataUtil.safeEqual(DataUtil.toUpper(filterRequest.getValueType()), "DATE")) {
                                        String datePattern = "dd/MM/yyyy";
                                        if (!DateUtil.isValidDateFormat(DataUtil.safeToString(filterRequest.getValueText()), "dd/MM/yyyy")) {
                                            throw new BusinessException("BCCS-POLICY-REASON-0002", "product.lb.productoffer.validateDate");
                                        } else {
                                            if (Boolean.TRUE.equals(filterRequest.getValueInRange())) {
                                                sqlQuery += " AND (d.VALUE_FROM is null or TRUNC(TO_DATE(d.VALUE_FROM, '" + datePattern + "')) <= TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "'))) " +
                                                        " AND (d.VALUE_TO is null or TRUNC(TO_DATE(d.VALUE_TO, '" + datePattern + "')) >= TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "'))))";
                                            } else {
                                                sqlQuery += " AND TRUNC(TO_DATE(d.VALUE, '" + datePattern + "')) " +
                                                        DataUtil.operatorToOracle(filterRequest) +
                                                        " TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "')))";
                                            }
                                        }
                                    } else {
                                        sqlQuery += "               AND d.VALUE " + DataUtil.operatorToOracle(filterRequest) + " :p_value_" + filterRequest.getProperty() + ")";
                                    }
                                    params.put("p_value_" + filterRequest.getProperty(), DataUtil.safeToString(filterRequest.getValueText()));
                                }else if (filterRequest.getProperty().equals(Const.ProductSpecChar.FTTH_PRODUCT_CODE) || filterRequest.getProperty().equals(Const.ProductSpecChar.FTTH_OLD_CODE)) {
                                    if (!DataUtil.isNullOrEmpty(filterRequest.getProperty())) {
                                        sqlQuery += " AND (',' || d.VALUE || ',') LIKE ('%,' || :p_value_" + filterRequest.getProperty() + " || ',%')) ";
                                    }
                                    if (DataUtil.safeEqual(DataUtil.toUpper(filterRequest.getValueType()), "LONG")) {
                                        sqlQuery += "               AND to_number(d.VALUE) " + DataUtil.operatorToOracle(filterRequest) + " to_number(:p_value_" + filterRequest.getProperty() + "))";
                                    } else if(DataUtil.safeEqual(DataUtil.toUpper(filterRequest.getValueType()), "DATE")) {
                                        String datePattern = "dd/MM/yyyy";
                                        if (!DateUtil.isValidDateFormat(DataUtil.safeToString(filterRequest.getValueText()), "dd/MM/yyyy")) {
                                            throw new BusinessException("BCCS-POLICY-REASON-0002", "product.lb.productoffer.validateDate");
                                        } else {
                                            if (Boolean.TRUE.equals(filterRequest.getValueInRange())) {
                                                sqlQuery += " AND (d.VALUE_FROM is null or TRUNC(TO_DATE(d.VALUE_FROM, '" + datePattern + "')) <= TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "'))) " +
                                                        " AND (d.VALUE_TO is null or TRUNC(TO_DATE(d.VALUE_TO, '" + datePattern + "')) >= TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "'))))";
                                            } else {
                                                sqlQuery += " AND TRUNC(TO_DATE(d.VALUE, '" + datePattern + "')) " +
                                                        DataUtil.operatorToOracle(filterRequest) +
                                                        " TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "')))";
                                            }
                                        }
                                    }
                                    params.put("p_value_" + filterRequest.getProperty(), DataUtil.safeToString(filterRequest.getValueText()));
                                } else {
                                    if (DataUtil.safeEqual(DataUtil.toUpper(filterRequest.getValueType()), "LONG")) {
                                        // neu truyen valueInRange = true thi check theo khoang gia tri
                                        if(Boolean.TRUE.equals(filterRequest.getValueInRange())){
                                            sqlQuery += " AND (d.VALUE_FROM is null or to_number(d.VALUE_FROM) <= to_number(:p_value_" + filterRequest.getProperty() +  ")) AND (d.VALUE_TO is null or to_number(d.VALUE_TO) >= to_number(:p_value_" + filterRequest.getProperty() +  ")))";
                                        }else{
                                            sqlQuery += "               AND to_number(d.VALUE) " + DataUtil.operatorToOracle(filterRequest) + " to_number(:p_value_" + filterRequest.getProperty() + "))";
                                        }
                                    } else if(DataUtil.safeEqual(DataUtil.toUpper(filterRequest.getValueType()), "DATE")) {
                                        String datePattern = "dd/MM/yyyy";
                                        if (!DateUtil.isValidDateFormat(DataUtil.safeToString(filterRequest.getValueText()), "dd/MM/yyyy")) {
                                            throw new BusinessException("BCCS-POLICY-REASON-0002", "product.lb.productoffer.validateDate");
                                        } else {
                                            if (Boolean.TRUE.equals(filterRequest.getValueInRange())) {
                                                sqlQuery += " AND (d.VALUE_FROM is null or TRUNC(TO_DATE(d.VALUE_FROM, '" + datePattern + "')) <= TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "'))) " +
                                                        " AND (d.VALUE_TO is null or TRUNC(TO_DATE(d.VALUE_TO, '" + datePattern + "')) >= TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "'))))";
                                            } else {
                                                sqlQuery += " AND TRUNC(TO_DATE(d.VALUE, '" + datePattern + "')) " +
                                                        DataUtil.operatorToOracle(filterRequest) +
                                                        " TRUNC(TO_DATE(:p_value_" + filterRequest.getProperty() + ", '" + datePattern + "')))";
                                            }
                                        }
                                    } else {
                                        sqlQuery += "               AND d.VALUE " + DataUtil.operatorToOracle(filterRequest) + " :p_value_" + filterRequest.getProperty() + ")";
                                    }
                                    params.put("p_value_" + filterRequest.getProperty(), DataUtil.safeToString(filterRequest.getValueText()));
                                }
                            } else {
                                if (!DataUtil.isNullOrEmpty(filterRequest.getLstValue())) {
                                    if (DataUtil.safeEqual(FilterRequest.Operator.LIKE, filterRequest.getOperator())) {
                                        sqlQuery += " AND ( 1 = 0 ";
                                        for (int i = 0; i < filterRequest.getLstValue().size(); i++) {
                                            sqlQuery += "   OR  ';' || d.VALUE || ';' " + DataUtil.operatorToOracle(filterRequest) + " :p_value_" + filterRequest.getProperty() + i + " ";
                                            params.put("p_value_" + filterRequest.getProperty() + i, "%;" + filterRequest.getLstValue().get(i) + ";%");
                                        }
                                        sqlQuery += " ) )  ";
                                    } else {
                                        sqlQuery += "               AND d.VALUE " + DataUtil.operatorToOracle(filterRequest) + " ( :p_value_" + filterRequest.getProperty() + " ) )";
                                        params.put("p_value_" + filterRequest.getProperty(), filterRequest.getLstValue());
                                    }


                                } else {
                                    sqlQuery += ") ";
                                }

                            }
                            sqlQuery += "       OR NOT EXISTS " +
                                    "               (SELECT 1 " +
                                    "                  FROM bccs_product.reason_char_use b, " +
                                    "                       bccs_product.product_spec_char c " +
                                    "                 WHERE     b.product_spec_char_id = c.product_spec_char_id " +
                                    "                       AND b.reason_id = rs.reason_id " +
                                    "                       AND b.status = 1 " +
                                    "                       AND c.status = 1 " +
                                    "                       AND c.code =  " + ":p_" + filterRequest.getProperty() + "))";
                            params.put("p_" + filterRequest.getProperty(), filterRequest.getProperty());
                        }
                    }
                }
            }
            Query query = em.createNativeQuery(sqlQuery, ReasonEntity.class);
            query.setParameter("reasonId", lstReasonId);
            params.forEach(query::setParameter);
            @SuppressWarnings("unchecked")
            List<ReasonEntity> resultEntities = query.getResultList();
            return mapper.toDTO(resultEntities);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }

    }

    @Override
    public Long findReasonIdByCodeActionAndTelService(String reasonCode, String actionCode, Long telServiceId) {
        String sqlQuery = "SELECT a.* FROM " + Const.DEFAULT_PRODUCT_SCHEMA + "reason a " +
                " WHERE a.reason_type IN (SELECT reason_type FROM " + Const.DEFAULT_PRODUCT_SCHEMA + "action WHERE status = '1' AND action_code = :actionCode) " +
                "   AND a.status = '1' " +
                "   AND (a.effect_datetime IS NULL OR a.effect_datetime < TRUNC(sysdate) + 1) " +
                "   AND (a.expire_datetime IS NULL OR a.expire_datetime >= TRUNC(sysdate)) " +
                "   AND a.reason_code = :reasonCode " +
                "   AND (','||a.tel_service||',' LIKE :telService OR a.tel_service IS NULL) " +
                " ORDER BY a.name ASC";

        Query query = em.createNativeQuery(sqlQuery, ReasonEntity.class);
        query.setParameter("actionCode", actionCode);
        query.setParameter("reasonCode", reasonCode);
        query.setParameter("telService", "%," + telServiceId + ",%");
        query.setMaxResults(1);

        @SuppressWarnings("unchecked")
        List<ReasonEntity> result = query.getResultList();
        return result.isEmpty() ? null : result.get(0).getReasonId();
    }

}