package com.viettel.bccs.policy.common.dto;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrExprFilterRequest implements Serializable {

    private List<FilterRequest> lstFilterRequest;
}