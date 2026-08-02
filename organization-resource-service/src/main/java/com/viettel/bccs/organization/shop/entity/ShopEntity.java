package com.viettel.bccs.organization.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "SHOP")
@Getter
@Setter
public class ShopEntity {

    @Id
    @Column(name = "SHOP_ID", nullable = false, precision = 10)
    private Long shopId;

    @Column(name = "NAME", nullable = false, length = 300)
    private String name;

    @Column(name = "PARENT_SHOP_ID", precision = 10)
    private Long parentShopId;

    @Column(name = "ACCOUNT", length = 40)
    private String account;

    @Column(name = "BANK_NAME", length = 150)
    private String bankName;

    @Column(name = "ADDRESS", length = 500)
    private String address;

    @Column(name = "TEL", length = 100)
    private String tel;

    @Column(name = "FAX", length = 100)
    private String fax;

    @Column(name = "SHOP_CODE", nullable = false, length = 40)
    private String shopCode;

    @Column(name = "SHOP_TYPE", length = 1)
    private String shopType;

    @Column(name = "CONTACT_NAME", length = 100)
    private String contactName;

    @Column(name = "CONTACT_TITLE", length = 30)
    private String contactTitle;

    @Column(name = "TEL_NUMBER", length = 30)
    private String telNumber;

    @Column(name = "EMAIL", length = 200)
    private String email;

    @Column(name = "DESCRIPTION", length = 100)
    private String description;

    @Column(name = "PROVINCE", length = 50)
    private String province;

    @Column(name = "PAR_SHOP_CODE", length = 20)
    private String parShopCode;

    @Column(name = "CENTER_CODE", length = 1)
    private String centerCode;

    @Column(name = "OLD_SHOP_CODE", length = 40)
    private String oldShopCode;

    @Column(name = "COMPANY", length = 4000)
    private String company;

    @Column(name = "TIN", length = 50)
    private String tin;

    @Column(name = "SHOP", length = 20)
    private String shop;

    @Column(name = "PROVINCE_CODE", length = 50)
    private String provinceCode;

    @Column(name = "PAY_COMM", length = 1)
    private String payComm;

    @Temporal(TemporalType.DATE)
    @Column(name = "CREATE_DATE")
    private Date createDate;

    @Column(name = "CHANNEL_TYPE_ID", nullable = false, precision = 10)
    private Long channelTypeId;

    @Column(name = "DISCOUNT_POLICY", length = 20)
    private String discountPolicy;

    @Column(name = "PRICE_POLICY", length = 20)
    private String pricePolicy;

    @Column(name = "SHOP_PATH", length = 500)
    private String shopPath;

    @Column(name = "DISTRICT", length = 50)
    private String district;

    @Column(name = "PRECINCT", length = 50)
    private String precinct;

    @Column(name = "AREA_CODE", length = 200)
    private String areaCode;

    @Column(name = "ID_NO", length = 50)
    private String idNo;

    @Column(name = "ID_ISSUE_PLACE", length = 100)
    private String idIssuePlace;

    @Temporal(TemporalType.DATE)
    @Column(name = "ID_ISSUE_DATE")
    private Date idIssueDate;

    @Column(name = "STREET_BLOCK", length = 100)
    private String streetBlock;

    @Column(name = "STREET", length = 100)
    private String street;

    @Column(name = "HOME", length = 100)
    private String home;

    @Column(name = "ID_TYPE", precision = 2)
    private Integer idType;

    @Column(name = "SHOP_PATH_NAME", length = 2000)
    private String shopPathName;

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

    @Column(name = "FILE_NAME", length = 200)
    private String fileName;

    @Column(name = "BUSINESS_LICENCE", length = 200)
    private String businessLicence;

    @Column(name = "BANKPLUS_MOBILE", length = 20)
    private String bankplusMobile;

    @Column(name = "STOCK_NUM", precision = 7)
    private Integer stockNum;

    @Column(name = "STOCK_NUM_IMP", precision = 7)
    private Integer stockNumImp;

    @Temporal(TemporalType.DATE)
    @Column(name = "UPDATE_DATE_TIME")
    private Date updateDateTime;

    @Column(name = "BANK_CODE", length = 30)
    private String bankCode;

    @Column(name = "SHOP_KEEPER_ID", precision = 20)
    private Long shopKeeperId;

    @Column(name = "SHOP_DIRECTOR_ID", precision = 20)
    private Long shopDirectorId;

    @Column(name = "FILE_ATTACHMENT_ID", precision = 20)
    private Long fileAttachmentId;

    @Column(name = "GROUP_CHANNEL_TYPE_ID", precision = 10)
    private Long groupChannelTypeId;

    @Column(name = "STAFF_OWNER_ID", precision = 10)
    private Long staffOwnerId;

    @Column(name = "TENANT_ID")
    private Long tenantId;

    @Column(name = "FILE_CONTRACT_ID", precision = 20)
    private Long fileContractId;

    @Column(name = "FILE_CONTRACT_CATALOG_ID", precision = 20)
    private Long fileContractCatalogId;

    @Column(name = "FILE_IDENTITY_ID", precision = 20)
    private Long fileIdentityId;

    @Column(name = "FILE_BUSINESS_LICENSE_ID", precision = 20)
    private Long fileBusinessLicenseId;

    @Column(name = "BUSINESS_PROVINCE", length = 5)
    private String businessProvince;

    @Column(name = "BUSINESS_DISTRICT", length = 5)
    private String businessDistrict;

    @Column(name = "BUSINESS_PRECINCT", length = 5)
    private String businessPrecinct;

    @Column(name = "BUSINESS_STREET_BLOCK", length = 100)
    private String businessStreetBlock;

    @Column(name = "BUSINESS_STREET", length = 100)
    private String businessStreet;

    @Column(name = "BUSINESS_HOME", length = 100)
    private String businessHome;

    @Column(name = "BUSINESS_AREACODE", length = 50)
    private String businessAreacode;

    @Column(name = "BUSINESS_ADDRESS", length = 500)
    private String businessAddress;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "TURNOVER", length = 1)
    private String turnover;

    @Temporal(TemporalType.DATE)
    @Column(name = "BIRTHDAY")
    private Date birthday;
}