package com.viettel.bccs.policy.mapskipdebtcharges.entity;

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
@Table(name = "MAP_SKIP_DEBT_CHARGES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MapSkipDebtChargesEntity {

    @Id
    @Column(name = "ID", precision = 10)
    private Long id;

    @Column(name = "TEL_SERVICE_ID", precision = 10)
    private Long telServiceId;

    @Column(name = "PRODUCT_CODE", length = 100)
    private String productCode;

    @Column(name = "PRODUCT_NAME", length = 100)
    private String productName;

    @Column(name = "REG_REASON_ID")
    private Long regReasonId;

    @Column(name = "REASON_NAME", length = 100)
    private String reasonName;

    @Column(name = "CHANNEL_TYPE_ID")
    private Long channelTypeId;

    @Column(name = "CHANNEL_NAME", length = 100)
    private String channelName;

    @Column(name = "PROVINCE_CODE", length = 100)
    private String provinceCode;

    @Column(name = "PROVINCE_NAME", length = 100)
    private String provinceName;

    @Column(name = "DISTRICT_CODE", length = 100)
    private String districtCode;

    @Column(name = "DISTRICT_NAME", length = 100)
    private String districtName;

    @Column(name = "PRECINCT_CODE", length = 100)
    private String precinctCode;

    @Column(name = "PRECINCT_NAME", length = 100)
    private String precinctName;

    @Column(name = "SHOP_CODE", length = 100)
    private String shopCode;

    @Column(name = "STAFF_CODE", length = 100)
    private String staffCode;

    @Column(name = "ACTION_CODE", length = 100)
    private String actionCode;

    @Column(name = "ACTION_NAME", length = 100)
    private String actionName;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "CREATE_USER", length = 100)
    private String createUser;

    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Column(name = "UPDATE_USER", length = 100)
    private String updateUser;

    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "CYCLE", precision = 10)
    private Long cycle;

    @Column(name = "SKIP_HOT_CHARGES", length = 2)
    private String skipHotCharges;

    @Column(name = "PAY_TYPE", length = 2)
    private String payType;

    @Column(name = "EFFECT_DATE")
    private Date effectDate;

    @Column(name = "END_DATE")
    private Date endDate;

    @Column(name = "OFFER_ID")
    private Long offerId;

    @Column(name = "CUST_GROUP_ID", precision = 1)
    private Long custGroupId;

    @Column(name = "CUST_TYPE", length = 10)
    private String custType;

    @Column(name = "CUST_ID_NO", length = 50)
    private String custIdNo;

    @Column(name = "CUST_ACCOUNT_NO", length = 100)
    private String custAccountNo;

    @Column(name = "CUST_CODE", length = 100)
    private String custCode;

    @Column(name = "ACT_STATUS", length = 3)
    private String actStatus;

    @Column(name = "SKIP_LAST_SUB", length = 2)
    private String skipLastSub;

    @Column(name = "SKIP_CONTRACT", length = 2)
    private String skipContract;
}
