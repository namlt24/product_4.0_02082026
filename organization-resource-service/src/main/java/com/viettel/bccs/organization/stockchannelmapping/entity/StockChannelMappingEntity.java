package com.viettel.bccs.organization.stockchannelmapping.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "STOCK_CHANNEL_MAPPING")
@Getter
@Setter
public class StockChannelMappingEntity {

    @Id
    @Column(name = "STOCK_CHANNEL_MAPPING_ID", nullable = false, precision = 10)
    private Long stockChannelMappingId;

    @Column(name = "TELECOM_SERVICE_ID", nullable = false, precision = 10)
    private Long telecomServiceId;

    @Column(name = "CHANNEL_TYPE_ID", nullable = false, precision = 10)
    private Long channelTypeId;

    @Column(name = "STOCK_SHOP_ID", nullable = false, precision = 10)
    private Long stockShopId;

    @Column(name = "SHOP_ID", nullable = false, precision = 10)
    private Long shopId;

    @Column(name = "STAFF_ID", nullable = false, precision = 10)
    private Long staffId;

    @Temporal(TemporalType.DATE)
    @Column(name = "EFFECT_DATE", nullable = false)
    private Date effectDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "EXPIRE_DATE")
    private Date expireDate;

    @Column(name = "STATUS", nullable = false, length = 1)
    private String status;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "CREATE_DATETIME", nullable = false)
    private Date createDatetime;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;
}
