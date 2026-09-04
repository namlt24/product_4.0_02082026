package com.viettel.bccs.organization.custchanneltype.entity;

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
@Table(name = "CUST_CHANNEL_TYPE_MAPPING")
@Getter
@Setter
public class CustChannelTypeMappingEntity {

    @Id
    @Column(name = "CUST_CHANNEL_TYPE_MAP_ID", nullable = false, precision = 10)
    private Long custChannelTypeMapId;

    @Column(name = "CUST_TYPE", length = 10)
    private String custType;

    @Column(name = "CHANNEL_TYPE_ID", nullable = false, precision = 10)
    private Long channelTypeId;

    @Column(name = "STATUS", nullable = false, length = 1)
    private String status;

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
}
