package com.viettel.bccs.productcatalog.productpackage.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private Long prodPackOfferId;

    @Schema(description = "ID sản phẩm")
    private Long productOfferingId;

    @Schema(description = "ID loại sản phẩm trong gói")
    private Long prodPackTypeId;

    @Schema(description = "ID giá sản phẩm")
    private Long productOfferPriceId;

    @Schema(description = "Trạng thái")
    private String status;

    @Schema(description = "Phương thức cung cấp")
    private String supplyMethod;

    @Schema(description = "Bắt buộc")
    private String isMandatory;

    @Schema(description = "Ngày tạo")
    private Date createDatetime;

    private String createUser;
    private String updateUser;

    @Schema(description = "Ngày cập nhật")
    private Date updateDatetime;

    @Schema(description = "Số lượng offer")
    private Long numOffer;

    private String description;

    private String newOrSold;

    @Schema(description = "Ngày hiệu lực")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hạn")
    private Date expireDatetime;

    @Schema(description = "Hiển thị/ẩn")
    private String showOrHide;

    private Long sapMaterialNumber;

    @Schema(description = "Mã sản phẩm (product_offering.code)")
    private String offerCode;

    @Schema(description = "Tên sản phẩm (product_offering.name)")
    private String offerName;

    @Schema(description = "Giá (product_offer_price.price)")
    private BigDecimal price;

    @Schema(description = "VAT (product_offer_price.vat)")
    private BigDecimal vat;

    @Schema(description = "Mã mô hình hạch toán (product_offering.accounting_model_code)")
    private String accountingModelCode;

    @Schema(description = "Tên mô hình hạch toán (product_offering.accounting_model_name)")
    private String accountingModelName;
}