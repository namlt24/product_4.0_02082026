package com.viettel.bccs.policy.reasoncharuse.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "REASON_CHAR_USE")
public class ReasonCharUseEntity {

    @Id
    @Column(name = "REASON_CHAR_USE_ID", precision = 10)
    private Long reasonCharUseId;

    @Column(name = "REASON_ID", precision = 10)
    private Long reasonId;

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

    @Column(name = "SPECIFIC_VALUE", length = 50)
    private String specificValue;

    @Column(name = "LIMITED", precision = 1)
    private Long limited;

    @Column(name = "LIMITED2", precision = 1)
    private Long limited2;

    @Column(name = "MIN", precision = 10)
    private Long min;

    @Column(name = "MAX", precision = 10)
    private Long max;

    // Getters
    public Long getReasonCharUseId() { return reasonCharUseId; }
    public Long getReasonId() { return reasonId; }
    public Long getProductSpecCharValueId() { return productSpecCharValueId; }
    public Long getProductSpecCharId() { return productSpecCharId; }
    public String getCreateUser() { return createUser; }
    public Date getCreateDatetime() { return createDatetime; }
    public String getUpdateUser() { return updateUser; }
    public Date getUpdateDatetime() { return updateDatetime; }
    public String getStatus() { return status; }
    public String getSpecificValue() { return specificValue; }
    public Long getLimited() { return limited; }
    public Long getLimited2() { return limited2; }
    public Long getMin() { return min; }
    public Long getMax() { return max; }

    // Setters
    public void setReasonCharUseId(Long reasonCharUseId) { this.reasonCharUseId = reasonCharUseId; }
    public void setReasonId(Long reasonId) { this.reasonId = reasonId; }
    public void setProductSpecCharValueId(Long productSpecCharValueId) { this.productSpecCharValueId = productSpecCharValueId; }
    public void setProductSpecCharId(Long productSpecCharId) { this.productSpecCharId = productSpecCharId; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public void setCreateDatetime(Date createDatetime) { this.createDatetime = createDatetime; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
    public void setUpdateDatetime(Date updateDatetime) { this.updateDatetime = updateDatetime; }
    public void setStatus(String status) { this.status = status; }
    public void setSpecificValue(String specificValue) { this.specificValue = specificValue; }
    public void setLimited(Long limited) { this.limited = limited; }
    public void setLimited2(Long limited2) { this.limited2 = limited2; }
    public void setMin(Long min) { this.min = min; }
    public void setMax(Long max) { this.max = max; }
}
