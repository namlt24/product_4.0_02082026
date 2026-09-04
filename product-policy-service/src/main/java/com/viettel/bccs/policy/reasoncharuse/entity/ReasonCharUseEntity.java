package com.viettel.bccs.policy.reasoncharuse.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "REASON_CHAR_USE")
@Getter
@Setter
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

    // Setters
}
