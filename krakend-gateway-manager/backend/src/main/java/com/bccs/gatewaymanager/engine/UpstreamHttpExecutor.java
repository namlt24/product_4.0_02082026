package com.bccs.gatewaymanager.engine;

import com.bccs.gatewaymanager.audit.AuditLogService;
import com.bccs.gatewaymanager.audit.BodyTruncator;
import com.bccs.gatewaymanager.audit.HopAuditEvent;
import com.bccs.gatewaymanager.cache.GatewayCacheService;
import com.bccs.gatewaymanager.dto.StepTraceDto;
import com.bccs.gatewaymanager.entity.UpstreamService;
import com.bccs.gatewaymanager.exception.SystemException;
import com.bccs.gatewaymanager.service.AlertCallObserver;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Thuc su thuc hien 1 lan goi HTTP ra Upstream, boc trong Redis cache-aside
 * (chi cho GET/POST + cacheEnabled cua tung BackendStep - xem tham so call(),
 * KHONG phai cua UpstreamService, vi 1 Upstream bi nhieu step goi toi nhieu ham/
 * path khac nhau, khong phai ham nao cung nen cache; PUT/PATCH/DELETE luon bi
 * chan cache vi gan nhu chac chan la mutating - xem GatewayCacheService.buildKey()
 * ve cach key phan biet theo body cho POST) va Resilience4j CircuitBreaker/
 * Retry/Bulkhead - rieng 3 cai nay van dat TEN THEO upstream.getName(), tao
 * dong tai lan goi dau (khong the dung @CircuitBreaker/@Cacheable tinh vi
 * backend duoc chon dong theo cau hinh DB, khong phai theo chu ky method co dinh).
 *
 * HTTP client: Apache HttpClient 5 POOLING (SIZING 1000 RPS, 2026-09-14 - thay
 * cho SimpleClientHttpRequestFactory/JDK HttpURLConnection mac dinh cua
 * RestTemplateBuilder, moi request mo ket noi moi roi dong - can port ephemeral
 * o 1000 RPS). Ket noi giu san sang giua cac request, so ket noi thuc te = so
 * request DONG THOI (Little's Law ~500/pod voi mean 1.5s), khong phai rps. Moi
 * (upstream, connectMs, readMs) la 1 pool rieng vi HttpClient 5 khong cho doi
 * ConnectionConfig (timeout) theo request tren 1 manager dung chung - xem
 * restTemplateFor(). Moi pool/thread nay TU DONG dong lai neu khong ai dung
 * toi qua gatewaymanager.upstream-http.idle-eviction-minutes phut (xem
 * sweepIdleClients()) - can thiet vi tap key co the phat sinh KHONG GIOI HAN
 * qua "Thu nhanh" (nguoi dung go tay connectTimeoutMs/readTimeoutMs).
 *
 * Thu tu boc decorator (dung khuyen nghi Resilience4j): Bulkhead trong cung,
 * CircuitBreaker boc ngoai Bulkhead, Retry boc ngoai cung - de moi lan retry
 * deu di qua kiem tra circuit breaker/bulkhead, khong bo qua.
 */
@Slf4j
@Component
public class UpstreamHttpExecutor {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final BulkheadRegistry bulkheadRegistry;
    private final GatewayCacheService cacheService;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;
    /** Gom so lieu cho canh bao (AlertDetector o DP, no-op o CP) - xem AlertCallObserver. */
    private final AlertCallObserver alertCallObserver;

    /**
     * Kich thuoc connection pool (SIZING 1000 RPS, 2026-09-14) - KHONG hardcode
     * trong code: doc tu env GATEWAY_HTTP_POOL_MAX_TOTAL / _MAX_PER_ROUTE (mac
     * dinh 600/550, xem gatewaymanager.upstream-http trong application.yml) de
     * scale up (2000 RPS...) chi can doi configmap + rollout restart, khong phai
     * build lai image. So ket noi thuc te = so request DONG THOI (Little's Law),
     * khong phai rps.
     */
    private final int poolMaxTotal;
    private final int poolMaxPerRoute;

    /**
     * So phut KHONG duoc dung toi truoc khi 1 (upstream, connectMs, readMs) tu
     * dong bi don don (xem sweepIdleClients()) - CHAN ro ri thread/connection-pool
     * that su: tu khi doi sang Apache HttpClient5 pooling (2026-09-14), moi key
     * MOI la 1 CloseableHttpClient rieng mang theo 1 thread evictIdleConnections
     * THAT + 1 pool ket noi - khac han truoc day khi moi key chi la 1 RestTemplate
     * "re" (JDK HttpURLConnection, khong thread). invalidate() chi don khi Upstream
     * doi ten/xoa - KHONG chay khi chi doi override connectTimeoutMs/readTimeoutMs
     * cua rieng 1 BackendStep (EndpointService khong goi invalidate()), va "Thu
     * nhanh" (EndpointTryService.tryAdhoc()) cho nguoi dung go tay 2 gia tri nay
     * TU DO (BackendStepDto @Min/@Max, khong gioi han tap gia tri) moi lan thu -
     * neu khong tu don theo thoi gian idle, moi lan thu 1 gia tri moi la 1 thread
     * ro ri VINH VIEN cho toi khi restart app.
     */
    private final int idleEvictionMinutes;

    /**
     * Constructor tu sinh (Lombok @RequiredArgsConstructor) khong biet map field
     * voi property nao - khai bao @Value ro rang de Spring inject gia tri tu
     * application.yml (gatewaymanager.upstream-http) khi khoi tao bean.
     */
    public UpstreamHttpExecutor(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            BulkheadRegistry bulkheadRegistry,
            GatewayCacheService cacheService,
            ObjectMapper objectMapper,
            AuditLogService auditLogService,
            AlertCallObserver alertCallObserver,
            @Value("${gatewaymanager.upstream-http.pool-max-total:600}") int poolMaxTotal,
            @Value("${gatewaymanager.upstream-http.pool-max-per-route:550}") int poolMaxPerRoute,
            @Value("${gatewaymanager.upstream-http.idle-eviction-minutes:15}") int idleEvictionMinutes) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.bulkheadRegistry = bulkheadRegistry;
        this.cacheService = cacheService;
        this.objectMapper = objectMapper;
        this.auditLogService = auditLogService;
        this.alertCallObserver = alertCallObserver;
        this.poolMaxTotal = poolMaxTotal;
        this.poolMaxPerRoute = poolMaxPerRoute;
        this.idleEvictionMinutes = idleEvictionMinutes;
    }

    private final Map<RestTemplateKey, RestTemplate> restTemplateCache = new ConcurrentHashMap<>();

    /**
     * Track client pool theo key de DONG LAI khi invalidate()/sweepIdleClients()/
     * shutdown - bat buoc vi moi pooled client co thread evictIdleConnections
     * rieng: khong close thi moi lan update Upstream (invalidate + rebuild cung
     * key) hoac moi key idle qua han la ro ri 1 thread + cac ket noi con mo.
     */
    private final Map<RestTemplateKey, CloseableHttpClient> pooledClients = new ConcurrentHashMap<>();

    /**
     * Thoi diem restTemplateFor() tra ve entry nay GAN NHAT (ca cache-hit lan
     * cache-miss) - dung de sweepIdleClients() biet key nao da lau khong ai dung
     * toi. Rieng biet voi 30s evictIdleConnections cua CHINH pool (cai do chi don
     * KET NOI TCP idle ben trong 1 pool dang song, khong dong ca pool/thread).
     */
    private final Map<RestTemplateKey, Instant> lastUsedAt = new ConcurrentHashMap<>();

    private final ScheduledExecutorService idleSweeper = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "gwm-http-pool-sweeper");
        t.setDaemon(true);
        return t;
    });

    @PostConstruct
    void startIdleSweeper() {
        // Chay thua hon nhieu so voi nguong idle (toi thieu 5 phut/lan, khong phu
        // thuoc idleEvictionMinutes cau hinh nho) - don pool/thread la thao tac re,
        // khong can chinh xac tuyet doi, chi can khong de ro ri vo han.
        long periodMinutes = Math.max(1, Math.min(5, idleEvictionMinutes));
        idleSweeper.scheduleWithFixedDelay(this::sweepIdleClients, periodMinutes, periodMinutes, TimeUnit.MINUTES);
        log.info("Don dep HTTP client pool idle da BAT - dong pool/thread khong dung toi qua {} phut, kiem tra moi {} phut.",
                idleEvictionMinutes, periodMinutes);
    }

    /**
     * Dong + xoa cac (upstream, connectMs, readMs) khong duoc restTemplateFor()
     * dung toi qua idleEvictionMinutes phut - chu yeu nham vao cac to hop phat
     * sinh tu "Thu nhanh" (nguoi dung go tay timeout, thu roi bo, khong bao gio
     * quay lai gia tri cu) ma invalidate() khong bao gio dung toi (xem javadoc
     * idleEvictionMinutes). RUI RO CHAP NHAN DUOC: neu 1 request MOI vua bat dau
     * dung dung 1 key GIA (lastUsedAt chua kip cap nhat) ngay luc sweep dang dong
     * client do, request nay se that bai 1 lan (loi ket noi) - nhung
     * Retry.decorateSupplier trong call() se tu thu lai NGAY (retryOnException ->
     * true cho loi ket noi chung), va lan retry se goi lai restTemplateFor() nen
     * tu tao lai entry moi tinh - tu phuc hoi, khong can xu ly gi them.
     */
    private void sweepIdleClients() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(idleEvictionMinutes));
        lastUsedAt.entrySet().removeIf(e -> {
            if (e.getValue().isAfter(cutoff)) {
                return false;
            }
            RestTemplateKey key = e.getKey();
            restTemplateCache.remove(key);
            CloseableHttpClient client = pooledClients.remove(key);
            if (client != null) {
                try {
                    client.close();
                } catch (Exception ex) {
                    log.warn("Loi dong pooled HttpClient idle qua han cho key={} (bo qua): {}", key, ex.getMessage());
                }
                log.debug("Da dong HTTP client pool idle qua {} phut: {}", idleEvictionMinutes, key);
            }
            return true;
        });
    }

    @PreDestroy
    void shutdownHttpClients() {
        idleSweeper.shutdown();
        pooledClients.values().forEach(c -> {
            try {
                c.close();
            } catch (Exception e) {
                log.warn("Loi dong pooled HttpClient khi shutdown (bo qua): {}", e.getMessage());
            }
        });
    }

    /**
     * RestTemplate cua Spring bake connectTimeout/readTimeout NGAY LUC BUILD qua
     * RestTemplateBuilder (khong sua duoc per-request) - nen key cache phai gom
     * ca 2 gia tri timeout HIEU LUC (khong chi ten upstream) de 1 BackendStep co
     * the override rieng ma khong anh huong RestTemplate dung chung cua cac step
     * khac cung Upstream nhung khong override (xem restTemplateFor()).
     */
    private record RestTemplateKey(String upstreamName, int connectMs, int readMs) {
    }

    // Dem hit/miss cache THEO TEN UPSTREAM (khong theo tung key rieng - qua nhieu key
    // se ton bo nho vo ich) - phuc vu man hinh "Dashboard suc khoe Upstream". Dat o day
    // (khong phai GatewayCacheService) vi upstream.getName() da co san tai day, tranh
    // GatewayCacheService phai parse nguoc ten upstream tu chuoi key.
    private final Map<String, LongAdder> cacheHits = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> cacheMisses = new ConcurrentHashMap<>();

    /**
     * Dem dong so lan 1 Bulkhead tu choi lenh goi (day cho, het maxWaitDurationMs) - mirror
     * dung pattern cacheHits/cacheMisses o tren, phuc vu CUNG man hinh "Dashboard suc khoe
     * Upstream". Key la TEN CUA CHINH BULKHEAD bi day (co the la ten Upstream cho bulkhead
     * dung chung, hoac "upstreamName#step:stepId" cho bulkhead rieng theo step - xem
     * bulkheadFor()) - ghi tu ResilienceConfig (lang nghe Bulkhead.onCallRejected() cua MOI
     * bulkhead dang ky, dat o do vi la noi da co san co che lang nghe tuong tu cho
     * CircuitBreaker.onStateTransition()), KHONG phai tu day, de giu call() khong doi gi.
     */
    private final Map<String, LongAdder> bulkheadRejections = new ConcurrentHashMap<>();

    /** Goi tu ResilienceConfig moi khi 1 Bulkhead (bat ky ten nao) tu choi 1 lenh goi. */
    public void recordBulkheadRejection(String bulkheadName) {
        bulkheadRejections.computeIfAbsent(bulkheadName, n -> new LongAdder()).increment();
    }

    /** Dung cho UpstreamHealthService - 0 neu bulkhead nay chua tung tu choi lenh goi nao. */
    public long bulkheadRejectionCount(String bulkheadName) {
        LongAdder a = bulkheadRejections.get(bulkheadName);
        return a == null ? 0 : a.sum();
    }

    /** Ket qua 1 lan goi HTTP THAT (khac cache-hit) - giu ca status vi ban than JsonNode khong mang status. */
    private record HttpCallResult(int status, JsonNode body) {
    }

    /**
     * @param stepOrder/stepName chi de ghi audit log (xem HopAuditEvent) - KHONG anh huong hanh vi goi.
     * @param stepConnectTimeoutMs/stepReadTimeoutMs override rieng cua BackendStep - null = dung
     *        dung mac dinh cua UpstreamService (xem BackendStep.connectTimeoutMs/readTimeoutMs).
     * @param stepId id CUA CHINH step (khong phai upstream) - CHI dung lam mot phan khoa Bulkhead
     *        rieng khi stepMaxConcurrentCallsOverride khac null (xem bulkheadFor()); truyen null khi
     *        khong dung override (vd loi goi bu tru - executeCompensationStep()) vi luc do khong doc
     *        toi field nay.
     * @param stepMaxConcurrentCallsOverride/stepMaxWaitDurationMsOverride override Bulkhead RIENG
     *        cua BackendStep - null (ca 2) = dung chung Bulkhead cua UpstreamService (hanh vi cu,
     *        khong doi - xem BackendStep.maxConcurrentCallsOverride).
     */
    public JsonNode call(UpstreamService upstream, HttpMethod method, String resolvedUrl,
                          HttpHeaders headers, JsonNode body, boolean cacheEnabled, int cacheTtlSeconds,
                          int stepOrder, String stepName, Integer stepConnectTimeoutMs, Integer stepReadTimeoutMs,
                          String stepId, Integer stepMaxConcurrentCallsOverride, Integer stepMaxWaitDurationMsOverride) {
        long startNanos = System.nanoTime();
        // POST duoc cho cache TU 2026-09 (truoc do chi GET) - dung cho API tim kiem/tra
        // cuu truyen filter qua body (POST khong sua/tao du lieu that su). PUT/PATCH/DELETE
        // VAN luon bi chan cache vi gan nhu chac chan la mutating. Rui ro cache nham 1 lenh
        // goi co side-effect (tra ket qua cu thay vi thuc thi that) do NGUOI QUAN LY cau hinh
        // (nguoi bat cacheEnabled cho tung BackendStep) tu chiu trach nhiem - he thong KHONG
        // con chan cung theo method nhu truoc, chi con chan PUT/PATCH/DELETE.
        boolean cacheable = cacheEnabled && (method == HttpMethod.GET || method == HttpMethod.POST);
        // traceId phai BOM VAO SAU khi tinh cacheKey (va khong ap dung tren nhanh
        // cache-hit) - traceId la du lieu QUAN SAT (moi request khac traceId),
        // khong phai dau vao nghiep vu: neu vao key cache thi 2 request cung tham
        // so khac traceId ra 2 key khac nhau, cache per-step tro nen vo nghia.
        String cacheKey = cacheable ? GatewayCacheService.buildKey(upstream.getName(), method.name(), resolvedUrl, body) : null;
        String traceId = MDC.get(TRACE_ID_MDC_KEY);

        // Bien tam de xay HopAuditEvent trong finally, du di theo nhanh nao (cache-hit/
        // that-cong/loi) - JAVA khong cho bien local final duoc gan lai trong try/catch,
        // nen khai bao thuong (mutable) o day.
        boolean cacheHit = false;
        Integer responseStatus = null;
        String responseBodyForAudit = null;
        String errorMessage = null;
        boolean success = true;
        // Set trong catch: loi nghiep vu 4xx (isBusinessError) KHONG tinh la failure cho
        // canh bao — upstream van khoe, chi client goi sai (dong bo voi CB ignoreException).
        boolean businessError = false;

        try {
            if (cacheable) {
                Optional<String> cached = cacheService.get(cacheKey);
                if (cached.isPresent()) {
                    try {
                        cacheHits.computeIfAbsent(upstream.getName(), n -> new LongAdder()).increment();
                        cacheHit = true;
                        responseBodyForAudit = cached.get();
                        return objectMapper.readTree(cached.get());
                    } catch (Exception e) {
                        log.warn("Cache gia tri hong cho key={}, bo qua cache: {}", cacheKey, e.getMessage());
                        cacheHit = false;
                    }
                } else {
                    cacheMisses.computeIfAbsent(upstream.getName(), n -> new LongAdder()).increment();
                }
            }

            // Chi toi day (cache miss, se goi HTTP that) moi bom traceId vao body/
            // header - tren nhanh cache-hit khong goi upstream nen khong co "xuong
            // backend step" de mang traceId theo.
            JsonNode outboundBody = injectTraceId(body, traceId);
            Supplier<HttpCallResult> callSupplier = () ->
                    doHttpCall(upstream, method, resolvedUrl, headers, outboundBody, traceId, stepConnectTimeoutMs, stepReadTimeoutMs);
            Supplier<HttpCallResult> decorated = Bulkhead.decorateSupplier(
                    bulkheadFor(upstream, stepId, stepMaxConcurrentCallsOverride, stepMaxWaitDurationMsOverride), callSupplier);
            if (upstream.isCircuitBreakerEnabled()) {
                decorated = CircuitBreaker.decorateSupplier(circuitBreakerFor(upstream), decorated);
            }
            if (upstream.isRetryEnabled()) {
                decorated = Retry.decorateSupplier(retryFor(upstream), decorated);
            }

            HttpCallResult callResult = decorated.get();
            responseStatus = callResult.status();
            responseBodyForAudit = callResult.body().toString();

            if (cacheable) {
                cacheService.put(cacheKey, callResult.body().toString(), cacheTtlSeconds);
            }
            return callResult.body();
        } catch (RuntimeException e) {
            success = false;
            errorMessage = e.getMessage();
            if (e instanceof UpstreamHttpErrorException httpError) {
                responseStatus = httpError.httpStatus();
                responseBodyForAudit = httpError.responseBody();
                if (isBusinessError(httpError)) {
                    businessError = true;
                }
            }
            throw e;
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            BodyTruncator.Result requestBodyResult = BodyTruncator.truncate(body == null ? null : body.toString());
            BodyTruncator.Result responseBodyResult = BodyTruncator.truncate(responseBodyForAudit);
            HopAuditEvent event = new HopAuditEvent(
                    MDC.get("requestId"), stepOrder, stepName, upstream.getName(), method.name(), resolvedUrl,
                    requestBodyResult.body(), requestBodyResult.truncated(),
                    responseStatus,
                    responseBodyResult.body(), responseBodyResult.truncated(),
                    durationMs, cacheHit, success, errorMessage, Instant.now());
            auditLogService.recordHop(event);

            // Gom so lieu canh bao (passive, implementation tu nuot exception) - giong
            // recordHop o tren, KHONG bao gio anh huong traffic that.
            // Loi nghiep vu 4xx khong tinh la failure (upstream van khoe — dung logic
            // voi ignoreException cua CB, khong thi chuoi client khong dang ky ->
            // ERROR_RATE_HIGH gia cho upstream khong loi gi).
            alertCallObserver.recordUpstreamCall(upstream.getName(), success || businessError);

            // "Thu nhanh" (xem TraceCollector) - no-op ngoai che do do (Data Plane
            // that khong bao gio goi TraceCollector.start()), tai su dung dung cac
            // gia tri vua tinh o tren cho HopAuditEvent, khong tinh toan them.
            TraceCollector.record(new StepTraceDto(stepOrder, stepName, upstream.getName(), method.name(), resolvedUrl,
                    requestBodyResult.body(), requestBodyResult.truncated(),
                    responseStatus,
                    responseBodyResult.body(), responseBodyResult.truncated(),
                    durationMs, cacheHit, success, errorMessage));
        }
    }

    private HttpCallResult doHttpCall(UpstreamService upstream, HttpMethod method, String resolvedUrl,
                                       HttpHeaders headers, JsonNode body, String traceId,
                                       Integer stepConnectTimeoutMs, Integer stepReadTimeoutMs) {
        RestTemplate restTemplate = restTemplateFor(upstream, stepConnectTimeoutMs, stepReadTimeoutMs);
        try {
            String bodyString = body == null ? null : body.toString();
            HttpHeaders callHeaders = withTraceIdHeader(headers, traceId);
            ResponseEntity<String> response = restTemplate.exchange(
                    resolvedUrl, method, new HttpEntity<>(bodyString, callHeaders), String.class);
            String responseBody = response.getBody();
            JsonNode parsed = (responseBody == null || responseBody.isBlank())
                    ? objectMapper.createObjectNode()
                    : objectMapper.readTree(responseBody);
            return new HttpCallResult(response.getStatusCode().value(), parsed);
        } catch (RestClientResponseException e) {
            // Upstream tra ve HTTP loi that su (4xx/5xx) - giu nguyen status/body de client
            // phan biet duoc "input sai"/"upstream bao loi nghiep vu" thay vi 1 mã 500 chung.
            log.warn("Upstream '{}' [{} {}] tra ve HTTP {}: {}", upstream.getName(), method, resolvedUrl,
                    e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new UpstreamHttpErrorException(upstream.getName(), e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            // Connect timeout / read timeout / connection refused - ha tang that su co van
            // de, khac han "upstream tra loi 4xx/5xx" o tren.
            log.warn("Loi ket noi Upstream '{}' [{} {}]: {}", upstream.getName(), method, resolvedUrl, e.getMessage());
            throw new UpstreamTimeoutException(upstream.getName(), e);
        } catch (Exception e) {
            log.warn("Loi goi Upstream '{}' [{} {}]: {}", upstream.getName(), method, resolvedUrl, e.getMessage());
            throw new UpstreamCallException(upstream.getName(), e);
        }
    }

    /**
     * Key MDC cua traceId do DynamicDispatcherController bat tu header X-Trace-Id
     * (Gravitee truyen xuong) - MDC la ThreadLocal NHUNG CompositeOrchestratorEngine
     * da copy MDC sang worker thread cua parallelStepExecutor (xem javadoc
     * executeStepsInParallel) nen doc duoc an toan o day, giong dung pattern
     * MDC.get("requestId") o finally cua call().
     */
    static final String TRACE_ID_MDC_KEY = "traceId";

    /**
     * Dong nhat traceId sang Upstream theo CA 2 kenh: (1) field "traceId" TRONG
     * body JSON - cac backend BCCS doc traceId tu body (da xac nhan thuc te tren
     * he thong chung); (2) header "X-Trace-Id" (xem withTraceIdHeader trong
     * doHttpCall) cho he thong doc theo hop dong header. CHI bom vao body da ton
     * tai la JSON object (vi du POST forwardOriginalBody) - KHONG tu tao body moi
     * (GET khong body se bat ngo mang body, nhieu server tu choi) va KHONG ghi de
     * field traceId client da forward san qua forwardOriginalBody. Body array/
     * gia tri don khong bom duoc field - chi con kenh header. DEEP-COPY truoc khi
     * them field: body co the la chinh step result dang luu trong ExecutionContext
     * (mapping $body tu STEP_RESPONSE) - mutate thang se lan traceId vao ket qua
     * step do, step khac (chay song song) doc cung node co the bi race/leak
     * traceId vao response cuoi.
     */
    private JsonNode injectTraceId(JsonNode body, String traceId) {
        if (traceId == null || traceId.isBlank() || body == null || !body.isObject()) {
            return body;
        }
        ObjectNode obj = ((ObjectNode) body).deepCopy();
        if (obj.get("traceId") == null) {
            obj.put("traceId", traceId);
        }
        return obj;
    }

    private HttpHeaders withTraceIdHeader(HttpHeaders headers, String traceId) {
        if (traceId == null || traceId.isBlank()) {
            return headers;
        }
        HttpHeaders out = new HttpHeaders();
        out.putAll(headers);
        out.set("X-Trace-Id", traceId);
        return out;
    }

    /**
     * Xoa RestTemplate/CircuitBreaker/Retry/Bulkhead da tao san cho 1 Upstream (theo
     * ten) - goi khi UpstreamServiceService.update()/delete() doi cau hinh, vi
     * cac cache/registry o tren chi tao instance 1 LAN DAU (computeIfAbsent/
     * registry.xxx(name, supplier) bo qua supplier moi neu instance da ton tai) -
     * khong xoa thi doi timeout/threshold xong se KHONG co hieu luc cho toi khi
     * restart app. Chi can remove(), khong can replace(): lan goi tiep theo se tu
     * rebuild voi cau hinh moi nhat tu DB qua computeIfAbsent/circuitBreaker(...).
     */
    public void invalidate(String upstreamName) {
        // Sau khi them override timeout theo BackendStep, 1 Upstream co the co
        // NHIEU RestTemplate da cache (moi (connectMs, readMs) hieu luc khac nhau
        // la 1 key rieng - xem RestTemplateKey) - phai xoa HET cac key thuoc
        // upstream nay, khong chi 1 key co dinh nhu truoc.
        restTemplateCache.keySet().removeIf(key -> key.upstreamName().equals(upstreamName));
        // Dong pooled client cua cac key vua xoa truoc khi mat tham chieu - moi
        // client nuoi 1 thread evictIdleConnections rieng, khong close = ro ri
        // thread + ket noi con mo (xem pooledClients).
        pooledClients.keySet().removeIf(key -> {
            if (key.upstreamName().equals(upstreamName)) {
                CloseableHttpClient client = pooledClients.get(key);
                try {
                    client.close();
                } catch (Exception e) {
                    log.warn("Loi dong pooled HttpClient khi invalidate upstream '{}' (bo qua): {}",
                            upstreamName, e.getMessage());
                }
                return true;
            }
            return false;
        });
        lastUsedAt.keySet().removeIf(key -> key.upstreamName().equals(upstreamName));
        circuitBreakerRegistry.remove(upstreamName);
        retryRegistry.remove(upstreamName);
        bulkheadRegistry.remove(upstreamName);
        // Bulkhead RIENG theo step (xem bulkheadFor(upstream, stepId, ...)) khoa ten khac
        // hoan toan ("upstreamName#step:stepId") nen bulkheadRegistry.remove(upstreamName)
        // o tren KHONG don duoc - phai loc theo tien to roi xoa tung entry rieng.
        String stepKeyPrefix = upstreamName + STEP_BULKHEAD_KEY_INFIX;
        bulkheadRegistry.getAllBulkheads().forEach(bh -> {
            if (bh.getName().startsWith(stepKeyPrefix)) {
                bulkheadRegistry.remove(bh.getName());
            }
        });
    }

    /**
     * Don DUNG 1 Bulkhead rieng cua step (khong dung ca Upstream) - goi khi CHINH
     * maxConcurrentCallsOverride/maxWaitDurationMsOverride cua step do doi (doi ten/xoa
     * Upstream da duoc invalidate() o tren xu ly rieng). Khong lam gi neu step chua tung
     * co Bulkhead rieng (chua tung goi voi override khac null) - bulkheadRegistry.remove()
     * voi ten chua ton tai la no-op an toan.
     */
    public void invalidateStepBulkhead(String upstreamName, String stepId) {
        bulkheadRegistry.remove(upstreamName + STEP_BULKHEAD_KEY_INFIX + stepId);
    }

    /** Dung cho UpstreamHealthService - 0 neu upstream chua tung co lan cache-eligible nao. */
    public long cacheHitCount(String upstreamName) {
        LongAdder a = cacheHits.get(upstreamName);
        return a == null ? 0 : a.sum();
    }

    public long cacheMissCount(String upstreamName) {
        LongAdder a = cacheMisses.get(upstreamName);
        return a == null ? 0 : a.sum();
    }

    private RestTemplate restTemplateFor(UpstreamService upstream, Integer stepConnectTimeoutMs, Integer stepReadTimeoutMs) {
        int effectiveConnectMs = stepConnectTimeoutMs != null ? stepConnectTimeoutMs : upstream.getConnectTimeoutMs();
        int effectiveReadMs = stepReadTimeoutMs != null ? stepReadTimeoutMs : upstream.getReadTimeoutMs();
        RestTemplateKey key = new RestTemplateKey(upstream.getName(), effectiveConnectMs, effectiveReadMs);
        // Cap nhat MOI LAN goi (ca cache-hit) - sweepIdleClients() dua vao day de biet
        // key nao con dang duoc dung toi, khong chi luc tao moi.
        lastUsedAt.put(key, Instant.now());
        return restTemplateCache.computeIfAbsent(key, k -> {
            // SIZING 1000 RPS (2026-09-14): HttpComponentsClientHttpRequestFactory +
            // connection pool keep-alive thay vi SimpleClientHttpRequestFactory mac
            // dinh (JDK HttpURLConnection moi request 1 ket noi moi - can port
            // ephemeral o 1000 RPS). So ket noi thuc te = so request DONG THOI
            // (Little's Law ~500/pod), khong phai rps. maxTotal 600 = tran cho
            // truong hop xau; maxPerRoute 550 = 1 upstream chiem gan het van khong
            // tu nghen (Bulkhead moi la lop chan that su theo upstream - cot
            // max_concurrent_calls trong DB, xem huong dan production).
            //
            // Moi (connectMs, readMs) la 1 manager rieng (HttpClient 5 khong cho
            // doi ConnectionConfig theo request tren 1 manager dung chung) - truoc
            // day tung RestTemplate cung da doc lap theo key, chi tiet thay doi la
            // co KEEP-ALIVE pool thay vi ket noi khong tai su dung.
            ConnectionConfig perTemplateConfig = ConnectionConfig.custom()
                    .setConnectTimeout(Timeout.ofMilliseconds(k.connectMs()))
                    .setSocketTimeout(Timeout.ofMilliseconds(k.readMs()))
                    .setValidateAfterInactivity(Timeout.ofSeconds(2))
                    .setTimeToLive(TimeValue.ofSeconds(30))
                    .build();
            PoolingHttpClientConnectionManager perTemplateManager =
                    PoolingHttpClientConnectionManagerBuilder.create()
                            .setDefaultConnectionConfig(perTemplateConfig)
                            .setMaxConnTotal(poolMaxTotal)
                            .setMaxConnPerRoute(poolMaxPerRoute)
                            .build();
            CloseableHttpClient client = HttpClients.custom()
                    .setConnectionManager(perTemplateManager)
                    .evictIdleConnections(TimeValue.ofSeconds(30))
                    .evictExpiredConnections()
                    .build();
            pooledClients.put(k, client);
            return new RestTemplate(new HttpComponentsClientHttpRequestFactory(client));
        });
    }

    private CircuitBreaker circuitBreakerFor(UpstreamService upstream) {
        return circuitBreakerRegistry.circuitBreaker(upstream.getName(), () -> CircuitBreakerConfig.custom()
                .failureRateThreshold(upstream.getFailureRateThreshold())
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .permittedNumberOfCallsInHalfOpenState(5)
                // LOI NGHIEP VU 4xx KHONG la failure cua CB: upstream van khoe, tra
                // loi dung nghiep vu (khong dang ky/quota het/input sai...). Neu khong
                // ignore thi chuoi request sai cua cac client khac lam CB OPEN ->
                // client HOP LE tiep theo bi fail-fast 503 trong khi upstream khong
                // van de gi (gap that: loadtest 2026-08-27 quota stock-management
                // tra loi lien tuc -> CB OPEN -> node het cho bi chan theo).
                // Van tinh failure: UpstreamTimeoutException (timeout/refused),
                // UpstreamCallException (loi khong ro), UpstreamHttpErrorException 5xx
                // (isBusinessError chi chap nhan <500) — ha tang that su co van de.
                .ignoreException(UpstreamHttpExecutor::isBusinessError)
                .build());
    }

    /**
     * True = loi NGHIEP VU do upstream chu dong tra ve (4xx) — KHONG tinh vao
     * CircuitBreaker va KHONG tinh la failure cho canh bao (upstream khong van de
     * ha tang). False (>=500 hoac loi khac) = van tinh failure de CB chuyen OPEN
     * khi upstream/hang tang that su chet. Dung chung o DynamicDispatcherController
     * cho canh bao per-endpoint (recordEndpointRequest) — 1 logic duy nhat.
     */
    public static boolean isBusinessError(Throwable e) {
        return e instanceof UpstreamHttpErrorException httpError && httpError.httpStatus() < 500;
    }

    /**
     * Tien to khoa cua Bulkhead RIENG theo step (xem bulkheadFor(upstream, stepId, ...)) -
     * dung "#step:" (mirror dung cu phap "service#operation" ma tai lieu chinh thuc
     * Resilience4j dung lam vi du dat ten bulkhead theo lenh goi cu the, xem
     * https://resilience4j.readme.io/docs/bulkhead) de dam bao KHONG bao gio trung ten
     * voi 1 Upstream that (ten Upstream khong duoc chua ky tu "#" - validate o
     * UpstreamServiceService neu can sau nay).
     */
    private static final String STEP_BULKHEAD_KEY_INFIX = "#step:";

    /** Bulkhead DUNG CHUNG theo Upstream (hanh vi mac dinh, tu truoc gio khong doi). */
    private Bulkhead bulkheadFor(UpstreamService upstream) {
        return bulkheadRegistry.bulkhead(upstream.getName(), () -> BulkheadConfig.custom()
                .maxConcurrentCalls(upstream.getMaxConcurrentCalls())
                .maxWaitDuration(Duration.ofMillis(upstream.getMaxWaitDurationMs()))
                .build());
    }

    /**
     * Bulkhead cho 1 lan goi CU THE - tra ve bulkhead DUNG CHUNG cua Upstream (hanh vi cu)
     * khi maxConcurrentCallsOverride null (KHONG doc stepId trong nhanh nay), hoac 1
     * Bulkhead RIENG, DOC LAP, khoa theo (ten Upstream, stepId) khi co override - xem
     * javadoc BackendStep.maxConcurrentCallsOverride ve y nghia/danh doi day du (mirror
     * pattern "mac dinh dung chung theo backend + override co chon loc theo route/subset"
     * cua Istio DestinationRule/Envoy priority bucket, KHONG tu dong kiem tra tong cac
     * override co vuot suc chiu that cua Upstream hay khong).
     */
    private Bulkhead bulkheadFor(UpstreamService upstream, String stepId, Integer maxConcurrentCallsOverride,
                                  Integer maxWaitDurationMsOverride) {
        if (maxConcurrentCallsOverride == null) {
            return bulkheadFor(upstream);
        }
        String key = upstream.getName() + STEP_BULKHEAD_KEY_INFIX + stepId;
        int effectiveWaitMs = maxWaitDurationMsOverride != null ? maxWaitDurationMsOverride : upstream.getMaxWaitDurationMs();
        return bulkheadRegistry.bulkhead(key, () -> BulkheadConfig.custom()
                .maxConcurrentCalls(maxConcurrentCallsOverride)
                .maxWaitDuration(Duration.ofMillis(effectiveWaitMs))
                .build());
    }

    private Retry retryFor(UpstreamService upstream) {
        return retryRegistry.retry(upstream.getName(), () -> RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(Duration.ofMillis(200), 2.0, 0.5))
                // retryOnException thay vi ignoreExceptions co dinh: KHONG retry khi circuit
                // breaker da mo (fail fast that su, khong doi backoff vo ich), KHONG retry khi
                // Bulkhead vua tu choi vi het cho (cung ly do - he thong dang tu bao ve, thu
                // lai ngay sau backoff khong giai quyet duoc gi, chi la 1 lan xin permit MOI
                // vo ich canh tranh voi request khac dung luc he thong dang yeu - xem
                // isRetryable()) VA khong retry khi upstream tra ve 4xx that su (loi do client/
                // du lieu sai, retry lai van se tra ve dung 4xx do, chi ton them ~600ms-1s +
                // tang tai vo ich len upstream moi lan). Van RETRY cho 5xx/timeout - nhung
                // truong hop co the la tam thoi.
                .retryOnException(UpstreamHttpExecutor::isRetryable)
                .build());
    }

    private static boolean isRetryable(Throwable e) {
        if (e instanceof CallNotPermittedException) {
            return false;
        }
        // Bulkhead vua tu choi vi HET CHO NGAY LUC NAY (day cho toi da maxWaitDurationMs da
        // het) - retry ngay sau backoff (~200-400ms) khong lam tinh trang do bien mat, chi
        // them 1 lan xin permit Bulkhead MOI (permit KHONG giu xuyen suot giua cac lan retry -
        // Retry boc NGOAI Bulkhead/CircuitBreaker, moi lan thu chay lai toan bo chuoi tu dau)
        // canh tranh vo ich voi request khac dung luc he thong dang qua tai. Cung ban chat voi
        // CallNotPermittedException o tren: he thong dang tu bao ve, dung co thu lai ngay.
        if (e instanceof BulkheadFullException) {
            return false;
        }
        if (e instanceof UpstreamHttpErrorException httpError) {
            return httpError.httpStatus() >= 500;
        }
        return true;
    }

    /** Boc loi goi Upstream khong xac dinh duoc nguyen nhan cu the (khac 2 loai duoi day) thanh 1 kieu chung de engine xu ly. */
    public static class UpstreamCallException extends SystemException {
        public UpstreamCallException(String upstreamName, Throwable cause) {
            super("Loi goi Upstream Service '" + upstreamName + "': " + cause.getMessage(), cause);
        }
    }

    /** Upstream tra ve HTTP loi that su (4xx/5xx) - giu status + body goc de GlobalExceptionHandler map ra ma loi phan biet duoc. */
    public static class UpstreamHttpErrorException extends SystemException {
        private final String upstreamName;
        private final int httpStatus;
        private final String responseBody;

        public UpstreamHttpErrorException(String upstreamName, int httpStatus, String responseBody) {
            super("Upstream '" + upstreamName + "' tra ve HTTP " + httpStatus + ": " + responseBody);
            this.upstreamName = upstreamName;
            this.httpStatus = httpStatus;
            this.responseBody = responseBody;
        }

        public String upstreamName() {
            return upstreamName;
        }

        public int httpStatus() {
            return httpStatus;
        }

        public String responseBody() {
            return responseBody;
        }
    }

    /** Timeout ket noi/doc du lieu hoac connection refused toi Upstream - ha tang that su co van de, khac loi nghiep vu 4xx/5xx. */
    public static class UpstreamTimeoutException extends SystemException {
        public UpstreamTimeoutException(String upstreamName, Throwable cause) {
            super("Upstream '" + upstreamName + "' khong phan hoi kip thoi (timeout/connection refused): " + cause.getMessage(), cause);
        }
    }
}
