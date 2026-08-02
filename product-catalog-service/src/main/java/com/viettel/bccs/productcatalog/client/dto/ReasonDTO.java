package com.viettel.bccs.productcatalog.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Schema(description = "Thông tin lý do")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReasonDTO {

    @Schema(description = "ID lý do", example = "1")
    @JsonProperty("REASON_ID")
    private Long reasonId;

    @Schema(description = "Mã lý do", example = "LYDO_001")
    @JsonProperty("CODE")
    private String code;

    @Schema(description = "Tên lý do", example = "Lý do PCCC")
    @JsonProperty("NAME")
    private String name;

    @Schema(description = "Mô tả", example = "Lý do phục vụ quản lý cước PCCC")
    @JsonProperty("DESCRIPTION")
    private String description;

    @Schema(description = "Trạng thái", example = "1")
    @JsonProperty("STATUS")
    private String status;

    @Schema(description = "Ngày tạo")
    @JsonProperty("CREATE_DATETIME")
    private Date createDatetime;

    @Schema(description = "Ngày cập nhật")
    @JsonProperty("UPDATE_DATETIME")
    private Date updateDatetime;

    @Schema(description = "Người tạo")
    @JsonProperty("CREATE_USER")
    private String createUser;

    @Schema(description = "Người cập nhật")
    @JsonProperty("UPDATE_USER")
    private String updateUser;
}