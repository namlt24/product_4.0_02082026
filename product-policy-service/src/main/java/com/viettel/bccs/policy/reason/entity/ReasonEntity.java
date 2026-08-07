package com.viettel.bccs.policy.reason.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "REASON")
public class ReasonEntity {

    @Id
    @Column(name = "REASON_ID", precision = 10)
    private Long reasonId;

    @Column(name = "REASON_CODE", length = 20)
    private String reasonCode;

    @Column(name = "REASON_TYPE", length = 20)
    private String reasonType;

    @Column(name = "NAME", length = 512)
    private String name;

    @Column(name = "PAY_TYPE", length = 1)
    private String payType;

    @Column(name = "TEL_SERVICE", length = 1000)
    private String telService;

    @Column(name = "DESCRIPTION", length = 512)
    private String description;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "LIMIT_NUMBER_ISDN", precision = 10)
    private Long limitNumberIsdn;

    @Column(name = "LIMIT_NUMBER_USER", precision = 10)
    private Long limitNumberUser;

    @Column(name = "TYPE", length = 1)
    private String type;

    @Column(name = "EFFECT_DATETIME")
    private Date effectDatetime;

    @Column(name = "EXPIRE_DATETIME")
    private Date expireDatetime;

    @Column(name = "PRIORITY", precision = 10)
    private Long priority;

    @Column(name = "NOTE", length = 1000)
    private String note;

    // Getters
    public Long getReasonId() { return reasonId; }
    public String getReasonCode() { return reasonCode; }
    public String getReasonType() { return reasonType; }
    public String getName() { return name; }
    public String getPayType() { return payType; }
    public String getTelService() { return telService; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getCreateUser() { return createUser; }
    public Date getCreateDatetime() { return createDatetime; }
    public String getUpdateUser() { return updateUser; }
    public Date getUpdateDatetime() { return updateDatetime; }
    public Long getLimitNumberIsdn() { return limitNumberIsdn; }
    public Long getLimitNumberUser() { return limitNumberUser; }
    public String getType() { return type; }
    public Date getEffectDatetime() { return effectDatetime; }
    public Date getExpireDatetime() { return expireDatetime; }
    public Long getPriority() { return priority; }
    public String getNote() { return note; }

    // Setters
    public void setReasonId(Long reasonId) { this.reasonId = reasonId; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    public void setReasonType(String reasonType) { this.reasonType = reasonType; }
    public void setName(String name) { this.name = name; }
    public void setPayType(String payType) { this.payType = payType; }
    public void setTelService(String telService) { this.telService = telService; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public void setCreateDatetime(Date createDatetime) { this.createDatetime = createDatetime; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
    public void setUpdateDatetime(Date updateDatetime) { this.updateDatetime = updateDatetime; }
    public void setLimitNumberIsdn(Long limitNumberIsdn) { this.limitNumberIsdn = limitNumberIsdn; }
    public void setLimitNumberUser(Long limitNumberUser) { this.limitNumberUser = limitNumberUser; }
    public void setType(String type) { this.type = type; }
    public void setEffectDatetime(Date effectDatetime) { this.effectDatetime = effectDatetime; }
    public void setExpireDatetime(Date expireDatetime) { this.expireDatetime = expireDatetime; }
    public void setPriority(Long priority) { this.priority = priority; }
    public void setNote(String note) { this.note = note; }
}