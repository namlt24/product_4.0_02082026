package com.viettel.bccs.policy.freecamequipment.service;

import com.viettel.bccs.policy.freecamequipment.dto.response.FreeCamEquipmentResponse;
import com.viettel.bccs.policy.freecamequipment.mapper.FreeCamEquipmentMapper;
import com.viettel.bccs.policy.freecamequipment.repository.FreeCamEquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FreeCamEquipmentService {

    private final FreeCamEquipmentRepository repository;
    private final FreeCamEquipmentMapper mapper;

    /**
     * Migrate từ mono: ProductOfferPriceServiceImpl.getPriceInServices gọi
     * freeCamEquipmentService.checkReasonFreeCam(temp) (temp = productPackageId).
     */
    public List<FreeCamEquipmentResponse> checkReasonFreeCam(Long productPackageId) {
        return repository.checkReasonFreeCam(productPackageId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
