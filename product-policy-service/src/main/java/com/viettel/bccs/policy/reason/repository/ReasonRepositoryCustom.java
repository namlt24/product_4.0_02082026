package com.viettel.bccs.policy.reason.repository;

import com.viettel.bccs.policy.common.dto.FilterRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
import com.viettel.bccs.policy.reason.dto.response.ReasonDTO;
import com.viettel.bccs.policy.reason.entity.ReasonEntity;

import java.util.List;

public interface ReasonRepositoryCustom {

    List<ReasonEntity> getListReasonByActionCodeAndTelServiceForAuditWithMappingChecking(
            String actionCode, Long telServiceId, String payType, Long numProduct, boolean checkStatus,
            List<String> excludeProdOfferTypeIds);

    List<ReasonEntity> getByActionCodeOrderByIdWithMappingChecking(
            String actionCode, Long telServiceId, Long numProduct, String productOfferType,
            List<String> excludeProdOfferTypeIds);

    List<ReasonDTO> findByLstIdWithSpec(List<Long> lstReasonId, List<FilterRequest> listProductSpec, String productCode);

    /**
     * Migrate từ mono: ExternalServiceForMbccs.getListStockTypeWS bước tìm reasonId theo
     * reasonCode (regType) + actionCode (qua reason_type của ACTION) + telServiceId.
     * Trả về reasonId của bản ghi REASON đầu tiên (sắp theo name ASC) còn hiệu lực, hoặc
     * {@code null} nếu không tìm thấy.
     */
    Long findReasonIdByCodeActionAndTelService(String reasonCode, String actionCode, Long telServiceId);
}