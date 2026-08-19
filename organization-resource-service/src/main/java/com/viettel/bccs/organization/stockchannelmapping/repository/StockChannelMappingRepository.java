package com.viettel.bccs.organization.stockchannelmapping.repository;

import com.viettel.bccs.organization.stockchannelmapping.entity.StockChannelMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockChannelMappingRepository
        extends JpaRepository<StockChannelMappingEntity, Long>, StockChannelMappingRepositoryCustom {

    List<StockChannelMappingEntity> findAllByStatus(String status);

    List<StockChannelMappingEntity> findAllByChannelTypeIdAndStatus(Long channelTypeId, String status);

}
