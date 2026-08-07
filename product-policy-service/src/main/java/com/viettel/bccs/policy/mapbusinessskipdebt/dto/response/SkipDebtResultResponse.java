package com.viettel.bccs.policy.mapbusinessskipdebt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Kết quả tra cứu bỏ qua công nợ kinh doanh - tất cả trường dạng String")
public record SkipDebtResultResponse(
        @Schema(description = "ID bản ghi ánh xạ")
        String mapId,

        @Schema(description = "Mã hành động")
        String actionCode,

        @Schema(description = "ID dịch vụ viễn thông")
        String telecomServiceId,

        @Schema(description = "Mã sản phẩm")
        String productCode,

        @Schema(description = "Ngày hiệu lực (dd/MM/yyyy)")
        String effectDateTime,

        @Schema(description = "Ngày hết hiệu lực (dd/MM/yyyy)")
        String expireDateTime,

        @Schema(description = "ID cửa hàng")
        String shopId,

        @Schema(description = "ID nhân viên")
        String staffId,

        @Schema(description = "Số thuê bao/doanh nghiệp")
        String businessNo,

        @Schema(description = "Số hợp đồng")
        String contractNo,

        @Schema(description = "Trạng thái bản ghi")
        String status,

        @Schema(description = "Mã IBM")
        String ibmCode,

        @Schema(description = "Người phê duyệt")
        String approveUser,

        @Schema(description = "Người tạo")
        String createUser,

        @Schema(description = "Người cập nhật")
        String updateUser,

        @Schema(description = "Thời gian tạo (dd/MM/yyyy)")
        String createDatetime,

        @Schema(description = "Thời gian cập nhật (dd/MM/yyyy)")
        String updateDateTime
) {
}