package com.viettel.bccs.policy.reasonpause.dto.response;

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
public class ReasonPauseDTO {

    @Schema(description = "ID REASON_PAUSE (PK)", example = "1")
    @Min(value = 0, message = "reasonPauseId phải >= 0")
    @Max(value = 9999999999L, message = "reasonPauseId vượt quá độ dài cột (precision 10)")
    private Long reasonPauseId;

    @Schema(description = "Số tháng tạm ngưng", example = "3")
    @Min(value = 0, message = "numMonth phải >= 0")
    @Max(value = 9999999999L, message = "numMonth vượt quá độ dài cột (precision 10)")
    private Long numMonth;

    @Schema(description = "Giá tạm ngưng", example = "50000")
    @Min(value = 0, message = "price phải >= 0")
    @Max(value = 9999999999L, message = "price vượt quá độ dài cột (precision 10)")
    private Long price;

    @Schema(description = "ID hình thức hòa mạng", example = "1")
    @Min(value = 0, message = "reasonId phải >= 0")
    @Max(value = 9999999999L, message = "reasonId vượt quá độ dài cột (precision 10)")
    private Long reasonId;

    @Schema(description = "Trạng thái (0/1)", example = "1")
    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Thời điểm tạo")
    private Date createDatetime;

    @Schema(description = "Người tạo", example = "system")
    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Schema(description = "Thời điểm cập nhật")
    private Date updateDatetime;

    @Schema(description = "Người cập nhật", example = "system")
    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;
}
