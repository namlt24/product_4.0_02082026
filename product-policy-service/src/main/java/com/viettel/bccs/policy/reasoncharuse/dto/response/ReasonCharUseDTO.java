package com.viettel.bccs.policy.reasoncharuse.dto.response;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReasonCharUseDTO {

    @Schema(description = "ID REASON_CHAR_USE (PK)", example = "1")
    @Min(value = 0, message = "reasonCharUseId phải >= 0")
    @Max(value = 9999999999L, message = "reasonCharUseId vượt quá độ dài cột (precision 10)")
    private Long reasonCharUseId;

    @Schema(description = "ID hình thức hòa mạng", example = "1")
    @Min(value = 0, message = "reasonId phải >= 0")
    @Max(value = 9999999999L, message = "reasonId vượt quá độ dài cột (precision 10)")
    private Long reasonId;

    @Schema(description = "ID giá trị đặc tính sản phẩm", example = "1")
    @Min(value = 0, message = "productSpecCharValueId phải >= 0")
    @Max(value = 9999999999L, message = "productSpecCharValueId vượt quá độ dài cột (precision 10)")
    private Long productSpecCharValueId;

    @Schema(description = "ID đặc tính sản phẩm", example = "1")
    @Min(value = 0, message = "productSpecCharId phải >= 0")
    @Max(value = 9999999999L, message = "productSpecCharId vượt quá độ dài cột (precision 10)")
    private Long productSpecCharId;

    @Schema(description = "Người tạo", example = "system")
    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Schema(description = "Thời điểm tạo")
    private Date createDatetime;

    @Schema(description = "Người cập nhật", example = "system")
    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Schema(description = "Thời điểm cập nhật")
    private Date updateDatetime;

    @Schema(description = "Trạng thái (0/1)", example = "1")
    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Giá trị cụ thể", example = "Red")
    @Size(max = 50, message = "specificValue tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "specificValue không được chứa ký tự điều khiển")
    private String specificValue;

    @Schema(description = "Cờ giới hạn (precision 1)", example = "1")
    @Min(value = 0, message = "limited phải >= 0")
    @Max(value = 9, message = "limited vượt quá độ dài cột (precision 1)")
    private Long limited;

    @Schema(description = "Cờ giới hạn 2 (precision 1)", example = "1")
    @Min(value = 0, message = "limited2 phải >= 0")
    @Max(value = 9, message = "limited2 vượt quá độ dài cột (precision 1)")
    private Long limited2;

    @Schema(description = "Giá trị nhỏ nhất", example = "0")
    @Min(value = 0, message = "min phải >= 0")
    @Max(value = 9999999999L, message = "min vượt quá độ dài cột (precision 10)")
    private Long min;

    @Schema(description = "Giá trị lớn nhất", example = "100")
    @Min(value = 0, message = "max phải >= 0")
    @Max(value = 9999999999L, message = "max vượt quá độ dài cột (precision 10)")
    private Long max;

    @Schema(description = "Mã (tra cứu bổ sung, không map trực tiếp cột DB)", example = "COLOR")
    @Size(max = 50, message = "code tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'")
    private String code;

    @Schema(description = "Giá trị hiển thị (tra cứu bổ sung, không map trực tiếp cột DB)", example = "Red")
    @Size(max = 512, message = "value tối đa 512 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "value không được chứa ký tự điều khiển")
    private String value;
}
