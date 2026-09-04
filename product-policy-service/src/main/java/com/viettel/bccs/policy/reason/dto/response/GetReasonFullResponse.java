package com.viettel.bccs.policy.reason.dto.response;

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
public class GetReasonFullResponse {
    @Schema(description = "ID hình thức hòa mạng (PK)", example = "1")
    @Min(value = 0, message = "reasonId phải >= 0")
    @Max(value = 9999999999L, message = "reasonId vượt quá độ dài cột (precision 10)")
    private Long reasonId;

    @Schema(description = "Mã hình thức hòa mạng", example = "HTHM01")
    @Size(max = 20, message = "reasonCode tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "reasonCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String reasonCode;

    @Schema(description = "Tên hình thức hòa mạng", example = "Hòa mạng mới")
    @Size(max = 512, message = "name tối đa 512 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Loại hình thức hòa mạng", example = "NEW")
    @Size(max = 20, message = "reasonType tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "reasonType chỉ gồm chữ, số, '_' hoặc '-'")
    private String reasonType;

    @Schema(description = "Mã dịch vụ bán hàng", example = "DVBH1")
    @Size(max = 100, message = "Mã dịch vụ bán hàng tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String saleServiceCode;
}
