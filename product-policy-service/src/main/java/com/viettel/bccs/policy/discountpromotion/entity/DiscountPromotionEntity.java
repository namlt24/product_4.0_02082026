package com.viettel.bccs.policy.discountpromotion.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DISCOUNT_PROMOTION")
@Getter
@Setter
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

    // Setters
}