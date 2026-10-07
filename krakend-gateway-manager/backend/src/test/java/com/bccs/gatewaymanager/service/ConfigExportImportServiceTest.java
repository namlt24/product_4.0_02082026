package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.ConfigExportDto;
import com.bccs.gatewaymanager.dto.ConfigImportResultDto;
import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.UpstreamServiceDto;
import com.bccs.gatewaymanager.config.CurrentTeamContext;
import com.bccs.gatewaymanager.entity.EndpointConfig;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import com.bccs.gatewaymanager.repository.EndpointConfigVersionRepository;
import com.bccs.gatewaymanager.repository.UpstreamServiceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigExportImportServiceTest {

    @Mock
    private UpstreamServiceRepository upstreamRepository;
    @Mock
    private UpstreamServiceService upstreamService;
    @Mock
    private EndpointConfigRepository endpointRepository;
    @Mock
    private EndpointService endpointService;
    @Mock
    private EndpointMapper endpointMapper;
    @Mock
    private EndpointConfigVersionRepository versionRepository;
    @Mock
    private com.bccs.gatewaymanager.repository.GwmFlagRepository flagRepository;

    private static final String TEAM = "default";

    private ConfigExportImportService service;

    @BeforeEach
    void setUp() {
        service = new ConfigExportImportService(upstreamRepository, upstreamService, endpointRepository, endpointService, endpointMapper, versionRepository, flagRepository);
        CurrentTeamContext.set(TEAM);
        // fingerprint cho ETag/304 (xem ConfigExportImportService.computeConfigHash()) - moi
        // query aggregate LUON tra dung 1 row, dung lenient() vi khong phai test nao cung
        // thuc su goi export() (vi du cac test chi test importConfig()).
        lenient().when(upstreamRepository.fingerprintByTeamCode(TEAM)).thenReturn(List.<Object[]>of(new Object[]{0L, null}));
        lenient().when(endpointRepository.fingerprintByTeamCode(TEAM)).thenReturn(List.<Object[]>of(new Object[]{0L, null}));
        lenient().when(versionRepository.fingerprintByTeamCode(TEAM)).thenReturn(List.<Object[]>of(new Object[]{0L, null}));
        lenient().when(flagRepository.fingerprintByTeamCode(TEAM)).thenReturn(List.<Object[]>of(new Object[]{0L, null}));
    }

    @AfterEach
    void tearDown() {
        CurrentTeamContext.clear();
    }

    private UpstreamServiceDto upstreamDto(String id, String name) {
        return new UpstreamServiceDto(id, name, null, "http://x", 1000, 3000, true, 50, true, 20, 500, null, null);
    }

    private BackendStepDto step(String upstreamId, String upstreamName) {
        return new BackendStepDto(null, 1, "s1", GatewayMethod.GET, "/x", upstreamId, upstreamName,
                false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                null, null,
                null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private EndpointResponseDto endpointDto(String path, String upstreamId, String upstreamName) {
        return new EndpointResponseDto("ep-old-id", "n", null, path, GatewayMethod.GET, true, "json",
                List.of(step(upstreamId, upstreamName)), List.of(), null, null, false, 86400, false, false, 300, List.of(), null);
    }

    @Test
    void export_gopUpstreamVaEndpointTuMapper() {
        when(upstreamService.list()).thenReturn(List.of(upstreamDto("u-1", "svc")));
        EndpointConfig entity = EndpointConfig.builder().id("ep-1").build();
        when(endpointRepository.findAllByTeamCodeOrderByUpdatedAtDesc(TEAM)).thenReturn(List.of(entity));
        when(endpointMapper.toResponseDto(entity)).thenReturn(endpointDto("/x", "u-1", "svc"));

        ResponseEntity<ConfigExportDto> response = service.export(null);
        ConfigExportDto result = response.getBody();

        assertThat(result.schemaVersion()).isEqualTo("1.0");
        assertThat(result.upstreams()).hasSize(1);
        assertThat(result.endpoints()).hasSize(1);
        assertThat(result.exportedAt()).isNotNull();
    }

    @Test
    void import_upstreamChuaTonTai_taoMoi() {
        lenient().when(upstreamService.list()).thenReturn(List.of());
        when(upstreamService.create(any())).thenReturn(upstreamDto("u-new", "svc-new"));
        ConfigExportDto bundle = new ConfigExportDto("1.0", Instant.now(), List.of(upstreamDto(null, "svc-new")), List.of(), List.of());

        ConfigImportResultDto result = service.importConfig(bundle);

        assertThat(result.upstreamsCreated()).isEqualTo(1);
        assertThat(result.upstreamsUpdated()).isEqualTo(0);
        verify(upstreamService).create(any());
        verify(upstreamService, never()).update(any(), any());
    }

    @Test
    void import_upstreamDaTonTaiTheoTen_capNhat() {
        lenient().when(upstreamService.list()).thenReturn(List.of(upstreamDto("u-1", "svc")));
        ConfigExportDto bundle = new ConfigExportDto("1.0", Instant.now(), List.of(upstreamDto("u-other-env", "svc")), List.of(), List.of());

        ConfigImportResultDto result = service.importConfig(bundle);

        assertThat(result.upstreamsUpdated()).isEqualTo(1);
        verify(upstreamService).update(org.mockito.ArgumentMatchers.eq("u-1"), any());
        verify(upstreamService, never()).create(any());
    }

    @Test
    void import_endpointChuaTonTaiTheoPath_taoMoi() {
        lenient().when(upstreamService.list()).thenReturn(List.of(upstreamDto("u-1", "svc")));
        when(endpointRepository.findByTeamCodeAndPath(TEAM, "/v1/new")).thenReturn(Optional.empty());
        ConfigExportDto bundle = new ConfigExportDto("1.0", Instant.now(), List.of(), List.of(endpointDto("/v1/new", "u-1", "svc")), List.of());

        ConfigImportResultDto result = service.importConfig(bundle);

        assertThat(result.endpointsCreated()).isEqualTo(1);
        verify(endpointService).create(any());
        verify(endpointService, never()).update(any(), any());
    }

    @Test
    void import_endpointDaTonTaiTheoPath_capNhat() {
        lenient().when(upstreamService.list()).thenReturn(List.of(upstreamDto("u-1", "svc")));
        EndpointConfig existing = EndpointConfig.builder().id("ep-existing").build();
        when(endpointRepository.findByTeamCodeAndPath(TEAM, "/v1/dup")).thenReturn(Optional.of(existing));
        ConfigExportDto bundle = new ConfigExportDto("1.0", Instant.now(), List.of(), List.of(endpointDto("/v1/dup", "u-1", "svc")), List.of());

        ConfigImportResultDto result = service.importConfig(bundle);

        assertThat(result.endpointsUpdated()).isEqualTo(1);
        verify(endpointService).update(org.mockito.ArgumentMatchers.eq("ep-existing"), any());
    }

    @Test
    void import_upstreamThamChieuKhongTonTai_ghiCanhBaoNhungVanTaoEndpoint() {
        lenient().when(upstreamService.list()).thenReturn(List.of()); // khong co upstream "svc-missing" nao ca
        when(endpointRepository.findByTeamCodeAndPath(TEAM, "/v1/x")).thenReturn(Optional.empty());
        ConfigExportDto bundle = new ConfigExportDto("1.0", Instant.now(), List.of(),
                List.of(endpointDto("/v1/x", "u-old-env-id", "svc-missing")), List.of());

        ConfigImportResultDto result = service.importConfig(bundle);

        assertThat(result.warnings()).hasSize(1);
        assertThat(result.warnings().get(0)).contains("svc-missing");
        verify(endpointService).create(any());

        ArgumentCaptor<EndpointRequestDto> captor = ArgumentCaptor.forClass(EndpointRequestDto.class);
        verify(endpointService).create(captor.capture());
        // Fallback: giu nguyen upstreamServiceId cu (se rat co the loi khi EndpointService that su
        // luu - dung y, dam bao KHONG am tham gan bua 1 upstream khac) thay vi crash ngay tai day.
        assertThat(captor.getValue().steps().get(0).upstreamServiceId()).isEqualTo("u-old-env-id");
    }

    // ---- V5: export/import CỜ (GWM_FLAG) ----

    private com.bccs.gatewaymanager.dto.FlagDto flagDto(String code, boolean enabled) {
        return new com.bccs.gatewaymanager.dto.FlagDto(null, TEAM, code, "Cờ " + code, null,
                enabled, Instant.now(), Instant.now());
    }

    @Test
    void export_kemTheoDanhSachCoCuaDoi() {
        when(upstreamService.list()).thenReturn(List.of());
        when(endpointRepository.findAllByTeamCodeOrderByUpdatedAtDesc(TEAM)).thenReturn(List.of());
        when(flagRepository.findByTeamCode(TEAM)).thenReturn(java.util.List.of(
                com.bccs.gatewaymanager.entity.GwmFlag.builder()
                        .id("f-1").teamCode(TEAM).flagCode("VIP").displayName("Khach VIP")
                        .enabled(true).build()));

        ResponseEntity<ConfigExportDto> response = service.export(null);
        ConfigExportDto result = response.getBody();

        assertThat(result.flags()).hasSize(1);
        assertThat(result.flags().get(0).flagCode()).isEqualTo("VIP");
        assertThat(result.flags().get(0).enabled()).isTrue();
    }

    @Test
    void import_coChuaTonTai_taoMoiTruocKhiTaoEndpoint() {
        when(flagRepository.findByTeamCodeAndFlagCodeIgnoreCase(TEAM, "VIP"))
                .thenReturn(java.util.Optional.empty());
        lenient().when(upstreamService.list()).thenReturn(List.of());
        when(endpointRepository.findByTeamCodeAndPath(TEAM, "/v1/x")).thenReturn(Optional.empty());
        var bundle = new ConfigExportDto("1.0", Instant.now(), List.of(),
                List.of(endpointDto("/v1/x", "u-1", "svc")),
                List.of(flagDto("VIP", true)));

        var result = service.importConfig(bundle);

        assertThat(result.endpointsCreated()).isEqualTo(1);
        verify(flagRepository).save(org.mockito.ArgumentMatchers.argThat(
                (com.bccs.gatewaymanager.entity.GwmFlag f) -> "VIP".equals(f.getFlagCode()) && f.isEnabled()));
    }

    @Test
    void import_coDaTonTaiTheoFlagCode_capNhat_giuNguyenId() {
        when(flagRepository.findByTeamCodeAndFlagCodeIgnoreCase(TEAM, "VIP"))
                .thenReturn(java.util.Optional.of(com.bccs.gatewaymanager.entity.GwmFlag.builder()
                        .id("f-existing").teamCode(TEAM).flagCode("VIP").displayName("Ten cu")
                        .enabled(false).build()));
        lenient().when(upstreamService.list()).thenReturn(List.of());
        var bundle = new ConfigExportDto("1.0", Instant.now(), List.of(), List.of(),
                List.of(flagDto("VIP", true)));

        var result = service.importConfig(bundle);

        assertThat(result.endpointsCreated()).isEqualTo(0);
        ArgumentCaptor<com.bccs.gatewaymanager.entity.GwmFlag> captor =
                ArgumentCaptor.forClass(com.bccs.gatewaymanager.entity.GwmFlag.class);
        verify(flagRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo("f-existing");
        assertThat(captor.getValue().isEnabled()).isTrue();
    }
}
