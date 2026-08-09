package com.viettel.bccs.policy.reasoncharuse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReasonCharUseDTO {
    private Long reasonCharUseId;
    private Long reasonId;
    private Long productSpecCharValueId;
    private Long productSpecCharId;
    private String createUser;
    private Date createDatetime;
    private String updateUser;
    private Date updateDatetime;
    private String status;
    private String specificValue;
    private Long limited;
    private Long limited2;
    private Long min;
    private Long max;
    private String code;
    private String value;
}
