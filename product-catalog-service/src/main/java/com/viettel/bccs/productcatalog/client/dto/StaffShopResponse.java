package com.viettel.bccs.productcatalog.client.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Thông tin nhân viên kèm cửa hàng (cross-service DTO, organization-resource-service)")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class StaffShopResponse implements Serializable {

    @Schema(description = "ID nhân viên", example = "12345")
    private Long staffId;

    @Schema(description = "Mã nhân viên", example = "NV_001")
    private String staffCode;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Thông tin cửa hàng của nhân viên")
    private ShopDTO shop;
}
