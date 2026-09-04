package com.viettel.bccs.productcatalog.productpackagecharuse.dto.response;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
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

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPackageCharUseDTO implements Serializable {

    @Schema(description = "ID thuộc tính sản phẩm trong gói")
    @Min(value = 1, message = "productPackageCharUseId phải >= 1")
    @Max(value = 9999999999L, message = "productPackageCharUseId vượt quá độ dài cột (precision 10)")
    private Long productPackageCharUseId;

    @Schema(description = "ID gói sản phẩm")
    @Min(value = 1, message = "productPackageId phải >= 1")
    @Max(value = 9999999999L, message = "productPackageId vượt quá độ dài cột (precision 10)")
    private Long productPackageId;

    @Schema(description = "ID thuộc tính sản phẩm")
    @Min(value = 1, message = "productSpecCharId phải >= 1")
    @Max(value = 9999999999L, message = "productSpecCharId vượt quá độ dài cột (precision 10)")
    private Long productSpecCharId;

    @Schema(description = "ID giá trị thuộc tính sản phẩm")
    @Min(value = 1, message = "productSpecCharValueId phải >= 1")
    @Max(value = 9999999999L, message = "productSpecCharValueId vượt quá độ dài cột (precision 10)")
    private Long productSpecCharValueId;

    @Schema(description = "Trạng thái", example = "1")
    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Giá trị cụ thể")
    @Size(max = 50, message = "specificValue tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "specificValue không được chứa ký tự điều khiển")
    private String specificValue;

    @Schema(description = "Giới hạn")
    @Min(value = 0, message = "limited phải >= 0")
    @Max(value = 9, message = "limited vượt quá độ dài cột (precision 1)")
    private Long limited;

    @Schema(description = "Mô tả")
    @Size(max = 100, message = "description tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "description không được chứa ký tự điều khiển")
    private String description;

    @Schema(description = "Ngày tạo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createDatetime;

    @Schema(description = "Ngày hiệu lực")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hạn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date expireDatetime;

    @Schema(description = "Ngày cập nhật")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date updateDatetime;

    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;
}