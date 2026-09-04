package com.viettel.bccs.productcatalog.product.mapper;

import org.springframework.stereotype.Component;

import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingDTO;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingResponse;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingSumaryDTO;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingSummaryDTO;
import com.viettel.bccs.productcatalog.product.dto.response.StockOfferingRow;
import com.viettel.bccs.productcatalog.product.entity.ProductOfferingEntity;

@Component
public class ProductOfferingMapper {

    public ProductOfferingResponse toResponse(ProductOfferingEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ProductOfferingResponse(
            entity.getProductOfferingId(),
            entity.getProductSpecId(),
            entity.getProductOfferTypeId(),
            entity.getName(),
            entity.getCode(),
            entity.getSubType(),
            entity.getTelecomServiceId(),
            entity.getDescription(),
            entity.getStatus(),
            entity.getEffectDatetime(),
            entity.getExpireDatetime(),
            entity.getCreateUser(),
            entity.getCreateDatetime(),
            entity.getUpdateUser(),
            entity.getUpdateDatetime(),
            entity.getVersion(),
            entity.getCheckSerial(),
            entity.getCheckDeposit(),
            entity.getUnit(),
            entity.getAccountingModelCode(),
            entity.getAccountingModelName(),
            entity.getAccountingName(),
            entity.getAccountingCode(),
            entity.getDemoDuration(),
            entity.getIsDemo(),
            entity.getDeviceType(),
            entity.getTransceiver(),
            entity.getStockModelType(),
            entity.getOwnerShopId(),
            entity.getReturnStockWhenCancelled(),
            entity.getReturnStockWhenCancelled1(),
            entity.getSapMaterialNumber(),
            entity.getUsageId(),
            entity.getDistribute(),
            entity.getNumMonth(),
            entity.getIsBundle(),
            entity.getSapProductType()
        );
    }

    public ProductOfferingDTO toDto(ProductOfferingEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProductOfferingDTO.builder()
            .productOfferingId(entity.getProductOfferingId())
            .productSpecId(entity.getProductSpecId())
            .productOfferTypeId(entity.getProductOfferTypeId())
            .name(entity.getName())
            .code(entity.getCode())
            .subType(entity.getSubType())
            .telecomServiceId(entity.getTelecomServiceId())
            .description(entity.getDescription())
            .status(entity.getStatus())
            .effectDatetime(entity.getEffectDatetime())
            .expireDatetime(entity.getExpireDatetime())
            .createUser(entity.getCreateUser())
            .createDatetime(entity.getCreateDatetime())
            .updateUser(entity.getUpdateUser())
            .updateDatetime(entity.getUpdateDatetime())
            .version(entity.getVersion())
            .checkSerial(entity.getCheckSerial() != null ? entity.getCheckSerial().shortValue() : null)
            .checkDeposit(entity.getCheckDeposit() != null ? entity.getCheckDeposit().shortValue() : null)
            .unit(entity.getUnit())
            .accountingModelCode(entity.getAccountingModelCode())
            .accountingModelName(entity.getAccountingModelName())
            .accountingName(entity.getAccountingName())
            .accountingCode(entity.getAccountingCode())
            .demoDuration(entity.getDemoDuration())
            .isDemo(entity.getIsDemo() != null ? entity.getIsDemo().shortValue() : null)
            .deviceType(entity.getDeviceType())
            .transceiver(entity.getTransceiver() != null ? entity.getTransceiver().shortValue() : null)
            .stockModelType(entity.getStockModelType() != null ? entity.getStockModelType().shortValue() : null)
            .ownerShopId(entity.getOwnerShopId())
            .sapMaterialNumber(entity.getSapMaterialNumber())
            .usageId(entity.getUsageId())
            .isBundle(entity.getIsBundle())
            .sapProductType(entity.getSapProductType())
            .build();
    }

    public ProductOfferingSummaryDTO toSummary(ProductOfferingEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProductOfferingSummaryDTO.builder()
            .code(entity.getCode())
            .name(entity.getName())
            .productOfferingId(entity.getProductOfferingId())
            .build();
    }

    public ProductOfferingSumaryDTO toSumary(ProductOfferingEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProductOfferingSumaryDTO.builder()
            .code(entity.getCode())
            .name(entity.getName())
            .productOfferingId(entity.getProductOfferingId())
            .productOfferTypeId(entity.getProductOfferTypeId())
            .telecomServiceId(entity.getTelecomServiceId())
            .status(entity.getStatus())
            .build();
    }

    /**
     * Map 1 dong ket qua tho (Object[]) tu native query
     * {@code ProductOfferingRepositoryCustom.getListStockModelBySaleServiceCode} sang
     * {@link StockOfferingRow}. Chuyen tu ProductOfferingService sang day de dung dung tang
     * trach nhiem "Entity/row tho <-> DTO" theo quy uoc cua du an, khong nam o Service.
     */
    public StockOfferingRow toStockOfferingRow(Object[] row) {
        return new StockOfferingRow(
                toLong(row[0]),
                toLong(row[1]),
                row[2] != null ? row[2].toString() : null,
                row[3] != null ? ((Number) row[3]).shortValue() : null,
                toLong(row[4]),
                row[5] != null ? row[5].toString() : null,
                row[6] != null ? row[6].toString() : null,
                toLong(row[7])
        );
    }

    private static Long toLong(Object value) {
        return value != null ? ((Number) value).longValue() : null;
    }
}