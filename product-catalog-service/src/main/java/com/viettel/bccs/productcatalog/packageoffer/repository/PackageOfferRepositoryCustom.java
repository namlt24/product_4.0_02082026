package com.viettel.bccs.productcatalog.packageoffer.repository;

import java.util.List;
import java.util.Map;

public interface PackageOfferRepositoryCustom {

    Map<Long, List<com.viettel.bccs.productcatalog.packageoffer.entity.PackageOfferEntity>> getPackageOfferByListProdPackTypeIds(List<Long> prodPackTypeIds);
}