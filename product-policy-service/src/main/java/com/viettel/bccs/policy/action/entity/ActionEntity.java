package com.viettel.bccs.policy.action.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ACTION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionEntity {

    @Id
    @Column(name = "ACTION_CODE", length = 10)
    private String actionCode;

    @Column(name = "NAME", length = 512)
    private String name;

    @Column(name = "DESCRIPTION", length = 512)
    private String description;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Column(name = "UPADTE_USER", length = 50)
    private String updateUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "TYPE", length = 2)
    private String type;

    @Column(name = "REASON_TYPE", length = 20)
    private String reasonType;
}