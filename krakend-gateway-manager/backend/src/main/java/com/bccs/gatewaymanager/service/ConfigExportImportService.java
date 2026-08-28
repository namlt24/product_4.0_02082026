package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.ConfigExportDto;
import com.bccs.gatewaymanager.dto.ConfigImportResultDto;
import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.FieldMappingDto;
import com.bccs.gatewaymanager.dto.UpstreamServiceDto;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import com.bccs.gatewaymanager.repository.UpstreamServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Export/Import toan bo cau hinh (P1) - backup, review qua Pull Request, hoac
 * dong bo giua 2 moi truong (vi du dev -> staging).
 *
 * Import la UPSERT (khong bao gio XOA gi): Upstream khop theo TEN, Endpoint
 * khop theo PATH (2 field nay co rang buoc UNIQUE that trong DB - dung lam
 * khoa doi chieu dang tin cay hon id, vi id co the khac nhau giua 2 moi
 * truong khac nhau du cung 1 "endpoint logic"). Toan bo import chay trong 1
 * TRANSACTION DUY NHAT - 1 endpoint trong bundle bi loi (vi du sai du lieu)
 * se ROLLBACK TOAN BO import, khong ap dung nua-vơi (an toan hon cho 1 thao
 * tac "restore cau hinh" - tranh trang thai lung chung kho doan).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigExportImportService {

    private static final String SCHEMA_VERSION = "1.0";

    private final UpstreamServiceRepository upstreamRepository;
    private final UpstreamServiceService upstreamService;
    private final EndpointConfigRepository endpointRepository;
    private final EndpointService endpointService;
    private final EndpointMapper endpointMapper;

    @Transactional(readOnly = true)
    public ConfigExportDto export() {
        List<UpstreamServiceDto> upstreams = upstreamService.list();
        List<EndpointResponseDto> endpoints = endpointRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(endpointMapper::toResponseDto)
                .toList();
        return new ConfigExportDto(SCHEMA_VERSION, Instant.now(), upstreams, endpoints);
    }

    @Transactional
    public ConfigImportResultDto importConfig(ConfigExportDto bundle) {
        Map<String, String> upstreamNameToId = new HashMap<>();
        for (UpstreamServiceDto existing : upstreamService.list()) {
            upstreamNameToId.put(existing.name(), existing.id());
        }

        int upstreamsCreated = 0;
        int upstreamsUpdated = 0;
        for (UpstreamServiceDto u : bundle.upstreams()) {
            if (upstreamNameToId.containsKey(u.name())) {
                upstreamService.update(upstreamNameToId.get(u.name()), u);
                upstreamsUpdated++;
            } else {
                UpstreamServiceDto created = upstreamService.create(u);
                upstreamNameToId.put(created.name(), created.id());
                upstreamsCreated++;
            }
        }

        List<String> warnings = new ArrayList<>();
        int endpointsCreated = 0;
        int endpointsUpdated = 0;
        for (EndpointResponseDto ep : bundle.endpoints()) {
            EndpointRequestDto request = toRequest(ep, upstreamNameToId, warnings);
            var existing = endpointRepository.findByPath(ep.path());
            if (existing.isPresent()) {
                endpointService.update(existing.get().getId(), request);
                endpointsUpdated++;
            } else {
                endpointService.create(request);
                endpointsCreated++;
            }
        }

        log.info("Import cau hinh xong: {} upstream moi, {} upstream cap nhat, {} endpoint moi, {} endpoint cap nhat, {} canh bao.",
                upstreamsCreated, upstreamsUpdated, endpointsCreated, endpointsUpdated, warnings.size());
        return new ConfigImportResultDto(upstreamsCreated, upstreamsUpdated, endpointsCreated, endpointsUpdated, warnings);
    }

    /** Chuyen 1 snapshot EndpointResponseDto (tu bundle) thanh EndpointRequestDto de goi lai create()/update() - dung LAI toan bo validate/cycle-check co san. */
    private EndpointRequestDto toRequest(EndpointResponseDto ep, Map<String, String> upstreamNameToId, List<String> warnings) {
        List<BackendStepDto> steps = ep.steps().stream()
                .map(s -> {
                    String resolvedUpstreamId = upstreamNameToId.get(s.upstreamServiceName());
                    if (resolvedUpstreamId == null) {
                        warnings.add("Endpoint '" + ep.name() + "' - step '" + s.name() + "': khong tim thay Upstream ten '"
                                + s.upstreamServiceName() + "' trong bundle hoac trong DB hien tai, giu nguyen id cu (co the loi khi luu).");
                        resolvedUpstreamId = s.upstreamServiceId();
                    }
                    return new BackendStepDto(null, s.stepOrder(), s.name(), s.method(), s.urlPattern(),
                            resolvedUpstreamId, s.upstreamServiceName(), s.forwardOriginalBody(), s.cacheEnabled(),
                            s.cacheTtlSeconds(), s.group(), s.target(), s.allowFields(), s.denyFields(),
                            s.fieldRenameMapping(), s.canvasX(), s.canvasY(),
                            s.connectTimeoutMs(), s.readTimeoutMs(),
                            // Dieu kien re nhanh (P1-5) giu nguyen tu bundle - la du lieu cau hinh
                            // thuan tuy (tham chieu stepOrder trong CUNG endpoint), khong gan voi
                            // moi truong nguon nhu upstreamServiceId nen khong can resolve lai gi.
                            s.conditionSourceType(), s.conditionSourceStepOrder(), s.conditionSourceField(),
                            s.conditionOperator(), s.conditionExpectedValue(),
                            s.nextStepOrderIfTrue(), s.nextStepOrderIfFalse());
                })
                .toList();

        List<FieldMappingDto> mappings = ep.mappings().stream()
                .map(m -> new FieldMappingDto(null, m.sourceType(), m.sourceStepOrder(), m.sourceField(),
                        m.sourceArrayField(), m.sourceElementField(), m.constantValue(), m.targetStepOrder(), m.targetType(),
                        m.targetParamName(), m.mappingOrder()))
                .toList();

        return new EndpointRequestDto(ep.name(), ep.description(), ep.path(), ep.method(),
                ep.sequential(), ep.outputEncoding(), steps, mappings,
                ep.idempotencyEnabled(), ep.idempotencyTtlSeconds());
    }
}
