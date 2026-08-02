package com.viettel.bccs.productcatalog.client;

import com.viettel.bccs.productcatalog.client.dto.CustTypeDTO;

import java.util.Optional;

/**
 * Client gọi sang organization-resource-service để truy vấn thông tin loại khách hàng.
 */
public interface CustTypeClient {

    /**
     * Tìm loại khách hàng đang hiệu lực theo mã.
     * Gọi sang organization-resource-service: GET /organization-resource-service/v1/cust-type/findActiveByCustType/{custType}
     *
     * @param custType mã loại khách hàng
     * @param status   trạng thái (thường truyền "1" cho hiệu lực)
     * @return CustTypeDTO hoặc null nếu không tìm thấy
     */
    Optional<CustTypeDTO> findActiveByCustType(String custType, String status);
}