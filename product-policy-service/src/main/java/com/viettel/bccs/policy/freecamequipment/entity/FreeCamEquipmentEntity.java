package com.viettel.bccs.policy.freecamequipment.entity;

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

/**
 * Migrate từ mono: bảng FREE_CAM_EQUIPMENT, dùng bởi
 * ProductOfferPriceServiceImpl.getPriceInServices (nhánh giá thiết bị CAM).
 * DDL theo đúng bản người dùng cung cấp (xem db-local/init/01_schema.sql).
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
    @Column(name = "FREE_CAM_EQUIPMENT_ID", precision = 10)
    private Long freeCamEquipmentId;

    @Column(name = "ACTION_CODE", length = 10)
    private String actionCode;

    @Column(name = "REASON_ID", precision = 10)
    private Long reasonId;

    @Column(name = "AREA_CODE", length = 200)
    private String areaCode;

    @Column(name = "CAM_INSIDE_NUMBER", precision = 10)
    private Long camInsideNumber;

    @Column(name = "CAM_OUTSIDE_NUMBER", precision = 10)
    private Long camOutsideNumber;

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

    @Column(name = "UPDATE_USER", length = 50)
    private String updateUser;

    @Temporal(TemporalType.DATE)
    @Column(name = "CREATE_DATETIME")
    private Date createDatetime;

    @Temporal(TemporalType.DATE)
    @Column(name = "UPDATE_DATETIME")
    private Date updateDatetime;

    @Column(name = "CAM_MAX_NUMBER", precision = 10)
    private Long camMaxNumber;

    @Column(name = "SHOP_CODE", length = 40)
    private String shopCode;

    @Column(name = "STAFF_CODE", length = 4000)
    private String staffCode;

    @Column(name = "DESCRIPTION", length = 4000)
    private String description;

    @Column(name = "CAM_INSIDE_PRICE", precision = 10)
    private Long camInsidePrice;

    @Column(name = "CAM_OUTSIDE_PRICE", precision = 10)
    private Long camOutsidePrice;

    @Column(name = "CUSTOMER_GROUP", length = 4000)
    private String customerGroup;

    @Column(name = "CUSTOMER_TYPE", length = 4000)
    private String customerType;
}
