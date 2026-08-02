package com.viettel.bccs.organization.custchanneltype.repository;

import com.viettel.bccs.organization.custchanneltype.entity.CustChannelTypeMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustChannelTypeMappingRepository extends JpaRepository<CustChannelTypeMappingEntity, Long> {

    List<CustChannelTypeMappingEntity> findAllByStatus(String status);

    List<CustChannelTypeMappingEntity> findAllByChannelTypeIdAndStatus(Long channelTypeId, String status);

    List<CustChannelTypeMappingEntity> findByCustTypeAndChannelTypeIdAndStatus(String custType, Long channelTypeId, String status);

    boolean existsByCustTypeAndChannelTypeIdAndStatus(String custType, Long channelTypeId, Integer status);
}