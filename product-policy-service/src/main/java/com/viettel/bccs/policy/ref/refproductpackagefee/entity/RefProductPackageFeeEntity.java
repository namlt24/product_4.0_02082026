package com.viettel.bccs.policy.ref.refproductpackagefee.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "REF_PRODUCT_PACKAGE_FEE")
@Getter
@Setter
public class RefProductPackageFeeEntity {

    @Id
    @Column(name = "PRODUCT_PACKAGE_FEE_ID", nullable = false, precision = 10)
    private Long productPackageFeeId;

    @Column(name = "PRODUCT_PACKAGE_ID", nullable = false, precision = 10)
    private Long productPackageId;

    @Column(name = "PRICE_POLICY_ID", nullable = false, precision = 10)
    private Long pricePolicyId;

    @Column(name = "PRICE_TYPE_ID", nullable = false, precision = 10)
    private Long priceTypeId;

    @Column(name = "NAME", length = 50)
    private String name;

    @Column(name = "PRICE", precision = 10)
    private Long price;

    @Column(name = "VAT", precision = 10)
    private Long vat;

    @Temporal(TemporalType.DATE)
    @Column(name = "EFFECT_DATETIME")
    private Date effectDatetime;

    @Temporal(TemporalType.DATE)
    @Column(name = "EXPIRE_DATETIME")
    private Date expireDatetime;

    @Column(name = "STATUS", length = 1)
    private String status;
}