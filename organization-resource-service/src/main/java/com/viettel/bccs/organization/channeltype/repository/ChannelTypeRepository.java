package com.viettel.bccs.organization.channeltype.repository;

import com.viettel.bccs.organization.channeltype.entity.ChannelTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChannelTypeRepository extends JpaRepository<ChannelTypeEntity, Long> {

    Optional<ChannelTypeEntity> findByChannelTypeIdAndStatus(Long channelTypeId, String status);

    Optional<ChannelTypeEntity> findByCodeAndStatus(String code, String status);

    boolean existsByChannelTypeId(Long channelTypeId);
}