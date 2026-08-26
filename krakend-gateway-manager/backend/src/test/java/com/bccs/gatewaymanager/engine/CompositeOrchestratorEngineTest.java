package com.bccs.gatewaymanager.engine;

import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.entity.ConditionOperator;
import com.bccs.gatewaymanager.entity.FieldMappingSourceType;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import com.bccs.gatewaymanager.entity.UpstreamService;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.service.UpstreamRegistryCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

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

    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private CompositeOrchestratorEngine engine;

    private final UpstreamService up1 = UpstreamService.builder().id("u1").name("up1").baseHost("http://u1").build();
    private final UpstreamService up2 = UpstreamService.builder().id("u2").name("up2").baseHost("http://u2").build();
    private final UpstreamService up3 = UpstreamService.builder().id("u3").name("up3").baseHost("http://u3").build();

    @BeforeEach
    void setUp() {
        engine = new CompositeOrchestratorEngine(upstreamHttpExecutor, upstreamRegistryCache, objectMapper);
        lenient().when(upstreamRegistryCache.getById("u1")).thenReturn(up1);
        lenient().when(upstreamRegistryCache.getById("u2")).thenReturn(up2);
        lenient().when(upstreamRegistryCache.getById("u3")).thenReturn(up3);
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
                conditionSourceType, conditionSourceStepOrder, conditionSourceField, operator, conditionExpectedValue,
                nextIfTrue, nextIfFalse);
    }

    private BackendStepDto plainStep(int order, String upstreamId) {
        return step(order, upstreamId, null, null, null, null, null, null, null);
    }

    private EndpointResponseDto endpoint(boolean sequential, BackendStepDto... steps) {
        return new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, sequential, "json",
                List.of(steps), List.of(), null, null);
    }

    private void stubCall(UpstreamService upstream, JsonNode response) {
        when(upstreamHttpExecutor.call(eq(upstream), any(), any(), any(), any(), anyBoolean(), anyInt()))
                .thenReturn(response);
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

    // ---- Re nhanh that: di theo nhanh dung/sai ----

    @Test
    void reNhanh_dieuKienDung_diTheoNhanhTrue_boQuaNhanhFalse() {
        stubCall(up1, json("{\"status\":\"active\"}"));
        stubCall(up2, json("{\"branch\":\"true-path\"}"));
        // CHI 2 step - step2 KHONG khai bao dieu kien nen la "natural next" cuoi
        // cung (khong con step nao co stepOrder lon hon 2) - neu them step3 vao
        // day, step2 (khong dieu kien) se TU DONG "roi tiep" sang step3 dung theo
        // natural-next (dung thiet ke, khong phai bug) - phai tranh nham lan do
        // trong chinh test nay bang cach khong tao step3.
        BackendStepDto s1 = step(1, "u1", 2, null, ConditionOperator.EQUALS, FieldMappingSourceType.STEP_RESPONSE, 1, "status", "active");
        EndpointResponseDto config = endpoint(true, s1, plainStep(2, "u2"));

        JsonNode result = engine.handle(config, Map.of(), Map.of(), null);

        assertThat(result.get("branch").asText()).isEqualTo("true-path");
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
}
