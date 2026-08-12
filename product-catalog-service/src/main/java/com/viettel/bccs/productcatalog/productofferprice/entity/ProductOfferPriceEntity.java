package com.viettel.bccs.productcatalog.productofferprice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "PRODUCT_OFFER_PRICE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOfferPriceEntity {

    @Id
    @Column(name = "PRODUCT_OFFER_PRICE_ID", precision = 15)
    private Long productOfferPriceId;

    @Column(name = "PRODUCT_OFFERING_ID", precision = 10)
    private Long productOfferingId;

    @Column(name = "PRICE_POLICY_ID", precision = 10)
    private Long pricePolicyId;

    @Column(name = "PRICE_TYPE_ID", precision = 10)
    private Long priceTypeId;

    @Column(name = "NAME", length = 50)
    private String name;

    @Column(name = "DESCRIPTION", length = 2000)
    private String description;

    @Column(name = "PRICE", precision = 20)
    private BigDecimal price;

    @Column(name = "VAT", precision = 10)
    private BigDecimal vat;

    @Column(name = "PLEDGE_AMOUNT", precision = 10)
    private BigDecimal pledgeAmount;

    @Column(name = "PLEDGE_TIME", precision = 10)
    private Long pledgeTime;

    // PRIOR_PAY là VARCHAR2(10) trên DB thật (không phải NUMBER) — khai báo BigDecimal trước đây
    // gây ORA-17026 (Numeric overflow) khi đọc dữ liệu thật qua native query SELECT a.* (phát hiện
    // khi test API getListStockTypeWS). Sửa đúng theo kiểu cột thật.
    @Column(name = "PRIOR_PAY", length = 10)
    private String priorPay;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Temporal(TemporalType.DATE)
    @Column(name = "EFFECT_DATETIME")
    private Date effectDatetime;

    @Temporal(TemporalType.DATE)
    @Column(name = "EXPIRE_DATETIME")
    private Date expireDatetime;

    @Column(name = "PRIORITY", precision = 2)
    private Long priority;

    @Column(name = "EFFECT_TYPE", length = 1)
    private String effectType;

    @Column(name = "CRON_EXPRESSION", length = 50)
    private String cronExpression;

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

    @Column(name = "PROGRAM_CODE", length = 50)
    private String programCode;

    @Column(name = "PROGRAM_MONTH", precision = 10)
    private Long programMonth;

    @Column(name = "IS_SELECT_ALL_SHOP", length = 10)
    private String isSelectAllShop;

    @Column(name = "LIMITED", precision = 1)
    private Long limited;
}