package com.viettel.bccs.policy.mapactiveinfo.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
import com.viettel.bccs.policy.mapactiveinfo.entity.MapActiveInfoEntity;
import com.viettel.bccs.policy.mapactiveinfotgdd.entity.MapActiveInfoTgddEntity;
import com.viettel.bccs.policy.utils.Const;
import com.viettel.bccs.policy.utils.DataUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
public class MapActiveInfoRepositoryCustomImpl implements MapActiveInfoRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    // entityManager.createNativeQuery(sql, Class) tra ve Query tho theo dung dac ta JPA (khong co
    // TypedQuery cho native query) - query.getResultList() vi vay tra ve List tho, khong the tranh
    // unchecked conversion khi return ve List<MapActiveInfoEntity>.
    @Override
    @SuppressWarnings("unchecked")
    public List<MapActiveInfoEntity> findByExample(MapActiveInfoDTO exampleMapActiveInfo, boolean searchInvidualField, boolean isTgdd) throws Exception {
        List<Object> param = new ArrayList<>();
        String sql = buildQuery(exampleMapActiveInfo, param, searchInvidualField, isTgdd);
        Query query;
        if (isTgdd) {
            query = em.createNativeQuery(sql, MapActiveInfoTgddEntity.class);
        } else {
            query = em.createNativeQuery(sql, MapActiveInfoEntity.class);
        }
        for (int i = 0; i < param.size(); i++) {
            query.setParameter(i + 1, param.get(i));
        }
        return query.getResultList();
    }

    private String buildQuery(MapActiveInfoDTO dto, List<Object> param, boolean searchInvidualField, boolean isTgdd) {
        StringBuilder query = new StringBuilder();
        if (isTgdd) {
            query.append(" SELECT a.* FROM ").append(Const.DEFAULT_PRODUCT_SCHEMA).append("MAP_ACTIVE_INFO_TGDD a ");
        } else {
            query.append(" SELECT a.* FROM ").append(Const.DEFAULT_PRODUCT_SCHEMA).append("MAP_ACTIVE_INFO a ");
        }
        query.append(" WHERE STATUS != 0 and (sysdate > trunc(EFFECT_DATE)) AND (END_DATE is null or END_DATE >= trunc(sysdate)) ");

        if (dto.getFromDate() != null) {
            query.append(" AND a.issue_datetime >= trunc(?) ");
            param.add(dto.getFromDate());
        }
        if (dto.getToDate() != null) {
            query.append(" AND a.update_datetime <= trunc(? + 1) ");
            param.add(dto.getToDate());
        }

        if (!DataUtil.isNullOrZero(dto.getId()) && !Long.valueOf(Const.DEFAULT_VALUE_MAP_SELECT_ALL).equals(dto.getId())) {
            query.append(buildColumnCondition("ID", dto.getId(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getPayType()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getPayType())) {
            query.append(buildColumnCondition("PAY_TYPE", dto.getPayType(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getActionCode()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getActionCode())) {
            query.append(buildColumnCondition("ACTION_CODE", dto.getActionCode(), param));
        }
        if (!DataUtil.isNullOrZero(dto.getTelServiceId()) && !Long.valueOf(Const.DEFAULT_VALUE_MAP_SELECT_ALL).equals(dto.getTelServiceId())) {
            query.append(buildColumnCondition("TEL_SERVICE_ID", dto.getTelServiceId(), param));
        }
        if (!DataUtil.isNullOrZero(dto.getChannelTypeId()) && !Long.valueOf(Const.DEFAULT_VALUE_MAP_SELECT_ALL).equals(dto.getChannelTypeId())) {
            query.append(buildColumnCondition("CHANNEL_TYPE_ID", dto.getChannelTypeId(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getProvinceCode()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getProvinceCode())) {
            query.append(buildColumnCondition("PROVINCE_CODE", dto.getProvinceCode(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getDistrictCode()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getDistrictCode())) {
            query.append(buildColumnCondition("DISTRICT_CODE", dto.getDistrictCode(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getPrecinctCode()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getPrecinctCode())) {
            query.append(buildColumnCondition("PRECINCT_CODE", dto.getPrecinctCode(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getShopCode()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getShopCode())) {
            query.append(buildColumnCondition("SHOP_CODE", dto.getShopCode(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getStaffCode()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getStaffCode())) {
            query.append(buildColumnCondition("STAFF_CODE", dto.getStaffCode(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getCustomerGroup()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getCustomerGroup())) {
            query.append(buildColumnCondition("CUSTOMER_GROUP", dto.getCustomerGroup(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getCustomerType()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getCustomerType())) {
            query.append(buildColumnCondition("CUSTOMER_TYPE", dto.getCustomerType(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getSubGroup()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getSubGroup())) {
            query.append(buildColumnCondition("SUB_GROUP", dto.getSubGroup(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getSubType()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getSubType())) {
            query.append(buildColumnCondition("SUB_TYPE", dto.getSubType(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getStationCodes()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getStationCodes())) {
            query.append(buildColumnCondition("STATION_CODES", dto.getStationCodes(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getTechnology()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getTechnology())) {
            query.append(buildColumnCondition("TECHNOLOGY", dto.getTechnology(), param));
        }
        if (!DataUtil.isNullOrEmpty(dto.getNodeCode()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getNodeCode())) {
            query.append(" AND (upper(a.node_code) LIKE upper(?) or (a.node_code is null) or ((a.node_code = '-1' or a.node_code = '-1;') and a.group_node_code is null) ");
            query.append(" or exists(select 1 from BCCS_PRODUCT.group_node gn, BCCS_PRODUCT.MAP_GROUP_NODE mgn, BCCS_PRODUCT.node n ");
            query.append(" where 1 = 1 and gn.status = 1 and mgn.status = 1 and n.STATUS = 1 ");
            query.append(" and n.NODE_ID = mgn.NODE_ID and gn.GROUP_NODE_ID = mgn.GROUP_NODE_ID ");
            query.append(" and upper(n.NODE_CODE) LIKE upper(?) and a.group_node_code = gn.CODE))");
            param.add("%" + dto.getNodeCode() + "%");
            param.add("%" + dto.getNodeCode() + "%");
        }

        if (searchInvidualField) {
            if (!DataUtil.isNullOrEmpty(dto.getProductCode()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getProductCode())) {
                query.append(buildColumnCondition("PRODUCT_CODE", dto.getProductCode(), param));
            }
            if (!DataUtil.isNullOrZero(dto.getRegReasonId()) && !Long.valueOf(Const.DEFAULT_VALUE_MAP_SELECT_ALL).equals(dto.getRegReasonId())) {
                query.append(buildColumnCondition("REG_REASON_ID", dto.getRegReasonId(), param));
            }
            if (!DataUtil.isNullOrEmpty(dto.getPromCode()) && !Const.DEFAULT_VALUE_MAP_SELECT_ALL.equals(dto.getPromCode())) {
                query.append(buildColumnCondition("PROM_CODE", dto.getPromCode(), param));
            }
        }

        query.append(" order by a.reg_reason_id asc ");
        return query.toString();
    }

    private String buildColumnCondition(String columnName, Object value, List<Object> param) {
        String temp;
        if ("STATION_CODES".equals(columnName)) {
            temp = " AND (" + columnName + " || ';' like ? OR " + columnName + "= ? OR " + columnName + " is null ) ";
            param.add("%" + value + ";%");
            param.add(Const.DEFAULT_VALUE_MAP_SELECT_ALL);
        } else {
            temp = " AND (" + columnName + "=? OR " + columnName + "= ? OR " + columnName + " is null ) ";
            param.add(value);
            param.add(Const.DEFAULT_VALUE_MAP_SELECT_ALL);
        }
        return temp;
    }

    @Override
    public List<MapActiveInfoEntity> getListMapActiveInfoByNode(String nodeCode, List<Long> mapActiveInfoId) throws Exception {
        if (mapActiveInfoId == null || mapActiveInfoId.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM BCCS_PRODUCT.MAP_ACTIVE_INFO a WHERE 1=1 ");

        if (!DataUtil.isNullOrEmpty(nodeCode)) {
            sql.append("and (upper(a.node_code) LIKE upper(:node_code) or (a.node_code is null) or ((a.node_code = '-1' or a.node_code = '-1;') and a.group_node_code is null) ");
            sql.append("or exists(select 1 from BCCS_PRODUCT.group_node gn, BCCS_PRODUCT.MAP_GROUP_NODE mgn, BCCS_PRODUCT.node n ");
            sql.append("where gn.status = 1 and mgn.status = 1 and n.STATUS = 1 ");
            sql.append("and n.NODE_ID = mgn.NODE_ID and gn.GROUP_NODE_ID = mgn.GROUP_NODE_ID ");
            sql.append("and upper(n.NODE_CODE) LIKE upper(:node_code) and a.group_node_code = gn.CODE)) ");
        }

        sql.append(" AND a.STATUS != 0 AND (sysdate > trunc(a.EFFECT_DATE)) AND (a.END_DATE is null or a.END_DATE >= trunc(sysdate)) ");
        sql.append("AND a.ID IN (:list_id) ORDER BY a.reg_reason_id ASC");

        Query query = em.createNativeQuery(sql.toString(), MapActiveInfoEntity.class);

        if (!DataUtil.isNullOrEmpty(nodeCode)) {
            query.setParameter("node_code", "%" + nodeCode + "%");
        }
        query.setParameter("list_id", mapActiveInfoId);

        @SuppressWarnings("unchecked")
        List<MapActiveInfoEntity> results = query.getResultList();
        return results;
    }
}