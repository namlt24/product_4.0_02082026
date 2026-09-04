package com.viettel.bccs.policy.mapbusinessskipdebt.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.viettel.bccs.policy.mapbusinessskipdebt.dto.response.MapBusinessSkipDebtResponse;
import com.viettel.bccs.policy.mapbusinessskipdebt.dto.response.SkipDebtResultResponse;
import com.viettel.bccs.policy.mapbusinessskipdebt.entity.MapBusinessSkipDebtEntity;

@Component
public class MapBusinessSkipDebtMapper {

    public MapBusinessSkipDebtResponse toResponse(MapBusinessSkipDebtEntity entity) {
        if (entity == null) {
            return null;
        }
        return new MapBusinessSkipDebtResponse(
                entity.getMapId(),
                entity.getActionCode(),
                entity.getTelecomServiceId(),
                entity.getProductCode(),
                entity.getEffectDatetime(),
                entity.getExpireDatetime(),
                entity.getShopId(),
                entity.getStaffId(),
                entity.getBusinessNo(),
                entity.getContractNo(),
                entity.getStatus(),
                entity.getIbmCode(),
                entity.getApproveUser(),
                entity.getCreateUser(),
                entity.getUpdateUser(),
                entity.getCreateDatetime(),
                entity.getUpdateDatetime()
        );
    }

    public List<MapBusinessSkipDebtResponse> toResponseList(List<MapBusinessSkipDebtEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(this::toResponse).toList();
    }

    public SkipDebtResultResponse toSearchResult(MapBusinessSkipDebtEntity entity) {
        if (entity == null) {
            return null;
        }
        return new SkipDebtResultResponse(
                str(entity.getMapId()),
                str(entity.getActionCode()),
                str(entity.getTelecomServiceId()),
                str(entity.getProductCode()),
                str(entity.getEffectDatetime()),
                str(entity.getExpireDatetime()),
                str(entity.getShopId()),
                str(entity.getStaffId()),
                str(entity.getBusinessNo()),
                str(entity.getContractNo()),
                str(entity.getStatus()),
                str(entity.getIbmCode()),
                str(entity.getApproveUser()),
                str(entity.getCreateUser()),
                str(entity.getUpdateUser()),
                str(entity.getCreateDatetime()),
                str(entity.getUpdateDatetime())
        );
    }

    public List<SkipDebtResultResponse> toSearchResultList(List<MapBusinessSkipDebtEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(this::toSearchResult).toList();
    }

    private String str(Object val) {
        return val == null ? null : val.toString();
    }
}