package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.config.CurrentTeamContext;
import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.entity.ConditionOperator;
import com.bccs.gatewaymanager.entity.EndpointConfig;
import com.bccs.gatewaymanager.entity.FieldMappingSourceType;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import com.bccs.gatewaymanager.entity.UpstreamService;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.UpstreamServiceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * EndpointMapper.findUpstreamOrThrow() la diem chan tham-chieu-cheo-doi DUY
 * NHAT tren duong LUU that (Thu nhanh co diem chan RIENG, xem
 * EndpointTryServiceTest) - test nay xac nhan 1 doi KHONG the tao/sua
 * Endpoint tham chieu Upstream cua doi khac du biet dung ID, va rang loi tra
 * ve giong het truong hop ID khong ton tai (khong tiet lo Upstream do co
 * that hay khong).
 */
@ExtendWith(MockitoExtension.class)
class EndpointMapperTest {

    private static final String TEAM = "TEAM_A";
    private static final String OTHER_TEAM = "TEAM_B";

    @Mock
    private UpstreamServiceRepository upstreamServiceRepository;

    private EndpointMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EndpointMapper(upstreamServiceRepository, new tools.jackson.databind.ObjectMapper());
        CurrentTeamContext.set(TEAM);
    }

    @AfterEach
    void tearDown() {
        CurrentTeamContext.clear();
    }

    private BackendStepDto step(String upstreamId, String compensationUpstreamId) {
        return new BackendStepDto(null, 1, "step1", GatewayMethod.GET, "/x", upstreamId, "up",
                false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null, null, null,
                null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                compensationUpstreamId, compensationUpstreamId == null ? null : "comp-up", null, null);
    }

    private EndpointRequestDto draft(String upstreamId, String compensationUpstreamId) {
        return new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(upstreamId, compensationUpstreamId)), List.of(), false, null, false, false, null, List.of(), null);
    }

    @Test
    void toEntity_stampTeamCodeTuCurrentTeamContext() {
        when(upstreamServiceRepository.findById("up-1"))
                .thenReturn(Optional.of(UpstreamService.builder().id("up-1").teamCode(TEAM).name("up").build()));

        EndpointConfig entity = mapper.toEntity(draft("up-1", null));

        assertThat(entity.getTeamCode()).isEqualTo(TEAM);
    }

    @Test
    void toEntity_upstreamThuocDoiKhac_bi404_khongLoDuLieu() {
        when(upstreamServiceRepository.findById("up-1"))
                .thenReturn(Optional.of(UpstreamService.builder().id("up-1").teamCode(OTHER_TEAM).name("up").build()));

        assertThatThrownBy(() -> mapper.toEntity(draft("up-1", null)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-UP-404");
    }

    @Test
    void toEntity_upstreamKhongTonTai_bi404_dungThongDiepVoiTruongHopThuocDoiKhac() {
        // Cung 1 thong diep loi cho ca 2 truong hop (ID sai hoan toan HOAC ID dung
        // nhung thuoc doi khac) - khong duoc tiet lo qua thong diep rang ID do co
        // ton tai o he thong (kenh do IDOR).
        when(upstreamServiceRepository.findById("up-missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mapper.toEntity(draft("up-missing", null)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-UP-404");
    }

    @Test
    void toEntity_upstreamBuTruThuocDoiKhac_bi404() {
        when(upstreamServiceRepository.findById("up-1"))
                .thenReturn(Optional.of(UpstreamService.builder().id("up-1").teamCode(TEAM).name("up").build()));
        when(upstreamServiceRepository.findById("up-comp-other-team"))
                .thenReturn(Optional.of(UpstreamService.builder().id("up-comp-other-team").teamCode(OTHER_TEAM).name("comp").build()));

        assertThatThrownBy(() -> mapper.toEntity(draft("up-1", "up-comp-other-team")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-UP-404");
    }

    @Test
    void toEntity_upstreamVaBuTruDeuThuocDungDoi_thanhCong() {
        when(upstreamServiceRepository.findById("up-1"))
                .thenReturn(Optional.of(UpstreamService.builder().id("up-1").teamCode(TEAM).name("up").build()));
        when(upstreamServiceRepository.findById("up-comp"))
                .thenReturn(Optional.of(UpstreamService.builder().id("up-comp").teamCode(TEAM).name("comp").build()));

        EndpointConfig entity = mapper.toEntity(draft("up-1", "up-comp"));

        assertThat(entity.getSteps()).hasSize(1);
        assertThat(entity.getSteps().get(0).getUpstreamService().getId()).isEqualTo("up-1");
        assertThat(entity.getSteps().get(0).getCompensationUpstreamService().getId()).isEqualTo("up-comp");
    }
    // ==================== Tinh nang ResponseMapping - round-trip DTO <-> CLOB ====================

    @Test
    void responseMapping_roundTrip_dtoQuaClobVeDungGiaTri() {
        var spec = new com.bccs.gatewaymanager.dto.ResponseMappingSpecDto("REPLACE",
                List.of(new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(1, "data.code", "data.code", 0),
                        new com.bccs.gatewaymanager.dto.ResponseFieldMappingDto(2, "code", "code", 1)));
        when(upstreamServiceRepository.findById("up-1"))
                .thenReturn(Optional.of(UpstreamService.builder().id("up-1").teamCode(TEAM).name("up").build()));
        CurrentTeamContext.set(TEAM);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(new BackendStepDto(null, 1, "s1", GatewayMethod.GET, "/x", "up-1", "up",
                        false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null, null, null,
                        null, null,
                        null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null)),
                List.of(), false, null, false, false, 300, List.of(), spec);

        EndpointConfig entity = mapper.toEntity(dto);
        var back = mapper.toResponseDto(entity);

        assertThat(back.responseMapping()).isNotNull();
        assertThat(back.responseMapping().mode()).isEqualTo("REPLACE");
        assertThat(back.responseMapping().mappings()).hasSize(2);
        assertThat(back.responseMapping().mappings().get(1).sourceStepOrder()).isEqualTo(2);
        assertThat(back.responseMapping().mappings().get(1).sourceField()).isEqualTo("code");
    }

    @Test
    void responseMapping_null_KhongGhiClob_khiDocTraNull() {
        when(upstreamServiceRepository.findById("up-1"))
                .thenReturn(Optional.of(UpstreamService.builder().id("up-1").teamCode(TEAM).name("up").build()));
        CurrentTeamContext.set(TEAM);
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(new BackendStepDto(null, 1, "s1", GatewayMethod.GET, "/x", "up-1", "up",
                        false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null, null, null,
                        null, null,
                        null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null)),
                List.of(), false, null, false, false, 300, List.of(), null);

        EndpointConfig entity = mapper.toEntity(dto);
        var back = mapper.toResponseDto(entity);

        assertThat(entity.getResponseMappingsJson()).isNull();
        assertThat(back.responseMapping()).isNull();
    }

    @Test
    void conditionGroups_roundTrip_dtoQuaClobVeDungGiaTri() {
        when(upstreamServiceRepository.findById("up-1"))
                .thenReturn(Optional.of(UpstreamService.builder().id("up-1").teamCode(TEAM).name("up").build()));
        var groups = List.of(
                List.of(new com.bccs.gatewaymanager.dto.ConditionRuleDto(FieldMappingSourceType.REQUEST_BODY, "permissionCodes", null, ConditionOperator.CONTAINS, "A"),
                        new com.bccs.gatewaymanager.dto.ConditionRuleDto(FieldMappingSourceType.STEP_RESPONSE, "ok", 1, ConditionOperator.EQUALS, "true")),
                List.of(new com.bccs.gatewaymanager.dto.ConditionRuleDto(FieldMappingSourceType.REQUEST_BODY, "x", null, ConditionOperator.NOT_CONTAINS, "B")));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(new BackendStepDto(null, 1, "s1", GatewayMethod.GET, "/x", "up-1", "up",
                        false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null, null, null,
                        null, null,
                        FieldMappingSourceType.REQUEST_BODY, null, "x", ConditionOperator.EXISTS, null,
                        null, groups, null, null, null, null, null, null, null, null)),
                List.of(), false, null, false, false, 300, List.of(), null);

        EndpointConfig entity = mapper.toEntity(dto);
        var back = mapper.toResponseDto(entity);

        assertThat(entity.getSteps().get(0).getConditionGroupsJson()).isNotBlank();
        assertThat(back.steps().get(0).conditionGroups()).isEqualTo(groups);
    }

    @Test
    void switchBranches_roundTrip_dtoQuaClobVeDungGiaTri() {
        when(upstreamServiceRepository.findById("up-1"))
                .thenReturn(Optional.of(UpstreamService.builder().id("up-1").teamCode(TEAM).name("up").build()));
        var branches = List.of(new com.bccs.gatewaymanager.dto.SwitchBranchDto(
                List.of(List.of(new com.bccs.gatewaymanager.dto.ConditionRuleDto(FieldMappingSourceType.REQUEST_BODY, "tier", null,
                        ConditionOperator.EQUALS, "gold"))), 2));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(new BackendStepDto(null, 1, "s1", GatewayMethod.GET, "/x", "up-1", "up",
                        false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, 2, null, null, null, null, null, null, branches)),
                List.of(), false, null, false, false, 300, List.of(), null);

        EndpointConfig entity = mapper.toEntity(dto);
        var back = mapper.toResponseDto(entity);

        assertThat(entity.getSteps().get(0).getSwitchBranchesJson()).isNotBlank();
        assertThat(back.steps().get(0).switchBranches()).isEqualTo(branches);
    }

    @Test
    void quantifier_roundTrip_giuNguyenKieuLapMang() {
        when(upstreamServiceRepository.findById("up-1"))
                .thenReturn(Optional.of(UpstreamService.builder().id("up-1").teamCode(TEAM).name("up").build()));
        var lap = com.bccs.gatewaymanager.dto.ConditionRuleDto.quantifier(FieldMappingSourceType.REQUEST_BODY, "items", null,
                "COUNT", "AND", List.of(new com.bccs.gatewaymanager.dto.ConditionRuleDto(FieldMappingSourceType.REQUEST_BODY,
                        "status", null, ConditionOperator.EQUALS, "ACTIVE")), ConditionOperator.GREATER_THAN_OR_EQUAL, "2");
        var groups = List.of(List.of(lap));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(new BackendStepDto(null, 1, "s1", GatewayMethod.GET, "/x", "up-1", "up",
                        false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null, null, null,
                        null, null, FieldMappingSourceType.REQUEST_BODY, null, "x",
                        ConditionOperator.EXISTS, null, null, groups, null, null, null, null, null, null, null, null, null)),
                List.of(), false, null, false, false, 300, List.of(), null);

        var back = mapper.toResponseDto(mapper.toEntity(dto));

        var restored = back.steps().get(0).conditionGroups().get(0).get(0);
        assertThat(restored.quantifier()).isEqualTo("COUNT");
        assertThat(restored.countOperator()).isEqualTo(ConditionOperator.GREATER_THAN_OR_EQUAL);
        assertThat(restored.countValue()).isEqualTo("2");
        assertThat(restored.children()).hasSize(1);
    }

    @Test
    void baoLoiNhanhSai_roundTrip_giuNguyenMaVaThongDiep() {
        when(upstreamServiceRepository.findById("up-1"))
                .thenReturn(Optional.of(UpstreamService.builder().id("up-1").teamCode(TEAM).name("up").build()));
        EndpointRequestDto dto = new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(new BackendStepDto(null, 1, "s1", GatewayMethod.GET, "/x", "up-1", "up",
                        false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null, null, null,
                        null, null,
                        FieldMappingSourceType.REQUEST_BODY, null, "tier", ConditionOperator.EQUALS, "gold",
                        null, null, 2, null, null, null, null, null, null, null, null, "GW-TIER", "Khong hop le")),
                List.of(), false, null, false, false, 300, List.of(), null);
        var back = mapper.toResponseDto(mapper.toEntity(dto));
        assertThat(back.steps().get(0).falseErrorCode()).isEqualTo("GW-TIER");
        assertThat(back.steps().get(0).falseErrorMessage()).isEqualTo("Khong hop le");
    }
}
