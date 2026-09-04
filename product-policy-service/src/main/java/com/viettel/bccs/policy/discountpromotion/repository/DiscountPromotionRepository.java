package com.viettel.bccs.policy.discountpromotion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.policy.discountpromotion.entity.DiscountPromotionEntity;

@Repository
public interface DiscountPromotionRepository
        extends JpaRepository<DiscountPromotionEntity, Long>, DiscountPromotionRepositoryCustom {
}