package com.viettel.bccs.organization.channeltype.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CHANNEL_TYPE")
@Getter
@Setter
public class ChannelTypeEntity {

    @Id
    @Column(name = "CHANNEL_TYPE_ID", nullable = false, precision = 10)
    private Long channelTypeId;

    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "OBJECT_TYPE", length = 1)
    private String objectType;

    @Column(name = "IS_VT_UNIT", length = 1)
    private String isVtUnit;

    @Column(name = "CHECK_COMM", length = 1)
    private String checkComm;

    @Column(name = "STOCK_TYPE", precision = 1)
    private Integer stockType;

    @Column(name = "STOCK_REPORT_TEMPLATE", length = 50)
    private String stockReportTemplate;

    @Column(name = "TOTAL_DEBIT", precision = 10)
    private Long totalDebit;

    @Column(name = "ALLOW_ADD_BATCH", precision = 1)
    private Integer allowAddBatch;

    @Column(name = "SUFFIX_OBJECT_CODE", length = 50)
    private String suffixObjectCode;

    @Column(name = "UPDATE_STAFF_OWNER_ROLE", length = 100)
    private String updateStaffOwnerRole;

    @Column(name = "DISCOUNT_POLICY_DEFAUT", length = 100)
    private String discountPolicyDefaut;

    @Column(name = "PRICE_POLICY_DEFAUT", length = 100)
    private String pricePolicyDefaut;

    @Column(name = "UPDATE_BLANK_CODE_ROLE", length = 100)
    private String updateBlankCodeRole;

    @Column(name = "UPDATE_OBJECT_INFO_ROLE", length = 100)
    private String updateObjectInfoRole;

    @Column(name = "UPDATE_SHOP_ROLE", length = 100)
    private String updateShopRole;

    @Column(name = "CODE", length = 50)
    private String code;

    @Column(name = "GROUP_CHANNEL_TYPE_ID", precision = 10)
    private Long groupChannelTypeId;

    @Column(name = "GROUP_CHANNEL_ID", precision = 10)
    private Long groupChannelId;

    @Column(name = "IS_COLL_CHANNEL", precision = 2)
    private Integer isCollChannel;

    @Column(name = "IS_NOT_BLANK_CODE", precision = 2)
    private Integer isNotBlankCode;

    @Column(name = "IS_VHR_CHANNEL", precision = 2)
    private Integer isVhrChannel;

    @Temporal(TemporalType.DATE)
    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Column(name = "CREATE_USER", length = 100)
    private String createUser;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "PAYMENT_CODE", length = 100)
    private String paymentCode;

    @Column(name = "PAYMENT_TAIL", length = 100)
    private String paymentTail;

    @Column(name = "ASSIGN_CUST_STATUS", precision = 1)
    private Integer assignCustStatus;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;
}
