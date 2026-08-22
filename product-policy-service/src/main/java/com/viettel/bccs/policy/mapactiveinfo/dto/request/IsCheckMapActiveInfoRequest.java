package com.viettel.bccs.policy.mapactiveinfo.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IsCheckMapActiveInfoRequest {

    @Size(max = 20, message = "productOfferType tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "productOfferType chỉ gồm chữ, số, '_' hoặc '-'")
    private String productOfferType;

    @Size(max = 10, message = "actionCode tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "actionCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String actionCode;

    // Không dùng @NotNull: DTO này hiện không được bind qua bất kỳ @RequestBody nào (dead code) —
    // không có entry point API để validate thủ công. Giữ @Min/@Max cho khi được dùng lại.
    @Min(value = 0, message = "telServiceId phải >= 0")
    @Max(value = 9999999999L, message = "telServiceId vượt quá độ dài cột (precision 10)")
    private Long telServiceId;
}