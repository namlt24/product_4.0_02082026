package com.viettel.bccs.productcatalog.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOfferOutputDTO implements Serializable {

    private Long productOfferId;
    private String name;
    private String code;
    private String status;
}