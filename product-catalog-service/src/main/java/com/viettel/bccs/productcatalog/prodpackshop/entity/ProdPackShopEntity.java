package com.viettel.bccs.productcatalog.prodpackshop.entity;

import java.util.Date;

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
@Table(name = "PROD_PACK_SHOP")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdPackShopEntity {

    @Id
    @Column(name = "PROD_PACK_SHOP_ID", precision = 10)
    private Long prodPackShopId;

    @Column(name = "SHOP_ID", precision = 10)
    private Long shopId;

    @Column(name = "PROD_PACK_TYPE_ID", precision = 10)
    private Long prodPackTypeId;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;
}