package com.viettel.bccs.policy.mapactiveinfo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IsCheckMapActiveInfoRequest {

    private String productOfferType;

    private String actionCode;

    @NotNull(message = "telServiceId is required")
    private Long telServiceId;
}