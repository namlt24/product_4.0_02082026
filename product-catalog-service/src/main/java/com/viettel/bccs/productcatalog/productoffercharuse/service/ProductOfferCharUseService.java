package com.viettel.bccs.productcatalog.productoffercharuse.service;

import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingCharacterFullDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharValueDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.mapper.ProductSpecCharUseMapper;
import com.viettel.bccs.productcatalog.productoffercharuse.mapper.ProductSpecCharValueUseMapper;
import com.viettel.bccs.productcatalog.productoffercharuse.repository.ProductOfferCharUseRepositoryCustom;
import com.viettel.bccs.productcatalog.productspecchar.entity.ProductSpecCharEntity;
import com.viettel.bccs.productcatalog.productspeccharvalue.entity.ProductSpecCharValueEntity;
import com.viettel.bccs.productcatalog.utils.Const;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import com.viettel.bccs.productcatalog.utils.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductOfferCharUseService {

    private final ProductOfferCharUseRepositoryCustom repository;
    private final ProductSpecCharUseMapper productSpecCharUseMapper;
    private final ProductSpecCharValueUseMapper productSpecCharValueUseMapper;

    @Cacheable(value = "productOfferCharUseCache", key = "'OFFERING_SPEC_CHAR_BATCH:' + T(String).join(',', #offeringIds.stream().sorted().toList())")
    public Map<Long, List<ProductSpecCharDTO>> getProductSpecCharByOfferingIds(List<String> offeringIds) {
        RequestValidator.requireNotEmpty(offeringIds, "offeringIds", "BCCS-PRODUCT-VALIDATE-0000");
        if (DataUtil.isNullOrEmpty(offeringIds)) {
            return Collections.emptyMap();
        }

        Map<Long, List<ProductSpecCharDTO>> resultMap = new LinkedHashMap<>();

        List<String> validIds = offeringIds.stream()
                .filter(id -> {
                    try { return Long.parseLong(id) <= Integer.MAX_VALUE; }
                    catch (NumberFormatException e) { return false; }
                })
                .toList();

        if (!validIds.isEmpty()) {
            List<Object[]> results = repository.findSpecCharsByOfferingIds(validIds);
            if (!DataUtil.isNullOrEmpty(results)) {
                for (Object[] row : results) {
                    Long productOfferingId = ((Number) row[0]).longValue();
                    Long offerCharUseId = ((Number) row[1]).longValue();
                    String offerCharUseType = row[2] != null ? row[2].toString() : null;

                    ProductSpecCharEntity charEntity = productSpecCharUseMapper.buildCharEntity(row);
                    ProductSpecCharValueEntity valueEntity = productSpecCharValueUseMapper.buildValueEntity(row);
                    String valueName = row[37] != null ? row[37].toString() : null;

                    ProductSpecCharValueDTO valueDto = productSpecCharValueUseMapper.toDto(valueEntity);
                    ProductSpecCharDTO dto = productSpecCharUseMapper.toDtoWithValue(
                            charEntity, valueDto, valueName, productOfferingId, offerCharUseId, offerCharUseType);

                    resultMap.computeIfAbsent(productOfferingId, k -> new ArrayList<>()).add(dto);
                }
            }
        }

        return resultMap;
    }

    @Cacheable(value = "productOfferCharUseCache", key = "'ATTR_VALUE:' + #offerId + ':' + #attributeName")
    public Optional<String> getAttributeValue(Long offerId, String attributeName) {
        RequestValidator.requireNotNull(offerId, "offerId", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotBlank(attributeName, "attributeName", "BCCS-PRODUCT-VALIDATE-0000");
        if (offerId == null || DataUtil.isNullOrEmpty(attributeName)) {
            return Optional.empty();
        }
        return repository.findAttributeValueByOfferingIdAndCharCode(offerId, attributeName);
    }

    @Cacheable(value = "productOfferCharUseCache", key = "'OFFER_CHARACTER:' + #productOfferingId")
    public List<ProductOfferingCharacterFullDTO> getProductOfferCharacter(Long productOfferingId) {
        RequestValidator.requireNotNull(productOfferingId, "productOfferingId", "BCCS-PRODUCT-VALIDATE-0000");
        if (productOfferingId == null) {
            return null;
        }

        List<Object[]> rows = repository.findProductOfferCharacter(productOfferingId);
        if (DataUtil.isNullOrEmpty(rows)) {
            return null;
        }

        List<ProductOfferingCharacterFullDTO> resultList = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            ProductSpecCharValueEntity valueEntity = productSpecCharValueUseMapper.buildValueEntity(row);
            // value bắt buộc (INNER JOIN) — chỉ build khi tồn tại để tránh NPE ở mapper
            if (valueEntity == null) {
                continue;
            }
            ProductOfferingCharacterFullDTO dto = new ProductOfferingCharacterFullDTO();
            dto.setProductOfferingId(((Number) row[0]).longValue());
            dto.setProductCode(str(row[40]));
            dto.setProductSpecCharDTO(productSpecCharUseMapper.toDto(productSpecCharUseMapper.buildCharEntity(row)));
            dto.setProductSpecCharValueDTO(productSpecCharValueUseMapper.toDto(valueEntity));
            resultList.add(dto);
        }
        return resultList;
    }

    @Cacheable(value = "productOfferCharUseCache", key = "'PRICE_PLAN:' + #productOfferingId")
    public List<ProductOfferingCharacterFullDTO> getListPricePlanByOfferId(Long productOfferingId) {
        RequestValidator.requireNotNull(productOfferingId, "productOfferingId", "BCCS-PRODUCT-VALIDATE-0000");
        List<ProductOfferingCharacterFullDTO> resultList = repository.getListPricePlanByOfferId(productOfferingId);
        if (DataUtil.isNullOrEmpty(resultList)) {
            return null;
        } else {
            return resultList;
        }
    }

    private String str(Object val) {
        return val != null ? val.toString() : null;
    }
}