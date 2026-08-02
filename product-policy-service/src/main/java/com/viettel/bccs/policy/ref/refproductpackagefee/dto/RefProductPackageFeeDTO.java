package com.viettel.bccs.policy.ref.refproductpackagefee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class RefProductPackageFeeDTO {

    @Schema(description = "ID phí gói sản phẩm", example = "1")
    private Long productPackageFeeId;

    @Schema(description = "ID gói sản phẩm", example = "1")
    private Long productPackageId;

    @Schema(description = "ID chính sách giá", example = "10")
    private Long pricePolicyId;

    @Schema(description = "ID loại giá", example = "5")
    private Long priceTypeId;

    @Schema(description = "Tên phí", example = "Phí dịch vụ tháng 1")
    private String name;

    @Schema(description = "Giá", example = "150000")
    private Long price;

    @Schema(description = "VAT", example = "15000")
    private Long vat;

    @Schema(description = "Ngày hiệu lực", example = "2024-01-01")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hiệu lực", example = "2025-12-31")
    private Date expireDatetime;

    @Schema(description = "Trạng thái: 0 Không hiệu lực, 1 Hiệu lực", example = "1")
    private String status;
}