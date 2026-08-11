package com.viettel.bccs.productcatalog.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

@Schema(description = "Yêu cầu lọc theo đặc tính sản phẩm")
public class FilterRequest implements Serializable {

    public enum Operator {
        IN,
        EQ,
        NE,
        NOTIN,
        LT,
        GT,
        LOE,
        GOE,
        BETWEEN,
        LIKE,
        EXACT,
        LIKE_BEGIN,
        LIKE_END,
        RANGE,
        STARTWITH,
        IS_NULL,
        IS_NOT_NULL
    }

    @Schema(description = "Mã thuộc tính đặc tính (code của product_spec_char)", example = "SPEED")
    @Size(max = 100, message = "property tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "property chỉ gồm chữ, số, '_' hoặc '-'")
    private String property;

    @Schema(description = "Giá trị so sánh dạng text", example = "100")
    @Size(max = 1000, message = "valueText tối đa 1000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "valueText không được chứa ký tự điều khiển")
    private String valueText;

    @Schema(description = "Kiểu giá trị: LONG, STRING, DATE...", example = "LONG")
    @Size(max = 20, message = "valueType tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "valueType chỉ gồm chữ, số, '_' hoặc '-'")
    private String valueType;

    @Schema(description = "Toán tử so sánh: EQ, NE, LT, GT, LOE, GOE, BETWEEN, LIKE...", example = "EQ")
    private Operator operator;

    @Schema(description = "True: dùng NOT EXISTS (loại trừ), False: dùng EXISTS (lọc theo)", example = "false")
    private Boolean notEqual;

    public FilterRequest() {
    }

    public FilterRequest(String property, String valueText, String valueType, Operator operator, Boolean notEqual) {
        this.property = property;
        this.valueText = valueText;
        this.valueType = valueType;
        this.operator = operator != null ? operator : Operator.EQ;
        this.notEqual = notEqual != null ? notEqual : false;
    }

    public static FilterRequest of(String property, String valueText, String valueType) {
        return new FilterRequest(property, valueText, valueType, Operator.EQ, false);
    }

    public static FilterRequest of(String property, String valueText, String valueType, Operator operator, boolean notEqual) {
        return new FilterRequest(property, valueText, valueType, operator, notEqual);
    }

    public String getProperty() { return property; }
    public void setProperty(String property) { this.property = property; }
    public String getValueText() { return valueText; }
    public void setValueText(String valueText) { this.valueText = valueText; }
    public String getValueType() { return valueType; }
    public void setValueType(String valueType) { this.valueType = valueType; }
    public Operator getOperator() { return operator != null ? operator : Operator.EQ; }
    public void setOperator(Operator operator) { this.operator = operator; }
    public boolean isNotEqual() { return notEqual != null && notEqual; }
    public void setNotEqual(Boolean notEqual) { this.notEqual = notEqual; }
}