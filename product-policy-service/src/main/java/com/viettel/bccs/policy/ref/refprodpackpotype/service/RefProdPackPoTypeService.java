package com.viettel.bccs.policy.ref.refprodpackpotype.service;

import com.viettel.bccs.policy.ref.refprodpackpotype.dto.RefProdPackPoTypeDTO;
import com.viettel.bccs.policy.ref.refprodpackpotype.mapper.RefProdPackPoTypeMapper;
import com.viettel.bccs.policy.ref.refprodpackpotype.repository.RefProdPackPoTypeRepository;
import com.viettel.bccs.policy.utils.Const;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefProdPackPoTypeService {

    private final RefProdPackPoTypeRepository refProdPackPoTypeRepository;
    private final RefProdPackPoTypeMapper refProdPackPoTypeMapper;

    @Transactional(readOnly = true)
    public List<RefProdPackPoTypeDTO> findAllActive() {
        log.info("Truy vấn tất cả REF_PROD_PACK_PO_TYPE đang hiệu lực");
        return refProdPackPoTypeRepository.findAllByStatus(Const.STATUS.ACTIVE)
                .stream()
                .map(refProdPackPoTypeMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RefProdPackPoTypeDTO> findByProductPackageId(Long productPackageId) {
        log.info("Truy vấn REF_PROD_PACK_PO_TYPE theo productPackageId: {}", productPackageId);
        return refProdPackPoTypeRepository.findAllByProductPackageIdAndStatus(productPackageId, Const.STATUS.ACTIVE)
                .stream()
                .map(refProdPackPoTypeMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RefProdPackPoTypeDTO> findByProductOfferTypeId(Long productOfferTypeId) {
        log.info("Truy vấn REF_PROD_PACK_PO_TYPE theo productOfferTypeId: {}", productOfferTypeId);
        return refProdPackPoTypeRepository.findAllByProductOfferTypeIdAndStatus(productOfferTypeId, Const.STATUS.ACTIVE)
                .stream()
                .map(refProdPackPoTypeMapper::toDTO)
                .toList();
    }
}