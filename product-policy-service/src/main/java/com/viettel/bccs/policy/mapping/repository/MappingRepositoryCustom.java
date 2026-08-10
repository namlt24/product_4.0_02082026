package com.viettel.bccs.policy.mapping.repository;

import com.viettel.bccs.policy.reason.entity.ReasonEntity;

import java.util.List;

public interface MappingRepositoryCustom {

    List<String> findSaleServiceCodeByReason(Long reasonId);

    /**
     * Migrate từ mono: MappingRepoImpl.getMappingReasonProductOfferPrice(Long productPackageId).
     * SQL gốc: Select c.* From mapping a, reason c Where a.reason_id = c.reason_id
     * And a.status = 1 and c.status = 1 And (a.end_effect_date is null or a.end_effect_date >= trunc(sysdate))
     * And a.sale_service_id = :productPackageId
     */
    List<ReasonEntity> getMappingReasonProductOfferPrice(Long productPackageId);
}