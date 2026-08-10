package com.viettel.bccs.productcatalog.packageoffer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "PACKAGE_OFFER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageOfferEntity {

    @Id
    @Column(name = "PROD_PACK_OFFER_ID", precision = 10)
    private Long prodPackOfferId;

    @Column(name = "PRODUCT_OFFERING_ID", precision = 10)
    private Long productOfferingId;

    @Column(name = "PROD_PACK_TYPE_ID", precision = 10)
    private Long prodPackTypeId;

    @Column(name = "PRODUCT_OFFER_PRICE_ID", precision = 10)
    private Long productOfferPriceId;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "SUPPLY_METHOD", length = 50)
    private String supplyMethod;

    @Column(name = "IS_MANDATORY", length = 1)
    private String isMandatory;

    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "NUM_OFFER", precision = 10)
    private Long numOffer;

    @Column(name = "DESCRIPTION", length = 512)
    private String description;

    @Column(name = "NEW_OR_SOLD", length = 1)
    private String newOrSold;

    @Column(name = "EFFECT_DATETIME")
    private Date effectDatetime;

    @Column(name = "EXPIRE_DATETIME")
    private Date expireDatetime;

    @Column(name = "SHOW_OR_HIDE", length = 1)
    private String showOrHide;

    @Column(name = "SAP_MATERIAL_NUMBER", precision = 10)
    private Long sapMaterialNumber;

    // Các field dưới đây KHÔNG phải cột của bảng PACKAGE_OFFER -- được join thêm từ PRODUCT_OFFER_PRICE
    // + PRODUCT_OFFERING trong PackageOfferRepositoryCustomImpl (native query + Tuple thủ công, không
    // phải entityManager.find), nên gán @Column ở đây không có tác dụng persist/load tự động, chỉ dùng
    // làm carrier để mapper chuyển sang PackageOfferDTO.
    @Transient
    private String offerCode;

    @Transient
    private String offerName;

    @Transient
    private BigDecimal price;

    @Transient
    private BigDecimal vat;

    @Transient
    private String accountingModelCode;

    @Transient
    private String accountingModelName;
}