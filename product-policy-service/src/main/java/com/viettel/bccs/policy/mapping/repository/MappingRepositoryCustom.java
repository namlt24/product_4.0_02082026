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

    /**
     * Migrate từ mono: ExternalServiceForMbccs.getListStockTypeWS bước tìm saleServiceCode.
     * SQL gốc (JPQL): Select m from Mapping m, Reason r, Action a where m.reasonId = r.reasonId
     * and a.reasonType = m.actionCode and r.status='1' and m.status='1' and a.status='1'
     * and a.actionCode = :actionCode (nếu actionCode không rỗng) and m.reasonId = :reasonId
     * and m.telServiceId = :telServiceId (nếu telServiceId != 0, ngược lại m.telServiceId is null)
     * and (m.productCode = :productCode or m.productCode is null) (nếu productCode không rỗng,
     * ngược lại chỉ m.productCode is null) order by m.productCode.
     * Cột MAPPING.ACTION_CODE thực chất lưu REASON_TYPE (đặt tên cột theo lịch sử, giữ nguyên).
     * Trả về saleServiceCode của bản ghi đầu tiên, hoặc {@code null} nếu không tìm thấy.
     */
    String getSaleServiceCode(Long telecomServiceId, Long reasonId, String productCode, String actionCode);
}