package com.viettel.bccs.policy.reason.repository;

import com.viettel.bccs.policy.reason.entity.ReasonEntity;

import java.util.List;

public interface ReasonRepositoryCustom {

    List<ReasonEntity> getListReasonByActionCodeAndTelServiceForAuditWithMappingChecking(
            String actionCode, Long telServiceId, String payType, Integer numProduct, boolean checkStatus);
}