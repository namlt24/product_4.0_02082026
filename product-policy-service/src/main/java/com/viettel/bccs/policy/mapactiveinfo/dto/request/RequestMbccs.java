package com.viettel.bccs.policy.mapactiveinfo.dto.request;

import com.viettel.bccs.policy.common.dto.FilterRequest;
import com.viettel.bccs.policy.utils.RequiredRoleMap;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO cho API getProductCodeByMapActiveInfo.
 * Gom cac thong tin nhan vien, loai thanh toan, ma hanh dong, dich vu, vai tro.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class RequestMbccs {
    @Schema(description = "Mã nhân viên", example = "VTT1", maxLength = 40)
    @Size(max = 40, message = "staffCode tối đa 40 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "staffCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String staffCode;
    @Schema(description = "ID kênh cửa hàng", example = "1", maxLength = 10)
    @Min(value = 1, message = "shopChannelTypeId phải >= 1")
    @Max(value = 9999999999L, message = "shopChannelTypeId vượt quá độ dài cho phép")
    private Long shopChannelTypeId;
    @Schema(description = "Nhóm kênh", example = "3", maxLength = 1000)
    @Size(max = 1000, message = "groupType tối đa 1000 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9,_-]{0,1000}$", message = "groupType chỉ gồm chữ, số, ',', '_' hoặc '-'")
    private String groupType;
    @Schema(description = "Hình thức thanh toán", example = "1", maxLength = 1)
    @Size(max = 1, message = "payType tối đa 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "payType chỉ gồm chữ hoặc số")
    private String payType;
    @Schema(description = "ID gói cước", example = "400000607", maxLength = 10)
    @Min(value = 1, message = "offerId phải >= 1")
    @Max(value = 9999999999L, message = "offerId vượt quá độ dài cho phép")
    private Long offerId;
    @Schema(description = "Mã tác động", example = "00", maxLength = 10)
    @Size(max = 10, message = "actionCode tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "actionCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String actionCode;
    @Schema(description = "Alias dịch vụ", example = "M", maxLength = 3)
    @Size(max = 3, message = "serviceType tối đa 3 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,3}$", message = "serviceType chỉ gồm chữ, số, '_' hoặc '-'")
    private String serviceType;
    @Schema(description = "Mã tỉnh", example = "H004", maxLength = 5)
    @Size(max = 5, message = "province tối đa 5 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,5}$", message = "province chỉ gồm chữ, số, '_' hoặc '-'")
    private String province;
    @Schema(description = "Mã huyện", example = "004", maxLength = 5)
    @Size(max = 5, message = "district tối đa 5 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,5}$", message = "district chỉ gồm chữ, số, '_' hoặc '-'")
    private String district;
    @Schema(description = "Mã xã", example = "001", maxLength = 5)
    @Size(max = 5, message = "precint tối đa 5 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,5}$", message = "precint chỉ gồm chữ, số, '_' hoặc '-'")
    private String precint;
    @Schema(description = "Nhóm loại khách hàng", example = "1", maxLength = 1000)
    @Size(max = 1000, message = "customerGroup tối đa 1000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "customerGroup không được chứa ký tự điều khiển")
    private String customerGroup;
    @Schema(description = "Loại khách hàng", example = "VIE", maxLength = 6)
    @Size(max = 6, message = "customerType tối đa 6 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,6}$", message = "customerType chỉ gồm chữ, số, '_' hoặc '-'")
    private String customerType;
    @Schema(description = "Loại thuê bao", example = "TBT", maxLength = 4)
    @Size(max = 4, message = "subType tối đa 4 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,4}$", message = "subType chỉ gồm chữ, số, '_' hoặc '-'")
    private String subType;
    @Schema(description = "Nhóm thuê bao", example = "1", maxLength = 10)
    @Size(max = 10, message = "subGroup tối đa 10 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,10}$", message = "subGroup không được chứa ký tự điều khiển")
    private String subGroup;
    @Schema(description = "Mã trạm", example = "CBG0433", maxLength = 1000)
    @Size(max = 1000, message = "stationCodes tối đa 1000 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9,_-]{0,1000}$", message = "stationCodes chỉ gồm chữ, số, ',', '_' hoặc '-'")
    private String stationCodes;
    @Schema(description = "Mã khuyến mãi", example = "FP001", maxLength = 50)
    @Size(max = 50, message = "promotionCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "promotionCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String promotionCode;
    @Schema(description = "Công nghệ", example = "4", maxLength = 10)
    @Size(max = 10, message = "technology tối đa 10 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,10}$", message = "technology không được chứa ký tự điều khiển")
    private String technology;
    @Schema(description = "Hình thức thay đổi", example = "1", maxLength = 10)
    @Size(max = 10, message = "changeMethod tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "changeMethod chỉ gồm chữ, số, '_' hoặc '-'")
    private String changeMethod;
    @Schema(description = "Loại hạ tầng", example = "1", maxLength = 10)
    @Size(max = 10, message = "infraType tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "infraType chỉ gồm chữ, số, '_' hoặc '-'")
    private String infraType;
    @Schema(description = "mode", example = "1", maxLength = 1)
    @Min(value = 0, message = "mode phải >= 0")
    @Max(value = 9, message = "mode phải <= 9")
    private Integer mode;
    @Schema(description = "Có lấy thuộc tính lý do/HTHM không", example = "true")
    private Boolean getReasonCharUse;
    @Schema(description = "Danh sách mã quyền", example = "BCCS2_SALE_SAUBAN_DVCD_TDMK_QUYEN_P")
    private RequiredRoleMap roleMap;
    @Schema(description = "Mã node", example = "CBG0433-GN12-SN11-SP01", maxLength = 1000)
    @Size(max = 1000, message = "nodeCode tối đa 1000 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,1000}$", message = "nodeCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String nodeCode;
    @Schema(description = "singleOrCombe", example = "1")
    @Min(value = 0, message = "singleOrCombo phải >= 0")
    @Max(value = 9, message = "singleOrCombo phải <= 9")
    private Long singleOrCombo;
    @Schema(description = "Danh sách thuộc tính")
    @Size(min = 0, max = 200, message = "listProductSpec tối đa 200 phần tử")
    private List<FilterRequest> listProductSpec;
    @Schema(description = "Danh sách số giấy tờ")
    @Size(min = 0, max = 100, message = "lstBusinessNo tối đa 100 phần tử")
    private List<String> lstBusinessNo;
    @Schema(description = "Mã địa bàn cha", example = "H004", maxLength = 25)
    @Size(max = 25, message = "parentCode tối đa 25 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,25}$", message = "parentCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String parentCode;
    @Schema(description = "par_name", example = "ACCEPT_USE_INFO_CUSTOMER_ND13", maxLength = 100)
    @Size(max = 100, message = "par_name tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "par_name không được chứa ký tự điều khiển")
    private String par_name;
    @Schema(description = "product_offer", maxLength = 100)
    @Size(max = 100, message = "productOffer tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "productOffer không được chứa ký tự điều khiển")
    private String productOffer;
    @Schema(description = "Mã gói cưới", example = "POBAS", maxLength = 50)
    @Size(max = 50, message = "productCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "productCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String productCode;
    @Schema(description = "Mã ngân hàng cha", example = "VCB", maxLength = 30)
    @Size(max = 30, message = "parentBankCode tối đa 30 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,30}$", message = "parentBankCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String parentBankCode;
    @Schema(description = "Mã địa bàn", example = "H004001008", maxLength = 25)
    @Size(max = 25, message = "areaCode tối đa 25 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,25}$", message = "areaCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String areaCode;
    @Schema(description = "Id lý do", example = "9003991499", maxLength = 10)
    @Min(value = 1, message = "regReasonId phải >= 1")
    @Max(value = 9999999999L, message = "regReasonId vượt quá độ dài cho phép")
    private Long regReasonId;
    @Schema(description = "Mã gói cước", example = "SG241", maxLength = 50)
    @Size(max = 50, message = "promCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "promCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String promCode;
    @Schema(description = "Mã lý do", example = "2", maxLength = 20)
    @Size(max = 20, message = "regType tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "regType chỉ gồm chữ, số, '_' hoặc '-'")
    private String regType;
    @Schema(description = "Loại dịch vụ", example = "1", maxLength = 10)
    @Size(max = 10, message = "telecomServiceId tối đa 10 ký tự")
    @Pattern(regexp = "^[0-9]{0,10}$", message = "telecomServiceId chỉ gồm số")
    private String telecomServiceId;
    @Schema(description = "Id lý do", example = "9004010287", maxLength = 10)
    @Min(value = 1, message = "reasonId phải >= 1")
    @Max(value = 9999999999L, message = "reasonId vượt quá độ dài cho phép")
    private Long reasonId;
    @Schema(description = "Mã chức năng", example = "DNTT")
    @Size(max = 50, message = "functionCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "functionCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String functionCode;
    @Schema(description = "Tên chức năng", example = "Đấu nối trả trước")
    @Size(max = 200, message = "functionName tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "functionName không được chứa ký tự điều khiển")
    private String functionName;
    @Schema(description = "Chi tiết hành vi", example = "{\"serviceType\": \"7\", \"reasonId\": \"123\", \"productCode\": \"PoBas\"}") // JSON string
    @Size(max = 4000, message = "behaviorDetail tối đa 4000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "behaviorDetail không được chứa ký tự điều khiển")
    private String behaviorDetail;
    @Schema(description = "Cấu hình chức năng", example = "{\"serviceType\": \"7\", \"reasonId\": \"123\", \"productCode\": \"PoBas\"}") // JSON string
    @Size(max = 4000, message = "functionConfig tối đa 4000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "functionConfig không được chứa ký tự điều khiển")
    private String functionConfig;
    @Schema(description = "Có lấy địa bàn mới không", example = "false")
    private Boolean isNewArea;
    @Schema(description = "Loại đơn hàng", example = "ORDER1")
    @Size(max = 50, message = "orderType tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "orderType chỉ gồm chữ, số, '_' hoặc '-'")
    private String orderType;
    @Schema(description = "true: Thêm mới, false: Sửa", example = "true")
    private Boolean isCreate;

    @Schema(maxLength = 100)
    @Size(max = 100, message = "addDate tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "addDate không được chứa ký tự điều khiển")
    private String addDate;
    @Schema(maxLength = 100)
    @Size(max = 100, message = "transCode tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "transCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String transCode;
    @Schema(maxLength = 1)
    @Size(max = 1, message = "isNew tối đa 1 ký tự")
    @Pattern(regexp = "^[01]{0,1}$", message = "isNew chỉ nhận giá trị 0 hoặc 1")
    private String isNew;
    @Schema(maxLength = 100)
    @Min(value = 0, message = "processStatus phải >= 0")
    @Max(value = 99, message = "processStatus phải <= 99")
    private Integer processStatus;
    @Size(min = 0, max = 1000, message = "lstAreaCode tối đa 1000 phần tử")
    private List<String> lstAreaCode;
    @Schema(description = "Id nhân viên", example = "123")
    @Min(value = 1, message = "staffId phải >= 1")
    @Max(value = 9999999999L, message = "staffId vượt quá độ dài cho phép")
    private Long staffId;
    @Schema(description = "ID loại kênh", example = "80043")
    @Size(max = 20, message = "channelTypeId tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9]{0,20}$", message = "channelTypeId chỉ gồm số")
    private String channelTypeId;
    @Schema(description = "Loại tổ chức", example = "1")
    @Size(max = 50, message = "organizationType tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "organizationType chỉ gồm chữ, số, '_' hoặc '-'")
    private String organizationType;
    @Schema(description = "Mã đơn vị trên product", example = "VT")
    @Size(max = 50, message = "shopCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "shopCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String shopCode;

    @Schema(description = "Tên nhân viên", example = "NamLT")
    @Size(max = 200, message = "name tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Ngày sinh nhật", example = "1999")
    @Size(max = 20, message = "birthday tối đa 20 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,20}$", message = "birthday không được chứa ký tự điều khiển")
    private String birthday;

    @Schema(description = "Số điện thoại liên hệ")
    @Size(max = 20, message = "tel tối đa 20 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,20}$", message = "tel không được chứa ký tự điều khiển")
    private String tel;

    @Schema(description = "email")
    @Size(max = 100, message = "email tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "email không được chứa ký tự điều khiển")
    private String email;

    @Schema(description = "Số giấy tờ")
    @Size(max = 20, message = "idNo tối đa 20 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,20}$", message = "idNo không được chứa ký tự điều khiển")
    private String idNo;

    @Schema(description = "Ngày cấp")
    @Size(max = 50, message = "idIssueDate tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "idIssueDate không được chứa ký tự điều khiển")
    private String idIssueDate;

    @Schema(description = "Nơi cấp")
    @Size(max = 200, message = "idIssuePlace tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "idIssuePlace không được chứa ký tự điều khiển")
    private String idIssuePlace;

    @Schema(description = "Mã thông tin nhân sự")
    @Size(max = 50, message = "ttnsCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "ttnsCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String ttnsCode;

    @Schema(description = "Từ khóa tìm kiếm")
    @Size(max = 200, message = "keySearch tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "keySearch không được chứa ký tự điều khiển")
    private String keySearch;

    @Schema(description = "Index trang")
    @Size(max = 10, message = "pageIndex tối đa 10 ký tự")
    @Pattern(regexp = "^[0-9]{0,10}$", message = "pageIndex chỉ gồm số")
    private String  pageIndex;

    @Schema(description = "Số trang")
    @Size(max = 10, message = "pageSize tối đa 10 ký tự")
    @Pattern(regexp = "^[0-9]{0,10}$", message = "pageSize chỉ gồm số")
    private String  pageSize;

    @Schema(description = "Danh sách Id đơn vị")
    @Size(min = 0, max = 500, message = "shopIds tối đa 500 phần tử")
    private List<Long> shopIds;

    @Schema(description = "Id đơn vị")
    @Min(value = 1, message = "shopId phải >= 1")
    @Max(value = 9999999999L, message = "shopId vượt quá độ dài cho phép")
    private Long shopId;

    @Schema(description = "Danh sách mã xã", example = "001", maxLength = 5)
    @Size(min = 0, max = 100, message = "precints tối đa 100 phần tử")
    private List<String> precints;

    @Schema(description = "Id Shop hiện tại")
    @Min(value = 1, message = "currentShopId phải >= 1")
    @Max(value = 9999999999L, message = "currentShopId vượt quá độ dài cho phép")
    private Long currentShopId;

    @Schema(description = "Cấp đơn vị")
    @Min(value = 0, message = "shopLevel phải >= 0")
    @Max(value = 20, message = "shopLevel phải <= 20")
    private Long shopLevel;

    @Schema(description = "Mã số thuế")
    @Size(max = 20, message = "tin tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9]{0,20}$", message = "tin chỉ gồm số")
    private String tin;

    @Schema(description = "Mã đơn vị ngân sách")
    @Size(max = 20, message = "shopDvns tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "shopDvns chỉ gồm chữ, số, '_' hoặc '-'")
    private String shopDvns;

    @Schema(description = "Danh sách Id nhân viên")
    @Size(min = 0, max = 500, message = "lstStaffId tối đa 500 phần tử")
    private List<Long> lstStaffId;

    public String getChannelTypeId() {
        return channelTypeId;
    }

    public void setChannelTypeId(String channelTypeId) {
        this.channelTypeId = channelTypeId;
    }

    public String getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(String organizationType) {
        this.organizationType = organizationType;
    }

    public String getPar_name() {
        return par_name;
    }

    public void setPar_name(String par_name) {
        this.par_name = par_name;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getPrecint() {
        return precint;
    }

    public void setPrecint(String precint) {
        this.precint = precint;
    }

    public String getCustomerGroup() {
        return customerGroup;
    }

    public void setCustomerGroup(String customerGroup) {
        this.customerGroup = customerGroup;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(String subGroup) {
        this.subGroup = subGroup;
    }

    public String getStationCodes() {
        return stationCodes;
    }

    public void setStationCodes(String stationCodes) {
        this.stationCodes = stationCodes;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getChangeMethod() {
        return changeMethod;
    }

    public void setChangeMethod(String changeMethod) {
        this.changeMethod = changeMethod;
    }

    public String getInfraType() {
        return infraType;
    }

    public void setInfraType(String infraType) {
        this.infraType = infraType;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public Boolean isGetReasonCharUse() {
        return getReasonCharUse;
    }

    public void setGetReasonCharUse(Boolean getReasonCharUse) {
        this.getReasonCharUse = getReasonCharUse;
    }

    public RequiredRoleMap getRoleMap() {
        return roleMap;
    }

    public void setRoleMap(RequiredRoleMap roleMap) {
        this.roleMap = roleMap;
    }

    public String getNodeCode() {
        return nodeCode;
    }

    public void setNodeCode(String nodeCode) {
        this.nodeCode = nodeCode;
    }

    public Long getSingleOrCombo() {
        return singleOrCombo;
    }

    public void setSingleOrCombo(Long singleOrCombo) {
        this.singleOrCombo = singleOrCombo;
    }

    public List<FilterRequest> getListProductSpec() {
        return listProductSpec;
    }

    public void setListProductSpec(List<FilterRequest> listProductSpec) {
        this.listProductSpec = listProductSpec;
    }

    public List<String> getLstBusinessNo() {
        return lstBusinessNo;
    }

    public void setLstBusinessNo(List<String> lstBusinessNo) {
        this.lstBusinessNo = lstBusinessNo;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getStaffCode() {
        return staffCode;
    }

    public void setStaffCode(String staffCode) {
        this.staffCode = staffCode;
    }

    public Long getShopChannelTypeId() {
        return shopChannelTypeId;
    }

    public void setShopChannelTypeId(Long shopChannelTypeId) {
        this.shopChannelTypeId = shopChannelTypeId;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getProductOffer() {
        return productOffer;
    }

    public void setProductOffer(String productOffer) {
        this.productOffer = productOffer;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getParentBankCode() {
        return parentBankCode;
    }

    public void setParentBankCode(String parentBankCode) {
        this.parentBankCode = parentBankCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public Long getRegReasonId() {
        return regReasonId;
    }

    public void setRegReasonId(Long regReasonId) {
        this.regReasonId = regReasonId;
    }

    public String getPromCode() {
        return promCode;
    }

    public void setPromCode(String promCode) {
        this.promCode = promCode;
    }

    public String getRegType() {
        return regType;
    }

    public void setRegType(String regType) {
        this.regType = regType;
    }

    public String getTelecomServiceId() {
        return telecomServiceId;
    }

    public void setTelecomServiceId(String telecomServiceId) {
        this.telecomServiceId = telecomServiceId;
    }

    public Long getReasonId() {
        return reasonId;
    }

    public void setReasonId(Long reasonId) {
        this.reasonId = reasonId;
    }

    public String getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(String functionCode) {
        this.functionCode = functionCode;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getBehaviorDetail() {
        return behaviorDetail;
    }

    public void setBehaviorDetail(String behaviorDetail) {
        this.behaviorDetail = behaviorDetail;
    }

    public String getFunctionConfig() {
        return functionConfig;
    }

    public void setFunctionConfig(String functionConfig) {
        this.functionConfig = functionConfig;
    }

    public Boolean isNewArea() {
        return isNewArea;
    }

    public void setNewArea(Boolean newArea) {
        isNewArea = newArea;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getAddDate() {
        return addDate;
    }

    public void setAddDate(String addDate) {
        this.addDate = addDate;
    }

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public Integer getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(Integer processStatus) {
        this.processStatus = processStatus;
    }

    public String getIsNew() {
        return isNew;
    }

    public void setIsNew(String isNew) {
        this.isNew = isNew;
    }

    public Boolean getIsCreate() {
        return isCreate;
    }

    public void setIsCreate(Boolean create) {
        isCreate = create;
    }

    public List<String> getLstAreaCode() {
        return lstAreaCode;
    }

    public void setLstAreaCode(List<String> lstAreaCode) {
        this.lstAreaCode = lstAreaCode;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public String getShopCode() {
        return shopCode;
    }

    public void setShopCode(String shopCode) {
        this.shopCode = shopCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getIdIssueDate() {
        return idIssueDate;
    }

    public void setIdIssueDate(String idIssueDate) {
        this.idIssueDate = idIssueDate;
    }

    public String getIdIssuePlace() {
        return idIssuePlace;
    }

    public void setIdIssuePlace(String idIssuePlace) {
        this.idIssuePlace = idIssuePlace;
    }

    public String getTtnsCode() {
        return ttnsCode;
    }

    public void setTtnsCode(String ttnsCode) {
        this.ttnsCode = ttnsCode;
    }

    public String getKeySearch() {
        return keySearch;
    }

    public void setKeySearch(String keySearch) {
        this.keySearch = keySearch;
    }

    public String getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(String pageIndex) {
        this.pageIndex = pageIndex;
    }

    public String getPageSize() {
        return pageSize;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

    public List<Long> getShopIds() {
        return shopIds;
    }

    public void setShopIds(List<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public List<String> getPrecints() {
        return precints;
    }

    public void setPrecints(List<String> precints) {
        this.precints = precints;
    }

    public Long getCurrentShopId() {
        return currentShopId;
    }

    public void setCurrentShopId(Long currentShopId) {
        this.currentShopId = currentShopId;
    }

    public Long getShopLevel() {
        return shopLevel;
    }

    public void setShopLevel(Long shopLevel) {
        this.shopLevel = shopLevel;
    }

    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    public String getShopDvns() {
        return shopDvns;
    }

    public void setShopDvns(String shopDvns) {
        this.shopDvns = shopDvns;
    }

    public List<Long> getLstStaffId() {
        return lstStaffId;
    }

    public void setLstStaffId(List<Long> lstStaffId) {
        this.lstStaffId = lstStaffId;
    }
}