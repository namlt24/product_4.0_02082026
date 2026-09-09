package com.bccs.gatewaymanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * 1 doi BCCS dang dung chung Control Plane nay - "team_code" do doi nen tang
 * (chung ta) tu quy dinh/cap phat luc tao (KHONG phai doi tu dang ky). Moi
 * dong o day tuong ung 1 API key rieng (xem ApiKeyAuthFilter) - request mang
 * dung key nao se duoc gan CurrentTeamContext theo dung "teamCode" cua dong
 * do, va MOI query/ghi tren EndpointConfig/UpstreamService deu tu dong loc
 * theo gia tri nay - day chinh la co che cach ly du lieu giua cac doi tren 1
 * DB dung chung.
 *
 * "apiKey" luu THANG (plaintext, khong hash/salt) - nhat quan voi cach
 * "gatewaymanager.admin-api-key" hien co dang luu (1 chuoi tinh so sanh
 * constant-time, khong co ha tang hash nao trong toan bo app). Ghi nhan day
 * la gioi han da biet o V1 (giong PII chua duoc redact truoc khi ghi audit
 * log) - khong chan viec dung, se nang cap sau neu can.
 */
@Entity
@Table(name = "gwm_team", uniqueConstraints = @UniqueConstraint(columnNames = "api_key"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    /** Ma doi, do doi nen tang tu dat luc tao (vi du "VCOM", "BILLING") - KHONG tu sinh UUID vi can de nho/tra cuu, xuat hien trong ten Resilience4j/APM cua Data Plane doi do. */
    @Id
    @Column(name = "team_code", length = 50)
    private String teamCode;

    @Column(name = "team_name", nullable = false)
    private String teamName;

    /** Khoa API rieng cua doi nay - dung cho ca UI/API Control Plane VA cho Data Plane cua doi tu dong bo config (xem RemoteConfigSyncService). Sinh ngau nhien luc tao, hien ra dung 1 lan (xem TeamService.create()). */
    @Column(name = "api_key", nullable = false)
    private String apiKey;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
