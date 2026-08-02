package com.viettel.bccs.productcatalog.productspeccharvalue.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "PRODUCT_SPEC_CHAR_VALUE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSpecCharValueEntity {

    @Id
    @Column(name = "PRODUCT_SPEC_CHAR_VALUE_ID", precision = 10)
    private Long productSpecCharValueId;

    @Column(name = "PRODUCT_SPEC_CHAR_ID", precision = 10)
    private Long productSpecCharId;

    @Column(name = "VALUE_TYPE", length = 10)
    private String valueType;

    @Column(name = "IS_DEFAULT", precision = 1, nullable = false)
    private Long isDefault;

    @Column(name = "VALUE", length = 4000)
    private String value;

    @Column(name = "UNIT_OF_MEASURE", length = 10)
    private String unitOfMeasure;

    @Column(name = "VALUE_FROM", length = 4000)
    private String valueFrom;

    @Column(name = "VALUE_TO", length = 4000)
    private String valueTo;

    @Column(name = "RANGE_INTERVAL", length = 10)
    private String rangeInterval;

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

    @Column(name = "NAME", length = 500)
    private String name;

    @Column(name = "SPECIFIC_VALUE", columnDefinition = "CLOB")
    private String specificValue;

    @Column(name = "NOTE", length = 256)
    private String note;
}