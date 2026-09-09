package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.config.CurrentTeamContext;
import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.entity.EndpointConfig;
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
        mapper = new EndpointMapper(upstreamServiceRepository);
        CurrentTeamContext.set(TEAM);
    }

    @AfterEach
    void tearDown() {
        CurrentTeamContext.clear();
    }

    private BackendStepDto step(String upstreamId, String compensationUpstreamId) {
        return new BackendStepDto(null, 1, "step1", GatewayMethod.GET, "/x", upstreamId, "up",
                false, false, 300, null, null, List.of(), List.of(), Map.of(), null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                compensationUpstreamId, compensationUpstreamId == null ? null : "comp-up", null, null);
    }

    private EndpointRequestDto draft(String upstreamId, String compensationUpstreamId) {
        return new EndpointRequestDto("n", null, "/x", GatewayMethod.GET, true, "json",
                List.of(step(upstreamId, compensationUpstreamId)), List.of(), false, null, false, false, null);
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
}
