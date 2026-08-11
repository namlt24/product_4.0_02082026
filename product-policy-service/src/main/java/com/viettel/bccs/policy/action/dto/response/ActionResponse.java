package com.viettel.bccs.policy.action.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Date;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài cột thật của ACTION (xem ActionEntity).
 */
public record ActionResponse(

        @Schema(description = "Mã hành động (PK)", example = "5001")
        @Size(min = 1, max = 10, message = "actionCode tối đa 10 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{1,10}$", message = "actionCode chỉ gồm chữ và số")
        String actionCode,

        @Schema(description = "Tên hành động", example = "Đấu nối mới")
        @Size(max = 512, message = "name tối đa 512 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "name không được chứa ký tự điều khiển")
        String name,

        @Schema(description = "Mô tả hành động", example = "Hành động đấu nối mới thuê bao")
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

        @Schema(description = "Loại hành động", example = "01")
        @Size(max = 2, message = "type tối đa 2 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,2}$", message = "type chỉ gồm chữ và số")
        String type,

        @Schema(description = "Loại lý do liên kết", example = "CONNECT")
        @Size(max = 20, message = "reasonType tối đa 20 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "reasonType chỉ gồm chữ, số, '_' hoặc '-'")
        String reasonType
) {
}
