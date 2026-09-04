package com.viettel.bccs.productcatalog.common.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOfferOutputDTO implements Serializable {

    @Schema(description = "Id sản phẩm (PRODUCT_OFFERING_ID)", example = "1")
    @Min(value = 0, message = "productOfferId phải >= 0")
    @Max(value = 9999999999L, message = "productOfferId vượt quá độ dài cột (precision 10)")
    private Long productOfferId;

    @Schema(description = "Tên sản phẩm", example = "Gói cước ABC")
    @Size(max = 500, message = "name tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Mã sản phẩm", example = "ABC01")
    @Size(max = 50, message = "code tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'")
    private String code;

    @Schema(description = "Trạng thái (0/1)", example = "1")
    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;
}