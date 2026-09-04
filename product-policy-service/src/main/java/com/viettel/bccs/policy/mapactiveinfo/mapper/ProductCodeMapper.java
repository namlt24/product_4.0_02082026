package com.viettel.bccs.policy.mapactiveinfo.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.viettel.bccs.policy.client.dto.ProductOfferingDTO;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.ProductCodeDTO;

@Component
public class ProductCodeMapper {

    public List<ProductCodeDTO> toProductCodeList(List<ProductOfferingDTO> sources) {
        if (sources == null) {
            return null;
        }
        return sources.stream().map(this::toProductCode).collect(Collectors.toList());
    }

    public ProductCodeDTO toProductCode(ProductOfferingDTO source) {
        if (source == null) {
            return null;
        }
        return ProductCodeDTO.builder()
                .code(source.getCode())
                .name(source.getName())
                .productOfferingId(source.getProductOfferingId())
                .productOfferTypeId(source.getProductOfferTypeId())
                .telecomServiceId(source.getTelecomServiceId())
                .subType(source.getSubType())
                .build();
    }
}
