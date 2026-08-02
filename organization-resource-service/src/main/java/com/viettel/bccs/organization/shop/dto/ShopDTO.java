package com.viettel.bccs.organization.shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;

@Schema
public class ShopDTO {

    @Schema(description = "ID cửa hàng", example = "12345")
    private Long shopId;

    @Schema(description = "Tên cửa hàng", example = "Viettel Store Hà Nội")
    private String name;

    @Schema(description = "ID cửa hàng cha", example = "10000")
    private Long parentShopId;

    @Schema(description = "Tài khoản ngân hàng", example = "1234567890")
    private String account;

    @Schema(description = "Tên ngân hàng", example = "Vietcombank")
    private String bankName;

    @Schema(description = "Địa chỉ cửa hàng", example = "123 Nguyễn Trãi, Quận 1, TP HCM")
    private String address;

    @Schema(description = "Số điện thoại", example = "0909123456")
    private String tel;

    @Schema(description = "Số fax", example = "02812345678")
    private String fax;

    @Schema(description = "Mã cửa hàng", example = "VTST_HN_001")
    private String shopCode;

    @Schema(description = "Loại cửa hàng", example = "1")
    private String shopType;

    @Schema(description = "Tên người liên hệ", example = "Nguyễn Văn A")
    private String contactName;

    @Schema(description = "Chức danh người liên hệ", example = "Giám đốc")
    private String contactTitle;

    @Schema(description = "Số điện thoại liên hệ", example = "0909123456")
    private String telNumber;

    @Schema(description = "Email", example = "contact@viettel.vn")
    private String email;

    @Schema(description = "Mô tả", example = "Cửa hàng Viettel Store")
    private String description;

    @Schema(description = "Tỉnh/Thành phố", example = "Hà Nội")
    private String province;

    @Schema(description = "Mã cửa hàng cha", example = "VTST_HN")
    private String parShopCode;

    @Schema(description = "Mã trung tâm", example = "1")
    private String centerCode;

    @Schema(description = "Mã cũ của cửa hàng", example = "OLD123")
    private String oldShopCode;

    @Schema(description = "Thông tin công ty", example = "Viettel Group")
    private String company;

    @Schema(description = "Mã số thuế", example = "0123456789")
    private String tin;

    @Schema(description = "Cửa hàng", example = "Viettel")
    private String shop;

    @Schema(description = "Mã tỉnh", example = "HN")
    private String provinceCode;

    @Schema(description = "Thanh toán hoa hồng", example = "Y")
    private String payComm;

    @Schema(description = "Ngày tạo", example = "2024-01-01")
    private Date createDate;

    @Schema(description = "ID loại kênh", example = "1")
    private Long channelTypeId;

    @Schema(description = "Chính sách giảm giá", example = "DISC_001")
    private String discountPolicy;

    @Schema(description = "Chính sách giá", example = "PRICE_001")
    private String pricePolicy;

    @Schema(description = "Đường dẫn cửa hàng", example = "/HN/VTST_HN_001")
    private String shopPath;

    @Schema(description = "Quận/Huyện", example = "Ba Đình")
    private String district;

    @Schema(description = "Phường/Xã", example = "Phường 1")
    private String precinct;

    @Schema(description = "Mã vùng", example = "HN")
    private String areaCode;

    @Schema(description = "Số CMND/CCCD", example = "001234567890")
    private String idNo;

    @Schema(description = "Nơi cấp CMND/CCCD", example = "Hà Nội")
    private String idIssuePlace;

    @Schema(description = "Ngày cấp CMND/CCCD", example = "2020-01-01")
    private Date idIssueDate;

    @Schema(description = "Khối đường", example = "Phường 1")
    private String streetBlock;

    @Schema(description = "Đường", example = "Nguyễn Trãi")
    private String street;

    @Schema(description = "Số nhà", example = "123")
    private String home;

    @Schema(description = "Loại giấy tờ", example = "1")
    private Integer idType;

    @Schema(description = "Đường dẫn tên cửa hàng", example = "Hà Nội > Ba Đình > Viettel Store")
    private String shopPathName;

    @Schema(description = "Số hợp đồng", example = "HD_2024_001")
    private String contractNo;

    @Schema(description = "Tên file đính kèm", example = "hop_dong.pdf")
    private String fileName;

    @Schema(description = "Giấy phép kinh doanh", example = "GPKD_001")
    private String businessLicence;

    @Schema(description = "Số bankplus", example = "0909123456")
    private String bankplusMobile;

    @Schema(description = "Số kho nhập", example = "100")
    private Integer stockNum;

    @Schema(description = "Số kho xuất", example = "50")
    private Integer stockNumImp;

    @Schema(description = "Ngày cập nhật", example = "2024-06-01")
    private Date updateDateTime;

    @Schema(description = "Mã ngân hàng", example = "VCB")
    private String bankCode;

