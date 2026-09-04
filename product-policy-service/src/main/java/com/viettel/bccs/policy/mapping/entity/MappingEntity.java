package com.viettel.bccs.policy.mapping.entity;

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
@Table(name = "MAPPING")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MappingEntity {

    @Id
    @Column(name = "ID", precision = 10)
    private Long id;

    @Column(name = "VAS", length = 10)
    private String vas;

    @Column(name = "VAS_NAME", length = 100)
    private String vasName;

    @Column(name = "PRODUCT_NAME", length = 100)
    private String productName;

    @Column(name = "PRODUCT_CODE", length = 15)
    private String productCode;

    @Column(name = "ACTION_NAME", length = 100)
    private String actionName;

    @Column(name = "ACTION_CODE", length = 30)
    private String actionCode;

    @Column(name = "REASON_ID", precision = 10)
    private Long reasonId;

    @Column(name = "REASON_NAME", length = 100)
    private String reasonName;

    @Column(name = "TEL_SERVICE_ID", precision = 10)
    private Long telServiceId;

    @Column(name = "SALE_SERVICE_ID", precision = 10)
    private Long saleServiceId;

    @Column(name = "SALE_SERVICE_NAME", length = 100)
    private String saleServiceName;

    @Column(name = "SALE_SERVICE_CODE", length = 20)
    private String saleServiceCode;

    @Column(name = "CHANNEL", length = 1)
    private String channel;

    @Column(name = "STATUS", length = 1, nullable = false)
    private String status;

    @Column(name = "USER_CREATE", length = 30)
    private String userCreate;

    @Column(name = "USER_UPDATE", length = 30)
    private String userUpdate;

    @Temporal(TemporalType.DATE)
    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Temporal(TemporalType.DATE)
    @Column(name = "CHANGE_DATETIME")
    private Date changeDatetime;

    @Column(name = "IP", length = 50)
    private String ip;

    @Temporal(TemporalType.DATE)
    @Column(name = "END_EFFECT_DATE")
    private Date endEffectDate;

    @Column(name = "TYPE_MAPPING", length = 1)
    private String typeMapping;

    @Column(name = "ACTION_ID", length = 30)
    private String actionId;
}