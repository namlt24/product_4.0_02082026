package com.viettel.bccs.area.area.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "AREA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaEntity {

    @Id
    @Column(name = "AREA_CODE", length = 200, nullable = false)
    private String areaCode;

    @Column(name = "PARENT_CODE", length = 200)
    private String parentCode;

    @Column(name = "AREA_GROUP", length = 5)
    private String areaGroup;

    @Column(name = "PROVINCE", length = 50)
    private String province;

    @Column(name = "DISTRICT", length = 50)
    private String district;

    @Column(name = "PRECINCT", length = 50)
    private String precinct;

    @Column(name = "STREET_BLOCK", length = 50)
    private String streetBlock;

    @Column(name = "NAME", length = 100)
    private String name;

    @Column(name = "FULL_NAME", length = 400)
    private String fullName;

    @Column(name = "CENTER", length = 1)
    private String center;

    @Column(name = "PSTN_CODE", length = 20)
    private String pstnCode;

    @Column(name = "PROVINCE_CODE", length = 50)
    private String provinceCode;

    @Column(name = "STATUS", length = 1)
    private String status;

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

    @Column(name = "REGION_ID", precision = 20)
    private BigDecimal regionId;

    @Column(name = "VT_MAP_CODE", length = 20)
    private String vtMapCode;

    @Column(name = "SQUARE", precision = 20)
    private BigDecimal square;

    @Column(name = "POPULATION", precision = 20)
    private BigDecimal population;

    @Column(name = "HOUSEHOLDS", precision = 20)
    private BigDecimal households;

    @Column(name = "AREA_TYPE", length = 10)
    private String areaType;

    @Column(name = "VN_CODE", length = 50)
    private String vnCode;

    @Column(name = "VN_NAME", length = 100)
    private String vnName;

    @Column(name = "IS_NEW", length = 1)
    private String isNew;
}