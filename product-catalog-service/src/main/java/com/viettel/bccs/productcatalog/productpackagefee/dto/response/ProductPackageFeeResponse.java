package com.viettel.bccs.productcatalog.productpackagefee.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPackageFeeResponse implements Serializable {

    @Schema(description = "ID phí gói sản phẩm")
    @JsonProperty(value = "PRODUCT_PACKAGE_FEE_ID")
    private Long productPackageFeeId;

    @Schema(description = "ID gói sản phẩm")
    @JsonProperty(value = "PRODUCT_PACKAGE_ID")
    private Long productPackageId;

    @Schema(description = "ID chính sách giá")
    @JsonProperty(value = "PRICE_POLICY_ID")
    private Long pricePolicyId;

    @Schema(description = "ID loại giá")
    @JsonProperty(value = "PRICE_TYPE_ID")
    private Long priceTypeId;

    @Schema(description = "Mã phí")
    @JsonProperty(value = "CODE")
    private String code;

    @Schema(description = "Tên phí")
    @JsonProperty(value = "NAME")
    private String name;

    @Schema(description = "Mô tả")
    private String description;

    @Schema(description = "Trạng thái", example = "1")
    @JsonProperty(value = "STATUS")
    private String status;

    @Schema(description = "Giá")
    private BigDecimal price;

    @Schema(description = "VAT")
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
    private Long priority;

    private String effectType;
    private String cronExpression;
    private String realStep;
    private String revenueObj;
    private String createUser;
    private String updateUser;

    @Schema(description = "ID file đính kèm")
    private Long fileAttachmentId;

    @Schema(description = "Phân phối")
    private Long distribute;

    @Schema(description = "Số vật liệu SAP")
    private Long sapMaterialNumber;
}