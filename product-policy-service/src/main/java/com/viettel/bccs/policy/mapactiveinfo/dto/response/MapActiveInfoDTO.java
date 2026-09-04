package com.viettel.bccs.policy.mapactiveinfo.dto.response;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.viettel.bccs.policy.discountpromotion.dto.response.DiscountPromotionDTO;
import com.viettel.bccs.policy.reason.dto.response.ReasonDTO;
import com.viettel.bccs.policy.utils.Const;
import com.viettel.bccs.policy.utils.DataUtil;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class MapActiveInfoDTO {
    public static enum Columns {
        VAS_CODE, VAS_NAME,
        ACTION_CODE, ACTION_GROUP, ACTION_GROUP_NAME, ACTION_NAME, CAPTCHAR_REQUIRE,
        CHANNEL_NAME, CHANNEL_TYPE_ID, CREATE_USER, CUSTOMER_GROUP, CUSTOMER_TYPE,
        DISTRICT_CODE, DISTRICT_NAME, EFFECT_DATE, END_DATE, FILE_ATTACH, FILE_NAME, ID,
        ISSUE_DATETIME, LIMIT_NUMBER, OFFER_ID, OFFER_NAME, PAY_TYPE,
        POLICY_DOC, PRECINCT_CODE, PRECINCT_NAME, PRODUCT_CODE, PRODUCT_NAME, PROM_CODE, PROM_NAME, PROVINCE_CODE,
        PROVINCE_NAME, REASON_NAME, REG_REASON_ID, SHOP_CODE, SHOP_ID, STAFF_CODE, STATION_CODES,
        STATION_ID, STATUS, SUB_GROUP, SUB_TYPE, TEL_SERVICE_ID, UNIT, EXCLUSE_ID_LIST, TECHNOLOGY, UPDATE_USER,
                UPDATE_DATETIME, AREA_GROUP_CODE, HISTORY_DATE, NODE_CODE,NOTE,
        ATTACH_PRODUCT_CODE, ATTACH_PROM_CODE, ATTACH_REASON_ID, ATTACH_TEL_SERVICE_ID, GROUP_NODE_CODE,
                STAFF_TYPE, BUSINESS_NO, SUB_GROUP_CODE, SME_CUSTOMER_GROUP, IMPORT_OFFLINE_ID
    }

    @Schema(description = "Mã hành động", example = "00")
    @Size(max = 10, message = "actionCode tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "actionCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String actionCode;

    @Schema(description = "Nhóm hành động", example = "GROUP_A")
    @Size(max = 50, message = "actionGroup tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "actionGroup chỉ gồm chữ, số, '_' hoặc '-'")
    private String actionGroup;

    @Schema(description = "Tên nhóm hành động", example = "Nhóm A")
    @Size(max = 200, message = "actionGroupName tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "actionGroupName không được chứa ký tự điều khiển")
    private String actionGroupName;

    @Schema(description = "Tên hành động", example = "Kích hoạt")
    @Size(max = 50, message = "actionName tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "actionName không được chứa ký tự điều khiển")
    private String actionName;

    @Schema(description = "Yêu cầu CAPTCHA", example = "1")
    @Min(value = 0, message = "captcharRequire phải >= 0")
    @Max(value = 9, message = "captcharRequire vượt quá độ dài cột (precision 1)")
    private Short captcharRequire;

    @Schema(description = "Tên kênh", example = "Kênh 1")
    @Size(max = 50, message = "channelName tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "channelName không được chứa ký tự điều khiển")
    private String channelName;

    @Schema(description = "ID loại kênh", example = "1")
    @Min(value = 0, message = "channelTypeId phải >= 0")
    @Max(value = 9999999999L, message = "channelTypeId vượt quá độ dài cột (precision 10)")
    private Long channelTypeId;

    @Schema(description = "Người tạo", example = "admin")
    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Schema(description = "Nhóm khách hàng", example = "VIP")
    @Size(max = 10, message = "customerGroup tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "customerGroup chỉ gồm chữ, số, '_' hoặc '-'")
    private String customerGroup;

    @Schema(description = "Loại khách hàng", example = "Individual")
    @Size(max = 10, message = "customerType tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "customerType chỉ gồm chữ, số, '_' hoặc '-'")
    private String customerType;

    @Schema(description = "Mã quận/huyện", example = "HN001")
    @Size(max = 50, message = "districtCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "districtCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String districtCode;

    @Schema(description = "Tên quận/huyện", example = "Hoàn Kiếm")
    @Size(max = 50, message = "districtName tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "districtName không được chứa ký tự điều khiển")
    private String districtName;

    @Schema(description = "Ngày hiệu lực", example = "2024-01-01")
    private Date effectDate;

    @Schema(description = "Ngày hết hiệu lực", example = "2025-12-31")
    private Date endDate;

    @Schema(description = "Ngày hết hiệu lực dạng chuỗi", example = "2025-12-31")
    @Size(max = 20, message = "endDateString tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9\\-/:. ]{0,20}$", message = "endDateString sai định dạng ngày")
    private String endDateString;

    @Size(max = 5_000_000, message = "fileAttach tối đa 5MB")
    private byte[] fileAttach;

    @Schema(description = "Tên file đính kèm", example = "policy.pdf")
    @Size(max = 200, message = "fileName tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "fileName không được chứa ký tự điều khiển")
    private String fileName;

    @Schema(description = "ID", example = "1")
    @Min(value = 0, message = "id phải >= 0")
    @Max(value = 9999999999L, message = "id vượt quá độ dài cột (precision 10)")
    private Long id;

    @Schema(description = "Ngày phát hành", example = "2024-01-01")
    private Date issueDatetime;

    @Schema(description = "Số lượng giới hạn", example = "100")
    @Size(max = 10, message = "limitNumber tối đa 10 ký tự")
    @Pattern(regexp = "^[0-9]{0,10}$", message = "limitNumber chỉ gồm chữ số")
    private String limitNumber;

    @Schema(description = "ID sản phẩm", example = "1")
    @Min(value = 0, message = "offerId phải >= 0")
    @Max(value = 9999999999L, message = "offerId vượt quá độ dài cột (precision 10)")
    private Long offerId;

    @Schema(description = "Tên sản phẩm", example = "Gói cước Mobifone 50")
    @Size(max = 100, message = "offerName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "offerName không được chứa ký tự điều khiển")
    private String offerName;

    @Schema(description = "Loại thanh toán", example = "1")
    @Size(max = 1, message = "payType đúng 1 ký tự")
    @Pattern(regexp = "^[0-9]{0,1}$", message = "payType chỉ nhận 1 chữ số")
    private String payType;

    @Schema(description = "Tài liệu chính sách", example = "POL001")
    @Size(max = 100, message = "policyDoc tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "policyDoc chỉ gồm chữ, số, '_' hoặc '-'")
    private String policyDoc;

    @Schema(description = "Mã phường/xã", example = "HN00101")
    @Size(max = 50, message = "precinctCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "precinctCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String precinctCode;

    @Schema(description = "Tên phường/xã", example = "Phường Lý Thái Tổ")
    @Size(max = 50, message = "precinctName tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "precinctName không được chứa ký tự điều khiển")
    private String precinctName;

    @Schema(description = "Mã sản phẩm", example = "PROD001")
    @Size(max = 50, message = "productCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "productCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String productCode;

    @Schema(description = "Tên sản phẩm", example = "Dịch vụ Mobifone")
    @Size(max = 100, message = "productName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "productName không được chứa ký tự điều khiển")
    private String productName;

    @Schema(description = "Mã khuyến mãi", example = "PROMO001")
    @Size(max = 50, message = "promCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "promCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String promCode;

    @Schema(description = "Tên khuyến mãi", example = "Khuyến mãi tháng 1")
    @Size(max = 150, message = "promName tối đa 150 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "promName không được chứa ký tự điều khiển")
    private String promName;

    @Schema(description = "Mã tỉnh/thành phố", example = "HN")
    @Size(max = 50, message = "provinceCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "provinceCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String provinceCode;

    @Schema(description = "Tên tỉnh/thành phố", example = "Hà Nội")
    @Size(max = 50, message = "provinceName tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "provinceName không được chứa ký tự điều khiển")
    private String provinceName;

    @Schema(description = "Tên lý do", example = "Lý do đăng ký mới")
    @Size(max = 100, message = "reasonName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "reasonName không được chứa ký tự điều khiển")
    private String reasonName;

    @Schema(description = "ID lý do đăng ký", example = "1")
    @Min(value = 0, message = "regReasonId phải >= 0")
    @Max(value = 9999999999L, message = "regReasonId vượt quá độ dài cột (precision 10)")
    private Long regReasonId;

    @Schema(description = "Mã cửa hàng", example = "SHOP001")
    @Size(max = 50, message = "shopCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "shopCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String shopCode;

    @Schema(description = "ID cửa hàng", example = "1")
    @Min(value = 0, message = "shopId phải >= 0")
    @Max(value = 9999999999L, message = "shopId vượt quá độ dài cột (precision 10)")
    private Long shopId;

    @Schema(description = "Mã nhân viên", example = "STAFF001")
    @Size(max = 50, message = "staffCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "staffCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String staffCode;

    @Schema(description = "Mã trạm", example = "ST001")
    @Size(max = 1000, message = "stationCodes tối đa 1000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "stationCodes không được chứa ký tự điều khiển")
    private String stationCodes;

    @Schema(description = "ID trạm", example = "1")
    @Min(value = 0, message = "stationId phải >= 0")
    @Max(value = 9999999999L, message = "stationId vượt quá độ dài cột (precision 10)")
    private Long stationId;

    @Schema(description = "Trạng thái", example = "1")
    @Size(max = 2, message = "status tối đa 2 ký tự")
    @Pattern(regexp = "^[0-9]{0,2}$", message = "status chỉ gồm chữ số")
    private String status;

    @Schema(description = "Nhóm phụ", example = "SUB_GRP")
    @Size(max = 10, message = "subGroup tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "subGroup chỉ gồm chữ, số, '_' hoặc '-'")
    private String subGroup;

    @Schema(description = "Loại phụ", example = "SUB_TYPE")
    @Size(max = 4, message = "subType tối đa 4 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,4}$", message = "subType chỉ gồm chữ, số, '_' hoặc '-'")
    private String subType;

    @Schema(description = "ID dịch vụ viễn thông", example = "1")
    @Min(value = 0, message = "telServiceId phải >= 0")
    @Max(value = 9999999999L, message = "telServiceId vượt quá độ dài cột (precision 10)")
    private Long telServiceId;

    @Schema(description = "Đơn vị", example = "tháng")
    @Size(max = 2, message = "unit tối đa 2 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,2}$", message = "unit chỉ gồm chữ và số")
    private String unit;

    @Schema(description = "Giới hạn số lượng", example = "10")
    @Size(max = 10, message = "numberLimit tối đa 10 ký tự")
    @Pattern(regexp = "^[0-9]{0,10}$", message = "numberLimit chỉ gồm chữ số")
    private String numberLimit;

    @Schema(description = "Công nghệ", example = "4G")
    @Size(max = 10, message = "technology tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "technology chỉ gồm chữ, số, '_' hoặc '-'")
    private String technology;

    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Schema(description = "Ngày cập nhật", example = "2024-01-01")
    private Date updateDatetime;

    @Schema(description = "Mã VAS", example = "VAS001")
    @Size(max = 50, message = "vasCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "vasCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String vasCode;

    @Schema(description = "Tên VAS", example = "Dịch vụ VAS")
    @Size(max = 100, message = "vasName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "vasName không được chứa ký tự điều khiển")
    private String vasName;

    @Schema(description = "Mã node", example = "NODE001")
    @Size(max = 1500, message = "nodeCode tối đa 1500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1500}$", message = "nodeCode không được chứa ký tự điều khiển")
    private String nodeCode;

    @Schema(description = "Ghi chú", example = "Ghi chú thông tin")
    @Size(max = 3000, message = "note tối đa 3000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,3000}$", message = "note không được chứa ký tự điều khiển")
    private String note;

    @Schema(description = "Mã nhóm node", example = "GRP_NODE")
    @Size(max = 50, message = "groupNodeCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "groupNodeCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String groupNodeCode;

    @Schema(description = "ID dạng chuỗi", example = "1")
    @Size(max = 19, message = "idString tối đa 19 ký tự")
    @Pattern(regexp = "^[0-9]{0,19}$", message = "idString chỉ gồm chữ số")
    private String idString;

    @Schema(description = "Mã nhóm khu vực", example = "AREA_GRP")
    @Size(max = 50, message = "areaGroupCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "areaGroupCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String areaGroupCode;

    @Schema(description = "Tên dịch vụ viễn thông", example = "Di động")
    @Size(max = 100, message = "telecomServiceName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "telecomServiceName không được chứa ký tự điều khiển")
    private String telecomServiceName;

    @Size(max = 1000, message = "staffCodes tối đa 1000 phần tử")
    private List<String> staffCodes;

    @Size(max = 1000, message = "areaCodes tối đa 1000 phần tử")
    private List<String> areaCodes;

    @Size(max = 1000, message = "areaGroupCodes tối đa 1000 phần tử")
    private List<String> areaGroupCodes;

    @Size(max = 1000, message = "promCodes tối đa 1000 phần tử")
    private List<String> promCodes;

    @Size(max = 1000, message = "vasCodes tối đa 1000 phần tử")
    private List<String> vasCodes;

    @Size(max = 1000, message = "productCodes tối đa 1000 phần tử")
    private List<String> productCodes;

    @Size(max = 1000, message = "reasonIDs tối đa 1000 phần tử")
    private List<String> reasonIDs;

    @Schema(description = "Ngày bắt đầu", example = "2024-01-01")
    private Date fromDate;

    @Schema(description = "Ngày kết thúc", example = "2025-12-31")
    private Date toDate;

    @Schema(description = "Ngày lịch sử", example = "2024-01-01")
    private Date historyDate;

    @Schema(description = "Trạng thái lý do đăng ký", example = "1")
    @Size(max = 2, message = "regReasonStatus tối đa 2 ký tự")
    @Pattern(regexp = "^[0-9]{0,2}$", message = "regReasonStatus chỉ gồm chữ số")
    private String regReasonStatus;

    @Schema(description = "Tên cửa hàng", example = "Cửa hàng A")
    @Size(max = 100, message = "shopName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "shopName không được chứa ký tự điều khiển")
    private String shopName;

    @Schema(description = "Tên nhân viên", example = "Nguyễn Văn A")
    @Size(max = 100, message = "staffName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "staffName không được chứa ký tự điều khiển")
    private String staffName;

    @Schema(description = "Mã lý do đăng ký", example = "REG001")
    @Size(max = 50, message = "regReasonCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "regReasonCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String regReasonCode;

    @Schema(description = "Mã lý do", example = "R001")
    @Size(max = 50, message = "reasonCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "reasonCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String reasonCode;

    @Schema(description = "Là mới", example = "Y")
    @Size(max = 1, message = "isNew đúng 1 ký tự")
    @Pattern(regexp = "^[YN]{0,1}$", message = "isNew chỉ nhận giá trị Y hoặc N")
    private String isNew;

    @Schema(description = "Mã trả trước", example = "PRE001")
    @Size(max = 50, message = "prepaidCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "prepaidCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String prepaidCode;


    @Schema(description = "Số tháng 100", example = "12")
    @Min(value = 0, message = "numberOfMonth100 phải >= 0")
    @Max(value = 999, message = "numberOfMonth100 phải <= 999")
    private Long numberOfMonth100;

    @Schema(description = "Số tháng trả trước", example = "6")
    @Min(value = 0, message = "numMonthPrepaid phải >= 0")
    @Max(value = 999, message = "numMonthPrepaid phải <= 999")
    private Long numMonthPrepaid;

    @Schema(description = "Phương thức kết nối", example = "1")
    @Min(value = 0, message = "connectMethod phải >= 0")
    @Max(value = 99, message = "connectMethod phải <= 99")
    private Long connectMethod;

    @Schema(description = "ID dịch vụ viễn thông đính kèm", example = "2")
    @Min(value = 0, message = "attachTelServiceId phải >= 0")
    @Max(value = 9999999999L, message = "attachTelServiceId vượt quá độ dài cột (precision 10)")
    private Long attachTelServiceId;

    @Schema(description = "Mã sản phẩm đính kèm", example = "PROD002")
    @Size(max = 50, message = "attachProductCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "attachProductCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String attachProductCode;

    @Schema(description = "Giá niêm yết", example = "150000")
    @Size(max = 20, message = "listingPrice tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9]{0,20}$", message = "listingPrice chỉ gồm chữ số")
    private String listingPrice;

    @Schema(description = "Tốc độ tải xuống", example = "100Mbps")
    @Size(max = 20, message = "downloadSpeed tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,20}$", message = "downloadSpeed chỉ gồm chữ và số")
    private String downloadSpeed;

    @Schema(description = "Tốc độ tải lên", example = "50Mbps")
    @Size(max = 20, message = "uploadSpeed tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,20}$", message = "uploadSpeed chỉ gồm chữ và số")
    private String uploadSpeed;

    @Schema(description = "ID khuyến mãi thanh toán", example = "5")
    @Min(value = 0, message = "billingPromotionId phải >= 0")
    @Max(value = 9999999999L, message = "billingPromotionId vượt quá độ dài cột (precision 10)")
    private Long billingPromotionId;

    @Schema(description = "Giá trị trả trước", example = "50000")
    @Size(max = 20, message = "prepaidValue tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9]{0,20}$", message = "prepaidValue chỉ gồm chữ số")
    private String prepaidValue;

    private DiscountPromotionDTO discountPromotionDTO;

    @Schema(description = "Số kênh", example = "100")
    @Min(value = 0, message = "numberOfChannel phải >= 0")
    @Max(value = 9999, message = "numberOfChannel phải <= 9999")
    private Long numberOfChannel;

    @Schema(description = "Số tháng 100 dạng chuỗi", example = "12")
    @Size(max = 5, message = "strNumMonth100 tối đa 5 ký tự")
    @Pattern(regexp = "^[0-9]{0,5}$", message = "strNumMonth100 chỉ gồm chữ số")
    private String strNumMonth100;

    @Schema(description = "Mã phường/xã mới", example = "HN00102")
    @Size(max = 50, message = "precinctCodeNew tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "precinctCodeNew chỉ gồm chữ, số, '_' hoặc '-'")
    private String precinctCodeNew;

    @Schema(description = "Tên phường/xã mới", example = "Phường Trần Hưng Đạo")
    @Size(max = 50, message = "precinctNameNew tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "precinctNameNew không được chứa ký tự điều khiển")
    private String precinctNameNew;

    @Schema(description = "Cảnh báo mapping tất cả", example = "true")
    private boolean warnMappingAll;

    @Schema(description = "Tên nhóm khách hàng", example = "Khách hàng VIP")
    @Size(max = 100, message = "customerGroupName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "customerGroupName không được chứa ký tự điều khiển")
    private String customerGroupName;

    @Schema(description = "Giá giảm", example = "50000")
    @Min(value = 0, message = "discountPrice phải >= 0")
    @Max(value = 9999999999L, message = "discountPrice vượt quá độ dài cột (precision 10)")
    private Long discountPrice;

    @Schema(description = "Mức độ ưu tiên", example = "1")
    @Min(value = 0, message = "priorityLevel phải >= 0")
    @Max(value = 99, message = "priorityLevel phải <= 99")
    private Long priorityLevel;

    @Schema(description = "Tổng giá trị CDT", example = "500000")
    // Ten field giu checkstyle-compliant (totalCdtValue, toi da 2 chu hoa lien tiep), nhung
    // @JsonProperty pin lai dung ten JSON goc "totalCDTValue" de khong doi hop dong wire.
    @Size(max = 20, message = "totalCdtValue tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9]{0,20}$", message = "totalCdtValue chỉ gồm chữ số")
    @JsonProperty("totalCDTValue")
    private String totalCdtValue;

    @Schema(description = "Tổng giá trị", example = "600000")
    @Size(max = 20, message = "grandTotalValue tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9]{0,20}$", message = "grandTotalValue chỉ gồm chữ số")
    private String grandTotalValue;

    @Schema(description = "Nhóm khách hàng SME", example = "SME_GRP")
    @Size(max = 4000, message = "smeCustomerGroup tối đa 4000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "smeCustomerGroup không được chứa ký tự điều khiển")
    private String smeCustomerGroup;

    @Size(max = 1000, message = "lstSmeCustomerGroup tối đa 1000 phần tử")
    private List<String> lstSmeCustomerGroup;

    @Schema(description = "Thay đổi công nghệ", example = "true")
    private boolean changeTechnology;

    @Schema(description = "Thanh toán hàng tháng", example = "true")
    private boolean payMonthly;

    @Schema(description = "Số tháng khuyến mãi hiển thị", example = "6.0")
    @DecimalMin(value = "0", message = "displayPromMonth phải >= 0")
    @DecimalMax(value = "999", message = "displayPromMonth phải <= 999")
    private Double displayPromMonth;

    @Schema(description = "ID lý do thay đổi khuyến mãi", example = "10")
    @Min(value = 0, message = "reasonChangePromId phải >= 0")
    @Max(value = 9999999999L, message = "reasonChangePromId vượt quá độ dài cột (precision 10)")
    private Long reasonChangePromId;

    @Schema(description = "Mô tả sản phẩm", example = "Gói cước tháng")
    @Size(max = 500, message = "offerDescription tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "offerDescription không được chứa ký tự điều khiển")
    private String offerDescription;

    @Schema(description = "Mã nhân viên cửa hàng", example = "SHOP_STAFF_001")
    @Size(max = 50, message = "shopStaffCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "shopStaffCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String shopStaffCode;

    @Schema(description = "Mô tả khuyến mãi", example = "Giảm 10%")
    @Size(max = 500, message = "promDescription tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "promDescription không được chứa ký tự điều khiển")
    private String promDescription;

    @Schema(description = "ID khuyến mãi giảm giá", example = "5")
    @Min(value = 0, message = "discountPromotionId phải >= 0")
    @Max(value = 9999999999L, message = "discountPromotionId vượt quá độ dài cột (precision 10)")
    private Long discountPromotionId;

    @Schema(description = "ID đối tượng", example = "100")
    @Min(value = 0, message = "objId phải >= 0")
    @Max(value = 9999999999L, message = "objId vượt quá độ dài cột (precision 10)")
    private Long objId;

    @Schema(description = "ID dịch vụ bán hàng", example = "50")
    @Min(value = 0, message = "saleServiceId phải >= 0")
    @Max(value = 9999999999L, message = "saleServiceId vượt quá độ dài cột (precision 10)")
    private Long saleServiceId;

    @Schema(description = "Mã dịch vụ bán hàng", example = "TAI_DVBH_DKM")
    @Size(max = 50, message = "saleServiceCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "saleServiceCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String saleServiceCode;

    @Size(max = 100, message = "lstTechnology tối đa 100 phần tử")
    private List<String> lstTechnology;

    @Size(max = 100, message = "lstSubType tối đa 100 phần tử")
    private List<String> lstSubType;

    @Size(max = 100, message = "lstCustomerGroup tối đa 100 phần tử")
    private List<String> lstCustomerGroup;

    @Size(max = 100, message = "lstCustomerType tối đa 100 phần tử")
    private List<String> lstCustomerType;

    @Size(max = 1000, message = "lstChannelTypeId tối đa 1000 phần tử")
    private List<Long> lstChannelTypeId;

    @Size(max = 1000, message = "lstPromCode tối đa 1000 phần tử")
    private List<String> lstPromCode;

    @Size(max = 1000, message = "lstShop tối đa 1000 phần tử")
    private List<String> lstShop;

    @Size(max = 100, message = "lstProvince tối đa 100 phần tử")
    private List<String> lstProvince;

    @Size(max = 100, message = "lstProvinceCheckNode tối đa 100 phần tử")
    private List<String> lstProvinceCheckNode;

    @Size(max = 1000, message = "lstDistrict tối đa 1000 phần tử")
    private List<String> lstDistrict;

    @Size(max = 1000, message = "lstPrecinct tối đa 1000 phần tử")
    private List<String> lstPrecinct;

    @Size(max = 100, message = "lstActionCode tối đa 100 phần tử")
    private List<String> lstActionCode;

    @Size(max = 1000, message = "lstTelServiceId tối đa 1000 phần tử")
    private List<Long> lstTelServiceId;

    @Size(max = 1000, message = "lstProductCode tối đa 1000 phần tử")
    private List<String> lstProductCode;

    @Size(max = 1000, message = "lstReasonCode tối đa 1000 phần tử")
    private List<String> lstReasonCode;

    @Size(max = 1000, message = "lstStaffCode tối đa 1000 phần tử")
    private List<String> lstStaffCode;

    @Size(max = 1000, message = "lstRegReasonCode tối đa 1000 phần tử")
    private List<String> lstRegReasonCode;

    @Schema(description = "Hành động", example = "create")
    @Size(max = 20, message = "action tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "action chỉ gồm chữ, số, '_' hoặc '-'")
    private String action;

    @Schema(description = "Xóa ngày hết hiệu lực", example = "true")
    private boolean deleteEndDate;

    @Schema(description = "Có lỗi", example = "false")
    private boolean isError;

    @Schema(description = "Dịch vụ bán hàng", example = "SERVICE001")
    @Size(max = 100, message = "saleService tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "saleService không được chứa ký tự điều khiển")
    private String saleService;

    @Schema(description = "Ngày hết hiệu lực lý do", example = "2025-12-31")
    private Date expireDatetimeReason;

    @Schema(description = "Ngày hết hiệu lực dịch vụ bán hàng", example = "2025-12-31")
    private Date expireDatetimeSaleService;

    @Schema(description = "Ngày hết hiệu lực khuyến mãi giảm giá", example = "2025-12-31")
    private Date expireDatetimeDiscountPromotion;

    @Schema(description = "Ngày hiệu lực dịch vụ bán hàng", example = "2024-01-01")
    private Date effectDatetimeSaleService;

    @Schema(description = "Ngày hiệu lực khuyến mãi giảm giá", example = "2024-01-01")
    private Date effectDatetimeDiscountPromotion;

    @Schema(description = "Ngày hiệu lực lý do", example = "2024-01-01")
    private Date effectDatetimeReason;

    @Schema(description = "Ngày hiệu lực phí gói sản phẩm", example = "2024-01-01")
    private Date effectDatetimePackageFee;

    @Schema(description = "Ngày hết hiệu lực phí gói sản phẩm", example = "2025-12-31")
    private Date expireDatetimePackageFee;

    @Size(max = 100, message = "lstStaffType tối đa 100 phần tử")
    private List<String> lstStaffType;

    @Schema(description = "Loại hành động", example = "TYPE_A")
    @Size(max = 20, message = "actionType tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "actionType chỉ gồm chữ, số, '_' hoặc '-'")
    private String actionType;

    @Schema(description = "Hành động file", example = "0")
    @Min(value = 0, message = "fileAction phải >= 0")
    @Max(value = 9, message = "fileAction phải <= 9")
    private int fileAction;

    @Schema(description = "Số kinh doanh", example = "BUS001")
    @Size(max = 4000, message = "businessNo tối đa 4000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "businessNo không được chứa ký tự điều khiển")
    private String businessNo;

    @Size(max = 1000, message = "lstBusinessNo tối đa 1000 phần tử")
    private List<String> lstBusinessNo;

    private boolean checkVasCode;

    @Schema(description = "Có kiểm tra khu vực", example = "true")
    private boolean hasValidateArea;

    private ReasonDTO reasonDTO;

    @Schema(description = "Phí kết nối", example = "100000")
    @Min(value = 0, message = "connectionFee phải >= 0")
    @Max(value = 9999999999L, message = "connectionFee vượt quá độ dài cột (precision 10)")
    private Long connectionFee;

    @Schema(description = "ID trả trước", example = "1")
    @Min(value = 0, message = "prepaidId phải >= 0")
    @Max(value = 9999999999L, message = "prepaidId vượt quá độ dài cột (precision 10)")
    private Long prepaidId;

    @Schema(description = "Phí thiết bị thêm", example = "50000")
    @Min(value = 0, message = "addDeviceFee phải >= 0")
    @Max(value = 9999999999L, message = "addDeviceFee vượt quá độ dài cột (precision 10)")
    private Long addDeviceFee;

    @Schema(description = "Số ngày trả trước", example = "30")
    @Min(value = 0, message = "numDayPrepaid phải >= 0")
    @Max(value = 999, message = "numDayPrepaid phải <= 999")
    private Long numDayPrepaid;

    @Schema(description = "Ngày hiệu lực từ", example = "2024-01-01")
    private Date effectDateFrom;

    @Schema(description = "Ngày hiệu lực đến", example = "2025-12-31")
    private Date effectDateTo;

    @Schema(description = "Ngày cập nhật từ", example = "2024-01-01")
    private Date updateDatetimeFrom;

    @Schema(description = "Ngày cập nhật đến", example = "2025-12-31")
    private Date updateDatetimeTo;

    @Schema(description = "Mã nhóm nhân viên cửa hàng", example = "SHOP_STAFF_GRP")
    @Size(max = 50, message = "groupShopStaffCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "groupShopStaffCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String groupShopStaffCode;

    @Schema(description = "Có chi tiết lý do", example = "Y")
    @Size(max = 1, message = "hasDetailReason đúng 1 ký tự")
    @Pattern(regexp = "^[YN]{0,1}$", message = "hasDetailReason chỉ nhận giá trị Y hoặc N")
    private String hasDetailReason;

    @Schema(description = "Mã khuyến mãi mới", example = "PROMO002")
    @Size(max = 50, message = "newPromCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "newPromCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String newPromCode;

    @Schema(description = "ID offline", example = "123")
    @Min(value = 0, message = "importOfflineId phải >= 0")
    @Max(value = 9999999999L, message = "importOfflineId vượt quá độ dài cột (precision 10)")
    private Long importOfflineId;

    @Schema(description = "Mã dự án", example = "PRJ001")
    @Size(max = 50, message = "projectCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "projectCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String projectCode;

    @Schema(description = "Tên dự án", example = "Dự án A")
    @Size(max = 200, message = "projectName tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "projectName không được chứa ký tự điều khiển")
    private String projectName;

    @Schema(description = "Địa điểm dự án", example = "Hà Nội")
    @Size(max = 200, message = "projectLocation tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "projectLocation không được chứa ký tự điều khiển")
    private String projectLocation;

    @Schema(description = "Giá dự án", example = "100000000")
    @Size(max = 20, message = "projectPrice tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9]{0,20}$", message = "projectPrice chỉ gồm chữ số")
    private String projectPrice;

    @Schema(description = "Phương thức áp dụng", example = "METHOD_A")
    @Size(max = 20, message = "applyMethod tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "applyMethod chỉ gồm chữ, số, '_' hoặc '-'")
    private String applyMethod;

    @Schema(description = "Giá trị trả trước sau giảm giá", example = "45000")
    @Size(max = 20, message = "prepaidValueAfterDiscount tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9]{0,20}$", message = "prepaidValueAfterDiscount chỉ gồm chữ số")
    private String prepaidValueAfterDiscount;

    @Schema(description = "Tổng phí trả trước", example = "300000")
    @Min(value = 0, message = "totalPrepaidFee phải >= 0")
    @Max(value = 9999999999L, message = "totalPrepaidFee vượt quá độ dài cột (precision 10)")
    private Long totalPrepaidFee;

    @Schema(description = "Tổng phí thường", example = "500000")
    @Min(value = 0, message = "totalNormalFee phải >= 0")
    @Max(value = 9999999999L, message = "totalNormalFee vượt quá độ dài cột (precision 10)")
    private Long totalNormalFee;

    @Schema(description = "Số thiết bị tối đa", example = "5")
    @Size(max = 10, message = "maxDevices tối đa 10 ký tự")
    @Pattern(regexp = "^[0-9]{0,10}$", message = "maxDevices chỉ gồm chữ số")
    private String maxDevices;

    @Size(max = 50, message = "infraType tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "infraType chỉ gồm chữ, số, '_' hoặc '-'")
    private String infraType;

    @Size(max = 4000, message = "lstBill tối đa 4000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "lstBill không được chứa ký tự điều khiển")
    private String lstBill;

    @Min(value = 0, message = "newChannelTypeId phải >= 0")
    @Max(value = 9999999999L, message = "newChannelTypeId vượt quá độ dài cột (precision 10)")
    private Long newChannelTypeId;

    private Date newEffectDate;
    private Date newEndDate;

    @Size(max = 5, message = "staffType tối đa 5 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,5}$", message = "staffType chỉ gồm chữ, số, '_' hoặc '-'")
    private String staffType;

    @Schema(description = "Mã nhóm phụ", example = "SUB_GRP_CODE")
    @Size(max = 4000, message = "subGroupCode tối đa 4000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "subGroupCode không được chứa ký tự điều khiển")
    private String subGroupCode;

    @Schema(description = "ID nhân viên", example = "1")
    @Min(value = 0, message = "staffId phải >= 0")
    @Max(value = 9999999999L, message = "staffId vượt quá độ dài cột (precision 10)")
    private Long staffId;

    public void setStationCodes(String stationCodes) {
        if (!DataUtil.isNullOrEmpty(stationCodes)) {
            this.stationCodes = stationCodes.trim().replaceAll("\\s+", "");
        } else {
            this.stationCodes = stationCodes;
        }
    }

    public void setNodeCode(String nodeCode) {
        if (!DataUtil.isNullOrEmpty(nodeCode)) {
            this.nodeCode = nodeCode.trim().replaceAll("\\s+", "");
        } else {
            this.nodeCode = nodeCode;
        }
    }

    public Object getByProperty(String propertyName) {
        if ("telServiceId".equals(propertyName)) {
            return this.getTelServiceId() == null ? Const.TelecomServiceId.DEFAULT_VALUE_MAP_SELECT_ALL :
                    this.getTelServiceId();
        }
        if ("productCode".equals(propertyName)) {
            return this.getProductCode();
        }
        if ("offerName".equals(propertyName)) {
            return this.getOfferName();
        }
        if ("offerId".equals(propertyName)) {
            return this.getOfferId() == null ? Const.TelecomServiceId.DEFAULT_VALUE_MAP_SELECT_ALL : this.getOfferId();
        }
        if ("regReasonId".equals(propertyName)) {
            return this.getRegReasonId() == null ? Const.TelecomServiceId.DEFAULT_VALUE_MAP_SELECT_ALL :
                    this.getRegReasonId();
        }
        if ("reasonName".equals(propertyName)) {
            return this.getReasonName();
        }
        if ("promCode".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getPromCode()) ? "" : this.getPromCode();
        }
        if ("promName".equals(propertyName)) {
            return this.getPromName();
        }
        if ("channelTypeId".equals(propertyName)) {
            return this.getChannelTypeId() == null ? Const.TelecomServiceId.DEFAULT_VALUE_MAP_SELECT_ALL :
                    this.getChannelTypeId();
        }
        if ("status".equals(propertyName)) {
            return this.getStatus();
        }
        if ("channelName".equals(propertyName)) {
            return this.getChannelName();
        }
        if ("provinceCode".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getProvinceCode()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL :
                    this.getProvinceCode();
        }
        if ("provinceName".equals(propertyName)) {
            return this.getProvinceName();
        }
        if ("precinctCode".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getPrecinctCode()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL :
                    this.getPrecinctCode();
        }
        if ("precinctName".equals(propertyName)) {
            return this.getPrecinctName();
        }
        if ("districtCode".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getDistrictCode()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL :
                    this.getDistrictCode();
        }
        if ("districtName".equals(propertyName)) {
            return this.getDistrictName();
        }
        if ("effectDate".equals(propertyName)) {
            return this.getEffectDate();
        }
        if ("endDate".equals(propertyName)) {
            return this.getEndDate();
        }
        if ("payType".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getPayType()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : this.getPayType();
        }
        if ("actionCode".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getActionCode()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL :
                    this.getActionCode();
        }
        if ("actionName".equals(propertyName)) {
            return this.getActionName();
        }
        if ("numberLimit".equals(propertyName)) {
            return this.getNumberLimit();
        }
        if ("shopCode".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getShopCode()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : this.getShopCode();
        }
        if ("staffCode".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getStaffCode()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : this.getStaffCode(
                    );
        }
        if ("captcharRequire".equals(propertyName)) {
            return this.getCaptcharRequire();
        }
        if ("unit".equals(propertyName)) {
            return this.getUnit();
        }
        if ("customerGroup".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getCustomerGroup()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL :
                    this.getCustomerGroup();
        }
        if ("customerType".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getCustomerType()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL :
                    this.getCustomerType();
        }
        if ("subType".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getSubType()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : this.getSubType();
        }
        if ("subGroup".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getSubGroup()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : this.getSubGroup();
        }
        if ("policyDoc".equals(propertyName)) {
            return this.getPolicyDoc();
        }
        if ("actionGroup".equals(propertyName)) {
            return this.getActionGroup();
        }
        if ("actionGroupName".equals(propertyName)) {
            return this.getActionGroupName();
        }
        if ("fileName".equals(propertyName)) {
            return this.getFileName();
        }
        if ("stationId".equals(propertyName)) {
            return this.getStationId() == null ? Const.TelecomServiceId.DEFAULT_VALUE_MAP_SELECT_ALL :
                    this.getStationId();
        }
        if ("stationCodes".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getStationCodes()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL :
                    this.getStationCodes();
        }
        if ("technology".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getTechnology()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL :
                    this.getTechnology();
        }
        if ("nodeCode".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getNodeCode()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : this.getNodeCode();
        }
        return null;
    }
}
