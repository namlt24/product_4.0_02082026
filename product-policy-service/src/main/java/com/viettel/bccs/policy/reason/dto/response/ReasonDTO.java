package com.viettel.bccs.policy.reason.dto.response;

import com.viettel.bccs.policy.reasonpause.dto.response.ReasonPauseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReasonDTO {
    private Long reasonId;
    private List<ReasonCharUseDTO> lstCharUse;
    private List<ReasonPauseDTO> listReasonPause;
    private String reasonCode;
    private String reasonType;
    private String name;
    private String payType;
    private String telService;
    private String description;
    private String status;
    private String createUser;
    private Date createDatetime;
    private String updateUser;
    private Date updateDatetime;
    private Long limitNumberIsdn;
    private Long limitNumberUser;
    private String type;
    private Date effectDatetime;
    private Date expireDatetime;
    private Long priority;
    private String note;
}
