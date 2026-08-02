package com.viettel.bccs.productcatalog.productpackagecharuse.repository;

import com.viettel.bccs.productcatalog.productpackagecharuse.entity.ProductPackageCharUseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductPackageCharUseRepository extends JpaRepository<ProductPackageCharUseEntity, Long>, ProductPackageCharUseRepositoryCustom {

}