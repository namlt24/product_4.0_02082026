package com.viettel.bccs.productcatalog.productofferprice.mapper;

import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceDTO;
import com.viettel.bccs.productcatalog.productofferprice.entity.ProductOfferPriceEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductOfferPriceMapper {

    public ProductOfferPriceDTO toDto(ProductOfferPriceEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProductOfferPriceDTO.builder()
                .productOfferPriceId(entity.getProductOfferPriceId())
                .productOfferingId(entity.getProductOfferingId())
                .pricePolicyId(entity.getPricePolicyId())
                .priceTypeId(entity.getPriceTypeId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .vat(entity.getVat())
                .pledgeAmount(entity.getPledgeAmount())
                .pledgeTime(entity.getPledgeTime())
                .priorPay(entity.getPriorPay())
                .status(entity.getStatus())
                .effectDatetime(entity.getEffectDatetime())
                .expireDatetime(entity.getExpireDatetime())
                .priority(entity.getPriority())
                .effectType(entity.getEffectType())
                .cronExpression(entity.getCronExpression())
                .createUser(entity.getCreateUser())
                .createDatetime(entity.getCreateDatetime())
                .updateUser(entity.getUpdateUser())
                .updateDatetime(entity.getUpdateDatetime())
                .programCode(entity.getProgramCode())
                .programMonth(entity.getProgramMonth())
                .isSelectAllShop(entity.getIsSelectAllShop())
                .limited(entity.getLimited())
                .build();
    }

    public List<ProductOfferPriceDTO> toDtoBean(List<ProductOfferPriceEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(this::toDto).toList();
    }
}