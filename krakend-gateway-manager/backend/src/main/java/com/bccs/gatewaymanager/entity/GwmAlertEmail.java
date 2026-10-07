package com.bccs.gatewaymanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * 1 dia chi email nhan canh bao cua 1 doi - doi tu quan ly bang team key qua
 * API /api/alerts/recipients (xem AlertEmailRecipientService). Khi Control
 * Plane tao 1 canh bao MOI (AlertService.ingestOne - log "Canh bao MOI"),
 * AlertEmailService gui mail den TAT CA dia chi cua doi do; doi chua khai dia
 * chi nao thi dung fallback config env GATEWAY_ALERT_EMAIL_FALLBACK (xem
 * AlertEmailService.resolveRecipients()).
 *
 * Bang rieng thay vi cot EMAIL trong GWM_TEAM (DDL V4__alert_mail.sql):
 * 1 doi co NHIEU nguoi nhan (dev lead + on-call...), email ca nhan thay doi
 * thuong xuyen nen doi tu quan ly la hop ly, va tranh dung vao GWM_TEAM dang
 * duoc ApiKeyAuthFilter tra cuu moi request. Unique (TEAM_CODE, EMAIL) o V4
 * chong khai trung - entity KHONG khai @UniqueConstraint (index do SQL tao,
 * giong GwmAlert voi functional index V3).
 */
@Entity
@Table(name = "gwm_alert_email")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GwmAlertEmail {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    /** Ma doi chu dia chi nay - FK sang GWM_TEAM (V4). CurrentTeamContext khi API goi. */
    @Column(name = "team_code", nullable = false)
    private String teamCode;

    /** Dia chi email nguoi nhan - validate phia app (format + duplicate + max 10/team, xem AlertEmailRecipientService). */
    @Column(name = "email", nullable = false)
    private String email;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
