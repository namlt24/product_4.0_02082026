package com.viettel.bccs.organization.custtype.dto;

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
public class CustTypeDTO {

    @Schema(description = "Mã loại khách hàng", example = "PREPAID")
    private String custType;

    @Schema(description = "Tên loại khách hàng", example = "Trả trước")
    private String name;

    @Schema(description = "Người tạo", example = "admin")
    private String createUser;

    @Schema(description = "Ngày tạo", example = "2024-01-01")
    private Date createDatetime;

    @Schema(description = "Người cập nhật", example = "admin")
    private String updateUser;

    @Schema(description = "Ngày cập nhật", example = "2024-06-01")
    private Date updateDatetime;

    @Schema(description = "Mô tả", example = "Khách hàng trả trước")
    private String description;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Loại khách hàng: 1 Cá nhân trong nước, 2 Doanh nghiệp, 3 Nước ngoài", example = "1")
    private String groupType;

    @Schema(description = "Thuế", example = "10")
    private Long tax;

    @Schema(description = "Quy hoạch: 0 Khách hàng cũ, 1 Khách hàng mới", example = "1")
    private String plan;

    @Schema(description = "Có khách hàng đại diện: 1 Có, 0 Không", example = "0")
    private String representCust;

    @Schema(description = "ID loại khách hàng", example = "1")
    private Long custTypeId;
}