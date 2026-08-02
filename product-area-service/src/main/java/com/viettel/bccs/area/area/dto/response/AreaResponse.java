package com.viettel.bccs.area.area.dto.response;

import java.math.BigDecimal;
import java.util.Date;

public record AreaResponse(
    String areaCode,
    String parentCode,
    String areaGroup,
    String province,
    String district,
    String precinct,
    String streetBlock,
    String name,
    String fullName,
    String center,
    String pstnCode,
    String provinceCode,
    String status,
    String createUser,
    Date createDatetime,
    String updateUser,
    Date updateDatetime,
    BigDecimal regionId,
    String vtMapCode,
    BigDecimal square,
    BigDecimal population,
    BigDecimal households,
    String areaType,
    String vnCode,
    String vnName,
    String isNew
) {
}