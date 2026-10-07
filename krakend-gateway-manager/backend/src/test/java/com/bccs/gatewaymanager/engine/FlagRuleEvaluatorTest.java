package com.bccs.gatewaymanager.engine;

import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.FlagRuleDto;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.service.FlagRegistryCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlagRuleEvaluatorTest {

    @Mock
    private FlagRegistryCache flagRegistryCache;

    private FlagRuleEvaluator evaluator;

    private static final String TEAM = "default";

    @BeforeEach
    void setUp() {
        evaluator = new FlagRuleEvaluator(flagRegistryCache);
    }

    private FlagRuleDto.FlagConditionDto cond(String flagCode, boolean expected) {
        return new FlagRuleDto.FlagConditionDto(flagCode, expected);
    }

    private FlagRuleDto startStepRule(int priority, List<List<FlagRuleDto.FlagConditionDto>> groups, int startStepOrder) {
        return new FlagRuleDto("r1", "Rule A", priority, groups,
                FlagRuleDto.ACTION_START_STEP, startStepOrder, null, null);
    }

    private FlagRuleDto blockRule(int priority, List<List<FlagRuleDto.FlagConditionDto>> groups) {
        return new FlagRuleDto("r2", "Rule B", priority, groups,
                FlagRuleDto.ACTION_BLOCK, null, "GW-FEATURE-DISABLED", "Tinh nang tam dung");
    }

    private EndpointResponseDto endpoint(List<FlagRuleDto> rules) {
        return new EndpointResponseDto("ep-1", "test", null, "/x", GatewayMethod.GET, true, "json",
                List.of(), List.of(), null, null, false, 86400, false, false, 300, rules, null);
    }

    @Test
    void endpointKhongCoRule_traVeKetQuaRong_hanhViCu() {
        var result = evaluator.evaluate(endpoint(List.of()), null);

        assertThat(result.matchedRule()).isNull();
        assertThat(result.initialStepOrder()).isNull();
    }

    @Test
    void rule1NhomANDKhop_tatCaDieuKienDung() {
        when(flagRegistryCache.get("VIP")).thenReturn(true);
        when(flagRegistryCache.get("KYC_DONE")).thenReturn(false);
        var rule = startStepRule(1, List.of(List.of(cond("VIP", true), cond("KYC_DONE", false))), 3);

        var result = evaluator.evaluate(endpoint(List.of(rule)), null);

        assertThat(result.matchedRule()).isSameAs(rule);
        assertThat(result.initialStepOrder()).isEqualTo(3);
    }

    @Test
    void rule1NhomANDKhongKhop_1DieuKienSai() {
        when(flagRegistryCache.get("VIP")).thenReturn(true);
        when(flagRegistryCache.get("KYC_DONE")).thenReturn(true);
        var rule = startStepRule(1, List.of(List.of(cond("VIP", true), cond("KYC_DONE", false))), 3);

        var result = evaluator.evaluate(endpoint(List.of(rule)), null);

        assertThat(result.matchedRule()).isNull();
        assertThat(result.initialStepOrder()).isNull();
    }

    @Test
    void ruleNhieuNhomOR_khopNeuItNhat1NhomDung() {
        when(flagRegistryCache.get("VIP")).thenReturn(false);
        when(flagRegistryCache.get("TRIAL")).thenReturn(true);
        var rule = startStepRule(1, List.of(
                List.of(cond("VIP", true)),
                List.of(cond("TRIAL", true))), 2);

        var result = evaluator.evaluate(endpoint(List.of(rule)), null);

        assertThat(result.initialStepOrder()).isEqualTo(2);
    }

    @Test
    void rulePriorityNhoDanhGiaTruoc_khopDauTienLaDung() {
        when(flagRegistryCache.get("A")).thenReturn(true);
        // Khong stub "B" - rule priority 1 khop truoc, cua "B" khong bao gio duoc danh gia.
        var rulePriority2 = startStepRule("r-lo", "Rule 2", 2, List.of(List.of(cond("B", true))), 5);
        var rulePriority1 = startStepRule("r-hi", "Rule 1", 1, List.of(List.of(cond("A", true))), 3);

        var result = evaluator.evaluate(endpoint(List.of(rulePriority2, rulePriority1)), null);

        assertThat(result.matchedRule().name()).isEqualTo("Rule 1");
        assertThat(result.initialStepOrder()).isEqualTo(3);
    }

    @Test
    void ruleBLOCKKhop_throwBusinessExceptionVoiBlockErrorCode() {
        when(flagRegistryCache.get("MAINTENANCE")).thenReturn(true);
        var rule = blockRule(1, List.of(List.of(cond("MAINTENANCE", true))));

        assertThatThrownBy(() -> evaluator.evaluate(endpoint(List.of(rule)), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Tinh nang tam dung")
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-FEATURE-DISABLED");
    }

    @Test
    void flagKhongTonTaiTrongCache_coiLaFalse_failsafeKhongThrow() {
        when(flagRegistryCache.get("VIP")).thenReturn(false);
        when(flagRegistryCache.exists("VIP")).thenReturn(false);
        // Rule mong doi VIP=false -> van khop du cờ mat khoi cache (fail-safe)
        var rule = startStepRule(1, List.of(List.of(cond("VIP", false))), 4);

        var result = evaluator.evaluate(endpoint(List.of(rule)), null);

        assertThat(result.initialStepOrder()).isEqualTo(4);
    }

    @Test
    void flagOverrides_gheDeGiaTriCache_chiTrongLanChayThu() {
        // Cache that: VIP=false (mac dinh mock) - override try-panel VIP=true PHAI thang.
        // Khong stub get() - override phai ghe de gia tri cache (mock default false).
        var rule = startStepRule(1, List.of(List.of(cond("VIP", true))), 2);

        var result = evaluator.evaluate(endpoint(List.of(rule)), Map.of("VIP", true));

        assertThat(result.initialStepOrder()).isEqualTo(2);
    }

    @Test
    void flagOverridesKhacKey_KhongAnhHuongDieuKienCuaKeyKhac() {
        when(flagRegistryCache.get("VIP")).thenReturn(false);
        var rule = startStepRule(1, List.of(List.of(cond("VIP", true))), 2);

        var result = evaluator.evaluate(endpoint(List.of(rule)), Map.of("KYC", true));

        assertThat(result.initialStepOrder()).isNull();
    }

    private FlagRuleDto startStepRule(String id, String name, int priority,
                                      List<List<FlagRuleDto.FlagConditionDto>> groups, int startStepOrder) {
        return new FlagRuleDto(id, name, priority, groups,
                FlagRuleDto.ACTION_START_STEP, startStepOrder, null, null);
    }
}
