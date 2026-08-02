package com.viettel.bccs.organization.staffext.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "STAFF_EXT")
@Getter
@Setter
public class StaffExtEntity {

    @Id
    @Column(name = "STAFF_EXT_ID", nullable = false, precision = 10)
    private Long staffExtId;

    @Column(name = "STAFF_ID", precision = 10)
    private Long staffId;

    @Column(name = "KEY", length = 50)
    private String staffExtKey;

    @Column(name = "VALUE", length = 4000)
    private String value;

    @Column(name = "STATUS", length = 2)
    private String status;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;
}