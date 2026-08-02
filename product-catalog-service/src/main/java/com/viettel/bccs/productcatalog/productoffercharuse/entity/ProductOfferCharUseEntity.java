package com.viettel.bccs.productcatalog.productoffercharuse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "PRODUCT_OFFER_CHAR_USE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOfferCharUseEntity {

    @Id
    @Column(name = "PRODUCT_OFFER_CHAR_USE_ID", precision = 10)
    private Long productOfferCharUseId;

    @Column(name = "ORDER_CHAR", precision = 10)
    private Long orderChar;

    @Column(name = "TYPE", length = 1)
    private String type;

    @Column(name = "PRODUCT_OFFERING_ID", precision = 10)
    private Long productOfferingId;

    @Column(name = "PRODUCT_SPEC_CHAR_VALUE_ID", precision = 10)
    private Long productSpecCharValueId;

    @Column(name = "PRODUCT_SPEC_CHAR_ID", precision = 10)
    private Long productSpecCharId;

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

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "SPECIFIC_VALUE", length = 500)
    private String specificValue;

    @Temporal(TemporalType.DATE)
    @Column(name = "EFFECT_DATETIME")
    private Date effectDatetime;

    @Temporal(TemporalType.DATE)
    @Column(name = "EXPIRE_DATETIME")
    private Date expireDatetime;

    @Column(name = "LIMITED", precision = 1)
    private Long limited;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;
}