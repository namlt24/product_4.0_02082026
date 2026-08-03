package com.viettel.bccs.policy.discountpromotion.dto.response;

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
@Builder(toBuilder = true)
public class DiscountPromotionDTO {
    private Long discountPromotionId;
    private String telecomServiceId;
    private String code;
    private String name;
    private String type;
    private String systemType;
    private String discountMethod;
    private String discountPolicy;
    private String subType;
    private Long monthCommitment;
    private String pricePlan;
    private Long monthAmount;
    private String status;
    private String description;
    private String content;
    private String areaCode;
    private Date effectDatetime;
    private Date expireDatetime;
    private String createUser;
    private Date createDatetime;
    private String updateUser;
    private Date updateDatetime;
    private Long cycle;
    private String listType;
    private Long subListId;
    private String note;

    private Long regReasonId;
    private String reasonName;
    private String subGroupCode;
    private String productCode;
}
