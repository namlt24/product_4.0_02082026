package com.viettel.bccs.policy.mapbusinessskipdebt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(description = "Thông tin cấu hình bỏ qua công nợ kinh doanh")
public record MapBusinessSkipDebtResponse(
        @Schema(description = "ID bản ghi", example = "1")
        Long mapId,

        @Schema(description = "Mã hành động", example = "ACT001")
        String actionCode,

        @Schema(description = "ID dịch vụ viễn thông", example = "100")
        Long telecomServiceId,

        @Schema(description = "Mã sản phẩm", example = "PROD001")
        String productCode,

        @Schema(description = "Ngày bắt đầu hiệu lực")
        Date effectDatetime,

        @Schema(description = "Ngày hết hiệu lực")
        Date expireDatetime,

        @Schema(description = "ID cửa hàng", example = "10")
        Long shopId,

        @Schema(description = "ID nhân viên", example = "20")
        Long staffId,

        @Schema(description = "Số kinh doanh", example = "BN001")
        String businessNo,

        @Schema(description = "Số hợp đồng", example = "CN001")
        String contractNo,

        @Schema(description = "Trạng thái (1: active, 0: inactive)", example = "1")
        Long status,

        @Schema(description = "Mã IBM", example = "IBM001")
        String ibmCode,

        @Schema(description = "Người phê duyệt", example = "admin")
        String approveUser,

        @Schema(description = "Người tạo", example = "admin")
        String createUser,

        @Schema(description = "Người cập nhật", example = "admin")
        String updateUser,

        @Schema(description = "Ngày tạo")
        Date createDatetime,

        @Schema(description = "Ngày cập nhật")
        Date updateDatetime
) {
}