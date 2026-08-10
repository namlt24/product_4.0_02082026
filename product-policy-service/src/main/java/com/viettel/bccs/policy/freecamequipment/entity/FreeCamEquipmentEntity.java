package com.viettel.bccs.policy.freecamequipment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Migrate từ mono: bảng FREE_CAM_EQUIPMENT, dùng bởi
 * ProductOfferPriceServiceImpl.getPriceInServices (nhánh giá thiết bị CAM).
 */
@Entity
@Table(name = "FREE_CAM_EQUIPMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreeCamEquipmentEntity {

    @Id
    @Column(name = "FREE_CAM_EQUIPMENT_ID", precision = 18)
    private Long freeCamEquipmentId;

    @Column(name = "ACTION_CODE", length = 30)
    private String actionCode;

    @Column(name = "REASON_ID", precision = 10)
    private Long reasonId;

    @Column(name = "AREA_CODE", length = 25)
    private String areaCode;

    @Column(name = "STATUS", length = 1, nullable = false)
    private String status;

    @Column(name = "CAM_INSIDE_NUMBER", precision = 10)
    private Long camInsideNumber;

    @Column(name = "CAM_OUTSIDE_NUMBER", precision = 10)
    private Long camOutsideNumber;

    @Column(name = "CAM_MAX_NUMBER", precision = 10)
    private Long camMaxNumber;

    @Column(name = "CAM_INSIDE_PRICE", precision = 18, scale = 2)
    private BigDecimal camInsidePrice;

    @Column(name = "CAM_OUTSIDE_PRICE", precision = 18, scale = 2)
    private BigDecimal camOutsidePrice;

    @Temporal(TemporalType.DATE)
    @Column(name = "EFFECT_DATETIME")
    private Date effectDatetime;

    @Temporal(TemporalType.DATE)
    @Column(name = "EXPIRE_DATETIME")
    private Date expireDatetime;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Column(name = "DESCRIPTION", length = 512)
    private String description;

    @Column(name = "SHOP_CODE", length = 30)
    private String shopCode;

    @Column(name = "STAFF_CODE", length = 40)
    private String staffCode;

    @Temporal(TemporalType.DATE)
    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Temporal(TemporalType.DATE)
    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "CUSTOMER_GROUP", length = 50)
    private String customerGroup;

    @Column(name = "CUSTOMER_TYPE", length = 50)
    private String customerType;
}
