package com.viettel.bccs.policy.reason.dto.response;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReasonResponse(

        @Schema(description = "ID hình thức hòa mạng (PK)", example = "1")
        @Min(value = 0, message = "reasonId phải >= 0")
        @Max(value = 9999999999L, message = "reasonId vượt quá độ dài cột (precision 10)")
        Long reasonId,

        @Schema(description = "Mã hình thức hòa mạng", example = "HTHM01")
        @Size(max = 20, message = "reasonCode tối đa 20 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "reasonCode chỉ gồm chữ, số, '_' hoặc '-'")
        String reasonCode,

        @Schema(description = "Loại hình thức hòa mạng", example = "NEW")
        @Size(max = 20, message = "reasonType tối đa 20 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "reasonType chỉ gồm chữ, số, '_' hoặc '-'")
        String reasonType,

        @Schema(description = "Tên hình thức hòa mạng", example = "Hòa mạng mới")
        @Size(max = 512, message = "name tối đa 512 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "name không được chứa ký tự điều khiển")
        String name,

        @Schema(description = "Hình thức thanh toán: 1 Trả sau, 2 Trả trước", example = "1")
        @Size(min = 1, max = 1, message = "payType đúng 1 ký tự")
        @Pattern(regexp = "^[12]$", message = "payType chỉ nhận giá trị 1 hoặc 2")
        String payType,

        @Schema(description = "Danh sách dịch vụ viễn thông áp dụng (danh sách ID phân tách bởi dấu phẩy)",
                example = "1,2,3")
        @Size(max = 1000, message = "telService tối đa 1000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "telService không được chứa ký tự điều khiển")
        String telService,

        @Schema(description = "Mô tả", example = "Hình thức hòa mạng mới cho thuê bao")
        @Size(max = 512, message = "description tối đa 512 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "description không được chứa ký tự điều khiển")
        String description,

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

        @Schema(description = "Giới hạn số thuê bao", example = "10")
        @Min(value = 0, message = "limitNumberIsdn phải >= 0")
        @Max(value = 9999999999L, message = "limitNumberIsdn vượt quá độ dài cột (precision 10)")
        Long limitNumberIsdn,

        @Schema(description = "Giới hạn số lượng người dùng", example = "10")
        @Min(value = 0, message = "limitNumberUser phải >= 0")
        @Max(value = 9999999999L, message = "limitNumberUser vượt quá độ dài cột (precision 10)")
        Long limitNumberUser,

        @Schema(description = "Loại hình thức hòa mạng (0/1)", example = "1")
        @Size(min = 1, max = 1, message = "type đúng 1 ký tự")
        @Pattern(regexp = "^[01]$", message = "type chỉ nhận giá trị 0 hoặc 1")
        String type,

        @Schema(description = "Ngày hiệu lực")
        Date effectDatetime,

        @Schema(description = "Ngày hết hiệu lực")
        Date expireDatetime,

        @Schema(description = "Độ ưu tiên", example = "1")
        @Min(value = 0, message = "priority phải >= 0")
        @Max(value = 9999999999L, message = "priority vượt quá độ dài cột (precision 10)")
        Long priority,

        @Schema(description = "Ghi chú", example = "Ghi chú hình thức hòa mạng")
        @Size(max = 1000, message = "note tối đa 1000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "note không được chứa ký tự điều khiển")
        String note
) {
}
