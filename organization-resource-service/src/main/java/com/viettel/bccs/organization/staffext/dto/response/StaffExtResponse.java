package com.viettel.bccs.organization.staffext.dto.response;

import java.util.Date;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài/precision cột thật của STAFF_EXT (xem StaffExtEntity).
 * Field không nullable ở DB vẫn cho phép null ở record, @Size/@Pattern/@Min/@Max chỉ áp dụng khi giá trị khác null.
 */
public record StaffExtResponse(

        @Min(value = 0, message = "staffExtId phải >= 0")
        @Max(value = 9999999999L, message = "staffExtId vượt quá độ dài cột (precision 10)")
        Long staffExtId,

        @Min(value = 0, message = "staffId phải >= 0")
        @Max(value = 9999999999L, message = "staffId vượt quá độ dài cột (precision 10)")
        Long staffId,

        @Size(max = 50, message = "key tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "key chỉ gồm chữ, số, '_' hoặc '-'")
        String key,

        @Size(max = 4000, message = "value tối đa 4000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "value không được chứa ký tự điều khiển")
        String value,

        @Size(max = 2, message = "status tối đa 2 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,2}$", message = "status chỉ gồm chữ hoặc số")
        String status,

        @Size(max = 50, message = "createUser tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String createUser,

        Date createDatetime,

        @Size(max = 50, message = "updateUser tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String updateUser,

        Date updateDatetime
) {
}
