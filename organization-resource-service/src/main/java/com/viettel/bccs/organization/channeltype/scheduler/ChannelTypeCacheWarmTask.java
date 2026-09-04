package com.viettel.bccs.organization.channeltype.scheduler;

import java.time.Duration;

import org.springframework.stereotype.Component;

import com.viettel.bccs.organization.cache.warmer.BccsCacheWarmTask;
import com.viettel.bccs.organization.channeltype.repository.ChannelTypeRepository;
import com.viettel.bccs.organization.channeltype.service.ChannelTypeService;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class ChannelTypeCacheWarmTask implements BccsCacheWarmTask {

    private final ChannelTypeRepository channelTypeRepository;
    private final ChannelTypeService channelTypeService;

    @Override
    public String cacheName() {
        return "channelTypeCache";
    }

    @Override
    public Duration interval() {
        return Duration.ofMinutes(4);
    }

    @Override
    public void warm() {
        for (Long id : channelTypeRepository.findActiveChannelTypeIds()) {
            channelTypeService.getActiveById(id);
        }
    }
}
