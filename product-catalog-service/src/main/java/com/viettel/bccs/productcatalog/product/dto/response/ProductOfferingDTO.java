package com.viettel.bccs.productcatalog.product.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productofferrelation.dto.response.ProductOfferRelationResponse;
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
import java.util.List;

@Schema(description = "Thông tin sản phẩm gói cước")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProductOfferingDTO implements Serializable {

    @Schema(description = "Mã sản phẩm", example = "PACKAGE_001")
    @Size(max = 50, message = "code tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'")
    private String code;

    @Schema(description = "Tên sản phẩm", example = "Gói cước data 50GB")
    @Size(max = 500, message = "name tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "ID sản phẩm", example = "12345")
    @Min(value = 1, message = "productOfferingId phải >= 1")
    @Max(value = 9999999999L, message = "productOfferingId vượt quá độ dài cột (precision 10)")
    private Long productOfferingId;

    @Schema(description = "ID loại sản phẩm", example = "1")
    @Min(value = 1, message = "productOfferTypeId phải >= 1")
    @Max(value = 9999999999L, message = "productOfferTypeId vượt quá độ dài cột (precision 10)")
    private Long productOfferTypeId;

    @Schema(description = "ID dịch vụ viễn thông", example = "1")
    @Min(value = 1, message = "telecomServiceId phải >= 1")
    @Max(value = 9999999999L, message = "telecomServiceId vượt quá độ dài cột (precision 10)")
    private Long telecomServiceId;

    @Schema(description = "Trạng thái", example = "1")
    @Size(max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "status chỉ gồm chữ hoặc số")
    private String status;

    @Schema(description = "Tên loại sản phẩm", example = "Gói cước data")
    @Size(max = 200, message = "productTypeName tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "productTypeName không được chứa ký tự điều khiển")
    private String productTypeName;

    @Schema(description = "ID đặc tả sản phẩm")
    @Min(value = 1, message = "productSpecId phải >= 1")
    @Max(value = 9999999999L, message = "productSpecId vượt quá độ dài cột (precision 10)")
    private Long productSpecId;

    @Schema(description = "Loại thuê bao (1: trả sau, 2: trả trước)")
    @Size(max = 1, message = "subType đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "subType chỉ gồm chữ hoặc số")
    private String subType;

    @Schema(description = "Mô tả sản phẩm")
    @Size(max = 512, message = "description tối đa 512 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "description không được chứa ký tự điều khiển")
    private String description;

    @Schema(description = "Phiên bản")
    @Size(max = 50, message = "version tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "version không được chứa ký tự điều khiển")
    private String version;

    @Schema(description = "Đơn vị tính")
    @Size(max = 10, message = "unit tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "unit chỉ gồm chữ, số, '_' hoặc '-'")
    private String unit;

    @Schema(description = "Loại thiết bị")
    @Size(max = 20, message = "deviceType tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "deviceType chỉ gồm chữ, số, '_' hoặc '-'")
    private String deviceType;

    @Schema(description = "ID mục đích sử dụng")
    @Size(max = 100, message = "usageId tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "usageId chỉ gồm chữ, số, '_' hoặc '-'")
    private String usageId;

    @Schema(description = "Là gói bundle")
    @Size(max = 10, message = "isBundle tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "isBundle chỉ gồm chữ, số, '_' hoặc '-'")
    private String isBundle;

    @Schema(description = "Tên dịch vụ viễn thông")
    @Size(max = 200, message = "telecomServiceName tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "telecomServiceName không được chứa ký tự điều khiển")
    private String telecomServiceName;

    @Schema(description = "Tên loại sản phẩm")
    @Size(max = 200, message = "productOfferTypeName tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "productOfferTypeName không được chứa ký tự điều khiển")
    private String productOfferTypeName;

    @Schema(description = "Tên đặc tả sản phẩm")
    @Size(max = 200, message = "productSpecName tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "productSpecName không được chứa ký tự điều khiển")
    private String productSpecName;

    @Schema(description = "Mã kế toán")
    @Size(max = 50, message = "accountingCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "accountingCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String accountingCode;

    @Schema(description = "Mã mô hình kế toán")
    @Size(max = 50, message = "accountingModelCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "accountingModelCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String accountingModelCode;

    @Schema(description = "Tên mô hình kế toán")
    @Size(max = 100, message = "accountingModelName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "accountingModelName không được chứa ký tự điều khiển")
    private String accountingModelName;

    @Schema(description = "Tên kế toán")
    @Size(max = 100, message = "accountingName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "accountingName không được chứa ký tự điều khiển")
    private String accountingName;

    @Schema(description = "Kiểm tra đặt cọc")
    @Min(value = 0, message = "checkDeposit phải >= 0")
    @Max(value = 9, message = "checkDeposit vượt quá độ dài cột (precision 1)")
    private Short checkDeposit;

    @Schema(description = "Kiểm tra serial")
    @Min(value = 0, message = "checkSerial phải >= 0")
    @Max(value = 9, message = "checkSerial vượt quá độ dài cột (precision 1)")
    private Short checkSerial;

    @Schema(description = "Là gói demo")
    @Min(value = 0, message = "isDemo phải >= 0")
    @Max(value = 9, message = "isDemo vượt quá độ dài cột (precision 1)")
    private Short isDemo;

    @Schema(description = "Bộ thu phát")
    @Min(value = 0, message = "transceiver phải >= 0")
    @Max(value = 9, message = "transceiver vượt quá độ dài cột (precision 1)")
    private Short transceiver;

    @Schema(description = "Loại mô hình kho")
    @Min(value = 0, message = "stockModelType phải >= 0")
    @Max(value = 9, message = "stockModelType vượt quá độ dài cột (precision 1)")
    private Short stockModelType;

    @Schema(description = "Thời lượng demo (ngày)")
    @Min(value = 0, message = "demoDuration phải >= 0")
    @Max(value = 9999999999L, message = "demoDuration vượt quá độ dài cột (precision 10)")
    private Long demoDuration;

    @Schema(description = "Ngày hiệu lực")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hạn")
    private Date expireDatetime;

    @Schema(description = "Ngày tạo")
    private Date createDatetime;

    @Schema(description = "Ngày cập nhật")
    private Date updateDatetime;

    @Schema(description = "Người tạo")
    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Schema(description = "Người cập nhật")
    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Schema(description = "ID cửa hàng sở hữu")
    @Min(value = 1, message = "ownerShopId phải >= 1")
    @Max(value = 9999999999L, message = "ownerShopId vượt quá độ dài cột (precision 10)")
    private Long ownerShopId;

    @Schema(description = "Số vật liệu SAP")
    @DecimalMin(value = "0", message = "sapMaterialNumber phải >= 0")
    @DecimalMax(value = "999999999", message = "sapMaterialNumber vượt quá độ dài cột (precision 9)")
    private BigDecimal sapMaterialNumber;

    @Schema(description = "Mã vùng")
    @Size(max = 200, message = "areaCode tối đa 200 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,200}$", message = "areaCode chỉ gồm chữ và số")
    private String areaCode;

    @Schema(description = "Mã cửa hàng")
    @Size(max = 50, message = "shopCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "shopCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String shopCode;

    @Schema(description = "Tên cửa hàng")
    @Size(max = 200, message = "shopName tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "shopName không được chứa ký tự điều khiển")
    private String shopName;

    @Schema(description = "ID loại kênh")
    @Min(value = 1, message = "channelTypeId phải >= 1")
    @Max(value = 9999999999L, message = "channelTypeId vượt quá độ dài cột (precision 10)")
    private Long channelTypeId;

    @Schema(description = "Tên loại kênh")
    @Size(max = 200, message = "channelTypeName tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "channelTypeName không được chứa ký tự điều khiển")
    private String channelTypeName;

    @Schema(description = "Công nghệ (4G/5G/FTTH)")
    @Size(max = 20, message = "technology tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "technology chỉ gồm chữ, số, '_' hoặc '-'")
    private String technology;

    @Schema(description = "Loại hạ tầng")
    @Size(max = 20, message = "infraType tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "infraType chỉ gồm chữ, số, '_' hoặc '-'")
    private String infraType;

    @Schema(description = "ID loại giá")
    @Size(max = 50, message = "priceTypeId tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "priceTypeId chỉ gồm chữ, số, '_' hoặc '-'")
    private String priceTypeId;

    @Schema(description = "ID chính sách giá")
    @Size(max = 50, message = "pricePolicyId tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "pricePolicyId chỉ gồm chữ, số, '_' hoặc '-'")
    private String pricePolicyId;

    @Schema(description = "Loại chi tiết")
    @Size(max = 20, message = "detailType tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "detailType chỉ gồm chữ, số, '_' hoặc '-'")
    private String detailType;

    @Schema(description = "Tự động điền")
    @Size(max = 10, message = "autoFill tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "autoFill chỉ gồm chữ, số, '_' hoặc '-'")
    private String autoFill;

    @Schema(description = "Loại sản phẩm SAP")
    @Size(max = 1, message = "sapProductType đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "sapProductType chỉ gồm chữ hoặc số")
    private String sapProductType;

    @Schema(description = "Phân cấp sản phẩm")
    @Size(max = 200, message = "productHierarchy tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "productHierarchy không được chứa ký tự điều khiển")
    private String productHierarchy;

    @Schema(description = "Trả hàng khi hủy")
    @Size(max = 2, message = "returnStock tối đa 2 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,2}$", message = "returnStock chỉ gồm chữ và số")
    private String returnStock;

    @Schema(description = "Loại cửa hàng")
    @Size(max = 20, message = "shopType tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "shopType chỉ gồm chữ, số, '_' hoặc '-'")
    private String shopType;

    @Schema(description = "Kiểm tra tồn kho cửa hàng")
    @Size(max = 1, message = "checkShopStock đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "checkShopStock chỉ gồm chữ hoặc số")
    private String checkShopStock;

    @Schema(description = "Kiểm tra tồn kho nhân viên")
    @Size(max = 1, message = "checkStaffStock đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "checkStaffStock chỉ gồm chữ hoặc số")
    private String checkStaffStock;

    @Schema(description = "Tính năng sản phẩm")
    @Size(max = 500, message = "offerFeature tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "offerFeature không được chứa ký tự điều khiển")
    private String offerFeature;

    @Schema(description = "Loại FTTH")
    @Size(max = 20, message = "ftthType tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "ftthType chỉ gồm chữ, số, '_' hoặc '-'")
    private String ftthType;

    @Schema(description = "Mã nhân viên")
    @Size(max = 50, message = "staffCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "staffCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String staffCode;

    @Schema(description = "Ghi chú")
    @Size(max = 500, message = "note tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "note không được chứa ký tự điều khiển")
    private String note;

    @Schema(description = "ID đặc tả")
    @Min(value = 1, message = "specificationId phải >= 1")
    @Max(value = 9999999999L, message = "specificationId vượt quá độ dài cột (precision 10)")
    private Long specificationId;

    @Schema(description = "Giá niêm yết")
    @Min(value = 0, message = "listingPrice phải >= 0")
    @Max(value = 999999999999999L, message = "listingPrice vượt quá độ dài cột (precision 15)")
    private Long listingPrice;

    @Schema(description = "Tốc độ tải xuống (Mbps)")
    @Min(value = 0, message = "downloadSpeed phải >= 0")
    @Max(value = 999999999L, message = "downloadSpeed vượt quá độ dài cột (precision 9)")
    private Long downloadSpeed;

    @Schema(description = "Tốc độ tải lên (Mbps)")
    @Min(value = 0, message = "uploadSpeed phải >= 0")
    @Max(value = 999999999L, message = "uploadSpeed vượt quá độ dài cột (precision 9)")
    private Long uploadSpeed;

    @Schema(description = "Số kênh")
    @Min(value = 0, message = "numberOfChannel phải >= 0")
    @Max(value = 9999, message = "numberOfChannel vượt quá độ dài cột (precision 4)")
    private Long numberOfChannel;

    @Schema(description = "Tốc độ tối đa (Mbps)")
    @Min(value = 0, message = "speedUpto phải >= 0")
    @Max(value = 999999999L, message = "speedUpto vượt quá độ dài cột (precision 9)")
    private Long speedUpto;

    @Schema(description = "Giá sản phẩm")
    @Min(value = 0, message = "price phải >= 0")
    @Max(value = 999999999999999L, message = "price vượt quá độ dài cột (precision 15)")
    private Long price;

    @Schema(description = "VAT")
    @Min(value = 0, message = "vat phải >= 0")
    @Max(value = 999999999999999L, message = "vat vượt quá độ dài cột (precision 15)")
    private Long vat;

    @Schema(description = "ID giá sản phẩm")
    @Min(value = 1, message = "productOfferPriceId phải >= 1")
    @Max(value = 9999999999L, message = "productOfferPriceId vượt quá độ dài cột (precision 10)")
    private Long productOfferPriceId;

    @Schema(description = "ID loại gói sản phẩm")
    @Min(value = 1, message = "prodPackTypeId phải >= 1")
    @Max(value = 9999999999L, message = "prodPackTypeId vượt quá độ dài cột (precision 10)")
    private Long prodPackTypeId;

    @Schema(description = "Giá thiết bị")
    @Min(value = 0, message = "devicePrice phải >= 0")
    @Max(value = 999999999999999L, message = "devicePrice vượt quá độ dài cột (precision 15)")
    private Long devicePrice;

    @Schema(description = "Giá sau khuyến mãi")
    @Min(value = 0, message = "priceAfterProm phải >= 0")
    @Max(value = 999999999999999L, message = "priceAfterProm vượt quá độ dài cột (precision 15)")
    private Long priceAfterProm;

    @Schema(description = "ID lý do")
    @Min(value = 1, message = "reasonId phải >= 1")
    @Max(value = 9999999999L, message = "reasonId vượt quá độ dài cột (precision 10)")
    private Long reasonId;

    @Schema(description = "Mã lý do")
    @Size(max = 50, message = "reasonCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "reasonCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String reasonCode;

    @Schema(description = "ID thuê bao")
    @Min(value = 1, message = "subId phải >= 1")
    @Max(value = 9999999999L, message = "subId vượt quá độ dài cột (precision 10)")
    private Long subId;

    @Schema(description = "Phân bổ chi phí")
    @Min(value = 0, message = "costAllocate phải >= 0")
    @Max(value = 999999999999999L, message = "costAllocate vượt quá độ dài cột (precision 15)")
    private Long costAllocate;

    @Schema(description = "Là dịch vụ SME")
    @Min(value = 0, message = "isSmeService phải >= 0")
    @Max(value = 9, message = "isSmeService vượt quá độ dài cột (precision 1)")
    private Long isSmeService;

    @Schema(description = "Kiểm tra độ tuổi")
    @Min(value = 0, message = "checkAge phải >= 0")
    @Max(value = 9, message = "checkAge vượt quá độ dài cột (precision 1)")
    private Long checkAge;

    @Schema(description = "ID dịch vụ RVN")
    @Min(value = 1, message = "rvnServiceId phải >= 1")
    @Max(value = 9999999999L, message = "rvnServiceId vượt quá độ dài cột (precision 10)")
    private Long rvnServiceId;

    @Schema(description = "ID chất lượng RVN")
    @Min(value = 1, message = "rvnQualityId phải >= 1")
    @Max(value = 9999999999L, message = "rvnQualityId vượt quá độ dài cột (precision 10)")
    private Long rvnQualityId;

    @Schema(description = "Chi phí giá")
    @Min(value = 0, message = "priceCost phải >= 0")
    @Max(value = 999999999999999L, message = "priceCost vượt quá độ dài cột (precision 15)")
    private Long priceCost;

    @Schema(description = "Số lượng offer")
    @Min(value = 0, message = "numOffer phải >= 0")
    @Max(value = 9999999999L, message = "numOffer vượt quá độ dài cột (precision 10)")
    private Long numOffer;

    @Schema(description = "ID lý do thay đổi khuyến mãi")
    @Min(value = 1, message = "reasonChangePromId phải >= 1")
    @Max(value = 9999999999L, message = "reasonChangePromId vượt quá độ dài cột (precision 10)")
    private Long reasonChangePromId;

    @Schema(description = "ID sản phẩm chính trong quan hệ bán kèm")
    @Min(value = 1, message = "mainOfferId phải >= 1")
    @Max(value = 9999999999L, message = "mainOfferId vượt quá độ dài cột (precision 10)")
    private Long mainOfferId;

    @Schema(description = "ID quan hệ sản phẩm")
    @Min(value = 1, message = "productOfferRelationId phải >= 1")
    @Max(value = 9999999999L, message = "productOfferRelationId vượt quá độ dài cột (precision 10)")
    private Long productOfferRelationId;

    @Schema(description = "Danh sách thuộc tính (product spec char) của VAS này")
    @Size(max = 1000, message = "lstProductSpecChars tối đa 1000 phần tử")
    private List<ProductSpecCharDTO> lstProductSpecChars;

    @Schema(description = "Thông tin quan hệ (product_offer_relation) gắn VAS này với sản phẩm chính")
    @Size(max = 1000, message = "lstProductOfferRelations tối đa 1000 phần tử")
    private List<ProductOfferRelationResponse> lstProductOfferRelations;

    @Schema(description = "ID loại kênh ánh xạ")
    @Min(value = 1, message = "mapChannelTypeId phải >= 1")
    @Max(value = 9999999999L, message = "mapChannelTypeId vượt quá độ dài cột (precision 10)")
    private Long mapChannelTypeId;

    @Schema(description = "Đặc tả sản phẩm")
    @Size(max = 1000, message = "productSpecification tối đa 1000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "productSpecification không được chứa ký tự điều khiển")
    private String productSpecification;

    @Schema(description = "Ngày bắt đầu lọc")
    private Date fromDate;

    @Schema(description = "Ngày kết thúc lọc")
    private Date toDate;

    @Schema(description = "Ngày cập nhật từ")
    private Date updateDatetimeFrom;

    @Schema(description = "Ngày cập nhật đến")
    private Date updateDatetimeTo;

    @Schema(description = "Ngày hiệu lực mở rộng")
    private Date extEffectDatetime;

    @Schema(description = "Ngày hết hạn mở rộng")
    private Date extExpireDatetime;

    @Schema(description = "Mã khuyến mãi đính kèm")
    @Size(max = 50, message = "attachPromCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "attachPromCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String attachPromCode;

    @Schema(description = "Mã khuyến mãi")
    @Size(max = 50, message = "promCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "promCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String promCode;

    @Schema(description = "Serial sản phẩm đính kèm")
    @Size(max = 50, message = "attachSerialProduct tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "attachSerialProduct chỉ gồm chữ, số, '_' hoặc '-'")
    private String attachSerialProduct;

    @Schema(description = "Mã chương trình bán hàng")
    @Size(max = 50, message = "saleProgramCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "saleProgramCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String saleProgramCode;

    @Schema(description = "Mã dịch vụ bán hàng")
    @Size(max = 50, message = "saleServiceCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "saleServiceCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String saleServiceCode;

    @Schema(description = "Tên dịch vụ bán hàng")
    @Size(max = 200, message = "saleServiceName tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "saleServiceName không được chứa ký tự điều khiển")
    private String saleServiceName;

    @Schema(description = "Đã hết hạn")
    @Size(max = 1, message = "isExpired đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "isExpired chỉ gồm chữ hoặc số")
    private String isExpired;

    @Schema(description = "Có chương trình bán hàng")
    @Size(max = 1, message = "hasSaleProgram đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "hasSaleProgram chỉ gồm chữ hoặc số")
    private String hasSaleProgram;

    @Schema(description = "Là chia sẻ doanh thu")
    @Size(max = 1, message = "isRevenueShare đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "isRevenueShare chỉ gồm chữ hoặc số")
    private String isRevenueShare;

    @Schema(description = "Loại chia sẻ")
    @Size(max = 20, message = "shareType tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "shareType chỉ gồm chữ, số, '_' hoặc '-'")
    private String shareType;

    @Schema(description = "Số tiền chia sẻ")
    @Size(max = 20, message = "shareAmount tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9.]{0,20}$", message = "shareAmount chỉ gồm chữ số và dấu chấm")
    private String shareAmount;

    @Schema(description = "Phân cấp chia sẻ")
    @Size(max = 200, message = "shareHierarchy tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "shareHierarchy không được chứa ký tự điều khiển")
    private String shareHierarchy;

    @Schema(description = "Đối tác chia sẻ")
    @Size(max = 100, message = "sharePartner tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "sharePartner không được chứa ký tự điều khiển")
    private String sharePartner;

    @Schema(description = "Tỷ lệ chia sẻ")
    @Size(max = 20, message = "shareRate tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9.]{0,20}$", message = "shareRate chỉ gồm chữ số và dấu chấm")
    private String shareRate;

    @Schema(description = "Loại mã số thuế")
    @Size(max = 20, message = "mstType tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "mstType chỉ gồm chữ, số, '_' hoặc '-'")
    private String mstType;

    @Schema(description = "Số RF mã số thuế")
    @Size(max = 50, message = "mstNoRf tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "mstNoRf chỉ gồm chữ, số, '_' hoặc '-'")
    private String mstNoRf;

    @Schema(description = "Mã nhóm cung ứng")
    @Size(max = 50, message = "supplyGroupCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "supplyGroupCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String supplyGroupCode;

    @Schema(description = "Tên nhóm cung ứng")
    @Size(max = 200, message = "supplyGroupName tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "supplyGroupName không được chứa ký tự điều khiển")
    private String supplyGroupName;

    @Schema(description = "Tên loại mô hình kho")
    @Size(max = 100, message = "stockModelTypeName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "stockModelTypeName không được chứa ký tự điều khiển")
    private String stockModelTypeName;

    @Schema(description = "Tên loại thiết bị")
    @Size(max = 100, message = "deviceTypeName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "deviceTypeName không được chứa ký tự điều khiển")
    private String deviceTypeName;

    @Schema(description = "Tên đơn vị tính")
    @Size(max = 100, message = "unitName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "unitName không được chứa ký tự điều khiển")
    private String unitName;

    @Schema(description = "Thông tin kênh")
    @Size(max = 500, message = "channelInfo tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "channelInfo không được chứa ký tự điều khiển")
    private String channelInfo;

    @Schema(description = "Thông tin cửa hàng")
    @Size(max = 500, message = "shopInfo tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "shopInfo không được chứa ký tự điều khiển")
    private String shopInfo;

    @Schema(description = "Hiển thị/Ẩn")
    @Size(max = 1, message = "showOrHide đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "showOrHide chỉ gồm chữ hoặc số")
    private String showOrHide;

    @Schema(description = "Mã nhóm con")
    @Size(max = 50, message = "subGroupCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "subGroupCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String subGroupCode;

    @Schema(description = "Tên dịch vụ RVN")
    @Size(max = 200, message = "rvnServiceName tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "rvnServiceName không được chứa ký tự điều khiển")
    private String rvnServiceName;

    @Schema(description = "Tên chất lượng RVN")
    @Size(max = 200, message = "rvnQualityName tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "rvnQualityName không được chứa ký tự điều khiển")
    private String rvnQualityName;

    @Schema(description = "Giá tăng khuyến mãi")
    @Size(max = 20, message = "promUpPrice tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9.]{0,20}$", message = "promUpPrice chỉ gồm chữ số và dấu chấm")
    private String promUpPrice;

    @Schema(description = "Mã tạm")
    @Size(max = 50, message = "tempCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "tempCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String tempCode;

    @Schema(description = "Là cập nhật bản ghi")
    @Size(max = 1, message = "isUpdateRecord đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "isUpdateRecord chỉ gồm chữ hoặc số")
    private String isUpdateRecord;

    @Schema(description = "Là sao chép bản ghi")
    @Size(max = 1, message = "isCopyRecord đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "isCopyRecord chỉ gồm chữ hoặc số")
    private String isCopyRecord;

    @Schema(description = "Là cập nhật mở rộng")
    @Size(max = 1, message = "isUpdateExtension đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "isUpdateExtension chỉ gồm chữ hoặc số")
    private String isUpdateExtension;

    @Schema(description = "Là cập nhật quan hệ")
    @Size(max = 1, message = "isUpdateRelation đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "isUpdateRelation chỉ gồm chữ hoặc số")
    private String isUpdateRelation;

    @Schema(description = "IT Telcol")
    @Size(max = 50, message = "itTelcol tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "itTelcol chỉ gồm chữ, số, '_' hoặc '-'")
    private String itTelcol;

    @Schema(description = "Loại nguồn")
    @Size(max = 20, message = "sourceType tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "sourceType chỉ gồm chữ, số, '_' hoặc '-'")
    private String sourceType;

    @Schema(description = "Cổng dịch vụ")
    @Size(max = 50, message = "serviceGate tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "serviceGate chỉ gồm chữ, số, '_' hoặc '-'")
    private String serviceGate;

    @Schema(description = "Thời gian lưu kho")
    @Size(max = 20, message = "storageTime tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9]{0,20}$", message = "storageTime chỉ gồm chữ số")
    private String storageTime;

    @Schema(description = "Số thiết bị mô phỏng")
    @Size(max = 20, message = "numDeviceSimul tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9]{0,20}$", message = "numDeviceSimul chỉ gồm chữ số")
    private String numDeviceSimul;

    @Schema(description = "Số thiết bị kết nối")
    @Size(max = 20, message = "numDeviceConnected tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9]{0,20}$", message = "numDeviceConnected chỉ gồm chữ số")
    private String numDeviceConnected;

    @Schema(description = "Loại sản phẩm")
    @Size(max = 50, message = "typeOfProduct tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "typeOfProduct chỉ gồm chữ, số, '_' hoặc '-'")
    private String typeOfProduct;

    @Schema(description = "Chuyển đổi serial")
    private boolean serialConveter;

    @Schema(description = "Chuyển đổi bundle")
    private boolean bundleConveter;

    @Schema(description = "Kiểm tra sử dụng đặc tính")
    private boolean chkCharUse;

    @Schema(description = "Kiểm tra chính xác")
    private boolean chkExact;

    @Schema(description = "Kiểm tra chính xác quan hệ")
    private boolean chkExactRelation;

    @Schema(description = "Yêu cầu VAS")
    private boolean requireVas;

    @Schema(description = "Thuộc Z36")
    private boolean belongToZ36;

    @Schema(description = "Sửa tên")
    private boolean editName;

    @Schema(description = "Sửa mã")
    private boolean editCode;

    @Schema(description = "Sửa ngày hiệu lực")
    private boolean editEffectDate;

    @Schema(description = "Sửa ngày hết hạn")
    private boolean editExpiDate;

    @Schema(description = "Là sản phẩm chính")
    private boolean main;

    @Schema(description = "Chỉ số loại")
    @Min(value = 0, message = "typeIndex phải >= 0")
    @Max(value = 99, message = "typeIndex tối đa 99")
    private int typeIndex;

    @Schema(description = "Dữ liệu hình ảnh")
    @Size(max = 5_000_000, message = "imageData tối đa 5MB")
    private byte[] imageData;

    @Schema(description = "Ngày cập nhật hình ảnh")
    private Date imageUpdateDatetime;
}
