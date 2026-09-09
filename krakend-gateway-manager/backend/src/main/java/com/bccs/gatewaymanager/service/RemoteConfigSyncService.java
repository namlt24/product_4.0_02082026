package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.ConfigExportDto;
import com.bccs.gatewaymanager.dto.UpstreamServiceDto;
import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor;
import com.bccs.gatewaymanager.entity.UpstreamService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Data-Plane-only (@Profile) - tu dong bo cau hinh Endpoint/Upstream tu
 * Control Plane dung chung QUA HTTP (khong con JPA/Oracle nao ca - xem SAD
 * ADR-07), thay the hoan toan cho co che cu (EndpointRegistryCache/
 * UpstreamRegistryCache tu doc JPA truc tiep khi Control+Data Plane con
 * chung 1 tien trinh).
 *
 * Co che: goi dinh ky GET {controlPlaneBaseUrl}/api/config/export bang chinh
 * api_key CUA DOI MINH (header X-Gateway-Admin-Key) - endpoint nay VON DA tu
 * loc theo doi goi vao (xem ConfigExportImportService.export() +
 * CurrentTeamContext), nen KHONG can API rieng nao cho viec dong bo nay,
 * KHONG can biet team_code cua minh la gi tai tang code (chi can dung DUNG
 * api_key).
 *
 * FAIL-OPEN dung triet ly xuyen suot he thong: Control Plane khong ket noi
 * duoc/loi HTTP chi log canh bao roi GIU NGUYEN cache cu trong bo nho, tu thu
 * lai chu ky ke tiep - KHONG bao gio lam rong cache/chan traffic that dang
 * chay chi vi 1 lan dong bo that bai.
 */
@Slf4j
@Service
@Profile("data-plane")
public class RemoteConfigSyncService {

