package com.viettel.bccs.policy.mapskipdebtcharges.mapper;

import com.viettel.bccs.policy.mapskipdebtcharges.dto.response.MapSkipDebtChargesDTO;
import com.viettel.bccs.policy.mapskipdebtcharges.entity.MapSkipDebtChargesEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MapSkipDebtChargesMapper {

    public MapSkipDebtChargesDTO toDTO(MapSkipDebtChargesEntity entity) {
        if (entity == null) {
            return null;
        }
        return MapSkipDebtChargesDTO.builder()
                .id(entity.getId())
                .telServiceId(entity.getTelServiceId())
                .productCode(entity.getProductCode())
                .productName(entity.getProductName())
                .regReasonId(entity.getRegReasonId())
                .reasonName(entity.getReasonName())
                .channelTypeId(entity.getChannelTypeId())
                .channelName(entity.getChannelName())
                .provinceCode(entity.getProvinceCode())
                .provinceName(entity.getProvinceName())
                .districtCode(entity.getDistrictCode())
                .districtName(entity.getDistrictName())
                .precinctCode(entity.getPrecinctCode())
                .precinctName(entity.getPrecinctName())
                .shopCode(entity.getShopCode())
                .staffCode(entity.getStaffCode())
                .actionCode(entity.getActionCode())
                .actionName(entity.getActionName())
                .status(entity.getStatus())
                .createUser(entity.getCreateUser())
                .createDatetime(entity.getCreateDatetime())
                .updateUser(entity.getUpdateUser())
                .updateDatetime(entity.getUpdateDatetime())
                .cycle(entity.getCycle())
                .skipHotCharges(entity.getSkipHotCharges())
                .payType(entity.getPayType())
                .effectDate(entity.getEffectDate())
                .endDate(entity.getEndDate())
                .offerId(entity.getOfferId())
                .custGroupId(entity.getCustGroupId())
                .custType(entity.getCustType())
                .custIdNo(entity.getCustIdNo())
                .custAccountNo(entity.getCustAccountNo())
                .custCode(entity.getCustCode())
                .actStatus(entity.getActStatus())
                .skipLastSub(entity.getSkipLastSub())
                .skipContract(entity.getSkipContract())
                .build();
    }

    public List<MapSkipDebtChargesDTO> toDTO(List<MapSkipDebtChargesEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        List<MapSkipDebtChargesDTO> result = new ArrayList<>(entities.size());
        for (MapSkipDebtChargesEntity entity : entities) {
            result.add(toDTO(entity));
        }
        return result;
    }
}
