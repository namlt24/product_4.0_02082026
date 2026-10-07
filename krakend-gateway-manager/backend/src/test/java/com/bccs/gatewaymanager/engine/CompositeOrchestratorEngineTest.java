package com.bccs.gatewaymanager.engine;

import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.FieldMappingDto;
import com.bccs.gatewaymanager.dto.StepTraceDto;
import com.bccs.gatewaymanager.entity.ConditionOperator;
import com.bccs.gatewaymanager.entity.FieldMappingSourceType;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import com.bccs.gatewaymanager.entity.MappingTargetContext;
import com.bccs.gatewaymanager.entity.MappingTargetType;
import com.bccs.gatewaymanager.entity.UpstreamService;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.service.UpstreamRegistryCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Test cho CompositeOrchestratorEngine - dac biet phan re nhanh (P1-5, viet
 * lai toan bo vong lap thuc thi tu for co dinh sang con tro). Engine truoc do
 * CHUA TUNG co unit test rieng (chi test tay qua curl suot session) - day la
 * bo test dau tien, uu tien cao nhat cho tinh tuong thich nguoc (test dau
 * tien phai la endpoint KHONG dung re nhanh chay dung y het hanh vi cu).
 */
@ExtendWith(MockitoExtension.class)
class CompositeOrchestratorEngineTest {

    @Mock
    private UpstreamHttpExecutor upstreamHttpExecutor;
    @Mock
    private UpstreamRegistryCache upstreamRegistryCache;
    @Mock
    private com.bccs.gatewaymanager.service.FlagRegistryCache flagRegistryCache;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    // Pool that thay vi mock/single-thread - muc 4 (parallelExecution) can nhieu thread
    // THAT su chay dong thoi de cac test lien quan co gia tri (mock đon giản, khong I/O
    // thuc, nen fixed(4) la du, khong can dung dung ProductionExecutorConfig).
    private final ExecutorService parallelStepExecutor = Executors.newFixedThreadPool(4);
    private CompositeOrchestratorEngine engine;

    private final UpstreamService up1 = UpstreamService.builder().id("u1").name("up1").baseHost("http://u1").build();
    private final UpstreamService up2 = UpstreamService.builder().id("u2").name("up2").baseHost("http://u2").build();
    private final UpstreamService up3 = UpstreamService.builder().id("u3").name("up3").baseHost("http://u3").build();
    // Upstream RIENG cho loi goi bu tru (muc 6) - khac up1/up2/up3 (loi goi CHINH) de
    // phan biet ro trong test (verify dung upstream bu tru duoc goi, khong nham voi chinh).
    private final UpstreamService upComp1 = UpstreamService.builder().id("uc1").name("upComp1").baseHost("http://upcomp1").build();
    private final UpstreamService upComp2 = UpstreamService.builder().id("uc2").name("upComp2").baseHost("http://upcomp2").build();

    @BeforeEach
    void setUp() {
        FlagRuleEvaluator flagRuleEvaluator = new FlagRuleEvaluator(flagRegistryCache);
        engine = new CompositeOrchestratorEngine(upstreamHttpExecutor, upstreamRegistryCache, objectMapper, parallelStepExecutor, flagRuleEvaluator, flagRegistryCache);
        lenient().when(upstreamRegistryCache.getById("u1")).thenReturn(up1);
        lenient().when(upstreamRegistryCache.getById("u2")).thenReturn(up2);
        lenient().when(upstreamRegistryCache.getById("u3")).thenReturn(up3);
        lenient().when(upstreamRegistryCache.getById("uc1")).thenReturn(upComp1);
        lenient().when(upstreamRegistryCache.getById("uc2")).thenReturn(upComp2);
    }

    private JsonNode json(String raw) {
        return objectMapper.readTree(raw);
    }

