package com.viettel.bccs.policy.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionSetValueResponse {

    @JsonProperty("optionSetValueId")
    private Long optionSetValueId;

    @JsonProperty("optionSetId")
    private Long optionSetId;

    @JsonProperty("optionSetCode")
    private String optionSetCode;

    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private String value;

    @JsonProperty("status")
    private String status;

    @JsonProperty("description")
    private String description;

    @JsonProperty("createUser")
    private String createUser;

    @JsonProperty("createDatetime")
    private Date createDatetime;

    @JsonProperty("updateUser")
    private String updateUser;

    @JsonProperty("updateDatetime")
    private Date updateDatetime;

    @JsonProperty("parentId")
    private Long parentId;
}