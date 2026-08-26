package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.entity.EndpointConfig;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EndpointService {

    private final EndpointConfigRepository repository;
    private final EndpointMapper mapper;
    private final EndpointRegistryCache registryCache;

    @Transactional(readOnly = true)
    public List<EndpointResponseDto> list(String search) {
        List<EndpointConfig> found = (search == null || search.isBlank())
                ? repository.findAllByOrderByUpdatedAtDesc()
                : repository.search(search.trim());
        return found.stream().map(mapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public EndpointResponseDto get(String id) {
        return mapper.toResponseDto(findOrThrow(id));
    }

    @Transactional
    public EndpointResponseDto create(EndpointRequestDto dto) {
        validateStepOrders(dto);
        if (repository.existsByPath(dto.path())) {
            throw new BusinessException("GW-001", "Path '" + dto.path() + "' da ton tai o mot endpoint khac.");
        }
        EndpointConfig entity = mapper.toEntity(dto);
        EndpointConfig saved = repository.save(entity);
        EndpointResponseDto result = mapper.toResponseDto(saved);
        registryCache.reload();
        log.info("Da tao endpoint moi: {} {}", saved.getMethod(), saved.getPath());
        return result;
    }

    @Transactional
    public EndpointResponseDto update(String id, EndpointRequestDto dto) {
        validateStepOrders(dto);
        EndpointConfig entity = findOrThrow(id);
        if (repository.existsByPathAndIdNot(dto.path(), id)) {
            throw new BusinessException("GW-001", "Path '" + dto.path() + "' da ton tai o mot endpoint khac.");
        }
        mapper.updateEntity(entity, dto);
        EndpointConfig saved = repository.save(entity);
        EndpointResponseDto result = mapper.toResponseDto(saved);
        registryCache.reload();
        log.info("Da cap nhat endpoint: {} {}", saved.getMethod(), saved.getPath());
        return result;
    }

    @Transactional
    public void delete(String id) {
        EndpointConfig entity = findOrThrow(id);
        repository.delete(entity);
        registryCache.reload();
        log.info("Da xoa endpoint: {} {}", entity.getMethod(), entity.getPath());
    }

    private EndpointConfig findOrThrow(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("GW-404", "Khong tim thay endpoint id=" + id));
    }

    /** Kiem tra: stepOrder phai bat dau tu 1, khong trung, va mapping phai tham chieu step co that. */
    private void validateStepOrders(EndpointRequestDto dto) {
        var orders = dto.steps().stream().map(com.bccs.gatewaymanager.dto.BackendStepDto::stepOrder).toList();
        if (orders.stream().distinct().count() != orders.size()) {
            throw new BusinessException("GW-002", "stepOrder bi trung lap giua cac backend step.");
        }
        int maxOrder = orders.stream().mapToInt(Integer::intValue).max().orElse(0);
        if (dto.mappings() != null) {
            for (var m : dto.mappings()) {
                boolean needsSourceStep = m.sourceType() != com.bccs.gatewaymanager.entity.FieldMappingSourceType.REQUEST_BODY;
                if (needsSourceStep && (m.sourceStepOrder() == null || m.sourceStepOrder() > maxOrder)) {
                    throw new BusinessException("GW-003", "FieldMapping (sourceType=" + m.sourceType()
                            + ") thieu sourceStepOrder hop le (max step = " + maxOrder + ").");
                }
                if (m.targetStepOrder() > maxOrder) {
                    throw new BusinessException("GW-003", "FieldMapping tham chieu step khong ton tai (max step = " + maxOrder + ").");
                }
            }
        }
    }
}
