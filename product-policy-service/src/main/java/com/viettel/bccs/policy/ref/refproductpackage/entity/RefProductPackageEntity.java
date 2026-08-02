package com.viettel.bccs.policy.ref.refproductpackage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "REF_PRODUCT_PACKAGE")
@Getter
@Setter
public class RefProductPackageEntity {

    @Id
    @Column(name = "PRODUCT_PACKAGE_ID", nullable = false, precision = 10)
    private Long productPackageId;

    @Column(name = "NAME", nullable = false, length = 500)
    private String name;

    @Column(name = "CODE", length = 50)
    private String code;

    @Column(name = "DESCRIPTION", length = 512)
    private String description;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Temporal(TemporalType.DATE)
    @Column(name = "EFFECT_DATETIME")
    private Date effectDatetime;

    @Temporal(TemporalType.DATE)
    @Column(name = "EXPIRE_DATETIME")
    private Date expireDatetime;

    @Temporal(TemporalType.DATE)
    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "TYPE", length = 1)
    private String type;

    @Column(name = "ACCOUNTING_ID", precision = 10)
    private Long accountingId;

    @Column(name = "FEE_TYPE", length = 1)
    private String feeType;

    @Column(name = "TELECOM_SERVICE_ID", precision = 10)
    private Long telecomServiceId;
}