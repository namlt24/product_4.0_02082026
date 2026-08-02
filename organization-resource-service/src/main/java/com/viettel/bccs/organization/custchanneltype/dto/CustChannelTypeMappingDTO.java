package com.viettel.bccs.organization.custchanneltype.dto;

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
public class CustChannelTypeMappingDTO {

    @Schema(description = "ID mapping", example = "1")
    private Long custChannelTypeMapId;

    @Schema(description = "Mã loại khách hàng", example = "PREPAID")
    private String custType;

    @Schema(description = "ID loại kênh", example = "1")
    private Long channelTypeId;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Người tạo", example = "admin")
    private String createUser;

    @Schema(description = "Ngày tạo", example = "2024-01-01")
    private Date createDatetime;

    @Schema(description = "Người cập nhật", example = "admin")
    private String updateUser;

    @Schema(description = "Ngày cập nhật", example = "2024-06-01")
    private Date updateDatetime;
}