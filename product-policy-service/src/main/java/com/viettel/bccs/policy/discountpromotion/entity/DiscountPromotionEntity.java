package com.viettel.bccs.policy.discountpromotion.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "DISCOUNT_PROMOTION")
public class DiscountPromotionEntity {

    @Id
    @Column(name = "DISCOUNT_PROMOTION_ID", precision = 10)
    private Long discountPromotionId;

    @Column(name = "TELECOM_SERVICE_ID", length = 100, nullable = false)
    private String telecomServiceId;

    @Column(name = "CODE", length = 20)
    private String code;

    @Column(name = "NAME", length = 1500)
    private String name;

    @Column(name = "TYPE", length = 1)
    private String type;

    @Column(name = "SYSTEM_TYPE", length = 1)
    private String systemType;

    @Column(name = "DISCOUNT_METHOD", length = 1)
    private String discountMethod;

    @Column(name = "DISCOUNT_POLICY", length = 10)
    private String discountPolicy;

    @Column(name = "SUB_TYPE", length = 1)
    private String subType;

    @Column(name = "MONTH_COMMITMENT", precision = 10)
    private Long monthCommitment;

    @Column(name = "PRICE_PLAN", length = 30)
    private String pricePlan;

    @Column(name = "MONTH_AMOUNT", precision = 10)
    private Long monthAmount;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "DESCRIPTION", length = 4000)
    private String description;

    @Column(name = "CONTENT", length = 4000)
    private String content;

    @Column(name = "AREA_CODE", length = 300)
    private String areaCode;

    @Column(name = "EFFECT_DATETIME", nullable = false)
    private Date effectDatetime;

    @Column(name = "EXPIRE_DATETIME")
    private Date expireDatetime;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "CYCLE", precision = 10)
    private Long cycle;

    @Column(name = "LIST_TYPE", length = 50)
    private String listType;

    @Column(name = "SUB_LIST_ID", precision = 7)
    private Long subListId;

    @Column(name = "NOTE", length = 4000)
    private String note;

    // Getters
    public Long getDiscountPromotionId() { return discountPromotionId; }
    public String getTelecomServiceId() { return telecomServiceId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getSystemType() { return systemType; }
    public String getDiscountMethod() { return discountMethod; }
    public String getDiscountPolicy() { return discountPolicy; }
    public String getSubType() { return subType; }
    public Long getMonthCommitment() { return monthCommitment; }
    public String getPricePlan() { return pricePlan; }
    public Long getMonthAmount() { return monthAmount; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }
    public String getContent() { return content; }
    public String getAreaCode() { return areaCode; }
    public Date getEffectDatetime() { return effectDatetime; }
    public Date getExpireDatetime() { return expireDatetime; }
    public String getCreateUser() { return createUser; }
    public Date getCreateDatetime() { return createDatetime; }
    public String getUpdateUser() { return updateUser; }
    public Date getUpdateDatetime() { return updateDatetime; }
    public Long getCycle() { return cycle; }
    public String getListType() { return listType; }
    public Long getSubListId() { return subListId; }
    public String getNote() { return note; }

    // Setters
    public void setDiscountPromotionId(Long discountPromotionId) { this.discountPromotionId = discountPromotionId; }
    public void setTelecomServiceId(String telecomServiceId) { this.telecomServiceId = telecomServiceId; }
    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setSystemType(String systemType) { this.systemType = systemType; }
    public void setDiscountMethod(String discountMethod) { this.discountMethod = discountMethod; }
    public void setDiscountPolicy(String discountPolicy) { this.discountPolicy = discountPolicy; }
    public void setSubType(String subType) { this.subType = subType; }
    public void setMonthCommitment(Long monthCommitment) { this.monthCommitment = monthCommitment; }
    public void setPricePlan(String pricePlan) { this.pricePlan = pricePlan; }
    public void setMonthAmount(Long monthAmount) { this.monthAmount = monthAmount; }
    public void setStatus(String status) { this.status = status; }
    public void setDescription(String description) { this.description = description; }
    public void setContent(String content) { this.content = content; }
    public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
    public void setEffectDatetime(Date effectDatetime) { this.effectDatetime = effectDatetime; }
    public void setExpireDatetime(Date expireDatetime) { this.expireDatetime = expireDatetime; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public void setCreateDatetime(Date createDatetime) { this.createDatetime = createDatetime; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
    public void setUpdateDatetime(Date updateDatetime) { this.updateDatetime = updateDatetime; }
    public void setCycle(Long cycle) { this.cycle = cycle; }
    public void setListType(String listType) { this.listType = listType; }
    public void setSubListId(Long subListId) { this.subListId = subListId; }
    public void setNote(String note) { this.note = note; }
}