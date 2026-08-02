package com.viettel.bccs.productcatalog.prodpackproductoffertype.repository;

import com.viettel.bccs.productcatalog.prodpackproductoffertype.dto.response.ProdPackProductOfferTypeDTO;

import java.util.List;

public interface ProdPackProductOfferTypeRepositoryCustom {

    List<ProdPackProductOfferTypeDTO> getByProductPackageIdAndStatus(Long productPackageId, String status);

    List<ProdPackProductOfferTypeDTO> getListByProductPackageIdAndOfferTypeIds(Long productPackageId, List<Long> offerTypeIds);
}