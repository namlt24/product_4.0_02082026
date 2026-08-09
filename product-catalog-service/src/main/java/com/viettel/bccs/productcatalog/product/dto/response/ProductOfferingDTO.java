package com.viettel.bccs.productcatalog.product.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productofferrelation.dto.response.ProductOfferRelationResponse;
import io.swagger.v3.oas.annotations.media.Schema;
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

    public static final String FIELD_PRODUCT_OFFER_TYPE_ID = "product_offer_type_id";
    public static final String FIELD_PRODUCT_OFFERING_ID = "product_offering_id";

    @Schema(description = "Mã sản phẩm", example = "PACKAGE_001")
    @JsonProperty(value = "code")
    private String code;

    @Schema(description = "Tên sản phẩm", example = "Gói cước data 50GB")
    @JsonProperty(value = "name")
    private String name;

    @Schema(description = "ID sản phẩm", example = "12345")
    @JsonProperty(value = "product_offering_id")
    private Long productOfferingId;

    @Schema(description = "ID loại sản phẩm", example = "1")
    @JsonProperty(value = "product_offer_type_id")
    private Long productOfferTypeId;

    @Schema(description = "ID dịch vụ viễn thông", example = "1")
    @JsonProperty(value = "telecom_service_id")
    private Long telecomServiceId;

    @Schema(description = "Trạng thái", example = "1")
    @JsonProperty(value = "status")
    private String status;

    @Schema(description = "Tên loại sản phẩm", example = "Gói cước data")
    @JsonProperty(value = "product_offer_type_name")
    private String productTypeName;

    @Schema(description = "ID đặc tả sản phẩm")
    private Long productSpecId;

    @Schema(description = "Loại thuê bao (1: trả sau, 2: trả trước)")
    private String subType;

    @Schema(description = "Mô tả sản phẩm")
    private String description;

    @Schema(description = "Phiên bản")
    private String version;

    @Schema(description = "Đơn vị tính")
    private String unit;

    @Schema(description = "Loại thiết bị")
    private String deviceType;

    @Schema(description = "ID mục đích sử dụng")
    private String usageId;

    @Schema(description = "Là gói bundle")
    private String isBundle;

    @Schema(description = "Tên dịch vụ viễn thông")
    private String telecomServiceName;

    @Schema(description = "Tên loại sản phẩm")
    private String productOfferTypeName;

    @Schema(description = "Tên đặc tả sản phẩm")
    private String productSpecName;

    @Schema(description = "Mã kế toán")
    private String accountingCode;

    @Schema(description = "Mã mô hình kế toán")
    private String accountingModelCode;

    @Schema(description = "Tên mô hình kế toán")
    private String accountingModelName;

    @Schema(description = "Tên kế toán")
    private String accountingName;

    @Schema(description = "Kiểm tra đặt cọc")
    private Short checkDeposit;

    @Schema(description = "Kiểm tra serial")
    private Short checkSerial;

    @Schema(description = "Là gói demo")
    private Short isDemo;

    @Schema(description = "Bộ thu phát")
    private Short transceiver;

    @Schema(description = "Loại mô hình kho")
    private Short stockModelType;

    @Schema(description = "Thời lượng demo (ngày)")
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
    private String createUser;

    @Schema(description = "Người cập nhật")
    private String updateUser;

    @Schema(description = "ID cửa hàng sở hữu")
    private Long ownerShopId;

    @Schema(description = "Số vật liệu SAP")
    private BigDecimal sapMaterialNumber;

    @Schema(description = "Mã vùng")
    private String areaCode;

    @Schema(description = "Mã cửa hàng")
    private String shopCode;

    @Schema(description = "Tên cửa hàng")
    private String shopName;

    @Schema(description = "ID loại kênh")
    private Long channelTypeId;

    @Schema(description = "Tên loại kênh")
    private String channelTypeName;

    @Schema(description = "Công nghệ (4G/5G/FTTH)")
    private String technology;

    @Schema(description = "Loại hạ tầng")
    private String infraType;

    @Schema(description = "ID loại giá")
    private String priceTypeId;

    @Schema(description = "ID chính sách giá")
    private String pricePolicyId;

    @Schema(description = "Loại chi tiết")
    private String detailType;

    @Schema(description = "Tự động điền")
    private String autoFill;

    @Schema(description = "Loại sản phẩm SAP")
    private String sapProductType;

    @Schema(description = "Phân cấp sản phẩm")
    private String productHierarchy;

    @Schema(description = "Trả hàng khi hủy")
    private String returnStock;

    @Schema(description = "Loại cửa hàng")
    private String shopType;

    @Schema(description = "Kiểm tra tồn kho cửa hàng")
    private String checkShopStock;

    @Schema(description = "Kiểm tra tồn kho nhân viên")
    private String checkStaffStock;

    @Schema(description = "Tính năng sản phẩm")
    private String offerFeature;

    @Schema(description = "Loại FTTH")
    private String ftthType;

    @Schema(description = "Mã nhân viên")
    private String staffCode;

    @Schema(description = "Ghi chú")
    private String note;

    @Schema(description = "ID đặc tả")
    private Long specificationId;

    @Schema(description = "Giá niêm yết")
    private Long listingPrice;

    @Schema(description = "Tốc độ tải xuống (Mbps)")
    private Long downloadSpeed;

    @Schema(description = "Tốc độ tải lên (Mbps)")
    private Long uploadSpeed;

    @Schema(description = "Số kênh")
    private Long numberOfChannel;

    @Schema(description = "Tốc độ tối đa (Mbps)")
    private Long speedUpto;

    @Schema(description = "Giá sản phẩm")
    private Long price;

    @Schema(description = "VAT")
    private Long vat;

    @Schema(description = "ID giá sản phẩm")
    private Long productOfferPriceId;

    @Schema(description = "ID loại gói sản phẩm")
    private Long prodPackTypeId;

    @Schema(description = "Giá thiết bị")
    private Long devicePrice;

    @Schema(description = "Giá sau khuyến mãi")
    private Long priceAfterProm;

    @Schema(description = "ID lý do")
    private Long reasonId;

    @Schema(description = "Mã lý do")
    private String reasonCode;

    @Schema(description = "ID thuê bao")
    private Long subId;

    @Schema(description = "Phân bổ chi phí")
    private Long costAllocate;

    @Schema(description = "Là dịch vụ SME")
    private Long isSmeService;

    @Schema(description = "Kiểm tra độ tuổi")
    private Long checkAge;

    @Schema(description = "ID dịch vụ RVN")
    private Long rvnServiceId;

    @Schema(description = "ID chất lượng RVN")
    private Long rvnQualityId;

    @Schema(description = "Chi phí giá")
    private Long priceCost;

    @Schema(description = "Số lượng offer")
    private Long numOffer;

    @Schema(description = "ID lý do thay đổi khuyến mãi")
    private Long reasonChangePromId;

    @Schema(description = "ID sản phẩm chính trong quan hệ bán kèm")
    private Long mainOfferId;

    @Schema(description = "ID quan hệ sản phẩm")
    private Long productOfferRelationId;

    @Schema(description = "Danh sách thuộc tính (product spec char) của VAS này")
    private List<ProductSpecCharDTO> lstProductSpecChars;

    @Schema(description = "Thông tin quan hệ (product_offer_relation) gắn VAS này với sản phẩm chính")
    private List<ProductOfferRelationResponse> lstProductOfferRelations;

    @Schema(description = "ID loại kênh ánh xạ")
    private Long mapChannelTypeId;

    @Schema(description = "Đặc tả sản phẩm")
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
    private String attachPromCode;

    @Schema(description = "Mã khuyến mãi")
    private String promCode;

    @Schema(description = "Serial sản phẩm đính kèm")
    private String attachSerialProduct;

    @Schema(description = "Mã chương trình bán hàng")
    private String saleProgramCode;

    @Schema(description = "Mã dịch vụ bán hàng")
    private String saleServiceCode;

    @Schema(description = "Tên dịch vụ bán hàng")
    private String saleServiceName;

    @Schema(description = "Đã hết hạn")
    private String isExpired;

    @Schema(description = "Có chương trình bán hàng")
    private String hasSaleProgram;

    @Schema(description = "Là chia sẻ doanh thu")
    private String isRevenueShare;

    @Schema(description = "Loại chia sẻ")
    private String shareType;

    @Schema(description = "Số tiền chia sẻ")
    private String shareAmount;

    @Schema(description = "Phân cấp chia sẻ")
    private String shareHierarchy;

    @Schema(description = "Đối tác chia sẻ")
    private String sharePartner;

    @Schema(description = "Tỷ lệ chia sẻ")
    private String shareRate;

    @Schema(description = "Loại mã số thuế")
    private String mstType;

    @Schema(description = "Số RF mã số thuế")
    private String mstNoRf;

    @Schema(description = "Mã nhóm cung ứng")
    private String supplyGroupCode;

    @Schema(description = "Tên nhóm cung ứng")
    private String supplyGroupName;

    @Schema(description = "Tên loại mô hình kho")
    private String stockModelTypeName;

    @Schema(description = "Tên loại thiết bị")
    private String deviceTypeName;

    @Schema(description = "Tên đơn vị tính")
    private String unitName;

    @Schema(description = "Thông tin kênh")
    private String channelInfo;

    @Schema(description = "Thông tin cửa hàng")
    private String shopInfo;

    @Schema(description = "Hiển thị/Ẩn")
    private String showOrHide;

    @Schema(description = "Mã nhóm con")
    private String subGroupCode;

    @Schema(description = "Tên dịch vụ RVN")
    private String rvnServiceName;

    @Schema(description = "Tên chất lượng RVN")
    private String rvnQualityName;

    @Schema(description = "Giá tăng khuyến mãi")
    private String promUpPrice;

    @Schema(description = "Mã tạm")
    private String tempCode;

    @Schema(description = "Là cập nhật bản ghi")
    private String isUpdateRecord;

    @Schema(description = "Là sao chép bản ghi")
    private String isCopyRecord;

    @Schema(description = "Là cập nhật mở rộng")
    private String isUpdateExtension;

    @Schema(description = "Là cập nhật quan hệ")
    private String isUpdateRelation;

    @Schema(description = "IT Telcol")
    private String itTelcol;

    @Schema(description = "Loại nguồn")
    private String sourceType;

    @Schema(description = "Cổng dịch vụ")
    private String serviceGate;

    @Schema(description = "Thời gian lưu kho")
    private String storageTime;

    @Schema(description = "Số thiết bị mô phỏng")
    private String numDeviceSimul;

    @Schema(description = "Số thiết bị kết nối")
    private String numDeviceConnected;

    @Schema(description = "Loại sản phẩm")
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
    private int typeIndex;

    @Schema(description = "Dữ liệu hình ảnh")
    private byte[] imageData;

    @Schema(description = "Ngày cập nhật hình ảnh")
    private Date imageUpdateDatetime;
}