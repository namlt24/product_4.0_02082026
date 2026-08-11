package com.viettel.bccs.productcatalog.optionset.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Date;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài cột thật của OPTION_SET (xem OptionSetEntity) —
 * field không nullable ở DB vẫn cho phép null ở response record (record component không có
 * @NotNull), @Size/@Pattern chỉ áp dụng khi giá trị khác null.
 */
public record OptionSetResponse(

        @Schema(description = "Id nhóm option set (PK)", example = "1")
        @Min(value = 0, message = "optionSetId phải >= 0")
        @Max(value = 9999999999L, message = "optionSetId vượt quá độ dài cột (precision 10)")
        Long optionSetId,

        @Schema(description = "Mã nhóm option set", example = "CUST_TYPE_GROUP_TYPE")
        @Size(max = 100, message = "code tối đa 100 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'")
        String code,

        @Schema(description = "Tên nhóm option set", example = "Nhóm loại khách hàng")
        @Size(max = 512, message = "name tối đa 512 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "name không được chứa ký tự điều khiển")
        String name,

        @Schema(description = "Trạng thái (0/1)", example = "1")
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

        @Schema(description = "Mô tả", example = "Nhóm loại khách hàng dùng cho MDealer")
        @Size(max = 512, message = "description tối đa 512 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "description không được chứa ký tự điều khiển")
        String description
) {
}
