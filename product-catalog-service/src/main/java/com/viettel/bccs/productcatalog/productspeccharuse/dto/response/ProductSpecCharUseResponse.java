package com.viettel.bccs.productcatalog.productspeccharuse.dto.response;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài cột thật của PRODUCT_SPEC_CHAR_USE (xem
 * ProductSpecCharUseEntity) — field không nullable ở DB vẫn cho phép null ở response record
 * (record component không có @NotNull), @Size/@Pattern chỉ áp dụng khi giá trị khác null.
 */
public record ProductSpecCharUseResponse(

        @Schema(description = "ID bản ghi sử dụng đặc tính sản phẩm")
        @Min(value = 1, message = "prodSpecCharUseId phải >= 1")
        @Max(value = 9999999999L, message = "prodSpecCharUseId vượt quá độ dài cột (precision 10)")
        Long prodSpecCharUseId,

        @Schema(description = "Thứ tự sắp xếp", example = "1")
        @Min(value = 0, message = "orderChar phải >= 0")
        @Max(value = 9999999999L, message = "orderChar vượt quá độ dài cột (precision 10)")
        Long orderChar,

        @Schema(description = "ID bộ đặc tính sản phẩm")
        @Min(value = 1, message = "productSpecId phải >= 1")
        @Max(value = 9999999999L, message = "productSpecId vượt quá độ dài cột (precision 10)")
        Long productSpecId,

        @Schema(description = "ID đặc tính sản phẩm")
        @Min(value = 1, message = "productSpecCharId phải >= 1")
        @Max(value = 9999999999L, message = "productSpecCharId vượt quá độ dài cột (precision 10)")
        Long productSpecCharId,

        @Schema(description = "ID giá trị đặc tính sản phẩm")
        @Min(value = 1, message = "productSpecCharValueId phải >= 1")
        @Max(value = 9999999999L, message = "productSpecCharValueId vượt quá độ dài cột (precision 10)")
        Long productSpecCharValueId,

        @Schema(description = "Trạng thái", example = "1")
        @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
        @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
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

        @Schema(description = "Loại hệ thống")
        @Size(max = 100, message = "systemType tối đa 100 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "systemType không được chứa ký tự điều khiển")
        String systemType,

        @Schema(description = "Giá trị cụ thể")
        @Size(max = 4000, message = "specificValue tối đa 4000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "specificValue không được chứa ký tự điều khiển")
        String specificValue,

        @Schema(description = "Giai đoạn cấu hình")
        @Size(max = 200, message = "configPhase tối đa 200 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "configPhase không được chứa ký tự điều khiển")
        String configPhase,

        @Schema(description = "Số lần tối thiểu")
        @Min(value = 0, message = "min phải >= 0")
        @Max(value = 9999999999L, message = "min vượt quá độ dài cột (precision 10)")
        Long min,

        @Schema(description = "Số lần tối đa")
        @Min(value = 0, message = "max phải >= 0")
        @Max(value = 9999999999L, message = "max vượt quá độ dài cột (precision 10)")
        Long max,

        @Schema(description = "Có bắt buộc không (0/1)", example = "0")
        @Size(min = 1, max = 1, message = "isRequired đúng 1 ký tự")
        @Pattern(regexp = "^[01]$", message = "isRequired chỉ nhận giá trị 0 hoặc 1")
        String isRequired,

        @Schema(description = "Ghi chú")
        @Size(max = 2000, message = "note tối đa 2000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,2000}$", message = "note không được chứa ký tự điều khiển")
        String note
) {
}
