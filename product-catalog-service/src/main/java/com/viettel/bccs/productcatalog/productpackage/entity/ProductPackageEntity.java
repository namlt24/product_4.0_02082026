package com.viettel.bccs.productcatalog.productpackage.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "PRODUCT_PACKAGE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPackageEntity {

    @Id
    @Column(name = "PRODUCT_PACKAGE_ID", precision = 10)
    private Long productPackageId;

    @Column(name = "NAME", length = 500)
    private String name;

    @Column(name = "CODE", length = 50)
    private String code;

    @Column(name = "DESCRIPTION", length = 512)
    private String description;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "EFFECT_DATETIME")
    private Date effectDatetime;

    @Column(name = "EXPIRE_DATETIME")
    private Date expireDatetime;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "VERSION", length = 50)
    private String version;

    @Column(name = "TYPE", length = 1)
    private String type;

    @Column(name = "ACCOUNTING_ID", precision = 10)
    private Long accountingId;

    @Column(name = "FEE_TYPE", length = 1)
    private String feeType;

    @Column(name = "TELECOM_SERVICE_ID", precision = 10)
    private Long telecomServiceId;

    @Column(name = "SALE_TYPE", length = 1)
    private String saleType;

    @Column(name = "UNIT", length = 10)
    private String unit;

    @Column(name = "NOTE1", length = 1000)
    private String note1;

    @Column(name = "NOTE2", length = 1000)
    private String note2;

    @Column(name = "NOTE", length = 1000)
    private String note;

    @Column(name = "AREA_GROUP_ID", precision = 10)
    private Long areaGroupId;

    @Column(name = "OWNER_SHOP_ID", precision = 10)
    private Long ownerShopId;

    @Column(name = "SAP_MATERIAL_NUMBER", precision = 10)
    private Long sapMaterialNumber;

    @Column(name = "IT_TELCOL", length = 6)
    private String itTelcol;
}