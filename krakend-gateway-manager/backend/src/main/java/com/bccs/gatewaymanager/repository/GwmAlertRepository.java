package com.bccs.gatewaymanager.repository;

import com.bccs.gatewaymanager.entity.AlertStatus;
import com.bccs.gatewaymanager.entity.AlertTargetType;
import com.bccs.gatewaymanager.entity.AlertType;
import com.bccs.gatewaymanager.entity.GwmAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GwmAlertRepository extends JpaRepository<GwmAlert, String> {

    /**
     * Dedup lookup - tim row DANG MO (ACTIVE hoac ACKNOWLEDGED) trung dedup key.
     * Phai gom ACKNOWLEDGED: neu chi tim ACTIVE thi sau khi ACK, chu ky touch ke
     * tiep khong thay row -> tao row moi song song row ACK (2 row/1 van de).
     * Unique functional index V3 (GWM_ALERT_ACTIVE_DEDUP_UK) dam bao toi da 1 row
     * ACTIVE, race 2 pod DP push dong thoi -> 1 trong 2 bat
     * DataIntegrityViolationException roi coi nhu touch.
     */
    Optional<GwmAlert> findFirstByTeamCodeAndAlertTypeAndTargetTypeAndTargetNameAndStatusInOrderByLastSeenAtDesc(
            String teamCode, AlertType alertType, AlertTargetType targetType, String targetName,
            List<AlertStatus> statuses);

    /** Trang danh sach - loc trang thai (thuong la ACTIVE moi), moi nhat theo lan touch gan nhat. */
    List<GwmAlert> findByTeamCodeAndStatusOrderByLastSeenAtDesc(String teamCode, AlertStatus status);

    /** Trang danh sach - khong loc trang thai (xem lich su tu dau). */
    List<GwmAlert> findByTeamCodeOrderByLastSeenAtDesc(String teamCode);

    /**
     * Sweep staleness - ACTIVE/ACKNOWLEDGED khong duoc touch trong khoang staleness
     * => CP tu resolve (van de chac chan het hoac DP chet). Toan cuc (KHONG loc
     * theo team vi thread scheduler khong co CurrentTeamContext) va loc ngay o DB.
     */
    List<GwmAlert> findByStatusInAndLastSeenAtBefore(List<AlertStatus> statuses, Instant threshold);

    /** Badge toolbar + trang dashboard phu - dem nhanh so ACTIVE, khong load detail. */
    long countByTeamCodeAndStatus(String teamCode, AlertStatus status);

    /** Tong hop theo loai cho summary - group ben Java vi so luong loai co dinh va it (4 loai P1). */
    @Query("select a.alertType, count(a) from GwmAlert a where a.teamCode = :teamCode and a.status = :status group by a.alertType")
    List<Object[]> countByTypeForTeamAndStatus(@Param("teamCode") String teamCode, @Param("status") AlertStatus status);
}
