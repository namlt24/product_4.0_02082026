package com.viettel.bccs.area.area.mapper;

import com.viettel.bccs.area.area.dto.response.AreaResponse;
import com.viettel.bccs.area.area.entity.AreaEntity;
import org.springframework.stereotype.Component;

@Component
public class AreaMapper {

    public AreaResponse toResponse(AreaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AreaResponse(
            entity.getAreaCode(),
            entity.getParentCode(),
            entity.getAreaGroup(),
            entity.getProvince(),
            entity.getDistrict(),
            entity.getPrecinct(),
            entity.getStreetBlock(),
            entity.getName(),
            entity.getFullName(),
            entity.getCenter(),
            entity.getPstnCode(),
            entity.getProvinceCode(),
            entity.getStatus(),
            entity.getCreateUser(),
            entity.getCreateDatetime(),
            entity.getUpdateUser(),
            entity.getUpdateDatetime(),
            entity.getRegionId(),
            entity.getVtMapCode(),
            entity.getSquare(),
            entity.getPopulation(),
            entity.getHouseholds(),
            entity.getAreaType(),
            entity.getVnCode(),
            entity.getVnName(),
            entity.getIsNew()
        );
    }
}