package com.viettel.bccs.organization.identitytype.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(min = 1, max = 10, message = "idType tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,10}$", message = "idType chỉ gồm chữ, số, '_' hoặc '-'")
    private String idType;

    @Schema(description = "Tên loại giấy tờ", example = "Chứng minh nhân dân")
    @Size(max = 100, message = "name tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Trạng thái: 0 Không hiệu lực, 1 Hiệu lực", example = "1")
    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Mô tả", example = "Giấy tờ tùy thân CMND/CCCD")
    @Size(max = 512, message = "description tối đa 512 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "description không được chứa ký tự điều khiển")
    private String description;

    @Schema(description = "Độ dài tối thiểu", example = "9")
    @Min(value = 0, message = "minLength phải >= 0")
    @Max(value = 999, message = "minLength vượt quá độ dài cột (precision 3)")
    private Integer minLength;

    @Schema(description = "Độ dài tối đa", example = "12")
    @Min(value = 0, message = "maxLength phải >= 0")
    @Max(value = 999, message = "maxLength vượt quá độ dài cột (precision 3)")
    private Integer maxLength;

    @Schema(description = "Mẫu giá trị (regex)", example = "^[0-9]{9,12}$")
    @Size(max = 100, message = "valuePattern tối đa 100 ký tự")
    private String valuePattern;
}
