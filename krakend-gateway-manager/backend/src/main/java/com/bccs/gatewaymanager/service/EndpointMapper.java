package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.FieldMappingDto;
import com.bccs.gatewaymanager.entity.BackendStep;
import com.bccs.gatewaymanager.entity.EndpointConfig;
import com.bccs.gatewaymanager.entity.FieldMapping;
import com.bccs.gatewaymanager.entity.UpstreamService;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.UpstreamServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Chuyen doi thu cong giua DTO (bien API) va Entity (model luu DB).
 * Khong dung MapStruct de giu build don gian, khong phu thuoc annotation
 * processor - phu hop voi mot service nho, so field it.
 */
@Component
@RequiredArgsConstructor
public class EndpointMapper {

    private final UpstreamServiceRepository upstreamServiceRepository;

    /** Dung khi tao moi hoac update - entity chua duoc gan id/timestamp. */
    public EndpointConfig toEntity(EndpointRequestDto dto) {
        EndpointConfig entity = EndpointConfig.builder()
                .name(dto.name())
                .description(dto.description())
                .path(dto.path())
                .method(dto.method())
                .sequential(dto.sequential())
                .outputEncoding(dto.outputEncoding())
                .build();
        applySteps(entity, dto.steps());
        applyMappings(entity, dto.mappings());
        return entity;
    }

    /** Cap nhat entity da co (giu nguyen id) tu request DTO khi PUT. */
    public void updateEntity(EndpointConfig entity, EndpointRequestDto dto) {
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setPath(dto.path());
        entity.setMethod(dto.method());
        entity.setSequential(dto.sequential());
        entity.setOutputEncoding(dto.outputEncoding());
        applySteps(entity, dto.steps());
        applyMappings(entity, dto.mappings());
    }

    private void applySteps(EndpointConfig entity, List<BackendStepDto> stepDtos) {
        List<BackendStep> steps = stepDtos.stream().map(s -> BackendStep.builder()
                .stepOrder(s.stepOrder())
                .name(s.name())
                .method(s.method())
                .urlPattern(s.urlPattern())
                .upstreamService(findUpstreamOrThrow(s.upstreamServiceId()))
                .forwardOriginalBody(s.forwardOriginalBody())
                .cacheEnabled(s.cacheEnabled())
                .cacheTtlSeconds(s.cacheTtlSeconds())
                .group(s.group())
                .target(s.target())
                .allowFields(s.allowFields() == null ? List.of() : s.allowFields())
                .denyFields(s.denyFields() == null ? List.of() : s.denyFields())
                .fieldRenameMapping(s.fieldRenameMapping() == null ? new java.util.HashMap<>() : s.fieldRenameMapping())
                .canvasX(s.canvasX())
                .canvasY(s.canvasY())
                .build()).toList();
        entity.replaceSteps(steps);
    }

    private void applyMappings(EndpointConfig entity, List<FieldMappingDto> mappingDtos) {
        List<FieldMapping> mappings = mappingDtos == null ? List.of() : mappingDtos.stream().map(m -> FieldMapping.builder()
                .sourceType(m.sourceType())
                .sourceStepOrder(m.sourceStepOrder())
                .sourceField(m.sourceField())
                .sourceArrayField(m.sourceArrayField())
                .sourceElementField(m.sourceElementField())
                .targetStepOrder(m.targetStepOrder())
                .targetType(m.targetType())
                .targetParamName(m.targetParamName())
                .mappingOrder(m.mappingOrder())
                .build()).toList();
        entity.replaceMappings(mappings);
    }

    private UpstreamService findUpstreamOrThrow(String upstreamServiceId) {
        return upstreamServiceRepository.findById(upstreamServiceId)
                .orElseThrow(() -> new BusinessException("GW-UP-404",
                        "Khong tim thay Upstream Service id=" + upstreamServiceId + " - hay dang ky truoc trong trang Upstream Services."));
    }

    public EndpointResponseDto toResponseDto(EndpointConfig entity) {
        List<BackendStepDto> steps = entity.getSteps().stream()
                .sorted((a, b) -> Integer.compare(a.getStepOrder(), b.getStepOrder()))
                .map(s -> new BackendStepDto(
                        s.getId(), s.getStepOrder(), s.getName(), s.getMethod(), s.getUrlPattern(),
                        s.getUpstreamService().getId(), s.getUpstreamService().getName(),
                        s.isForwardOriginalBody(), s.isCacheEnabled(), s.getCacheTtlSeconds(),
                        s.getGroup(), s.getTarget(),
                        // Cac @ElementCollection la LAZY - phai materialize (copy) ra collection
                        // Java thuong NGAY TRONG luc con transaction, neu khong DTO se giu tham
                        // chieu toi PersistentBag/PersistentMap con song cua Hibernate; khi Jackson
                        // serialize response SAU KHI @Transactional cua service da ket thuc (session
                        // da dong), no se nem LazyInitializationException.
                        new ArrayList<>(s.getAllowFields()), new ArrayList<>(s.getDenyFields()),
                        new HashMap<>(s.getFieldRenameMapping()),
                        s.getCanvasX(), s.getCanvasY()))
                .toList();

        List<FieldMappingDto> mappings = entity.getMappings().stream()
                .map(m -> new FieldMappingDto(
                        m.getId(), m.getSourceType(), m.getSourceStepOrder(), m.getSourceField(),
                        m.getSourceArrayField(), m.getSourceElementField(),
                        m.getTargetStepOrder(), m.getTargetType(), m.getTargetParamName(),
                        m.getMappingOrder()))
                .toList();

        return new EndpointResponseDto(
                entity.getId(), entity.getName(), entity.getDescription(), entity.getPath(),
                entity.getMethod(), entity.isSequential(), entity.getOutputEncoding(),
                steps, mappings, entity.getCreatedAt(), entity.getUpdatedAt());
    }

    /** So sanh path co doi khong (dung khi update de kiem tra trung path voi endpoint khac). */
    public boolean pathChanged(EndpointConfig entity, EndpointRequestDto dto) {
        return !Objects.equals(entity.getPath(), dto.path());
    }
}
