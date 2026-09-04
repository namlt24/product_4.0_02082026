package com.viettel.bccs.policy.mapping.repository;

import java.util.List;
import java.util.Map;

import com.viettel.bccs.policy.reason.entity.ReasonEntity;

public interface MappingRepositoryCustom {

    List<String> findSaleServiceCodeByReason(Long reasonId);

    List<ReasonEntity> getMappingReasonProductOfferPrice(Long productPackageId);

    String getSaleServiceCode(Long telecomServiceId, Long reasonId, String productCode, String actionCode);

    Map<String, String> getLstMapPackageByActionCodeAndReasonCodes(List<String> reasonCodes, String actionCode);
}