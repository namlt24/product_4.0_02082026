package com.viettel.bccs.productcatalog.prodpackproductoffertype.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.productcatalog.prodpackproductoffertype.dto.response.ProdPackProductOfferTypeDTO;
import com.viettel.bccs.productcatalog.prodpackproductoffertype.mapper.ProdPackProductOfferTypeMapper;
import com.viettel.bccs.productcatalog.prodpackproductoffertype.repository.ProdPackProductOfferTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProdPackProductOfferTypeService {

    private final ProdPackProductOfferTypeRepository repository;
    private final ProdPackProductOfferTypeMapper mapper;

    public List<ProdPackProductOfferTypeDTO> getByProductPackageIdAndStatus(Long productPackageId, String status) {
        return repository.getByProductPackageIdAndStatus(productPackageId, status);
    }

    public List<ProdPackProductOfferTypeDTO> getListByProductPackageIdAndOfferTypeIds(Long productPackageId,
        List<Long> offerTypeIds) {
        return repository.getListByProductPackageIdAndOfferTypeIds(productPackageId, offerTypeIds);
    }
}