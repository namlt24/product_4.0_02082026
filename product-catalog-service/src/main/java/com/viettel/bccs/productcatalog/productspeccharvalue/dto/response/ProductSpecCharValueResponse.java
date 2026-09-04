package com.viettel.bccs.productcatalog.productspeccharvalue.dto.response;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài cột thật của PRODUCT_SPEC_CHAR_VALUE (xem
 * ProductSpecCharValueEntity) — field không nullable ở DB vẫn cho phép null ở response record
 * (record component không có @NotNull), @Size/@Pattern chỉ áp dụng khi giá trị khác null.
 */
public record ProductSpecCharValueResponse(

        @Schema(description = "ID giá trị thuộc tính sản phẩm")
        @Min(value = 1, message = "productSpecCharValueId phải >= 1")
        @Max(value = 9999999999L, message = "productSpecCharValueId vượt quá độ dài cột (precision 10)")
        Long productSpecCharValueId,

        @Schema(description = "ID thuộc tính sản phẩm")
        @Min(value = 1, message = "productSpecCharId phải >= 1")
        @Max(value = 9999999999L, message = "productSpecCharId vượt quá độ dài cột (precision 10)")
        Long productSpecCharId,

        @Schema(description = "Loại giá trị")
        @Size(max = 10, message = "valueType tối đa 10 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,10}$", message = "valueType không được chứa ký tự điều khiển")
        String valueType,

        @Schema(description = "Cờ mặc định (0/1)", example = "0")
        @Min(value = 0, message = "isDefault phải >= 0")
        @Max(value = 9, message = "isDefault vượt quá độ dài cột (precision 1)")
        Long isDefault,

        @Schema(description = "Giá trị")
        @Size(max = 4000, message = "value tối đa 4000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "value không được chứa ký tự điều khiển")
        String value,

        @Schema(description = "Đơn vị tính")
        @Size(max = 10, message = "unitOfMeasure tối đa 10 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,10}$", message = "unitOfMeasure không được chứa ký tự điều khiển")
        String unitOfMeasure,

        @Schema(description = "Giá trị từ")
        @Size(max = 4000, message = "valueFrom tối đa 4000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "valueFrom không được chứa ký tự điều khiển")
        String valueFrom,

        @Schema(description = "Giá trị đến")
        @Size(max = 4000, message = "valueTo tối đa 4000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "valueTo không được chứa ký tự điều khiển")
        String valueTo,

        @Schema(description = "Khoảng giá trị")
        @Size(max = 10, message = "rangeInterval tối đa 10 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,10}$", message = "rangeInterval không được chứa ký tự điều khiển")
        String rangeInterval,

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

        @Schema(description = "Tên giá trị")
        @Size(max = 500, message = "name tối đa 500 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "name không được chứa ký tự điều khiển")
        String name,

        @Schema(description = "Giá trị cụ thể")
        @Size(max = 10000, message = "specificValue tối đa 10000 ký tự")
        String specificValue,

        @Schema(description = "Ghi chú")
        @Size(max = 256, message = "note tối đa 256 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,256}$", message = "note không được chứa ký tự điều khiển")
        String note
) {
}
