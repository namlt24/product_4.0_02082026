package com.viettel.bccs.policy.reasonpause.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "REASON_PAUSE")
@Getter
@Setter
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

    // Setters
}
