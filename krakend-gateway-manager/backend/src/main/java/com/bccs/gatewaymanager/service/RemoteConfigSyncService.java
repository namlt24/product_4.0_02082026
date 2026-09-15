package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.cache.GatewayCacheService;
import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.ConfigExportDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
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
import org.springframework.http.HttpStatus;
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
    private final GatewayCacheService cacheService;

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

    /**
     * Mirror dung y het lastKnownUpstreamsById nhung cho Endpoint - dung de phat
     * hien cau hinh cache (BackendStep.cacheEnabled/EndpointConfig.responseCacheEnabled...)
     * doi giua 2 lan dong bo, xem invalidateChangedEndpointCaches(). Khong dung
     * chung 1 field voi Upstream vi 2 loai du lieu khac nhau hoan toan (endpoint
     * co danh sach step long nhau, khong the gom vao 1 Map<String, ?> duy nhat
     * ma van giu type-safe don gian).
     */
    private volatile Map<String, EndpointResponseDto> lastKnownEndpointsById = Map.of();

    /** true = da dong bo THANH CONG it nhat 1 lan tu luc khoi dong - dung lam co so cho readiness probe sau nay (xem plan giai doan trien khai). */
    private volatile boolean everSynced = false;

    /**
     * ETag (dau van tay config) cua lan export THANH CONG gan nhat - gui lai
     * trong header If-None-Match o chu ky ke tiep de CP tra 304 Not Modified
     * neu config khong doi (99% chu ky), thay vi truyen lai toan bo payload
     * (hang tram KB khi 1 doi co nhieu endpoint). null = chua tung export thanh
     * cong tu luc khoi dong -> chu ky dau tien luon nhan 200 day du. Volatile
     * de thread scheduler doc gia tri moi nhat ghi boi chinh no (thuc te 1
     * thread duy nhat - ghi ra day la khai bao the hien y dinh).
     */
    private volatile String lastEtag;

    public RemoteConfigSyncService(RestTemplateBuilder restTemplateBuilder,
                                    @Value("${gatewaymanager.control-plane.base-url}") String controlPlaneBaseUrl,
                                    @Value("${gatewaymanager.control-plane.sync-api-key}") String syncApiKey,
                                    @Value("${gatewaymanager.control-plane.sync-interval-seconds:15}") int syncIntervalSeconds,
                                    EndpointRegistryCache endpointRegistryCache,
                                    UpstreamRegistryCache upstreamRegistryCache,
                                    UpstreamHttpExecutor upstreamHttpExecutor,
                                    GatewayCacheService cacheService) {
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
        this.cacheService = cacheService;
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
            // Conditional request (RFC 9110): neu co ETag cua lan thanh cong gan
            // nhat thi mang lai - CP so voi dau van tay config hien tai, khong
            // doi thi tra 304 (body rong), doi thi tra 200 day du + ETag moi.
            if (lastEtag != null) {
                headers.set(HttpHeaders.IF_NONE_MATCH, lastEtag);
            }
            ResponseEntity<ConfigExportDto> response = restTemplate.exchange(
                    controlPlaneBaseUrl + "/api/config/export", HttpMethod.GET,
                    new HttpEntity<>(headers), ConfigExportDto.class);
            // 304: DefaultResponseErrorHandler cua RestTemplate CHI nem exception
            // cho 4xx/5xx nen 304 di qua nhu response thuong voi body null - phai
            // xu ly TRUOC khoi check bundle==null ben duoi (304 khong phai "body
            // rong bat thuong"). Config chua doi -> giu nguyen cache, chu ky sau
            // van gui lai If-None-Match cung ETag cu.
            if (response.getStatusCode() == HttpStatus.NOT_MODIFIED) {
                everSynced = true;
                log.debug("Dong bo cau hinh: config Control Plane khong thay doi (304), giu nguyen cache.");
                return;
            }
            ConfigExportDto bundle = response.getBody();
            if (bundle == null) {
                log.warn("Dong bo cau hinh: Control Plane tra ve body rong, bo qua lan nay.");
                return;
            }

            invalidateChangedUpstreams(bundle.upstreams());
            invalidateChangedEndpointCaches(bundle.endpoints());
            invalidateChangedStepBulkheads(bundle.endpoints());
            upstreamRegistryCache.reload(bundle.upstreams().stream().map(this::toEntity).toList());
            endpointRegistryCache.reload(bundle.endpoints());

            Map<String, UpstreamServiceDto> fresh = new HashMap<>();
            bundle.upstreams().forEach(u -> fresh.put(u.id(), u));
            this.lastKnownUpstreamsById = Map.copyOf(fresh);

            Map<String, EndpointResponseDto> freshEndpoints = new HashMap<>();
            bundle.endpoints().forEach(e -> freshEndpoints.put(e.id(), e));
            this.lastKnownEndpointsById = Map.copyOf(freshEndpoints);

            everSynced = true;
            // Luu ETag moi de chu ky ke tiep thanh conditional request (304 neu khong doi).
            this.lastEtag = response.getHeaders().getETag();
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

    /**
     * Mirror dung y het invalidateChangedUpstreams() nhung don cache Redis CUA
     * CHINH DATA PLANE NAY (thay vi invalidate Resilience4j) - xem bug that:
     * bat cache (response loi bi cache) -> tat cache -> BAT LAI cache -> loi
     * tai xuat hien vi key cache Redis KHONG doi khi cacheEnabled doi, entry cu
     * (mang response loi) van con nguyen toi khi het TTL. Control Plane da tu
     * don Redis cua CHINH NO luc luu (xem EndpointService.purgeStaleCacheIfChanged())
     * nhung Data Plane co Redis RIENG, khac mang/khac tien trinh - phai tu phat
     * hien qua so sanh 2 snapshot dong bo lien tiep (dung CACH DUY NHAT hop kien
     * truc poll hien co - xem ADR-07, khong co kenh push truc tiep tu CP).
     */
    private void invalidateChangedEndpointCaches(List<EndpointResponseDto> fresh) {
        for (EndpointResponseDto ep : fresh) {
            EndpointResponseDto old = lastKnownEndpointsById.get(ep.id());
            if (old == null) {
                continue; // Endpoint moi - chua co gi duoc cache duoi id nay.
            }
            boolean responseCacheKeyMayChange = !old.path().equals(ep.path()) || old.method() != ep.method();
            if (old.responseCacheEnabled() != ep.responseCacheEnabled()
                    || ((old.responseCacheEnabled() || ep.responseCacheEnabled()) && responseCacheKeyMayChange)) {
                cacheService.deleteByPattern("gwm:response-cache:" + ep.id() + ":*");
            }

            Map<Integer, BackendStepDto> oldByOrder = new HashMap<>();
            for (BackendStepDto s : old.steps()) {
                oldByOrder.put(s.stepOrder(), s);
            }
            for (BackendStepDto after : ep.steps()) {
                BackendStepDto before = oldByOrder.get(after.stepOrder());
                if (before == null) {
                    continue;
                }
                boolean cacheKeyMayChange = !before.upstreamServiceName().equals(after.upstreamServiceName())
                        || before.method() != after.method() || !before.urlPattern().equals(after.urlPattern());
                if (before.cacheEnabled() != after.cacheEnabled()
                        || ((before.cacheEnabled() || after.cacheEnabled()) && cacheKeyMayChange)) {
                    cacheService.deleteByPattern("gwm:cache:" + before.upstreamServiceName() + ":" + before.method() + ":*");
                    if (!before.upstreamServiceName().equals(after.upstreamServiceName()) || before.method() != after.method()) {
                        cacheService.deleteByPattern("gwm:cache:" + after.upstreamServiceName() + ":" + after.method() + ":*");
                    }
                }
            }
        }
    }

    /**
     * Mirror dung y het invalidateChangedEndpointCaches() nhung don Bulkhead RIENG theo
     * step (xem BackendStep.maxConcurrentCallsOverride, UpstreamHttpExecutor.bulkheadFor())
     * thay vi cache Redis - Data Plane cung phai tu phat hien qua so sanh 2 snapshot dong
     * bo lien tiep (khong co kenh push truc tiep tu CP, xem ADR-07), neu khong doi
     * override tren Control Plane se KHONG co hieu luc tren Data Plane cho toi khi restart pod.
     */
    private void invalidateChangedStepBulkheads(List<EndpointResponseDto> fresh) {
        for (EndpointResponseDto ep : fresh) {
            EndpointResponseDto old = lastKnownEndpointsById.get(ep.id());
            if (old == null) {
                continue; // Endpoint moi - chua co gi de don.
            }
            Map<Integer, BackendStepDto> oldByOrder = new HashMap<>();
            for (BackendStepDto s : old.steps()) {
                oldByOrder.put(s.stepOrder(), s);
            }
            for (BackendStepDto after : ep.steps()) {
                BackendStepDto before = oldByOrder.get(after.stepOrder());
                if (before == null) {
                    continue;
                }
                boolean overrideChanged = !java.util.Objects.equals(before.maxConcurrentCallsOverride(), after.maxConcurrentCallsOverride())
                        || !java.util.Objects.equals(before.maxWaitDurationMsOverride(), after.maxWaitDurationMsOverride());
                boolean upstreamChangedWithOldOverride = before.maxConcurrentCallsOverride() != null
                        && !before.upstreamServiceName().equals(after.upstreamServiceName());
                if (overrideChanged || upstreamChangedWithOldOverride) {
                    upstreamHttpExecutor.invalidateStepBulkhead(before.upstreamServiceName(), before.id());
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
