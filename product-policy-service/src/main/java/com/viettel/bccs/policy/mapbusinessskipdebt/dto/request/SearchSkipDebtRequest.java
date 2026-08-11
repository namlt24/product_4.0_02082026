package com.viettel.bccs.policy.mapbusinessskipdebt.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Yêu cầu tra cứu cấu hình bỏ qua công nợ kinh doanh")
public record SearchSkipDebtRequest(
        @Parameter(description = "Mã hành động nghiệp vụ", example = "ACT001", required = true)
        @Size(min = 1, max = 10, message = "actionCode tối đa 10 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{1,10}$", message = "actionCode chỉ gồm chữ và số")
        String actionCode,

        @Parameter(description = "ID dịch vụ viễn thông", example = "100", required = true)
        @Min(value = 0, message = "telecomServiceId phải >= 0")
        @Max(value = 9999999999L, message = "telecomServiceId vượt quá độ dài cột (precision 10)")
        Long telecomServiceId,

        @Parameter(description = "Thời điểm kiểm tra hiệu lực (dd/MM/yyyy)", example = "01/08/2026", required = true)
        @Size(min = 10, max = 10, message = "effectDatetime phải đúng định dạng dd/MM/yyyy")
        @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "effectDatetime phải đúng định dạng dd/MM/yyyy")
        String effectDatetime,

        @Parameter(description = "Mã cửa hàng/đại lý", example = "SHOP001", required = true)
        @Size(min = 1, max = 40, message = "shopCode tối đa 40 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{1,40}$", message = "shopCode chỉ gồm chữ, số, '_' hoặc '-'")
        String shopCode,

        @Parameter(description = "Mã nhân viên", example = "STAFF001", required = true)
        @Size(min = 1, max = 40, message = "staffCode tối đa 40 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{1,40}$", message = "staffCode chỉ gồm chữ, số, '_' hoặc '-'")
        String staffCode,

        @Parameter(description = "Số thuê bao / mã doanh nghiệp", example = "BN001", required = true)
        @Size(min = 1, max = 2000, message = "businessNo tối đa 2000 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{1,2000}$", message = "businessNo chỉ gồm chữ, số, '_' hoặc '-'")
        String businessNo,

        @Parameter(description = "Số hợp đồng (tùy chọn)", example = "CN001")
        @Size(max = 2000, message = "contractNo tối đa 2000 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,2000}$", message = "contractNo chỉ gồm chữ, số, '_' hoặc '-'")
        String contractNo
) {
}
