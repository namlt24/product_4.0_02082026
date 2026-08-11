package com.viettel.bccs.policy.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterRequest implements Serializable {

    public enum Operator {
        IN,
        EQ,
        NE,
        NOTIN,
        EQALL,
        AQANY,
        AS,
        LT,
        GT,
        GT_DATE,
        LT_DATE,
        LOE,
        GOE,
        BETWEEN,
        LIKE,
        EXACT,
        LIKE_BEGIN,
        LIKE_END,
        RANGE,
        STARTWITH,
        TRUNC_DAY_LOE,
        IS_NULL,
        IS_NOT_NULL,
        LIKE_CONCAT
    }

    @Schema(description = "Mã thuộc tính")
    @Size(max = 100, message = "property tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_.]{0,100}$", message = "property chỉ gồm chữ, số, '_' hoặc '.'")
    private String property;

    @Size(max = 100, message = "entity tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_.]{0,100}$", message = "entity chỉ gồm chữ, số, '_' hoặc '.'")
    private String entity;

    @Schema(description = "Giá trị thuộc tính")
    private Object value;

    private Operator operator;

    @Size(max = 1000, message = "valueText tối đa 1000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "valueText không được chứa ký tự điều khiển")
    private String valueText;

    @Size(max = 20, message = "valueType tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "valueType chỉ gồm chữ, số, '_' hoặc '-'")
    private String valueType;

    // Boolean (không phải boolean nguyên thủy) để client được phép bỏ qua field này trong JSON —
    // trước đây là primitive boolean, Jackson ở service này bắt buộc phải có mặt tường minh
    // (FAIL_ON_NULL_FOR_PRIMITIVES) nếu không sẽ báo lỗi 400 dù field không liên quan tới nghiệp vụ
    // đang gọi. @Builder.Default đảm bảo mặc định vẫn là false ở mọi cách khởi tạo (no-args, builder).
    @Builder.Default
    private Boolean notEqual = Boolean.FALSE;
    @Builder.Default
    private Boolean extract = Boolean.FALSE;
    @Builder.Default
    private Boolean valueInRange = Boolean.FALSE;

    @Size(max = 500, message = "lstValue tối đa 500 phần tử")
    private List<String> lstValue;

    @Override
    public int hashCode() {
        return Objects.hash(property, value, operator);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        FilterRequest other = (FilterRequest) obj;
        return Objects.equals(this.property, other.property)
                && Objects.equals(this.value, other.value)
                && Objects.equals(this.operator, other.operator);
    }

    @Override
    public String toString() {
        return "Property: " + property + " -- Value: " + value;
    }
}