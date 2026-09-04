package com.viettel.bccs.productcatalog.productpackagefee.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
public class ProductPackageFeeResponse implements Serializable {

    @Schema(description = "ID phí gói sản phẩm")
    @Min(value = 1, message = "productPackageFeeId phải >= 1")
    @Max(value = 9999999999L, message = "productPackageFeeId vượt quá độ dài cột (precision 10)")
    private Long productPackageFeeId;

    @Schema(description = "ID gói sản phẩm")
    @Min(value = 1, message = "productPackageId phải >= 1")
    @Max(value = 9999999999L, message = "productPackageId vượt quá độ dài cột (precision 10)")
    private Long productPackageId;

    @Schema(description = "ID chính sách giá")
    @Min(value = 1, message = "pricePolicyId phải >= 1")
    @Max(value = 9999999999L, message = "pricePolicyId vượt quá độ dài cột (precision 10)")
    private Long pricePolicyId;

    @Schema(description = "ID loại giá")
    @Min(value = 1, message = "priceTypeId phải >= 1")
    @Max(value = 9999999999L, message = "priceTypeId vượt quá độ dài cột (precision 10)")
    private Long priceTypeId;

    @Schema(description = "Mã phí")
    @Size(max = 50, message = "code tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'")
    private String code;

    @Schema(description = "Tên phí")
    @Size(max = 50, message = "name tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Mô tả")
    @Size(max = 512, message = "description tối đa 512 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "description không được chứa ký tự điều khiển")
    private String description;

    @Schema(description = "Trạng thái", example = "1")
    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Giá")
    @DecimalMin(value = "0", message = "price phải >= 0")
    @DecimalMax(value = "9999999999", message = "price vượt quá độ dài cột (precision 10)")
    private BigDecimal price;

    @Schema(description = "VAT")
    @DecimalMin(value = "0", message = "vat phải >= 0")
    @DecimalMax(value = "9999999999", message = "vat vượt quá độ dài cột (precision 10)")
    private BigDecimal vat;

    @Schema(description = "Ngày hiệu lực")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hạn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date expireDatetime;

    @Schema(description = "Ngày tạo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createDatetime;

    @Schema(description = "Ngày cập nhật")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date updateDatetime;

    @Schema(description = "Độ ưu tiên")
    @Min(value = 0, message = "priority phải >= 0")
    @Max(value = 99, message = "priority vượt quá độ dài cột (precision 2)")
    private Long priority;

    @Size(min = 1, max = 1, message = "effectType đúng 1 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1}$", message = "effectType không được chứa ký tự điều khiển")
    private String effectType;

    @Size(max = 50, message = "cronExpression tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "cronExpression không được chứa ký tự điều khiển")
    private String cronExpression;

    @Size(max = 50, message = "realStep tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "realStep không được chứa ký tự điều khiển")
    private String realStep;

    @Size(max = 50, message = "revenueObj tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "revenueObj không được chứa ký tự điều khiển")
    private String revenueObj;

    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Schema(description = "ID file đính kèm")
    @Min(value = 1, message = "fileAttachmentId phải >= 1")
    @Max(value = 9999999999L, message = "fileAttachmentId vượt quá độ dài cột (precision 10)")
    private Long fileAttachmentId;

    @Schema(description = "Phân phối")
    @Min(value = 0, message = "distribute phải >= 0")
    @Max(value = 99, message = "distribute vượt quá độ dài cột (precision 2)")
    private Long distribute;

    @Schema(description = "Số vật liệu SAP")
    @Min(value = 1, message = "sapMaterialNumber phải >= 1")
    @Max(value = 9999999999L, message = "sapMaterialNumber vượt quá độ dài cột (precision 10)")
    private Long sapMaterialNumber;
}