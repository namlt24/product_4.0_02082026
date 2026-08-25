package com.viettel.bccs.productcatalog.productofferprice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PledgePriceResponse(

    @Schema(description = "Giá tiền", example = "1500000")
    @DecimalMin(value = "0", message = "price phải >= 0")
    @DecimalMax(value = "99999999999999999999", message = "price vượt quá độ dài cột (precision 20)")
    BigDecimal price,

    @Schema(description = "Số tiền cam kết", example = "500000")
    @DecimalMin(value = "0", message = "pledgeAmount phải >= 0")
    @DecimalMax(value = "9999999999", message = "pledgeAmount vượt quá độ dài cột (precision 10)")
    BigDecimal pledgeAmount,

    @Schema(description = "Số tháng cam kết", example = "12")
    @Min(value = 0, message = "pledgeTime phải >= 0")
    @Max(value = 9999999999L, message = "pledgeTime vượt quá độ dài cột (precision 10)")
    Long pledgeTime,

    @Schema(description = "Số tháng ứng trước", example = "3")
    @Size(max = 10, message = "priorPay tối đa 10 ký tự")
    String priorPay
) {}
