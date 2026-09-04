package com.viettel.bccs.productcatalog.client.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Thông tin lý do")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReasonDTO {

    @Schema(description = "ID lý do", example = "1")
    private Long reasonId;

    @Schema(description = "Mã lý do", example = "LYDO_001")
    private String code;

    @Schema(description = "Tên lý do", example = "Lý do PCCC")
    private String name;

    @Schema(description = "Mô tả", example = "Lý do phục vụ quản lý cước PCCC")
    private String description;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Ngày tạo")
    private Date createDatetime;

    @Schema(description = "Ngày cập nhật")
    private Date updateDatetime;

    @Schema(description = "Người tạo")
    private String createUser;

    @Schema(description = "Người cập nhật")
    private String updateUser;
}