package com.viettel.bccs.productcatalog.productpackagefee.repository;

import com.viettel.bccs.productcatalog.productpackagefee.entity.ProductPackageFeeEntity;
import com.viettel.bccs.productcatalog.utils.Const;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductPackageFeeRepositoryCustomImpl implements ProductPackageFeeRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public List<ProductPackageFeeEntity> findByProductPackageId(Long productPackageId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT a.* FROM ").append(Const.DEFAULT_PRODUCT_SCHEMA).append("product_package_fee a");
        sql.append(" WHERE a.status = '1'");
        sql.append(" AND (a.effect_datetime IS NULL OR TRUNC(a.effect_datetime) <= TRUNC(SYSDATE))");
        sql.append(" AND (a.expire_datetime IS NULL OR TRUNC(a.expire_datetime) >= TRUNC(SYSDATE))");
        sql.append(" AND a.product_package_id = :productPackageId");
        sql.append(" ORDER BY a.product_package_fee_id");

        Query query = entityManager.createNativeQuery(sql.toString(), ProductPackageFeeEntity.class);
        query.setParameter("productPackageId", productPackageId);

        return query.getResultList();
    }
}