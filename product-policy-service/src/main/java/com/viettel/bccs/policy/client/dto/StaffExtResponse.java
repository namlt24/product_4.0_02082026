package com.viettel.bccs.policy.client.dto;

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
public class StaffExtResponse {

    @Schema(description = "ID", example = "1")
    private Long staffExtId;

    @Schema(description = "ID nhân viên", example = "12345")
    private Long staffId;

    @Schema(description = "Key", example = "BUSINESS_SPEC")
    private String key;

    @Schema(description = "Giá trị", example = "{\"value\": \"HN\"}")
    private String value;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Người tạo", example = "admin")
    private String createUser;

    @Schema(description = "Ngày tạo", example = "2024-01-01")
    private Date createDatetime;

    @Schema(description = "Người cập nhật", example = "admin")
    private String updateUser;

    @Schema(description = "Ngày cập nhật", example = "2024-01-01")
    private Date updateDatetime;
}