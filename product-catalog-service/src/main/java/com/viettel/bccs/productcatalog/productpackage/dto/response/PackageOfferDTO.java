package com.viettel.bccs.productcatalog.productpackage.dto.response;

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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Schema(description = "Thông tin gói sản phẩm trong offer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PackageOfferDTO implements Serializable {

    @Schema(description = "ID offer trong gói")
    @Min(value = 1, message = "prodPackOfferId phải >= 1")
    @Max(value = 9999999999L, message = "prodPackOfferId vượt quá độ dài cho phép")
    private Long prodPackOfferId;

    @Schema(description = "ID sản phẩm")
    @Min(value = 1, message = "productOfferingId phải >= 1")
    @Max(value = 9999999999L, message = "productOfferingId vượt quá độ dài cho phép")
    private Long productOfferingId;

    @Schema(description = "ID loại sản phẩm trong gói")
    @Min(value = 1, message = "prodPackTypeId phải >= 1")
    @Max(value = 9999999999L, message = "prodPackTypeId vượt quá độ dài cho phép")
    private Long prodPackTypeId;

    @Schema(description = "ID giá sản phẩm")
    @Min(value = 1, message = "productOfferPriceId phải >= 1")
    @Max(value = 9999999999L, message = "productOfferPriceId vượt quá độ dài cho phép")
    private Long productOfferPriceId;

    @Schema(description = "Trạng thái")
    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Phương thức cung cấp")
    @Size(max = 50, message = "supplyMethod tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "supplyMethod không được chứa ký tự điều khiển")
    private String supplyMethod;

    @Schema(description = "Bắt buộc")
    @Size(min = 1, max = 1, message = "isMandatory đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "isMandatory chỉ nhận giá trị 0 hoặc 1")
    private String isMandatory;

    @Schema(description = "Ngày tạo")
    private Date createDatetime;

    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Schema(description = "Ngày cập nhật")
    private Date updateDatetime;

    @Schema(description = "Số lượng offer")
    @Min(value = 0, message = "numOffer phải >= 0")
    @Max(value = 9999999999L, message = "numOffer vượt quá độ dài cho phép")
    private Long numOffer;

    @Size(max = 512, message = "description tối đa 512 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "description không được chứa ký tự điều khiển")
    private String description;

    @Size(max = 20, message = "newOrSold tối đa 20 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,20}$", message = "newOrSold không được chứa ký tự điều khiển")
    private String newOrSold;

    @Schema(description = "Ngày hiệu lực")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hạn")
    private Date expireDatetime;

    @Schema(description = "Hiển thị/ẩn")
    @Size(min = 1, max = 1, message = "showOrHide đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "showOrHide chỉ nhận giá trị 0 hoặc 1")
    private String showOrHide;

    @Min(value = 1, message = "sapMaterialNumber phải >= 1")
    @Max(value = 9999999999L, message = "sapMaterialNumber vượt quá độ dài cho phép")
    private Long sapMaterialNumber;

    @Schema(description = "Mã sản phẩm (product_offering.code)")
    @Size(max = 50, message = "offerCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "offerCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String offerCode;

    @Schema(description = "Tên sản phẩm (product_offering.name)")
    @Size(max = 500, message = "offerName tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "offerName không được chứa ký tự điều khiển")
    private String offerName;

    @Schema(description = "Giá (product_offer_price.price)")
    @DecimalMin(value = "0", message = "price phải >= 0")
    @DecimalMax(value = "99999999999999999999", message = "price vượt quá độ dài cho phép")
    private BigDecimal price;

    @Schema(description = "VAT (product_offer_price.vat)")
    @DecimalMin(value = "0", message = "vat phải >= 0")
    @DecimalMax(value = "99999999999999999999", message = "vat vượt quá độ dài cho phép")
    private BigDecimal vat;

    @Schema(description = "Mã mô hình hạch toán (product_offering.accounting_model_code)")
    @Size(max = 50, message = "accountingModelCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "accountingModelCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String accountingModelCode;

    @Schema(description = "Tên mô hình hạch toán (product_offering.accounting_model_name)")
    @Size(max = 500, message = "accountingModelName tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "accountingModelName không được chứa ký tự điều khiển")
    private String accountingModelName;
}