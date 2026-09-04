package com.viettel.bccs.productcatalog.productofferprice.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.viettel.bccs.productcatalog.productofferprice.dto.response.PledgePriceResponse;
import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceDTO;
import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceResponse;
import com.viettel.bccs.productcatalog.productofferprice.entity.ProductOfferPriceEntity;

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
     * Chuyển ProductOfferPriceDTO (kết quả getPriceInServicesForPccc, nhánh PCCC) sang
     * ProductOfferPriceResponse (kết quả getPriceInServices, nhánh thường) để 2 nhánh dùng chung
     * 1 shape khi gộp kết quả cho API getListStockTypeWS. priceEquipmentId/priceEquipmentTypeId
     * không có ở nhánh PCCC (chỉ ghi đè priceEquipment) nên luôn null.
     */
    public ProductOfferPriceResponse toResponse(ProductOfferPriceDTO dto) {
        if (dto == null) {
            return null;
        }
        return new ProductOfferPriceResponse(
                dto.getProductOfferPriceId(),
                dto.getProductOfferingId(),
                dto.getPricePolicyId(),
                dto.getPriceTypeId(),
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getVat(),
                dto.getPledgeAmount(),
                dto.getPledgeTime(),
                dto.getPriorPay(),
                dto.getStatus(),
                dto.getEffectDatetime(),
                dto.getExpireDatetime(),
                dto.getPriority(),
                dto.getEffectType(),
                dto.getCronExpression(),
                dto.getCreateUser(),
                dto.getCreateDatetime(),
                dto.getUpdateUser(),
                dto.getUpdateDatetime(),
                dto.getProgramCode(),
                dto.getProgramMonth(),
                dto.getIsSelectAllShop(),
                dto.getLimited(),
                dto.getProductOfferName(),
                dto.getPriceEquipment(),
                null,
                null
        );
    }

    public List<ProductOfferPriceResponse> toResponseFromDto(List<ProductOfferPriceDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream().map(this::toResponse).toList();
    }

    /**
     * ProductOfferPriceResponse là record (immutable) — dùng method này để tạo bản sao mới của
     * {@code base} với productOfferName/priceEquipment/priceEquipmentId/priceEquipmentTypeId được
     * cập nhật, giữ nguyên toàn bộ field còn lại. Dùng trong getPriceInServices khi cần "set" giá
     * thiết bị lên từng dòng kết quả (thay cho việc mutate trực tiếp như legacy).
     */
    public ProductOfferPriceResponse withPriceInfo(ProductOfferPriceResponse base, String productOfferName,
                                                    Long priceEquipment, Long priceEquipmentId,
                                                    Long priceEquipmentTypeId) {
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
    public PledgePriceResponse toPledgePriceResponse(ProductOfferPriceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new PledgePriceResponse(entity.getPrice(), entity.getPledgeAmount(), entity.getPledgeTime(),
                entity.getPriorPay());
    }

    public List<PledgePriceResponse> toPledgePriceResponseList(List<ProductOfferPriceEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(this::toPledgePriceResponse).toList();
    }

    public ProductOfferPriceResponse emptyWithPriceEquipment(Long priceEquipment) {
        return new ProductOfferPriceResponse(
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null,
                priceEquipment, null, null
        );
    }
}