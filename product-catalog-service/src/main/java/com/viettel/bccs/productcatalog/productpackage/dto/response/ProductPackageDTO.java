package com.viettel.bccs.productcatalog.productpackage.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.viettel.bccs.productcatalog.prodpackproductoffertype.dto.response.ProdPackProductOfferTypeDTO;
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
    @Min(value = 1, message = "productPackageId phải >= 1")
    @Max(value = 9999999999L, message = "productPackageId vượt quá độ dài cột (precision 10)")
    private Long productPackageId;

    @Schema(description = "Mã gói sản phẩm")
    @Size(max = 50, message = "code tối đa 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]{0,50}$", message = "code chỉ gồm chữ, số hoặc '_'")
    private String code;

    @Schema(description = "Tên gói sản phẩm")
    @Size(max = 500, message = "name tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Loại gói sản phẩm")
    @Size(min = 1, max = 1, message = "type đúng 1 ký tự")
    @Pattern(regexp = "^[12]$", message = "type chỉ nhận giá trị 1 (hàng hoá) hoặc 2 (dịch vụ bán hàng)")
    private String type;

    @Schema(description = "Trạng thái", example = "1")
    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    // === Telecom & accounting ===
    @Schema(description = "ID dịch vụ viễn thông")
    @Min(value = 1, message = "telecomServiceId phải >= 1")
    @Max(value = 9999999999L, message = "telecomServiceId vượt quá độ dài cột (precision 10)")
    private Long telecomServiceId;

    @Schema(description = "Tên dịch vụ viễn thông")
    @Size(max = 500, message = "telecomService tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "telecomService không được chứa ký tự điều khiển")
    private String telecomService;

    @Schema(description = "ID kế toán")
    @Min(value = 1, message = "accountingId phải >= 1")
    @Max(value = 9999999999L, message = "accountingId vượt quá độ dài cột (precision 10)")
    private Long accountingId;

    @Schema(description = "Mã kế toán")
    @Size(max = 50, message = "accountingCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "accountingCode chỉ gồm chữ, số, '_' hoặc '-'")
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
    @Size(max = 30, message = "strEffectDatetime tối đa 30 ký tự")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}([ T]\\d{2}:\\d{2}:\\d{2})?$", message = "strEffectDatetime phải theo định dạng yyyy-MM-dd hoặc yyyy-MM-dd HH:mm:ss")
    private String strEffectDatetime;

    @Schema(description = "Ngày hết hạn (string)")
    @Size(max = 30, message = "strExpireDatetime tối đa 30 ký tự")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}([ T]\\d{2}:\\d{2}:\\d{2})?$", message = "strExpireDatetime phải theo định dạng yyyy-MM-dd hoặc yyyy-MM-dd HH:mm:ss")
    private String strExpireDatetime;

    // === User & version ===
    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Size(max = 50, message = "version tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "version không được chứa ký tự điều khiển")
    private String version;

    @Size(max = 20, message = "action tối đa 20 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,20}$", message = "action không được chứa ký tự điều khiển")
    private String action;

    // === Description & note ===
    @Size(max = 512, message = "description tối đa 512 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "description không được chứa ký tự điều khiển")
    private String description;

    @Size(max = 1000, message = "note tối đa 1000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "note không được chứa ký tự điều khiển")
    private String note;

    @Size(max = 50, message = "detailType tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "detailType không được chứa ký tự điều khiển")
    private String detailType;

    // === Price & goods ===
    @Min(value = 0, message = "price phải >= 0")
    @Max(value = 9999999999L, message = "price vượt quá độ dài cho phép")
    private Long price;

    @Min(value = 0, message = "vat phải >= 0")
    @Max(value = 9999999999L, message = "vat vượt quá độ dài cho phép")
    private Long vat;

    @Min(value = 0, message = "numOffer phải >= 0")
    @Max(value = 9999999999L, message = "numOffer vượt quá độ dài cho phép")
    private Long numOffer;

    @Min(value = 0, message = "minPrice phải >= 0")
    @Max(value = 9999999999L, message = "minPrice vượt quá độ dài cho phép")
    private Long minPrice;

    @Min(value = 0, message = "maxPrice phải >= 0")
    @Max(value = 9999999999L, message = "maxPrice vượt quá độ dài cho phép")
    private Long maxPrice;

    @Min(value = 1, message = "goodsId phải >= 1")
    @Max(value = 9999999999L, message = "goodsId vượt quá độ dài cho phép")
    private Long goodsId;

    @Min(value = 1, message = "goodsTypeId phải >= 1")
    @Max(value = 9999999999L, message = "goodsTypeId vượt quá độ dài cho phép")
    private Long goodsTypeId;

    @Size(max = 50, message = "goodsCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "goodsCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String goodsCode;

    @Size(max = 500, message = "goodsName tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "goodsName không được chứa ký tự điều khiển")
    private String goodsName;

    @Size(max = 10, message = "unit tối đa 10 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,10}$", message = "unit không được chứa ký tự điều khiển")
    private String unit;

    // === SAP & hierarchy ===
    @Min(value = 1, message = "sapMaterialNumber phải >= 1")
    @Max(value = 9999999999L, message = "sapMaterialNumber vượt quá độ dài cột (precision 10)")
    private Long sapMaterialNumber;

    @Size(max = 100, message = "sapMaterialNumberValue tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "sapMaterialNumberValue không được chứa ký tự điều khiển")
    private String sapMaterialNumberValue;

    @Size(max = 100, message = "productHierarchy tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "productHierarchy không được chứa ký tự điều khiển")
    private String productHierarchy;

    @Size(max = 100, message = "invoiceDeclaration tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "invoiceDeclaration không được chứa ký tự điều khiển")
    private String invoiceDeclaration;

    // === IT & misc ===
    @Size(max = 6, message = "itTelcol tối đa 6 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,6}$", message = "itTelcol không được chứa ký tự điều khiển")
    private String itTelcol;

    @Size(min = 1, max = 1, message = "feeType đúng 1 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1}$", message = "feeType không được chứa ký tự điều khiển")
    private String feeType;

    @Size(max = 50, message = "revenueServiceId tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "revenueServiceId chỉ gồm chữ, số, '_' hoặc '-'")
    private String revenueServiceId;

    @Size(max = 50, message = "quanlityServiceId tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "quanlityServiceId chỉ gồm chữ, số, '_' hoặc '-'")
    private String quanlityServiceId;

    @Size(max = 500, message = "saleServiceComboName tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "saleServiceComboName không được chứa ký tự điều khiển")
    private String saleServiceComboName;

    // === Area group ===
    @Min(value = 1, message = "areaGroupId phải >= 1")
    @Max(value = 9999999999L, message = "areaGroupId vượt quá độ dài cột (precision 10)")
    private Long areaGroupId;

    @Size(max = 50, message = "areaGroupCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "areaGroupCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String areaGroupCode;

    // === Owner shop ===
    @Min(value = 1, message = "ownerShopId phải >= 1")
    @Max(value = 9999999999L, message = "ownerShopId vượt quá độ dài cột (precision 10)")
    private Long ownerShopId;

    @Size(max = 50, message = "ownerShopCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "ownerShopCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String ownerShopCode;

    // === UI state & flags ===
    @Min(value = 0, message = "index phải >= 0")
    @Max(value = 9999999999L, message = "index vượt quá độ dài cho phép")
    private Long index;

    @Size(max = 50, message = "errorCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "errorCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String errorCode;

    @Size(max = 200, message = "keyMes tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "keyMes không được chứa ký tự điều khiển")
    private String keyMes;

    @Builder.Default
    private Boolean selectedRow = false;

    @Size(max = 500, message = "defaultName tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "defaultName không được chứa ký tự điều khiển")
    private String defaultName;

    @Size(max = 500, message = "listProdPackType tối đa 500 phần tử")
    private List<ProdPackProductOfferTypeDTO> listProdPackType = new ArrayList<>();

}