package com.viettel.bccs.policy.discountpromotion.repository;

import com.viettel.bccs.policy.discountpromotion.entity.DiscountPromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountPromotionRepository
        extends JpaRepository<DiscountPromotionEntity, Long>, DiscountPromotionRepositoryCustom {
}