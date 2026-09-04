package com.viettel.bccs.productcatalog.client;

import java.util.List;

import com.viettel.bccs.productcatalog.client.dto.FreeCamEquipmentDTO;

/**
 * Client gọi sang product-policy-service để truy vấn thông tin thiết bị CAM miễn phí.
 */
public interface FreeCamEquipmentClient {

    /**
     * Kiểm tra và lấy danh sách thiết bị CAM miễn phí theo productPackageId.
     * Gọi sang product-policy-service: GET
       * /product-policy-service/v1/free-cam-equipment/checkReasonFreeCam/{productPackageId}
     *
     * @param productPackageId id gói sản phẩm
     * @return danh sách FreeCamEquipmentDTO, hoặc null nếu không có kết quả
     */
    List<FreeCamEquipmentDTO> checkReasonFreeCam(Long productPackageId);
}