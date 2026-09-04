package com.viettel.bccs.organization.identitytype.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "IDENTITY_TYPE")
@Getter
@Setter
public class IdentityTypeEntity {

    @Id
    @Column(name = "ID_TYPE", nullable = false, length = 10)
    private String idType;

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

    @Column(name = "MIN_LENGTH", precision = 3)
    private Integer minLength;

    @Column(name = "MAX_LENGTH", precision = 3)
    private Integer maxLength;

    @Column(name = "VALUE_PATTERN", length = 100)
    private String valuePattern;
}
