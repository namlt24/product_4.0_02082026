package com.viettel.bccs.organization.custtype.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.organization.custtype.dto.CustTypeDTO;
import com.viettel.bccs.organization.custtype.mapper.CustTypeMapper;
import com.viettel.bccs.organization.custtype.repository.CustTypeRepository;
import com.viettel.bccs.organization.utils.Const;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustTypeService {

    private final CustTypeRepository custTypeRepository;
    private final CustTypeMapper custTypeMapper;

    @Cacheable(value = "custTypeCache", key = "'CUST_TYPE:' + #custType")
    @Transactional(readOnly = true)
    public CustTypeDTO findActiveByCustType(String custType) {
        log.info("Truy vấn loại khách hàng từ DB theo mã: {}", custType);
        return custTypeRepository.findByCustTypeAndStatus(custType, Const.STATUS.ACTIVE)
                .map(custTypeMapper::toDTO)
                .orElseThrow(() -> new BusinessException("BCCS-ORGANIZATION-CUSTTYPE-0001", "Không tìm thấy loại khách hàng với mã: " + custType));
    }

    @Transactional(readOnly = true)
    public List<CustTypeDTO> getAllActive() {
        log.info("Truy vấn tất cả loại khách hàng đang hiệu lực");
        return custTypeRepository.findAllByStatus(Const.STATUS.ACTIVE)
                .stream()
                .map(custTypeMapper::toDTO)
                .toList();
    }

    @Cacheable(value = "custTypeCache", key = "'MAPPING_CHANNEL_CUST_TYPE:' + #channelTypeId + ':' + #groupType")
    @Transactional(readOnly = true)
    public List<CustTypeDTO> getMappingChannelCustType(Long channelTypeId, String groupType) {
        log.info("Truy vấn loại khách hàng theo kênh {} và nhóm {}", channelTypeId, groupType);
        return custTypeRepository.getMappingChannelCustType(channelTypeId, groupType)
                .stream()
                .map(custTypeMapper::toDTO)
                .toList();
    }
}