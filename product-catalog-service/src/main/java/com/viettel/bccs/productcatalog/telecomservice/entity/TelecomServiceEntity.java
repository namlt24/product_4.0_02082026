package com.viettel.bccs.productcatalog.telecomservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "TELECOM_SERVICE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelecomServiceEntity {

    @Id
    @Column(name = "TELECOM_SERVICE_ID", precision = 10)
    private Long telecomServiceId;

    @Column(name = "NAME", length = 200, nullable = false)
    private String name;

    @Column(name = "STATUS", length = 1, nullable = false)
    private String status;

    @Column(name = "DESCRIPTION", length = 512)
    private String description;

    @Column(name = "SERVICE_ALIAS", length = 3)
    private String serviceAlias;

    @Column(name = "CREATE_USER", length = 50, nullable = false)
    private String createUser;

    @Column(name = "CREATE_DATETIME", nullable = false)
    private Date createDatetime;

    @Column(name = "UPDATE_USER", length = 50, nullable = false)
    private String updateUser;

    @Column(name = "UPDATE_DATETIME", nullable = false)
    private Date updateDatetime;
}
