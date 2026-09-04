package com.viettel.bccs.organization.stockchannelmapping.dto.response;

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
public class StockChannelMappingResponse {

    @Schema(description = "ID bản ghi mapping", example = "1")
    @Min(value = 1, message = "stockChannelMappingId phải >= 1")
    @Max(value = 9999999999L, message = "stockChannelMappingId vượt quá độ dài cột (precision 10)")
    private Long stockChannelMappingId;

    @Schema(description = "ID loại dịch vụ (-1 = tất cả)", example = "3")
    @Min(value = -1, message = "telecomServiceId phải >= -1")
    @Max(value = 9999999999L, message = "telecomServiceId vượt quá độ dài cột (precision 10)")
    private Long telecomServiceId;

    @Schema(description = "ID loại kênh (-1 = tất cả)", example = "2")
    @Min(value = -1, message = "channelTypeId phải >= -1")
    @Max(value = 9999999999L, message = "channelTypeId vượt quá độ dài cột (precision 10)")
    private Long channelTypeId;

    @Schema(description = "ID kho số chức năng (-1 = tất cả)", example = "101")
    @Min(value = -1, message = "stockShopId phải >= -1")
    @Max(value = 9999999999L, message = "stockShopId vượt quá độ dài cột (precision 10)")
    private Long stockShopId;

    @Schema(description = "ID cửa hàng (-1 = tất cả)", example = "12345")
    @Min(value = -1, message = "shopId phải >= -1")
    @Max(value = 9999999999L, message = "shopId vượt quá độ dài cột (precision 10)")
    private Long shopId;

    @Schema(description = "ID user (-1 = tất cả)", example = "-1")
    @Min(value = -1, message = "staffId phải >= -1")
    @Max(value = 9999999999L, message = "staffId vượt quá độ dài cột (precision 10)")
    private Long staffId;

    @Schema(description = "Ngày hiệu lực", example = "2026-08-19")
    private Date effectDate;

    @Schema(description = "Ngày hết hiệu lực (null = không giới hạn)", example = "2027-08-19")
    private Date expireDate;

    @Schema(description = "Trạng thái (1 = hiệu lực, 0 = không hiệu lực)", example = "1")
    @Size(min = 1, max = 1, message = "status phải có độ dài 1")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Người tạo", example = "admin")
    @Size(max = 50, message = "createUser không được vượt quá 50 ký tự")
    private String createUser;

    @Schema(description = "Ngày tạo", example = "2026-08-19")
    private Date createDatetime;

    @Schema(description = "Người cập nhật", example = "admin")
    @Size(max = 50, message = "updateUser không được vượt quá 50 ký tự")
    private String updateUser;

    @Schema(description = "Ngày cập nhật", example = "2026-08-19")
    private Date updateDatetime;
}
