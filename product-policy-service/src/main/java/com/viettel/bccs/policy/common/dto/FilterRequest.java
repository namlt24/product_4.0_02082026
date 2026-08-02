package com.viettel.bccs.policy.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    private String property;
    private String entity;

    @Schema(description = "Giá trị thuộc tính")
    private Object value;

    private Operator operator;

    private OrExprFilterRequest orExprFilterRequest;
    private ListFilterRequest listFilterRequest;

    private String valueText;

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