package com.viettel.bccs.policy.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StandardClientResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("data")
    private Object data;

    @JsonProperty("message")
    private String message;

    @JsonProperty("code")
    private String code;
}