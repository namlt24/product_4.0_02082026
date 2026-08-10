package com.viettel.bccs.productcatalog.productofferprice.mapper;

import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceDTO;
import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceResponse;
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

    public ProductOfferPriceResponse toResponse(ProductOfferPriceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ProductOfferPriceResponse(
                entity.getProductOfferPriceId(),
                entity.getProductOfferingId(),
                entity.getPricePolicyId(),
                entity.getPriceTypeId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getVat(),
                entity.getPledgeAmount(),
                entity.getPledgeTime(),
                entity.getPriorPay(),
                entity.getStatus(),
                entity.getEffectDatetime() != null ? entity.getEffectDatetime(): null,
                entity.getExpireDatetime() != null ? entity.getExpireDatetime(): null,
                entity.getPriority(),
                entity.getEffectType(),
                entity.getCronExpression(),
                entity.getCreateUser(),
                entity.getCreateDatetime(),
                entity.getUpdateUser(),
                entity.getUpdateDatetime(),
                entity.getProgramCode(),
                entity.getProgramMonth(),
                entity.getIsSelectAllShop(),
                entity.getLimited(),
                null,
                null,
                null,
                null
        );
    }


    public List<ProductOfferPriceResponse> toResponseList(List<ProductOfferPriceEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return new java.util.ArrayList<>(entities.stream().map(this::toResponse).toList());
    }

    /**
     * ProductOfferPriceResponse là record (immutable) — dùng method này để tạo bản sao mới của
     * {@code base} với productOfferName/priceEquipment/priceEquipmentId/priceEquipmentTypeId được
     * cập nhật, giữ nguyên toàn bộ field còn lại. Dùng trong getPriceInServices khi cần "set" giá
     * thiết bị lên từng dòng kết quả (thay cho việc mutate trực tiếp như legacy).
     */
    public ProductOfferPriceResponse withPriceInfo(ProductOfferPriceResponse base, String productOfferName,
                                                    Long priceEquipment, Long priceEquipmentId, Long priceEquipmentTypeId) {
        if (base == null) {
            return null;
        }
        return new ProductOfferPriceResponse(
                base.productOfferPriceId(),
                base.productOfferingId(),
                base.pricePolicyId(),
                base.priceTypeId(),
                base.name(),
                base.description(),
                base.price(),
                base.vat(),
                base.pledgeAmount(),
                base.pledgeTime(),
                base.priorPay(),
                base.status(),
                base.effectDatetime(),
                base.expireDatetime(),
                base.priority(),
                base.effectType(),
                base.cronExpression(),
                base.createUser(),
                base.createDatetime(),
                base.updateUser(),
                base.updateDatetime(),
                base.programCode(),
                base.programMonth(),
                base.isSelectAllShop(),
                base.limited(),
                productOfferName != null ? productOfferName : base.productOfferName(),
                priceEquipment,
                priceEquipmentId,
                priceEquipmentTypeId
        );
    }

    /**
     * Migrate từ mono ProductOfferPriceServiceImpl.getPriceInServices dòng 381-383:
     * khi lstResult rỗng nhưng vẫn tìm được giá thiết bị CAM, legacy thêm 1
     * ProductOfferPriceDTO rỗng chỉ để mang priceEquipment. Do record immutable,
     * tạo bản rỗng tương đương ở đây.
     */
    public ProductOfferPriceResponse emptyWithPriceEquipment(Long priceEquipment) {
        return new ProductOfferPriceResponse(
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null,
                priceEquipment, null, null
        );
    }
}