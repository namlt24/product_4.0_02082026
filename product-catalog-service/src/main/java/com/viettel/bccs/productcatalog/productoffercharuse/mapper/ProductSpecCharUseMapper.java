package com.viettel.bccs.productcatalog.productoffercharuse.mapper;

import org.springframework.stereotype.Component;

import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharValueDTO;
import com.viettel.bccs.productcatalog.productspecchar.entity.ProductSpecCharEntity;

@Component("productOfferSpecCharUseMapper")
public class ProductSpecCharUseMapper {

    public ProductSpecCharDTO toDto(ProductSpecCharEntity entity) {
        if (entity == null) {
            return null;
        }
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

    public ProductSpecCharDTO toDtoWithValue(ProductSpecCharEntity entity, ProductSpecCharValueDTO valueDto,
        String valueName, Long productOfferingId, Long offerCharUseId, String offerCharUseType) {
        if (entity == null) {
            return null;
        }
        ProductSpecCharDTO dto = toDto(entity);
        dto.setProductSpecCharValueDTO(valueDto);
        dto.setValueName(valueName);
        dto.setProductOfferingId(productOfferingId);
        dto.setOfferCharUseId(offerCharUseId);
        dto.setOfferCharUseType(offerCharUseType);
        return dto;
    }

    /**
     * Map 1 dong ket qua tho (Object[]) tu native query
     * {@code ProductOfferCharUseRepositoryCustom.findSpecCharsByOfferingIds}/
     * {@code findProductOfferCharacter} sang {@link ProductSpecCharEntity} (offset cot 3-26).
     * Chuyen tu ProductOfferCharUseService sang day - dung dung tang trach nhiem cua Mapper.
     */
    public ProductSpecCharEntity buildCharEntity(Object[] row) {
        return ProductSpecCharEntity.builder()
                .productSpecCharId(row[3] != null ? ((Number) row[3]).longValue() : null)
                .name(str(row[4]))
                .description(str(row[5]))
                .valueType(str(row[6]))
                .charType(str(row[7]))
                .minCardinality(row[8] != null ? ((Number) row[8]).longValue() : null)
                .maxCardinality(row[9] != null ? ((Number) row[9]).longValue() : null)
                .status(str(row[10]))
                .code(str(row[11]))
                .productSpecCharTypeId(str(row[12]))
                .valueSetType(row[13] != null ? ((Number) row[13]).longValue() : null)
                .responseClass(str(row[14]))
                .sqlQuery(str(row[15]))
                .displayObject(str(row[16]))
                .valueObject(str(row[17]))
                .solrQuery(str(row[18]))
                .solrCore(str(row[19]))
                .solrSchema(str(row[20]))
                .dataType(str(row[21]))
                .wsWsdl(str(row[22]))
                .templateRequest(str(row[23]))
                .validatePattern(str(row[24]))
                .extData(str(row[25]))
                .note(str(row[26]))
                .build();
    }

    /**
     * Map 1 dong ket qua tho (Object[]) tu native query
     * {@code ProductSpecCharRepositoryCustom.findByLstSpecCodeAndLstProductCode} sang
     * {@link ProductSpecCharEntity} (offset cot 0-23 - khac offset voi {@link #buildCharEntity},
     * do khac cau truoc SELECT cua 2 native query). Chuyen tu ProductOfferingService sang day.
     */
    public ProductSpecCharEntity buildSpecCharEntity(Object[] row) {
        return ProductSpecCharEntity.builder()
                .productSpecCharId(row[0] != null ? ((Number) row[0]).longValue() : null)
                .name(str(row[1]))
                .description(str(row[2]))
                .valueType(str(row[3]))
                .charType(str(row[4]))
                .minCardinality(row[5] != null ? ((Number) row[5]).longValue() : null)
                .maxCardinality(row[6] != null ? ((Number) row[6]).longValue() : null)
                .status(str(row[7]))
                .code(str(row[8]))
                .productSpecCharTypeId(str(row[9]))
                .valueSetType(row[10] != null ? ((Number) row[10]).longValue() : null)
                .responseClass(str(row[11]))
                .sqlQuery(str(row[12]))
                .displayObject(str(row[13]))
                .valueObject(str(row[14]))
                .solrQuery(str(row[15]))
                .solrCore(str(row[16]))
                .solrSchema(str(row[17]))
                .dataType(str(row[18]))
                .wsWsdl(str(row[19]))
                .templateRequest(str(row[20]))
                .validatePattern(str(row[21]))
                .extData(str(row[22]))
                .note(str(row[23]))
                .build();
    }

    private static String str(Object val) {
        return val != null ? val.toString() : null;
    }
}