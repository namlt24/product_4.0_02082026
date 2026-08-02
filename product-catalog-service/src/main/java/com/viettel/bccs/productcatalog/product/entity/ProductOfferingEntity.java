package com.viettel.bccs.productcatalog.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "PRODUCT_OFFERING")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOfferingEntity {

    @Id
    @Column(name = "PRODUCT_OFFERING_ID", precision = 10)
    private Long productOfferingId;

    @Column(name = "PRODUCT_SPEC_ID", precision = 10)
    private Long productSpecId;

    @Column(name = "PRODUCT_OFFER_TYPE_ID", precision = 10)
    private Long productOfferTypeId;

    @Column(name = "NAME", length = 500)
    private String name;

    @Column(name = "CODE", length = 50)
    private String code;

    @Column(name = "SUB_TYPE", length = 1)
    private String subType;

    @Column(name = "TELECOM_SERVICE_ID", precision = 10)
    private Long telecomServiceId;

    @Column(name = "DESCRIPTION", length = 512)
    private String description;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Temporal(TemporalType.DATE)
    @Column(name = "EFFECT_DATETIME")
    private Date effectDatetime;

    @Temporal(TemporalType.DATE)
    @Column(name = "EXPIRE_DATETIME")
    private Date expireDatetime;

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

    @Column(name = "VERSION", length = 50)
    private String version;

    @Column(name = "CHECK_SERIAL", precision = 1)
    private BigDecimal checkSerial;

    @Column(name = "CHECK_DEPOSIT", precision = 1)
    private BigDecimal checkDeposit;

    @Column(name = "UNIT", length = 10)
    private String unit;

    @Column(name = "ACCOUNTING_MODEL_CODE", length = 50)
    private String accountingModelCode;

    @Column(name = "ACCOUNTING_MODEL_NAME", length = 100)
    private String accountingModelName;

    @Column(name = "ACCOUNTING_NAME", length = 100)
    private String accountingName;

    @Column(name = "ACCOUNTING_CODE", length = 50)
    private String accountingCode;

    @Column(name = "DEMO_DURATION", precision = 10)
    private Long demoDuration;

    @Column(name = "IS_DEMO", precision = 1)
    private BigDecimal isDemo;

    @Column(name = "DEVICE_TYPE", length = 20)
    private String deviceType;

    @Column(name = "TRANSCEIVER", precision = 1)
    private BigDecimal transceiver;

    @Column(name = "STOCK_MODEL_TYPE", precision = 1)
    private BigDecimal stockModelType;

    @Column(name = "OWNER_SHOP_ID", precision = 10)
    private Long ownerShopId;

    @Column(name = "RETURN_STOCK_WHEN_CANCELLED", length = 2)
    private String returnStockWhenCancelled;

    @Column(name = "RETURN_STOCK_WHEN_CANCELLED1", length = 2)
    private String returnStockWhenCancelled1;

    @Column(name = "SAP_MATERIAL_NUMBER", precision = 9)
    private BigDecimal sapMaterialNumber;

    @Column(name = "USAGE_ID", length = 100)
    private String usageId;

    @Column(name = "DISTRIBUTE", length = 1)
    private String distribute;

    @Column(name = "NUM_MONTH", precision = 10)
    private Long numMonth;

    @Column(name = "IS_BUNDLE", length = 10)
    private String isBundle;

    @Column(name = "SAP_PRODUCT_TYPE", length = 1)
    private String sapProductType;
}