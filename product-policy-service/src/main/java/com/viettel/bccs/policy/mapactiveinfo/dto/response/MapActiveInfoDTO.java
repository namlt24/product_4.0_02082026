package com.viettel.bccs.policy.mapactiveinfo.dto.response;

import com.viettel.bccs.policy.discountpromotion.dto.response.DiscountPromotionDTO;
import com.viettel.bccs.policy.reason.dto.response.ReasonDTO;
import com.viettel.bccs.policy.utils.Const;
import com.viettel.bccs.policy.utils.DataUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class MapActiveInfoDTO {
    public static enum COLUMNS {
        VAS_CODE, VAS_NAME,
        ACTION_CODE, ACTION_GROUP, ACTION_GROUP_NAME, ACTION_NAME, CAPTCHAR_REQUIRE,
        CHANNEL_NAME, CHANNEL_TYPE_ID, CREATE_USER, CUSTOMER_GROUP, CUSTOMER_TYPE,
        DISTRICT_CODE, DISTRICT_NAME, EFFECT_DATE, END_DATE, FILE_ATTACH, FILE_NAME, ID,
        ISSUE_DATETIME, LIMIT_NUMBER, OFFER_ID, OFFER_NAME, PAY_TYPE,
        POLICY_DOC, PRECINCT_CODE, PRECINCT_NAME, PRODUCT_CODE, PRODUCT_NAME, PROM_CODE, PROM_NAME, PROVINCE_CODE,
        PROVINCE_NAME, REASON_NAME, REG_REASON_ID, SHOP_CODE, SHOP_ID, STAFF_CODE, STATION_CODES,
        STATION_ID, STATUS, SUB_GROUP, SUB_TYPE, TEL_SERVICE_ID, UNIT, EXCLUSE_ID_LIST, TECHNOLOGY, UPDATE_USER, UPDATE_DATETIME, AREA_GROUP_CODE, HISTORY_DATE, NODE_CODE,NOTE,
        ATTACH_PRODUCT_CODE, ATTACH_PROM_CODE, ATTACH_REASON_ID, ATTACH_TEL_SERVICE_ID, GROUP_NODE_CODE,STAFF_TYPE, BUSINESS_NO, SUB_GROUP_CODE, SME_CUSTOMER_GROUP, IMPORT_OFFLINE_ID
    }

    @Schema(description = "Mã hành động", example = "00")
    private String actionCode;

    @Schema(description = "Nhóm hành động", example = "GROUP_A")
    private String actionGroup;

    @Schema(description = "Tên nhóm hành động", example = "Nhóm A")
    private String actionGroupName;

    @Schema(description = "Tên hành động", example = "Kích hoạt")
    private String actionName;

    @Schema(description = "Yêu cầu CAPTCHA", example = "1")
    private Short captcharRequire;

    @Schema(description = "Tên kênh", example = "Kênh 1")
    private String channelName;

    @Schema(description = "ID loại kênh", example = "1")
    private Long channelTypeId;

    @Schema(description = "Người tạo", example = "admin")
    private String createUser;

    @Schema(description = "Nhóm khách hàng", example = "VIP")
    private String customerGroup;

    @Schema(description = "Loại khách hàng", example = "Individual")
    private String customerType;

    @Schema(description = "Mã quận/huyện", example = "HN001")
    private String districtCode;

    @Schema(description = "Tên quận/huyện", example = "Hoàn Kiếm")
    private String districtName;

    @Schema(description = "Ngày hiệu lực", example = "2024-01-01")
    private LocalDateTime effectDate;

    @Schema(description = "Ngày hết hiệu lực", example = "2025-12-31")
    private LocalDateTime endDate;

    @Schema(description = "Ngày hết hiệu lực dạng chuỗi", example = "2025-12-31")
    private String endDateString;

    private byte[] fileAttach;

    @Schema(description = "Tên file đính kèm", example = "policy.pdf")
    private String fileName;

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "Ngày phát hành", example = "2024-01-01")
    private LocalDateTime issueDatetime;

    @Schema(description = "Số lượng giới hạn", example = "100")
    private String limitNumber;

    @Schema(description = "ID sản phẩm", example = "1")
    private Long offerId;

    @Schema(description = "Tên sản phẩm", example = "Gói cước Mobifone 50")
    private String offerName;

    @Schema(description = "Loại thanh toán", example = "1")
    private String payType;

    @Schema(description = "Tài liệu chính sách", example = "POL001")
    private String policyDoc;

    @Schema(description = "Mã phường/xã", example = "HN00101")
    private String precinctCode;

    @Schema(description = "Tên phường/xã", example = "Phường Lý Thái Tổ")
    private String precinctName;

    @Schema(description = "Mã sản phẩm", example = "PROD001")
    private String productCode;

    @Schema(description = "Tên sản phẩm", example = "Dịch vụ Mobifone")
    private String productName;

    @Schema(description = "Mã khuyến mãi", example = "PROMO001")
    private String promCode;

    @Schema(description = "Tên khuyến mãi", example = "Khuyến mãi tháng 1")
    private String promName;

    @Schema(description = "Mã tỉnh/thành phố", example = "HN")
    private String provinceCode;

    @Schema(description = "Tên tỉnh/thành phố", example = "Hà Nội")
    private String provinceName;

    @Schema(description = "Tên lý do", example = "Lý do đăng ký mới")
    private String reasonName;

    @Schema(description = "ID lý do đăng ký", example = "1")
    private Long regReasonId;

    @Schema(description = "Mã cửa hàng", example = "SHOP001")
    private String shopCode;

    @Schema(description = "ID cửa hàng", example = "1")
    private Long shopId;

    @Schema(description = "Mã nhân viên", example = "STAFF001")
    private String staffCode;

    @Schema(description = "Mã trạm", example = "ST001")
    private String stationCodes;

    @Schema(description = "ID trạm", example = "1")
    private Long stationId;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Nhóm phụ", example = "SUB_GRP")
    private String subGroup;

    @Schema(description = "Loại phụ", example = "SUB_TYPE")
    private String subType;

    @Schema(description = "ID dịch vụ viễn thông", example = "1")
    private Long telServiceId;

    @Schema(description = "Đơn vị", example = "tháng")
    private String unit;

    @Schema(description = "Giới hạn số lượng", example = "10")
    private String numberLimit;

    @Schema(description = "Công nghệ", example = "4G")
    private String technology;

    private String updateUser;

    @Schema(description = "Ngày cập nhật", example = "2024-01-01")
    private LocalDateTime updateDatetime;

    @Schema(description = "Mã VAS", example = "VAS001")
    private String vasCode;

    @Schema(description = "Tên VAS", example = "Dịch vụ VAS")
    private String vasName;

    @Schema(description = "Mã node", example = "NODE001")
    private String nodeCode;

    @Schema(description = "Ghi chú", example = "Ghi chú thông tin")
    private String note;

    @Schema(description = "Mã nhóm node", example = "GRP_NODE")
    private String groupNodeCode;

    @Schema(description = "ID dạng chuỗi", example = "1")
    private String idString;

    @Schema(description = "Mã nhóm khu vực", example = "AREA_GRP")
    private String areaGroupCode;

    @Schema(description = "Tên dịch vụ viễn thông", example = "Di động")
    private String telecomServiceName;

    private List<String> staffCodes;
    private List<String> areaCodes;
    private List<String> areaGroupCodes;
    private List<String> promCodes;
    private List<String> vasCodes;
    private List<String> productCodes;
    private List<String> reasonIDs;

    @Schema(description = "Ngày bắt đầu", example = "2024-01-01")
    private LocalDateTime fromDate;

    @Schema(description = "Ngày kết thúc", example = "2025-12-31")
    private LocalDateTime toDate;

    @Schema(description = "Ngày lịch sử", example = "2024-01-01")
    private Date historyDate;

    @Schema(description = "Trạng thái lý do đăng ký", example = "1")
    private String regReasonStatus;

    @Schema(description = "Tên cửa hàng", example = "Cửa hàng A")
    private String shopName;

    @Schema(description = "Tên nhân viên", example = "Nguyễn Văn A")
    private String staffName;

    @Schema(description = "Mã lý do đăng ký", example = "REG001")
    private String regReasonCode;

    @Schema(description = "Mã lý do", example = "R001")
    private String reasonCode;

    @Schema(description = "Là mới", example = "Y")
    private String isNew;

    @Schema(description = "Mã trả trước", example = "PRE001")
    private String prepaidCode;


    @Schema(description = "Số tháng 100", example = "12")
    private Long numberOfMonth100;

    @Schema(description = "Số tháng trả trước", example = "6")
    private Long numMonthPrepaid;

    @Schema(description = "Phương thức kết nối", example = "1")
    private Long connectMethod;

    @Schema(description = "ID dịch vụ viễn thông đính kèm", example = "2")
    private Long attachTelServiceId;

    @Schema(description = "Mã sản phẩm đính kèm", example = "PROD002")
    private String attachProductCode;

    @Schema(description = "Giá niêm yết", example = "150000")
    private String listingPrice;

    @Schema(description = "Tốc độ tải xuống", example = "100Mbps")
    private String downloadSpeed;

    @Schema(description = "Tốc độ tải lên", example = "50Mbps")
    private String uploadSpeed;

    @Schema(description = "ID khuyến mãi thanh toán", example = "5")
    private Long billingPromotionId;

    @Schema(description = "Giá trị trả trước", example = "50000")
    private String prepaidValue;

    private DiscountPromotionDTO discountPromotionDTO;

    @Schema(description = "Số kênh", example = "100")
    private Long numberOfChannel;

    @Schema(description = "Số tháng 100 dạng chuỗi", example = "12")
    private String strNumMonth100;

    @Schema(description = "Mã phường/xã mới", example = "HN00102")
    private String precinctCodeNew;

    @Schema(description = "Tên phường/xã mới", example = "Phường Trần Hưng Đạo")
    private String precinctNameNew;

    @Schema(description = "Cảnh báo mapping tất cả", example = "true")
    private boolean warnMappingAll;

    @Schema(description = "Tên nhóm khách hàng", example = "Khách hàng VIP")
    private String customerGroupName;

    @Schema(description = "Giá giảm", example = "50000")
    private Long discountPrice;

    @Schema(description = "Mức độ ưu tiên", example = "1")
    private Long priorityLevel;

    @Schema(description = "Tổng giá trị CDT", example = "500000")
    private String totalCDTValue;

    @Schema(description = "Tổng giá trị", example = "600000")
    private String grandTotalValue;

    @Schema(description = "Nhóm khách hàng SME", example = "SME_GRP")
    private String smeCustomerGroup;

    private List<String> lstSmeCustomerGroup;

    @Schema(description = "Thay đổi công nghệ", example = "true")
    private boolean changeTechnology;

    @Schema(description = "Thanh toán hàng tháng", example = "true")
    private boolean payMonthly;

    @Schema(description = "Số tháng khuyến mãi hiển thị", example = "6.0")
    private Double displayPromMonth;

    @Schema(description = "ID lý do thay đổi khuyến mãi", example = "10")
    private Long reasonChangePromId;

    @Schema(description = "Mô tả sản phẩm", example = "Gói cước tháng")
    private String offerDescription;

    @Schema(description = "Mã nhân viên cửa hàng", example = "SHOP_STAFF_001")
    private String shopStaffCode;

    @Schema(description = "Mô tả khuyến mãi", example = "Giảm 10%")
    private String promDescription;

    @Schema(description = "ID khuyến mãi giảm giá", example = "5")
    private Long discountPromotionId;

    @Schema(description = "ID đối tượng", example = "100")
    private Long objId;

    @Schema(description = "ID dịch vụ bán hàng", example = "50")
    private Long saleServiceId;

    private List<String> lstTechnology;
    private List<String> lstSubType;
    private List<String> lstCustomerGroup;
    private List<String> lstCustomerType;
    private List<Long> lstChannelTypeId;
    private List<String> lstPromCode;
    private List<String> lstShop;
    private List<String> lstProvince;
    private List<String> lstProvinceCheckNode;
    private List<String> lstDistrict;
    private List<String> lstPrecinct;
    private List<String> lstActionCode;
    private List<Long> lstTelServiceId;
    private List<String> lstProductCode;
    private List<String> lstReasonCode;
    private List<String> lstStaffCode;
    private List<String> lstRegReasonCode;

    @Schema(description = "Hành động", example = "create")
    private String action;

    @Schema(description = "Xóa ngày hết hiệu lực", example = "true")
    private boolean deleteEndDate;

    @Schema(description = "Có lỗi", example = "false")
    private boolean isError;

    @Schema(description = "Dịch vụ bán hàng", example = "SERVICE001")
    private String saleService;

    @Schema(description = "Ngày hết hiệu lực lý do", example = "2025-12-31")
    private LocalDateTime expireDatetimeReason;

    @Schema(description = "Ngày hết hiệu lực dịch vụ bán hàng", example = "2025-12-31")
    private LocalDateTime expireDatetimeSaleService;

    @Schema(description = "Ngày hết hiệu lực khuyến mãi giảm giá", example = "2025-12-31")
    private LocalDateTime expireDatetimeDiscountPromotion;

    @Schema(description = "Ngày hiệu lực dịch vụ bán hàng", example = "2024-01-01")
    private LocalDateTime effectDatetimeSaleService;

    @Schema(description = "Ngày hiệu lực khuyến mãi giảm giá", example = "2024-01-01")
    private LocalDateTime effectDatetimeDiscountPromotion;

    @Schema(description = "Ngày hiệu lực lý do", example = "2024-01-01")
    private LocalDateTime effectDatetimeReason;

    @Schema(description = "Ngày hiệu lực phí gói sản phẩm", example = "2024-01-01")
    private LocalDateTime effectDatetimePackageFee;

    @Schema(description = "Ngày hết hiệu lực phí gói sản phẩm", example = "2025-12-31")
    private LocalDateTime expireDatetimePackageFee;

    private List<String> lstStaffType;

    @Schema(description = "Loại hành động", example = "TYPE_A")
    private String actionType;

    @Schema(description = "Hành động file", example = "0")
    private int fileAction;

    @Schema(description = "Số kinh doanh", example = "BUS001")
    private String businessNo;

    private List<String> lstBusinessNo;

    private boolean checkVasCode;

    @Schema(description = "Có kiểm tra khu vực", example = "true")
    private boolean hasValidateArea;

    private ReasonDTO reasonDTO;

    @Schema(description = "Phí kết nối", example = "100000")
    private Long connectionFee;

    @Schema(description = "ID trả trước", example = "1")
    private Long prepaidId;

    @Schema(description = "Phí thiết bị thêm", example = "50000")
    private Long addDeviceFee;

    @Schema(description = "Số ngày trả trước", example = "30")
    private Long numDayPrepaid;

    @Schema(description = "Ngày hiệu lực từ", example = "2024-01-01")
    private LocalDateTime effectDateFrom;

    @Schema(description = "Ngày hiệu lực đến", example = "2025-12-31")
    private LocalDateTime effectDateTo;

    @Schema(description = "Ngày cập nhật từ", example = "2024-01-01")
    private LocalDateTime updateDatetimeFrom;

    @Schema(description = "Ngày cập nhật đến", example = "2025-12-31")
    private LocalDateTime updateDatetimeTo;

    @Schema(description = "Mã nhóm nhân viên cửa hàng", example = "SHOP_STAFF_GRP")
    private String groupShopStaffCode;

    @Schema(description = "Có chi tiết lý do", example = "Y")
    private String hasDetailReason;

    @Schema(description = "Mã khuyến mãi mới", example = "PROMO002")
    private String newPromCode;

    @Schema(description = "ID offline", example = "123")
    private Long importOfflineId;

    @Schema(description = "Mã dự án", example = "PRJ001")
    private String projectCode;

    @Schema(description = "Tên dự án", example = "Dự án A")
    private String projectName;

    @Schema(description = "Địa điểm dự án", example = "Hà Nội")
    private String projectLocation;

    @Schema(description = "Giá dự án", example = "100000000")
    private String projectPrice;

    @Schema(description = "Phương thức áp dụng", example = "METHOD_A")
    private String applyMethod;

    @Schema(description = "Giá trị trả trước sau giảm giá", example = "45000")
    private String prepaidValueAfterDiscount;

    @Schema(description = "Tổng phí trả trước", example = "300000")
    private Long totalPrepaidFee;

    @Schema(description = "Tổng phí thường", example = "500000")
    private Long totalNormalFee;

    @Schema(description = "Số thiết bị tối đa", example = "5")
    private String maxDevices;

    private String infraType;
    private String lstBill;
    private Long newChannelTypeId;
    private LocalDateTime newEffectDate;
    private LocalDateTime newEndDate;
    private String staffType;

    @Schema(description = "Mã nhóm phụ", example = "SUB_GRP_CODE")
    private String subGroupCode;

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
            return this.getTelServiceId() == null ? Const.TELECOM_SERVICE_ID.DEFAULT_VALUE_MAP_SELECT_ALL : this.getTelServiceId();
        }
        if ("productCode".equals(propertyName)) {
            return this.getProductCode();
        }
        if ("offerName".equals(propertyName)) {
            return this.getOfferName();
        }
        if ("offerId".equals(propertyName)) {
            return this.getOfferId() == null ? Const.TELECOM_SERVICE_ID.DEFAULT_VALUE_MAP_SELECT_ALL : this.getOfferId();
        }
        if ("regReasonId".equals(propertyName)) {
            return this.getRegReasonId() == null ? Const.TELECOM_SERVICE_ID.DEFAULT_VALUE_MAP_SELECT_ALL : this.getRegReasonId();
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
            return this.getChannelTypeId() == null ? Const.TELECOM_SERVICE_ID.DEFAULT_VALUE_MAP_SELECT_ALL : this.getChannelTypeId();
        }
        if ("status".equals(propertyName)) {
            return this.getStatus();
        }
        if ("channelName".equals(propertyName)) {
            return this.getChannelName();
        }
        if ("provinceCode".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getProvinceCode()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : this.getProvinceCode();
        }
        if ("provinceName".equals(propertyName)) {
            return this.getProvinceName();
        }
        if ("precinctCode".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getPrecinctCode()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : this.getPrecinctCode();
        }
        if ("precinctName".equals(propertyName)) {
            return this.getPrecinctName();
        }
        if ("districtCode".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getDistrictCode()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : this.getDistrictCode();
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
            return DataUtil.isNullOrEmpty(this.getActionCode()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : this.getActionCode();
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
            return DataUtil.isNullOrEmpty(this.getStaffCode()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : this.getStaffCode();
        }
        if ("captcharRequire".equals(propertyName)) {
            return this.getCaptcharRequire();
        }
        if ("unit".equals(propertyName)) {
            return this.getUnit();
        }
        if ("customerGroup".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getCustomerGroup()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : this.getCustomerGroup();
        }
        if ("customerType".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getCustomerType()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : this.getCustomerType();
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
            return this.getStationId() == null ? Const.TELECOM_SERVICE_ID.DEFAULT_VALUE_MAP_SELECT_ALL : this.getStationId();
        }
        if ("stationCodes".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getStationCodes()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : this.getStationCodes();
        }
        if ("technology".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getTechnology()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : this.getTechnology();
        }
        if ("nodeCode".equals(propertyName)) {
            return DataUtil.isNullOrEmpty(this.getNodeCode()) ? Const.DEFAULT_VALUE_MAP_SELECT_ALL : this.getNodeCode();
        }
        return null;
    }
}