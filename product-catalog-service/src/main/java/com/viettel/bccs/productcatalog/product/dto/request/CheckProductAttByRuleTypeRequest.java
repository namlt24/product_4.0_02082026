package com.viettel.bccs.productcatalog.product.dto.request;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request cho API checkProductAttByRuleType — kiểm tra gói cước theo rule type.
 * productCode là mã gói cước (product_offering.code); ruleType là loại rule cần đánh giá
 * (vd CHECK_PACKAGE_ELIGIBILITY). Tính bắt buộc được kiểm tra ở tầng controller/service
 * (RequestValidator), giữ đúng phong cách request DTO của service.
 */
@Schema(description = "Request kiểm tra gói cước theo rule type")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckProductAttByRuleTypeRequest implements Serializable {

    @Schema(description = "Mã gói cước (product_offering.code)", example = "POBAS")
    @Size(max = 50, message = "productCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "productCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String productCode;

    @Schema(description = "Loại rule cần đánh giá", example = "CHECK_PACKAGE_ELIGIBILITY")
    @Size(max = 100, message = "ruleType tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_]{0,100}$", message = "ruleType chỉ gồm chữ, số, '_'")
    private String ruleType;
}
