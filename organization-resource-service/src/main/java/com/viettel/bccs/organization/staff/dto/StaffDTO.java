package com.viettel.bccs.organization.staff.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Schema
public class StaffDTO implements Serializable {

    public StaffDTO() {
    }

    public StaffDTO(String staffCode) {
        this.staffCode = staffCode;
    }

    public static enum FILTER {staffCode, name, staffId, status}

    public static enum COLUMNS {address, areaCode, bankplusMobile, birthday, btsCode, businessLicence,
        businessMethod, channelTypeId, contractFromDate, contractMethod, contractNo, contractToDate,
        depositValue, discountPolicy, district, email, fileName, hasEquipment, hasTin, home, idIssueDate,
        idIssuePlace, idNo, idType, imsi, isdn, lastLockTime, lastModified, lockFlag, lockStatus, name, note,
        paymentLimit, paymentUsage, pin, pointOfSale, pointOfSaleType, precinct, pricePolicy, province, registerInfo, serial,
        shopId, shopOwnerId, staffCode, staffId, staffOwnType, staffOwnerId, subOwnerId, subOwnerType, tel, tin, ttnsCode, type, userId, warningContent, mapAreaChainChannel, createUser, installationStatus}

    @Schema(description = "Ten trang thai", example = "Dang hoat dong")
    private String statusName;
    @Schema(description = "So bankplus", example = "0909123456")
    private String bankplusMobile;
    @Schema(description = "Ngay sinh", example = "1990-01-01")
    private Date birthday;
    @Schema(description = "Ma BTS", example = "BTS_HN_001")
    private String btsCode;
    @Schema(description = "Giay phep kinh doanh", example = "GPKD_123456")
    private String businessLicence;
    @Schema(description = "Phuong thuc kinh doanh", example = "1")
    private Long businessMethod;
    @Schema(description = "ID loai kenh", example = "1")
    private Long channelTypeId;
    @Schema(description = "Ngay bat dau hop dong", example = "2024-01-01")
    private Date contractFromDate;
    @Schema(description = "Phuong thuc hop dong", example = "1")
    private Long contractMethod;
    @Schema(description = "So hop dong", example = "HD_2024_001")
    private String contractNo;
    @Schema(description = "So tai khoan nhan dien", example = "1234567890")
    private String identifyAccountNo;
    @Schema(description = "Ten tai khoan nhan dien", example = "Nguyen Van A")
    private String identifyAccountName;
    @Schema(description = "Loai phe duyet", example = "APPROVE")
    private String approvalType;
    @Schema(description = "Ngay ket thuc hop dong", example = "2025-01-01")
    private Date contractToDate;
    @Schema(description = "Gia tri dat coc", example = "5000000")
    private Long depositValue;
    @Schema(description = "Chinh sach giam gia", example = "DISC_001")
    private String discountPolicy;
    @Schema(description = "Quan/huyen", example = "Ba Dinh")
    private String district;
    @Schema(description = "Email", example = "nguyenvana@viettel.vn")
    private String email;
    @Schema(description = "Dia chi", example = "123 Nguyen Trai, Quan 1, TP HCM")
    private String address;
    @Schema(description = "Ten file dinh kem", example = "hop_dong.pdf")
    private String fileName;
    @Schema(description = "Gioi tinh", example = "1")
    private Long gender;
    @Schema(description = "Co thiet bi", example = "1")
    private Long hasEquipment;
    @Schema(description = "Co ma so thue", example = "1")
    private Long hasTin;
    @Schema(description = "Ngay cap CMND/CCCD", example = "2020-01-01")
    private Date idIssueDate;
    @Schema(description = "Noi cap CMND/CCCD", example = "Ha Noi")
    private String idIssuePlace;
    @Schema(description = "So CMND/CCCD", example = "001234567890")
    private String idNo;
    @Schema(description = "La nhan vien moi", example = "Y")
    private String isNew;
    @Schema(description = "Loai CMND/CCCD", example = "1")
    private Long idType;
    @Schema(description = "IMSI", example = "452011234567890")
    private String imsi;
    @Schema(description = "So dien thoai ISDN", example = "0909123456")
    private String isdn;
    @Schema(description = "Thoi gian khoa cuoi", example = "2024-06-01")
    private Date lastLockTime;
    @Schema(description = "Lan cap nhat cuoi", example = "2024-06-01")
    private Date lastModified;
    @Schema(description = "Flag khoa", example = "1")
    private String lockFlag;
    @Schema(description = "Trang thai khoa", example = "0")
    private Long lockStatus;
    @Schema(name = "name", description = "Ten nhan vien/diem ban", example = "Nguyen Van A")
    private String name;
    @Schema(description = "Ghi chu", example = "Ghi chu nhan vien")
    private String note;
    @Schema(description = "Gioi han thanh toan", example = "50000000")
    private Long paymentLimit;
    @Schema(description = "Su dung thanh toan", example = "10000000")
    private Long paymentUsage;
    @Schema(description = "Ma PIN", example = "1234")
    private String pin;
    @Schema(description = "Diem ban hang", example = "POS_001")
    private String pointOfSale;
    @Schema(description = "Loai diem ban", example = "1")
    private Long pointOfSaleType;
    @Schema(description = "Chinh sach gia", example = "PRICE_001")
    private String pricePolicy;
    @Schema(description = "Thong tin dang ky", example = "Dang ky tai khoan")
    private String registerInfo;
    @Schema(description = "Serial", example = "SN123456789")
    private String serial;
    @Schema(description = "ID cua hang/dai ly", example = "12345")
    private Long shopId;
    @Schema(description = "ID chu cua hang", example = "12345")
    private Long shopOwnerId;
    @Schema(description = "Ma nhan vien/diem ban", example = "NV_001")
    private String staffCode;
    @Schema(description = "ID nhan vien/diem ban", example = "12345")
    private Long staffId;
    @Schema(description = "Loai chu so huu", example = "OWNER")
    private String staffOwnType;
    @Schema(description = "ID nhan vien quan ly", example = "12345")
    private Long staffOwnerId;
    @Schema(description = "Ma nhan vien quan ly", example = "NV_002")
    private String staffOwnerCode;
    @Schema(description = "Ten nhan vien quan ly", example = "Tran Van B")
    private String staffOwnerName;
    @Schema(description = "Dien thoai nhan vien quan ly", example = "0909123457")
    private String staffOwnerTel;
    @Schema(description = "Trang thai", example = "1")
    private String status;
    @Schema(description = "So kho nhap", example = "100")
    private Long stockNum;
    @Schema(description = "So kho xuat", example = "50")
    private Long stockNumImp;
    @Schema(description = "Duong", example = "Nguyen Trai")
    private String street;
    @Schema(description = "ID chu so huu phu", example = "12345")
    private Long subOwnerId;
    @Schema(description = "Loai chu so huu phu", example = "SUB_OWNER")
    private Long subOwnerType;
    @Schema(description = "So dien thoai lien he", example = "0909123456")
    private String tel;
    @Schema(description = "Ma so thue", example = "0123456789")
    private String tin;
    @Schema(description = "Ma thong tin nhan su", example = "TTNS_001")
    private String ttnsCode;
    @Schema(description = "Loai nhan vien", example = "1")
    private Long type;
    @Schema(description = "ID nguoi dung", example = "12345")
    private Long userId;
    @Schema(description = "Noi dung canh bao", example = "Canh bao tai khoan")
    private String warningContent;
    @Schema(description = "Ma cua hang", example = "VTST_HN_001")
    private String shopCode;
    @Schema(description = "Ten cua hang", example = "Viettel Store Ha Noi")
    private String shopName;
    @Schema(description = "La diem ban hang", example = "true")
    private boolean isPointOfSale;
    @Schema(description = "ID loai kenh cua hang", example = "1")
    private Long shopChanelTypeId;
    @Schema(description = "Tinh/thanh pho cua hang", example = "Ha Noi")
    private String shopProvince;
    @Schema(description = "Quan/huyen cua hang", example = "Ba Dinh")
    private String shopDistrict;
    @Schema(description = "Phuong/xa cua hang", example = "Phuong 1")
    private String shopPrecinct;
    @Schema(description = "Duong dan cua hang", example = "/HN/VTST_HN_001")
    private String shopPath;
    @Schema(description = "ID nhan vien giao dich", example = "12345")
    private Long saleTransStaffId;
    @Schema(description = "Dia chi IP", example = "192.168.1.1")
    private String ipAddress;
    @Schema(description = "Khoa chinh bang", example = "staff_id")
    private String tablePk;
    @Schema(description = "Ten loai kenh", example = "Dai ly")
    private String channelTypeName;
    @Schema(description = "Ten nhan vien quan ly", example = "Tran Van B")
    private String managerName;
    @Schema(description = "Loai cua hang", example = "1")
    private String shopType;
    @Schema(description = "Hanh dong", example = "CREATE")
    private String action;
    @Schema(description = "So SDN", example = "SDN_001")
    private String sdnNumber;
    @Schema(description = "Cua hang la dai ly", example = "true")
    private Boolean shopIsAgent;
    @Schema(description = "ID nhom loai kenh", example = "5")
    private Long groupChannelTypeId;
    @Schema(description = "Ma chuc danh", example = "POS_001")
    private String positionCode;
    @Schema(description = "Ten chuc danh", example = "Nhan vien ban hang")
    private String positionName;
    @Schema(description = "Ten to chuc", example = "Viettel Ha Noi")
    private String organizationName;
    @Schema(description = "Ma ngan hang bankplus", example = "VCB")
    private String bankplusBankCode;
    @Schema(description = "Ma ngan hang cha", example = "VCB")
    private String parentBankCode;
    @Schema(description = "Ma CITAD", example = "CITAD_001")
    private String citadCode;
    @Schema(description = "So tai khoan", example = "1234567890")
    private String accountNumber;
    @Schema(description = "Chu tai khoan", example = "Nguyen Van A")
    private String accountOwner;
    @Schema(description = "ID map shop bank CITAD", example = "12345")
    private Long mapShopBankCitadId;
    @Schema(description = "ID tenant", example = "1")
    private Long tenantId;
    @Schema(description = "Ten chinh sach gia", example = "Chinh sach gia 2024")
    private String pricePolicyName;
    @Schema(description = "Ten chinh sach giam gia", example = "Chinh sach giam gia 2024")
    private String discountPolicyName;
    @Schema(description = "Ten tinh/thanh pho", example = "Ha Noi")
    private String provinceName;
    @Schema(description = "Map vung chan", example = "HN_BD")
    private String mapAreaChainChannel;
    @Schema(description = "ID loai kenh phu", example = "2")
    private Long subChannelTypeId;
    @Schema(description = "Yeu cau VSA", example = "false")
    private boolean vsaRequest = false;
    @Schema(description = "Da dang ky viettel wallet", example = "true")
    private boolean isWalletRegister;
    @Schema(description = "Ten quan/huyen", example = "Ba Dinh")
    private String districtName;
    @Schema(description = "Ten phuong/xa", example = "Phuong 1")
    private String precinctName;
    @Schema(description = "Du lieu nhi phan", example = "...")
    private byte[] data;
    @Schema(description = "Kiem tra map nhom vi tri", example = "false")
    private boolean checkMapGroupPos;
    @Schema(description = "Danh sach ID kenh", example = "[1, 2, 3]")
    private List<Long> channelList;
    @Schema(description = "Ma nhom vi tri", example = "PG_001")
    private String positionGroup;
    @Schema(description = "Ten nhom vi tri", example = "Nhom vi tri ban hang")
    private String positionGroupName;
    @Schema(description = "Kiem tra", example = "Y")
    private String check;
    @Schema(description = "Nguoi tao", example = "admin")
    private String createUser;
    @Schema(description = "Nguoi cap nhat", example = "admin")
    private String updateUser;
    @Schema(description = "Ngay tao", example = "2024-01-01")
    private Date createDatetime;
    @Schema(description = "Ngay cap nhat", example = "2024-06-01")
    private Date updateDatetime;
    @Schema(description = "ID to chuc", example = "12345")
    private String organizationId;
    @Schema(description = "ID cua hang cha", example = "12345")
    private Long parentShopId;
    @Schema(description = "Ten thuong hieu", example = "Viettel")
    private String brandName;
    @Schema(description = "Cap bac", example = "1")
    private Long lv;
    @Schema(description = "Ma ngan hang cha", example = "VCB")
    private String parenBank;
    @Schema(description = "Dia chi theo CMND", example = "123 Nguyen Trai, Ba Dinh, Ha Noi")
    private String addressIdNo;
    @Schema(description = "Loai hinh kinh doanh", example = "1")
    private String businessType;
    @Schema(description = "Tong so ban ghi", example = "100")
    private String totalRecords;
    @Schema(description = "Ma so thue cong ty", example = "0123456789")
    private String companyTax;
    @Schema(description = "Loai thue", example = "1")
    private Long taxType;
    @Schema(description = "So giay phep kinh doanh", example = "GPKD_001")
    private String companyBusinessNo;
    @Schema(description = "Chuc danh dai dien", example = "Giam doc")
    private String companyRepresentPosition;
    @Schema(description = "So thu tu", example = "1")
    private Long rowNum;
    @Schema(description = "Ma kenh", example = "CH_001")
    private String channelCode;
    @Schema(description = "Ma kho", example = "STOCK_001")
    private String stockCode;
    @Schema(description = "CMND/CCCD", example = "001234567890")
    private String identityCard;
    @Schema(description = "Ngay cap", example = "2020-01-01")
    private Date issueDate;
    @Schema(description = "Noi cap", example = "Ha Noi")
    private String issuePlace;
    @Schema(description = "So dien thoai", example = "0909123456")
    private String phoneNumber;
    @Schema(description = "Danh sach ID vai tro", example = "[\"ROLE_001\", \"ROLE_002\"]")
    private List<String> roleIds;
    @Schema(description = "Ho va ten day du", example = "Nguyen Van A")
    private String fullName;
    @Schema(description = "ID loai kenh cua nhan vien", example = "1")
    private Long channelTypeOfStaff;
    @Schema(description = "Ten loai kenh cua nhan vien", example = "Dai ly")
    private String nameOfChannelStaff;
    @Schema(description = "ID loai kenh cua cua hang", example = "1")
    private Long channelTypeOfShop;
    @Schema(description = "Ten loai kenh cua cua hang", example = "Dai ly")
    private String nameOfChannelShop;
    @Schema(description = "Gioi thieu cong ty", example = "Cong ty Viettel")
    private String companyPresent;
    @Schema(description = "Dia chi hoat dong", example = "123 Nguyen Trai, Ba Dinh, Ha Noi")
    private String activeAddress;
    @Schema(description = "Ngay sinh", example = "1990-01-01")
    private String birthDay;
    @Schema(description = "Ngay cap AI", example = "2020-01-01")
    private String idIssueDateAI;
    @Schema(description = "File CMND/CCCD mat truoc", example = "cmnd_mat_truoc.jpg")
    private String fileIdentifyId;
    @Schema(description = "File chan dung", example = "chan_dung.jpg")
    private String filePortraitId;
    @Schema(description = "File CMND/CCCD mat sau", example = "cmnd_mat_sau.jpg")
    private String fileIdentifyIdUp;
    @Schema(description = "File CMND/CCCD un", example = "cmnd_un.jpg")
    private String fileIdentifyIdUn;
    @Schema(description = "Mo ta", example = "Mo ta nhan vien")
    private String description;
    @Schema(description = "Ngay hieu luc", example = "2024-01-01")
    private Date effectDatetime;
    @Schema(description = "Ngay het han", example = "2025-01-01")
    private Date expireDatetime;
    @Schema(description = "Ten dang nhap", example = "nguyenvana")
    private String username;
    @Schema(description = "Trang thai cai dat", example = "INSTALLED")
    private String installationStatus;
    @Schema(description = "Can kiem tra tat ca vai tro", example = "false")
    private boolean needCheckAllRole;
    @Schema(description = "La chu so huu", example = "true")
    private boolean owner;
    @Schema(description = "Tin nhan tu SAP", example = "SAP_001")
    private String sapMessage;
    @Schema(description = "Gui tin nhan", example = "false")
    private boolean sendMessage;
    @Schema(description = "Danh sach ma vung", example = "[\"HN\", \"HCM\"]")
    private List<String> lstAreaCode;
    @Schema(description = "Ten tinh/thanh pho cua hang", example = "Ha Noi")
    private String shopProvinceName;
    @Schema(description = "Loai thong tin nhan vien", example = "FULL_TIME")
    private String staffInfoType;
    @Schema(description = "My Viettel Order", example = "true")
    private Boolean myViettelOrder;
    @Schema(description = "Ghi chu cap nhat", example = "Cap nhat thong tin nhan vien")
    private String updateNote;
    @Schema(description = "Khoa", example = "false")
    private boolean disable = false;
    @Schema(description = "Danh sach nhan vien", example = "[]")
    private List<StaffDTO> staffApp;
    @Schema(description = "Ngay kinh doanh cong ty", example = "2020-01-01")
    private Date companyBusinessDate;
    @Schema(description = "Noi kinh doanh cong ty", example = "Ha Noi")
    private String companyBusinessPlace;
    @Schema(description = "Ten chuong trinh", example = "CT_001")
    private String programName;
    @Schema(description = "URL hien tai", example = "/staff/detail")
    private String currentURL;
    @Schema(description = "Tinh/thanh pho", example = "Ha Noi")
    private String province;
    @Schema(description = "Ma vung", example = "HN")
    private String areaCode;
    @Schema(description = "Khoi duong", example = "Phuong 1")
    private String streetBlock;
    @Schema(description = "So nha", example = "123")
    private String home;
    @Schema(description = "Phuong/xa", example = "Phuong 1")
    private String precinct;
    @Schema(description = "Thong tin cua hang", example = "...")
    private com.viettel.bccs.organization.shop.dto.ShopDTO shop;

