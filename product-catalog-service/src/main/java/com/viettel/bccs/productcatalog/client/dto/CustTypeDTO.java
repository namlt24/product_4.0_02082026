package com.viettel.bccs.productcatalog.client.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustTypeDTO {

    @JsonProperty("custType")
    private String custType;

    @JsonProperty("name")
    private String name;

    @JsonProperty("createUser")
    private String createUser;

    @JsonProperty("createDatetime")
    private Date createDatetime;

    @JsonProperty("updateUser")
    private String updateUser;

    @JsonProperty("updateDatetime")
    private Date updateDatetime;

    @JsonProperty("description")
    private String description;

    @JsonProperty("status")
    private String status;

    @JsonProperty("groupType")
    private String groupType;

    @JsonProperty("tax")
    private Long tax;

    @JsonProperty("plan")
    private String plan;

    @JsonProperty("representCust")
    private String representCust;

    @JsonProperty("custTypeId")
    private Long custTypeId;
}