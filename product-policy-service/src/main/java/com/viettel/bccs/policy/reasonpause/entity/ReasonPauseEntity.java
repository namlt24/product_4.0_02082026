package com.viettel.bccs.policy.reasonpause.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "REASON_PAUSE")
public class ReasonPauseEntity {

    @Id
    @Column(name = "REASON_PAUSE_ID", precision = 10)
    private Long reasonPauseId;

    @Column(name = "NUM_MONTH", precision = 10)
    private Long numMonth;

    @Column(name = "PRICE", precision = 10)
    private Long price;

    @Column(name = "REASON_ID", precision = 10)
    private Long reasonId;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    // Getters
    public Long getReasonPauseId() { return reasonPauseId; }
    public Long getNumMonth() { return numMonth; }
    public Long getPrice() { return price; }
    public Long getReasonId() { return reasonId; }
    public String getStatus() { return status; }
    public Date getCreateDatetime() { return createDatetime; }
    public String getCreateUser() { return createUser; }
    public Date getUpdateDatetime() { return updateDatetime; }
    public String getUpdateUser() { return updateUser; }

    // Setters
    public void setReasonPauseId(Long reasonPauseId) { this.reasonPauseId = reasonPauseId; }
    public void setNumMonth(Long numMonth) { this.numMonth = numMonth; }
    public void setPrice(Long price) { this.price = price; }
    public void setReasonId(Long reasonId) { this.reasonId = reasonId; }
    public void setStatus(String status) { this.status = status; }
    public void setCreateDatetime(Date createDatetime) { this.createDatetime = createDatetime; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public void setUpdateDatetime(Date updateDatetime) { this.updateDatetime = updateDatetime; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
}
