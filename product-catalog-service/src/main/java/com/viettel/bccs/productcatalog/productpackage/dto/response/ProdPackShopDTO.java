package com.viettel.bccs.productcatalog.productpackage.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Schema(description = "Cửa hàng trong gói sản phẩm")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProdPackShopDTO implements Serializable {

    // === ID fields ===
    @Schema(description = "ID cửa hàng trong gói")
    @JsonProperty(value = "PROD_PACK_SHOP_ID")
    private Long prodPackShopId;

    @Schema(description = "ID loại sản phẩm trong gói")
    @JsonProperty(value = "PROD_PACK_TYPE_ID")
    private Long prodPackTypeId;

    @Schema(description = "ID cửa hàng")
    @JsonProperty(value = "SHOP_ID")
    private Long shopId;

    @Schema(description = "ID loại sản phẩm")
    @JsonProperty(value = "PRODUCT_OFFER_TYPE_ID")
    private Long productOfferTypeId;

    // === Basic info ===
    private String shopCode;
    private String status;
    private String productPackageCode;

    // === Date fields ===
    @Schema(description = "Ngày tạo")
    private Date createDatetime;

    @Schema(description = "Ngày cập nhật")
    private Date updateDatetime;

    // === User ===
    private String createUser;
    private String updateUser;

    // === Exclude list ===
    @Builder.Default
    private List<Long> excluseIdList = new ArrayList<>();
}