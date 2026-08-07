package com.viettel.bccs.productcatalog.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreeCamEquipmentDTO {

    @JsonProperty("FREE_CAM_EQUIPMENT_ID")
    private Long freeCamEquipmentId;

    @JsonProperty("ACTION_CODE")
    private String actionCode;

    @JsonProperty("REASON_ID")
    private Long reasonId;

    @JsonProperty("AREA_CODE")
    private String areaCode;

    @JsonProperty("CAM_INSIDE_NUMBER")
    private Long camInsideNumber;

    @JsonProperty("CAM_OUTSIDE_NUMBER")
    private Long camOutsideNumber;

    @JsonProperty("STATUS")
    private String status;

    @JsonProperty("CAM_MAX_NUMBER")
    private Long camMaxNumber;

    @JsonProperty("CAM_INSIDE_PRICE")
    private BigDecimal camInsidePrice;

    @JsonProperty("CAM_OUTSIDE_PRICE")
    private BigDecimal camOutsidePrice;

    @JsonProperty("CUSTOMER_GROUP")
    private String customerGroup;

    @JsonProperty("CUSTOMER_TYPE")
    private String customerType;
}