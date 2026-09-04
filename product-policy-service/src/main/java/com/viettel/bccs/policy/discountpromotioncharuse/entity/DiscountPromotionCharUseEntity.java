package com.viettel.bccs.policy.discountpromotioncharuse.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DISCOUNT_PROMOTION_CHAR_USE")
@Getter
@Setter
public class DiscountPromotionCharUseEntity {

    @Id
    @Column(name = "DISCOUNT_PROMOTION_CHAR_USE_ID", precision = 10)
    private Long discountPromotionCharUseId;

    @Column(name = "DISCOUNT_PROMOTION_ID", precision = 10)
    private Long discountPromotionId;

    @Column(name = "PRODUCT_SPEC_CHAR_VALUE_ID", precision = 10)
    private Long productSpecCharValueId;

    @Column(name = "PRODUCT_SPEC_CHAR_ID", precision = 10)
    private Long productSpecCharId;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "SPECIFIC_VALUE", length = 2000)
    private String specificValue;

    @Column(name = "EFFECT_DATETIME")
    private Date effectDatetime;

    @Column(name = "EXPIRE_DATETIME")
    private Date expireDatetime;

    @Column(name = "LIMITED", precision = 1)
    private Long limited;

    @Column(name = "NOTE", length = 2000)
    private String note;

    // Getters

    // Setters
}