package com.bccs.gatewaymanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * 1 dong lich su doi gia tri cờ (GWM_FLAG_HISTORY - DDL V5) - ghi moi lan
 * toggle/tao moi/truoc khi xoa. KHONG FK len GWM_FLAG: row cờ co the bi xoa
 * nhung lich su van giu de doi soat "tai sao 10h sang luong doi" (oldValue/
 * newValue + flagCode snapshot ngay trong row).
 */
@Entity
@Table(name = "gwm_flag_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GwmFlagHistory {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Column(name = "flag_id", nullable = false)
    private String flagId;

    /** Coppy cua teamCode/flagCode tai thoi diem ghi - row van doc duoc neu cờ bi xoa. */
    @Column(name = "team_code", nullable = false, length = 50)
    private String teamCode;

    @Column(name = "flag_code", nullable = false, length = 50)
    private String flagCode;

    /** null = cờ vua duoc tao (chua co gia tri cu). Boolean -> NUMBER(1) qua Hibernate. */
    @Column(name = "old_value")
    private Boolean oldValue;

    @Column(name = "new_value", nullable = false)
    private Boolean newValue;

    /** Nguon thao tac: "TOGGLE" (nut bat/tat UI), "CREATE", "UPDATE", "DELETE", "IMPORT". */
    @Column(nullable = false, length = 50)
    private String source;

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    private Instant changedAt;
}
