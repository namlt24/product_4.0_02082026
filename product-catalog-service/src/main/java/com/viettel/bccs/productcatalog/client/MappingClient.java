package com.viettel.bccs.productcatalog.client;

import com.viettel.bccs.productcatalog.client.dto.ReasonDTO;

import java.util.List;

/**
 * Client gọi sang product-policy-service để truy vấn thông tin mapping dịch vụ bán hàng.
 */
public interface MappingClient {

    /**
     * Tìm danh sách mã dịch vụ bán hàng (SALE_SERVICE_CODE) theo lý do (REASON_ID).
     * Gọi sang product-policy-service: GET /product-policy-service/v1/mapping/findSaleServiceCodeByReason
     *
     * @param reasonId id lý do
     * @return danh sách mã dịch vụ bán hàng, hoặc null nếu không có kết quả
     */
    List<String> findSaleServiceCodeByReason(Long reasonId);

    /**
     * Lấy danh sách lý do theo productPackageId phục vụ quản lý cước PCCC.
     * Gọi sang product-policy-service: GET /product-policy-service/v1/mapping/getMappingReasonProductOfferPrice/{productPackageId}
     *
     * @param productPackageId id gói sản phẩm
     * @return danh sách ReasonDTO, hoặc null nếu không có kết quả
     */
    List<ReasonDTO> getMappingReasonProductOfferPrice(Long productPackageId);

    /**
     * Tìm mã dịch vụ bán hàng (SALE_SERVICE_CODE) theo dịch vụ viễn thông, lý do, mã gói cước,
     * mã hành động. Gọi sang product-policy-service:
     * GET /product-policy-service/v1/mapping/getSaleServiceCode
     *
     * @param telecomServiceId id dịch vụ viễn thông (có thể null)
     * @param reasonId         id lý do
     * @param productCode      mã gói cước (có thể null)
     * @param actionCode       mã hành động (có thể null)
     * @return mã dịch vụ bán hàng, hoặc null nếu không tìm thấy
     */
    String getSaleServiceCode(Long telecomServiceId, Long reasonId, String productCode, String actionCode);
}