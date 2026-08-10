package com.viettel.bccs.policy.mapactiveinfo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
    private Long shopChanelTypeId;

    @Schema(description = "ID kênh của nhân viên", example = "1")
    private Long channelTypeId;

    @Schema(description = "Điểm bán (point of sale)", example = "1")
    private String pointOfSale;
}
