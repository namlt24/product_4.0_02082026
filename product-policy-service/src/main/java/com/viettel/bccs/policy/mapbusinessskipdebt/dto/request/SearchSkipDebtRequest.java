package com.viettel.bccs.policy.mapbusinessskipdebt.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Yêu cầu tra cứu cấu hình bỏ qua công nợ kinh doanh")
public record SearchSkipDebtRequest(
        @Parameter(description = "Mã hành động nghiệp vụ", example = "ACT001", required = true)
        String actionCode,

        @Parameter(description = "ID dịch vụ viễn thông", example = "100", required = true)
        Long telecomServiceId,

        @Parameter(description = "Thời điểm kiểm tra hiệu lực (dd/MM/yyyy)", example = "01/08/2026", required = true)
        String effectDatetime,

        @Parameter(description = "Mã cửa hàng/đại lý", example = "SHOP001", required = true)
        String shopCode,

        @Parameter(description = "Mã nhân viên", example = "STAFF001", required = true)
        String staffCode,

        @Parameter(description = "Số thuê bao / mã doanh nghiệp", example = "BN001", required = true)
        String businessNo,

        @Parameter(description = "Số hợp đồng (tùy chọn)", example = "CN001")
        String contractNo
) {
}