package com.viettel.bccs.productcatalog.packageoffer.mapper;

import com.viettel.bccs.productcatalog.productpackage.dto.response.PackageOfferDTO;
import com.viettel.bccs.productcatalog.packageoffer.entity.PackageOfferEntity;
import org.springframework.stereotype.Component;

@Component
public class PackageOfferMapper {

    public PackageOfferDTO toDto(PackageOfferEntity entity) {
        if (entity == null) {
            return null;
        }
        return PackageOfferDTO.builder()
                .prodPackOfferId(entity.getProdPackOfferId())
                .productOfferingId(entity.getProductOfferingId())
                .prodPackTypeId(entity.getProdPackTypeId())
                .productOfferPriceId(entity.getProductOfferPriceId())
                .status(entity.getStatus())
                .supplyMethod(entity.getSupplyMethod())
                .isMandatory(entity.getIsMandatory())
                .createDatetime(entity.getCreateDatetime())
                .createUser(entity.getCreateUser())
                .updateUser(entity.getUpdateUser())
                .updateDatetime(entity.getUpdateDatetime())
                .numOffer(entity.getNumOffer())
                .description(entity.getDescription())
                .newOrSold(entity.getNewOrSold())
                .effectDatetime(entity.getEffectDatetime())
                .expireDatetime(entity.getExpireDatetime())
                .showOrHide(entity.getShowOrHide())
                .sapMaterialNumber(entity.getSapMaterialNumber())
                .offerCode(entity.getOfferCode())
                .offerName(entity.getOfferName())
                .price(entity.getPrice())
                .vat(entity.getVat())
                .accountingModelCode(entity.getAccountingModelCode())
                .accountingModelName(entity.getAccountingModelName())
                .priceTypeId(entity.getPriceTypeId())
                .build();
    }
}