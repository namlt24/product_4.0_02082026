package com.viettel.bccs.policy.mapactiveinfo.dto.request;

import com.viettel.bccs.policy.common.dto.FilterRequest;
import com.viettel.bccs.policy.utils.RequiredRoleMap;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String staffCode;
    @Schema(description = "ID kênh cửa hàng", example = "1", maxLength = 10)
    private Long shopChannelTypeId;
    @Schema(description = "Nhóm kênh", example = "3", maxLength = 1000)
    private String groupType;
    @Schema(description = "Hình thức thanh toán", example = "1", maxLength = 1)
    private String payType;
    @Schema(description = "ID gói cước", example = "400000607", maxLength = 10)
    private Long offerId;
    @Schema(description = "Mã tác động", example = "00", maxLength = 10)
    private String actionCode;
    @Schema(description = "Alias dịch vụ", example = "M", maxLength = 3)
    private String serviceType;
    @Schema(description = "Mã tỉnh", example = "H004", maxLength = 5)
    private String province;
    @Schema(description = "Mã huyện", example = "004", maxLength = 5)
    private String district;
    @Schema(description = "Mã xã", example = "001", maxLength = 5)
    private String precint;
    @Schema(description = "Nhóm loại khách hàng", example = "1", maxLength = 1000)
    private String customerGroup;
    @Schema(description = "Loại khách hàng", example = "VIE", maxLength = 6)
    private String customerType;
    @Schema(description = "Loại thuê bao", example = "TBT", maxLength = 4)
    private String subType;
    @Schema(description = "Nhóm thuê bao", example = "1", maxLength = 10)
    private String subGroup;
    @Schema(description = "Mã trạm", example = "CBG0433", maxLength = 1000)
    private String stationCodes;
    @Schema(description = "Mã khuyến mãi", example = "FP001", maxLength = 50)
    private String promotionCode;
    @Schema(description = "Công nghệ", example = "4", maxLength = 10)
    private String technology;
    @Schema(description = "Hình thức thay đổi", example = "1", maxLength = 10)
    private String changeMethod;
    @Schema(description = "Loại hạ tầng", example = "1", maxLength = 10)
    private String infraType;
    @Schema(description = "mode", example = "1", maxLength = 1)
    private Integer mode;
    @Schema(description = "Có lấy thuộc tính lý do/HTHM không", example = "true")
    private Boolean getReasonCharUse;
    @Schema(description = "Danh sách mã quyền", example = "BCCS2_SALE_SAUBAN_DVCD_TDMK_QUYEN_P")
    private RequiredRoleMap roleMap;
    @Schema(description = "Mã node", example = "CBG0433-GN12-SN11-SP01", maxLength = 1000)
    private String nodeCode;
    @Schema(description = "singleOrCombe", example = "1")
    private Long singleOrCombo;
    @Schema(description = "Danh sách thuộc tính")
    private List<FilterRequest> listProductSpec;
    @Schema(description = "Danh sách số giấy tờ")
    private List<String> lstBusinessNo;
    @Schema(description = "Mã địa bàn cha", example = "H004", maxLength = 25)
    private String parentCode;
    @Schema(description = "par_name", example = "ACCEPT_USE_INFO_CUSTOMER_ND13", maxLength = 100)
    private String par_name;
    @Schema(description = "product_offer", maxLength = 100)
    private String productOffer;
    @Schema(description = "Mã gói cưới", example = "POBAS", maxLength = 50)
    private String productCode;
    @Schema(description = "Mã ngân hàng cha", example = "VCB", maxLength = 30)
    private String parentBankCode;
    @Schema(description = "Mã địa bàn", example = "H004001008", maxLength = 25)
    private String areaCode;
    @Schema(description = "Id lý do", example = "9003991499", maxLength = 10)
    private Long regReasonId;
    @Schema(description = "Mã gói cước", example = "SG241", maxLength = 50)
    private String promCode;
    @Schema(description = "Mã lý do", example = "2", maxLength = 20)
    private String regType;
    @Schema(description = "Loại dịch vụ", example = "1", maxLength = 10)
    private String telecomServiceId;
    @Schema(description = "Id lý do", example = "9004010287", maxLength = 10)
    private Long reasonId;
    @Schema(description = "Mã chức năng", example = "DNTT")
    private String functionCode;
    @Schema(description = "Tên chức năng", example = "Đấu nối trả trước")
    private String functionName;
    @Schema(description = "Chi tiết hành vi", example = "{\"serviceType\": \"7\", \"reasonId\": \"123\", \"productCode\": \"PoBas\"}") // JSON string
    private String behaviorDetail;
    @Schema(description = "Cấu hình chức năng", example = "{\"serviceType\": \"7\", \"reasonId\": \"123\", \"productCode\": \"PoBas\"}") // JSON string
    private String functionConfig;
    @Schema(description = "Có lấy địa bàn mới không", example = "false")
    private Boolean isNewArea;
    @Schema(description = "Loại đơn hàng", example = "ORDER1")
    private String orderType;
    @Schema(description = "true: Thêm mới, false: Sửa", example = "true")
    private Boolean isCreate;

    @Schema(maxLength = 100)
    private String addDate;
    @Schema(maxLength = 100)
    private String transCode;
    @Schema(maxLength = 1)
    private String isNew;
    @Schema(maxLength = 100)
    private Integer processStatus;
    private List<String> lstAreaCode;
    @Schema(description = "Id nhân viên", example = "123")
    private Long staffId;
    @Schema(description = "ID loại kênh", example = "80043")
    private String channelTypeId;
    @Schema(description = "Loại tổ chức", example = "1")
    private String organizationType;
    @Schema(description = "Mã đơn vị trên product", example = "VT")
    private String shopCode;

    @Schema(description = "Tên nhân viên", example = "NamLT")
    private String name;

    @Schema(description = "Ngày sinh nhật", example = "1999")
    private String birthday;

    @Schema(description = "Số điện thoại liên hệ")
    private String tel;

    @Schema(description = "email")
    private String email;

    @Schema(description = "Số giấy tờ")
    private String idNo;

    @Schema(description = "Ngày cấp")
    private String idIssueDate;

    @Schema(description = "Nơi cấp")
    private String idIssuePlace;

    @Schema(description = "Mã thông tin nhân sự")
    private String ttnsCode;

    @Schema(description = "Từ khóa tìm kiếm")
    private String keySearch;

    @Schema(description = "Index trang")
    private String  pageIndex;

    @Schema(description = "Số trang")
    private String  pageSize;

    @Schema(description = "Danh sách Id đơn vị")
    private List<Long> shopIds;

    @Schema(description = "Id đơn vị")
    private Long shopId;

    @Schema(description = "Danh sách mã xã", example = "001", maxLength = 5)
    private List<String> precints;

    @Schema(description = "Id Shop hiện tại")
    private Long currentShopId;

    @Schema(description = "Cấp đơn vị")
    private Long shopLevel;

    @Schema(description = "Mã số thuế")
    private String tin;

    @Schema(description = "Mã đơn vị ngân sách")
    private String shopDvns;

    @Schema(description = "Danh sách Id nhân viên")
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