    // --- Getters ---

    public String getStatusName() { return statusName; }
    public String getBankplusMobile() { return bankplusMobile; }
    public Date getBirthday() { return birthday; }
    public String getBtsCode() { return btsCode; }
    public String getBusinessLicence() { return businessLicence; }
    public Long getBusinessMethod() { return businessMethod; }
    public Long getChannelTypeId() { return channelTypeId; }
    public Date getContractFromDate() { return contractFromDate; }
    public Long getContractMethod() { return contractMethod; }
    public String getContractNo() { return contractNo; }
    public String getIdentifyAccountNo() { return identifyAccountNo; }
    public String getIdentifyAccountName() { return identifyAccountName; }
    public String getApprovalType() { return approvalType; }
    public Date getContractToDate() { return contractToDate; }
    public Long getDepositValue() { return depositValue; }
    public String getDiscountPolicy() { return discountPolicy; }
    public String getDistrict() { return district; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getFileName() { return fileName; }
    public Long getGender() { return gender; }
    public Long getHasEquipment() { return hasEquipment; }
    public Long getHasTin() { return hasTin; }
    public Date getIdIssueDate() { return idIssueDate; }
    public String getIdIssuePlace() { return idIssuePlace; }
    public String getIdNo() { return idNo; }
    public String getIsNew() { return isNew; }
    public Long getIdType() { return idType; }
    public String getImsi() { return imsi; }
    public String getIsdn() { return isdn; }
    public Date getLastLockTime() { return lastLockTime; }
    public Date getLastModified() { return lastModified; }
    public String getLockFlag() { return lockFlag; }
    public Long getLockStatus() { return lockStatus; }
    public String getName() { return name; }
    public String getNote() { return note; }
    public Long getPaymentLimit() { return paymentLimit; }
    public Long getPaymentUsage() { return paymentUsage; }
    public String getPin() { return pin; }
    public String getPointOfSale() { return pointOfSale; }
    public Long getPointOfSaleType() { return pointOfSaleType; }
    public String getPricePolicy() { return pricePolicy; }
    public String getProvince() { return province; }
    public String getAreaCode() { return areaCode; }
    public String getStreetBlock() { return streetBlock; }
    public String getHome() { return home; }
    public String getPrecinct() { return precinct; }
    public String getRegisterInfo() { return registerInfo; }
    public String getSerial() { return serial; }
    public Long getShopId() { return shopId; }
    public Long getShopOwnerId() { return shopOwnerId; }
    public String getStaffCode() { return staffCode; }
    public Long getStaffId() { return staffId; }
    public String getStaffOwnType() { return staffOwnType; }
    public Long getStaffOwnerId() { return staffOwnerId; }
    public String getStaffOwnerCode() { return staffOwnerCode; }
    public String getStaffOwnerName() { return staffOwnerName; }
    public String getStaffOwnerTel() { return staffOwnerTel; }
    public String getStatus() { return status; }
    public Long getStockNum() { return stockNum; }
    public Long getStockNumImp() { return stockNumImp; }
    public String getStreet() { return street; }
    public Long getSubOwnerId() { return subOwnerId; }
    public Long getSubOwnerType() { return subOwnerType; }
    public String getTel() { return tel; }
    public String getTin() { return tin; }
    public String getTtnsCode() { return ttnsCode; }
    public Long getType() { return type; }
    public Long getUserId() { return userId; }
    public String getWarningContent() { return warningContent; }
    public String getShopCode() { return shopCode; }
    public String getShopName() { return shopName; }
    public boolean getIsPointOfSale() { return isPointOfSale; }
    public Long getShopChanelTypeId() { return shopChanelTypeId; }
    public String getTablePk() { return tablePk; }
    public String getIpAddress() { return ipAddress; }
    public Long getSaleTransStaffId() { return saleTransStaffId; }
    public String getShopPath() { return shopPath; }
    public String getShopProvince() { return shopProvince; }
    public String getShopPrecinct() { return shopPrecinct; }
    public String getChannelTypeName() { return channelTypeName; }
    public String getManagerName() { return managerName; }
    public String getShopType() { return shopType; }
    public String getAction() { return action; }
    public String getProgramName() { return programName; }
    public String getCurrentURL() { return currentURL; }
    public String getSdnNumber() { return sdnNumber; }
    public Boolean getShopIsAgent() { return shopIsAgent; }
    public Long getGroupChannelTypeId() { return groupChannelTypeId; }
    public String getPositionName() { return positionName; }
    public String getBankplusBankCode() { return bankplusBankCode; }
    public String getParentBankCode() { return parentBankCode; }
    public String getCitadCode() { return citadCode; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountOwner() { return accountOwner; }
    public Long getMapShopBankCitadId() { return mapShopBankCitadId; }
    public Long getTenantId() { return tenantId; }
    public String getPricePolicyName() { return pricePolicyName; }
    public String getDiscountPolicyName() { return discountPolicyName; }
    public String getProvinceName() { return provinceName; }
    public String getMapAreaChainChannel() { return mapAreaChainChannel; }
    public Long getSubChannelTypeId() { return subChannelTypeId; }
    public boolean isVsaRequest() { return vsaRequest; }
    public boolean getIsWalletRegister() { return isWalletRegister; }
    public String getDistrictName() { return districtName; }
    public String getPrecinctName() { return precinctName; }
    public byte[] getData() { return data; }
    public boolean isCheckMapGroupPos() { return checkMapGroupPos; }
    public List<Long> getChannelList() { return channelList; }
    public String getPositionGroup() { return positionGroup; }
    public String getPositionGroupName() { return positionGroupName; }
    public String getCheck() { return check; }
    public String getCreateUser() { return createUser; }
    public String getUpdateUser() { return updateUser; }
    public Date getCreateDatetime() { return createDatetime; }
    public Date getUpdateDatetime() { return updateDatetime; }
    public String getOrganizationId() { return organizationId; }
    public Long getParentShopId() { return parentShopId; }
    public String getBrandName() { return brandName; }
    public Long getLv() { return lv; }
    public String getParenBank() { return parenBank; }
    public String getAddressIdNo() { return addressIdNo; }
    public String getBusinessType() { return businessType; }
    public String getTotalRecords() { return totalRecords; }
    public String getCompanyTax() { return companyTax; }
    public Long getTaxType() { return taxType; }
    public String getCompanyBusinessNo() { return companyBusinessNo; }
    public String getCompanyRepresentPosition() { return companyRepresentPosition; }
    public Long getRowNum() { return rowNum; }
    public String getChannelCode() { return channelCode; }
    public String getStockCode() { return stockCode; }
    public String getIdentityCard() { return identityCard; }
    public Date getIssueDate() { return issueDate; }
    public String getIssuePlace() { return issuePlace; }
    public String getPhoneNumber() { return phoneNumber; }
    public List<String> getRoleIds() { return roleIds; }
    public String getFullName() { return fullName; }
    public Long getChannelTypeOfStaff() { return channelTypeOfStaff; }
    public String getNameOfChannelStaff() { return nameOfChannelStaff; }
    public Long getChannelTypeOfShop() { return channelTypeOfShop; }
    public String getNameOfChannelShop() { return nameOfChannelShop; }
    public String getCompanyPresent() { return companyPresent; }
    public String getActiveAddress() { return activeAddress; }
    public String getBirthDay() { return birthDay; }
    public String getIdIssueDateAI() { return idIssueDateAI; }
    public String getFileIdentifyId() { return fileIdentifyId; }
    public String getFilePortraitId() { return filePortraitId; }
    public String getFileIdentifyIdUp() { return fileIdentifyIdUp; }
    public String getFileIdentifyIdUn() { return fileIdentifyIdUn; }
    public String getDescription() { return description; }
    public Date getEffectDatetime() { return effectDatetime; }
    public Date getExpireDatetime() { return expireDatetime; }
    public String getUsername() { return username; }
    public String getInstallationStatus() { return installationStatus; }
    public boolean getNeedCheckAllRole() { return needCheckAllRole; }
    public boolean isOwner() { return owner; }
    public String getSapMessage() { return sapMessage; }
    public boolean isSendMessage() { return sendMessage; }
    public List<String> getLstAreaCode() { return lstAreaCode; }
    public String getShopProvinceName() { return shopProvinceName; }
    public String getStaffInfoType() { return staffInfoType; }
    public Boolean getMyViettelOrder() { return myViettelOrder; }
    public String getUpdateNote() { return updateNote; }
    public boolean isDisable() { return disable; }
    public List<StaffDTO> getStaffApp() { return staffApp; }
    public Date getCompanyBusinessDate() { return companyBusinessDate; }
    public String getCompanyBusinessPlace() { return companyBusinessPlace; }
    public String getOrganizationName() { return organizationName; }
    public String getPositionCode() { return positionCode; }

    // --- Setters ---

    public void setStatusName(String statusName) { this.statusName = statusName; }
    public void setBankplusMobile(String bankplusMobile) { this.bankplusMobile = bankplusMobile; }
    public void setBirthday(Date birthday) { this.birthday = birthday; }
    public void setBtsCode(String btsCode) { this.btsCode = btsCode; }
    public void setBusinessLicence(String businessLicence) { this.businessLicence = businessLicence; }
    public void setBusinessMethod(Long businessMethod) { this.businessMethod = businessMethod; }
    public void setChannelTypeId(Long channelTypeId) { this.channelTypeId = channelTypeId; }
    public void setContractFromDate(Date contractFromDate) { this.contractFromDate = contractFromDate; }
    public void setContractMethod(Long contractMethod) { this.contractMethod = contractMethod; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public void setIdentifyAccountNo(String identifyAccountNo) { this.identifyAccountNo = identifyAccountNo; }
    public void setIdentifyAccountName(String identifyAccountName) { this.identifyAccountName = identifyAccountName; }
    public void setApprovalType(String approvalType) { this.approvalType = approvalType; }
    public void setContractToDate(Date contractToDate) { this.contractToDate = contractToDate; }
    public void setDepositValue(Long depositValue) { this.depositValue = depositValue; }
    public void setDiscountPolicy(String discountPolicy) { this.discountPolicy = discountPolicy; }
    public void setDistrict(String district) { this.district = district; }
    public void setEmail(String email) { this.email = email; }
    public void setAddress(String address) { this.address = address; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setGender(Long gender) { this.gender = gender; }
    public void setHasEquipment(Long hasEquipment) { this.hasEquipment = hasEquipment; }
    public void setHasTin(Long hasTin) { this.hasTin = hasTin; }
    public void setIdIssueDate(Date idIssueDate) { this.idIssueDate = idIssueDate; }
    public void setIdIssuePlace(String idIssuePlace) { this.idIssuePlace = idIssuePlace; }
    public void setIdNo(String idNo) { this.idNo = idNo; }
    public void setIsNew(String isNew) { this.isNew = isNew; }
    public void setIdType(Long idType) { this.idType = idType; }
    public void setImsi(String imsi) { this.imsi = imsi; }
    public void setIsdn(String isdn) { this.isdn = isdn; }
    public void setLastLockTime(Date lastLockTime) { this.lastLockTime = lastLockTime; }
    public void setLastModified(Date lastModified) { this.lastModified = lastModified; }
    public void setLockFlag(String lockFlag) { this.lockFlag = lockFlag; }
    public void setLockStatus(Long lockStatus) { this.lockStatus = lockStatus; }
    public void setName(String name) { this.name = name; }
    public void setNote(String note) { this.note = note; }
    public void setPaymentLimit(Long paymentLimit) { this.paymentLimit = paymentLimit; }
    public void setPaymentUsage(Long paymentUsage) { this.paymentUsage = paymentUsage; }
    public void setPin(String pin) { this.pin = pin; }
    public void setPointOfSale(String pointOfSale) { this.pointOfSale = pointOfSale; }
    public void setPointOfSaleType(Long pointOfSaleType) { this.pointOfSaleType = pointOfSaleType; }
    public void setPricePolicy(String pricePolicy) { this.pricePolicy = pricePolicy; }
    public void setProvince(String province) { this.province = province; }
    public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
    public void setStreetBlock(String streetBlock) { this.streetBlock = streetBlock; }
    public void setHome(String home) { this.home = home; }
    public void setPrecinct(String precinct) { this.precinct = precinct; }
    public void setRegisterInfo(String registerInfo) { this.registerInfo = registerInfo; }
    public void setSerial(String serial) { this.serial = serial; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    public void setShopOwnerId(Long shopOwnerId) { this.shopOwnerId = shopOwnerId; }
    public void setStaffCode(String staffCode) { this.staffCode = staffCode; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
    public void setStaffOwnType(String staffOwnType) { this.staffOwnType = staffOwnType; }
    public void setStaffOwnerId(Long staffOwnerId) { this.staffOwnerId = staffOwnerId; }
    public void setStaffOwnerCode(String staffOwnerCode) { this.staffOwnerCode = staffOwnerCode; }
    public void setStaffOwnerName(String staffOwnerName) { this.staffOwnerName = staffOwnerName; }
    public void setStaffOwnerTel(String staffOwnerTel) { this.staffOwnerTel = staffOwnerTel; }
    public void setStatus(String status) { this.status = status; }
    public void setStockNum(Long stockNum) { this.stockNum = stockNum; }
    public void setStockNumImp(Long stockNumImp) { this.stockNumImp = stockNumImp; }
    public void setStreet(String street) { this.street = street; }
    public void setSubOwnerId(Long subOwnerId) { this.subOwnerId = subOwnerId; }
    public void setSubOwnerType(Long subOwnerType) { this.subOwnerType = subOwnerType; }
    public void setTel(String tel) { this.tel = tel; }
    public void setTin(String tin) { this.tin = tin; }
    public void setTtnsCode(String ttnsCode) { this.ttnsCode = ttnsCode; }
    public void setType(Long type) { this.type = type; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setWarningContent(String warningContent) { this.warningContent = warningContent; }
    public void setShopCode(String shopCode) { this.shopCode = shopCode; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public void setIsPointOfSale(boolean isPointOfSale) { this.isPointOfSale = isPointOfSale; }
    public void setShopChanelTypeId(Long shopChanelTypeId) { this.shopChanelTypeId = shopChanelTypeId; }
    public void setTablePk(String tablePk) { this.tablePk = tablePk; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setSaleTransStaffId(Long saleTransStaffId) { this.saleTransStaffId = saleTransStaffId; }
    public void setShopPath(String shopPath) { this.shopPath = shopPath; }
    public void setShopProvince(String shopProvince) { this.shopProvince = shopProvince; }
    public void setShopPrecinct(String shopPrecinct) { this.shopPrecinct = shopPrecinct; }
    public void setChannelTypeName(String channelTypeName) { this.channelTypeName = channelTypeName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public void setShopType(String shopType) { this.shopType = shopType; }
    public void setAction(String action) { this.action = action; }
    public void setProgramName(String programName) { this.programName = programName; }
    public void setCurrentURL(String currentURL) { this.currentURL = currentURL; }
    public void setSdnNumber(String sdnNumber) { this.sdnNumber = sdnNumber; }
    public void setShopIsAgent(Boolean shopIsAgent) { this.shopIsAgent = shopIsAgent; }
    public void setGroupChannelTypeId(Long groupChannelTypeId) { this.groupChannelTypeId = groupChannelTypeId; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
    public void setBankplusBankCode(String bankplusBankCode) { this.bankplusBankCode = bankplusBankCode; }
    public void setParentBankCode(String parentBankCode) { this.parentBankCode = parentBankCode; }
    public void setCitadCode(String citadCode) { this.citadCode = citadCode; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setAccountOwner(String accountOwner) { this.accountOwner = accountOwner; }
    public void setMapShopBankCitadId(Long mapShopBankCitadId) { this.mapShopBankCitadId = mapShopBankCitadId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public void setPricePolicyName(String pricePolicyName) { this.pricePolicyName = pricePolicyName; }
    public void setDiscountPolicyName(String discountPolicyName) { this.discountPolicyName = discountPolicyName; }
    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }
    public void setMapAreaChainChannel(String mapAreaChainChannel) { this.mapAreaChainChannel = mapAreaChainChannel; }
    public void setSubChannelTypeId(Long subChannelTypeId) { this.subChannelTypeId = subChannelTypeId; }
    public void setVsaRequest(boolean vsaRequest) { this.vsaRequest = vsaRequest; }
    public void setIsWalletRegister(boolean walletRegister) { isWalletRegister = walletRegister; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }
    public void setPrecinctName(String precinctName) { this.precinctName = precinctName; }
    public void setData(byte[] data) { this.data = data; }
    public void setCheckMapGroupPos(boolean checkMapGroupPos) { this.checkMapGroupPos = checkMapGroupPos; }
    public void setChannelList(List<Long> channelList) { this.channelList = channelList; }
    public void setPositionGroup(String positionGroup) { this.positionGroup = positionGroup; }
    public void setPositionGroupName(String positionGroupName) { this.positionGroupName = positionGroupName; }
    public void setCheck(String check) { this.check = check; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
    public void setCreateDatetime(Date createDatetime) { this.createDatetime = createDatetime; }
    public void setUpdateDatetime(Date updateDatetime) { this.updateDatetime = updateDatetime; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public void setParentShopId(Long parentShopId) { this.parentShopId = parentShopId; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public void setLv(Long lv) { this.lv = lv; }
    public void setParenBank(String parenBank) { this.parenBank = parenBank; }
    public void setAddressIdNo(String addressIdNo) { this.addressIdNo = addressIdNo; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public void setTotalRecords(String totalRecords) { this.totalRecords = totalRecords; }
    public void setCompanyTax(String companyTax) { this.companyTax = companyTax; }
    public void setTaxType(Long taxType) { this.taxType = taxType; }
    public void setCompanyBusinessNo(String companyBusinessNo) { this.companyBusinessNo = companyBusinessNo; }
    public void setCompanyRepresentPosition(String companyRepresentPosition) { this.companyRepresentPosition = companyRepresentPosition; }
    public void setRowNum(Long rowNum) { this.rowNum = rowNum; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }
    public void setIdentityCard(String identityCard) { this.identityCard = identityCard; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }
    public void setIssuePlace(String issuePlace) { this.issuePlace = issuePlace; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setRoleIds(List<String> roleIds) { this.roleIds = roleIds; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setChannelTypeOfStaff(Long channelTypeOfStaff) { this.channelTypeOfStaff = channelTypeOfStaff; }
    public void setNameOfChannelStaff(String nameOfChannelStaff) { this.nameOfChannelStaff = nameOfChannelStaff; }
    public void setChannelTypeOfShop(Long channelTypeOfShop) { this.channelTypeOfShop = channelTypeOfShop; }
    public void setNameOfChannelShop(String nameOfChannelShop) { this.nameOfChannelShop = nameOfChannelShop; }
    public void setCompanyPresent(String companyPresent) { this.companyPresent = companyPresent; }
    public void setActiveAddress(String activeAddress) { this.activeAddress = activeAddress; }
    public void setBirthDay(String birthDay) { this.birthDay = birthDay; }
    public void setIdIssueDateAI(String idIssueDateAI) { this.idIssueDateAI = idIssueDateAI; }
    public void setFileIdentifyId(String fileIdentifyId) { this.fileIdentifyId = fileIdentifyId; }
    public void setFilePortraitId(String filePortraitId) { this.filePortraitId = filePortraitId; }
    public void setFileIdentifyIdUp(String fileIdentifyIdUp) { this.fileIdentifyIdUp = fileIdentifyIdUp; }
    public void setFileIdentifyIdUn(String fileIdentifyIdUn) { this.fileIdentifyIdUn = fileIdentifyIdUn; }
    public void setDescription(String description) { this.description = description; }
    public void setEffectDatetime(Date effectDatetime) { this.effectDatetime = effectDatetime; }
    public void setExpireDatetime(Date expireDatetime) { this.expireDatetime = expireDatetime; }
    public void setUsername(String username) { this.username = username; }
    public void setInstallationStatus(String installationStatus) { this.installationStatus = installationStatus; }
    public void setNeedCheckAllRole(boolean needCheckAllRole) { this.needCheckAllRole = needCheckAllRole; }
    public void setOwner(boolean owner) { this.owner = owner; }
    public void setSapMessage(String sapMessage) { this.sapMessage = sapMessage; }
    public void setSendMessage(boolean sendMessage) { this.sendMessage = sendMessage; }
    public void setLstAreaCode(List<String> lstAreaCode) { this.lstAreaCode = lstAreaCode; }
    public void setShopProvinceName(String shopProvinceName) { this.shopProvinceName = shopProvinceName; }
    public void setStaffInfoType(String staffInfoType) { this.staffInfoType = staffInfoType; }
    public void setMyViettelOrder(Boolean myViettelOrder) { this.myViettelOrder = myViettelOrder; }
    public void setUpdateNote(String updateNote) { this.updateNote = updateNote; }
    public void setDisable(boolean disable) { this.disable = disable; }
    public void setStaffApp(List<StaffDTO> staffApp) { this.staffApp = staffApp; }
    public void setCompanyBusinessDate(Date companyBusinessDate) { this.companyBusinessDate = companyBusinessDate; }
    public void setCompanyBusinessPlace(String companyBusinessPlace) { this.companyBusinessPlace = companyBusinessPlace; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public void setPositionCode(String positionCode) { this.positionCode = positionCode; }
    public com.viettel.bccs.organization.shop.dto.ShopDTO getShop() { return shop; }
    public void setShop(com.viettel.bccs.organization.shop.dto.ShopDTO shop) { this.shop = shop; }

    @Override
    public String toString() {
        return "" + staffId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaffDTO staffDTO = (StaffDTO) o;
        if (staffId != null ? !staffId.equals(staffDTO.staffId) : staffDTO.staffId != null) return false;
        if (staffCode != null ? !staffCode.equals(staffDTO.staffCode) : staffDTO.staffCode != null) return false;
        return name != null ? name.equals(staffDTO.name) : staffDTO.name == null;
    }

    @Override
    public int hashCode() {
        int result = staffId != null ? staffId.hashCode() : 0;
        result = 31 * result + (staffCode != null ? staffCode.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}