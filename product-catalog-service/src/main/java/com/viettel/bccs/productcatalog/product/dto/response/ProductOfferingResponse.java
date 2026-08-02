package com.viettel.bccs.productcatalog.product.dto.response;

import java.math.BigDecimal;
import java.util.Date;

public record ProductOfferingResponse(
    Long productOfferingId,
    Long productSpecId,
    Long productOfferTypeId,
    String name,
    String code,
    String subType,
    Long telecomServiceId,
    String description,
    String status,
    Date effectDatetime,
    Date expireDatetime,
    String createUser,
    Date createDatetime,
    String updateUser,
    Date updateDatetime,
    String version,
    BigDecimal checkSerial,
    BigDecimal checkDeposit,
    String unit,
    String accountingModelCode,
    String accountingModelName,
    String accountingName,
    String accountingCode,
    Long demoDuration,
    BigDecimal isDemo,
    String deviceType,
    BigDecimal transceiver,
    BigDecimal stockModelType,
    Long ownerShopId,
    String returnStockWhenCancelled,
    String returnStockWhenCancelled1,
    BigDecimal sapMaterialNumber,
    String usageId,
    String distribute,
    Long numMonth,
    String isBundle,
    String sapProductType
) {
}