package com.viettel.bccs.policy.ref.refproductpackage.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class RefProductPackageDTO {

    @Schema(description = "ID gói sản phẩm", example = "1")
    @Min(value = 0, message = "productPackageId phải >= 0")
    @Max(value = 9999999999L, message = "productPackageId vượt quá độ dài cột (precision 10)")
    private Long productPackageId;

    @Schema(description = "Tên gói sản phẩm", example = "Gói cước trả sau")
    @Size(max = 500, message = "name tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Mã gói sản phẩm", example = "POSTPAID_001")
    @Size(max = 50, message = "code tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'")
    private String code;

    @Schema(description = "Mô tả", example = "Gói cước trả sau dành cho khách hàng")
    @Size(max = 512, message = "description tối đa 512 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "description không được chứa ký tự điều khiển")
    private String description;

    @Schema(description = "Trạng thái: 0 Không hiệu lực, 1 Hiệu lực", example = "1")
    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Ngày hiệu lực", example = "2024-01-01")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hiệu lực", example = "2025-12-31")
    private Date expireDatetime;

    @Schema(description = "Ngày cập nhật", example = "2024-06-15")
    private Date updateDatetime;

    @Schema(description = "Loại: 0 Mặc định, 1 Khuyến mại", example = "0")
    @Size(min = 1, max = 1, message = "type đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "type chỉ nhận giá trị 0 hoặc 1")
    private String type;

    @Schema(description = "ID kế toán", example = "100")
    @Min(value = 0, message = "accountingId phải >= 0")
    @Max(value = 9999999999L, message = "accountingId vượt quá độ dài cột (precision 10)")
    private Long accountingId;

    @Schema(description = "Loại phí: 0 Trả trước, 1 Trả sau", example = "1")
    @Size(min = 1, max = 1, message = "feeType đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "feeType chỉ nhận giá trị 0 hoặc 1")
    private String feeType;

    @Schema(description = "ID dịch vụ viễn thông", example = "1")
    @Min(value = 0, message = "telecomServiceId phải >= 0")
    @Max(value = 9999999999L, message = "telecomServiceId vượt quá độ dài cột (precision 10)")
    private Long telecomServiceId;
}
