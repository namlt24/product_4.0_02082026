package com.viettel.bccs.productcatalog.client;

/**
 * Client gọi sang product-policy-service để truy vấn thông tin hình thức hòa mạng (reason).
 */
public interface ReasonClient {

    /**
     * Tìm reasonId theo mã lý do (reasonCode), mã hành động (actionCode) và dịch vụ viễn thông.
     * Gọi sang product-policy-service: GET /product-policy-service/v1/reason/getReasonIdByTypeAndCode
     *
     * @param reasonCode       mã lý do (REASON_CODE)
     * @param actionCode       mã hành động (ACTION_CODE)
     * @param telecomServiceId id dịch vụ viễn thông
     * @return reasonId, hoặc null nếu không tìm thấy
     */
    Long getReasonIdByTypeAndCode(String reasonCode, String actionCode, Long telecomServiceId);
}
