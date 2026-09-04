package com.viettel.bccs.productcatalog.productspeccharuse.entity;

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
@Table(name = "PRODUCT_SPEC_CHAR_USE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSpecCharUseEntity {

    @Id
    @Column(name = "PROD_SPEC_CHAR_USE_ID", precision = 10)
    private Long prodSpecCharUseId;

    @Column(name = "ORDER_CHAR", precision = 10)
    private Long orderChar;

    @Column(name = "PRODUCT_SPEC_ID", precision = 10, nullable = false)
    private Long productSpecId;

    @Column(name = "PRODUCT_SPEC_CHAR_ID", precision = 10, nullable = false)
    private Long productSpecCharId;

    @Column(name = "PRODUCT_SPEC_CHAR_VALUE_ID", precision = 10)
    private Long productSpecCharValueId;

    @Column(name = "STATUS", length = 1)
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

    @Column(name = "SYSTEM_TYPE", length = 100)
    private String systemType;

    @Column(name = "SPECIFIC_VALUE", length = 4000)
    private String specificValue;

    @Column(name = "CONFIG_PHASE", length = 200)
    private String configPhase;

    @Column(name = "MIN", precision = 10)
    private Long min;

    @Column(name = "MAX", precision = 10)
    private Long max;

    @Column(name = "IS_REQUIRED", length = 1)
    private String isRequired;

    @Column(name = "NOTE", length = 2000)
    private String note;
}
