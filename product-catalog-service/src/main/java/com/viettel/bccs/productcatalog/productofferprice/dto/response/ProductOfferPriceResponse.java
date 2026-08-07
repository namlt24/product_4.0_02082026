package com.viettel.bccs.productcatalog.productofferprice.dto.response;

import java.math.BigDecimal;
import java.util.Date;

public record ProductOfferPriceResponse(
        Long productOfferPriceId,
        Long productOfferingId,
        Long pricePolicyId,
        Long priceTypeId,
        String name,
        String description,
        BigDecimal price,
        BigDecimal vat,
        BigDecimal pledgeAmount,
        Long pledgeTime,
        BigDecimal priorPay,
        String status,
        Date effectDatetime,
        Date expireDatetime,
        Long priority,
        String effectType,
        String cronExpression,
        String createUser,
        Date createDatetime,
        String updateUser,
        Date updateDatetime,
        String programCode,
        Long programMonth,
        String isSelectAllShop,
        Long limited,
        String productOfferName,
        Long priceEquipment,
        Long priceEquipmentId,
        Long priceEquipmentTypeId
) {
}