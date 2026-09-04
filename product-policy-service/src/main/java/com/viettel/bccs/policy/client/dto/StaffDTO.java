package com.viettel.bccs.policy.client.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class StaffDTO implements Serializable {

    public StaffDTO() {
    }

    public StaffDTO(String staffCode) {
        this.staffCode = staffCode;
    }

    public static enum Filter {staffCode, name, staffId, status
    }

    public static enum Columns {address, areaCode, bankplusMobile, birthday, btsCode, businessLicence,
        businessMethod, channelTypeId, contractFromDate, contractMethod, contractNo, contractToDate,
        depositValue, discountPolicy, district, email, fileName, hasEquipment, hasTin, home, idIssueDate,
        idIssuePlace, idNo, idType, imsi, isdn, lastLockTime, lastModified, lockFlag, lockStatus, name, note,
        paymentLimit, paymentUsage, pin, pointOfSale, pointOfSaleType, precinct, pricePolicy, province,
                registerInfo, serial,
        shopId, shopOwnerId, staffCode, staffId, staffOwnType, staffOwnerId, status, stockNum, stockNumImp,
                street, streetBlock,
        subOwnerId, subOwnerType, tel, tin, ttnsCode, type, userId, warningContent, mapAreaChainChannel, createUser,
        installationStatus
    }

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
    // Lombok sinh getter isPointOfSale()/isWalletRegister() cho field boolean da co tien to "is",
    // Jackson se tu tach tien to "is" khi doc ten thuoc tinh JSON (ra "pointOfSale" thay vi
    // "isPointOfSale") - pin lai dung ten field goc bang @JsonProperty de khong doi hop dong JSON.
    @JsonProperty("isPointOfSale")
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
    @JsonProperty("isWalletRegister")
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
    // Ten field giu checkstyle-compliant (currentUrl, toi da 2 chu hoa lien tiep), nhung @JsonProperty
    // pin lai dung ten JSON goc "currentURL" de khong doi hop dong wire voi client ngoai.
    @JsonProperty("currentURL")
    private String currentUrl;
    private String province;
    private String areaCode;
    private String streetBlock;
    private String home;
    private String precinct;
    private String address;
    private Long gender;

    // --- Getters ---



    // --- Setters ---


    @Override
    public String toString() {
        return "" + staffId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StaffDTO staffDTO = (StaffDTO) o;
        if (staffId != null ? !staffId.equals(staffDTO.staffId) : staffDTO.staffId != null) {
            return false;
        }
        if (staffCode != null ? !staffCode.equals(staffDTO.staffCode) : staffDTO.staffCode != null) {
            return false;
        }
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