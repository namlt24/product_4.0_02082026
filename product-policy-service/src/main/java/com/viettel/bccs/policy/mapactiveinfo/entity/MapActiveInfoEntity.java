package com.viettel.bccs.policy.mapactiveinfo.entity;

import jakarta.persistence.*;

import java.sql.Blob;
import java.util.Date;

@Entity
@Table(name = "MAP_ACTIVE_INFO")
public class MapActiveInfoEntity {

    @Id
    @Column(name = "ID", precision = 10)
    private Long id;

    @Column(name = "TEL_SERVICE_ID", precision = 10, nullable = false)
    private Long telServiceId;

    @Column(name = "PRODUCT_CODE", length = 50)
    private String productCode;

    @Column(name = "PRODUCT_NAME", length = 100)
    private String productName;

    @Column(name = "REG_REASON_ID", precision = 10, nullable = false)
    private Long regReasonId;

    @Column(name = "REASON_NAME", length = 100)
    private String reasonName;

    @Column(name = "PROM_CODE", length = 50)
    private String promCode;

    @Column(name = "PROM_NAME", length = 150)
    private String promName;

    @Column(name = "CHANNEL_TYPE_ID", precision = 10, nullable = false)
    private Long channelTypeId;

    @Column(name = "CHANNEL_NAME", length = 50)
    private String channelName;

    @Column(name = "PROVINCE_CODE", length = 50, nullable = false)
    private String provinceCode;

    @Column(name = "DISTRICT_CODE", length = 50)
    private String districtCode;

