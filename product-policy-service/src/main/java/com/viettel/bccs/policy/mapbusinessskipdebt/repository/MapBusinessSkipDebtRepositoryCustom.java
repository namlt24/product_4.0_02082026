package com.viettel.bccs.policy.mapbusinessskipdebt.repository;

import com.viettel.bccs.policy.mapbusinessskipdebt.entity.MapBusinessSkipDebtEntity;

import java.util.List;

public interface MapBusinessSkipDebtRepositoryCustom {

    List<MapBusinessSkipDebtEntity> findActiveByActionCodeAndTelecomServiceId(String actionCode, Long telecomServiceId);

    List<MapBusinessSkipDebtEntity> findActiveByShopId(Long shopId);

    List<MapBusinessSkipDebtEntity> findActiveByStaffId(Long staffId);

    List<MapBusinessSkipDebtEntity> searchForAPI(String actionCode, Long telecomServiceId,
                                                 String effectDatetime, String shopCode,
                                                 String staffCode, String businessNo, String contractNo);
}