package com.viettel.bccs.policy.mapbusinessskipdebt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài/precision cột thật của
 * MAP_BUSINESS_SKIP_DEBT (xem MapBusinessSkipDebtEntity) - tất cả trường được format thành String.
 */
@Schema(description = "Kết quả tra cứu bỏ qua công nợ kinh doanh - tất cả trường dạng String")
public record SkipDebtResultResponse(
        @Schema(description = "ID bản ghi ánh xạ")
        @Size(max = 10, message = "mapId tối đa 10 chữ số")
        @Pattern(regexp = "^[0-9]{0,10}$", message = "mapId chỉ gồm chữ số")
        String mapId,

        @Schema(description = "Mã hành động")
        @Size(max = 10, message = "actionCode tối đa 10 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,10}$", message = "actionCode chỉ gồm chữ và số")
        String actionCode,

        @Schema(description = "ID dịch vụ viễn thông")
        @Size(max = 10, message = "telecomServiceId tối đa 10 chữ số")
        @Pattern(regexp = "^[0-9]{0,10}$", message = "telecomServiceId chỉ gồm chữ số")
        String telecomServiceId,

        @Schema(description = "Mã sản phẩm")
        @Size(max = 200, message = "productCode tối đa 200 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,200}$", message = "productCode chỉ gồm chữ, số, '_' hoặc '-'")
        String productCode,

        @Schema(description = "Ngày hiệu lực (dd/MM/yyyy)")
        @Size(max = 10, message = "effectDateTime phải đúng định dạng dd/MM/yyyy")
        @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$|^$", message = "effectDateTime phải đúng định dạng dd/MM/yyyy")
        String effectDateTime,

        @Schema(description = "Ngày hết hiệu lực (dd/MM/yyyy)")
        @Size(max = 10, message = "expireDateTime phải đúng định dạng dd/MM/yyyy")
        @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$|^$", message = "expireDateTime phải đúng định dạng dd/MM/yyyy")
        String expireDateTime,

        @Schema(description = "ID cửa hàng")
        @Size(max = 10, message = "shopId tối đa 10 chữ số")
        @Pattern(regexp = "^[0-9]{0,10}$", message = "shopId chỉ gồm chữ số")
        String shopId,

        @Schema(description = "ID nhân viên")
        @Size(max = 10, message = "staffId tối đa 10 chữ số")
        @Pattern(regexp = "^[0-9]{0,10}$", message = "staffId chỉ gồm chữ số")
        String staffId,

        @Schema(description = "Số thuê bao/doanh nghiệp")
        @Size(max = 2000, message = "businessNo tối đa 2000 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,2000}$", message = "businessNo chỉ gồm chữ, số, '_' hoặc '-'")
        String businessNo,

        @Schema(description = "Số hợp đồng")
        @Size(max = 2000, message = "contractNo tối đa 2000 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,2000}$", message = "contractNo chỉ gồm chữ, số, '_' hoặc '-'")
        String contractNo,

        @Schema(description = "Trạng thái bản ghi")
        @Size(max = 2, message = "status tối đa 2 chữ số")
        @Pattern(regexp = "^[0-9]{0,2}$", message = "status chỉ gồm chữ số")
        String status,

        @Schema(description = "Mã IBM")
        @Size(max = 200, message = "ibmCode tối đa 200 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,200}$", message = "ibmCode chỉ gồm chữ, số, '_' hoặc '-'")
        String ibmCode,

        @Schema(description = "Người phê duyệt")
        @Size(max = 200, message = "approveUser tối đa 200 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,200}$", message = "approveUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String approveUser,

        @Schema(description = "Người tạo")
        @Size(max = 50, message = "createUser tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String createUser,

        @Schema(description = "Người cập nhật")
        @Size(max = 50, message = "updateUser tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String updateUser,

        @Schema(description = "Thời gian tạo (dd/MM/yyyy)")
        @Size(max = 10, message = "createDatetime phải đúng định dạng dd/MM/yyyy")
        @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$|^$", message = "createDatetime phải đúng định dạng dd/MM/yyyy")
        String createDatetime,

        @Schema(description = "Thời gian cập nhật (dd/MM/yyyy)")
        @Size(max = 10, message = "updateDateTime phải đúng định dạng dd/MM/yyyy")
        @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$|^$", message = "updateDateTime phải đúng định dạng dd/MM/yyyy")
        String updateDateTime
) {
}
