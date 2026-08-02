package com.viettel.bccs.organization.custtype.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "CUST_TYPE")
@Getter
@Setter
public class CustTypeEntity {

    @Id
    @Column(name = "CUST_TYPE", nullable = false, length = 6)
    private String custType;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "CREATE_USER", nullable = false, length = 50)
    private String createUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "CREATE_DATETIME", nullable = false)
    private Date createDatetime;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "DESCRIPTION", length = 512)
    private String description;

    @Column(name = "STATUS", nullable = false, length = 1)
    private String status;

    @Column(name = "GROUP_TYPE", length = 1)
    private String groupType;

    @Column(name = "TAX", precision = 10)
    private Long tax;

    @Column(name = "PLAN", length = 1)
    private String plan;

    @Column(name = "REPRESENT_CUST", length = 1)
    private String representCust;

    @Column(name = "CUST_TYPE_ID", precision = 10)
    private Long custTypeId;
}