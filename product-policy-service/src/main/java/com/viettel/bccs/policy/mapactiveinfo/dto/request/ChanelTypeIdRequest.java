package com.viettel.bccs.policy.mapactiveinfo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho API getChanelTypeIdMapActiveInfo.
 * Chỉ gồm 3 field thực sự được dùng để suy ra channelTypeId (thay vì nhận nguyên object
 * StaffDTO ở package client.dto, việc controller phụ thuộc package client vi phạm
 * LayeredArchitectureTest).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class ChanelTypeIdRequest {

    @Schema(description = "ID kênh của shop nhân viên", example = "1")
    @Min(value = 0, message = "shopChanelTypeId phải >= 0")
    @Max(value = 9999999999L, message = "shopChanelTypeId vượt quá độ dài cho phép")
    private Long shopChanelTypeId;

    @Schema(description = "ID kênh của nhân viên", example = "1")
    @Min(value = 0, message = "channelTypeId phải >= 0")
    @Max(value = 9999999999L, message = "channelTypeId vượt quá độ dài cho phép")
    private Long channelTypeId;

    @Schema(description = "Điểm bán (point of sale)", example = "1")
    @Size(max = 5, message = "pointOfSale tối đa 5 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,5}$", message = "pointOfSale chỉ gồm chữ, số, '_' hoặc '-'")
    private String pointOfSale;
}
