package com.viettel.bccs.productcatalog.productpackage.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Schema(description = "Thông tin gói sản phẩm trong offer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PackageOfferDTO implements Serializable {

    @Schema(description = "ID offer trong gói")
    @JsonProperty(value = "PROD_PACK_OFFER_ID")
    private Long prodPackOfferId;

    @Schema(description = "ID sản phẩm")
    @JsonProperty(value = "PRODUCT_OFFERING_ID")
    private Long productOfferingId;

    @Schema(description = "ID loại sản phẩm trong gói")
    @JsonProperty(value = "PROD_PACK_TYPE_ID")
    private Long prodPackTypeId;

    @Schema(description = "ID giá sản phẩm")
    @JsonProperty(value = "PRODUCT_OFFER_PRICE_ID")
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
}