package com.viettel.bccs.productcatalog.productofferrelationdetail.entity;

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
@Table(name = "PRODUCT_OFFER_RELATION_DETAIL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOfferRelationDetailEntity {

    @Id
    @Column(name = "PRODUCT_OFFER_RELATION_DETAIL", precision = 38)
    private Long productOfferRelationDetail;

    @Column(name = "PRODUCT_OFFER_RELATION_ID", precision = 10)
    private Long productOfferRelationId;

    @Column(name = "PRODUCT_SPEC_CHAR_ID", precision = 10)
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

    @Column(name = "SPECIFIC_VALUE", length = 50)
    private String specificValue;

    @Column(name = "DESCRIPTION", length = 512)
    private String description;

    @Temporal(TemporalType.DATE)
    @Column(name = "EFFECT_DATETIME")
    private Date effectDatetime;

    @Temporal(TemporalType.DATE)
    @Column(name = "EXPIRE_DATETIME")
    private Date expireDatetime;
}