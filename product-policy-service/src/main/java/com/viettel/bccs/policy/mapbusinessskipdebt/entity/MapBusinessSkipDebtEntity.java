package com.viettel.bccs.policy.mapbusinessskipdebt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "MAP_BUSINESS_SKIP_DEBT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MapBusinessSkipDebtEntity {

    @Id
    @Column(name = "MAP_ID", precision = 10)
    private Long mapId;

    @Column(name = "ACTION_CODE", length = 10)
    private String actionCode;

    @Column(name = "TELECOM_SERVICE_ID", precision = 10)
    private Long telecomServiceId;

    @Column(name = "PRODUCT_CODE", length = 200)
    private String productCode;

    @Column(name = "EFFECT_DATETIME")
    private Date effectDatetime;

    @Column(name = "EXPIRE_DATETIME")
    private Date expireDatetime;

    @Column(name = "SHOP_ID", precision = 10)
    private Long shopId;

    @Column(name = "STAFF_ID", precision = 10)
    private Long staffId;

    @Column(name = "BUSINESS_NO", length = 2000)
    private String businessNo;

    @Column(name = "CONTRACT_NO", length = 2000)
    private String contractNo;

    @Column(name = "STATUS", precision = 2)
    private Long status;

    @Column(name = "IBM_CODE", length = 200)
    private String ibmCode;

    @Column(name = "APPROVE_USER", length = 200)
    private String approveUser;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;
}