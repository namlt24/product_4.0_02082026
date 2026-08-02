package com.viettel.bccs.policy.client;

import com.viettel.bccs.policy.client.dto.ProductOfferingDTO;

import java.util.List;

public interface ProductOfferingClient {

    List<ProductOfferingDTO> getListOfferAlterStatus(Long offerId, String changeChannel, boolean checkStatus);

    List<ProductOfferingDTO> findByTelecomSubTypeOfferTypeCheckProductStatus(Long telecomServiceId, String subType, Long offerTypeId, boolean getActiveProduct);

    List<ProductOfferingDTO> findByCodesAndProductOfferType(List<String> codes, Long productOfferTypeId);
}