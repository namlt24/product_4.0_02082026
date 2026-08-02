package com.viettel.bccs.policy.discountpromotioncharuse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DISCOUNT_PROMOTION_CHAR_USE")
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
    private LocalDateTime createDatetime;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Column(name = "UPDATE_DATETIME")
    private LocalDateTime updateDatetime;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "SPECIFIC_VALUE", length = 2000)
    private String specificValue;

    @Column(name = "EFFECT_DATETIME")
    private LocalDateTime effectDatetime;

    @Column(name = "EXPIRE_DATETIME")
    private LocalDateTime expireDatetime;

    @Column(name = "LIMITED", precision = 1)
    private Long limited;

    @Column(name = "NOTE", length = 2000)
    private String note;

    // Getters
    public Long getDiscountPromotionCharUseId() { return discountPromotionCharUseId; }
    public Long getDiscountPromotionId() { return discountPromotionId; }
    public Long getProductSpecCharValueId() { return productSpecCharValueId; }
    public Long getProductSpecCharId() { return productSpecCharId; }
    public String getCreateUser() { return createUser; }
    public LocalDateTime getCreateDatetime() { return createDatetime; }
    public String getUpdateUser() { return updateUser; }
    public LocalDateTime getUpdateDatetime() { return updateDatetime; }
    public String getStatus() { return status; }
    public String getSpecificValue() { return specificValue; }
    public LocalDateTime getEffectDatetime() { return effectDatetime; }
    public LocalDateTime getExpireDatetime() { return expireDatetime; }
    public Long getLimited() { return limited; }
    public String getNote() { return note; }

    // Setters
    public void setDiscountPromotionCharUseId(Long discountPromotionCharUseId) { this.discountPromotionCharUseId = discountPromotionCharUseId; }
    public void setDiscountPromotionId(Long discountPromotionId) { this.discountPromotionId = discountPromotionId; }
    public void setProductSpecCharValueId(Long productSpecCharValueId) { this.productSpecCharValueId = productSpecCharValueId; }
    public void setProductSpecCharId(Long productSpecCharId) { this.productSpecCharId = productSpecCharId; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public void setCreateDatetime(LocalDateTime createDatetime) { this.createDatetime = createDatetime; }
    public void setUpdateDatetime(LocalDateTime updateDatetime) { this.updateDatetime = updateDatetime; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
    public void setStatus(String status) { this.status = status; }
    public void setSpecificValue(String specificValue) { this.specificValue = specificValue; }
    public void setEffectDatetime(LocalDateTime effectDatetime) { this.effectDatetime = effectDatetime; }
    public void setExpireDatetime(LocalDateTime expireDatetime) { this.expireDatetime = expireDatetime; }
    public void setLimited(Long limited) { this.limited = limited; }
    public void setNote(String note) { this.note = note; }
}