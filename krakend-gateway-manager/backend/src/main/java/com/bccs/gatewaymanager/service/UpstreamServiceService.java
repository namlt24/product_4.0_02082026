package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.config.CurrentTeamContext;
import com.bccs.gatewaymanager.dto.UpstreamServiceDto;
import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor;
import com.bccs.gatewaymanager.entity.UpstreamService;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import com.bccs.gatewaymanager.repository.UpstreamServiceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Control-Plane-only (@Profile) - CRUD dang ky Upstream Service (backend
 * that), dung 1 lan, tai su dung o nhieu BackendStep.
 *
 * Class nay CON giu vai tro nap UpstreamRegistryCache (khac EndpointRegistryCache
 * - da bo hoan toan khoi Control Plane tu 2026-09, xem javadoc EndpointService)
 * - ly do: UpstreamRegistryCache duoc CompositeOrchestratorEngine dung ca cho
 * traffic that (Data Plane) LAN "Thu ngay"/"Thu nhanh" (Control Plane, xem
 * EndpointTryService) - Control Plane can nap TOAN BO Upstream (moi doi, KHONG
 * loc theo CurrentTeamContext o day) de "Thu" hoat dong dung cho request dang
 * xu ly, an toan vi quyen so huu Upstream da duoc kiem tra RIENG truoc khi toi
 * duoc buoc goi engine (xem EndpointMapper.findUpstreamOrThrow()/
 * EndpointTryService.validateUpstreamOwnership()).
 */
@Slf4j
@Service
@Profile("control-plane")
@RequiredArgsConstructor
public class UpstreamServiceService {

    private final UpstreamServiceRepository repository;
    private final EndpointConfigRepository endpointConfigRepository;
    private final UpstreamRegistryCache upstreamRegistryCache;
    private final UpstreamHttpExecutor upstreamHttpExecutor;

    @PostConstruct
    void loadUpstreamRegistryCacheAtStartup() {
        upstreamRegistryCache.reload(repository.findAll());
    }

    @Transactional(readOnly = true)
    public List<UpstreamServiceDto> list() {
        return repository.findAllByTeamCodeOrderByNameAsc(CurrentTeamContext.require()).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public UpstreamServiceDto get(String id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public UpstreamServiceDto create(UpstreamServiceDto dto) {
        String teamCode = CurrentTeamContext.require();
        if (repository.existsByTeamCodeAndName(teamCode, dto.name())) {
            throw new BusinessException("GW-UP-001", "Upstream ten '" + dto.name() + "' da ton tai.");
        }
        UpstreamService entity = UpstreamService.builder()
                .teamCode(teamCode)
                .name(dto.name())
                .description(dto.description())
                .baseHost(stripTrailingSlash(dto.baseHost()))
                .connectTimeoutMs(dto.connectTimeoutMs())
                .readTimeoutMs(dto.readTimeoutMs())
                .circuitBreakerEnabled(dto.circuitBreakerEnabled())
                .failureRateThreshold(dto.failureRateThreshold())
                .retryEnabled(dto.retryEnabled())
                .maxConcurrentCalls(dto.maxConcurrentCalls())
                .maxWaitDurationMs(dto.maxWaitDurationMs())
                .build();
        UpstreamService saved = repository.save(entity);
        upstreamRegistryCache.reload(repository.findAll());
        log.info("Da dang ky Upstream Service moi: {} -> {}", saved.getName(), saved.getBaseHost());
        return toDto(saved);
    }

    @Transactional
    public UpstreamServiceDto update(String id, UpstreamServiceDto dto) {
        UpstreamService entity = findOrThrow(id);
        if (repository.existsByTeamCodeAndNameAndIdNot(CurrentTeamContext.require(), dto.name(), id)) {
            throw new BusinessException("GW-UP-001", "Upstream ten '" + dto.name() + "' da ton tai.");
        }
        String oldName = entity.getName();
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setBaseHost(stripTrailingSlash(dto.baseHost()));
        entity.setConnectTimeoutMs(dto.connectTimeoutMs());
        entity.setReadTimeoutMs(dto.readTimeoutMs());
        entity.setCircuitBreakerEnabled(dto.circuitBreakerEnabled());
        entity.setFailureRateThreshold(dto.failureRateThreshold());
        entity.setRetryEnabled(dto.retryEnabled());
        entity.setMaxConcurrentCalls(dto.maxConcurrentCalls());
        entity.setMaxWaitDurationMs(dto.maxWaitDurationMs());
        UpstreamService saved = repository.save(entity);
        // Upstream doi (timeout/circuit-breaker) anh huong toi moi endpoint dang tham
        // chieu no - nap lai cache trong-process de co hieu luc ngay cho Control Plane
        // (Data Plane tu dong bo rieng qua RemoteConfigSyncService, xem javadoc class).
        upstreamRegistryCache.reload(repository.findAll());
        // reload() o tren chi lam UpstreamService entity moi hon trong cache - RestTemplate/
        // CircuitBreaker/Retry/Bulkhead cua UpstreamHttpExecutor van la instance CU (tao 1 lan
        // duy nhat theo ten, xem UpstreamHttpExecutor.invalidate() javadoc) neu khong xoa o
        // day, doi connectTimeoutMs/failureRateThreshold... se khong co tac dung gi.
        upstreamHttpExecutor.invalidate(oldName);
        if (!oldName.equals(saved.getName())) {
            upstreamHttpExecutor.invalidate(saved.getName());
        }
        log.info("Da cap nhat Upstream Service: {}", saved.getName());
        return toDto(saved);
    }

    @Transactional
    public void delete(String id) {
        UpstreamService entity = findOrThrow(id);
        long stepCount = endpointConfigRepository.countStepsByUpstreamId(id, CurrentTeamContext.require());
        if (stepCount > 0) {
            throw new BusinessException("GW-UP-INUSE", "Upstream '" + entity.getName()
                    + "' dang duoc dung boi " + stepCount + " backend step(s), khong the xoa.");
        }
        repository.delete(entity);
        upstreamRegistryCache.reload(repository.findAll());
        upstreamHttpExecutor.invalidate(entity.getName());
        log.info("Da xoa Upstream Service: {}", entity.getName());
    }

    /** Tra theo id VA CurrentTeamContext - 1 doi khong the sua/xoa Upstream cua doi khac du doan dung ID (IDOR). */
    private UpstreamService findOrThrow(String id) {
        return repository.findByIdAndTeamCode(id, CurrentTeamContext.require())
                .orElseThrow(() -> new BusinessException("GW-UP-404", "Khong tim thay Upstream Service id=" + id));
    }

    private String stripTrailingSlash(String host) {
        return host != null && host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
    }

    private UpstreamServiceDto toDto(UpstreamService e) {
        return new UpstreamServiceDto(e.getId(), e.getName(), e.getDescription(), e.getBaseHost(),
                e.getConnectTimeoutMs(), e.getReadTimeoutMs(), e.isCircuitBreakerEnabled(),
                e.getFailureRateThreshold(), e.isRetryEnabled(),
                e.getMaxConcurrentCalls(), e.getMaxWaitDurationMs(),
                e.getCreatedAt(), e.getUpdatedAt());
    }
}
