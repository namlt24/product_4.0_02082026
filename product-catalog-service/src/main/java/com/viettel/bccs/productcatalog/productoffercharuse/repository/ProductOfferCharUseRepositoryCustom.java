package com.viettel.bccs.productcatalog.productoffercharuse.repository;

import java.util.List;
import java.util.Optional;

import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingCharacterFullDTO;

public interface ProductOfferCharUseRepositoryCustom {

    List<Object[]> findSpecCharsByOfferingIds(List<String> offeringIds);

    Optional<String> findAttributeValueByOfferingIdAndCharCode(Long offeringId, String charCode);

    List<Object[]> findCharsByOfferingIdAndCharType(Long offeringId, String charType);

    List<ProductOfferingCharacterFullDTO> getListPricePlanByOfferId(Long productOfferingId);

    List<Object[]> findProductOfferCharacter(Long productOfferingId);

}