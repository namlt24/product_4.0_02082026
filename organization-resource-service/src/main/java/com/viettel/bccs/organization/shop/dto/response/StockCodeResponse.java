package com.viettel.bccs.organization.shop.dto.response;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StockCodeResponse(
        @Size(max = 40, message = "stockCode tối đa 40 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "stockCode chỉ gồm chữ, số, '_' hoặc '-'")
        String stockCode
) {
}
