package com.viettel.bccs.productcatalog.packageoffer.repository;

import java.util.List;
import java.util.Map;

import com.viettel.bccs.productcatalog.packageoffer.entity.PackageOfferEntity;

public interface PackageOfferRepositoryCustom {

    Map<Long, List<PackageOfferEntity>> getPackageOfferByListProdPackTypeIds(List<Long> prodPackTypeIds);
}