    /** Step co day du field, mac dinh KHONG co dieu kien/re nhanh - override tung field can qua tham so. */
    private BackendStepDto step(int order, String upstreamId, Integer nextIfTrue, Integer nextIfFalse,
                                 ConditionOperator operator, FieldMappingSourceType conditionSourceType,
                                 Integer conditionSourceStepOrder, String conditionSourceField, String conditionExpectedValue) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", upstreamId, "up" + order,
                false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                null, null,
                null, null,
                conditionSourceType, conditionSourceStepOrder, conditionSourceField, operator, conditionExpectedValue,
                null, null, nextIfTrue, nextIfFalse, null, null, null, null, null, null);
    }

    private BackendStepDto plainStep(int order, String upstreamId) {
        return step(order, upstreamId, null, null, null, null, null, null, null);
    }

    private EndpointResponseDto endpoint(boolean sequential, BackendStepDto... steps) {
        return new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, sequential, "json",
                List.of(steps), List.of(), null, null, false, 86400, false, false, 300, List.of(), null);
    }

    /** Endpoint non-sequential VOI parallelExecution=true (muc 4) - dung cho nhom test song song hoa. */
    private EndpointResponseDto endpointParallel(BackendStepDto... steps) {
        return new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, false, "json",
                List.of(steps), List.of(), null, null, false, 86400, true, false, 300, List.of(), null);
    }

    /** Endpoint sequential=true VOI mappings tuy chinh - dung cho nhom test wave (parallelGroup) can FieldMapping. */
    private EndpointResponseDto endpointWithMappings(List<FieldMappingDto> mappings, BackendStepDto... steps) {
        return new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(steps), mappings, null, null, false, 86400, false, false, 300, List.of(), null);
    }

    private void stubCall(UpstreamService upstream, JsonNode response) {
        when(upstreamHttpExecutor.call(eq(upstream), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any()))
                .thenReturn(response);
    }

    private void stubCallThrows(UpstreamService upstream, RuntimeException ex) {
        when(upstreamHttpExecutor.call(eq(upstream), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any()))
                .thenThrow(ex);
    }

    /** Step co onErrorStepOrder rieng - dung cho test fallback khi step loi (P-3, doc lap voi conditionOperator). */
    private BackendStepDto stepWithOnError(int order, String upstreamId, Integer onErrorStepOrder) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", upstreamId, "up" + order,
                false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                null, null,
                null, null,
                null, null, null, null, null,
                null, null, null, null, onErrorStepOrder, null, null, null, null, null);
    }

    /** Step trong 1 "wave" song song (parallelGroup) - dung cho test wave/dependency execution. */
    private BackendStepDto stepWithGroup(int order, String upstreamId, Integer parallelGroup) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", upstreamId, "up" + order,
                false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                null, null,
                null, null,
                null, null, null, null, null,
                null, null, null, null, null, parallelGroup, null, null, null, null);
    }

    /** Step co cau hinh bu tru/rollback (muc 6) - dung cho nhom test saga best-effort. */
    private BackendStepDto stepWithCompensation(int order, String upstreamId, String compUpstreamId, String compUrlPattern) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", upstreamId, "up" + order,
                false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                null, null,
                null, null,
                null, null, null, null, null,
                null, null, null, null, null, null,
                compUpstreamId, "upComp", GatewayMethod.DELETE, compUrlPattern);
    }

    // ---- Tuong thich nguoc: endpoint KHONG dung re nhanh phai chay DUNG y het hanh vi cu ----

    @Test
    void khongDieuKien_chayHetTatCaStepTheoDungThuTu_tuongThichNguoc() {
        stubCall(up1, json("{\"v\":1}"));
        stubCall(up2, json("{\"v\":2}"));
        stubCall(up3, json("{\"v\":3}"));
        EndpointResponseDto config = endpoint(true, plainStep(1, "u1"), plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        // Sequential (khong re nhanh) = tra ve ket qua step CUOI CUNG - dung y het hanh vi cu.
        assertThat(result.get("v").asInt()).isEqualTo(3);
    }

    @Test
    void nonSequential_boQuaHoanToanFieldReNhanh_vanChayHetTatCaStep() {
        // Step 1 co khai bao dieu kien/re nhanh nhung config KHONG sequential -
        // theo dung thiet ke (re nhanh chi ap dung khi sequential=true), field
        // nay phai bi BO QUA hoan toan, tat ca step van chay het + gop lai.
        stubCall(up1, json("{\"a\":1}"));
        stubCall(up2, json("{\"b\":2}"));
        BackendStepDto s1 = step(1, "u1", 99, 99, ConditionOperator.EXISTS, FieldMappingSourceType.REQUEST_BODY, null, "x", null);
        EndpointResponseDto config = endpoint(false, s1, plainStep(2, "u2"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("a").asInt()).isEqualTo(1);
        assertThat(result.get("b").asInt()).isEqualTo(2);
    }

    // ---- Muc 4: song song hoa THAT SU step doc lap (parallelExecution) ----

    @Test
    void parallel_ketQuaGiongHetVongLapTuanTu_khiCacStepDocLap() {
        // parallelExecution=true chi doi CACH thuc thi (dong thoi thay vi tuan tu), KHONG
        // duoc doi KET QUA - phai gop dung du field cua ca 3 step, giong het test
        // nonSequential_boQuaHoanToanFieldReNhanh... o tren (chi khac endpointParallel()).
        stubCall(up1, json("{\"a\":1}"));
        stubCall(up2, json("{\"b\":2}"));
        stubCall(up3, json("{\"c\":3}"));
        EndpointResponseDto config = endpointParallel(plainStep(1, "u1"), plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("a").asInt()).isEqualTo(1);
        assertThat(result.get("b").asInt()).isEqualTo(2);
        assertThat(result.get("c").asInt()).isEqualTo(3);
    }

    @Test
    void parallel_chayThatSuDongThoi_khongPhaiTuanTuNguyTrang() {
        // Chung minh "song song" o day la THAT (dung thread pool, khong chi la vong lap
        // tuan tu doi lot) - 3 step, moi step gia lap goi upstream mat 200ms. Neu chay
        // tuan tu: >= 600ms. Neu chay dong thoi: ~200ms (+overhead). Nguong 450ms du xa
        // 2 phia de khong flaky tren may cham, nhung van chan chac chan hon tuan tu.
        int delayMs = 200;
        when(upstreamHttpExecutor.call(eq(up1), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(inv -> { Thread.sleep(delayMs); return json("{\"a\":1}"); });
        when(upstreamHttpExecutor.call(eq(up2), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(inv -> { Thread.sleep(delayMs); return json("{\"b\":2}"); });
        when(upstreamHttpExecutor.call(eq(up3), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(inv -> { Thread.sleep(delayMs); return json("{\"c\":3}"); });
        EndpointResponseDto config = endpointParallel(plainStep(1, "u1"), plainStep(2, "u2"), plainStep(3, "u3"));

        long start = System.nanoTime();
        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertThat(result.get("a").asInt()).isEqualTo(1);
        assertThat(result.get("b").asInt()).isEqualTo(2);
        assertThat(result.get("c").asInt()).isEqualTo(3);
        assertThat(elapsedMs).isLessThan(3L * delayMs - 100);
    }

    @Test
    void parallel_1StepLoi_tatCaStepDocLapVanDuocSubmit_roiThrowLoiGoc() {
        // Khac han vong lap tuan tu (loi o dau dung ngay o do, step sau KHONG BAO GIO
        // chay) - o parallel, TAT CA step da duoc submit truoc khi loi duoc phat hien.
        // Verify ca 2: (1) step khac VAN duoc goi (khong bi "huy" giua chung boi loi cua
        // step kia), (2) loi goc van throw ra ngoai dung nguyen ban (khong bi nuot/doi kieu).
        stubCall(up2, json("{\"b\":2}"));
        stubCallThrows(up1, new BusinessException("GW-UP-500", "upstream 1 loi"));
        EndpointResponseDto config = endpointParallel(plainStep(1, "u1"), plainStep(2, "u2"));

        assertThatThrownBy(() -> engine.handle(config, Map.of(), Map.of(), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("upstream 1 loi");
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.times(1))
                .call(eq(up2), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void parallelExecutionTat_macDinhFalse_vanChayTuanTuNhuCu() {
        // Regression: endpoint non-sequential hien co (parallelExecution=false mac dinh)
        // phai giu nguyen 100% hanh vi cu - dung endpoint() (khong phai endpointParallel()).
        stubCall(up1, json("{\"a\":1}"));
        stubCall(up2, json("{\"b\":2}"));
        EndpointResponseDto config = endpoint(false, plainStep(1, "u1"), plainStep(2, "u2"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("a").asInt()).isEqualTo(1);
        assertThat(result.get("b").asInt()).isEqualTo(2);
    }

    @Test
    void parallel_MDCRequestIdLanSangThreadPool_khongBiMatKhoiHopAuditLog() {
        // Bug THAT phat hien khi verify song tinh nang wave (chi thay 1/3 hop trong audit
        // log, dung ra phai co 3) - MDC ("requestId", dung de noi hop audit log voi dung
        // request, xem UpstreamHttpExecutor.call()) la ThreadLocal, KHONG tu lan sang
        // thread cua parallelStepExecutor - truoc fix, hop chay trong thread song song bi
        // ghi requestId=null, mat hoan toan khoi "Tra cuu Log". Test nay khong goi
        // UpstreamHttpExecutor that (da mock) nhung mo phong DUNG cach no doc MDC.get()
        // ngay trong luc thuc thi (thenAnswer chay TREN chinh thread cua parallelStepExecutor).
        MDC.put("requestId", "req-mdc-test");
        try {
            List<String> capturedRequestIds = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
            when(upstreamHttpExecutor.call(eq(up1), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any()))
                    .thenAnswer(inv -> { capturedRequestIds.add(MDC.get("requestId")); return json("{\"a\":1}"); });
            when(upstreamHttpExecutor.call(eq(up2), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any()))
                    .thenAnswer(inv -> { capturedRequestIds.add(MDC.get("requestId")); return json("{\"b\":2}"); });
            EndpointResponseDto config = endpointParallel(plainStep(1, "u1"), plainStep(2, "u2"));

            engine.handle(config, Map.of(), Map.of(), null);

            assertThat(capturedRequestIds).containsExactlyInAnyOrder("req-mdc-test", "req-mdc-test");
        } finally {
            MDC.remove("requestId");
        }
    }

    @Test
    void parallel_TraceCollectorLanSangThreadPool_gomDuHopCuaTatCaStepSongSong() {
        // Mirror dung test MDC o tren - TraceCollector ("Thu nhanh") cung la ThreadLocal,
        // cung can lan truyen tay sang worker thread cua parallelStepExecutor, nhung khac
        // MDC (moi thread giu 1 ban String rieng): TAT CA worker thread duoc gan CHUNG 1
        // list de 2 step song song cung ghi duoc vao 1 ket qua duy nhat.
        List<StepTraceDto> hops = TraceCollector.start();
        try {
            when(upstreamHttpExecutor.call(eq(up1), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any()))
                    .thenAnswer(inv -> {
                        TraceCollector.record(new StepTraceDto(1, "s1", "u1", "GET", "http://u1", null, false,
                                200, "{}", false, 5L, false, true, null));
                        return json("{\"a\":1}");
                    });
            when(upstreamHttpExecutor.call(eq(up2), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any()))
                    .thenAnswer(inv -> {
                        TraceCollector.record(new StepTraceDto(2, "s2", "u2", "GET", "http://u2", null, false,
                                200, "{}", false, 5L, false, true, null));
                        return json("{\"b\":2}");
                    });
            EndpointResponseDto config = endpointParallel(plainStep(1, "u1"), plainStep(2, "u2"));

            engine.handle(config, Map.of(), Map.of(), null);

            assertThat(hops).extracting(StepTraceDto::stepOrder).containsExactlyInAnyOrder(1, 2);
        } finally {
            TraceCollector.stop();
        }
    }

    // ---- Wave song song trong 1 chuoi sequential (parallelGroup) ----
    // Khac muc 4 (parallelExecution - TOAN BO step khong sequential chay song song):
    // day la 1 "doan" song song NAM TRONG 1 chuoi sequential=true, step SAU wave van
    // chay tuan tu binh thuong va co the doc du lieu da gop cua CA wave.

    @Test
    void wave_2StepSongSong_step3TuanTuDocDuLieuGopCuaCaWave() {
        // Chinh nghiep vu nguoi dung mo ta: step1+step2 (cung parallelGroup=1) chay song
        // song, step3 (tuan tu, KHONG group) dung 2 FieldMapping doc field "a" tu step1 va
        // "b" tu step2 - phai co CA HAI, khong bi race/null (day la dieu workaround "goi
        // lai chinh gateway" tung phai lam qua 2 endpoint moi dam bao dung).
        stubCall(up1, json("{\"a\":1}"));
        stubCall(up2, json("{\"b\":2}"));
        stubCall(up3, json("{\"ok\":true}"));
        BackendStepDto s1 = stepWithGroup(1, "u1", 1);
        BackendStepDto s2 = stepWithGroup(2, "u2", 1);
        BackendStepDto s3 = plainStep(3, "u3");
        FieldMappingDto m1 = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 1, "a", null, null, null,
                3, MappingTargetType.QUERY, "fromStep1", 0, null);
        FieldMappingDto m2 = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 2, "b", null, null, null,
                3, MappingTargetType.QUERY, "fromStep2", 1, null);
        EndpointResponseDto config = endpointWithMappings(List.of(m1, m2), s1, s2, s3);

        engine.handle(config, Map.of(), Map.of(), null);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor)
                .call(eq(up3), any(), urlCaptor.capture(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
        assertThat(urlCaptor.getValue()).contains("fromStep1=1").contains("fromStep2=2");
    }

    @Test
    void wave_chayThatSuDongThoi_khongPhaiTuanTuNguyTrang() {
        // Dung y het pattern do thoi gian cua muc 4 (parallel_chayThatSuDongThoi...) -
        // chung minh wave TRONG 1 chuoi sequential cung chay THAT dong thoi, khong phai
        // tuan tu doi lot.
        int delayMs = 200;
        when(upstreamHttpExecutor.call(eq(up1), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(inv -> { Thread.sleep(delayMs); return json("{\"a\":1}"); });
        when(upstreamHttpExecutor.call(eq(up2), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(inv -> { Thread.sleep(delayMs); return json("{\"b\":2}"); });
        EndpointResponseDto config = endpoint(true, stepWithGroup(1, "u1", 1), stepWithGroup(2, "u2", 1));

        long start = System.nanoTime();
        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertThat(result.get("a").asInt()).isEqualTo(1);
        assertThat(result.get("b").asInt()).isEqualTo(2);
        assertThat(elapsedMs).isLessThan(2L * delayMs - 50);
    }

    @Test
    void wave_1ThanhVienLoi_doiCaWaveXongRoiThrowLoiGoc() {
        stubCall(up2, json("{\"b\":2}"));
        stubCallThrows(up1, new BusinessException("GW-UP-500", "upstream 1 loi"));
        EndpointResponseDto config = endpoint(true, stepWithGroup(1, "u1", 1), stepWithGroup(2, "u2", 1));

        assertThatThrownBy(() -> engine.handle(config, Map.of(), Map.of(), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("upstream 1 loi");
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.times(1))
                .call(eq(up2), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void wave_laStepCuoiCungCuaChuoi_ketQuaLaGopCaWave() {
        // Khong co step nao sau wave -> response cuoi cung = gop field cua ca wave, dung
        // y het cach assembleFinalResponse() gop cho endpoint non-sequential (tai su dung
        // code, xem CompositeOrchestratorEngine.executeSequentialChain()).
        stubCall(up1, json("{\"a\":1}"));
        stubCall(up2, json("{\"b\":2}"));
        EndpointResponseDto config = endpoint(true, stepWithGroup(1, "u1", 1), stepWithGroup(2, "u2", 1));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("a").asInt()).isEqualTo(1);
        assertThat(result.get("b").asInt()).isEqualTo(2);
    }

    // ---- Index mang trong duong dan qua "[N]" (JsonPathUtil) - lay 1 field cua phan tu CU
    // THE trong mang, hoac ca phan tu do, dung lam sourceField cua FieldMapping HOAC lam
    // target de "boc vo" response 1 step - ca 2 noi dung chung JsonPathUtil.getByDotPath()
    // nen tu dong duoc ho tro, khong can FieldMappingSourceType/cot DB moi. ----

    /** Step co target tuy chinh (boc vo response) - dung cho test index mang qua target. */
    private BackendStepDto stepWithTarget(int order, String upstreamId, String target) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", upstreamId, "up" + order,
                false, false, 300, null, target, List.of(), List.of(), Map.of(), null, null,
                null, null,
                null, null,
                null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null);
    }

    @Test
    void sourceField_layFieldCuaPhanTuMangTheoIndex_dungThangTrongFieldMapping() {
        // Cach dung PHO BIEN nhat: KHONG can target boc vo o step nguon - sourceField cua
        // FieldMapping tu index thang vao mang (vi du response tra ve 1 danh sach "items",
        // muon lay ten cua PHAN TU THU 2 lam query param cho step sau).
        stubCall(up1, json("{\"items\":[{\"id\":10,\"name\":\"Alpha\"},{\"id\":20,\"name\":\"Beta\"}]}"));
        stubCall(up2, json("{\"ok\":true}"));
        BackendStepDto s1 = plainStep(1, "u1");
        BackendStepDto s2 = plainStep(2, "u2");
        FieldMappingDto m = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 1, "items[1].name",
                null, null, null, 2, MappingTargetType.QUERY, "itemName", 0, null);
        EndpointResponseDto config = endpointWithMappings(List.of(m), s1, s2);

        engine.handle(config, Map.of(), Map.of(), null);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor)
                .call(eq(up2), any(), urlCaptor.capture(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
        assertThat(urlCaptor.getValue()).contains("itemName=Beta");
    }

    @Test
    void stepTarget_boQuaVoPhanTuMangTheoIndex_traVeNguyenPhanTuDoLamResponseCuaStep() {
        // step.target="items[1]" (KHONG co truong sau) -> response CUA CHINH STEP NAY sau
        // khi boc vo la NGUYEN object phan tu do - dung khi muon dung ca 1 phan tu mang lam
        // response tra ve, hoac lam nguon cho FieldMapping cua step sau (sourceField tinh
        // TU goc phan tu do, khong phai tu response goc).
        stubCall(up1, json("{\"items\":[{\"id\":10,\"name\":\"Alpha\"},{\"id\":20,\"name\":\"Beta\"}]}"));
        BackendStepDto s1 = stepWithTarget(1, "u1", "items[1]");
        EndpointResponseDto config = endpoint(true, s1);

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("id").asInt()).isEqualTo(20);
        assertThat(result.get("name").asString()).isEqualTo("Beta");
    }

    @Test
    void sourceField_indexVuotQuaSizeMang_traVeNullAnToan_khongThrow() {
        // Graceful null nhu moi tham chieu khac trong engine - khong throw chi vi mang
        // ngan hon du kien (vi du backend tra ve it phan tu hon binh thuong).
        stubCall(up1, json("{\"items\":[{\"name\":\"Alpha\"}]}"));
        stubCall(up2, json("{\"ok\":true}"));
        BackendStepDto s1 = plainStep(1, "u1");
        BackendStepDto s2 = plainStep(2, "u2");
        FieldMappingDto m = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 1, "items[5].name",
                null, null, null, 2, MappingTargetType.QUERY, "itemName", 0, null);
        EndpointResponseDto config = endpointWithMappings(List.of(m), s1, s2);

        engine.handle(config, Map.of(), Map.of(), null);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor)
                .call(eq(up2), any(), urlCaptor.capture(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
        assertThat(urlCaptor.getValue()).doesNotContain("itemName=");
    }

    // ---- Bu tru/rollback nghiep vu (muc 6, saga best-effort) ----

    @Test
    void compensation_step1ThanhCongCoBuTru_step2LoiKhongOnError_buTruStep1DuocGoi_loiGocVanThrow() {
        stubCall(up1, json("{\"a\":1}"));
        stubCallThrows(up2, new BusinessException("GW-UP-500", "upstream 2 loi"));
        stubCall(upComp1, json("{\"ok\":true}"));
        BackendStepDto s1 = stepWithCompensation(1, "u1", "uc1", "/orders/cancel");
        BackendStepDto s2 = plainStep(2, "u2");
        EndpointResponseDto config = endpoint(true, s1, s2);

        assertThatThrownBy(() -> engine.handle(config, Map.of(), Map.of(), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("upstream 2 loi");

        org.mockito.Mockito.verify(upstreamHttpExecutor)
                .call(eq(upComp1), eq(HttpMethod.DELETE), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void compensation_onErrorStepOrderXuLyThanhCong_buTruKhongDuocGoi() {
        // onErrorStepOrder da xu ly loi thanh cong (fallback sang step3 thanh cong) -
        // KHONG co exception nao thoat ra ngoai executeSequentialChain(), runCompensations()
        // khong bao gio duoc goi - du step1 co cau hinh bu tru.
        stubCall(up1, json("{\"a\":1}"));
        stubCallThrows(up2, new BusinessException("GW-UP-500", "loi tam thoi"));
        stubCall(up3, json("{\"fallback\":true}"));
        BackendStepDto s1 = stepWithCompensation(1, "u1", "uc1", "/orders/cancel");
        BackendStepDto s2 = stepWithOnError(2, "u2", 3);
        BackendStepDto s3 = plainStep(3, "u3");
        EndpointResponseDto config = endpoint(true, s1, s2, s3);

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("fallback").asBoolean()).isTrue();
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(upComp1), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void compensation_nhieuStepThanhCong_buTruTheoThuTuNguoc() {
        stubCall(up1, json("{\"a\":1}"));
        stubCall(up2, json("{\"b\":2}"));
        stubCallThrows(up3, new BusinessException("GW-UP-500", "step3 loi"));
        stubCall(upComp1, json("{\"ok\":true}"));
        stubCall(upComp2, json("{\"ok\":true}"));
        BackendStepDto s1 = stepWithCompensation(1, "u1", "uc1", "/undo1");
        BackendStepDto s2 = stepWithCompensation(2, "u2", "uc2", "/undo2");
        BackendStepDto s3 = plainStep(3, "u3");
        EndpointResponseDto config = endpoint(true, s1, s2, s3);

        assertThatThrownBy(() -> engine.handle(config, Map.of(), Map.of(), null)).isInstanceOf(BusinessException.class);

        // Undo cai GAN NHAT (step2) TRUOC, roi moi toi step1 - thu tu NGUOC voi thu tu hoan thanh.
        InOrder inOrder = org.mockito.Mockito.inOrder(upstreamHttpExecutor);
        inOrder.verify(upstreamHttpExecutor).call(eq(upComp2), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
        inOrder.verify(upstreamHttpExecutor).call(eq(upComp1), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void compensation_1LoiBuTruKhongChanCacBuTruKhac_khongDoiLoiGoc() {
        stubCall(up1, json("{\"a\":1}"));
        stubCall(up2, json("{\"b\":2}"));
        stubCallThrows(up3, new BusinessException("GW-UP-500", "step3 loi goc"));
        stubCallThrows(upComp2, new RuntimeException("bu tru step2 that bai"));
        stubCall(upComp1, json("{\"ok\":true}"));
        BackendStepDto s1 = stepWithCompensation(1, "u1", "uc1", "/undo1");
        BackendStepDto s2 = stepWithCompensation(2, "u2", "uc2", "/undo2");
        BackendStepDto s3 = plainStep(3, "u3");
        EndpointResponseDto config = endpoint(true, s1, s2, s3);

        // Loi GOC (step3) van la loi client nhin thay - KHONG bi loi bu tru cua step2 che mat.
        assertThatThrownBy(() -> engine.handle(config, Map.of(), Map.of(), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("step3 loi goc");

        // Bu tru step1 VAN duoc goi du bu tru step2 (chay TRUOC, vi thu tu nguoc) that bai.
        org.mockito.Mockito.verify(upstreamHttpExecutor)
                .call(eq(upComp1), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void compensation_stepKhongCauHinh_khongDuocGoi() {
        stubCall(up1, json("{\"a\":1}"));
        stubCallThrows(up2, new BusinessException("GW-UP-500", "loi"));
        BackendStepDto s1 = plainStep(1, "u1"); // KHONG cau hinh bu tru
        BackendStepDto s2 = plainStep(2, "u2");
        EndpointResponseDto config = endpoint(true, s1, s2);

        assertThatThrownBy(() -> engine.handle(config, Map.of(), Map.of(), null)).isInstanceOf(BusinessException.class);

        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(upComp1), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void compensation_duLieuKhongToanVen_compensationMethodNull_khongNemNpe_loiGocVanThrow() {
        // Gia lap du lieu bi hong (bypass EndpointService.validateCompensationConfig(),
        // vi du sua tay trong DB): compensationUpstreamServiceId co gia tri nhung
        // compensationMethod=null - truoc fix se NPE tai step.compensationMethod().name(),
        // sau fix phai bao BusinessException ro rang (GW-COMPENSATION-CONFIG-INVALID),
        // van duoc runCompensations() nuot an toan, khong lam sap request, khong che loi goc.
        stubCall(up1, json("{\"a\":1}"));
        stubCallThrows(up2, new BusinessException("GW-UP-500", "upstream 2 loi"));
        BackendStepDto s1 = new BackendStepDto(null, 1, "step1", GatewayMethod.GET, "/x", "u1", "up1",
                false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                null, null,
                null, null,
                null, null, null, null, null,
                null, null, null, null, null, null,
                "uc1", "upComp", null, "/orders/cancel");
        BackendStepDto s2 = plainStep(2, "u2");
        EndpointResponseDto config = endpoint(true, s1, s2);

        // Loi GOC (step2) van la loi client nhin thay - khong bi thay the boi NPE/loi cau hinh bu tru.
        assertThatThrownBy(() -> engine.handle(config, Map.of(), Map.of(), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("upstream 2 loi");

        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(upComp1), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void compensation_waveThanhVienThanhCongCoBuTru_stepSauLoi_caWaveDuocBuTru() {
        // 2 thanh vien wave (parallelGroup=1), CA HAI co bu tru rieng - chung minh
        // completedStepOrders ghi nhan TUNG thanh vien rieng le (khong phai ca wave nhu
        // 1 khoi), du chinh wave nay THANH CONG (chi step3 SAU wave moi loi).
        stubCall(up1, json("{\"a\":1}"));
        stubCall(up2, json("{\"b\":2}"));
        stubCallThrows(up3, new BusinessException("GW-UP-500", "step3 loi"));
        stubCall(upComp1, json("{\"ok\":true}"));
        stubCall(upComp2, json("{\"ok\":true}"));
        BackendStepDto s1 = new BackendStepDto(null, 1, "step1", GatewayMethod.GET, "/x", "u1", "up1",
                false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                null, null,
                null, null,
                null, null, null, null, null,
                null, null, null, null, null, 1,
                "uc1", "upComp1", GatewayMethod.DELETE, "/undo1");
        BackendStepDto s2 = new BackendStepDto(null, 2, "step2", GatewayMethod.GET, "/x", "u2", "up2",
                false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                null, null,
                null, null,
                null, null, null, null, null,
                null, null, null, null, null, 1,
                "uc2", "upComp2", GatewayMethod.DELETE, "/undo2");
        BackendStepDto s3 = plainStep(3, "u3");
        EndpointResponseDto config = endpoint(true, s1, s2, s3);

        assertThatThrownBy(() -> engine.handle(config, Map.of(), Map.of(), null)).isInstanceOf(BusinessException.class);

        org.mockito.Mockito.verify(upstreamHttpExecutor)
                .call(eq(upComp1), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
        org.mockito.Mockito.verify(upstreamHttpExecutor)
                .call(eq(upComp2), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void compensation_fieldMappingCompensationContext_dungDuLieuTuChinhResponseStepDo() {
        // sourceStepOrder==targetStepOrder (bu tru step1 dung CHINH response cua step1,
        // truong hop pho bien nhat - vd lay "orderId" vua tao de dien vao URL DELETE).
        stubCall(up1, json("{\"orderId\":\"O123\"}"));
        stubCallThrows(up2, new BusinessException("GW-UP-500", "step2 loi"));
        stubCall(upComp1, json("{\"ok\":true}"));
        BackendStepDto s1 = stepWithCompensation(1, "u1", "uc1", "/orders/{orderId}");
        BackendStepDto s2 = plainStep(2, "u2");
        FieldMappingDto compMapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE, 1, "orderId",
                null, null, null, 1, MappingTargetType.PATH, "orderId", 0, MappingTargetContext.COMPENSATION);
        EndpointResponseDto config = endpointWithMappings(List.of(compMapping), s1, s2);

        assertThatThrownBy(() -> engine.handle(config, Map.of(), Map.of(), null)).isInstanceOf(BusinessException.class);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor)
                .call(eq(upComp1), any(), urlCaptor.capture(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
        assertThat(urlCaptor.getValue()).contains("/orders/O123");
    }

    // ---- Re nhanh that: di theo nhanh dung/sai ----

    @Test
    void reNhanh_dieuKienDung_diTheoNhanhTrue_boQuaNhanhFalse() {
        stubCall(up1, json("{\"status\":\"active\"}"));
        stubCall(up2, json("{\"branch\":\"true-path\"}"));
        BackendStepDto s1 = step(1, "u1", 2, null, ConditionOperator.EQUALS, FieldMappingSourceType.STEP_RESPONSE, 1, "status", "active");
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("branch").asText()).isEqualTo("true-path");
    }

    @Test
    void reNhanh_2NhanhLoaiTruLanNhau_khongCoConditionOperatorRieng_CHI_chayDungMOTNhanh() {
        // Regression test: bug phat hien khi verify "Numeric Branch Demo" - step2/step3 la
        // 2 "nhanh la" (branch leaf) LOAI TRU LAN NHAU, CA HAI deu KHONG khai bao
        // conditionOperator rieng (dung y het cach "Re nhanh Demo - Channel Type theo
        // StaffId" da seed truoc P1-5). TRUOC FIX: sau khi vao dung nhanh TRUE (step2),
        // engine tu dong "roi tiep" sang step3 (stepOrder lon hon ke tiep) theo quy tac
        // natural-next -> goi NHAM ca 2 nhanh, tra ve nham ket qua nhanh FALSE. SAU FIX:
        // step2/step3 la dich cua nhanh re -> KHONG duoc tu dong roi tiep, dung dung tai
        // nhanh vua duoc chon.
        stubCall(up1, json("{\"status\":\"active\"}"));
        stubCall(up2, json("{\"branch\":\"true-path\"}"));
        BackendStepDto s1 = step(1, "u1", 2, 3, ConditionOperator.EQUALS, FieldMappingSourceType.STEP_RESPONSE, 1, "status", "active");
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("branch").asText()).isEqualTo("true-path");
        // Nhanh FALSE (up3) KHONG duoc goi - chi 1 trong 2 nhanh loai tru lan nhau duoc chay.
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(up3), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void reNhanh_dieuKienSai_diTheoNhanhFalse() {
        stubCall(up1, json("{\"status\":\"inactive\"}"));
        stubCall(up3, json("{\"branch\":\"false-path\"}"));
        BackendStepDto s1 = step(1, "u1", 2, 3, ConditionOperator.EQUALS, FieldMappingSourceType.STEP_RESPONSE, 1, "status", "active");
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("branch").asText()).isEqualTo("false-path");
    }

    @Test
    void reNhanh_ketQuaCuoiCung_laStepCuoiCungTHUCSUChay_khongPhaiStepOrderLonNhat() {
        // step1 (order=1) -> dung -> step3 (order=3), KHONG chay step2 (order=2) -
        // ket qua cuoi cung phai la cua step3, du step2 co stepOrder nho hon.
        stubCall(up1, json("{\"go\":true}"));
        stubCall(up3, json("{\"final\":\"from-step3\"}"));
        BackendStepDto s1 = step(1, "u1", 3, null, ConditionOperator.EXISTS, FieldMappingSourceType.STEP_RESPONSE, 1, "go", null);
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("final").asText()).isEqualTo("from-step3");
    }

    @Test
    void nextStepOrderNull_ketThucSomTaiDay_khongChayTiep() {
        stubCall(up1, json("{\"result\":\"chi-mot-minh-step1\"}"));
        BackendStepDto s1 = step(1, "u1", null, null, ConditionOperator.EXISTS,
                FieldMappingSourceType.REQUEST_BODY, null, "anything", null);
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        // "anything" khong ton tai trong body (null) -> EXISTS = false -> nextStepOrderIfFalse = null -> ket thuc ngay.
        assertThat(result.get("result").asText()).isEqualTo("chi-mot-minh-step1");
    }

    @Test
    void dieuKienThamChieuStepChuaTungChay_traVeNullAnToan_khongThrow() {
        // step1 -> nhay thang toi step3 (bo qua step2). step3 co dieu kien tham
        // chieu response cua step2 (CHUA TUNG chay) - phai coi la "khong ton
        // tai" (NOT_EXISTS=true), khong duoc throw NPE/loi gi ca.
        stubCall(up1, json("{}"));
        stubCall(up3, json("{\"skip2seen\":true}"));
        BackendStepDto s1 = step(1, "u1", 3, 3, ConditionOperator.EXISTS, FieldMappingSourceType.REQUEST_BODY, null, "never", null);
        BackendStepDto s3 = step(3, "u3", null, null, ConditionOperator.NOT_EXISTS, FieldMappingSourceType.STEP_RESPONSE, 2, "someField", null);
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), s3);

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("skip2seen").asBoolean()).isTrue();
    }

    @Test
    void vongLapReNhanh_throwGW_BRANCH_LOOP() {
        stubCall(up1, json("{\"loop\":true}"));
        // step1 dieu kien luon dung -> nextStepOrderIfTrue tro VE CHINH NO -> vong lap vo han.
        BackendStepDto s1 = step(1, "u1", 1, null, ConditionOperator.EXISTS, FieldMappingSourceType.STEP_RESPONSE, 1, "loop", null);
        EndpointResponseDto config = endpoint(true, s1);

        assertThatThrownBy(() -> engine.handle(config, Map.of(), Map.of(), null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-BRANCH-LOOP");
    }

    // ---- 4 operator ----

    @Test
    void operator_EQUALS_dung() {
        stubCall(up1, json("{\"code\":\"A\"}"));
        stubCall(up2, json("{\"r\":\"eq-true\"}"));
        BackendStepDto s1 = step(1, "u1", 2, null, ConditionOperator.EQUALS, FieldMappingSourceType.STEP_RESPONSE, 1, "code", "A");
        JsonNode result = engine.handle(endpoint(true, s1, plainStep(2, "u2")), Map.of(), Map.of(), null);
        assertThat(result.get("r").asText()).isEqualTo("eq-true");
    }

    @Test
    void operator_EQUALS_sai() {
        stubCall(up1, json("{\"code\":\"B\"}"));
        BackendStepDto s1 = step(1, "u1", 2, null, ConditionOperator.EQUALS, FieldMappingSourceType.STEP_RESPONSE, 1, "code", "A");
        JsonNode result = engine.handle(endpoint(true, s1, plainStep(2, "u2")), Map.of(), Map.of(), null);
        // false -> nextStepOrderIfFalse = null -> ket thuc tai step1
        assertThat(result.get("code").asText()).isEqualTo("B");
    }

    @Test
    void operator_NOT_EQUALS() {
        stubCall(up1, json("{\"code\":\"B\"}"));
        stubCall(up2, json("{\"r\":\"not-eq-true\"}"));
        BackendStepDto s1 = step(1, "u1", 2, null, ConditionOperator.NOT_EQUALS, FieldMappingSourceType.STEP_RESPONSE, 1, "code", "A");
        JsonNode result = engine.handle(endpoint(true, s1, plainStep(2, "u2")), Map.of(), Map.of(), null);
        assertThat(result.get("r").asText()).isEqualTo("not-eq-true");
    }

    @Test
    void operator_EXISTS() {
        stubCall(up1, json("{\"field\":\"co-gia-tri\"}"));
        stubCall(up2, json("{\"r\":\"exists-true\"}"));
        BackendStepDto s1 = step(1, "u1", 2, null, ConditionOperator.EXISTS, FieldMappingSourceType.STEP_RESPONSE, 1, "field", null);
        JsonNode result = engine.handle(endpoint(true, s1, plainStep(2, "u2")), Map.of(), Map.of(), null);
        assertThat(result.get("r").asText()).isEqualTo("exists-true");
    }

    @Test
    void operator_NOT_EXISTS() {
        stubCall(up1, json("{}"));
        stubCall(up2, json("{\"r\":\"not-exists-true\"}"));
        BackendStepDto s1 = step(1, "u1", 2, null, ConditionOperator.NOT_EXISTS, FieldMappingSourceType.STEP_RESPONSE, 1, "field", null);
        JsonNode result = engine.handle(endpoint(true, s1, plainStep(2, "u2")), Map.of(), Map.of(), null);
        assertThat(result.get("r").asText()).isEqualTo("not-exists-true");
    }

    // ---- 4 toan tu so sanh SO moi them (>,>=,<,<=) - dung cho case "response <=3 goi X voi
    // tham so A, >3 goi CUNG X voi tham so B" (2 step rieng, cung 1 co che re nhanh da co). ----

    @Test
    void operator_GREATER_THAN_dungKhiSoJsonThatLonHon() {
        stubCall(up1, json("{\"count\":5}"));
        stubCall(up2, json("{\"r\":\"gt-true\"}"));
        BackendStepDto s1 = step(1, "u1", 2, null, ConditionOperator.GREATER_THAN, FieldMappingSourceType.STEP_RESPONSE, 1, "count", "3");
        JsonNode result = engine.handle(endpoint(true, s1, plainStep(2, "u2")), Map.of(), Map.of(), null);
        assertThat(result.get("r").asText()).isEqualTo("gt-true");
    }

    @Test
    void operator_GREATER_THAN_saiKhiSoNhoHonHoacBang() {
        stubCall(up1, json("{\"count\":3}"));
        BackendStepDto s1 = step(1, "u1", 2, null, ConditionOperator.GREATER_THAN, FieldMappingSourceType.STEP_RESPONSE, 1, "count", "3");
        JsonNode result = engine.handle(endpoint(true, s1, plainStep(2, "u2")), Map.of(), Map.of(), null);
        // false -> nextStepOrderIfFalse = null -> ket thuc tai step1
        assertThat(result.get("count").asInt()).isEqualTo(3);
    }

    @Test
    void operator_LESS_THAN_OR_EQUAL_dungKhiSoBang() {
        stubCall(up1, json("{\"count\":3}"));
        stubCall(up2, json("{\"r\":\"lte-true\"}"));
        BackendStepDto s1 = step(1, "u1", 2, null, ConditionOperator.LESS_THAN_OR_EQUAL, FieldMappingSourceType.STEP_RESPONSE, 1, "count", "3");
        JsonNode result = engine.handle(endpoint(true, s1, plainStep(2, "u2")), Map.of(), Map.of(), null);
        assertThat(result.get("r").asText()).isEqualTo("lte-true");
    }

    @Test
    void operator_GREATER_THAN_OR_EQUAL_dungVoiGiaTriDangChuoiSo() {
        // "count" tra ve dang CHUOI (vd tu 1 API tra text) - van phai parse duoc thanh so.
        stubCall(up1, json("{\"count\":\"5\"}"));
        stubCall(up2, json("{\"r\":\"gte-true\"}"));
        BackendStepDto s1 = step(1, "u1", 2, null, ConditionOperator.GREATER_THAN_OR_EQUAL, FieldMappingSourceType.STEP_RESPONSE, 1, "count", "5");
        JsonNode result = engine.handle(endpoint(true, s1, plainStep(2, "u2")), Map.of(), Map.of(), null);
        assertThat(result.get("r").asText()).isEqualTo("gte-true");
    }

    @Test
    void operator_LESS_THAN_giaTriResponseKhongTonTai_traFalseKhongThrow() {
        stubCall(up1, json("{}")); // khong co field "count"
        BackendStepDto s1 = step(1, "u1", 2, null, ConditionOperator.LESS_THAN, FieldMappingSourceType.STEP_RESPONSE, 1, "count", "3");
        JsonNode result = engine.handle(endpoint(true, s1, plainStep(2, "u2")), Map.of(), Map.of(), null);
        // khong throw, coi nhu false (giong het cach EQUALS xu ly "exists") - ket thuc
        // tai step1, response goc van la {} (khong co field "count").
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    void operator_GREATER_THAN_giaTriResponseKhongPhaiSo_throwBusinessException() {
        stubCall(up1, json("{\"count\":\"khong-phai-so\"}"));
        BackendStepDto s1 = step(1, "u1", 2, null, ConditionOperator.GREATER_THAN, FieldMappingSourceType.STEP_RESPONSE, 1, "count", "3");
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"));

        assertThatThrownBy(() -> engine.handle(config, Map.of(), Map.of(), null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-CONDITION-NOT-NUMERIC");
    }

    // ---- Override connectTimeoutMs/readTimeoutMs theo tung BackendStep - engine phai CHUYEN
    // DUNG gia tri (hoac null) tu step xuong upstreamHttpExecutor.call(), khong tu y doi. ----

    @Test
    void step_coOverrideTimeout_duocChuyenDungXuongUpstreamHttpExecutor() {
        stubCall(up1, json("{\"v\":1}"));
        BackendStepDto stepWithOverride = new BackendStepDto(null, 1, "step1", GatewayMethod.GET, "/x", "u1", "up1",
                false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                750, 5000, // connectTimeoutMs/readTimeoutMs override rieng cho step nay
                null, null, // maxConcurrentCallsOverride/maxWaitDurationMsOverride: khong tach Bulkhead rieng
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        engine.handle(endpoint(true, stepWithOverride), Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<Integer> connectCaptor = org.mockito.ArgumentCaptor.forClass(Integer.class);
        org.mockito.ArgumentCaptor<Integer> readCaptor = org.mockito.ArgumentCaptor.forClass(Integer.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up1), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(),
                any(), connectCaptor.capture(), readCaptor.capture(), any(), any(), any());
        assertThat(connectCaptor.getValue()).isEqualTo(750);
        assertThat(readCaptor.getValue()).isEqualTo(5000);
    }

    @Test
    void step_khongOverrideTimeout_chuyenNullXuongUpstreamHttpExecutor() {
        stubCall(up1, json("{\"v\":1}"));

        engine.handle(endpoint(true, plainStep(1, "u1")), Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<Integer> connectCaptor = org.mockito.ArgumentCaptor.forClass(Integer.class);
        org.mockito.ArgumentCaptor<Integer> readCaptor = org.mockito.ArgumentCaptor.forClass(Integer.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up1), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(),
                any(), connectCaptor.capture(), readCaptor.capture(), any(), any(), any());
        assertThat(connectCaptor.getValue()).isNull();
        assertThat(readCaptor.getValue()).isNull();
    }

    // ---- QUERY_PARAM (nguon FieldMapping moi - doc query param cua chinh client, khong phai response step nao) ----

    @Test
    void queryParamMapping_forwardDungGiaTriVaoQuery() {
        stubCall(up1, json("{\"v\":1}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.QUERY_PARAM, null, "staffCode", null, null, null,
                1, MappingTargetType.QUERY, "staffCode", 0, null);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1")), List.of(mapping), null, null, false, 86400, false, false, 300, List.of(), null);

        engine.handle(config, Map.of(), Map.of("staffCode", new String[]{"QUITT"}), null);

        org.mockito.ArgumentCaptor<String> urlCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up1), any(), urlCaptor.capture(), any(), any(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
        assertThat(urlCaptor.getValue()).contains("staffCode=QUITT");
    }

    @Test
    void queryParamMapping_forwardDungGiaTriVaoBodyField() {
        stubCall(up1, json("{\"v\":1}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.QUERY_PARAM, null, "staffCode", null, null, null,
                1, MappingTargetType.BODY_FIELD, "staffCode", 0, null);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1")), List.of(mapping), null, null, false, 86400, false, false, 300, List.of(), null);

        engine.handle(config, Map.of(), Map.of("staffCode", new String[]{"QUITT"}), null);

        org.mockito.ArgumentCaptor<JsonNode> bodyCaptor = org.mockito.ArgumentCaptor.forClass(JsonNode.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up1), any(), any(), any(), bodyCaptor.capture(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
        assertThat(bodyCaptor.getValue().get("staffCode").asText()).isEqualTo("QUITT");
    }

    @Test
    void queryParamMapping_khongCoGiaTri_traNullKhongThrow() {
        stubCall(up1, json("{\"v\":1}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.QUERY_PARAM, null, "staffCode", null, null, null,
                1, MappingTargetType.BODY_FIELD, "staffCode", 0, null);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1")), List.of(mapping), null, null, false, 86400, false, false, 300, List.of(), null);

        // Khong truyen queryParams nao ca (Map.of()) - staffCode khong ton tai, phai ra null, khong throw.
        engine.handle(config, Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<JsonNode> bodyCaptor = org.mockito.ArgumentCaptor.forClass(JsonNode.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up1), any(), any(), any(), bodyCaptor.capture(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
        assertThat(bodyCaptor.getValue().get("staffCode").isNull()).isTrue();
    }

    // ---- CONSTANT (nguon FieldMapping moi - hang so co dinh, khong doc tu request/response) - dung cho
    // case "2 nhanh re cung goi 1 API nhung fix cung khac nhau tham so, khong phu thuoc input". ----

    @Test
    void constantMapping_forwardDungGiaTriVaoQuery_luonLaChuoiTextNguyenBan() {
        stubCall(up1, json("{\"v\":1}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.CONSTANT, null, null, null, null, "low",
                1, MappingTargetType.QUERY, "priority", 0, null);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1")), List.of(mapping), null, null, false, 86400, false, false, 300, List.of(), null);

        engine.handle(config, Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<String> urlCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up1), any(), urlCaptor.capture(), any(), any(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
        assertThat(urlCaptor.getValue()).contains("priority=low");
    }

    @Test
    void constantMapping_bodyField_chuoiSoTuDongThanhSoJSON() {
        stubCall(up1, json("{\"v\":1}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.CONSTANT, null, null, null, null, "3",
                1, MappingTargetType.BODY_FIELD, "priority", 0, null);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1")), List.of(mapping), null, null, false, 86400, false, false, 300, List.of(), null);

        engine.handle(config, Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<JsonNode> bodyCaptor = org.mockito.ArgumentCaptor.forClass(JsonNode.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up1), any(), any(), any(), bodyCaptor.capture(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
        // "3" parse duoc nhu JSON -> so JSON THAT (isNumber), khong phai chuoi "3".
        assertThat(bodyCaptor.getValue().get("priority").isNumber()).isTrue();
        assertThat(bodyCaptor.getValue().get("priority").asInt()).isEqualTo(3);
    }

    @Test
    void constantMapping_bodyField_chuoiKhongPhaiJson_giuNguyenDangChuoi() {
        stubCall(up1, json("{\"v\":1}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.CONSTANT, null, null, null, null, "low",
                1, MappingTargetType.BODY_FIELD, "priority", 0, null);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1")), List.of(mapping), null, null, false, 86400, false, false, 300, List.of(), null);

        engine.handle(config, Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<JsonNode> bodyCaptor = org.mockito.ArgumentCaptor.forClass(JsonNode.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up1), any(), any(), any(), bodyCaptor.capture(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
        // "low" khong phai JSON hop le -> fallback giu nguyen chuoi text.
        assertThat(bodyCaptor.getValue().get("priority").isTextual()).isTrue();
        assertThat(bodyCaptor.getValue().get("priority").asText()).isEqualTo("low");
    }

    // ---- STEP_RESPONSE_ARRAY_MERGE (nguon FieldMapping moi - gop TOAN BO field cua tung
    // phan tu trong 1 mang thanh 1 OBJECT DUY NHAT) - dung cho case that "response la
    // List<HashMap>, trich tung key/value cua tung HashMap thanh duy nhat 1 HashMap". ----

    @Test
    void arrayMergeMapping_gopNObject1KeyThanh1ObjectDuyNhat() {
        stubCall(up1, json("{\"data\":[{\"500173047\":\"1\"},{\"400017940\":\"1\"},{\"400019046\":\"1\"}]}"));
        stubCall(up2, json("{\"ok\":true}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE_ARRAY_MERGE, 1, null, "data", null, null,
                2, MappingTargetType.BODY_FIELD, "$body", 0, null);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1"), plainStep(2, "u2")), List.of(mapping), null, null, false, 86400, false, false, 300, List.of(), null);

        engine.handle(config, Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<JsonNode> bodyCaptor = org.mockito.ArgumentCaptor.forClass(JsonNode.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up2), any(), any(), any(), bodyCaptor.capture(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
        JsonNode body = bodyCaptor.getValue();
        assertThat(body.get("500173047").asText()).isEqualTo("1");
        assertThat(body.get("400017940").asText()).isEqualTo("1");
        assertThat(body.get("400019046").asText()).isEqualTo("1");
        assertThat(body.size()).isEqualTo(3);
    }

    @Test
    void arrayMergeMapping_keyTrungNhau_phanTuDenSauGhiDe() {
        stubCall(up1, json("{\"data\":[{\"a\":\"1\"},{\"a\":\"2\"}]}"));
        stubCall(up2, json("{\"ok\":true}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE_ARRAY_MERGE, 1, null, "data", null, null,
                2, MappingTargetType.BODY_FIELD, "$body", 0, null);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1"), plainStep(2, "u2")), List.of(mapping), null, null, false, 86400, false, false, 300, List.of(), null);

        engine.handle(config, Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<JsonNode> bodyCaptor = org.mockito.ArgumentCaptor.forClass(JsonNode.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up2), any(), any(), any(), bodyCaptor.capture(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
        assertThat(bodyCaptor.getValue().get("a").asText()).isEqualTo("2");
    }

    @Test
    void arrayMergeMapping_phanTuKhongPhaiObject_boQuaKhongThrow() {
        stubCall(up1, json("{\"data\":[{\"a\":\"1\"},\"khong-phai-object\",123]}"));
        stubCall(up2, json("{\"ok\":true}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE_ARRAY_MERGE, 1, null, "data", null, null,
                2, MappingTargetType.BODY_FIELD, "$body", 0, null);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1"), plainStep(2, "u2")), List.of(mapping), null, null, false, 86400, false, false, 300, List.of(), null);

        engine.handle(config, Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<JsonNode> bodyCaptor = org.mockito.ArgumentCaptor.forClass(JsonNode.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up2), any(), any(), any(), bodyCaptor.capture(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
        assertThat(bodyCaptor.getValue().get("a").asText()).isEqualTo("1");
        assertThat(bodyCaptor.getValue().size()).isEqualTo(1);
    }

    @Test
    void arrayMergeMapping_khongTimThayMang_traObjectRongKhongThrow() {
        stubCall(up1, json("{}"));
        stubCall(up2, json("{\"ok\":true}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE_ARRAY_MERGE, 1, null, "data", null, null,
                2, MappingTargetType.BODY_FIELD, "$body", 0, null);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1"), plainStep(2, "u2")), List.of(mapping), null, null, false, 86400, false, false, 300, List.of(), null);

        engine.handle(config, Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<JsonNode> bodyCaptor = org.mockito.ArgumentCaptor.forClass(JsonNode.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up2), any(), any(), any(), bodyCaptor.capture(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
        assertThat(bodyCaptor.getValue().size()).isEqualTo(0);
    }

    // ---- onErrorStepOrder (nang luc moi - fallback khi step LOI, doc lap voi conditionOperator) ----

    @Test
    void onErrorStepOrder_stepLoiCoFallback_nhaySangStepFallback_traVeKetQuaStepFallback() {
        stubCallThrows(up1, new RuntimeException("upstream that loi"));
        stubCall(up2, json("{\"fallback\":true}"));
        BackendStepDto s1 = stepWithOnError(1, "u1", 2);
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("fallback").asBoolean()).isTrue();
    }

    @Test
    void onErrorStepOrder_null_stepLoi_throwNguyenBanYHetTruocDay() {
        // Regression: step KHONG khai bao onErrorStepOrder phai throw dung nguyen ban
        // exception, KHONG bi nuot am tham - step 2 KHONG duoc goi (chuoi dung ngay).
        stubCallThrows(up1, new BusinessException("GW-UPSTREAM-TIMEOUT", "timeout"));
        BackendStepDto s1 = plainStep(1, "u1");
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"));

        assertThatThrownBy(() -> engine.handle(config, Map.of(), Map.of(), null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-UPSTREAM-TIMEOUT");
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(up2), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void onErrorStepOrder_troToiChinhNo_throwGW_BRANCH_LOOP() {
        stubCallThrows(up1, new RuntimeException("boom"));
        BackendStepDto s1 = stepWithOnError(1, "u1", 1);
        EndpointResponseDto config = endpoint(true, s1);

        assertThatThrownBy(() -> engine.handle(config, Map.of(), Map.of(), null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-BRANCH-LOOP");
    }

    @Test
    void onErrorStepOrder_stepFallbackKhongCoDieuKienRieng_DungLaiKhongRoiTiepStepSau() {
        // Dung y het pattern bug branch-cascade da fix truoc do: step fallback (step2) la
        // DICH cua 1 "buoc nhay dac biet" (onErrorStepOrder) nhung ban than khong co dieu
        // kien rieng -> phai DUNG LAI, khong duoc tu dong roi tiep sang step3.
        stubCallThrows(up1, new RuntimeException("boom"));
        stubCall(up2, json("{\"v\":2}"));
        BackendStepDto s1 = stepWithOnError(1, "u1", 2);
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("v").asInt()).isEqualTo(2);
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(up3), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    // ---- V5: Rule cờ (FlagRuleEvaluator) - bat dau tu step / chặn request ----

    private com.bccs.gatewaymanager.dto.FlagRuleDto.FlagConditionDto flagCond(String code, boolean expected) {
        return new com.bccs.gatewaymanager.dto.FlagRuleDto.FlagConditionDto(code, expected);
    }

    @Test
    void flagRuleStartStep_khop_chuoiChayTuStartStepOrder_boQuaCacStepTruoc() {
        // Rule: VIP=true -> bat dau tu step 3. Step 1 KHONG bao gio duoc goi.
        when(flagRegistryCache.get("VIP")).thenReturn(true);
        stubCall(up3, json("{\"v\":3}"));
        var rule = new com.bccs.gatewaymanager.dto.FlagRuleDto("r1", "Luong VIP", 1,
                List.of(List.of(flagCond("VIP", true))),
                com.bccs.gatewaymanager.dto.FlagRuleDto.ACTION_START_STEP, 3, null, null);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1"), plainStep(2, "u2"), plainStep(3, "u3")), List.of(), null, null,
                false, 86400, false, false, 300, List.of(rule), null);

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("v").asInt()).isEqualTo(3);
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(up1), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void flagRuleKhongKhop_chayTuStepNhoNhatYHetHanhViCu() {
        // Khong rule khop -> chay tu step nho nhat Y HET hanh vi cu: up1 PHAI duoc goi
        // (neu rule khop nham thi chuoi se bat dau tu step 2 va up1 khong bao gio chay).
        when(flagRegistryCache.get("VIP")).thenReturn(false);
        stubCall(up1, json("{\"v\":1}"));
        stubCall(up2, json("{\"v\":2}"));
        var rule = new com.bccs.gatewaymanager.dto.FlagRuleDto("r1", "Luong VIP", 1,
                List.of(List.of(flagCond("VIP", true))),
                com.bccs.gatewaymanager.dto.FlagRuleDto.ACTION_START_STEP, 2, null, null);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1"), plainStep(2, "u2")), List.of(), null, null,
                false, 86400, false, false, 300, List.of(rule), null);

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("v").asInt()).isEqualTo(2);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up1), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void flagRuleBLOCK_khop_throwBusinessExceptionKhongGoiUpstreamNao() {
        when(flagRegistryCache.get("MAINTENANCE")).thenReturn(true);
        var rule = new com.bccs.gatewaymanager.dto.FlagRuleDto("r2", "Bao tri", 1,
                List.of(List.of(flagCond("MAINTENANCE", true))),
                com.bccs.gatewaymanager.dto.FlagRuleDto.ACTION_BLOCK, null, "GW-FEATURE-DISABLED", "Tinh nang tam dung");
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1")), List.of(), null, null,
                false, 86400, false, false, 300, List.of(rule), null);

        assertThatThrownBy(() -> engine.handle(config, Map.of(), Map.of(), null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-FEATURE-DISABLED");
        org.mockito.Mockito.verifyNoInteractions(upstreamHttpExecutor);
    }

    @Test
    void flagOverrides_tuThuNgay_gheDeGiaTriCache() {
        // Cache that VIP=false; try-panel override VIP=true -> chuoi chay tu step 2.
        // Khong stub flagRegistryCache.get - override PHAI ghe de ca gia tri cache mock
        // (stub nao se tro nen "thua" neu evaluate khong bao gio cham cache).
        stubCall(up2, json("{\"v\":2}"));
        var rule = new com.bccs.gatewaymanager.dto.FlagRuleDto("r1", "Luong VIP", 1,
                List.of(List.of(flagCond("VIP", true))),
                com.bccs.gatewaymanager.dto.FlagRuleDto.ACTION_START_STEP, 2, null, null);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1"), plainStep(2, "u2")), List.of(), null, null,
                false, 86400, false, false, 300, List.of(rule), null);

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null, Map.of("VIP", true));

        assertThat(result.get("v").asInt()).isEqualTo(2);
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(up1), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    // ---- V5: dieu kien re nhanh tai step voi nguon SYSTEM_FLAG ----
    // LUU Y engine: step co dieu kien van THUC HIEN HTTP call cua chinh no TRUOC,
    // dieu kien chi quyet dinh step TIEP THEO (determineNextStepOrder sau executeStep).

    @Test
    void systemFlagCondition_cờBat_chayNhanhIfTrue() {
        when(flagRegistryCache.get("VIP")).thenReturn(true);
        stubCall(up1, json("{\"step\":1}"));
        stubCall(up2, json("{\"branch\":\"vip\"}"));
        BackendStepDto s1 = step(1, "u1", 2, 3, ConditionOperator.EQUALS,
                FieldMappingSourceType.SYSTEM_FLAG, null, "VIP", "true");
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("branch").asText()).isEqualTo("vip");
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(up3), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void systemFlagCondition_cờTat_chayNhanhIfFalse() {
        // Cờ = false, dieu kien mong cờ = true -> dieu kien SAI -> nhay nextIfFalse (step 3).
        when(flagRegistryCache.get("VIP")).thenReturn(false);
        stubCall(up1, json("{\"step\":1}"));
        stubCall(up3, json("{\"branch\":\"normal\"}"));
        BackendStepDto s1 = step(1, "u1", 2, 3, ConditionOperator.EQUALS,
                FieldMappingSourceType.SYSTEM_FLAG, null, "VIP", "true");
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("branch").asText()).isEqualTo("normal");
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(up2), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void systemFlagCondition_kyVongCờTat_cờDangTat_khopDieuKienChayNhanhIfTrue() {
        // Nguoc lai: dieu kien mong cờ = false, cờ DANG false -> dieu kien DUNG ->
        // nhay nextIfTrue (step 2) - xac nhan expected="false" duoc so sanh dung chieu.
        when(flagRegistryCache.get("VIP")).thenReturn(false);
        stubCall(up1, json("{\"step\":1}"));
        stubCall(up2, json("{\"branch\":\"normal\"}"));
        BackendStepDto s1 = step(1, "u1", 2, 3, ConditionOperator.EQUALS,
                FieldMappingSourceType.SYSTEM_FLAG, null, "VIP", "false");
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("branch").asText()).isEqualTo("normal");
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(up3), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void systemFlagCondition_cờMatKhoiCache_coiLaFalse_failsafe() {
        // Cờ da bi xoa sau khi endpoint luu - KHONG stub get() (mock default = false,
        // tuong duong "cờ mat khoi cache coi la false"): dieu kien mong "true" -> SAI ->
        // nhanh nextIfFalse van di dung huong, KHONG throw (fail-safe).
        stubCall(up1, json("{\"step\":1}"));
        stubCall(up3, json("{\"branch\":\"normal\"}"));
        BackendStepDto s1 = step(1, "u1", 2, 3, ConditionOperator.EQUALS,
                FieldMappingSourceType.SYSTEM_FLAG, null, "VIP", "true");
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("branch").asText()).isEqualTo("normal");
    }

    // ---- V5b: dieu kien cờ NHIEU NHOM tai step (AND trong nhom, OR giua nhom) ----

    /** Step SYSTEM_FLAG mang conditionFlagGroups - ghi de helper step() chinh. */
    private BackendStepDto stepWithFlagGroups(int order, String upstreamId, List<List<com.bccs.gatewaymanager.dto.FlagRuleDto.FlagConditionDto>> groups,
                                              Integer nextIfTrue, Integer nextIfFalse) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", upstreamId, "up" + order,
                false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                null, null,
                null, null,
                FieldMappingSourceType.SYSTEM_FLAG, null, "FALLBACK", ConditionOperator.EQUALS, "true", groups, null,
                nextIfTrue, nextIfFalse, null, null, null, null, null, null);
    }

    @Test
    void systemFlagGroups_1NhomTatCaDung_chayNhanhIfTrue() {
        when(flagRegistryCache.get("VIP")).thenReturn(true);
        when(flagRegistryCache.get("PROMO")).thenReturn(true);
        stubCall(up1, json("{\"step\":1}"));
        stubCall(up2, json("{\"branch\":\"vip\"}"));
        BackendStepDto s1 = stepWithFlagGroups(1, "u1",
                List.of(List.of(flagCond("VIP", true), flagCond("PROMO", true))), 2, 3);
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("branch").asText()).isEqualTo("vip");
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(up3), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void systemFlagGroups_1DieuKienSaiTrongNhom_caNhomSai_chayNhanhIfFalse() {
        // AND trong nhom: VIP=true nhung PROMO=false -> nhom 1 khong thoa -> nhanh false.
        when(flagRegistryCache.get("VIP")).thenReturn(true);
        when(flagRegistryCache.get("PROMO")).thenReturn(false);
        stubCall(up1, json("{\"step\":1}"));
        stubCall(up3, json("{\"branch\":\"normal\"}"));
        BackendStepDto s1 = stepWithFlagGroups(1, "u1",
                List.of(List.of(flagCond("VIP", true), flagCond("PROMO", true))), 2, 3);
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("branch").asText()).isEqualTo("normal");
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(up2), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void systemFlagGroups_nhom1Sai_nhom2Dung_khoNhomDung_chayNhanhIfTrue() {
        // OR giua nhom: nhom 1 (VIP=false ∧ PROMO=true) sai ngay tai VIP (mock default
        // PROMO=false nen khong can stub - tranh UnnecessaryStubbing), nhom 2 (B2B=true)
        // dung -> DIEU KIEN DUNG.
        when(flagRegistryCache.get("VIP")).thenReturn(false);
        when(flagRegistryCache.get("B2B")).thenReturn(true);
        stubCall(up1, json("{\"step\":1}"));
        stubCall(up2, json("{\"branch\":\"b2b\"}"));
        BackendStepDto s1 = stepWithFlagGroups(1, "u1",
                List.of(
                        List.of(flagCond("VIP", true), flagCond("PROMO", true)),
                        List.of(flagCond("B2B", true))),
                2, 3);
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("branch").asText()).isEqualTo("b2b");
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(up3), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void systemFlagGroups_caNhomSai_chayNhanhIfFalse() {
        when(flagRegistryCache.get("VIP")).thenReturn(false);
        when(flagRegistryCache.get("B2B")).thenReturn(false);
        stubCall(up1, json("{\"step\":1}"));
        stubCall(up3, json("{\"branch\":\"normal\"}"));
        BackendStepDto s1 = stepWithFlagGroups(1, "u1",
                List.of(List.of(flagCond("VIP", true)), List.of(flagCond("B2B", true))), 2, 3);
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("branch").asText()).isEqualTo("normal");
    }

    @Test
    void systemFlagGroups_nhomRong_boQua_khongThrow() {
        // Nhom rong duoc bo qua (engine skip) - du lieu cu/hong khong lam sap request.
        when(flagRegistryCache.get("B2B")).thenReturn(true);
        stubCall(up1, json("{\"step\":1}"));
        stubCall(up2, json("{\"branch\":\"b2b\"}"));
        BackendStepDto s1 = stepWithFlagGroups(1, "u1",
                List.of(List.of(), List.of(flagCond("B2B", true))), 2, 3);
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("branch").asText()).isEqualTo("b2b");
    }

    // ==================== V6: CONTAINS/NOT_CONTAINS + conditionGroups (nhieu nguon, nhieu nhom AND/OR) ====================

    @Test
    void contains_mangChuaGiaTri_dieuKienDung_chayNhanhIfTrue() {
        stubCall(up1, json("{\"step\":1}"));
        stubCall(up2, json("{\"branch\":\"co-quyen-a\"}"));
        BackendStepDto s1 = step(1, "u1", 2, 3, ConditionOperator.CONTAINS,
                FieldMappingSourceType.REQUEST_BODY, null, "permissionCodes", "A");
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), "{\"permissionCodes\":[\"A\",\"B\"]}");

        assertThat(result.get("branch").asText()).isEqualTo("co-quyen-a");
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(up3), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void contains_mangKhongChuaGiaTri_dieuKienSai_chayNhanhIfFalse() {
        stubCall(up1, json("{\"step\":1}"));
        stubCall(up3, json("{\"branch\":\"khong-co-quyen-a\"}"));
        BackendStepDto s1 = step(1, "u1", 2, 3, ConditionOperator.CONTAINS,
                FieldMappingSourceType.REQUEST_BODY, null, "permissionCodes", "A");
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), "{\"permissionCodes\":[\"B\",\"C\"]}");

        assertThat(result.get("branch").asText()).isEqualTo("khong-co-quyen-a");
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(up2), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void contains_giaTriKhongPhaiMang_coiLaKhongKhop_failSafe() {
        // permissionCodes la 1 string, khong phai mang -> CONTAINS fail-safe = khong khop.
        stubCall(up1, json("{\"step\":1}"));
        stubCall(up3, json("{\"branch\":\"normal\"}"));
        BackendStepDto s1 = step(1, "u1", 2, 3, ConditionOperator.CONTAINS,
                FieldMappingSourceType.REQUEST_BODY, null, "permissionCodes", "A");
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), "{\"permissionCodes\":\"A\"}");

        assertThat(result.get("branch").asText()).isEqualTo("normal");
    }

    @Test
    void notContains_mangKhongChuaGiaTri_dieuKienDung_chayNhanhIfTrue() {
        stubCall(up1, json("{\"step\":1}"));
        stubCall(up2, json("{\"branch\":\"khong-co-quyen-a\"}"));
        BackendStepDto s1 = step(1, "u1", 2, 3, ConditionOperator.NOT_CONTAINS,
                FieldMappingSourceType.REQUEST_BODY, null, "permissionCodes", "A");
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), "{\"permissionCodes\":[\"B\",\"C\"]}");

        assertThat(result.get("branch").asText()).isEqualTo("khong-co-quyen-a");
    }

    @Test
    void notContains_fieldKhongTonTaiTrongBody_coiLaDung_failSafe() {
        // Mirror NOT_EQUALS: field khong co trong body -> !exists -> NOT_CONTAINS = dung.
        stubCall(up1, json("{\"step\":1}"));
        stubCall(up2, json("{\"branch\":\"ok\"}"));
        BackendStepDto s1 = step(1, "u1", 2, 3, ConditionOperator.NOT_CONTAINS,
                FieldMappingSourceType.REQUEST_BODY, null, "permissionCodes", "A");
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), "{}");

        assertThat(result.get("branch").asText()).isEqualTo("ok");
    }

    private com.bccs.gatewaymanager.dto.ConditionRuleDto rule(FieldMappingSourceType sourceType, String sourceField,
                                                                Integer sourceStepOrder, ConditionOperator operator, String expectedValue) {
        return new com.bccs.gatewaymanager.dto.ConditionRuleDto(sourceType, sourceField, sourceStepOrder, operator, expectedValue);
    }

    /** Step V6 mang conditionGroups - ghi de helper step() chinh (mirror stepWithFlagGroups). */
    private BackendStepDto stepWithConditionGroups(int order, String upstreamId,
                                                     List<List<com.bccs.gatewaymanager.dto.ConditionRuleDto>> groups,
                                                     Integer nextIfTrue, Integer nextIfFalse) {
        return stepWithFlagGroupsAndConditionGroups(order, upstreamId, null, groups, nextIfTrue, nextIfFalse);
    }

    /** Step co CA conditionFlagGroups (SYSTEM_FLAG) VA conditionGroups - dung rieng cho test uu tien (regression). */
    private BackendStepDto stepWithFlagGroupsAndConditionGroups(int order, String upstreamId,
            List<List<com.bccs.gatewaymanager.dto.FlagRuleDto.FlagConditionDto>> flagGroups,
            List<List<com.bccs.gatewaymanager.dto.ConditionRuleDto>> conditionGroups,
            Integer nextIfTrue, Integer nextIfFalse) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", upstreamId, "up" + order,
                false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                null, null,
                null, null,
                FieldMappingSourceType.SYSTEM_FLAG, null, "VIP", ConditionOperator.EQUALS, "true",
                flagGroups, conditionGroups,
                nextIfTrue, nextIfFalse, null, null, null, null, null, null);
    }

    @Test
    void conditionGroups_andTrongNhom_caHaiDung_chayNhanhIfTrue() {
        // Case thuc te cua nguoi dung: permissionCodes CONTAINS "A" VA fieldX EQUALS "true".
        stubCall(up1, json("{\"step\":1}"));
        stubCall(up2, json("{\"branch\":\"vip\"}"));
        BackendStepDto s1 = stepWithConditionGroups(1, "u1",
                List.of(List.of(
                        rule(FieldMappingSourceType.REQUEST_BODY, "permissionCodes", null, ConditionOperator.CONTAINS, "A"),
                        rule(FieldMappingSourceType.REQUEST_BODY, "fieldX", null, ConditionOperator.EQUALS, "true"))),
                2, 3);
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), "{\"permissionCodes\":[\"A\",\"B\"],\"fieldX\":\"true\"}");

        assertThat(result.get("branch").asText()).isEqualTo("vip");
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(up3), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void conditionGroups_andTrongNhom_1DieuKienSai_caNhomSai_chayNhanhIfFalse() {
        stubCall(up1, json("{\"step\":1}"));
        stubCall(up3, json("{\"branch\":\"normal\"}"));
        BackendStepDto s1 = stepWithConditionGroups(1, "u1",
                List.of(List.of(
                        rule(FieldMappingSourceType.REQUEST_BODY, "permissionCodes", null, ConditionOperator.CONTAINS, "A"),
                        rule(FieldMappingSourceType.REQUEST_BODY, "fieldX", null, ConditionOperator.EQUALS, "true"))),
                2, 3);
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        // Co quyen A nhung fieldX=false -> AND trong nhom sai.
        JsonNode result = engine.handle(config, Map.of(), Map.of(), "{\"permissionCodes\":[\"A\"],\"fieldX\":\"false\"}");

        assertThat(result.get("branch").asText()).isEqualTo("normal");
    }

    @Test
    void conditionGroups_orGiuaNhom_nhom1Sai_nhom2Dung_honHopRequestBodyVaStepResponse_chayNhanhIfTrue() {
        // Nhom 1: permissionCodes CONTAINS "A" (sai). Nhom 2: STEP_RESPONSE step1.ok EQUALS "true" (dung).
        stubCall(up1, json("{\"ok\":\"true\"}"));
        stubCall(up2, json("{\"branch\":\"ok\"}"));
        BackendStepDto s1 = stepWithConditionGroups(1, "u1",
                List.of(
                        List.of(rule(FieldMappingSourceType.REQUEST_BODY, "permissionCodes", null, ConditionOperator.CONTAINS, "A")),
                        List.of(rule(FieldMappingSourceType.STEP_RESPONSE, "ok", 1, ConditionOperator.EQUALS, "true"))),
                2, 3);
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), "{\"permissionCodes\":[\"B\"]}");

        assertThat(result.get("branch").asText()).isEqualTo("ok");
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(up3), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void conditionGroups_caNhomSai_chayNhanhIfFalse() {
        stubCall(up1, json("{\"ok\":\"false\"}"));
        stubCall(up3, json("{\"branch\":\"normal\"}"));
        BackendStepDto s1 = stepWithConditionGroups(1, "u1",
                List.of(
                        List.of(rule(FieldMappingSourceType.REQUEST_BODY, "permissionCodes", null, ConditionOperator.CONTAINS, "A")),
                        List.of(rule(FieldMappingSourceType.STEP_RESPONSE, "ok", 1, ConditionOperator.EQUALS, "true"))),
                2, 3);
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), "{\"permissionCodes\":[\"B\"]}");

        assertThat(result.get("branch").asText()).isEqualTo("normal");
    }

    @Test
    void conditionGroups_uuTienHonSystemFlagVaDieuKienDonLe_khiCungKhaiBao() {
        // Regression quan trong: step co CA conditionFlagGroups (SYSTEM_FLAG) VA conditionGroups
        // -> engine PHAI dung conditionGroups, bo qua HOAN TOAN nhanh SYSTEM_FLAG (khong stub
        // flagRegistryCache.get("VIP") - neu engine lo di doc nhanh SYSTEM_FLAG thi UnnecessaryStubbing
        // se KHONG bao loi nhung verify(never) ben duoi se that bai, bat loi dung cho nhanh).
        stubCall(up1, json("{\"step\":1}"));
        stubCall(up2, json("{\"branch\":\"conditionGroups-thang\"}"));
        BackendStepDto s1 = stepWithFlagGroupsAndConditionGroups(1, "u1",
                List.of(List.of(flagCond("VIP", true))),
                List.of(List.of(rule(FieldMappingSourceType.REQUEST_BODY, "permissionCodes", null, ConditionOperator.CONTAINS, "A"))),
                2, 3);
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), "{\"permissionCodes\":[\"A\"]}");

        assertThat(result.get("branch").asText()).isEqualTo("conditionGroups-thang");
        org.mockito.Mockito.verify(flagRegistryCache, org.mockito.Mockito.never()).get("VIP");
    }

    // ==================== Giai doan 0-1: toan tu moi, nguon QUERY_PARAM/HEADER, trace dieu kien ====================

    private void lenientCall(UpstreamService upstream, JsonNode response) {
        lenient().when(upstreamHttpExecutor.call(eq(upstream), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any()))
                .thenReturn(response);
    }

    /** Chay chuoi 3 step: step 1 la dieu kien, dung -> step 2 (branch=T), sai -> step 3 (branch=F). */
    private JsonNode runWithCondition(BackendStepDto s1, String body, Map<String, String> headers, Map<String, String[]> queryParams) {
        lenientCall(up1, json("{\"step\":1}"));
        lenientCall(up2, json("{\"branch\":\"T\"}"));
        lenientCall(up3, json("{\"branch\":\"F\"}"));
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));
        return engine.handle(config, Map.of(), queryParams, body, null, headers);
    }

    private BackendStepDto bodyCondition(ConditionOperator op, String field, String expected) {
        return step(1, "u1", 2, 3, op, FieldMappingSourceType.REQUEST_BODY, null, field, expected);
    }

    @Test
    void in_giaTriNamTrongDanhSach_chayNhanhDung() {
        JsonNode result = runWithCondition(bodyCondition(ConditionOperator.IN, "code", "[\"TT_02\",\"TT_03\"]"),
                "{\"code\":\"TT_03\"}", Map.of(), Map.of());
        assertThat(result.get("branch").asText()).isEqualTo("T");
    }

    @Test
    void notIn_giaTriKhongCoTrongDanhSach_chayNhanhDung() {
        JsonNode result = runWithCondition(bodyCondition(ConditionOperator.NOT_IN, "code", "[\"TT_02\",\"TT_03\"]"),
                "{\"code\":\"TT_09\"}", Map.of(), Map.of());
        assertThat(result.get("branch").asText()).isEqualTo("T");
    }

    @Test
    void in_expectedValueKhongPhaiMang_failSafeKhongKhop() {
        // Du lieu cu/sua tay: validate da chan luc luu, runtime bao loi ro rang thay vi am tham.
        assertThatThrownBy(() -> runWithCondition(bodyCondition(ConditionOperator.IN, "code", "TT_02"),
                "{\"code\":\"TT_02\"}", Map.of(), Map.of()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-CONDITION-INVALID-VALUE");
    }

    @Test
    void startsEndsStringContains_soSanhChuoi() {
        assertThat(runWithCondition(bodyCondition(ConditionOperator.STARTS_WITH, "code", "TT_"), "{\"code\":\"TT_02\"}", Map.of(), Map.of())
                .get("branch").asText()).isEqualTo("T");
        assertThat(runWithCondition(bodyCondition(ConditionOperator.ENDS_WITH, "code", "_02"), "{\"code\":\"TT_02\"}", Map.of(), Map.of())
                .get("branch").asText()).isEqualTo("T");
        assertThat(runWithCondition(bodyCondition(ConditionOperator.STRING_CONTAINS, "code", "T_0"), "{\"code\":\"TT_02\"}", Map.of(), Map.of())
                .get("branch").asText()).isEqualTo("T");
        assertThat(runWithCondition(bodyCondition(ConditionOperator.STARTS_WITH, "code", "XX"), "{\"code\":\"TT_02\"}", Map.of(), Map.of())
                .get("branch").asText()).isEqualTo("F");
    }

    @Test
    void isEmpty_mangRongHoacThieuField_laRong() {
        assertThat(runWithCondition(bodyCondition(ConditionOperator.IS_EMPTY, "list", null), "{\"list\":[]}", Map.of(), Map.of())
                .get("branch").asText()).isEqualTo("T");
        assertThat(runWithCondition(bodyCondition(ConditionOperator.IS_EMPTY, "list", null), "{}", Map.of(), Map.of())
                .get("branch").asText()).isEqualTo("T");
        assertThat(runWithCondition(bodyCondition(ConditionOperator.IS_NOT_EMPTY, "name", null), "{\"name\":\"x\"}", Map.of(), Map.of())
                .get("branch").asText()).isEqualTo("T");
    }

    @Test
    void matchesRegex_khopToanBoChuoi() {
        assertThat(runWithCondition(bodyCondition(ConditionOperator.MATCHES_REGEX, "code", "[A-Z]{2}\\d+"), "{\"code\":\"AB12\"}", Map.of(), Map.of())
                .get("branch").asText()).isEqualTo("T");
        // Khop 1 phan (co "AB12" trong "xAB12y") nhung KHONG khop toan bo -> sai.
        assertThat(runWithCondition(bodyCondition(ConditionOperator.MATCHES_REGEX, "code", "[A-Z]{2}\\d+"), "{\"code\":\"xAB12y\"}", Map.of(), Map.of())
                .get("branch").asText()).isEqualTo("F");
    }

    @Test
    void matchesRegex_quaNganSachThoiGian_biChanKhongTreo() {
        // Chuoi rat dai + regex phai duyet het (a*b): vuot ngan sach REGEX_TIME_BUDGET -> phai bi chan, khong treo.
        String input = "a".repeat(5_000_000);
        long start = System.nanoTime();
        assertThatThrownBy(() -> runWithCondition(bodyCondition(ConditionOperator.MATCHES_REGEX, "code", "a*b"),
                "{\"code\":\"" + input + "\"}", Map.of(), Map.of()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-CONDITION-REGEX-TIMEOUT");
        assertThat(java.time.Duration.ofNanos(System.nanoTime() - start)).isLessThan(java.time.Duration.ofSeconds(10));
    }

    @Test
    void queryParamCondition_chayNhanhDung() {
        BackendStepDto s1 = step(1, "u1", 2, 3, ConditionOperator.EQUALS, FieldMappingSourceType.QUERY_PARAM, null, "tier", "gold");
        JsonNode result = runWithCondition(s1, null, Map.of(), Map.of("tier", new String[]{"gold"}));
        assertThat(result.get("branch").asText()).isEqualTo("T");
    }

    @Test
    void headerCondition_tenHeaderKhongPhanBietHoaThuong() {
        BackendStepDto s1 = step(1, "u1", 2, 3, ConditionOperator.EQUALS, FieldMappingSourceType.HEADER, null, "X-Customer-Tier", "gold");
        JsonNode result = runWithCondition(s1, null, Map.of("X-CUSTOMER-TIER", "gold"), Map.of());
        assertThat(result.get("branch").asText()).isEqualTo("T");
    }

    @Test
    void trace_fieldThieuTrongResponse_ghiRoKhongCoVaSai() {
        // Dung dung loi da gap: condition doc field KHONG co trong response -> phai ghi ro "KHONG CO", khong am tham.
        lenientCall(up1, json("{\"code\":\"SUCCESS\",\"data\":{\"haveMoreThan3Sub\":true}}"));
        lenientCall(up2, json("{\"branch\":\"T\"}"));
        lenientCall(up3, json("{\"branch\":\"F\"}"));
        BackendStepDto s1 = step(1, "u1", 2, 3, ConditionOperator.EQUALS, FieldMappingSourceType.STEP_RESPONSE, 1, "haveMoreThan3Sub", "true");
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"), plainStep(3, "u3"));

        java.util.List<StepTraceDto> trace = TraceCollector.start();
        try {
            JsonNode result = engine.handle(config, Map.of(), Map.of(), null);
            assertThat(result.get("branch").asText()).isEqualTo("F");
            assertThat(trace).anySatisfy(t -> {
                assertThat(t.conditionInfo()).contains("response step 1.haveMoreThan3Sub = KHONG CO");
                assertThat(t.conditionInfo()).endsWith("-> SAI");
            });
        } finally {
            TraceCollector.stop();
        }
    }

    // ==================== Giai doan 2-4: so sanh 2 field, cay AND/OR long nhau, co he thong trong nhom ====================

    private com.bccs.gatewaymanager.dto.ConditionRuleDto leafNode(FieldMappingSourceType st, String field,
                                                                   ConditionOperator op, String expected) {
        return new com.bccs.gatewaymanager.dto.ConditionRuleDto(st, field, null, op, expected);
    }

    private com.bccs.gatewaymanager.dto.ConditionRuleDto compareNode(FieldMappingSourceType st, String field, ConditionOperator op,
                                                                      FieldMappingSourceType cst, String cfield) {
        return new com.bccs.gatewaymanager.dto.ConditionRuleDto(st, field, null, op, null, null, null, cst, cfield, null);
    }

    private com.bccs.gatewaymanager.dto.ConditionRuleDto orGroupNode(com.bccs.gatewaymanager.dto.ConditionRuleDto... kids) {
        return com.bccs.gatewaymanager.dto.ConditionRuleDto.group("OR", List.of(kids));
    }

    private com.bccs.gatewaymanager.dto.ConditionRuleDto andGroupNode(com.bccs.gatewaymanager.dto.ConditionRuleDto... kids) {
        return com.bccs.gatewaymanager.dto.ConditionRuleDto.group("AND", List.of(kids));
    }

    @Test
    void compareHaiField_giaTriVeLaFieldKhac_dung() {
        BackendStepDto s1 = stepWithConditionGroups(1, "u1", List.of(List.of(
                compareNode(FieldMappingSourceType.REQUEST_BODY, "amount", ConditionOperator.GREATER_THAN,
                        FieldMappingSourceType.REQUEST_BODY, "limit"))), 2, 3);
        assertThat(runWithCondition(s1, "{\"amount\":10,\"limit\":5}", Map.of(), Map.of()).get("branch").asText()).isEqualTo("T");
        assertThat(runWithCondition(s1, "{\"amount\":1,\"limit\":5}", Map.of(), Map.of()).get("branch").asText()).isEqualTo("F");
    }

    @Test
    void compareHaiField_veLaThieu_khongKhop() {
        BackendStepDto s1 = stepWithConditionGroups(1, "u1", List.of(List.of(
                compareNode(FieldMappingSourceType.REQUEST_BODY, "code", ConditionOperator.EQUALS,
                        FieldMappingSourceType.REQUEST_BODY, "thieuField"))), 2, 3);
        assertThat(runWithCondition(s1, "{\"code\":\"X\"}", Map.of(), Map.of()).get("branch").asText()).isEqualTo("F");
    }

    @Test
    void cayLongNhau_AND_cha_voi_OR_con_dungDungNgu() {
        // permissionCodes CONTAINS "A" VA (fieldX = true HOAC vipLevel > 5)
        BackendStepDto s1 = stepWithConditionGroups(1, "u1", List.of(List.of(
                leafNode(FieldMappingSourceType.REQUEST_BODY, "permissionCodes", ConditionOperator.CONTAINS, "A"),
                orGroupNode(
                        leafNode(FieldMappingSourceType.REQUEST_BODY, "fieldX", ConditionOperator.EQUALS, "true"),
                        leafNode(FieldMappingSourceType.REQUEST_BODY, "vipLevel", ConditionOperator.GREATER_THAN, "5")))), 2, 3);
        assertThat(runWithCondition(s1, "{\"permissionCodes\":[\"A\"],\"fieldX\":\"false\",\"vipLevel\":9}", Map.of(), Map.of())
                .get("branch").asText()).isEqualTo("T");
        assertThat(runWithCondition(s1, "{\"permissionCodes\":[\"A\"],\"fieldX\":\"false\",\"vipLevel\":1}", Map.of(), Map.of())
                .get("branch").asText()).isEqualTo("F");
        assertThat(runWithCondition(s1, "{\"permissionCodes\":[\"B\"],\"fieldX\":\"true\",\"vipLevel\":9}", Map.of(), Map.of())
                .get("branch").asText()).isEqualTo("F");
    }

    @Test
    void coFlagTrongNhom_flagBat_OR_voiDieuKienBody_dung() {
        when(flagRegistryCache.get("VIP")).thenReturn(true);
        BackendStepDto s1 = stepWithConditionGroups(1, "u1", List.of(List.of(
                orGroupNode(
                        leafNode(FieldMappingSourceType.SYSTEM_FLAG, "VIP", ConditionOperator.EQUALS, "true"),
                        leafNode(FieldMappingSourceType.REQUEST_BODY, "permissionCodes", ConditionOperator.CONTAINS, "A")))), 2, 3);
        assertThat(runWithCondition(s1, "{\"permissionCodes\":[\"B\"]}", Map.of(), Map.of()).get("branch").asText()).isEqualTo("T");
    }

    @Test
    void coFlagTrongNhom_flagTat_AND_voiBody_sai() {
        when(flagRegistryCache.get("VIP")).thenReturn(false);
        BackendStepDto s1 = stepWithConditionGroups(1, "u1", List.of(List.of(
                leafNode(FieldMappingSourceType.SYSTEM_FLAG, "VIP", ConditionOperator.EQUALS, "true"),
                leafNode(FieldMappingSourceType.REQUEST_BODY, "permissionCodes", ConditionOperator.CONTAINS, "A"))), 2, 3);
        assertThat(runWithCondition(s1, "{\"permissionCodes\":[\"A\"]}", Map.of(), Map.of()).get("branch").asText()).isEqualTo("F");
    }

    // ==================== V7: kich thuoc, lap mang (ANY/ALL/COUNT), rẽ nhiều nhánh (switch) ====================

    private final UpstreamService up4 = UpstreamService.builder().id("u4").name("up4").baseHost("http://u4").build();

    private BackendStepDto stepWithSwitch(int order, String upstreamId, List<com.bccs.gatewaymanager.dto.SwitchBranchDto> branches,
                                          Integer nextIfFalse) {
        return new BackendStepDto(null, order, "step" + order, GatewayMethod.GET, "/x", upstreamId, "up" + order,
                false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                null, null, null, null,
                null, null, null, null, null,
                null, null, null, nextIfFalse,
                null, null, null, null, null, null, branches);
    }

    private com.bccs.gatewaymanager.dto.ConditionRuleDto quantNode(String quantifier, String arrayField,
                                                                    com.bccs.gatewaymanager.dto.ConditionRuleDto... children) {
        return com.bccs.gatewaymanager.dto.ConditionRuleDto.quantifier(FieldMappingSourceType.REQUEST_BODY, arrayField, null,
                quantifier, "AND", List.of(children), null, null);
    }

    private BackendStepDto stepWithQuantifier(com.bccs.gatewaymanager.dto.ConditionRuleDto node) {
        return stepWithConditionGroups(1, "u1", List.of(List.of(node)), 2, 3);
    }

    private static final String ITEMS_MIXED = "{\"items\":[{\"code\":\"SIM\",\"status\":\"ACTIVE\",\"amount\":50},"
            + "{\"code\":\"CARD\",\"status\":\"BLOCKED\",\"amount\":0}]}";

    @Test
    void size_mangDaiHon_dung_vaMangNgan_sai() {
        BackendStepDto s1 = stepWithConditionGroups(1, "u1", List.of(List.of(
                leafNode(FieldMappingSourceType.REQUEST_BODY, "items", ConditionOperator.SIZE_GREATER_THAN, "2"))), 2, 3);
        assertThat(runWithCondition(s1, "{\"items\":[1,2,3]}", Map.of(), Map.of()).get("branch").asText()).isEqualTo("T");
        assertThat(runWithCondition(s1, "{\"items\":[1]}", Map.of(), Map.of()).get("branch").asText()).isEqualTo("F");
    }

    @Test
    void size_chuoiDoDaiKyTuCodePoint_vaSoKhongCoKichThuoc() {
        BackendStepDto eq = stepWithConditionGroups(1, "u1", List.of(List.of(
                leafNode(FieldMappingSourceType.REQUEST_BODY, "code", ConditionOperator.SIZE_EQUALS, "3"))), 2, 3);
        assertThat(runWithCondition(eq, "{\"code\":\"abc\"}", Map.of(), Map.of()).get("branch").asText()).isEqualTo("T");
        // So khong co kich thuoc -> khong khop (fail-safe), khong throw.
        assertThat(runWithCondition(eq, "{\"code\":12345}", Map.of(), Map.of()).get("branch").asText()).isEqualTo("F");
    }

    @Test
    void any_coPhanTuKhop_dung() {
        JsonNode r = runWithCondition(stepWithQuantifier(quantNode("ANY", "items",
                leafNode(FieldMappingSourceType.REQUEST_BODY, "status", ConditionOperator.EQUALS, "BLOCKED"))),
                ITEMS_MIXED, Map.of(), Map.of());
        assertThat(r.get("branch").asText()).isEqualTo("T");
    }

    @Test
    void any_khongPhanTuNaoKhop_sai() {
        JsonNode r = runWithCondition(stepWithQuantifier(quantNode("ANY", "items",
                leafNode(FieldMappingSourceType.REQUEST_BODY, "status", ConditionOperator.EQUALS, "CLOSED"))),
                ITEMS_MIXED, Map.of(), Map.of());
        assertThat(r.get("branch").asText()).isEqualTo("F");
    }

    @Test
    void all_tatCaPhanTuThoaDieuKien_dung_vaCoMotPhanTuSai_sai() {
        var positive = leafNode(FieldMappingSourceType.REQUEST_BODY, "amount", ConditionOperator.GREATER_THAN, "0");
        assertThat(runWithCondition(stepWithQuantifier(quantNode("ALL", "items", positive)),
                "{\"items\":[{\"amount\":1},{\"amount\":2}]}", Map.of(), Map.of()).get("branch").asText()).isEqualTo("T");
        assertThat(runWithCondition(stepWithQuantifier(quantNode("ALL", "items", positive)),
                ITEMS_MIXED, Map.of(), Map.of()).get("branch").asText()).isEqualTo("F");
    }

    @Test
    void all_mangRong_khongKhop_failSafe() {
        assertThat(runWithCondition(stepWithQuantifier(quantNode("ALL", "items",
                leafNode(FieldMappingSourceType.REQUEST_BODY, "amount", ConditionOperator.GREATER_THAN, "0"))),
                "{\"items\":[]}", Map.of(), Map.of()).get("branch").asText()).isEqualTo("F");
    }

    @Test
    void count_soPhanTuKhopSoSanhNguong() {
        String body = "{\"items\":[{\"status\":\"ACTIVE\"},{\"status\":\"ACTIVE\"},{\"status\":\"BLOCKED\"}]}";
        var active = leafNode(FieldMappingSourceType.REQUEST_BODY, "status", ConditionOperator.EQUALS, "ACTIVE");
        var atLeastTwo = com.bccs.gatewaymanager.dto.ConditionRuleDto.quantifier(FieldMappingSourceType.REQUEST_BODY, "items", null,
                "COUNT", "AND", List.of(active), ConditionOperator.GREATER_THAN_OR_EQUAL, "2");
        assertThat(runWithCondition(stepWithQuantifier(atLeastTwo), body, Map.of(), Map.of()).get("branch").asText()).isEqualTo("T");
        var atLeastThree = com.bccs.gatewaymanager.dto.ConditionRuleDto.quantifier(FieldMappingSourceType.REQUEST_BODY, "items", null,
                "COUNT", "AND", List.of(active), ConditionOperator.GREATER_THAN_OR_EQUAL, "3");
        assertThat(runWithCondition(stepWithQuantifier(atLeastThree), body, Map.of(), Map.of()).get("branch").asText()).isEqualTo("F");
    }

    @Test
    void any_dieuKienNhieuTruongTrenCungPhanTu() {
        // Co 1 phan tu vua code=SIM vua status=ACTIVE; phan tu khac chi co 1 trong 2.
        String body = "{\"items\":[{\"code\":\"SIM\",\"status\":\"BLOCKED\"},{\"code\":\"CARD\",\"status\":\"ACTIVE\"}]}";
        var both = com.bccs.gatewaymanager.dto.ConditionRuleDto.quantifier(FieldMappingSourceType.REQUEST_BODY, "items", null,
                "ANY", "AND", List.of(
                        leafNode(FieldMappingSourceType.REQUEST_BODY, "code", ConditionOperator.EQUALS, "SIM"),
                        leafNode(FieldMappingSourceType.REQUEST_BODY, "status", ConditionOperator.EQUALS, "ACTIVE")), null, null);
        assertThat(runWithCondition(stepWithQuantifier(both), body, Map.of(), Map.of()).get("branch").asText()).isEqualTo("F");
    }

    @Test
    void lapLong_mangBenTrongPhanTu() {
        // ANY(orders, ALL(items, amount > 0)): co 1 order ma moi items cua no deu amount>0.
        String body = "{\"orders\":[{\"items\":[{\"amount\":1},{\"amount\":0}]},{\"items\":[{\"amount\":3}]}]}";
        var innerAll = com.bccs.gatewaymanager.dto.ConditionRuleDto.quantifier(FieldMappingSourceType.REQUEST_BODY, "items", null,
                "ALL", "AND", List.of(leafNode(FieldMappingSourceType.REQUEST_BODY, "amount", ConditionOperator.GREATER_THAN, "0")),
                null, null);
        var outerAny = com.bccs.gatewaymanager.dto.ConditionRuleDto.quantifier(FieldMappingSourceType.REQUEST_BODY, "orders", null,
                "ANY", "AND", List.of(innerAll), null, null);
        assertThat(runWithCondition(stepWithQuantifier(outerAny), body, Map.of(), Map.of()).get("branch").asText()).isEqualTo("T");
    }

    @Test
    void mangKhongPhaiMang_khongKhop() {
        assertThat(runWithCondition(stepWithQuantifier(quantNode("ANY", "items",
                leafNode(FieldMappingSourceType.REQUEST_BODY, "status", ConditionOperator.EQUALS, "BLOCKED"))),
                "{\"items\":\"BLOCKED\"}", Map.of(), Map.of()).get("branch").asText()).isEqualTo("F");
    }

    @Test
    void traceLapMang_coTomTat() {
        java.util.List<StepTraceDto> trace = TraceCollector.start();
        try {
            runWithCondition(stepWithQuantifier(quantNode("ANY", "items",
                    leafNode(FieldMappingSourceType.REQUEST_BODY, "status", ConditionOperator.EQUALS, "BLOCKED"))),
                    ITEMS_MIXED, Map.of(), Map.of());
            assertThat(trace).anySatisfy(t -> assertThat(t.conditionInfo()).contains("ANY(items)").contains("1/2 phan tu khop").endsWith("DUNG"));
        } finally {
            TraceCollector.stop();
        }
    }

    // ---- switch ----

    private com.bccs.gatewaymanager.dto.SwitchBranchDto branch(String tier, int next) {
        return new com.bccs.gatewaymanager.dto.SwitchBranchDto(List.of(List.of(
                leafNode(FieldMappingSourceType.REQUEST_BODY, "tier", ConditionOperator.EQUALS, tier))), next);
    }

    private JsonNode runSwitch(List<com.bccs.gatewaymanager.dto.SwitchBranchDto> branches, Integer nextIfFalse, String body) {
        lenient().when(upstreamRegistryCache.getById("u4")).thenReturn(up4);
        lenientCall(up1, json("{\"step\":1}"));
        lenientCall(up2, json("{\"branch\":\"two\"}"));
        lenientCall(up3, json("{\"branch\":\"three\"}"));
        lenientCall(up4, json("{\"branch\":\"four\"}"));
        lenientCall(up1, json("{\"branch\":\"one\"}"));
        EndpointResponseDto config = endpoint(true, stepWithSwitch(1, "u1", branches, nextIfFalse),
                plainStep(2, "u2"), plainStep(3, "u3"), plainStep(4, "u4"));
        return engine.handle(config, Map.of(), Map.of(), body);
    }

    @Test
    void switch_chonNhanhKhopDauTien() {
        var branches = List.of(branch("gold", 2), branch("silver", 3));
        assertThat(runSwitch(branches, 4, "{\"tier\":\"silver\"}").get("branch").asText()).isEqualTo("three");
        assertThat(runSwitch(branches, 4, "{\"tier\":\"gold\"}").get("branch").asText()).isEqualTo("two");
    }

    @Test
    void switch_khongNhanhNaoKhop_veMacDinh() {
        var branches = List.of(branch("gold", 2), branch("silver", 3));
        assertThat(runSwitch(branches, 4, "{\"tier\":\"bronze\"}").get("branch").asText()).isEqualTo("four");
    }

    @Test
    void switch_nhieuNhanhKhop_layNhanhDauTien() {
        var branches = List.of(branch("gold", 2), new com.bccs.gatewaymanager.dto.SwitchBranchDto(
                List.of(List.of(leafNode(FieldMappingSourceType.REQUEST_BODY, "vip", ConditionOperator.EXISTS, null))), 3));
        assertThat(runSwitch(branches, 4, "{\"tier\":\"gold\",\"vip\":true}").get("branch").asText()).isEqualTo("two");
    }

    @Test
    void switch_nhanhKhopThiKhongChayStepMacDinh() {
        var branches = List.of(branch("gold", 2));
        runSwitch(branches, 4, "{\"tier\":\"gold\"}");
        org.mockito.Mockito.verify(upstreamHttpExecutor, org.mockito.Mockito.never())
                .call(eq(up4), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void switch_khongCoSwitch_hanhViCuKhongDoi() {
        // step thuong (khong switch) van chay theo nextStepOrderIfTrue/False nhu truoc.
        BackendStepDto s1 = step(1, "u1", 2, 3, ConditionOperator.EQUALS, FieldMappingSourceType.REQUEST_BODY, null, "tier", "gold");
        assertThat(runWithCondition(s1, "{\"tier\":\"gold\"}", Map.of(), Map.of()).get("branch").asText()).isEqualTo("T");
    }


    // ==================== Nhanh SAI bao loi (thay vi chuyen step) ====================

    private BackendStepDto condStepWithFalseError(String code, String msg) {
        return new BackendStepDto(null, 1, "step1", GatewayMethod.GET, "/x", "u1", "up1",
                false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null, null, null,
                null, null,
                FieldMappingSourceType.REQUEST_BODY, null, "tier", ConditionOperator.EQUALS, "gold",
                null, null, 2, null, null, null, null, null, null, null, null, code, msg);
    }

    @Test
    void reNhanhSai_baoLoi_ketThucVoiMaVaThongDiep() {
        lenientCall(up1, json("{\"step\":1}"));
        lenientCall(up2, json("{\"branch\":\"T\"}"));
        EndpointResponseDto config = endpoint(true, condStepWithFalseError("GW-TIER-SAI", "Khach khong thuoc hang gold"),
                plainStep(2, "u2"));
        BusinessException ex = org.junit.jupiter.api.Assertions.assertThrows(BusinessException.class,
                () -> engine.handle(config, Map.of(), Map.of(), "{\"tier\":\"silver\"}"));
        assertThat(ex.getErrorCode()).isEqualTo("GW-TIER-SAI");
        assertThat(ex.getMessage()).contains("Khach khong thuoc hang gold");
    }

    @Test
    void reNhanhDung_khongBiAnhHuongBoiCauHinhLoi() {
        lenientCall(up1, json("{\"step\":1}"));
        lenientCall(up2, json("{\"branch\":\"T\"}"));
        EndpointResponseDto config = endpoint(true, condStepWithFalseError("GW-TIER-SAI", "x"), plainStep(2, "u2"));
        assertThat(engine.handle(config, Map.of(), Map.of(), "{\"tier\":\"gold\"}").get("branch").asText()).isEqualTo("T");
    }

    @Test
    void switchKhongNhanhNao_baoLoi() {
        lenientCall(up1, json("{\"step\":1}"));
        lenientCall(up2, json("{\"branch\":\"two\"}"));
        var branches = List.of(branch("gold", 2));
        BackendStepDto sw = new BackendStepDto(null, 1, "step1", GatewayMethod.GET, "/x", "u1", "up1",
                false, false, 300, null, null, List.of(), List.of(), Map.of(),
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                branches, "GW-NO-BRANCH", "Khong nhanh nao phu hop");
        EndpointResponseDto config = endpoint(true, sw, plainStep(2, "u2"));
        assertThatThrownBy(() -> engine.handle(config, Map.of(), Map.of(), "{\"tier\":\"bronze\"}"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-NO-BRANCH");
    }
    // ==================== Tinh nang ResponseMapping (ghep field tu bat ky step nao vao response cuoi) ====================

    private com.bccs.gatewaymanager.dto.ResponseMappingSpecDto spec(
            com.bccs.gatewaymanager.dto.ResponseFieldMappingDto... mappings) {
        return new com.bccs.gatewaymanager.dto.ResponseMappingSpecDto("MERGE", List.of(mappings));
    }

    @Test
    void responseMapping_null_khongDoiHanhViCu() {
        // Regression: spec null = 100% endpoint cu - ket qua phai dung y het truoc.
        stubCall(up1, json("{\"code\":\"SUCCESS\",\"data\":{\"a\":1}}"));
        EndpointResponseDto config = endpoint(true, plainStep(1, "u1"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("code").asText()).isEqualTo("SUCCESS");
        assertThat(result.get("data").get("a").asInt()).isEqualTo(1);
    }

    @Test
    void responseMapping_merge_gopFieldTuStepGiuaChuoiSequential() {
        // Chuoi 2 step: step 1 -> step 2 (step 2 tra ket qua cuoi). MERGE bo sung
        // field "isdn" cua step 1 vao response cuoi (dung khi can du lieu tho cua
        // step GIUA chuoi - khac target/allow chi tac dong len 1 step).
        stubCall(up1, json("{\"isdn\":\"84123456789\",\"step\":1}"));
        stubCall(up2, json("{\"productCode\":\"TOM11\"}"));
        BackendStepDto s1 = plainStep(1, "u1");
        BackendStepDto s2 = plainStep(2, "u2");
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, s2), List.of(), null, null, false, 86400, false, false, 300, List.of(),
                spec(new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(1, "isdn", "isdn", 0)));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("isdn").asText()).isEqualTo("84123456789");
        assertThat(result.get("productCode").asText()).isEqualTo("TOM11");
    }

    @Test
    void responseMapping_replace_traDungVaChiCacFieldKhaiBao_giuEnvelope() {
        // Giai case van de: step target=null giu nguyen raw StandardResponse, REPLACE
        // chi giu cac field khai bao (envelope + data da loc) - bo field thua.
        stubCall(up1, json("{\"code\":\"SUCCESS\",\"data\":{\"productOfferingId\":400005389,\"name\":\"Tomato\",\"secret\":true},\"message\":\"Ok\",\"junkField\":\"x\"}"));
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1")), List.of(), null, null, false, 86400, false, false, 300, List.of(),
                new com.bccs.gatewaymanager.dto.ResponseMappingSpecDto("REPLACE", List.of(
                        new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(1, "code", "code", 0),
                        new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(1, "data.productOfferingId", "data.productOfferingId", 1),
                        new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(1, "data.name", "data.name", 2))));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("code").asText()).isEqualTo("SUCCESS");
        assertThat(result.get("data").get("productOfferingId").asLong()).isEqualTo(400005389L);
        assertThat(result.get("data").get("name").asText()).isEqualTo("Tomato");
        assertThat(result.has("message")).isFalse();
        assertThat(result.has("junkField")).isFalse();
        assertThat(result.get("data").has("secret")).isFalse();
    }

    @Test
    void responseMapping_fieldNguonThieu_skipKhongThrow_failOpen() {
        // Mapping tro step khong co field do / step khong chay - skip + log.warn,
        // request VAN tra duoc cac field con lai (validate luc luu chi chan
        // sourceStepOrder khong ton tai voi bat ky step nao trong steps).
        stubCall(up1, json("{\"code\":\"SUCCESS\",\"data\":{}}"));
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1")), List.of(), null, null, false, 86400, false, false, 300, List.of(),
                spec(new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(1, "missing.field", "out", 0),
                        new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(99, "anything", "out2", 1),
                        new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(1, "code", "code", 2)));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("code").asText()).isEqualTo("SUCCESS");
        assertThat(result.has("out")).isFalse();
        assertThat(result.has("out2")).isFalse();
    }

    @Test
    void responseMapping_mappingOrderLonGhiDeTrungTen() {
        stubCall(up1, json("{\"code\":\"SUCCESS\",\"data\":{\"a\":1}}"));
        stubCall(up2, json("{\"code\":\"FROM_STEP2\"}"));
        BackendStepDto s1 = plainStep(1, "u1");
        BackendStepDto s2 = plainStep(2, "u2");
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, s2), List.of(), null, null, false, 86400, false, false, 300, List.of(),
                spec(new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(1, "code", "code", 0),
                        new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(2, "code", "code", 1)));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("code").asText()).isEqualTo("FROM_STEP2");
    }

    @Test
    void responseMapping_nonSequentialVanApDuoc() {
        stubCall(up1, json("{\"a\":1}"));
        stubCall(up2, json("{\"b\":2}"));
        BackendStepDto s1 = plainStep(1, "u1");
        BackendStepDto s2 = plainStep(2, "u2");
        // sequential=false - assembleFinalResponse merge 2 step; MERGE them field tu step 2.
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, false, "json",
                List.of(s1, s2), List.of(), null, null, false, 86400, false, false, 300, List.of(),
                spec(new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(2, "b", "extra", 0)));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("a").asInt()).isEqualTo(1);
        assertThat(result.get("extra").asInt()).isEqualTo(2);
    }

    @Test
    void responseMapping_stepReNhanhKhongChay_mappingTroStepDo_BiBoQua() {
        // Dieu kien step 1 DUNG -> chay nhanh nextIfTrue=2 (step 2 la "nhanh la" - DUNG
        // tai day, step 3 KHONG BAO GIO chay trong nhanh nay). Mapping tro step 3
        // phai skip AN TOAN (ctx.stepResults khong co step 3), khong throw; mapping
        // tro step 2 (da chay) van ghep binh thuong.
        stubCall(up1, json("{\"ok\":true}"));
        stubCall(up2, json("{\"branch\":2}"));
        // Step 3 KHONG chay trong nhanh nay (nhanh la 2 dung chuoi) - stub lenient de
        // Mockito khong bat loi "UnnecessaryStubbing" cho call khong bao gio duoc goi.
        lenient().when(upstreamHttpExecutor.call(eq(up3), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any()))
                .thenReturn(json("{\"branch\":3}"));
        BackendStepDto s1 = step(1, "u1", 2, 3, ConditionOperator.EXISTS,
                FieldMappingSourceType.STEP_RESPONSE, 1, "ok", null);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(s1, plainStep(2, "u2"), plainStep(3, "u3")), List.of(), null, null, false, 86400, false, false, 300, List.of(),
                spec(new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(3, "branch", "skippedBranch", 0),
                        new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(2, "branch", "takenBranch", 1)));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.has("skippedBranch")).isFalse();
        assertThat(result.get("takenBranch").asInt()).isEqualTo(2);
    }
}
