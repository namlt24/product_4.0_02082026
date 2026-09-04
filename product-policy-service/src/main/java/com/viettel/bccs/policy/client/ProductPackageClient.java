package com.viettel.bccs.policy.client;

import java.util.List;

public interface ProductPackageClient {

    List<String> getPackageCodesByProductOfferTypeCount(String excludeProdOfferType, Integer packageNumber);
}