    @Schema(description = "ID người quản lý cửa hàng", example = "999")
    private Long shopKeeperId;

    @Schema(description = "ID giám đốc cửa hàng", example = "888")
    private Long shopDirectorId;

    @Schema(description = "ID nhóm loại kênh", example = "5")
    private Long groupChannelTypeId;

    @Schema(description = "ID chủ sở hữu nhân viên", example = "100")
    private Long staffOwnerId;

    @Schema(description = "ID tenant", example = "1")
    private Long tenantId;

    @Schema(description = "Tỉnh kinh doanh", example = "HN")
    private String businessProvince;

    @Schema(description = "Quận kinh doanh", example = "Ba Đình")
    private String businessDistrict;

    @Schema(description = "Phường kinh doanh", example = "Phường 1")
    private String businessPrecinct;

    @Schema(description = "Khối đường kinh doanh", example = "Phường 1")
    private String businessStreetBlock;

    @Schema(description = "Đường kinh doanh", example = "Nguyễn Trãi")
    private String businessStreet;

    @Schema(description = "Số nhà kinh doanh", example = "456")
    private String businessHome;

    @Schema(description = "Mã vùng kinh doanh", example = "HN")
    private String businessAreacode;

    @Schema(description = "Địa chỉ kinh doanh", example = "456 Nguyễn Trãi, Ba Đình, Hà Nội")
    private String businessAddress;

    @Schema(description = "Người tạo", example = "admin")
    private String createUser;

    @Schema(description = "Người cập nhật", example = "admin")
    private String updateUser;

    @Schema(description = "Ngày tạo", example = "2024-01-01")
    private Date createDatetime;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Doanh thu", example = "Y")
    private String turnover;

    @Schema(description = "Ngày sinh", example = "1990-01-01")
    private Date birthday;

    // --- Getters ---

    public Long getShopId() { return shopId; }
    public String getName() { return name; }
    public Long getParentShopId() { return parentShopId; }
    public String getAccount() { return account; }
    public String getBankName() { return bankName; }
    public String getAddress() { return address; }
    public String getTel() { return tel; }
    public String getFax() { return fax; }
    public String getShopCode() { return shopCode; }
    public String getShopType() { return shopType; }
    public String getContactName() { return contactName; }
    public String getContactTitle() { return contactTitle; }
    public String getTelNumber() { return telNumber; }
    public String getEmail() { return email; }
    public String getDescription() { return description; }
    public String getProvince() { return province; }
    public String getParShopCode() { return parShopCode; }
    public String getCenterCode() { return centerCode; }
    public String getOldShopCode() { return oldShopCode; }
    public String getCompany() { return company; }
    public String getTin() { return tin; }
    public String getShop() { return shop; }
    public String getProvinceCode() { return provinceCode; }
    public String getPayComm() { return payComm; }
    public Date getCreateDate() { return createDate; }
    public Long getChannelTypeId() { return channelTypeId; }
    public String getDiscountPolicy() { return discountPolicy; }
    public String getPricePolicy() { return pricePolicy; }
    public String getShopPath() { return shopPath; }
    public String getDistrict() { return district; }
    public String getPrecinct() { return precinct; }
    public String getAreaCode() { return areaCode; }
    public String getIdNo() { return idNo; }
    public String getIdIssuePlace() { return idIssuePlace; }
    public Date getIdIssueDate() { return idIssueDate; }
    public String getStreetBlock() { return streetBlock; }
    public String getStreet() { return street; }
    public String getHome() { return home; }
    public Integer getIdType() { return idType; }
    public String getShopPathName() { return shopPathName; }
    public String getContractNo() { return contractNo; }
    public String getFileName() { return fileName; }
    public String getBusinessLicence() { return businessLicence; }
    public String getBankplusMobile() { return bankplusMobile; }
    public Integer getStockNum() { return stockNum; }
    public Integer getStockNumImp() { return stockNumImp; }
    public Date getUpdateDateTime() { return updateDateTime; }
    public String getBankCode() { return bankCode; }
    public Long getShopKeeperId() { return shopKeeperId; }
    public Long getShopDirectorId() { return shopDirectorId; }
    public Long getGroupChannelTypeId() { return groupChannelTypeId; }
    public Long getStaffOwnerId() { return staffOwnerId; }
    public Long getTenantId() { return tenantId; }
    public String getBusinessProvince() { return businessProvince; }
    public String getBusinessDistrict() { return businessDistrict; }
    public String getBusinessPrecinct() { return businessPrecinct; }
    public String getBusinessStreetBlock() { return businessStreetBlock; }
    public String getBusinessStreet() { return businessStreet; }
    public String getBusinessHome() { return businessHome; }
    public String getBusinessAreacode() { return businessAreacode; }
    public String getBusinessAddress() { return businessAddress; }
    public String getCreateUser() { return createUser; }
    public String getUpdateUser() { return updateUser; }
    public Date getCreateDatetime() { return createDatetime; }
    public String getStatus() { return status; }
    public String getTurnover() { return turnover; }
    public Date getBirthday() { return birthday; }

