package com.viettel.bccs.productcatalog.client;

import com.viettel.bccs.productcatalog.client.dto.SensorFeeRuleDTO;

import java.util.List;

/**
 * Client gọi sang organization-resource-service để truy vấn thông tin phí cảm biến.
 */
public interface SensorFreeClient {

    /**
     * Lấy danh sách quy tắc phí cảm biến theo productPackageId.
     * Gọi sang organization-resource-service: GET /organization-resource-service/v1/sensor-fee/checkReasonSensorFee/{productPackageId}
     *
     * @param productPackageId id gói sản phẩm
     * @return danh sách SensorFeeRuleDTO, hoặc null nếu không có kết quả
     */
    List<SensorFeeRuleDTO> checkReasonSensorFee(Long productPackageId);
}