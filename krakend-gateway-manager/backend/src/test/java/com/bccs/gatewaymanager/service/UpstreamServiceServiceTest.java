package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.UpstreamServiceDto;
import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor;
import com.bccs.gatewaymanager.entity.UpstreamService;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import com.bccs.gatewaymanager.repository.UpstreamServiceRepository;
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
    private EndpointRegistryCache registryCache;
    @Mock
    private UpstreamRegistryCache upstreamRegistryCache;
    @Mock
    private UpstreamHttpExecutor upstreamHttpExecutor;

    private UpstreamServiceService service;

    @BeforeEach
    void setUp() {
        service = new UpstreamServiceService(repository, endpointConfigRepository, registryCache,
                upstreamRegistryCache, upstreamHttpExecutor);
    }

    private UpstreamService entity(String id, String name) {
        return UpstreamService.builder().id(id).name(name).baseHost("http://x:8080").build();
    }

    // ---- Finding #7: chan xoa Upstream con dang duoc dung ----

    @Test
    void delete_rejectsWhenStillReferencedByBackendSteps() {
        when(repository.findById("up-1")).thenReturn(Optional.of(entity("up-1", "svc")));
        when(endpointConfigRepository.countStepsByUpstreamId("up-1")).thenReturn(2L);

        assertThatThrownBy(() -> service.delete("up-1"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo("GW-UP-INUSE");

        verify(repository, never()).delete(any());
        verify(upstreamHttpExecutor, never()).invalidate(any());
    }

    @Test
    void delete_succeedsAndInvalidatesExecutorCacheWhenUnused() {
        when(repository.findById("up-1")).thenReturn(Optional.of(entity("up-1", "svc")));
        when(endpointConfigRepository.countStepsByUpstreamId("up-1")).thenReturn(0L);

        service.delete("up-1");

        verify(repository).delete(any());
        verify(upstreamHttpExecutor).invalidate("svc");
    }

    // ---- Finding #4: doi cau hinh Upstream phai invalidate cache RestTemplate/CircuitBreaker/Retry/Bulkhead ----

    @Test
    void update_invalidatesExecutorCacheForOldName() {
        when(repository.findById("up-1")).thenReturn(Optional.of(entity("up-1", "old-name")));
        lenient().when(repository.existsByNameAndIdNot(any(), any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        UpstreamServiceDto dto = new UpstreamServiceDto(null, "old-name", null, "http://x:8080",
                5000, 5000, true, 60, true, 20, 500, Instant.now(), Instant.now());

        service.update("up-1", dto);

        verify(upstreamHttpExecutor).invalidate("old-name");
    }

    @Test
    void update_invalidatesBothOldAndNewNameWhenRenamed() {
        when(repository.findById("up-1")).thenReturn(Optional.of(entity("up-1", "old-name")));
        lenient().when(repository.existsByNameAndIdNot(any(), any())).thenReturn(false);
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
        when(repository.findById("up-1")).thenReturn(Optional.of(entity("up-1", "svc")));
        lenient().when(repository.existsByNameAndIdNot(any(), any())).thenReturn(false);
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
