package com.viettel.bccs.productcatalog.productpackagefee.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "PRODUCT_PACKAGE_FEE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPackageFeeEntity {

    @Id
    @Column(name = "PRODUCT_PACKAGE_FEE_ID", precision = 10)
    private Long productPackageFeeId;

    @Column(name = "PRODUCT_PACKAGE_ID", precision = 10)
    private Long productPackageId;

    @Column(name = "PRICE_POLICY_ID", precision = 10)
    private Long pricePolicyId;

    @Column(name = "PRICE_TYPE_ID", precision = 10)
    private Long priceTypeId;

    @Column(name = "NAME", length = 50)
    private String name;

    @Column(name = "DESCRIPTION", length = 512)
    private String description;

    @Column(name = "PRICE", precision = 10)
    private BigDecimal price;

    @Column(name = "VAT", precision = 10)
    private BigDecimal vat;

    @Column(name = "EFFECT_DATETIME")
    private Date effectDatetime;

    @Column(name = "EXPIRE_DATETIME")
    private Date expireDatetime;

    @Column(name = "PRIORITY", precision = 2)
    private Long priority;

    @Column(name = "EFFECT_TYPE", length = 1)
    private String effectType;

    @Column(name = "CRON_EXPRESSION", length = 50)
    private String cronExpression;

    @Column(name = "REAL_STEP", length = 50)
    private String realStep;

    @Column(name = "REVENUE_OBJ", length = 50)
    private String revenueObj;

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

    @Column(name = "CODE", length = 50)
    private String code;

    @Column(name = "FILE_ATTACHMENT_ID", precision = 10)
    private Long fileAttachmentId;

    @Column(name = "DISTRIBUTE", precision = 2)
    private Long distribute;

    @Column(name = "SAP_MATERIAL_NUMBER", precision = 10)
    private Long sapMaterialNumber;
}