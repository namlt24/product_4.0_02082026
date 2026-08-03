package com.viettel.bccs.policy.discountpromotion.repository;

import com.viettel.bccs.policy.discountpromotion.dto.response.DiscountPromotionDTO;
import com.viettel.bccs.policy.discountpromotion.entity.DiscountPromotionEntity;
import com.viettel.bccs.policy.discountpromotion.mapper.DiscountPromotionMapper;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
import com.viettel.bccs.policy.utils.Const;
import com.viettel.bccs.policy.utils.DataUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class DiscountPromotionRepositoryCustomImpl implements DiscountPromotionRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private DiscountPromotionMapper mapper;

    @Override
    public List<DiscountPromotionEntity> getPromotionList(
            Long telecomServiceId,
            boolean checkStatus,
            boolean checkEffectDate,
            LocalDateTime endDate) {

        StringBuilder queryString = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();

        if (telecomServiceId != null) {
            queryString.append("SELECT dp FROM DiscountPromotionEntity dp WHERE dp.type = '1' AND dp.systemType = '2' "
                    + "AND ',' || dp.telecomServiceId || ',' LIKE :telService ");
            parameters.put("telService", "%," + telecomServiceId + ",%");
        } else {
            queryString.append("SELECT dp FROM DiscountPromotionEntity dp WHERE dp.status = '1' "
                    + "AND dp.type = '1' AND dp.systemType = '2' ");
        }

        if (checkEffectDate) {
            queryString.append("AND (dp.effectDatetime IS NULL OR dp.effectDatetime <= CURRENT_DATE) ");
        }

        if (checkStatus) {
            queryString.append("AND dp.status = '1' ");
        }

        if (endDate != null && checkEffectDate) {
            queryString.append("AND (dp.expireDatetime IS NULL OR dp.expireDatetime >= :endDate) ");
            parameters.put("endDate", endDate);
        } else if (checkEffectDate) {
            queryString.append("AND (dp.expireDatetime IS NULL OR dp.expireDatetime >= CURRENT_DATE) ");
        }

        queryString.append("ORDER BY FUNCTION('NLSSORT', dp.code, 'NLS_SORT=vietnamese')");

        Query query = em.createQuery(queryString.toString(), DiscountPromotionEntity.class);

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        return query.getResultList();
    }

    @Override
    public List<DiscountPromotionDTO> getPromFromMapActiveInfosCheckDuplicate(List<MapActiveInfoDTO> mapActiveInfos, boolean getDulicateProm) {
        if (DataUtil.isNullOrEmpty(mapActiveInfos)) return null;
//        List<String> lstPromCode = mapActiveInfos.stream().map(MapActiveInfoDTO::getPromCode).collect(Collectors.toList());
        String telService = DataUtil.safeToString(mapActiveInfos.get(0).getByProperty("telServiceId"));

        String queryStr = "select a.* from Discount_Promotion a where a.status = 1 "
                + " and a.type ='1'  "//khuyen mai
                + " and a.system_Type ='2'  "//Dau noi
                + " and ( a.effect_Datetime <= trunc(sysdate + 1) ) "
                + " and ( a.expire_Datetime is null or a.expire_Datetime >=  trunc(sysdate) ) "
                + " and ',' || a.telecom_service_id || ',' LIKE '%,' || :telService || ',%'"
                + " order by a.code";
        Query query = em.createNativeQuery(queryStr.toString(), DiscountPromotionEntity.class);
        query.setParameter("telService", telService);
        @SuppressWarnings("unchecked")
        List<DiscountPromotionEntity> resultEntities = query.getResultList();
        List<DiscountPromotionDTO> lstPromotions = mapper.toDTOList(resultEntities);
        List<DiscountPromotionDTO> lstResult = new ArrayList<>();

        if (!DataUtil.isNullOrEmpty(lstPromotions)) {
            String promotionCode;
            String telecomServiceId;

            Long regReasonId;
            String reasonName;
            String productCode;

            boolean isContainMapAllAll = false;
            int counter = 0;

            Long reasonIdMapAll = null;
            String reasonNameMapAll = null;

            String subGroupCode = null;
            for (DiscountPromotionDTO discountPromotionDTO : lstPromotions) {
                if (isContainMapAllAll) {
                    for (DiscountPromotionDTO obj : lstPromotions) {
                        obj.setRegReasonId(reasonIdMapAll);
                        obj.setReasonName(reasonNameMapAll);
                        obj.setSubGroupCode(subGroupCode);
                    }
                    return lstPromotions;
                }

                for (MapActiveInfoDTO mapActiveInfo : mapActiveInfos) {
                    promotionCode = DataUtil.safeToString(mapActiveInfo.getByProperty("promCode"));
                    telecomServiceId = DataUtil.safeToString(mapActiveInfo.getByProperty("telServiceId"));
                    regReasonId = DataUtil.safeToLong(mapActiveInfo.getByProperty("regReasonId"));
                    reasonName = DataUtil.safeToString(mapActiveInfo.getByProperty("reasonName"));
                    productCode = DataUtil.safeToString(mapActiveInfo.getByProperty("productCode"));

                    if (DataUtil.safeEqual(promotionCode, Const.DEFAULT_VALUE_MAP_SELECT_ALL)
                            && DataUtil.safeEqual(telecomServiceId, Const.DEFAULT_VALUE_MAP_SELECT_ALL)) {

                        isContainMapAllAll = true;
                        subGroupCode = mapActiveInfo.getSubGroupCode();
                        reasonIdMapAll = regReasonId;
                        reasonNameMapAll = reasonName;
                        break;
                    } else if (("," + discountPromotionDTO.getTelecomServiceId() + ",").contains("," + telecomServiceId + ",")
                            && DataUtil.safeEqual(discountPromotionDTO.getCode(), promotionCode)) {

                        DiscountPromotionDTO cloneBean = discountPromotionDTO.toBuilder().build();
                        cloneBean.setProductCode(productCode);
                        cloneBean.setRegReasonId(regReasonId);
                        cloneBean.setReasonName(reasonName);
                        if(!DataUtil.isNullOrEmpty(mapActiveInfo.getSubGroupCode())){
                            cloneBean.setSubGroupCode(mapActiveInfo.getSubGroupCode());
                        }

                        lstResult.add(cloneBean);
                        if (!getDulicateProm) {
                            break;
                        }

                    } else if (DataUtil.safeEqual(promotionCode, Const.DEFAULT_VALUE_MAP_SELECT_ALL)
                            && ("," + discountPromotionDTO.getTelecomServiceId() + ",").contains("," + telecomServiceId + ",")) {

                        isContainMapAllAll = true;
                        subGroupCode = mapActiveInfo.getSubGroupCode();
                        reasonIdMapAll = regReasonId;
                        reasonNameMapAll = reasonName;
                        break;
                    } else if (DataUtil.safeEqual(telecomServiceId, Const.DEFAULT_VALUE_MAP_SELECT_ALL)
                            && DataUtil.safeEqual(discountPromotionDTO.getCode(), promotionCode)) {

                        DiscountPromotionDTO cloneBean = discountPromotionDTO.toBuilder().build();
                        cloneBean.setRegReasonId(regReasonId);
                        cloneBean.setReasonName(reasonName);
                        cloneBean.setProductCode(productCode);
                        if(!DataUtil.isNullOrEmpty(mapActiveInfo.getSubGroupCode())){
                            cloneBean.setSubGroupCode(mapActiveInfo.getSubGroupCode());
                        }
                        lstResult.add(cloneBean);
                        if (!getDulicateProm) {
                            break;
                        }
                    }
                }
            }
            log.info("CHECKPOINT_2");
        }
        return lstResult;
    }
}