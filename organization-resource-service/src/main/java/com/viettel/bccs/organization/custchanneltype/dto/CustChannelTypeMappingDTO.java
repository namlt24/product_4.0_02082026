package com.viettel.bccs.organization.custchanneltype.dto;

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
public class CustChannelTypeMappingDTO {

    @Schema(description = "ID mapping", example = "1")
    @Min(value = 0, message = "custChannelTypeMapId phải >= 0")
    @Max(value = 9999999999L, message = "custChannelTypeMapId vượt quá độ dài cột (precision 10)")
    private Long custChannelTypeMapId;

    @Schema(description = "Mã loại khách hàng", example = "PREPAID")
    @Size(max = 10, message = "custType tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "custType chỉ gồm chữ, số, '_' hoặc '-'")
    private String custType;

    @Schema(description = "ID loại kênh", example = "1")
    @Min(value = 0, message = "channelTypeId phải >= 0")
    @Max(value = 9999999999L, message = "channelTypeId vượt quá độ dài cột (precision 10)")
    private Long channelTypeId;

    @Schema(description = "Trạng thái", example = "1")
    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Người tạo", example = "admin")
    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Schema(description = "Ngày tạo", example = "2024-01-01")
    private Date createDatetime;

    @Schema(description = "Người cập nhật", example = "admin")
    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Schema(description = "Ngày cập nhật", example = "2024-06-01")
    private Date updateDatetime;
}
