package com.viettel.bccs.productcatalog.productoffercharuse.repository;

import java.util.List;
import java.util.Optional;

public interface ProductOfferCharUseRepositoryCustom {

    List<Object[]> findSpecCharsByOfferingIds(List<String> offeringIds);

    Optional<String> findAttributeValueByOfferingIdAndCharCode(Long offeringId, String charCode);
}