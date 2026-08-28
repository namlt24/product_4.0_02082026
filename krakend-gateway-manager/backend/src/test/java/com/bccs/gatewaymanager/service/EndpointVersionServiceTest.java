package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.EndpointVersionSummaryDto;
import com.bccs.gatewaymanager.entity.EndpointChangeType;
import com.bccs.gatewaymanager.entity.EndpointConfig;
import com.bccs.gatewaymanager.entity.EndpointConfigVersion;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.EndpointConfigVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EndpointVersionServiceTest {

    @Mock
    private EndpointConfigVersionRepository repository;
    @Mock
    private EndpointMapper mapper;

    // Dung ObjectMapper THAT (khong mock) - muc dich chinh cua test nay la xac
    // nhan JSON round-trip (serialize luc ghi version, deserialize luc doc lai
    // de rollback) khop nhau, mock se khong phat hien duoc loi that trong quan
    // he do.
    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private EndpointVersionService service;

    @BeforeEach
    void setUp() {
        service = new EndpointVersionService(repository, mapper, objectMapper);
    }

    private EndpointResponseDto responseDto(String id, String name, String path) {
        BackendStepDto step = new BackendStepDto(null, 1, "step1", GatewayMethod.GET, "/x", "up-1", "up",
                false, false, 300, null, null, List.of("a"), List.of(), java.util.Map.of(), 100, 200,
                null, null,
                null, null, null, null, null, null, null, null);
        return new EndpointResponseDto(id, name, "desc", path, GatewayMethod.GET, true, "json",
                List.of(step), List.of(), null, null, false, 86400);
    }

    @Test
    void recordSnapshot_endpointChuaCoVersion_batDauTu1() {
        EndpointConfig entity = EndpointConfig.builder().id("ep-1").build();
        when(mapper.toResponseDto(entity)).thenReturn(responseDto("ep-1", "n", "/x"));
        when(repository.findTopByEndpointIdOrderByVersionNumberDesc("ep-1")).thenReturn(Optional.empty());

        service.recordSnapshot(entity, EndpointChangeType.CREATED);

        ArgumentCaptor<EndpointConfigVersion> captor = ArgumentCaptor.forClass(EndpointConfigVersion.class);
        verify(repository).save(captor.capture());
        EndpointConfigVersion saved = captor.getValue();
        assertThat(saved.getVersionNumber()).isEqualTo(1);
        assertThat(saved.getEndpointId()).isEqualTo("ep-1");
        assertThat(saved.getChangeType()).isEqualTo(EndpointChangeType.CREATED);
        assertThat(saved.getSnapshotJson()).contains("\"ep-1\"").contains("\"/x\"");
    }

    @Test
    void recordSnapshot_daCoVersion3_taoVersion4() {
        EndpointConfig entity = EndpointConfig.builder().id("ep-1").build();
        when(mapper.toResponseDto(entity)).thenReturn(responseDto("ep-1", "n", "/x"));
        when(repository.findTopByEndpointIdOrderByVersionNumberDesc("ep-1"))
                .thenReturn(Optional.of(EndpointConfigVersion.builder().versionNumber(3).build()));

        service.recordSnapshot(entity, EndpointChangeType.UPDATED);

        ArgumentCaptor<EndpointConfigVersion> captor = ArgumentCaptor.forClass(EndpointConfigVersion.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getVersionNumber()).isEqualTo(4);
    }

    @Test
    void getVersionDetail_docLaiDungSnapshotDaGhi() {
        EndpointConfig entity = EndpointConfig.builder().id("ep-1").build();
        EndpointResponseDto original = responseDto("ep-1", "ten goc", "/original-path");
        String json = objectMapper.writeValueAsString(original);
        EndpointConfigVersion stored = EndpointConfigVersion.builder()
                .id("v-1").endpointId("ep-1").versionNumber(1).changeType(EndpointChangeType.CREATED)
                .snapshotJson(json).build();
        when(repository.findById("v-1")).thenReturn(Optional.of(stored));

        EndpointResponseDto result = service.getVersionDetail("ep-1", "v-1");

        assertThat(result.name()).isEqualTo("ten goc");
        assertThat(result.path()).isEqualTo("/original-path");
        assertThat(result.steps()).hasSize(1);
        assertThat(result.steps().get(0).canvasX()).isEqualTo(100);
    }

    @Test
    void getVersionDetail_versionThuocEndpointKhac_bi404() {
        EndpointConfigVersion stored = EndpointConfigVersion.builder()
                .id("v-1").endpointId("ep-OTHER").versionNumber(1).changeType(EndpointChangeType.CREATED)
                .snapshotJson("{}").build();
        lenient().when(repository.findById("v-1")).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> service.getVersionDetail("ep-1", "v-1"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-VERSION-404");
    }

    @Test
    void getVersionDetail_versionIdKhongTonTai_bi404() {
        when(repository.findById("v-missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getVersionDetail("ep-1", "v-missing"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-VERSION-404");
    }

    @Test
    void toRequestDtoForRollback_boIdCuaStep_deEndpointMapperTaoStepMoiThayViUpdateNham() {
        EndpointResponseDto original = responseDto("ep-1", "n", "/x");
        // Gia lap step DA co id that (nhu khi doc tu DB that) - id nay phai bi bo
        // khi chuyen ve EndpointRequestDto, xem stripStepId().
        BackendStepDto stepWithId = new BackendStepDto("step-id-123", 1, "step1", GatewayMethod.GET, "/x",
                "up-1", "up", false, false, 300, null, null, List.of("a"), List.of(), java.util.Map.of(), 100, 200,
                null, null,
                null, null, null, null, null, null, null, null);
        EndpointResponseDto withStepId = new EndpointResponseDto(original.id(), original.name(), original.description(),
                original.path(), original.method(), original.sequential(), original.outputEncoding(),
                List.of(stepWithId), original.mappings(), null, null, false, 86400);
        String json = objectMapper.writeValueAsString(withStepId);
        EndpointConfigVersion stored = EndpointConfigVersion.builder()
                .id("v-1").endpointId("ep-1").versionNumber(1).changeType(EndpointChangeType.CREATED)
                .snapshotJson(json).build();
        when(repository.findById("v-1")).thenReturn(Optional.of(stored));

        EndpointRequestDto result = service.toRequestDtoForRollback("ep-1", "v-1");

        assertThat(result.steps()).hasSize(1);
        assertThat(result.steps().get(0).id()).isNull();
        assertThat(result.steps().get(0).canvasX()).isEqualTo(100);
    }

    @Test
    void listVersions_sapXepMoiNhatTruoc() {
        when(repository.findByEndpointIdOrderByVersionNumberDesc("ep-1")).thenReturn(List.of(
                EndpointConfigVersion.builder().id("v-2").endpointId("ep-1").versionNumber(2)
                        .changeType(EndpointChangeType.UPDATED).name("n").path("/x").method(GatewayMethod.GET).build(),
                EndpointConfigVersion.builder().id("v-1").endpointId("ep-1").versionNumber(1)
                        .changeType(EndpointChangeType.CREATED).name("n").path("/x").method(GatewayMethod.GET).build()
        ));

        List<EndpointVersionSummaryDto> result = service.listVersions("ep-1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).versionNumber()).isEqualTo(2);
        assertThat(result.get(1).versionNumber()).isEqualTo(1);
    }

    @Test
    void deleteAllForEndpoint_goiDungRepository() {
        when(repository.deleteByEndpointId("ep-1")).thenReturn(3L);

        service.deleteAllForEndpoint("ep-1");

        verify(repository).deleteByEndpointId("ep-1");
    }
}
