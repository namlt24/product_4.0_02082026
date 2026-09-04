package com.viettel.bccs.policy.mapbusinessskipdebt.dto.response;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài/precision cột thật của
 * MAP_BUSINESS_SKIP_DEBT (xem MapBusinessSkipDebtEntity).
 */
@Schema(description = "Thông tin cấu hình bỏ qua công nợ kinh doanh")
public record MapBusinessSkipDebtResponse(
        @Schema(description = "ID bản ghi", example = "1")
        @Min(value = 0, message = "mapId phải >= 0")
        @Max(value = 9999999999L, message = "mapId vượt quá độ dài cột (precision 10)")
        Long mapId,

        @Schema(description = "Mã hành động", example = "ACT001")
        @Size(max = 10, message = "actionCode tối đa 10 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,10}$", message = "actionCode chỉ gồm chữ và số")
        String actionCode,

        @Schema(description = "ID dịch vụ viễn thông", example = "100")
        @Min(value = 0, message = "telecomServiceId phải >= 0")
        @Max(value = 9999999999L, message = "telecomServiceId vượt quá độ dài cột (precision 10)")
        Long telecomServiceId,

        @Schema(description = "Mã sản phẩm", example = "PROD001")
        @Size(max = 200, message = "productCode tối đa 200 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,200}$", message = "productCode chỉ gồm chữ, số, '_' hoặc '-'")
        String productCode,

        @Schema(description = "Ngày bắt đầu hiệu lực")
        Date effectDatetime,

        @Schema(description = "Ngày hết hiệu lực")
        Date expireDatetime,

        @Schema(description = "ID cửa hàng", example = "10")
        @Min(value = 0, message = "shopId phải >= 0")
        @Max(value = 9999999999L, message = "shopId vượt quá độ dài cột (precision 10)")
        Long shopId,

        @Schema(description = "ID nhân viên", example = "20")
        @Min(value = 0, message = "staffId phải >= 0")
        @Max(value = 9999999999L, message = "staffId vượt quá độ dài cột (precision 10)")
        Long staffId,

        @Schema(description = "Số kinh doanh", example = "BN001")
        @Size(max = 2000, message = "businessNo tối đa 2000 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,2000}$", message = "businessNo chỉ gồm chữ, số, '_' hoặc '-'")
        String businessNo,

        @Schema(description = "Số hợp đồng", example = "CN001")
        @Size(max = 2000, message = "contractNo tối đa 2000 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,2000}$", message = "contractNo chỉ gồm chữ, số, '_' hoặc '-'")
        String contractNo,

        @Schema(description = "Trạng thái (1: active, 0: inactive)", example = "1")
        @Min(value = 0, message = "status phải >= 0")
        @Max(value = 99, message = "status vượt quá độ dài cột (precision 2)")
        Long status,

        @Schema(description = "Mã IBM", example = "IBM001")
        @Size(max = 200, message = "ibmCode tối đa 200 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,200}$", message = "ibmCode chỉ gồm chữ, số, '_' hoặc '-'")
        String ibmCode,

        @Schema(description = "Người phê duyệt", example = "admin")
        @Size(max = 200, message = "approveUser tối đa 200 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,200}$", message = "approveUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String approveUser,

        @Schema(description = "Người tạo", example = "admin")
        @Size(max = 50, message = "createUser tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String createUser,

        @Schema(description = "Người cập nhật", example = "admin")
        @Size(max = 50, message = "updateUser tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String updateUser,

        @Schema(description = "Ngày tạo")
        Date createDatetime,

        @Schema(description = "Ngày cập nhật")
        Date updateDatetime
) {
}
