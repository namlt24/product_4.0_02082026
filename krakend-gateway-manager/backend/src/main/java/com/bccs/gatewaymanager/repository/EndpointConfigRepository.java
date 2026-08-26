package com.bccs.gatewaymanager.repository;

import com.bccs.gatewaymanager.entity.EndpointConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EndpointConfigRepository extends JpaRepository<EndpointConfig, String> {

    boolean existsByPath(String path);

    boolean existsByPathAndIdNot(String path, String id);

    Optional<EndpointConfig> findByPath(String path);

    /**
     * Luon duoc goi voi q KHONG null/rong (xem EndpointService.list()).
     * Neu truyen null truc tiep vao :q khi no chi xuat hien ben trong lower(...),
     * driver PostgreSQL khong suy duoc kieu tham so va bind no thanh "bytea",
     * gay loi "function lower(bytea) does not exist".
     */
    @Query("select e from EndpointConfig e where " +
            "lower(e.name) like lower(concat('%', :q, '%')) " +
            "or lower(e.path) like lower(concat('%', :q, '%')) " +
            "order by e.updatedAt desc")
    List<EndpointConfig> search(@Param("q") String q);

    List<EndpointConfig> findAllByOrderByUpdatedAtDesc();

    /** Dem so BackendStep (thuoc bat ky endpoint nao) dang tham chieu 1 Upstream Service - dung de chan xoa Upstream con dang dung. */
    @Query("select count(s) from EndpointConfig e join e.steps s where s.upstreamService.id = :upstreamId")
    long countStepsByUpstreamId(@Param("upstreamId") String upstreamId);
}
