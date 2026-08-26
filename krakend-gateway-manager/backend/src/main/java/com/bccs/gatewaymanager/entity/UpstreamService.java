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
 * CircuitBreaker/Retry/Bulkhead + namespace cache Redis (dung chung 1 ten =
 * upstream.name cho ca 3 - xem CompositeOrchestratorEngine).
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

    @Builder.Default
    @Column(name = "cache_enabled", nullable = false)
    private boolean cacheEnabled = false;

    @Builder.Default
    @Column(name = "cache_ttl_seconds", nullable = false)
    private int cacheTtlSeconds = 300;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
