package com.viettel.bccs.policy.discountpromotioncharuse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.policy.discountpromotioncharuse.entity.DiscountPromotionCharUseEntity;

@Repository
public interface DiscountPromotionCharUseRepository extends JpaRepository<DiscountPromotionCharUseEntity, Long> {
}