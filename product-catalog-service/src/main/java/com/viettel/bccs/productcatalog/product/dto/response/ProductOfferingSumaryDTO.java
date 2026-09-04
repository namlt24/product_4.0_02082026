package com.viettel.bccs.productcatalog.product.dto.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Thông tin sản phẩm gói cước")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProductOfferingSumaryDTO implements Serializable {

    @Schema(description = "Mã sản phẩm", example = "PACKAGE_001")
    @Size(max = 50, message = "code tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'")
    private String code;

    @Schema(description = "Tên sản phẩm", example = "Gói cước data 50GB")
    @Size(max = 500, message = "name tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "ID sản phẩm", example = "12345")
    @Min(value = 1, message = "productOfferingId phải >= 1")
    @Max(value = 9999999999L, message = "productOfferingId vượt quá độ dài cột (precision 10)")
    private Long productOfferingId;

    @Schema(description = "ID loại sản phẩm", example = "1")
    @Min(value = 1, message = "productOfferTypeId phải >= 1")
    @Max(value = 9999999999L, message = "productOfferTypeId vượt quá độ dài cột (precision 10)")
    private Long productOfferTypeId;

    @Schema(description = "ID dịch vụ viễn thông", example = "1")
    @Min(value = 1, message = "telecomServiceId phải >= 1")
    @Max(value = 9999999999L, message = "telecomServiceId vượt quá độ dài cột (precision 10)")
    private Long telecomServiceId;

    @Schema(description = "Trạng thái", example = "1")
    @Size(max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "status chỉ gồm chữ hoặc số")
    private String status;
}