    @Temporal(TemporalType.DATE)
    @Column(name = "EFFECT_DATE")
    private Date effectDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "END_DATE")
    private Date endDate;

    @Column(name = "PROVINCE_NAME", length = 50)
    private String provinceName;

    @Column(name = "DISTRICT_NAME", length = 50)
    private String districtName;

    @Column(name = "OFFER_ID", precision = 10)
    private Long offerId;

    @Column(name = "OFFER_NAME", length = 100)
    private String offerName;

    @Column(name = "STATUS", precision = 2)
    private Long status;

    @Column(name = "PRECINCT_NAME", length = 50)
    private String precinctName;

    @Column(name = "PRECINCT_CODE", length = 50)
    private String precinctCode;

    @Column(name = "SHOP_CODE", length = 50)
    private String shopCode;

    @Column(name = "STAFF_CODE", length = 50)
    private String staffCode;

    @Column(name = "ACTION_CODE", length = 10)
    private String actionCode;

    @Column(name = "ACTION_NAME", length = 50)
    private String actionName;

    @Column(name = "LIMIT_NUMBER", length = 10)
    private String limitNumber;

    @Column(name = "CAPTCHAR_REQUIRE", precision = 1)
    private Long captcharRequire;

    @Column(name = "UNIT", length = 2)
    private String unit;

    @Column(name = "CUSTOMER_GROUP", length = 10)
    private String customerGroup;

    @Column(name = "CUSTOMER_TYPE", length = 10)
    private String customerType;

    @Column(name = "SUB_TYPE", length = 4)
    private String subType;

    @Column(name = "SUB_GROUP", length = 10)
    private String subGroup;

    @Column(name = "POLICY_DOC", length = 100)
    private String policyDoc;

    @Column(name = "ACTION_GROUP", length = 50)
    private String actionGroup;

    @Column(name = "ACTION_GROUP_NAME", length = 200)
    private String actionGroupName;

    @Column(name = "FILE_NAME", length = 200)
    private String fileName;

    @Column(name = "STATION_ID", precision = 10)
    private Long stationId;

    @Column(name = "SHOP_ID", precision = 10)
    private Long shopId;

    @Column(name = "FILE_ATTACH")
    private Blob fileAttach;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "ISSUE_DATETIME")
    private Date issueDatetime;

    @Column(name = "STATION_CODES", length = 1000)
    private String stationCodes;

    @Column(name = "PAY_TYPE", length = 1)
    private String payType;

    @Column(name = "TECHNOLOGY", length = 10)
    private String technology;

    @Temporal(TemporalType.DATE)
    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Column(name = "AREA_GROUP_CODE", length = 50)
    private String areaGroupCode;

    @Column(name = "VAS_CODE", length = 50)
    private String vasCode;

    @Column(name = "VAS_NAME", length = 100)
    private String vasName;

    @Column(name = "NODE_CODE", length = 1500)
    private String nodeCode;

    @Column(name = "NOTE", length = 3000)
    private String note;

    @Column(name = "GROUP_NODE_CODE", length = 50)
    private String groupNodeCode;

    @Column(name = "STAFF_TYPE", length = 5)
    private String staffType;

    @Column(name = "BUSINESS_NO", length = 4000)
    private String businessNo;

    @Column(name = "SUB_GROUP_CODE", length = 4000)
    private String subGroupCode;

    @Column(name = "SME_CUSTOMER_GROUP", length = 4000)
    private String smeCustomerGroup;

    @Column(name = "IMPORT_OFFLINE_ID", precision = 10)
    private Long importOfflineId;

    // Getters
    public Long getId() { return id; }
    public Long getTelServiceId() { return telServiceId; }
    public String getProductCode() { return productCode; }
    public String getProductName() { return productName; }
    public Long getRegReasonId() { return regReasonId; }
    public String getReasonName() { return reasonName; }
    public String getPromCode() { return promCode; }
    public String getPromName() { return promName; }
    public Long getChannelTypeId() { return channelTypeId; }
    public String getChannelName() { return channelName; }
    public String getProvinceCode() { return provinceCode; }
    public String getDistrictCode() { return districtCode; }
    public Date getEffectDate() { return effectDate; }
    public Date getEndDate() { return endDate; }
    public String getProvinceName() { return provinceName; }
    public String getDistrictName() { return districtName; }
    public Long getOfferId() { return offerId; }
    public String getOfferName() { return offerName; }
    public Long getStatus() { return status; }
    public String getPrecinctName() { return precinctName; }
    public String getPrecinctCode() { return precinctCode; }
    public String getShopCode() { return shopCode; }
    public String getStaffCode() { return staffCode; }
    public String getActionCode() { return actionCode; }
    public String getActionName() { return actionName; }
    public String getLimitNumber() { return limitNumber; }
    public Long getCaptcharRequire() { return captcharRequire; }
    public String getUnit() { return unit; }
    public String getCustomerGroup() { return customerGroup; }
    public String getCustomerType() { return customerType; }
    public String getSubType() { return subType; }
    public String getSubGroup() { return subGroup; }
    public String getPolicyDoc() { return policyDoc; }
    public String getActionGroup() { return actionGroup; }
    public String getActionGroupName() { return actionGroupName; }
    public String getFileName() { return fileName; }
    public Long getStationId() { return stationId; }
    public Long getShopId() { return shopId; }
    public Blob getFileAttach() { return fileAttach; }
    public String getCreateUser() { return createUser; }
    public Date getIssueDatetime() { return issueDatetime; }
    public String getStationCodes() { return stationCodes; }
    public String getPayType() { return payType; }
    public String getTechnology() { return technology; }
    public Date getUpdateDatetime() { return updateDatetime; }
    public String getUpdateUser() { return updateUser; }
    public String getAreaGroupCode() { return areaGroupCode; }
    public String getVasCode() { return vasCode; }
    public String getVasName() { return vasName; }
    public String getNodeCode() { return nodeCode; }
    public String getNote() { return note; }
    public String getGroupNodeCode() { return groupNodeCode; }
    public String getStaffType() { return staffType; }
    public String getBusinessNo() { return businessNo; }
    public String getSubGroupCode() { return subGroupCode; }
    public String getSmeCustomerGroup() { return smeCustomerGroup; }
    public Long getImportOfflineId() { return importOfflineId; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTelServiceId(Long telServiceId) { this.telServiceId = telServiceId; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setRegReasonId(Long regReasonId) { this.regReasonId = regReasonId; }
    public void setReasonName(String reasonName) { this.reasonName = reasonName; }
    public void setPromCode(String promCode) { this.promCode = promCode; }
    public void setPromName(String promName) { this.promName = promName; }
    public void setChannelTypeId(Long channelTypeId) { this.channelTypeId = channelTypeId; }
    public void setChannelName(String channelName) { this.channelName = channelName; }
    public void setProvinceCode(String provinceCode) { this.provinceCode = provinceCode; }
    public void setDistrictCode(String districtCode) { this.districtCode = districtCode; }
    public void setEffectDate(Date effectDate) { this.effectDate = effectDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }
    public void setOfferId(Long offerId) { this.offerId = offerId; }
    public void setOfferName(String offerName) { this.offerName = offerName; }
    public void setStatus(Long status) { this.status = status; }
    public void setPrecinctName(String precinctName) { this.precinctName = precinctName; }
    public void setPrecinctCode(String precinctCode) { this.precinctCode = precinctCode; }
    public void setShopCode(String shopCode) { this.shopCode = shopCode; }
    public void setStaffCode(String staffCode) { this.staffCode = staffCode; }
    public void setActionCode(String actionCode) { this.actionCode = actionCode; }
    public void setActionName(String actionName) { this.actionName = actionName; }
    public void setLimitNumber(String limitNumber) { this.limitNumber = limitNumber; }
    public void setCaptcharRequire(Long captcharRequire) { this.captcharRequire = captcharRequire; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setCustomerGroup(String customerGroup) { this.customerGroup = customerGroup; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }
    public void setSubType(String subType) { this.subType = subType; }
    public void setSubGroup(String subGroup) { this.subGroup = subGroup; }
    public void setPolicyDoc(String policyDoc) { this.policyDoc = policyDoc; }
    public void setActionGroup(String actionGroup) { this.actionGroup = actionGroup; }
    public void setActionGroupName(String actionGroupName) { this.actionGroupName = actionGroupName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setStationId(Long stationId) { this.stationId = stationId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    public void setFileAttach(Blob fileAttach) { this.fileAttach = fileAttach; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public void setIssueDatetime(Date issueDatetime) { this.issueDatetime = issueDatetime; }
    public void setStationCodes(String stationCodes) { this.stationCodes = stationCodes; }
    public void setPayType(String payType) { this.payType = payType; }
    public void setTechnology(String technology) { this.technology = technology; }
    public void setUpdateDatetime(Date updateDatetime) { this.updateDatetime = updateDatetime; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
    public void setAreaGroupCode(String areaGroupCode) { this.areaGroupCode = areaGroupCode; }
    public void setVasCode(String vasCode) { this.vasCode = vasCode; }
    public void setVasName(String vasName) { this.vasName = vasName; }
    public void setNodeCode(String nodeCode) { this.nodeCode = nodeCode; }
    public void setNote(String note) { this.note = note; }
    public void setGroupNodeCode(String groupNodeCode) { this.groupNodeCode = groupNodeCode; }
    public void setStaffType(String staffType) { this.staffType = staffType; }
    public void setBusinessNo(String businessNo) { this.businessNo = businessNo; }
    public void setSubGroupCode(String subGroupCode) { this.subGroupCode = subGroupCode; }
    public void setSmeCustomerGroup(String smeCustomerGroup) { this.smeCustomerGroup = smeCustomerGroup; }
    public void setImportOfflineId(Long importOfflineId) { this.importOfflineId = importOfflineId; }
}