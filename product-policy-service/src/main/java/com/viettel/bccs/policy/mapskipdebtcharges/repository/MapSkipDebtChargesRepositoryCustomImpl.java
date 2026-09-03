package com.viettel.bccs.policy.mapskipdebtcharges.repository;

import com.viettel.bccs.policy.mapskipdebtcharges.dto.response.MapSkipDebtChargesDTO;
import com.viettel.bccs.policy.mapskipdebtcharges.entity.MapSkipDebtChargesEntity;
import com.viettel.bccs.policy.utils.Const;
import com.viettel.bccs.policy.utils.DataUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class MapSkipDebtChargesRepositoryCustomImpl implements MapSkipDebtChargesRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<MapSkipDebtChargesEntity> findByExample(MapSkipDebtChargesDTO exampleMapActiveInfo) throws Exception {
        List<Object> param = new ArrayList<>();
        String sql = buildQuery(exampleMapActiveInfo, param);
        Query query = em.createNativeQuery(sql, MapSkipDebtChargesEntity.class);
        for (int i = 0; i < param.size(); i++) {
            query.setParameter(i + 1, param.get(i));
        }
        return query.getResultList();
    }
    private String buildQuery(MapSkipDebtChargesDTO dto, List<Object> param) {
        StringBuilder query = new StringBuilder("");
        query.append(" SELECT a.* FROM MAP_SKIP_DEBT_CHARGES a ");
        query.append(" WHERE STATUS != 0 and (sysdate > trunc(EFFECT_DATE)) AND(END_DATE is null or END_DATE>=trunc(sysdate)) ");

        if (!DataUtil.isNullOrZero(dto.getId()) && (!Long.valueOf(Const.DEFAULT_VALUE_MAP_SELECT_ALL).equals(dto.getId()))) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.ID.name(), dto.getId(), param));
        }

        if (!DataUtil.isNullOrEmpty(dto.getPayType()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getPayType())) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.PAY_TYPE.name(), dto.getPayType(), param));
        }

        if (!DataUtil.isNullOrEmpty(dto.getActionCode()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getActionCode())) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.ACTION_CODE.name(), dto.getActionCode(), param));
        }

        if (!DataUtil.isNullOrZero(dto.getTelServiceId()) && (!Long.valueOf(Const.DEFAULT_VALUE_MAP_SELECT_ALL).equals(dto.getTelServiceId()))) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.TEL_SERVICE_ID.name(), dto.getTelServiceId(), param));
        }

        if (!DataUtil.isNullOrZero(dto.getChannelTypeId()) && (!Long.valueOf(Const.DEFAULT_VALUE_MAP_SELECT_ALL).equals(dto.getChannelTypeId()))) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.CHANNEL_TYPE_ID.name(), dto.getChannelTypeId(), param));
        }

        if (!DataUtil.isNullOrEmpty(dto.getProvinceCode()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getProvinceCode())) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.PROVINCE_CODE.name(), dto.getProvinceCode(), param));
        }

        if (!DataUtil.isNullOrEmpty(dto.getDistrictCode()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getDistrictCode())) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.DISTRICT_CODE.name(), dto.getDistrictCode(), param));
        }

        if (!DataUtil.isNullOrEmpty(dto.getPrecinctCode()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getPrecinctCode())) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.PRECINCT_CODE.name(), dto.getPrecinctCode(), param));
        }

        if (!DataUtil.isNullOrEmpty(dto.getShopCode()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getShopCode())) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.SHOP_CODE.name(), dto.getShopCode(), param));
        }

        if (!DataUtil.isNullOrEmpty(dto.getStaffCode()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getStaffCode())) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.STAFF_CODE.name(), dto.getStaffCode(), param));
        }
        if (!DataUtil.isNullOrZero(dto.getCycle()) && !Long.valueOf(Const.DEFAULT_VALUE_MAP_SELECT_ALL).equals(dto.getCycle())) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.CYCLE.name(), dto.getCycle(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getSkipHotCharges()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getSkipHotCharges())) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.SKIP_HOT_CHARGES.name(), dto.getSkipHotCharges(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getProductCode()) && (!Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getProductCode()))) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.PRODUCT_CODE.name(), dto.getProductCode(), param));
        }
        if (!DataUtil.isNullOrZero(dto.getRegReasonId()) && !Long.valueOf(Const.DEFAULT_VALUE_MAP_SELECT_ALL).equals(dto.getRegReasonId())) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.REG_REASON_ID.name(), dto.getRegReasonId(), param));
        }
        if (!DataUtil.isNullOrZero(dto.getCustGroupId()) && !Long.valueOf(Const.DEFAULT_VALUE_MAP_SELECT_ALL).equals(dto.getCustGroupId().toString())) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.CUST_GROUP_ID.name(), dto.getCustGroupId(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getCustType()) && !Long.valueOf(Const.DEFAULT_VALUE_MAP_SELECT_ALL).equals(dto.getCustType())) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.CUST_TYPE.name(), dto.getCustType(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getCustIdNo()) && !Long.valueOf(Const.DEFAULT_VALUE_MAP_SELECT_ALL).equals(dto.getCustIdNo())) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.CUST_ID_NO.name(), dto.getCustIdNo(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getCustAccountNo()) && !Long.valueOf(Const.DEFAULT_VALUE_MAP_SELECT_ALL).equals(dto.getCustAccountNo())) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.CUST_ACCOUNT_NO.name(), dto.getCustAccountNo(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getCustCode()) && !Long.valueOf(Const.DEFAULT_VALUE_MAP_SELECT_ALL).equals(dto.getCustCode())) {
            query.append(buildColumnCondition(MapSkipDebtChargesDTO.COLUMNS.CUST_CODE.name(), dto.getCustCode(), param));
        }

        query.append(" order by a.reg_reason_id asc ");
        return query.toString();
    }


    private String buildColumnCondition(String columnName, Object value, List<Object> param) {
        String temp = " AND (" + columnName + "=? OR " + columnName + "= ? OR " + columnName + " is null ) ";
        param.add(value);
        param.add(Const.DEFAULT_VALUE_MAP_SELECT_ALL);
        return temp;
    }
}
