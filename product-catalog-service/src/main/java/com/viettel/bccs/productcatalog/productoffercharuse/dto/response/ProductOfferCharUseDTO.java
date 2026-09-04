package com.viettel.bccs.productcatalog.productoffercharuse.dto.response;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOfferCharUseDTO {

    @Schema(description = "ID sử dụng đặc tính sản phẩm", example = "12345")
    @Min(value = 1, message = "productOfferCharUseId phải >= 1")
    @Max(value = 9999999999L, message = "productOfferCharUseId vượt quá độ dài cột (precision 10)")
    private Long productOfferCharUseId;

    @Schema(description = "Thứ tự", example = "1")
    @Min(value = 0, message = "orderChar phải >= 0")
    @Max(value = 9999999999L, message = "orderChar vượt quá độ dài cột (precision 10)")
    private Long orderChar;

    @Schema(description = "Loại", example = "1")
    @Size(max = 1, message = "type đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "type chỉ gồm chữ hoặc số")
    private String type;

    @Schema(description = "ID sản phẩm", example = "67890")
    @Min(value = 1, message = "productOfferingId phải >= 1")
    @Max(value = 9999999999L, message = "productOfferingId vượt quá độ dài cột (precision 10)")
    private Long productOfferingId;

    @Schema(description = "ID giá trị đặc tính sản phẩm", example = "111")
    @Min(value = 1, message = "productSpecCharValueId phải >= 1")
    @Max(value = 9999999999L, message = "productSpecCharValueId vượt quá độ dài cột (precision 10)")
    private Long productSpecCharValueId;

    @Schema(description = "ID đặc tính sản phẩm", example = "222")
    @Min(value = 1, message = "productSpecCharId phải >= 1")
    @Max(value = 9999999999L, message = "productSpecCharId vượt quá độ dài cột (precision 10)")
    private Long productSpecCharId;

    @Schema(description = "Người tạo", example = "admin")
    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Schema(description = "Ngày tạo")
    private Date createDatetime;

    @Schema(description = "Người cập nhật", example = "admin")
    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Schema(description = "Ngày cập nhật")
    private Date updateDatetime;

    @Schema(description = "Trạng thái", example = "1")
    @Size(max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "status chỉ gồm chữ hoặc số")
    private String status;

    @Schema(description = "Giá trị đặc biệt", example = "SPEC_VAL")
    @Size(max = 500, message = "specificValue tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "specificValue không được chứa ký tự điều khiển")
    private String specificValue;

    @Schema(description = "Ngày hiệu lực")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hạn")
    private Date expireDatetime;

    @Schema(description = "Giới hạn", example = "1")
    @Min(value = 0, message = "limited phải >= 0")
    @Max(value = 9, message = "limited vượt quá độ dài cột (precision 1)")
    private Long limited;

    @Schema(description = "Mô tả", example = "Mô tả sử dụng đặc tính")
    @Size(max = 1000, message = "description tối đa 1000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "description không được chứa ký tự điều khiển")
    private String description;
}
