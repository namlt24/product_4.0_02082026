package com.viettel.bccs.productcatalog.client.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreeCamEquipmentDTO {

    private Long freeCamEquipmentId;

    private String actionCode;

    private Long reasonId;

    private String areaCode;

    private Long camInsideNumber;

    private Long camOutsideNumber;

    private String status;

    private Long camMaxNumber;

    private BigDecimal camInsidePrice;

    private BigDecimal camOutsidePrice;

    private String customerGroup;

    private String customerType;
}