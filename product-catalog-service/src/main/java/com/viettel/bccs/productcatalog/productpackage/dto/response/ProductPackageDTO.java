package com.viettel.bccs.productcatalog.productpackage.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.viettel.bccs.productcatalog.prodpackproductoffertype.dto.response.ProdPackProductOfferTypeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Schema(description = "Thông tin gói sản phẩm")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProductPackageDTO implements Serializable {

    // === ID & basic info ===
    @Schema(description = "ID gói sản phẩm")
    @JsonProperty(value = "PRODUCT_PACKAGE_ID")
    private Long productPackageId;

    @Schema(description = "Mã gói sản phẩm")
    @JsonProperty(value = "CODE")
    private String code;

    @Schema(description = "Tên gói sản phẩm")
    @JsonProperty(value = "NAME")
    private String name;

    @Schema(description = "Loại gói sản phẩm")
    @JsonProperty(value = "TYPE")
    private String type;

    @Schema(description = "Trạng thái", example = "1")
    @JsonProperty(value = "STATUS")
    private String status;

    // === Telecom & accounting ===
    @Schema(description = "ID dịch vụ viễn thông")
    @JsonProperty(value = "TELECOM_SERVICE_ID")
    private Long telecomServiceId;

    @Schema(description = "Tên dịch vụ viễn thông")
    private String telecomService;

    @Schema(description = "ID kế toán")
    @JsonProperty(value = "ACCOUNTING_ID")
    private Long accountingId;

    @Schema(description = "Mã kế toán")
    private String accountingCode;

    // === Date fields ===
    @Schema(description = "Ngày tạo")
    private Date createDatetime;

    @Schema(description = "Ngày hiệu lực")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hạn")
    private Date expireDatetime;

    @Schema(description = "Ngày cập nhật")
    private Date updateDatetime;

    @Schema(description = "Ngày bắt đầu hiệu lực (string)")
    private String strEffectDatetime;

    @Schema(description = "Ngày hết hạn (string)")
    private String strExpireDatetime;

    // === User & version ===
    private String createUser;
    private String updateUser;
    private String version;
    private String action;

    // === Description & note ===
    private String description;
    private String note;
    private String detailType;

    // === Price & goods ===
    private Long price;
    private Long vat;
    private Long numOffer;
    private Long minPrice;
    private Long maxPrice;
    private Long goodsId;
    private Long goodsTypeId;
    private String goodsCode;
    private String goodsName;
    private String unit;

    // === SAP & hierarchy ===
    private Long sapMaterialNumber;
    private String sapMaterialNumberValue;
    private String productHierarchy;
    private String invoiceDeclaration;

    // === IT & misc ===
    private String itTelcol;
    private String feeType;
    private String revenueServiceId;
    private String quanlityServiceId;
    private String saleServiceComboName;

    // === Area group ===
    private Long areaGroupId;
    private String areaGroupCode;

    // === Owner shop ===
    private Long ownerShopId;
    private String ownerShopCode;

    // === UI state & flags ===
    private Long index;
    private String errorCode;
    private String keyMes;

    @Builder.Default
    private Boolean selectedRow = false;

    private String defaultName;

    private List<ProdPackProductOfferTypeDTO> listProdPackType = new ArrayList<>();

}