package com.viettel.bccs.productcatalog.productofferrelation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Date;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài cột thật của PRODUCT_OFFER_RELATION
 * (xem ProductOfferRelationEntity) — field không nullable ở DB vẫn cho phép null ở response
 * record (record component không có @NotNull), @Size/@Pattern/@Min/@Max chỉ áp dụng khi
 * giá trị khác null.
 */
public record ProductOfferRelationResponse(

        @Schema(description = "ID liên kết mặt hàng (PK)", example = "1")
        @Min(value = 1, message = "productOfferRelationId phải >= 1")
        @Max(value = 9999999999L, message = "productOfferRelationId vượt quá độ dài cột (precision 10)")
        Long productOfferRelationId,

        @Schema(description = "ID loại liên kết", example = "1")
        @Min(value = 1, message = "relationTypeId phải >= 1")
        @Max(value = 9999999999L, message = "relationTypeId vượt quá độ dài cột (precision 10)")
        Long relationTypeId,

        @Schema(description = "ID mặt hàng chính", example = "100")
        @Min(value = 1, message = "mainOfferId phải >= 1")
        @Max(value = 9999999999L, message = "mainOfferId vượt quá độ dài cột (precision 10)")
        Long mainOfferId,

        @Schema(description = "ID mặt hàng liên kết", example = "200")
        @Min(value = 1, message = "relationOfferId phải >= 1")
        @Max(value = 9999999999L, message = "relationOfferId vượt quá độ dài cột (precision 10)")
        Long relationOfferId,

        @Schema(description = "Trạng thái", example = "1")
        @Size(max = 1, message = "status đúng 1 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "status chỉ gồm chữ hoặc số")
        String status,

        @Schema(description = "Người tạo", example = "system")
        @Size(max = 50, message = "createUser tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String createUser,

        @Schema(description = "Thời điểm tạo")
        Date createDatetime,

        @Schema(description = "Người cập nhật", example = "system")
        @Size(max = 50, message = "updateUser tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String updateUser,

        @Schema(description = "Thời điểm cập nhật")
        Date updateDatetime,

        @Schema(description = "Giai đoạn cấu hình", example = "1")
        @Size(max = 200, message = "configPhase tối đa 200 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "configPhase không được chứa ký tự điều khiển")
        String configPhase,

        @Schema(description = "Mô tả")
        @Size(max = 512, message = "description tối đa 512 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "description không được chứa ký tự điều khiển")
        String description,

        @Schema(description = "Ngày hiệu lực")
        Date effectDatetime,

        @Schema(description = "Ngày hết hạn")
        Date expireDatetime
) {
}