    // --- Setters ---

    public void setShopId(Long shopId) { this.shopId = shopId; }
    public void setName(String name) { this.name = name; }
    public void setParentShopId(Long parentShopId) { this.parentShopId = parentShopId; }
    public void setAccount(String account) { this.account = account; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public void setAddress(String address) { this.address = address; }
    public void setTel(String tel) { this.tel = tel; }
    public void setFax(String fax) { this.fax = fax; }
    public void setShopCode(String shopCode) { this.shopCode = shopCode; }
    public void setShopType(String shopType) { this.shopType = shopType; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public void setContactTitle(String contactTitle) { this.contactTitle = contactTitle; }
    public void setTelNumber(String telNumber) { this.telNumber = telNumber; }
    public void setEmail(String email) { this.email = email; }
    public void setDescription(String description) { this.description = description; }
    public void setProvince(String province) { this.province = province; }
    public void setParShopCode(String parShopCode) { this.parShopCode = parShopCode; }
    public void setCenterCode(String centerCode) { this.centerCode = centerCode; }
    public void setOldShopCode(String oldShopCode) { this.oldShopCode = oldShopCode; }
    public void setCompany(String company) { this.company = company; }
    public void setTin(String tin) { this.tin = tin; }
    public void setShop(String shop) { this.shop = shop; }
    public void setProvinceCode(String provinceCode) { this.provinceCode = provinceCode; }
    public void setPayComm(String payComm) { this.payComm = payComm; }
    public void setCreateDate(Date createDate) { this.createDate = createDate; }
    public void setChannelTypeId(Long channelTypeId) { this.channelTypeId = channelTypeId; }
    public void setDiscountPolicy(String discountPolicy) { this.discountPolicy = discountPolicy; }
    public void setPricePolicy(String pricePolicy) { this.pricePolicy = pricePolicy; }
    public void setShopPath(String shopPath) { this.shopPath = shopPath; }
    public void setDistrict(String district) { this.district = district; }
    public void setPrecinct(String precinct) { this.precinct = precinct; }
    public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
    public void setIdNo(String idNo) { this.idNo = idNo; }
    public void setIdIssuePlace(String idIssuePlace) { this.idIssuePlace = idIssuePlace; }
    public void setIdIssueDate(Date idIssueDate) { this.idIssueDate = idIssueDate; }
    public void setStreetBlock(String streetBlock) { this.streetBlock = streetBlock; }
    public void setStreet(String street) { this.street = street; }
    public void setHome(String home) { this.home = home; }
    public void setIdType(Integer idType) { this.idType = idType; }
    public void setShopPathName(String shopPathName) { this.shopPathName = shopPathName; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setBusinessLicence(String businessLicence) { this.businessLicence = businessLicence; }
    public void setBankplusMobile(String bankplusMobile) { this.bankplusMobile = bankplusMobile; }
    public void setStockNum(Integer stockNum) { this.stockNum = stockNum; }
    public void setStockNumImp(Integer stockNumImp) { this.stockNumImp = stockNumImp; }
    public void setUpdateDateTime(Date updateDateTime) { this.updateDateTime = updateDateTime; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    public void setShopKeeperId(Long shopKeeperId) { this.shopKeeperId = shopKeeperId; }
    public void setShopDirectorId(Long shopDirectorId) { this.shopDirectorId = shopDirectorId; }
    public void setGroupChannelTypeId(Long groupChannelTypeId) { this.groupChannelTypeId = groupChannelTypeId; }
    public void setStaffOwnerId(Long staffOwnerId) { this.staffOwnerId = staffOwnerId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public void setBusinessProvince(String businessProvince) { this.businessProvince = businessProvince; }
    public void setBusinessDistrict(String businessDistrict) { this.businessDistrict = businessDistrict; }
    public void setBusinessPrecinct(String businessPrecinct) { this.businessPrecinct = businessPrecinct; }
    public void setBusinessStreetBlock(String businessStreetBlock) { this.businessStreetBlock = businessStreetBlock; }
    public void setBusinessStreet(String businessStreet) { this.businessStreet = businessStreet; }
    public void setBusinessHome(String businessHome) { this.businessHome = businessHome; }
    public void setBusinessAreacode(String businessAreacode) { this.businessAreacode = businessAreacode; }
    public void setBusinessAddress(String businessAddress) { this.businessAddress = businessAddress; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
    public void setCreateDatetime(Date createDatetime) { this.createDatetime = createDatetime; }
    public void setStatus(String status) { this.status = status; }
    public void setTurnover(String turnover) { this.turnover = turnover; }
    public void setBirthday(Date birthday) { this.birthday = birthday; }
}