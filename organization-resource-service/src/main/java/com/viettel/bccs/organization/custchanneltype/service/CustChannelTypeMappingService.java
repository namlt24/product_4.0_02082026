package com.viettel.bccs.organization.custchanneltype.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.organization.custchanneltype.dto.CustChannelTypeMappingDTO;
import com.viettel.bccs.organization.custchanneltype.entity.CustChannelTypeMappingEntity;
import com.viettel.bccs.organization.custchanneltype.mapper.CustChannelTypeMappingMapper;
import com.viettel.bccs.organization.custchanneltype.repository.CustChannelTypeMappingRepository;
import com.viettel.bccs.organization.utils.Const;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustChannelTypeMappingService {

    private final CustChannelTypeMappingRepository mappingRepository;
    private final CustChannelTypeMappingMapper mappingMapper;

    @Cacheable(value = "custChannelTypeMappingCache", key = "'ALL_ACTIVE'")
    @Transactional(readOnly = true)
    public List<CustChannelTypeMappingDTO> getAllActive() {
        log.info("Truy vấn tất cả mapping đang hiệu lực");
        return mappingRepository.findAllByStatus(Const.Status.ACTIVE)
                .stream()
                .map(mappingMapper::toDTO)
                .toList();
    }

    @Cacheable(value = "custChannelTypeMappingCache", key = "'CHANNEL_TYPE:' + #channelTypeId")
    @Transactional(readOnly = true)
    public List<CustChannelTypeMappingDTO> getByChannelTypeId(Long channelTypeId) {
        log.info("Truy vấn mapping theo loại kênh: {}", channelTypeId);
        return mappingRepository.findAllByChannelTypeIdAndStatus(channelTypeId, Const.Status.ACTIVE)
                .stream()
                .map(mappingMapper::toDTO)
                .toList();
    }

    @Cacheable(value = "custChannelTypeMappingCache", key = "'CUST_CHANNEL:' + #custType + ':' + #channelTypeId")
    @Transactional(readOnly = true)
    public CustChannelTypeMappingDTO getByCustTypeAndChannelType(String custType, Long channelTypeId) {
        log.info("Truy vấn mapping theo loại khách hàng {} và loại kênh {}", custType, channelTypeId);
        List<CustChannelTypeMappingEntity> results =
                mappingRepository.findByCustTypeAndChannelTypeIdAndStatus(custType, channelTypeId, Const.Status.ACTIVE);
        if (results.isEmpty()) {
            throw new BusinessException("BCCS-ORGANIZATION-CUSTCHANTYPE-0001",
                    "Không tìm thấy mapping cho loại khách hàng: " + custType + " và loại kênh: " + channelTypeId);
        }
        return mappingMapper.toDTO(results.get(0));
    }
}
