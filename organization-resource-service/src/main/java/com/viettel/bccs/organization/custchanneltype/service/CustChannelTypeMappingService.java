package com.viettel.bccs.organization.custchanneltype.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.organization.custchanneltype.dto.CustChannelTypeMappingDTO;
import com.viettel.bccs.organization.custchanneltype.entity.CustChannelTypeMappingEntity;
import com.viettel.bccs.organization.custchanneltype.mapper.CustChannelTypeMappingMapper;
import com.viettel.bccs.organization.custchanneltype.repository.CustChannelTypeMappingRepository;
import com.viettel.bccs.organization.utils.Const;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustChannelTypeMappingService {

    private final CustChannelTypeMappingRepository mappingRepository;
    private final CustChannelTypeMappingMapper mappingMapper;

    @Transactional(readOnly = true)
    public List<CustChannelTypeMappingDTO> getAllActive() {
        log.info("Truy vấn tất cả mapping đang hiệu lực");
        return mappingRepository.findAllByStatus(Const.STATUS.ACTIVE)
                .stream()
                .map(mappingMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustChannelTypeMappingDTO> getByChannelTypeId(Long channelTypeId) {
        log.info("Truy vấn mapping theo loại kênh: {}", channelTypeId);
        return mappingRepository.findAllByChannelTypeIdAndStatus(channelTypeId, Const.STATUS.ACTIVE)
                .stream()
                .map(mappingMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustChannelTypeMappingDTO getByCustTypeAndChannelType(String custType, Long channelTypeId) {
        log.info("Truy vấn mapping theo loại khách hàng {} và loại kênh {}", custType, channelTypeId);
        List<CustChannelTypeMappingEntity> results = mappingRepository.findByCustTypeAndChannelTypeIdAndStatus(custType, channelTypeId, Const.STATUS.ACTIVE);
        if (results.isEmpty()) {
            throw new BusinessException("ORGANIZATION-CUST_CHANNEL_TYPE-002",
                    "Không tìm thấy mapping cho loại khách hàng: " + custType + " và loại kênh: " + channelTypeId);
        }
        return mappingMapper.toDTO(results.get(0));
    }
}