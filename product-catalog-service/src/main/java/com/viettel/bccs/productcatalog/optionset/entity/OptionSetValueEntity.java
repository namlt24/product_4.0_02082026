package com.viettel.bccs.productcatalog.optionset.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "OPTION_SET_VALUE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionSetValueEntity {

    @Id
    @Column(name = "OPTION_SET_VALUE_ID", precision = 10)
    private Long optionSetValueId;

    @Column(name = "OPTION_SET_ID", precision = 10)
    private Long optionSetId;

    @Column(name = "NAME", length = 512)
    private String name;

    @Column(name = "VALUE", length = 1000)
    private String value;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "DESCRIPTION", length = 512)
    private String description;

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

    @Column(name = "PARENT_ID", precision = 10)
    private Long parentId;
}