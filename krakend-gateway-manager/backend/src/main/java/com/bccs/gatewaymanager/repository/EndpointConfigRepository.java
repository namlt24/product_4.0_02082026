package com.bccs.gatewaymanager.repository;

import com.bccs.gatewaymanager.entity.EndpointConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EndpointConfigRepository extends JpaRepository<EndpointConfig, String> {

    // --- Method CU (khong con team_code) - CHI con dung noi bo boi Data Plane cu/
    // truoc khi tach profile (xem RemoteConfigSyncService) - MOI duong CRUD cua
    // Control Plane phai dung ban scoped-theo-team_code ben duoi, khong dung lai
    // method nay de tranh 1 doi thay/sua duoc du lieu doi khac. ---
    boolean existsByPath(String path);

    boolean existsByPathAndIdNot(String path, String id);

    Optional<EndpointConfig> findByPath(String path);

    List<EndpointConfig> findAllByOrderByUpdatedAtDesc();

    // --- Method MOI, scoped theo team_code - dung cho toan bo Control Plane. ---
    boolean existsByTeamCodeAndPath(String teamCode, String path);

    boolean existsByTeamCodeAndPathAndIdNot(String teamCode, String path, String id);

    Optional<EndpointConfig> findByTeamCodeAndPath(String teamCode, String path);

    Optional<EndpointConfig> findByIdAndTeamCode(String id, String teamCode);

    List<EndpointConfig> findAllByTeamCodeOrderByUpdatedAtDesc(String teamCode);

    /**
     * Luon duoc goi voi q KHONG null/rong (xem EndpointService.list()).
     * Neu truyen null truc tiep vao :q khi no chi xuat hien ben trong lower(...),
     * driver PostgreSQL khong suy duoc kieu tham so va bind no thanh "bytea",
     * gay loi "function lower(bytea) does not exist".
     */
    @Query("select e from EndpointConfig e where e.teamCode = :teamCode and (" +
            "lower(e.name) like lower(concat('%', :q, '%')) " +
            "or lower(e.path) like lower(concat('%', :q, '%'))) " +
            "order by e.updatedAt desc")
    List<EndpointConfig> search(@Param("teamCode") String teamCode, @Param("q") String q);

    /** Dem so BackendStep (thuoc bat ky endpoint CUNG DOI nao) dang tham chieu 1 Upstream Service - dung de chan xoa Upstream con dang dung. */
    @Query("select count(s) from EndpointConfig e join e.steps s where s.upstreamService.id = :upstreamId and e.teamCode = :teamCode")
    long countStepsByUpstreamId(@Param("upstreamId") String upstreamId, @Param("teamCode") String teamCode);

    /**
     * Dau van tay config cua 1 doi cho co che ETag/304 cua sync DP->CP (xem
     * ConfigExportImportService.computeConfigHash()): chi doc 2 gia tri tong hop
     * (COUNT + MAX(updated_at)) thay vi SELECT toan bo du lieu - goi moi chu ky
     * sync 15s nen phai re. MAX(updated_at) nhay khi row endpoint nao do duoc
     * create/update (JPA @UpdateTimestamp); COUNT nhay khi them/xoa endpoint.
     * LUON tra 1 row (aggregate query) - phan tu la Object[]{count, max}.
     */
    @Query("select count(e), max(e.updatedAt) from EndpointConfig e where e.teamCode = :teamCode")
    List<Object[]> fingerprintByTeamCode(@Param("teamCode") String teamCode);
}
