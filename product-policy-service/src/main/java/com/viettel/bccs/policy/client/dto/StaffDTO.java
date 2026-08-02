package com.viettel.bccs.policy.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Schema
@JsonIgnoreProperties(ignoreUnknown = true)
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
        shopId, shopOwnerId, staffCode, staffId, staffOwnType, staffOwnerId, status, stockNum, stockNumImp, street, streetBlock,
        subOwnerId, subOwnerType, tel, tin, ttnsCode, type, userId, warningContent, mapAreaChainChannel, createUser, installationStatus}

    private String statusName;
    @Schema(description = "So bankplus")
    private String bankplusMobile;
    private Date birthday;
    private String btsCode;
    private String businessLicence;
    private Long businessMethod;
    @Schema(description = "ID loai kenh")
    private Long channelTypeId;
    private Date contractFromDate;
    private Long contractMethod;
    private String contractNo;
    private String identifyAccountNo;
    private String identifyAccountName;
    private String approvalType;
    private Date contractToDate;
    private Long depositValue;
    private String discountPolicy;
    private String district;
    @Schema(description = "email")
    private String email;
    private String fileName;
    private Long hasEquipment;
    private Long hasTin;
    private Date idIssueDate;
    private String idIssuePlace;
    private String idNo;
    private String isNew;
    private Long idType;
    private String imsi;
    private String isdn;
    private Date lastLockTime;
    private Date lastModified;
    private String lockFlag;
    private Long lockStatus;
    @Schema(name = "name", description = "Ten nhan vien/diem ban")
    private String name;
    private String note;
    private Long paymentLimit;
    private Long paymentUsage;
    private String pin;
    private String pointOfSale;
    private Long pointOfSaleType;
    private String pricePolicy;
    private String registerInfo;
    private String serial;
    @Schema(description = "ID cua hang/dai ly")
    private Long shopId;
    private Long shopOwnerId;
    @Schema(description = "Ma nhan vien/diem ban")
    private String staffCode;
    @Schema(description = "ID nhan vien/diem ban")
    private Long staffId;
    private String staffOwnType;
    @Schema(description = "ID nhan vien quan ly")
    private Long staffOwnerId;
    private String staffOwnerCode;
    private String staffOwnerName;
    private String staffOwnerTel;
    @Schema(description = "Trang thai")
    private Long status;
    private Long stockNum;
    private Long stockNumImp;
    private String street;
    private Long subOwnerId;
    private Long subOwnerType;
    @Schema(description = "So dien thoai lien he")
    private String tel;
    private String tin;
    @Schema(description = "Ma thong tin nhan su")
    private String ttnsCode;
    private Long type;
    private Long userId;
    private String warningContent;
    private String shopCode;
    private String shopName;
    private boolean isPointOfSale;
    private Long shopChanelTypeId;
    private String shopProvince;
    private String shopDistrict;
    private String shopPrecinct;
    private String shopPath;
    private Long saleTransStaffId;
    private String ipAddress;
    private String tablePk;
    private String channelTypeName;
    private String managerName;
    private String shopType;
    private String action;
    private String sdnNumber;
    private Boolean shopIsAgent;
    private Long groupChannelTypeId;
    private String positionCode;
    private String positionName;
    private String organizationName;
    private String bankplusBankCode;
    private String parentBankCode;
    private String citadCode;
    private String accountNumber;
    private String accountOwner;
    private Long mapShopBankCitadId;
    private Long tenantId;
    private String pricePolicyName;
    private String discountPolicyName;
    private String provinceName;
    private String mapAreaChainChannel;
    private Long subChannelTypeId;
    private boolean vsaRequest = false;
    private boolean isWalletRegister;
    private String districtName;
    private String precinctName;
    private byte[] data;
    private boolean checkMapGroupPos;
    private List<Long> channelList;
    private String positionGroup;
    private String positionGroupName;
    private String check;
    private String createUser;
    private String updateUser;
    private Date createDatetime;
    private Date updateDatetime;
    private String organizationId;
    private Long parentShopId;
    private String brandName;
    private Long lv;
    private String parenBank;
    private String addressIdNo;
    private String businessType;
    private String totalRecords;
    private String companyTax;
    private Long taxType;
    private String companyBusinessNo;
    private String companyRepresentPosition;
    private Long rowNum;
    private String channelCode;
    private String stockCode;
    private String identityCard;
    private Date issueDate;
    private String issuePlace;
    private String phoneNumber;
    private List<String> roleIds;
    private String fullName;
    private Long channelTypeOfStaff;
    private String nameOfChannelStaff;
    private Long channelTypeOfShop;
    private String nameOfChannelShop;
    private String companyPresent;
    private String activeAddress;
    private String birthDay;
    private String idIssueDateAI;
    private String fileIdentifyId;
    private String filePortraitId;
    private String fileIdentifyIdUp;
    private String fileIdentifyIdUn;
    private String description;
    private Date effectDatetime;
    private Date expireDatetime;
    private String username;
    private String installationStatus;
    private boolean needCheckAllRole;
    private boolean owner;
    private String sapMessage;
    private boolean sendMessage;
    private List<String> lstAreaCode;
    private String shopProvinceName;
    private String staffInfoType;
    private Boolean myViettelOrder;
    private String updateNote;
    private boolean disable = false;
    private List<StaffDTO> staffApp;
    private Date companyBusinessDate;
    private String companyBusinessPlace;
    private String programName;
    private String currentURL;
    private String province;
    private String areaCode;
    private String streetBlock;
    private String home;
    private String precinct;
    private String address;
    private Long gender;

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
    public String getFileName() { return fileName; }
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
    public Long getStatus() { return status; }
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
    public String getShopDistrict() { return shopDistrict; }

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
    public String getPrecinct() { return precinct; }
    public String getAddress() { return address; }
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
    public Long getGender() { return gender; }

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
    public void setFileName(String fileName) { this.fileName = fileName; }
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
    public void setStatus(Long status) { this.status = status; }
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
    public void setPrecinct(String precinct) { this.precinct = precinct; }
    public void setAddress(String address) { this.address = address; }
    public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
    public void setShopDistrict(String shopDistrict) { this.shopDistrict = shopDistrict; }
    public void setGender(Long gender) { this.gender = gender; }

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