package com.viettel.bccs.policy.mapactiveinfotgdd.mapper;

import org.springframework.stereotype.Component;

import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfotgdd.entity.MapActiveInfoTgddEntity;

@Component
public class MapActiveInfoTgddMapper {

    public MapActiveInfoResponse toResponse(MapActiveInfoTgddEntity entity) {
        if (entity == null) {
            return null;
        }
        return new MapActiveInfoResponse(
                entity.getId(),
                entity.getTelServiceId(),
                entity.getProductCode(),
                entity.getProductName(),
                entity.getRegReasonId(),
                entity.getReasonName(),
                entity.getPromCode(),
                entity.getPromName(),
                entity.getChannelTypeId(),
                entity.getChannelName(),
                entity.getProvinceCode(),
                entity.getDistrictCode(),
                entity.getEffectDate(),
                entity.getEndDate(),
                entity.getProvinceName(),
                entity.getDistrictName(),
                entity.getOfferId(),
                entity.getOfferName(),
                entity.getStatus(),
                entity.getPrecinctName(),
                entity.getPrecinctCode(),
                entity.getShopCode(),
                entity.getStaffCode(),
                entity.getActionCode(),
                entity.getActionName(),
                entity.getLimitNumber(),
                entity.getCaptcharRequire(),
                entity.getUnit(),
                entity.getCustomerGroup(),
                entity.getCustomerType(),
                entity.getSubType(),
                entity.getSubGroup(),
                entity.getPolicyDoc(),
                entity.getActionGroup(),
                entity.getActionGroupName(),
                entity.getFileName(),
                entity.getStationId(),
                entity.getShopId(),
                entity.getCreateUser(),
                entity.getIssueDatetime(),
                entity.getStationCodes(),
                entity.getPayType(),
                entity.getTechnology(),
                entity.getUpdateDatetime(),
                entity.getUpdateUser(),
                entity.getAreaGroupCode(),
                entity.getVasCode(),
                entity.getVasName(),
                entity.getNodeCode(),
                entity.getNote(),
                entity.getGroupNodeCode(),
                null,
                null,
                null,
                null,
                entity.getImportOfflineId(),
                entity.getConnectMethod(),
                entity.getAttachTelServiceId(),
                entity.getAttachProductCode(),
                entity.getSingleOrCombo(),
                entity.getOldProductCode(),
                entity.getProjectCode()
        );
    }
}