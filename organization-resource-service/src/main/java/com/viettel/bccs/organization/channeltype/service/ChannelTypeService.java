package com.viettel.bccs.organization.channeltype.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.organization.channeltype.dto.ChannelTypeDTO;
import com.viettel.bccs.organization.channeltype.mapper.ChannelTypeMapper;
import com.viettel.bccs.organization.channeltype.repository.ChannelTypeRepository;
import com.viettel.bccs.organization.utils.Const;
import com.viettel.bccs.organization.utils.DataUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelTypeService {

    private final ChannelTypeRepository channelTypeRepository;
    private final ChannelTypeMapper channelTypeMapper;

    @Cacheable(value = "channelTypeCache", key = "'CHANNEL_TYPE:' + #channelTypeId")
    @Transactional(readOnly = true)
    public ChannelTypeDTO getActiveById(Long channelTypeId) {
        log.info("Truy vấn loại kênh active từ DB theo id: {}", channelTypeId);
        return channelTypeRepository.findByChannelTypeIdAndStatus(channelTypeId, Const.STATUS.ACTIVE)
                .map(channelTypeMapper::toDTO)
                .orElseThrow(() -> new BusinessException("BCCS-ORGANIZATION-CHANNELTYPE-0001", "Không tìm thấy loại kênh với id: " + channelTypeId));
    }


    @Transactional(readOnly = true)
    public Boolean isChannelOfSalePoint(Long channelTypeId) {
        if (DataUtil.isNullObject(channelTypeId)) {
            return false;
        }
        ChannelTypeDTO channelTypeDTO;
        try {
            channelTypeDTO = getActiveById(channelTypeId);
        } catch (BusinessException e) {
            log.warn("Không tìm thấy loại kênh hoạt động, coi như không phải kênh bán: {}", channelTypeId);
            return false;
        }
        if (DataUtil.isNullObject(channelTypeDTO)) {
            return false;
        }
        return DataUtil.safeEqual(Const.CHANNEL_TYPE.IS_NOT_VT_UNIT, channelTypeDTO.getIsVtUnit())
                && DataUtil.safeEqual(Const.CHANNEL_TYPE.OBJECT_TYPE_STAFF, channelTypeDTO.getObjectType());
    }


    @Transactional(readOnly = true)
    public Boolean isChannelOfAgent(Long channelTypeId) {
        if (DataUtil.isNullObject(channelTypeId)) {
            return false;
        }
        ChannelTypeDTO channelTypeDTO;
        try {
            channelTypeDTO = getActiveById(channelTypeId);
        } catch (BusinessException e) {
            log.warn("Không tìm thấy loại kênh hoạt động, coi như không phải kênh đại lý: {}", channelTypeId);
            return false;
        }
        if (DataUtil.isNullObject(channelTypeDTO)) {
            return false;
        }
        return DataUtil.safeEqual(Const.CHANNEL_TYPE.IS_NOT_VT_UNIT, channelTypeDTO.getIsVtUnit())
                && DataUtil.safeEqual(Const.CHANNEL_TYPE.OBJECT_TYPE_SHOP, channelTypeDTO.getObjectType());
    }

}