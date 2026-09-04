package com.viettel.bccs.organization.shop.dto.request;

import java.util.List;

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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetListStockValidRequest {

    @Size(max = 40, message = "staffCode tối đa 40 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_\\-]{1,40}$", message = "staffCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String staffCode;

    @Size(min = 1, max = 1000, message = "shopIds phải có từ 1 đến 1000 phần tử")
    private List<Long> shopIds;

    @Min(value = 0, message = "telServiceId phải >= 0")
    @Max(value = 9999999999L, message = "telServiceId vượt quá độ dài cột (precision 10)")
    private Long telServiceId;
}
