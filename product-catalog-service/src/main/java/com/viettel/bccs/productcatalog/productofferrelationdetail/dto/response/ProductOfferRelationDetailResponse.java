package com.viettel.bccs.productcatalog.productofferrelationdetail.dto.response;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài cột thật của PRODUCT_OFFER_RELATION_DETAIL
 * (xem ProductOfferRelationDetailEntity) — field không nullable ở DB vẫn cho phép null ở response
 * record (record component không có @NotNull), @Size/@Pattern/@Min/@Max chỉ áp dụng khi
 * giá trị khác null. productOfferRelationDetail có precision 38 vượt phạm vi Long, nên bound
 * @Max thực chất bị giới hạn bởi Long.MAX_VALUE.
 */
public record ProductOfferRelationDetailResponse(

        @Schema(description = "ID chi tiết liên kết mặt hàng (PK)", example = "1")
        @Min(value = 1, message = "productOfferRelationDetail phải >= 1")
        @Max(value = Long.MAX_VALUE, message = "productOfferRelationDetail vượt quá độ dài cột (precision 38)")
        Long productOfferRelationDetail,

        @Schema(description = "ID liên kết mặt hàng", example = "1")
        @Min(value = 1, message = "productOfferRelationId phải >= 1")
        @Max(value = 9999999999L, message = "productOfferRelationId vượt quá độ dài cột (precision 10)")
        Long productOfferRelationId,

        @Schema(description = "ID đặc tính sản phẩm", example = "10")
        @Min(value = 1, message = "productSpecCharId phải >= 1")
        @Max(value = 9999999999L, message = "productSpecCharId vượt quá độ dài cột (precision 10)")
        Long productSpecCharId,

        @Schema(description = "ID giá trị đặc tính sản phẩm", example = "20")
        @Min(value = 1, message = "productSpecCharValueId phải >= 1")
        @Max(value = 9999999999L, message = "productSpecCharValueId vượt quá độ dài cột (precision 10)")
        Long productSpecCharValueId,

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

        @Schema(description = "Giá trị cụ thể", example = "100")
        @Size(max = 50, message = "specificValue tối đa 50 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "specificValue không được chứa ký tự điều khiển")
        String specificValue,

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
