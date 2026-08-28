package com.bccs.gatewaymanager.engine;

import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.FieldMappingDto;
import com.bccs.gatewaymanager.entity.ConditionOperator;
import com.bccs.gatewaymanager.entity.FieldMappingSourceType;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import com.bccs.gatewaymanager.entity.MappingTargetType;
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
                null, null,
                conditionSourceType, conditionSourceStepOrder, conditionSourceField, operator, conditionExpectedValue,
                nextIfTrue, nextIfFalse);
    }

    private BackendStepDto plainStep(int order, String upstreamId) {
        return step(order, upstreamId, null, null, null, null, null, null, null);
    }

    private EndpointResponseDto endpoint(boolean sequential, BackendStepDto... steps) {
        return new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, sequential, "json",
                List.of(steps), List.of(), null, null, false, 86400);
    }

    private void stubCall(UpstreamService upstream, JsonNode response) {
        when(upstreamHttpExecutor.call(eq(upstream), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any()))
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
                .call(eq(up3), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(), any(), any(), any());
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
                null, null, null, null, null, null, null);

        engine.handle(endpoint(true, stepWithOverride), Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<Integer> connectCaptor = org.mockito.ArgumentCaptor.forClass(Integer.class);
        org.mockito.ArgumentCaptor<Integer> readCaptor = org.mockito.ArgumentCaptor.forClass(Integer.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up1), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt(),
                any(), connectCaptor.capture(), readCaptor.capture());
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
                any(), connectCaptor.capture(), readCaptor.capture());
        assertThat(connectCaptor.getValue()).isNull();
        assertThat(readCaptor.getValue()).isNull();
    }

    // ---- QUERY_PARAM (nguon FieldMapping moi - doc query param cua chinh client, khong phai response step nao) ----

    @Test
    void queryParamMapping_forwardDungGiaTriVaoQuery() {
        stubCall(up1, json("{\"v\":1}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.QUERY_PARAM, null, "staffCode", null, null, null,
                1, MappingTargetType.QUERY, "staffCode", 0);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1")), List.of(mapping), null, null, false, 86400);

        engine.handle(config, Map.of(), Map.of("staffCode", new String[]{"QUITT"}), null);

        org.mockito.ArgumentCaptor<String> urlCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up1), any(), urlCaptor.capture(), any(), any(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any());
        assertThat(urlCaptor.getValue()).contains("staffCode=QUITT");
    }

    @Test
    void queryParamMapping_forwardDungGiaTriVaoBodyField() {
        stubCall(up1, json("{\"v\":1}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.QUERY_PARAM, null, "staffCode", null, null, null,
                1, MappingTargetType.BODY_FIELD, "staffCode", 0);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1")), List.of(mapping), null, null, false, 86400);

        engine.handle(config, Map.of(), Map.of("staffCode", new String[]{"QUITT"}), null);

        org.mockito.ArgumentCaptor<JsonNode> bodyCaptor = org.mockito.ArgumentCaptor.forClass(JsonNode.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up1), any(), any(), any(), bodyCaptor.capture(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any());
        assertThat(bodyCaptor.getValue().get("staffCode").asText()).isEqualTo("QUITT");
    }

    @Test
    void queryParamMapping_khongCoGiaTri_traNullKhongThrow() {
        stubCall(up1, json("{\"v\":1}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.QUERY_PARAM, null, "staffCode", null, null, null,
                1, MappingTargetType.BODY_FIELD, "staffCode", 0);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1")), List.of(mapping), null, null, false, 86400);

        // Khong truyen queryParams nao ca (Map.of()) - staffCode khong ton tai, phai ra null, khong throw.
        engine.handle(config, Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<JsonNode> bodyCaptor = org.mockito.ArgumentCaptor.forClass(JsonNode.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up1), any(), any(), any(), bodyCaptor.capture(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any());
        assertThat(bodyCaptor.getValue().get("staffCode").isNull()).isTrue();
    }

    // ---- CONSTANT (nguon FieldMapping moi - hang so co dinh, khong doc tu request/response) - dung cho
    // case "2 nhanh re cung goi 1 API nhung fix cung khac nhau tham so, khong phu thuoc input". ----

    @Test
    void constantMapping_forwardDungGiaTriVaoQuery_luonLaChuoiTextNguyenBan() {
        stubCall(up1, json("{\"v\":1}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.CONSTANT, null, null, null, null, "low",
                1, MappingTargetType.QUERY, "priority", 0);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1")), List.of(mapping), null, null, false, 86400);

        engine.handle(config, Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<String> urlCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up1), any(), urlCaptor.capture(), any(), any(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any());
        assertThat(urlCaptor.getValue()).contains("priority=low");
    }

    @Test
    void constantMapping_bodyField_chuoiSoTuDongThanhSoJSON() {
        stubCall(up1, json("{\"v\":1}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.CONSTANT, null, null, null, null, "3",
                1, MappingTargetType.BODY_FIELD, "priority", 0);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1")), List.of(mapping), null, null, false, 86400);

        engine.handle(config, Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<JsonNode> bodyCaptor = org.mockito.ArgumentCaptor.forClass(JsonNode.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up1), any(), any(), any(), bodyCaptor.capture(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any());
        // "3" parse duoc nhu JSON -> so JSON THAT (isNumber), khong phai chuoi "3".
        assertThat(bodyCaptor.getValue().get("priority").isNumber()).isTrue();
        assertThat(bodyCaptor.getValue().get("priority").asInt()).isEqualTo(3);
    }

    @Test
    void constantMapping_bodyField_chuoiKhongPhaiJson_giuNguyenDangChuoi() {
        stubCall(up1, json("{\"v\":1}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.CONSTANT, null, null, null, null, "low",
                1, MappingTargetType.BODY_FIELD, "priority", 0);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1")), List.of(mapping), null, null, false, 86400);

        engine.handle(config, Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<JsonNode> bodyCaptor = org.mockito.ArgumentCaptor.forClass(JsonNode.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up1), any(), any(), any(), bodyCaptor.capture(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any());
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
                2, MappingTargetType.BODY_FIELD, "$body", 0);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1"), plainStep(2, "u2")), List.of(mapping), null, null, false, 86400);

        engine.handle(config, Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<JsonNode> bodyCaptor = org.mockito.ArgumentCaptor.forClass(JsonNode.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up2), any(), any(), any(), bodyCaptor.capture(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any());
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
                2, MappingTargetType.BODY_FIELD, "$body", 0);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1"), plainStep(2, "u2")), List.of(mapping), null, null, false, 86400);

        engine.handle(config, Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<JsonNode> bodyCaptor = org.mockito.ArgumentCaptor.forClass(JsonNode.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up2), any(), any(), any(), bodyCaptor.capture(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any());
        assertThat(bodyCaptor.getValue().get("a").asText()).isEqualTo("2");
    }

    @Test
    void arrayMergeMapping_phanTuKhongPhaiObject_boQuaKhongThrow() {
        stubCall(up1, json("{\"data\":[{\"a\":\"1\"},\"khong-phai-object\",123]}"));
        stubCall(up2, json("{\"ok\":true}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE_ARRAY_MERGE, 1, null, "data", null, null,
                2, MappingTargetType.BODY_FIELD, "$body", 0);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1"), plainStep(2, "u2")), List.of(mapping), null, null, false, 86400);

        engine.handle(config, Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<JsonNode> bodyCaptor = org.mockito.ArgumentCaptor.forClass(JsonNode.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up2), any(), any(), any(), bodyCaptor.capture(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any());
        assertThat(bodyCaptor.getValue().get("a").asText()).isEqualTo("1");
        assertThat(bodyCaptor.getValue().size()).isEqualTo(1);
    }

    @Test
    void arrayMergeMapping_khongTimThayMang_traObjectRongKhongThrow() {
        stubCall(up1, json("{}"));
        stubCall(up2, json("{\"ok\":true}"));
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.STEP_RESPONSE_ARRAY_MERGE, 1, null, "data", null, null,
                2, MappingTargetType.BODY_FIELD, "$body", 0);
        EndpointResponseDto config = new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(plainStep(1, "u1"), plainStep(2, "u2")), List.of(mapping), null, null, false, 86400);

        engine.handle(config, Map.of(), Map.of(), null);

        org.mockito.ArgumentCaptor<JsonNode> bodyCaptor = org.mockito.ArgumentCaptor.forClass(JsonNode.class);
        org.mockito.Mockito.verify(upstreamHttpExecutor).call(eq(up2), any(), any(), any(), bodyCaptor.capture(),
                anyBoolean(), anyInt(), anyInt(), any(), any(), any());
        assertThat(bodyCaptor.getValue().size()).isEqualTo(0);
    }
}
