package com.bccs.gatewaymanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * 1 canh bao (van de dang xay hoac da xay) tren Data Plane - Data Plane PHAT
 * HIEN (CB state, sliding window loi, bulkhead) va day ve Control Plane qua
 * POST /api/internal/alerts; Control Plane LUU + dedup + hien UI. Dat ten
 * gwm_alert (tien to gwm_ nhu gwm_team) vi la bang "ho" cua gateway manager.
 *
 * GIAO THUC DEDUP (chong spam): DP co khong phai thoi diem gi cung day event -
 * DP tai gui event resolved=false moi chu ky (30s) khi van van de dang ton tai,
 * CP phan biet "touch" (update lastSeenAt/occurrenceCount, KHONG tao row moi,
 * KHONG thong bao lai) vs "van de moi" (row chua ton tai voi status ACTIVE).
 * Van de giong nhau NUNG RESOLVED xong lap lai se tao row moi (chu ky moi) -
 * hop le, vi do la 1 phat sinh moi.
 */
@Entity
@Table(name = "gwm_alert")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GwmAlert {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    /** Ma doi chu canh bao - CP resolve TU TEAM KEY DP gui (DP khong biet team_code). */
    @Column(name = "team_code", nullable = false)
    private String teamCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    /** Chi ACTIVE duoc gach dong duy nhat theo (team, type, target) - unique functional index V3. */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status = AlertStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private AlertTargetType targetType;

    /** Ten UpstreamService hoac path EndpointConfig - xem javadoc AlertTargetType. */
    @Column(name = "target_name", nullable = false)
    private String targetName;

    /** UUID cua upstream/endpoint neu biet - co the null (target da bi xoa truoc khi alert resolve). */
    @Column(name = "target_id")
    private String targetId;

    @Column(nullable = false)
    private String title;

    /** Chi tiet ky thuat (ty le %, so call, error moi nhat...) - de dieu tra, khong cho vao title. */
    @Lob
    @Column(name = "detail")
    private String detail;

    /** Lan dau phat hien van de nay (DP gui occurredAt cua lan dau; dedup giu nguyen). */
    @Column(name = "first_seen_at", nullable = false, updatable = false)
    private Instant firstSeenAt;

    /** Lan gan nhat DP xac nhan van de van con - dung cho sweep staleness (treo qua lau = het van de). */
    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    /** So chu ky touch da nhan (khong phai so lan gap loi thuc te - 1 chu ky co the co nhieu call). */
    @Builder.Default
    @Column(name = "occurrence_count", nullable = false)
    private int occurrenceCount = 1;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    /** DP_RESOLVED (DP bao het van de) / STALENESS (qua han khong co tin moi) / MANUAL (nguoi dung resolve tay). */
    @Column(name = "resolved_reason")
    private String resolvedReason;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}
