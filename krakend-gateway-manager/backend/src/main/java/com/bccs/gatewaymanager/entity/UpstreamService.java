package com.bccs.gatewaymanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Dang ky 1 lan cho 1 backend that (vi du "product-catalog-service"). Cac
 * BackendStep tham chieu toi day thay vi tu go host/timeout rieng le - tranh
 * lap lai cau hinh tren tung step, va la noi dat ten cho Resilience4j
 * CircuitBreaker/Retry/Bulkhead (dung upstream.name lam ten instance - xem
 * UpstreamHttpExecutor). Cache Redis KHONG con cau hinh o day - moi Upstream
 * bi nhieu BackendStep goi toi nhieu ham/path khac nhau, khong phai ham nao
 * cung nen cache, nen cache duoc chuyen xuong tung BackendStep (xem
 * BackendStep.cacheEnabled/cacheTtlSeconds).
 */
@Entity
@Table(name = "upstream_service", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpstreamService {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    /** Ten dinh danh duy nhat, dung lam ten instance Resilience4j + namespace cache. */
    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    /** Vi du "http://10.207.252.17:8045" - khong gom path. */
    @Column(name = "base_host", nullable = false)
    private String baseHost;

    @Builder.Default
    @Column(name = "connect_timeout_ms", nullable = false)
    private int connectTimeoutMs = 1000;

    @Builder.Default
    @Column(name = "read_timeout_ms", nullable = false)
    private int readTimeoutMs = 3000;

    @Builder.Default
    @Column(name = "circuit_breaker_enabled", nullable = false)
    private boolean circuitBreakerEnabled = true;

    /** Nguong ty le loi (%) de mo circuit breaker. Mac dinh 50. */
    @Builder.Default
    @Column(name = "failure_rate_threshold", nullable = false)
    private int failureRateThreshold = 50;

    /**
     * An toan bat cho ca GET lan POST khi backend chi phuc vu doc du lieu
     * (khong co side-effect ghi) - xac nhan tung upstream truoc khi bat de
     * tranh retry lam trung lap thao tac ghi.
     */
    @Builder.Default
    @Column(name = "retry_enabled", nullable = false)
    private boolean retryEnabled = true;

    /**
     * So luong lenh goi dong thoi toi da qua Resilience4j Bulkhead cho Upstream nay
     * (xem UpstreamHttpExecutor.bulkheadFor()) - truoc day fix cung 20 cho MOI Upstream,
     * gio chinh duoc rieng tung Upstream. Mac dinh 20 = dung y het gia tri fix cung cu,
     * Upstream chua tung chinh khong doi hanh vi.
     */
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("20")
    @Column(name = "max_concurrent_calls", nullable = false)
    private int maxConcurrentCalls = 20;

    /**
     * Thoi gian toi da (ms) 1 lenh goi cho co "cho" trong Bulkhead truoc khi bi tu
     * choi (BulkheadFullException) - truoc day fix cung 500ms. Mac dinh 500 = dung
     * y het gia tri fix cung cu.
     */
    @Builder.Default
    @org.hibernate.annotations.ColumnDefault("500")
    @Column(name = "max_wait_duration_ms", nullable = false)
    private int maxWaitDurationMs = 500;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
