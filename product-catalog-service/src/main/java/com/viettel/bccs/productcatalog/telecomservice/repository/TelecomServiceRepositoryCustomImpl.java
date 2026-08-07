package com.viettel.bccs.productcatalog.telecomservice.repository;

import com.viettel.bccs.productcatalog.telecomservice.entity.TelecomServiceEntity;
import com.viettel.bccs.productcatalog.utils.Const;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TelecomServiceRepositoryCustomImpl implements TelecomServiceRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public TelecomServiceEntity getTelServiceByAlias(String alias) {
        String sql = "SELECT a.* FROM " + Const.DEFAULT_PRODUCT_SCHEMA + "telecom_service a" +
                " WHERE a.status = '1' AND a.service_alias = :alias";

        Query query = entityManager.createNativeQuery(sql, TelecomServiceEntity.class);
        query.setParameter("alias", alias);

        @SuppressWarnings("unchecked")
        List<TelecomServiceEntity> result = query.getResultList();
        return result.isEmpty() ? null : result.get(0);
    }
}
