package com.viettel.bccs.productcatalog.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubTypeDTO {

    @Schema(description = "Tên loại thuê bao (1: Trả sau, 2: Trả trước)", example = "Trả sau")
    @Size(max = 50, message = "subTypeName tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "subTypeName không được chứa ký tự điều khiển")
    private String subTypeName;

    @Schema(description = "Giá trị loại thuê bao (1: Trả sau, 2: Trả trước)", example = "1")
    @Size(max = 1, message = "subTypeValue đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "subTypeValue chỉ gồm chữ hoặc số")
    private String subTypeValue;

    public SubTypeDTO() {
    }

    public SubTypeDTO(String subTypeName, String subTypeValue) {
        this.subTypeName = subTypeName;
        this.subTypeValue = subTypeValue;
    }
}
