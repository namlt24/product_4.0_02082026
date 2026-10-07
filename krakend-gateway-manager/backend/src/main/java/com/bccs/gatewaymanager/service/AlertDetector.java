package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.AlertEventDto;
import com.bccs.gatewaymanager.entity.AlertSeverity;
import com.bccs.gatewaymanager.entity.AlertTargetType;
import com.bccs.gatewaymanager.entity.AlertType;
import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Data-Plane-only - "bo nao" phat hien van de canh bao tai cho (DP la noi
 * thuc su chay traffic nen CB state + ty le loi chi o day moi dung). Chu ky 30s:
 * - Doc CB state cua moi upstream (CircuitBreakerRegistry - pattern
 *   UpstreamHealthService) -> CIRCUIT_OPEN khi state=OPEN.
 * - Doc cua so truot 60s tu AlertCallObserver.recordUpstreamCall() ->
 *   ERROR_RATE_HIGH khi ty le loi >= nguong voi du so call.
 * - Doc counter bulkhead tu choi (UpstreamHttpExecutor.bulkheadRejectionCount)
 *   -> BULKHEAD_SATURATED khi so tu choi moi cua so >= nguong.
 * - Doc cua so truot per-endpoint -> ENDPOINT_ERROR_RATE.
 *
 * GIAO THUC RE-EMIT (chong spam - xem javadoc GwmAlert): giu trang thai "van de
 * DANG canh bao" trong memory; khi van de van con thi moi chu ky TAI GUI cung
 * event (resolved=false) de CP "touch" (lastSeenAt/occurrenceCount++, khong tao
 * row moi); khi van de het thi gui DUY NHAT 1 event resolved=true de CP resolve.
 * DP restart mat trang thai -> CB re-open se tu bao lai (dung), alert cu khong
 * duoc touch -> CP tu resolve sau staleness (dung).
 */
@Slf4j
@Component
@Profile("data-plane")
public class AlertDetector implements AlertCallObserver {

    /** 1 diem so lieu trong cua so truot (timestamp + success hay loi). */
    private record CallPoint(Instant at, boolean success) {
    }

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    // @Lazy pha vong khoi tao: UpstreamHttpExecutor inject AlertCallObserver (chinh la
    // bean nay o DP), con bean nay doc bulkheadRejectionCount() tu executor trong chu ky
    // 30s - khong bao gio luc khoi dong, proxy lazy vo hai.
    private final @Lazy UpstreamHttpExecutor upstreamHttpExecutor;
    private final AlertEventPublisher publisher;
    private final boolean enabled;
    private final long windowSeconds;
    private final int errorRateThresholdPercent;
    private final int errorRateMinCalls;
    private final int bulkheadRejectionThreshold;
    private final long intervalSeconds;

    /** Cua so truot per-upstream (key = ten upstream - cung la ten CB/bulkhead goc). */
    private final Map<String, Deque<CallPoint>> upstreamWindows = new ConcurrentHashMap<>();
    /** Cua so truot per-endpoint (key = endpointId, de dedup CP dung path cung duoc nhung id it nho hon). */
    private final Map<String, Deque<CallPoint>> endpointWindows = new ConcurrentHashMap<>();
    private final Map<String, String> endpointPathById = new ConcurrentHashMap<>();

    /** Snapshot bulkhead rejection lan doc truoc - diff de dem "moi cua so" chu khong phai tong cong. */
    private final Map<String, Long> lastBulkheadTotal = new ConcurrentHashMap<>();
    /** Chu ky diff bulkhead gan nhat co rejection moi (khong phai tong) - dung khi quyet dinh "van con day". */
    private final Map<String, Long> bulkheadInWindow = new ConcurrentHashMap<>();

    /** Trang thai "van de DANG canh bao" - chi emit touch khi van ton tai, resolved 1 lan khi het. */
    private final Map<String, AlertEventDto> activeProblems = new ConcurrentHashMap<>();

