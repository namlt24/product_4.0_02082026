package com.viettel.bccs.organization.channeltype.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.organization.channeltype.entity.ChannelTypeEntity;

@Repository
public interface ChannelTypeRepository extends JpaRepository<ChannelTypeEntity, Long>, ChannelTypeRepositoryCustom {

    Optional<ChannelTypeEntity> findByChannelTypeIdAndStatus(Long channelTypeId, String status);

    Optional<ChannelTypeEntity> findByCodeAndStatus(String code, String status);

    boolean existsByChannelTypeId(Long channelTypeId);
}
