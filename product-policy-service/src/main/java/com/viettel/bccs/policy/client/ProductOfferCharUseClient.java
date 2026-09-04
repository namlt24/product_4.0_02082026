package com.viettel.bccs.policy.client;

import java.util.List;
import java.util.Map;

import com.viettel.bccs.policy.client.dto.ProductSpecCharDTO;

public interface ProductOfferCharUseClient {

    Map<Long, List<ProductSpecCharDTO>> getProductSpecCharByOfferingIds(List<String> offeringIds);
}