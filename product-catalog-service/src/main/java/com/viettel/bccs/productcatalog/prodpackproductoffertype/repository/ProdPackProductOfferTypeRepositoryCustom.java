package com.viettel.bccs.productcatalog.prodpackproductoffertype.repository;

import java.util.List;

import com.viettel.bccs.productcatalog.prodpackproductoffertype.dto.response.ProdPackProductOfferTypeDTO;

public interface ProdPackProductOfferTypeRepositoryCustom {

    List<ProdPackProductOfferTypeDTO> getByProductPackageIdAndStatus(Long productPackageId, String status);

    List<ProdPackProductOfferTypeDTO> getListByProductPackageIdAndOfferTypeIds(Long productPackageId,
        List<Long> offerTypeIds);
}