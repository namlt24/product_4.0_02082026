package com.viettel.bccs.policy.discountpromotioncharuse.dto.response;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài/precision cột thật của
 * DISCOUNT_PROMOTION_CHAR_USE (xem DiscountPromotionCharUseEntity).
 */
public record DiscountPromotionCharUseResponse(

        @Schema(description = "Id sử dụng thuộc tính khuyến mãi (PK)", example = "1")
        @Min(value = 0, message = "discountPromotionCharUseId phải >= 0")
        @Max(value = 9999999999L, message = "discountPromotionCharUseId vượt quá độ dài cột (precision 10)")
        Long discountPromotionCharUseId,

        @Schema(description = "Id khuyến mãi giảm giá", example = "1")
        @Min(value = 0, message = "discountPromotionId phải >= 0")
        @Max(value = 9999999999L, message = "discountPromotionId vượt quá độ dài cột (precision 10)")
        Long discountPromotionId,

        @Schema(description = "Id giá trị thuộc tính sản phẩm", example = "1")
        @Min(value = 0, message = "productSpecCharValueId phải >= 0")
        @Max(value = 9999999999L, message = "productSpecCharValueId vượt quá độ dài cột (precision 10)")
        Long productSpecCharValueId,

        @Schema(description = "Id thuộc tính sản phẩm", example = "1")
        @Min(value = 0, message = "productSpecCharId phải >= 0")
        @Max(value = 9999999999L, message = "productSpecCharId vượt quá độ dài cột (precision 10)")
        Long productSpecCharId,

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

        @Schema(description = "Trạng thái (0/1)", example = "1")
        @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
        @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
        String status,

        @Schema(description = "Giá trị cụ thể")
        @Size(max = 2000, message = "specificValue tối đa 2000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,2000}$", message = "specificValue không được chứa ký tự điều khiển")
        String specificValue,

        @Schema(description = "Ngày hiệu lực")
        Date effectDatetime,

        @Schema(description = "Ngày hết hiệu lực")
        Date expireDatetime,

        @Schema(description = "Giới hạn", example = "1")
        @Min(value = 0, message = "limited phải >= 0")
        @Max(value = 9, message = "limited vượt quá độ dài cột (precision 1)")
        Long limited,

        @Schema(description = "Ghi chú")
        @Size(max = 2000, message = "note tối đa 2000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,2000}$", message = "note không được chứa ký tự điều khiển")
        String note
) {
}
