package com.viettel.bccs.productcatalog.productspecchar.mapper;

import com.viettel.bccs.productcatalog.productspecchar.dto.response.ProductSpecCharResponse;
import com.viettel.bccs.productcatalog.productspecchar.entity.ProductSpecCharEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductSpecCharMapper {

    public ProductSpecCharResponse toResponse(ProductSpecCharEntity entity) {
        return new ProductSpecCharResponse(
                entity.getProductSpecCharId(),
                entity.getName(),
                entity.getDescription(),
                entity.getValueType(),
                entity.getCharType(),
                entity.getMinCardinality(),
                entity.getMaxCardinality(),
                entity.getStatus(),
                entity.getCreateUser(),
                entity.getCreateDatetime(),
                entity.getUpdateUser(),
                entity.getUpdateDatetime(),
                entity.getCode(),
                entity.getProductSpecCharTypeId(),
                entity.getValueSetType(),
                entity.getResponseClass(),
                entity.getSqlQuery(),
                entity.getDisplayObject(),
                entity.getValueObject(),
                entity.getSolrQuery(),
                entity.getSolrCore(),
                entity.getSolrSchema(),
                entity.getDataType(),
                entity.getWsWsdl(),
                entity.getTemplateRequest(),
                entity.getValidatePattern(),
                entity.getExtData(),
                entity.getNote());
    }
}