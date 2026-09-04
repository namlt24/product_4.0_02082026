package com.viettel.bccs.organization.staff.entity;

import java.util.Date;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "STAFF")
@Getter
@Setter
public class StaffEntity {

    @Id
    @Column(name = "STAFF_ID", nullable = false, precision = 10)
    private Long staffId;

    @Column(name = "SHOP_ID", nullable = false, precision = 10)
    private Long shopId;

    @Column(name = "STAFF_CODE", nullable = false, length = 40)
    private String staffCode;

    @Column(name = "NAME", length = 300)
    private String name;

    @Temporal(TemporalType.DATE)
    @Column(name = "BIRTHDAY")
    private Date birthday;

    @Column(name = "ID_NO", length = 20)
    private String idNo;

    @Column(name = "ID_ISSUE_PLACE", length = 150)
    private String idIssuePlace;

    @Temporal(TemporalType.DATE)
    @Column(name = "ID_ISSUE_DATE")
    private Date idIssueDate;

    @Column(name = "TEL", length = 15)
    private String tel;

    @Column(name = "TYPE", precision = 10)
    private Long type;

    @Column(name = "SERIAL", length = 20)
    private String serial;

    @Column(name = "ISDN", length = 15)
    private String isdn;

    @Column(name = "PIN", length = 10)
    private String pin;

    @Column(name = "ADDRESS", length = 500)
    private String address;

    @Column(name = "PROVINCE", length = 50)
    private String province;

    @Column(name = "STAFF_OWN_TYPE", length = 1)
    private String staffOwnType;

    @Column(name = "STAFF_OWNER_ID", precision = 10)
    private Long staffOwnerId;

    @Column(name = "CHANNEL_TYPE_ID", precision = 10)
    private Long channelTypeId;

    @Column(name = "PRICE_POLICY", length = 20)
    private String pricePolicy;

    @Column(name = "DISCOUNT_POLICY", length = 50)
    private String discountPolicy;

    @Column(name = "POINT_OF_SALE", length = 5)
    private String pointOfSale;

    @Column(name = "LOCK_STATUS", precision = 10)
    private Long lockStatus;

    @Temporal(TemporalType.DATE)
    @Column(name = "LAST_LOCK_TIME")
    private Date lastLockTime;

    @Column(name = "IMSI", length = 20)
    private String imsi;

    @Column(name = "PAYMENT_LIMIT", precision = 10)
    private Long paymentLimit;

    @Column(name = "PAYMENT_USAGE", precision = 10)
    private Long paymentUsage;

    @Column(name = "NOTE", length = 500)
    private String note;

    @Column(name = "BUSINESS_METHOD", precision = 2)
    private Integer businessMethod;

    @Column(name = "CONTRACT_METHOD", precision = 2)
    private Integer contractMethod;

    @Column(name = "HAS_EQUIPMENT", precision = 2)
    private Integer hasEquipment;

    @Column(name = "HAS_TIN", precision = 2)
    private Integer hasTin;

    @Column(name = "TIN", length = 50)
    private String tin;

    @Column(name = "DISTRICT", length = 50)
    private String district;

    @Column(name = "PRECINCT", length = 50)
    private String precinct;

    @Column(name = "AREA_CODE", length = 200)
    private String areaCode;

    @Column(name = "STREET_BLOCK", length = 100)
    private String streetBlock;

    @Column(name = "STREET", length = 100)
    private String street;

    @Column(name = "HOME", length = 100)
    private String home;

    @Column(name = "STOCK_NUM_IMP", precision = 7)
    private Integer stockNumImp;

    @Column(name = "STOCK_NUM", precision = 7)
    private Integer stockNum;

    @Column(name = "POINT_OF_SALE_TYPE", precision = 1)
    private Integer pointOfSaleType;

    @Column(name = "TTNS_CODE", length = 200)
    private String ttnsCode;

    @Column(name = "REGISTER_INFO", length = 2)
    private String registerInfo;

    @Column(name = "ID_TYPE", precision = 2)
    private Integer idType;

    @Column(name = "LOCK_FLAG", length = 10)
    private String lockFlag;

    @Column(name = "WARNING_CONTENT", length = 500)
    private String warningContent;

    @Column(name = "CONTRACT_NO", length = 100)
    private String contractNo;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "FILE_ATTACH")
    private byte[] fileAttach;

    @Temporal(TemporalType.DATE)
    @Column(name = "CONTRACT_FROM_DATE")
    private Date contractFromDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "CONTRACT_TO_DATE")
    private Date contractToDate;

    @Column(name = "DEPOSIT_VALUE", precision = 10)
    private Long depositValue;

    @Column(name = "BTS_CODE", length = 10)
    private String btsCode;

    @Column(name = "FILE_NAME", length = 200)
    private String fileName;

    @Column(name = "BANKPLUS_MOBILE", length = 20)
    private String bankplusMobile;

    @Column(name = "EMAIL", length = 100)
    private String email;

    @Column(name = "BUSINESS_LICENCE", length = 200)
    private String businessLicence;

    @Column(name = "SUB_OWNER_TYPE", precision = 1)
    private Integer subOwnerType;

    @Column(name = "SUB_OWNER_ID", precision = 20)
    private Long subOwnerId;

    @Temporal(TemporalType.DATE)
    @Column(name = "LAST_MODIFIED")
    private Date lastModified;

    @Column(name = "SHOP_OWNER_ID", precision = 20)
    private Long shopOwnerId;

    @Column(name = "USER_ID", precision = 20)
    private Long userId;

    @Column(name = "GROUP_CHANNEL_TYPE_ID", precision = 10)
    private Long groupChannelTypeId;

    @Column(name = "BANKPLUS_BANK_CODE", length = 50)
    private String bankplusBankCode;

    @Column(name = "GENDER", precision = 2)
    private Integer gender;

    @Column(name = "TENANT_ID")
    private Long tenantId;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Temporal(TemporalType.DATE)
    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "MAP_AREA_CHAIN_CHANNEL", length = 50)
    private String mapAreaChainChannel;
}
