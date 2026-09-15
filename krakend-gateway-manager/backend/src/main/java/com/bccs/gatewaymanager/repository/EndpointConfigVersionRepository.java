package com.bccs.gatewaymanager.repository;

import com.bccs.gatewaymanager.entity.EndpointConfigVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EndpointConfigVersionRepository extends JpaRepository<EndpointConfigVersion, String> {

    List<EndpointConfigVersion> findByEndpointIdOrderByVersionNumberDesc(String endpointId);

    Optional<EndpointConfigVersion> findTopByEndpointIdOrderByVersionNumberDesc(String endpointId);

    /** Tra ve so ban ghi da xoa - goi trong EndpointService.delete() de don version cua 1 endpoint bi xoa. */
    long deleteByEndpointId(String endpointId);

    /**
     * Dau van tay lich su phien ban cho ETag/304 sync (xem
     * ConfigExportImportService.computeConfigHash()): moi duong sua endpoint
     * (create/update/rollback/delete) deu ghi 1 version trong CUNG transaction
     * (xem EndpointVersionService.recordSnapshot) nen MAX(created_at) cua bang
     * nay bat duoc ca thay doi chi dong den step con (allow/deny/rename...) -
     * truong hop updated_at cua row endpoint cha co the khong nhay.
     * Version table khong co team_code - join qua EndpointConfig de loc theo doi.
     */
    @Query("select count(v), max(v.createdAt) from EndpointConfigVersion v join EndpointConfig e on v.endpointId = e.id where e.teamCode = :teamCode")
    List<Object[]> fingerprintByTeamCode(@Param("teamCode") String teamCode);
}
