package com.viettel.bccs.productcatalog.productoffercharuse.mapper;

import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharValueDTO;
import com.viettel.bccs.productcatalog.productspecchar.entity.ProductSpecCharEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductSpecCharUseMapper {

    public ProductSpecCharDTO toDto(ProductSpecCharEntity entity) {
        if (entity == null) return null;
        return ProductSpecCharDTO.builder()
                .productSpecCharId(entity.getProductSpecCharId())
                .name(entity.getName())
                .description(entity.getDescription())
                .valueType(entity.getValueType())
                .charType(entity.getCharType())
                .minCardinality(entity.getMinCardinality())
                .maxCardinality(entity.getMaxCardinality())
                .status(entity.getStatus())
                .code(entity.getCode())
                .productSpecCharTypeId(entity.getProductSpecCharTypeId())
                .valueSetType(entity.getValueSetType())
                .responseClass(entity.getResponseClass())
                .sqlQuery(entity.getSqlQuery())
                .displayObject(entity.getDisplayObject())
                .valueObject(entity.getValueObject())
                .solrQuery(entity.getSolrQuery())
                .solrCore(entity.getSolrCore())
                .solrSchema(entity.getSolrSchema())
                .dataType(entity.getDataType())
                .wsWsdl(entity.getWsWsdl())
                .templateRequest(entity.getTemplateRequest())
                .validatePattern(entity.getValidatePattern())
                .extData(entity.getExtData())
                .note(entity.getNote())
                .build();
    }

    public ProductSpecCharDTO toDtoWithValue(ProductSpecCharEntity entity, ProductSpecCharValueDTO valueDto, String valueName, Long productOfferingId, Long offerCharUseId, String offerCharUseType) {
        if (entity == null) return null;
        ProductSpecCharDTO dto = toDto(entity);
        dto.setProductSpecCharValueDTO(valueDto);
        dto.setValueName(valueName);
        dto.setProductOfferingId(productOfferingId);
        dto.setOfferCharUseId(offerCharUseId);
        dto.setOfferCharUseType(offerCharUseType);
        return dto;
    }
}