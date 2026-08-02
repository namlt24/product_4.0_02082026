package com.viettel.bccs.organization.staffext.repository;

import com.viettel.bccs.organization.staffext.entity.StaffExtEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StaffExtRepositoryCustomImpl implements StaffExtRepositoryCustom {

    private final EntityManager em;

    @Override
    public Optional<StaffExtEntity> findByStaffIdAndKey(Long staffId, String key, String checkValue) {
        StringBuilder builder = new StringBuilder("SELECT s.*");
        builder.append(" FROM bccs_product.staff_ext s ");
        builder.append(" WHERE 1 = 1 AND s.status = 1 ");
        builder.append(" AND s.staff_id = :staffId ");
        builder.append(" AND s.key = :key");
        if (checkValue != null) {
            builder.append(" and value is not null ");
        }

        @SuppressWarnings("unchecked")
        Query query = em.createNativeQuery(builder.toString(), StaffExtEntity.class);
        query.setParameter("staffId", staffId);
        query.setParameter("key", key);

        return Optional.ofNullable((StaffExtEntity) query.getSingleResult());
    }
}