package com.viettel.bccs.policy.reasonpause.dto.response;

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
public class ReasonPauseDTO {
    private Long reasonPauseId;
    private Long numMonth;
    private Long price;
    private Long reasonId;
    private String status;
    private Date createDatetime;
    private String createUser;
    private Date updateDatetime;
    private String updateUser;
}
