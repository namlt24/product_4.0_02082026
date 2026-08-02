package com.viettel.bccs.organization.identitytype.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class IdentityTypeDTO implements Serializable {

    @Schema(description = "Mã loại giấy tờ", example = "IDC")
    private String idType;

    @Schema(description = "Tên loại giấy tờ", example = "Chứng minh nhân dân")
    private String name;

    @Schema(description = "Trạng thái: 0 Không hiệu lực, 1 Hiệu lực", example = "1")
    private String status;

    @Schema(description = "Mô tả", example = "Giấy tờ tùy thân CMND/CCCD")
    private String description;

    @Schema(description = "Độ dài tối thiểu", example = "9")
    private Integer minLength;

    @Schema(description = "Độ dài tối đa", example = "12")
    private Integer maxLength;

    @Schema(description = "Mẫu giá trị (regex)", example = "^[0-9]{9,12}$")
    private String valuePattern;
}