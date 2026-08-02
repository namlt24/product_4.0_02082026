package com.viettel.bccs.policy.discountpromotioncharuse.repository;

import com.viettel.bccs.policy.discountpromotioncharuse.entity.DiscountPromotionCharUseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountPromotionCharUseRepository extends JpaRepository<DiscountPromotionCharUseEntity, Long> {
}