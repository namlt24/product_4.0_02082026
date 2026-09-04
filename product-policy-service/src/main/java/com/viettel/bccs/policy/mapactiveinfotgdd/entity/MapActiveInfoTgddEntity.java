package com.viettel.bccs.policy.mapactiveinfotgdd.entity;

import java.sql.Blob;
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
@Table(name = "MAP_ACTIVE_INFO_TGDD")
@Getter
@Setter
public class MapActiveInfoTgddEntity {

    @Id
    @Column(name = "ID", precision = 10)
    private Long id;

    @Column(name = "TEL_SERVICE_ID", precision = 10)
    private Long telServiceId;

    @Column(name = "PRODUCT_CODE", length = 30)
    private String productCode;

    @Column(name = "PRODUCT_NAME", length = 100)
    private String productName;

    @Column(name = "REG_REASON_ID", precision = 10)
    private Long regReasonId;

    @Column(name = "REASON_NAME", length = 100)
    private String reasonName;

    @Column(name = "PROM_CODE", length = 10)
    private String promCode;

    @Column(name = "PROM_NAME", length = 150)
    private String promName;

    @Column(name = "CHANNEL_TYPE_ID", precision = 10)
    private Long channelTypeId;

    @Column(name = "CHANNEL_NAME", length = 50)
    private String channelName;

    @Column(name = "PROVINCE_CODE", length = 50)
    private String provinceCode;

    @Column(name = "DISTRICT_CODE", length = 50)
    private String districtCode;

    @Temporal(TemporalType.DATE)
    @Column(name = "EFFECT_DATE")
    private Date effectDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "END_DATE")
    private Date endDate;

    @Column(name = "PROVINCE_NAME", length = 50)
    private String provinceName;

    @Column(name = "DISTRICT_NAME", length = 50)
    private String districtName;

    @Column(name = "OFFER_ID", precision = 10)
    private Long offerId;

    @Column(name = "OFFER_NAME", length = 100)
    private String offerName;

    @Column(name = "STATUS", precision = 2)
    private Long status;

    @Column(name = "PRECINCT_NAME", length = 50)
    private String precinctName;

    @Column(name = "PRECINCT_CODE", length = 50)
    private String precinctCode;

    @Column(name = "SHOP_CODE", length = 20)
    private String shopCode;

    @Column(name = "STAFF_CODE", length = 50)
    private String staffCode;

    @Column(name = "ACTION_CODE", length = 10)
    private String actionCode;

    @Column(name = "ACTION_NAME", length = 100)
    private String actionName;

    @Column(name = "LIMIT_NUMBER", length = 10)
    private String limitNumber;

    @Column(name = "CAPTCHAR_REQUIRE", precision = 1)
    private Long captcharRequire;

    @Column(name = "UNIT", length = 2)
    private String unit;

    @Column(name = "CUSTOMER_GROUP", length = 10)
    private String customerGroup;

    @Column(name = "CUSTOMER_TYPE", length = 10)
    private String customerType;

    @Column(name = "SUB_TYPE", length = 4)
    private String subType;

    @Column(name = "SUB_GROUP", length = 10)
    private String subGroup;

    @Column(name = "POLICY_DOC", length = 100)
    private String policyDoc;

    @Column(name = "ACTION_GROUP", length = 50)
    private String actionGroup;

    @Column(name = "ACTION_GROUP_NAME", length = 200)
    private String actionGroupName;

    @Column(name = "FILE_NAME", length = 200)
    private String fileName;

    @Column(name = "STATION_ID", precision = 10)
    private Long stationId;

    @Column(name = "SHOP_ID", precision = 10)
    private Long shopId;

    @Column(name = "FILE_ATTACH")
    private Blob fileAttach;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "ISSUE_DATETIME")
    private Date issueDatetime;

    @Column(name = "STATION_CODES", length = 1000)
    private String stationCodes;

    @Column(name = "PAY_TYPE", length = 1)
    private String payType;

    @Column(name = "TECHNOLOGY", length = 10)
    private String technology;

    @Temporal(TemporalType.DATE)
    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Column(name = "AREA_GROUP_CODE", length = 50)
    private String areaGroupCode;

    @Column(name = "VAS_CODE", length = 50)
    private String vasCode;

    @Column(name = "VAS_NAME", length = 100)
    private String vasName;

    @Column(name = "NODE_CODE", length = 1500)
    private String nodeCode;

    @Column(name = "NOTE", length = 3000)
    private String note;

    @Column(name = "GROUP_NODE_CODE", length = 50)
    private String groupNodeCode;

    @Column(name = "CONNECT_METHOD", precision = 1)
    private Long connectMethod;

    @Column(name = "ATTACH_TEL_SERVICE_ID", precision = 10)
    private Long attachTelServiceId;

    @Column(name = "ATTACH_PRODUCT_CODE", length = 100)
    private String attachProductCode;

    @Column(name = "SINGLE_OR_COMBO", precision = 1)
    private Long singleOrCombo;

    @Column(name = "OLD_PRODUCT_CODE", length = 30)
    private String oldProductCode;

    @Column(name = "ATTACH_PROM_CODE", length = 30)
    private String attachPromCode;

    @Column(name = "ATTACH_REASON_ID", precision = 10)
    private Long attachReasonId;

    @Column(name = "SUB_GROUP_CODE", length = 4000)
    private String subGroupCode;

    @Column(name = "PROJECT_CODE", length = 4000)
    private String projectCode;

    @Column(name = "IMPORT_OFFLINE_ID", precision = 10)
    private Long importOfflineId;

    // Getters
}