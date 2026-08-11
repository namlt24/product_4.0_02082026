package com.viettel.bccs.productcatalog.productpackagefee.dto.response;

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

import java.io.Serializable;
import java.util.Date;

@Schema(description = "Phí gói sản phẩm")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProductPackageFeeDTO implements Serializable {

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

    @Size(max = 50, message = "code tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'")
    private String code;

    @Size(max = 50, message = "name tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Size(max = 512, message = "description tối đa 512 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "description không được chứa ký tự điều khiển")
    private String description;

    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Size(max = 50, message = "packageCode tối đa 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]{0,50}$", message = "packageCode chỉ gồm chữ, số hoặc '_'")
    private String packageCode;

    @Size(max = 500, message = "packageName tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "packageName không được chứa ký tự điều khiển")
    private String packageName;

    @Min(value = 0, message = "price phải >= 0")
    @Max(value = 9999999999L, message = "price vượt quá độ dài cột (precision 10)")
    private Long price;

    @Min(value = 0, message = "vat phải >= 0")
    @Max(value = 9999999999L, message = "vat vượt quá độ dài cột (precision 10)")
    private Long vat;

    @Size(max = 50, message = "feeSuite tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "feeSuite không được chứa ký tự điều khiển")
    private String feeSuite;

    @Size(max = 20, message = "modeDistribute tối đa 20 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,20}$", message = "modeDistribute không được chứa ký tự điều khiển")
    private String modeDistribute;

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

    private Date reasonEffectDate;
    private Date reasonExpireDate;

    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Size(min = 1, max = 1, message = "effectType đúng 1 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1}$", message = "effectType không được chứa ký tự điều khiển")
    private String effectType;

    @Size(max = 50, message = "realStep tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "realStep không được chứa ký tự điều khiển")
    private String realStep;

    @Size(max = 500, message = "realStepName tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "realStepName không được chứa ký tự điều khiển")
    private String realStepName;

    @Size(max = 50, message = "revenueObj tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "revenueObj không được chứa ký tự điều khiển")
    private String revenueObj;

    @Size(max = 500, message = "revenueObjName tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "revenueObjName không được chứa ký tự điều khiển")
    private String revenueObjName;

    @Min(value = 0, message = "priority phải >= 0")
    @Max(value = 99, message = "priority vượt quá độ dài cột (precision 2)")
    private Short priority;

    @Size(max = 50, message = "cronExpression tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "cronExpression không được chứa ký tự điều khiển")
    private String cronExpression;

    @Size(max = 500, message = "pricePolicy tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "pricePolicy không được chứa ký tự điều khiển")
    private String pricePolicy;

    @Size(max = 500, message = "priceType tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "priceType không được chứa ký tự điều khiển")
    private String priceType;

    @Min(value = 0, message = "distribute phải >= 0")
    @Max(value = 99, message = "distribute vượt quá độ dài cột (precision 2)")
    private Long distribute;

    @Size(max = 20, message = "action tối đa 20 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,20}$", message = "action không được chứa ký tự điều khiển")
    private String action;

    @Size(max = 50, message = "productPackageCode tối đa 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]{0,50}$", message = "productPackageCode chỉ gồm chữ, số hoặc '_'")
    private String productPackageCode;

    @Size(max = 50, message = "reasonCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "reasonCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String reasonCode;

    @Size(max = 500, message = "reasonName tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "reasonName không được chứa ký tự điều khiển")
    private String reasonName;

    @Min(value = 1, message = "fileAttachmentId phải >= 1")
    @Max(value = 9999999999L, message = "fileAttachmentId vượt quá độ dài cột (precision 10)")
    private Long fileAttachmentId;

    @Min(value = 1, message = "sapMaterialNumber phải >= 1")
    @Max(value = 9999999999L, message = "sapMaterialNumber vượt quá độ dài cho phép")
    private Long sapMaterialNumber;

    @Size(max = 500, message = "sapMaterialName tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "sapMaterialName không được chứa ký tự điều khiển")
    private String sapMaterialName;

    @Size(max = 100, message = "productHierarchy tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "productHierarchy không được chứa ký tự điều khiển")
    private String productHierarchy;

    @Size(max = 20, message = "typeAllocation tối đa 20 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,20}$", message = "typeAllocation không được chứa ký tự điều khiển")
    private String typeAllocation;

    @Size(max = 10, message = "numMonthAllocation tối đa 10 ký tự")
    @Pattern(regexp = "^[0-9]{0,10}$", message = "numMonthAllocation chỉ gồm chữ số")
    private String numMonthAllocation;

    @Builder.Default
    private boolean selectedRow = false;
}