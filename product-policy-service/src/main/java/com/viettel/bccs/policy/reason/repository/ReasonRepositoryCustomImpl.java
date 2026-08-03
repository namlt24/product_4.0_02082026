package com.viettel.bccs.policy.reason.repository;

import com.viettel.bccs.policy.utils.DataUtil;
import com.viettel.bccs.policy.reason.entity.ReasonEntity;
import com.viettel.bccs.policy.utils.Const;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class ReasonRepositoryCustomImpl implements ReasonRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ReasonEntity> getListReasonByActionCodeAndTelServiceForAuditWithMappingChecking(
            String actionCode, Long telServiceId, String payType, Integer numProduct, boolean checkStatus) {
        StringBuilder strQuery = new StringBuilder("");
        if (Const.ACTION_CODE.SUB_CONNECTION.equals(actionCode)) {
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
                        "AND to_char(ppp.product_offer_type_id)  in (select value from option_set_value where option_set_id = (select option_set_id from option_set where code = :excludeProdOfferType and status  = '1' ) )) = :pNumber ");

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
                        "AND to_char(ppp.product_offer_type_id)  in (select value from option_set_value where option_set_id = (select option_set_id from option_set where code = :excludeProdOfferType and status  = '1' ) )) = :pNumber ");

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
        query.setParameter("status", Const.STATUS.ACTIVE);
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
            query.setParameter("excludeProdOfferType", Const.PRODUCT_PACKAGE.EXCLUDE_PROD_OFFER_TYPE_ID);

        }
        List<ReasonEntity> listResult = query.getResultList();
        return listResult;
    }

    @Override
    public List<ReasonEntity> getByActionCodeOrderByIdWithMappingChecking(String actionCode, Long telServiceId, Long numProduct, String productOfferType) {
        return null;
    }
}