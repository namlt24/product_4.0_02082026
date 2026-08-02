package com.viettel.bccs.productcatalog.prodpackproductoffertype.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "PROD_PACK_PRODUCT_OFFER_TYPE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdPackProductOfferTypeEntity {

    @Id
    @Column(name = "PROD_PACK_TYPE_ID", precision = 10)
    private Long prodPackTypeId;

    @Column(name = "PRODUCT_PACKAGE_ID", precision = 10)
    private Long productPackageId;

    @Column(name = "PRODUCT_OFFER_TYPE_ID", precision = 10)
    private Long productOfferTypeId;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "UPDATE_STOCK", length = 1)
    private String updateStock;

    @Column(name = "CHECK_STAFF_STOCK", length = 1)
    private String checkStaffStock;

    @Column(name = "CHECK_SHOP_STOCK", length = 1)
    private String checkShopStock;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "REQUIRE", length = 1)
    private String require;

    @Column(name = "DESCRIPTION", length = 200)
    private String description;

    @Column(name = "LIMIT_GOODS", precision = 10)
    private Long limitGoods;

    @Column(name = "DISTRIBUTE", precision = 2)
    private Long distribute;

    @Column(name = "TRANSFER_IM", length = 1)
    private String transferIm;
}