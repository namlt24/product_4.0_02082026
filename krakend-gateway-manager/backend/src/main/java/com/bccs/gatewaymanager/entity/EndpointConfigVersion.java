package com.bccs.gatewaymanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * 1 "snapshot" toan bo EndpointConfig (bao gom steps + mappings) tai 1 thoi
 * diem save (create/update/rollback) - phuc vu man hinh "Lich su phien ban" +
 * rollback. Bang MOI hoan toan (khong ALTER bang da co du lieu) nen moi cot co
 * the NOT NULL ngay tu dau, khong can @ColumnDefault nhu cac lan them cot vao
 * bang da co du lieu truoc day (xem BackendStep.cacheEnabled/FieldMapping.mappingOrder).
 *
 * KHONG dung JPA relationship (@ManyToOne) toi EndpointConfig - co tinh giu
 * loi long ghep (chi luu endpointId dang String phang) de tranh dung cham vao
 * quan he/cascade da phuc tap san cua EndpointConfig (steps/mappings). Xoa cac
 * version cua 1 endpoint la thao tac tuong minh o EndpointVersionService.deleteAllForEndpoint(),
 * KHONG dua vao cascade tu dong.
 *
 * versionNumber tang dan theo tung endpoint (1, 2, 3, ...) - tinh o EndpointVersionService
 * bang SELECT MAX+1 trong CUNG 1 transaction voi lan save entity chinh. Unique
 * constraint (endpoint_id, version_number) o day la luoi an toan: neu co race
 * condition that hiem (2 request sua CUNG 1 endpoint gan nhu dong thoi) thi DB
 * se tu choi 1 trong 2 (loi 500 ro rang) thay vi am tham ghi 2 ban ghi trung
 * versionNumber - chap nhan duoc cho 1 cong cu admin, khong ky vong tan suat cao.
 */
@Entity
@Table(
        name = "endpoint_config_version",
        indexes = @Index(name = "idx_ecv_endpoint_id", columnList = "endpoint_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"endpoint_id", "version_number"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointConfigVersion {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Column(name = "endpoint_id", nullable = false)
    private String endpointId;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private EndpointChangeType changeType;

    // Denormalized tu snapshot - hien thi nhanh trong danh sach lich su ma khong
    // can parse JSON. Cung do dai mac dinh (255) voi EndpointConfig.name/path
    // that vi gia tri luon sao chep tu do - khong tao rui ro moi.
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GatewayMethod method;

    /** Toan bo EndpointResponseDto tai thoi diem save, serialize JSON - dung de rollback. */
    @Lob
    @Column(name = "snapshot_json", nullable = false)
    private String snapshotJson;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
