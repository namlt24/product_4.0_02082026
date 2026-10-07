package com.bccs.gatewaymanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * 1 CỜ NGHIỆP VỤ (Business Flag) do doi tu khai bao tren he thong - KHONG doc
 * tu request client (client khong truyen; gia tri do doi chu dong bat/tat).
 * Boolean duy nhat (BAT/TAT). Dung o 2 noi:
 *  - Rule cờ tren endpoint (endpoint_config.flag_rules - DDL V5): to hop cờ
 *    (trong nhom AND, giua nhom OR) -> bat dau tu step nao / chan request.
 *  - Dieu kien re nhanh tai step (P1-5): conditionSourceType=SYSTEM_FLAG ->
 *    doc gia tri hien tai cua cờ thay vi body/response.
 *
 * Multi-tenant: UNIQUE(team_code, flag_code) - engine tra cuu theo flagCode
 * trong pham vi 1 doi; 2 doi duoc phep trung flag_code (giong endpoint path).
 */
@Entity
@Table(name = "gwm_flag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GwmFlag {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Column(name = "team_code", nullable = false)
    private String teamCode;

    /** Ma cờ - dùng trong Rule + dieu kien step, vd "VIP", "KYC_DONE". Duy nhat trong team. */
    @Column(name = "flag_code", nullable = false, length = 100)
    private String flagCode;

    /** Ten hien thi tren UI (doc cho BA/tester) - co the trung flag_code khi khong khai. */
    @Column(name = "display_name")
    private String displayName;

    @Column(length = 1000)
    private String description;

    /** true = BAT. Toggle khi chay that -> DP sync <=15s -> traffic chay luong moi ngay. */
    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
