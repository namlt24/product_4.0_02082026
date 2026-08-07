package com.viettel.bccs.policy.discountpromotioncharuse.mapper;

import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.entity.MapActiveInfoEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MapActiveInfoMapper {

    public List<MapActiveInfoDTO> toDtoBean(List<MapActiveInfoEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(this::toDtoBean).collect(Collectors.toList());
    }

    public MapActiveInfoDTO toDtoBean(MapActiveInfoEntity entity) {
        if (entity == null) {
            return null;
        }
        return MapActiveInfoDTO.builder()
                .id(entity.getId())
                .telServiceId(entity.getTelServiceId())
                .productCode(entity.getProductCode())
                .productName(entity.getProductName())
                .regReasonId(entity.getRegReasonId())
                .reasonName(entity.getReasonName())
                .promCode(entity.getPromCode())
                .promName(entity.getPromName())
                .channelTypeId(entity.getChannelTypeId())
                .channelName(entity.getChannelName())
                .provinceCode(entity.getProvinceCode())
                .districtCode(entity.getDistrictCode())
                .effectDate(entity.getEffectDate())
                .endDate(entity.getEndDate())
                .provinceName(entity.getProvinceName())
                .districtName(entity.getDistrictName())
                .offerId(entity.getOfferId())
                .offerName(entity.getOfferName())
                .status(entity.getStatus() != null ? String.valueOf(entity.getStatus()) : null)
                .precinctName(entity.getPrecinctName())
                .precinctCode(entity.getPrecinctCode())
                .shopCode(entity.getShopCode())
                .staffCode(entity.getStaffCode())
                .actionCode(entity.getActionCode())
                .actionName(entity.getActionName())
                .limitNumber(entity.getLimitNumber())
                .captcharRequire(entity.getCaptcharRequire() != null ? entity.getCaptcharRequire().shortValue() : null)
                .unit(entity.getUnit())
                .customerGroup(entity.getCustomerGroup())
                .customerType(entity.getCustomerType())
                .subType(entity.getSubType())
                .subGroup(entity.getSubGroup())
                .policyDoc(entity.getPolicyDoc())
                .actionGroup(entity.getActionGroup())
                .actionGroupName(entity.getActionGroupName())
                .fileName(entity.getFileName())
                .stationId(entity.getStationId())
                .shopId(entity.getShopId())
                .createUser(entity.getCreateUser())
                .issueDatetime(entity.getIssueDatetime())
                .stationCodes(entity.getStationCodes())
                .payType(entity.getPayType())
                .technology(entity.getTechnology())
                .updateDatetime(entity.getUpdateDatetime())
                .updateUser(entity.getUpdateUser())
                .areaGroupCode(entity.getAreaGroupCode())
                .vasCode(entity.getVasCode())
                .vasName(entity.getVasName())
                .nodeCode(entity.getNodeCode())
                .note(entity.getNote())
                .groupNodeCode(entity.getGroupNodeCode())
                .staffType(entity.getStaffType())
                .businessNo(entity.getBusinessNo())
                .subGroupCode(entity.getSubGroupCode())
                .smeCustomerGroup(entity.getSmeCustomerGroup())
                .importOfflineId(entity.getImportOfflineId())
                .build();
    }

    public MapActiveInfoResponse toResponse(MapActiveInfoEntity entity) {
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
                entity.getStaffType(),
                entity.getBusinessNo(),
                entity.getSubGroupCode(),
                entity.getSmeCustomerGroup(),
                entity.getImportOfflineId(),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}