    private final RestTemplate restTemplate;
    private final String controlPlaneBaseUrl;
    private final String syncApiKey;
    private final int syncIntervalSeconds;
    private final EndpointRegistryCache endpointRegistryCache;
    private final UpstreamRegistryCache upstreamRegistryCache;
    private final UpstreamHttpExecutor upstreamHttpExecutor;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "gwm-config-sync");
        t.setDaemon(true);
        return t;
    });

    /**
     * Ghi nhan snapshot Upstream cua LAN DONG BO GAN NHAT THANH CONG (khoa theo
     * id) - dung de so sanh voi lan ke tiep, CHI invalidate RestTemplate/
     * CircuitBreaker/Retry/Bulkhead (xem UpstreamHttpExecutor) cho DUNG nhung
     * Upstream co cau hinh THAT SU thay doi. Khac voi cach lam "invalidate tat
     * ca moi lan dong bo" (don gian hon nhung SAI): se xoa mat trang thai
     * CircuitBreaker dang OPEN cua 1 Upstream dang that su loi, cho phep
     * traffic tran vao lai ngay ca khi loi chua duoc khac phuc that.
     */
    private volatile Map<String, UpstreamServiceDto> lastKnownUpstreamsById = Map.of();

    /** true = da dong bo THANH CONG it nhat 1 lan tu luc khoi dong - dung lam co so cho readiness probe sau nay (xem plan giai doan trien khai). */
    private volatile boolean everSynced = false;

    public RemoteConfigSyncService(RestTemplateBuilder restTemplateBuilder,
                                    @Value("${gatewaymanager.control-plane.base-url}") String controlPlaneBaseUrl,
                                    @Value("${gatewaymanager.control-plane.sync-api-key}") String syncApiKey,
                                    @Value("${gatewaymanager.control-plane.sync-interval-seconds:15}") int syncIntervalSeconds,
                                    EndpointRegistryCache endpointRegistryCache,
                                    UpstreamRegistryCache upstreamRegistryCache,
                                    UpstreamHttpExecutor upstreamHttpExecutor) {
        // Timeout rieng (khac RestTemplate cua UpstreamHttpExecutor, von phai cau
        // hinh dong theo tung Upstream) - 5s la du rong cho 1 lan goi noi bo toi
        // Control Plane (khac Upstream ben ngoai co the cham), nhung van chan
        // duoc truong hop Control Plane treo hoan toan lam ket thuc thread dong
        // bo vo thoi han.
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5))
                .build();
        // Bo dau "/" thua o cuoi de tranh URL bi lap "//api/config/export" neu
        // nguoi dien .env vo tinh go kem dau "/" cuoi base-url.
        this.controlPlaneBaseUrl = controlPlaneBaseUrl != null && controlPlaneBaseUrl.endsWith("/")
                ? controlPlaneBaseUrl.substring(0, controlPlaneBaseUrl.length() - 1)
                : controlPlaneBaseUrl;
        this.syncApiKey = syncApiKey;
        this.syncIntervalSeconds = syncIntervalSeconds;
        this.endpointRegistryCache = endpointRegistryCache;
        this.upstreamRegistryCache = upstreamRegistryCache;
        this.upstreamHttpExecutor = upstreamHttpExecutor;
    }

    @PostConstruct
    void start() {
        // initialDelay=0: co gang dong bo NGAY luc khoi dong (truoc khi nhan
        // traffic that) thay vi cho het 1 chu ky - giam thoi gian Pod "song
        // nhung rong cache" luc vua len.
        scheduler.scheduleWithFixedDelay(this::sync, 0, syncIntervalSeconds, TimeUnit.SECONDS);
        log.info("Dong bo cau hinh tu Control Plane da BAT - {} moi {}s.", controlPlaneBaseUrl, syncIntervalSeconds);
    }

    @PreDestroy
    void stop() {
        scheduler.shutdown();
    }

    public boolean isReady() {
        return everSynced;
    }

    /** Package-private (khong phai private) de test goi truc tiep thay vi phai cho scheduler. */
    void sync() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Gateway-Admin-Key", syncApiKey);
            ResponseEntity<ConfigExportDto> response = restTemplate.exchange(
                    controlPlaneBaseUrl + "/api/config/export", HttpMethod.GET,
                    new HttpEntity<>(headers), ConfigExportDto.class);
            ConfigExportDto bundle = response.getBody();
            if (bundle == null) {
                log.warn("Dong bo cau hinh: Control Plane tra ve body rong, bo qua lan nay.");
                return;
            }

            invalidateChangedUpstreams(bundle.upstreams());
            upstreamRegistryCache.reload(bundle.upstreams().stream().map(this::toEntity).toList());
            endpointRegistryCache.reload(bundle.endpoints());

            Map<String, UpstreamServiceDto> fresh = new HashMap<>();
            bundle.upstreams().forEach(u -> fresh.put(u.id(), u));
            this.lastKnownUpstreamsById = Map.copyOf(fresh);

            everSynced = true;
            log.info("Da dong bo cau hinh tu Control Plane: {} upstream, {} endpoint.",
                    bundle.upstreams().size(), bundle.endpoints().size());
        } catch (Exception e) {
            log.warn("Loi dong bo cau hinh tu Control Plane (giu nguyen cache cu, KHONG anh huong traffic dang chay, se thu lai sau {}s): {}: {}",
                    syncIntervalSeconds, e.getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * Chi invalidate RestTemplate/CircuitBreaker/Retry/Bulkhead (xem
     * UpstreamHttpExecutor.invalidate()) cho Upstream co cau hinh anh huong
     * resilience THAT SU thay doi so voi lan dong bo truoc - mirror dung logic
     * UpstreamServiceService.update() da lam khi con chung 1 tien trinh, chi
     * khac la o day phat hien qua so sanh 2 snapshot thay vi biet truoc "vua
     * sua cai gi".
     */
    private void invalidateChangedUpstreams(List<UpstreamServiceDto> fresh) {
        for (UpstreamServiceDto u : fresh) {
            UpstreamServiceDto old = lastKnownUpstreamsById.get(u.id());
            if (old == null) {
                continue; // Upstream moi - chua co gi de invalidate.
            }
            if (resilienceConfigChanged(old, u)) {
                upstreamHttpExecutor.invalidate(old.name());
                if (!old.name().equals(u.name())) {
                    upstreamHttpExecutor.invalidate(u.name());
                }
            }
        }
    }

    private boolean resilienceConfigChanged(UpstreamServiceDto a, UpstreamServiceDto b) {
        return !a.name().equals(b.name())
                || a.connectTimeoutMs() != b.connectTimeoutMs()
                || a.readTimeoutMs() != b.readTimeoutMs()
                || a.circuitBreakerEnabled() != b.circuitBreakerEnabled()
                || a.failureRateThreshold() != b.failureRateThreshold()
                || a.retryEnabled() != b.retryEnabled()
                || a.maxConcurrentCalls() != b.maxConcurrentCalls()
                || a.maxWaitDurationMs() != b.maxWaitDurationMs();
    }

    private UpstreamService toEntity(UpstreamServiceDto dto) {
        return UpstreamService.builder()
                .id(dto.id())
                .name(dto.name())
                .description(dto.description())
                .baseHost(dto.baseHost())
                .connectTimeoutMs(dto.connectTimeoutMs())
                .readTimeoutMs(dto.readTimeoutMs())
                .circuitBreakerEnabled(dto.circuitBreakerEnabled())
                .failureRateThreshold(dto.failureRateThreshold())
                .retryEnabled(dto.retryEnabled())
                .maxConcurrentCalls(dto.maxConcurrentCalls())
                .maxWaitDurationMs(dto.maxWaitDurationMs())
                .build();
    }
}
