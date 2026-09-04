package com.viettel.bccs.productcatalog.productpackagecharuse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT_PACKAGE_CHAR_USE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPackageCharUseEntity {

    @Id
    @Column(name = "PRODUCT_PACKAGE_CHAR_USE_ID", precision = 10)
    private Long productPackageCharUseId;

    @Column(name = "PRODUCT_PACKAGE_ID", precision = 10)
    private Long productPackageId;

    @Column(name = "PRODUCT_SPEC_CHAR_ID", precision = 10)
    private Long productSpecCharId;

    @Column(name = "PRODUCT_SPEC_CHAR_VALUE_ID", precision = 10)
    private Long productSpecCharValueId;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Column(name = "CREATE_DATETIME")
    private java.util.Date createDatetime;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Column(name = "UPDATE_DATETIME")
    private java.util.Date updateDatetime;

    @Column(name = "SPECIFIC_VALUE", length = 50)
    private String specificValue;

    @Column(name = "LIMITED", precision = 1)
    private Long limited;

    @Column(name = "EFFECT_DATETIME")
    private java.util.Date effectDatetime;

    @Column(name = "EXPIRE_DATETIME")
    private java.util.Date expireDatetime;

    @Column(name = "DESCRIPTION", length = 100)
    private String description;
}