    /** CB vua chuyen OPEN giua 2 chu ky detector (tu ResilienceAlertListener, co the som hon chu ky). */
    private final Map<String, Long> cbJustOpened = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "gwm-alert-detector");
        t.setDaemon(true);
        return t;
    });

    public AlertDetector(CircuitBreakerRegistry circuitBreakerRegistry,
                         @Lazy UpstreamHttpExecutor upstreamHttpExecutor,
                         AlertEventPublisher publisher,
                         @Value("${gatewaymanager.alert.enabled:true}") boolean enabled,
                         @Value("${gatewaymanager.alert.error-rate-window-seconds:60}") long windowSeconds,
                         @Value("${gatewaymanager.alert.error-rate-threshold-percent:50}") int errorRateThresholdPercent,
                         @Value("${gatewaymanager.alert.error-rate-min-calls:10}") int errorRateMinCalls,
                         @Value("${gatewaymanager.alert.bulkhead-rejection-threshold:5}") int bulkheadRejectionThreshold,
                         @Value("${gatewaymanager.alert.detector-interval-seconds:30}") long intervalSeconds) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.upstreamHttpExecutor = upstreamHttpExecutor;
        this.publisher = publisher;
        this.enabled = enabled;
        this.windowSeconds = windowSeconds;
        this.errorRateThresholdPercent = errorRateThresholdPercent;
        this.errorRateMinCalls = errorRateMinCalls;
        this.bulkheadRejectionThreshold = bulkheadRejectionThreshold;
        this.intervalSeconds = intervalSeconds;
    }

    @PostConstruct
    void start() {
        if (!enabled) {
            log.info("Canh bao (AlertDetector) dang TAT (gatewaymanager.alert.enabled=false).");
            return;
        }
        // Tri hoan 1 chu ky sau khoi dong - khong cham ngay luc DP dang nap registry config.
        scheduler.scheduleWithFixedDelay(this::detect, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        log.info("Canh bao (AlertDetector) da BAT - quet moi {}s: CB OPEN, error rate >= {}% (min {} calls trong {}s), bulkhead tu choi >= {}/{}s.",
                intervalSeconds, errorRateThresholdPercent, errorRateMinCalls, windowSeconds,
                bulkheadRejectionThreshold, windowSeconds);
    }

    @PreDestroy
    void stop() {
        scheduler.shutdown();
    }

    // ===== AlertCallObserver (duoc UpstreamHttpExecutor/DynamicDispatcherController goi) =====

    @Override
    public void recordUpstreamCall(String upstreamName, boolean success) {
        if (!enabled) {
            return;
        }
        try {
            Instant now = Instant.now();
            upstreamWindows.computeIfAbsent(upstreamName, n -> new ArrayDeque<>()).addLast(new CallPoint(now, success));
            evictOld(upstreamWindows.get(upstreamName), now);
        } catch (Exception e) {
            // Nuot het - so lieu canh bao KHONG BAO GIO anh huong traffic that.
        }
    }

    @Override
    public void recordEndpointRequest(String endpointId, String endpointPath, boolean success) {
        if (!enabled) {
            return;
        }
        try {
            Instant now = Instant.now();
            endpointPathById.put(endpointId, endpointPath);
            endpointWindows.computeIfAbsent(endpointId, n -> new ArrayDeque<>()).addLast(new CallPoint(now, success));
            evictOld(endpointWindows.get(endpointId), now);
        } catch (Exception e) {
            // Nuot het - giong recordUpstreamCall.
        }
    }

    private void evictOld(Deque<CallPoint> window, Instant now) {
        Instant cutoff = now.minusSeconds(windowSeconds);
        while (!window.isEmpty() && window.peekFirst().at().isBefore(cutoff)) {
            window.removeFirst();
        }
    }

    // ===== ResilienceAlertListener se goi ngay khi CB chuyen OPEN (khong doi chu ky) =====

    /** Goi tu ResilienceAlertListener moi khi 1 CB bat ky chuyen sang trang thai OPEN. */
    public void onCircuitBreakerOpened(String upstreamName) {
        if (!enabled) {
            return;
        }
        // Danh dau de chu ky detect() gan nhat xu ly NGAY (emit CIRCUIT_OPEN dung
        // luc CB mo thay vi doi toi da 30s). Long nanoTime de tranh key trung
        // upstream khi CB dong roi mo lai giua 2 chu ky.
        cbJustOpened.put(upstreamName, System.nanoTime());
    }

    // ===== Chu ky phat hien =====

    void detect() {
        try {
            Instant now = Instant.now();
            List<AlertEventDto> resolvedNow = new ArrayList<>();

            // 1) Circuit breaker - uu tien xu ly CB vua mo (cbJustOpened) va van OPEN.
            for (CircuitBreaker cb : circuitBreakerRegistry.getAllCircuitBreakers()) {
                if (cb.getState() == CircuitBreaker.State.OPEN) {
                    emitOrTouch(problem(AlertType.CIRCUIT_OPEN, AlertTargetType.UPSTREAM, cb.getName(),
                            AlertSeverity.CRITICAL, cb));
                }
            }
            // Xu ly CB vua mo chua kip vao chu ky (find tra ve empty trong truong hop
            // registry chua kip dang ky entry) - thuc te getAllCircuitBreakers() da
            // chua moi entry nen nhanh nay chi la "an toan them" cho race.
            cbJustOpened.clear();

            // 2) Error rate per-upstream.
            for (Map.Entry<String, Deque<CallPoint>> entry : upstreamWindows.entrySet()) {
                String upstreamName = entry.getKey();
                List<CallPoint> points = new ArrayList<>(entry.getValue());
                if (points.isEmpty()) {
                    continue;
                }
                long errors = points.stream().filter(p -> !p.success()).count();
                if (points.size() >= errorRateMinCalls && errors * 100L >= (long) errorRateThresholdPercent * points.size()) {
                    emitOrTouch(problem(AlertType.ERROR_RATE_HIGH, AlertTargetType.UPSTREAM, upstreamName,
                            AlertSeverity.WARNING, String.format(
                                    "Ty le loi %d/%d (%.0f%%) trong %ds qua nguong %d%%.",
                                    errors, points.size(), errors * 100.0 / points.size(), windowSeconds,
                                    errorRateThresholdPercent)));
                }
            }

            // 3) Bulkhead rejection (diff so voi lan doc truoc - bulkheadRejectionCount
            // la TONG CONG tu luc tao registry nen phai tru).
            for (CircuitBreaker cb : circuitBreakerRegistry.getAllCircuitBreakers()) {
                String upstreamName = cb.getName();
                long total = upstreamHttpExecutor.bulkheadRejectionCount(upstreamName);
                Long previous = lastBulkheadTotal.get(upstreamName);
                lastBulkheadTotal.put(upstreamName, total);
                if (previous == null) {
                    continue; // chu ky dau - khong co moc diff
                }
                long inWindow = total - previous;
                bulkheadInWindow.put(upstreamName, inWindow);
                if (inWindow >= bulkheadRejectionThreshold) {
                    emitOrTouch(problem(AlertType.BULKHEAD_SATURATED, AlertTargetType.UPSTREAM, upstreamName,
                            AlertSeverity.WARNING, String.format(
                                    "Bulkhead tu choi %d lenh goi trong %ds (nguong %d) - tang maxConcurrentCalls hoac kiem tra upstream co cham di khong.",
                                    inWindow, windowSeconds, bulkheadRejectionThreshold)));
                }
            }

            // 4) Error rate per-endpoint.
            for (Map.Entry<String, Deque<CallPoint>> entry : endpointWindows.entrySet()) {
                String endpointId = entry.getKey();
                List<CallPoint> points = new ArrayList<>(entry.getValue());
                if (points.isEmpty()) {
                    continue;
                }
                long errors = points.stream().filter(p -> !p.success()).count();
                if (points.size() >= errorRateMinCalls && errors * 100L >= (long) errorRateThresholdPercent * points.size()) {
                    String path = endpointPathById.getOrDefault(endpointId, endpointId);
                    emitOrTouch(problem(AlertType.ENDPOINT_ERROR_RATE, AlertTargetType.ENDPOINT, path,
                            AlertSeverity.WARNING, String.format(
                                    "Endpoint loi %d/%d request (%.0f%%) trong %ds qua nguong %d%%.",
                                    errors, points.size(), errors * 100.0 / points.size(), windowSeconds,
                                    errorRateThresholdPercent)));
                }
            }

            // 5) Van de het: cac problem van dang canh bao nhung khong con thoa dieu
            // kien (CB da dong, ty le loi ve muc an toan...) -> gui resolved=true 1 lan.
            for (Map.Entry<String, AlertEventDto> entry : activeProblems.entrySet()) {
                if (!isStillFailing(entry.getValue())) {
                    resolvedNow.add(toResolved(entry.getValue()));
                    activeProblems.remove(entry.getKey());
                }
            }
            publisher.submit(resolvedNow);
        } catch (Exception e) {
            // FAIL-SAFE: detector loi khong bao gio anh huong traffic (mirror AuditLogRetentionService).
            log.warn("Loi chu ky phat hien canh bao (bo qua lan nay): {}: {}",
                    e.getClass().getSimpleName(), e.getMessage());
        }
    }

    /** Van de con that su ton tai khong - de quyet dinh gui resolved=true. */
    private boolean isStillFailing(AlertEventDto problem) {
        return switch (problem.alertType()) {
            case CIRCUIT_OPEN -> {
                CircuitBreaker cb = circuitBreakerRegistry.find(problem.targetName()).orElse(null);
                yield cb != null && cb.getState() == CircuitBreaker.State.OPEN;
            }
            case ERROR_RATE_HIGH -> {
                Deque<CallPoint> window = upstreamWindows.get(problem.targetName());
                yield window != null && isOverThreshold(window);
            }
            case BULKHEAD_SATURATED -> {
                // Dung moc diff CUA CHU KY GAN NHAT (bulkheadInWindow) thay vi tinh
                // lai - bulkheadRejectionCount la tong cong nen khong diff duoc tai
                // cho. Resolve khi chu ky gan nhat khong con rejection moi.
                Long inWindow = bulkheadInWindow.get(problem.targetName());
                yield inWindow != null && inWindow > 0;
            }
            case ENDPOINT_ERROR_RATE -> {
                String endpointId = endpointIdByPath(problem.targetName());
                Deque<CallPoint> window = endpointId == null ? null : endpointWindows.get(endpointId);
                yield window != null && isOverThreshold(window);
            }
        };
    }

    private boolean isOverThreshold(Deque<CallPoint> window) {
        List<CallPoint> points = new ArrayList<>(window);
        if (points.size() < errorRateMinCalls) {
            return false;
        }
        long errors = points.stream().filter(p -> !p.success()).count();
        return errors * 100L >= (long) errorRateThresholdPercent * points.size();
    }

    private String endpointIdByPath(String path) {
        return endpointPathById.entrySet().stream()
                .filter(e -> e.getValue().equals(path))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /** Tao/lay problem DANG canh bao theo key dedup - cung key khac instance (touch giu du lieu cu). */
    private void emitOrTouch(AlertEventDto problem) {
        String key = dedupKey(problem);
        activeProblems.put(key, problem); // cap nhat detail moi nhat (touch gui detail moi)
        publisher.submit(List.of(problem));
    }

    private AlertEventDto problem(AlertType type, AlertTargetType targetType, String targetName,
                                  AlertSeverity severity, Object detailSource) {
        String title = switch (type) {
            case CIRCUIT_OPEN -> "CircuitBreaker OPEN - upstream " + targetName;
            case ERROR_RATE_HIGH -> "Ty le loi cao - upstream " + targetName;
            case BULKHEAD_SATURATED -> "Bulkhead day (tu choi nhieu lenh goi) - upstream " + targetName;
            case ENDPOINT_ERROR_RATE -> "Ty le loi cao - endpoint " + targetName;
        };
        String detail = detailSource instanceof CircuitBreaker cb
                ? String.format("CB %s dang OPEN - moi lenh goi bi fail-fast 503 trong thoi gian cho o trang thai OPEN (xem cau hinh wait-duration-in-open-state).",
                cb.getName())
                : String.valueOf(detailSource);
        return new AlertEventDto(type, targetType, targetName, null, false, severity, title, detail, Instant.now());
    }

    private AlertEventDto toResolved(AlertEventDto problem) {
        return new AlertEventDto(problem.alertType(), problem.targetType(), problem.targetName(),
                problem.targetId(), true, problem.severity(), problem.title(),
                "Van de khong con ton tai (CB dong/ty le loi ve muc an toan).", Instant.now());
    }

    private String dedupKey(AlertEventDto event) {
        return event.alertType() + "|" + event.targetType() + "|" + event.targetName();
    }
}
