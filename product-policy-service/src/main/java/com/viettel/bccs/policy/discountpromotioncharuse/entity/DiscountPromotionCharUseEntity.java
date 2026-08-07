package com.viettel.bccs.policy.discountpromotioncharuse.entity;

import jakarta.persistence.*;
import java.util.Date;

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
    public Long getDiscountPromotionCharUseId() { return discountPromotionCharUseId; }
    public Long getDiscountPromotionId() { return discountPromotionId; }
    public Long getProductSpecCharValueId() { return productSpecCharValueId; }
    public Long getProductSpecCharId() { return productSpecCharId; }
    public String getCreateUser() { return createUser; }
    public Date getCreateDatetime() { return createDatetime; }
    public String getUpdateUser() { return updateUser; }
    public Date getUpdateDatetime() { return updateDatetime; }
    public String getStatus() { return status; }
    public String getSpecificValue() { return specificValue; }
    public Date getEffectDatetime() { return effectDatetime; }
    public Date getExpireDatetime() { return expireDatetime; }
    public Long getLimited() { return limited; }
    public String getNote() { return note; }

    // Setters
    public void setDiscountPromotionCharUseId(Long discountPromotionCharUseId) { this.discountPromotionCharUseId = discountPromotionCharUseId; }
    public void setDiscountPromotionId(Long discountPromotionId) { this.discountPromotionId = discountPromotionId; }
    public void setProductSpecCharValueId(Long productSpecCharValueId) { this.productSpecCharValueId = productSpecCharValueId; }
    public void setProductSpecCharId(Long productSpecCharId) { this.productSpecCharId = productSpecCharId; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public void setCreateDatetime(Date createDatetime) { this.createDatetime = createDatetime; }
    public void setUpdateDatetime(Date updateDatetime) { this.updateDatetime = updateDatetime; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
    public void setStatus(String status) { this.status = status; }
    public void setSpecificValue(String specificValue) { this.specificValue = specificValue; }
    public void setEffectDatetime(Date effectDatetime) { this.effectDatetime = effectDatetime; }
    public void setExpireDatetime(Date expireDatetime) { this.expireDatetime = expireDatetime; }
    public void setLimited(Long limited) { this.limited = limited; }
    public void setNote(String note) { this.note = note; }
}