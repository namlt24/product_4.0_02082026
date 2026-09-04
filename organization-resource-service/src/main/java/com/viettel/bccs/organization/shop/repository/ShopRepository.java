package com.viettel.bccs.organization.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.organization.shop.entity.ShopEntity;

@Repository
public interface ShopRepository extends JpaRepository<ShopEntity, Long>, ShopRepositoryCustom {

    Optional<ShopEntity> findByShopCodeAndStatus(String shopCode, String status);

    Optional<ShopEntity> findByShopIdAndStatus(Long shopId, String status);

    List<ShopEntity> findAllByChannelTypeIdAndStatus(Long channelTypeId, String status);

    boolean existsByShopCode(String shopCode);
}
