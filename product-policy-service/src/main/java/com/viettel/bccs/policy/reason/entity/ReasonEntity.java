package com.viettel.bccs.policy.reason.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "REASON")
@Getter
@Setter
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

    // Setters
}