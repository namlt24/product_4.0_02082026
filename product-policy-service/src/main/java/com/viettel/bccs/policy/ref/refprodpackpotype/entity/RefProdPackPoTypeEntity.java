package com.viettel.bccs.policy.ref.refprodpackpotype.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "REF_PROD_PACK_PO_TYPE")
@Getter
@Setter
public class RefProdPackPoTypeEntity {

    @Id
    @Column(name = "PROD_PACK_TYPE_ID", nullable = false, precision = 10)
    private Long prodPackTypeId;

    @Column(name = "PRODUCT_PACKAGE_ID", nullable = false, precision = 10)
    private Long productPackageId;

    @Column(name = "PRODUCT_OFFER_TYPE_ID", nullable = false, precision = 10)
    private Long productOfferTypeId;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "UPDATE_STOCK", length = 1)
    private String updateStock;

    @Column(name = "CHECK_STAFF_STOCK", length = 1)
    private String checkStaffStock;

    @Column(name = "CHECK_SHOP_STOCK", length = 1)
    private String checkShopStock;

    @Temporal(TemporalType.DATE)
    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "LIMIT_GOODS", precision = 10)
    private Long limitGoods;

    @Column(name = "TRANSFER_IM", length = 1)
    private String transferIm;
}