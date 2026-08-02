package com.viettel.bccs.policy.client;

import com.viettel.bccs.policy.client.dto.ProductSpecCharDTO;

import java.util.List;
import java.util.Map;

public interface ProductOfferCharUseClient {

    Map<Long, List<ProductSpecCharDTO>> getProductSpecCharByOfferingIds(List<String> offeringIds);
}