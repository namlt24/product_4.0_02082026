package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.config.CurrentTeamContext;
import com.bccs.gatewaymanager.dto.UpstreamServiceDto;
import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor;
import com.bccs.gatewaymanager.entity.UpstreamService;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import com.bccs.gatewaymanager.repository.UpstreamServiceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpstreamServiceServiceTest {

    @Mock
    private UpstreamServiceRepository repository;
    @Mock
    private EndpointConfigRepository endpointConfigRepository;
    @Mock
    private UpstreamRegistryCache upstreamRegistryCache;
    @Mock
    private UpstreamHttpExecutor upstreamHttpExecutor;

    private UpstreamServiceService service;

    private static final String TEAM = "default";

    @BeforeEach
    void setUp() {
        service = new UpstreamServiceService(repository, endpointConfigRepository,
                upstreamRegistryCache, upstreamHttpExecutor);
        // Moi method cua UpstreamServiceService gio doc CurrentTeamContext.require() -
        // xem ApiKeyAuthFilter, thiet lap gia tri gia dinh nay o tang test (khong co
        // request/filter that trong unit test).
        CurrentTeamContext.set(TEAM);
    }

    @AfterEach
    void tearDown() {
        CurrentTeamContext.clear();
    }

    private UpstreamService entity(String id, String name) {
        return UpstreamService.builder().id(id).teamCode(TEAM).name(name).baseHost("http://x:8080").build();
    }

    // ---- Nap UpstreamRegistryCache (dung cho engine, ca Try lan traffic that Data Plane truoc khi tach) ----

    @Test
    void loadUpstreamRegistryCacheAtStartup_naplaiTATCAUpstream_khongLocTheoDoi() {
        // TAT CA doi (khong chi doi hien tai) - xem javadoc class: UpstreamRegistryCache
        // phuc vu "Thu ngay"/"Thu nhanh" cho MOI request dang xu ly, quyen so huu da
        // duoc kiem tra rieng truoc do (EndpointMapper/EndpointTryService).
        var all = java.util.List.of(entity("up-1", "svc1"), entity("up-2", "svc2"));
        when(repository.findAll()).thenReturn(all);

        service.loadUpstreamRegistryCacheAtStartup();

        verify(upstreamRegistryCache).reload(all);
    }

    @Test
    void create_naplaiUpstreamRegistryCacheBangKetQuaFindAllMoiNhat() {
        var freshAll = java.util.List.of(entity("up-1", "svc"));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(repository.findAll()).thenReturn(freshAll);
        UpstreamServiceDto dto = new UpstreamServiceDto(null, "svc", null, "http://x:8080",
                1000, 3000, true, 50, true, 20, 500, null, null);

        service.create(dto);

        verify(upstreamRegistryCache).reload(freshAll);
    }

    // ---- Finding #7: chan xoa Upstream con dang duoc dung ----

    @Test
    void delete_rejectsWhenStillReferencedByBackendSteps() {
        when(repository.findByIdAndTeamCode("up-1", TEAM)).thenReturn(Optional.of(entity("up-1", "svc")));
        when(endpointConfigRepository.countStepsByUpstreamId("up-1", TEAM)).thenReturn(2L);

        assertThatThrownBy(() -> service.delete("up-1"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-UP-INUSE");

        verify(repository, never()).delete(any());
        verify(upstreamHttpExecutor, never()).invalidate(any());
    }

    @Test
    void delete_succeedsAndInvalidatesExecutorCacheWhenUnused() {
        when(repository.findByIdAndTeamCode("up-1", TEAM)).thenReturn(Optional.of(entity("up-1", "svc")));
        when(endpointConfigRepository.countStepsByUpstreamId("up-1", TEAM)).thenReturn(0L);

        service.delete("up-1");

        verify(repository).delete(any());
        verify(upstreamHttpExecutor).invalidate("svc");
    }

    // ---- Finding #4: doi cau hinh Upstream phai invalidate cache RestTemplate/CircuitBreaker/Retry/Bulkhead ----

    @Test
    void update_invalidatesExecutorCacheForOldName() {
        when(repository.findByIdAndTeamCode("up-1", TEAM)).thenReturn(Optional.of(entity("up-1", "old-name")));
        lenient().when(repository.existsByTeamCodeAndNameAndIdNot(any(), any(), any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        UpstreamServiceDto dto = new UpstreamServiceDto(null, "old-name", null, "http://x:8080",
                5000, 5000, true, 60, true, 20, 500, Instant.now(), Instant.now());

        service.update("up-1", dto);

        verify(upstreamHttpExecutor).invalidate("old-name");
    }

    @Test
    void update_invalidatesBothOldAndNewNameWhenRenamed() {
        when(repository.findByIdAndTeamCode("up-1", TEAM)).thenReturn(Optional.of(entity("up-1", "old-name")));
        lenient().when(repository.existsByTeamCodeAndNameAndIdNot(any(), any(), any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        UpstreamServiceDto dto = new UpstreamServiceDto(null, "new-name", null, "http://x:8080",
                5000, 5000, true, 60, true, 20, 500, Instant.now(), Instant.now());

        service.update("up-1", dto);

        verify(upstreamHttpExecutor).invalidate("old-name");
        verify(upstreamHttpExecutor).invalidate("new-name");
    }

    // ---- Bulkhead cau hinh duoc theo tung Upstream (truoc day fix cung 20/500ms) ----

    @Test
    void create_wireDungMaxConcurrentCallsVaMaxWaitDurationMsVaoEntity() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        UpstreamServiceDto dto = new UpstreamServiceDto(null, "svc", null, "http://x:8080",
                1000, 3000, true, 50, true, 5, 250, null, null);

        UpstreamServiceDto result = service.create(dto);

        ArgumentCaptor<UpstreamService> captor = ArgumentCaptor.forClass(UpstreamService.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getMaxConcurrentCalls()).isEqualTo(5);
        assertThat(captor.getValue().getMaxWaitDurationMs()).isEqualTo(250);
        assertThat(result.maxConcurrentCalls()).isEqualTo(5);
        assertThat(result.maxWaitDurationMs()).isEqualTo(250);
    }

    @Test
    void update_wireDungMaxConcurrentCallsVaMaxWaitDurationMsVaoEntity() {
        when(repository.findByIdAndTeamCode("up-1", TEAM)).thenReturn(Optional.of(entity("up-1", "svc")));
        lenient().when(repository.existsByTeamCodeAndNameAndIdNot(any(), any(), any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        UpstreamServiceDto dto = new UpstreamServiceDto(null, "svc", null, "http://x:8080",
                5000, 5000, true, 60, true, 8, 750, Instant.now(), Instant.now());

        service.update("up-1", dto);

        ArgumentCaptor<UpstreamService> captor = ArgumentCaptor.forClass(UpstreamService.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getMaxConcurrentCalls()).isEqualTo(8);
        assertThat(captor.getValue().getMaxWaitDurationMs()).isEqualTo(750);
    }
}
