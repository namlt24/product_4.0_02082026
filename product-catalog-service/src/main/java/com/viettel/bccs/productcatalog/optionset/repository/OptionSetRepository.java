package com.viettel.bccs.productcatalog.optionset.repository;

import com.viettel.bccs.productcatalog.optionset.entity.OptionSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OptionSetRepository extends JpaRepository<OptionSetEntity, Long> {

    Optional<OptionSetEntity> findByCode(String code);

    Optional<OptionSetEntity> findByCodeAndStatus(String code, String status);

    boolean existsByCode(String code);
}