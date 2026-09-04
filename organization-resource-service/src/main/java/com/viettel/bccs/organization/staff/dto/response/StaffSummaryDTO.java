package com.viettel.bccs.organization.staff.dto.response;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO rút gọn của STAFF chỉ chứa 3 trường dùng để trả về nhân viên duyệt đơn
 * (staffCode, name, staffId) — không phơi toàn bộ thông tin nhân viên ra ngoài.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class StaffSummaryDTO implements Serializable {

    @Schema(description = "Mã nhân viên", example = "NV_002")
    @Size(max = 40, message = "staffCode tối đa 40 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "staffCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String staffCode;

    @Schema(description = "Tên nhân viên", example = "Trần Văn B")
    @Size(max = 300, message = "name tối đa 300 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,300}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "ID nhân viên", example = "12346")
    @Min(value = 0, message = "staffId phải >= 0")
    @Max(value = 9999999999L, message = "staffId vượt quá độ dài cột (precision 10)")
    private Long staffId;
}
