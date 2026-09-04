package com.viettel.bccs.policy.ref.refproductpackagefee.dto;

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
public class RefProductPackageFeeDTO {

    @Schema(description = "ID phí gói sản phẩm", example = "1")
    @Min(value = 0, message = "productPackageFeeId phải >= 0")
    @Max(value = 9999999999L, message = "productPackageFeeId vượt quá độ dài cột (precision 10)")
    private Long productPackageFeeId;

    @Schema(description = "ID gói sản phẩm", example = "1")
    @Min(value = 0, message = "productPackageId phải >= 0")
    @Max(value = 9999999999L, message = "productPackageId vượt quá độ dài cột (precision 10)")
    private Long productPackageId;

    @Schema(description = "ID chính sách giá", example = "10")
    @Min(value = 0, message = "pricePolicyId phải >= 0")
    @Max(value = 9999999999L, message = "pricePolicyId vượt quá độ dài cột (precision 10)")
    private Long pricePolicyId;

    @Schema(description = "ID loại giá", example = "5")
    @Min(value = 0, message = "priceTypeId phải >= 0")
    @Max(value = 9999999999L, message = "priceTypeId vượt quá độ dài cột (precision 10)")
    private Long priceTypeId;

    @Schema(description = "Tên phí", example = "Phí dịch vụ tháng 1")
    @Size(max = 50, message = "name tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Giá", example = "150000")
    @Min(value = 0, message = "price phải >= 0")
    @Max(value = 9999999999L, message = "price vượt quá độ dài cột (precision 10)")
    private Long price;

    @Schema(description = "VAT", example = "15000")
    @Min(value = 0, message = "vat phải >= 0")
    @Max(value = 9999999999L, message = "vat vượt quá độ dài cột (precision 10)")
    private Long vat;

    @Schema(description = "Ngày hiệu lực", example = "2024-01-01")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hiệu lực", example = "2025-12-31")
    private Date expireDatetime;

    @Schema(description = "Trạng thái: 0 Không hiệu lực, 1 Hiệu lực", example = "1")
    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;
}
