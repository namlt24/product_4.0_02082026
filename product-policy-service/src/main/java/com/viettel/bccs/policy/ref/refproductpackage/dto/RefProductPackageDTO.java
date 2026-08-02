package com.viettel.bccs.policy.ref.refproductpackage.dto;

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
public class RefProductPackageDTO {

    @Schema(description = "ID gói sản phẩm", example = "1")
    private Long productPackageId;

    @Schema(description = "Tên gói sản phẩm", example = "Gói cước trả sau")
    private String name;

    @Schema(description = "Mã gói sản phẩm", example = "POSTPAID_001")
    private String code;

    @Schema(description = "Mô tả", example = "Gói cước trả sau dành cho khách hàng")
    private String description;

    @Schema(description = "Trạng thái: 0 Không hiệu lực, 1 Hiệu lực", example = "1")
    private String status;

    @Schema(description = "Ngày hiệu lực", example = "2024-01-01")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hiệu lực", example = "2025-12-31")
    private Date expireDatetime;

    @Schema(description = "Ngày cập nhật", example = "2024-06-15")
    private Date updateDatetime;

    @Schema(description = "Loại: 0 Mặc định, 1 Khuyến mại", example = "0")
    private String type;

    @Schema(description = "ID kế toán", example = "100")
    private Long accountingId;

    @Schema(description = "Loại phí: 0 Trả trước, 1 Trả sau", example = "1")
    private String feeType;

    @Schema(description = "ID dịch vụ viễn thông", example = "1")
    private Long telecomServiceId;
}