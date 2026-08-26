package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.dto.DependencyGraphDto;
import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.EndpointTryRequestDto;
import com.bccs.gatewaymanager.dto.EndpointVersionSummaryDto;
import com.bccs.gatewaymanager.service.DependencyAnalyzer;
import com.bccs.gatewaymanager.service.EndpointService;
import com.bccs.gatewaymanager.service.EndpointTryService;
import com.bccs.gatewaymanager.service.EndpointVersionService;
import com.bccs.gatewaymanager.service.OpenApiGeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

/** CRUD cho dinh nghia Endpoint Gateway. Luu = co hieu luc ngay (xem EndpointRegistryCache). */
@RestController
@RequestMapping("/api/endpoints")
@RequiredArgsConstructor
public class EndpointController {

    private final EndpointService endpointService;
    private final DependencyAnalyzer dependencyAnalyzer;
    private final EndpointVersionService versionService;
    private final EndpointTryService tryService;
    private final OpenApiGeneratorService openApiGeneratorService;

    @GetMapping
    public ResponseEntity<List<EndpointResponseDto>> list(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(endpointService.list(q));
    }

    /**
     * So do phu thuoc giua cac endpoint - suy ra tu cac BackendStep co Upstream tro
     * nguoc ve chinh gateway nay (xem DependencyAnalyzer). Dung de ve dependency
     * graph, hien badge "duoc dung boi N endpoint", va canh bao vong lap.
     */
    @GetMapping("/dependency-graph")
    public ResponseEntity<DependencyGraphDto> dependencyGraph() {
        return ResponseEntity.ok(dependencyAnalyzer.buildGraph());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EndpointResponseDto> get(@PathVariable String id) {
        return ResponseEntity.ok(endpointService.get(id));
    }

    @PostMapping
    public ResponseEntity<EndpointResponseDto> create(@Valid @RequestBody EndpointRequestDto dto) {
        return ResponseEntity.ok(endpointService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EndpointResponseDto> update(@PathVariable String id, @Valid @RequestBody EndpointRequestDto dto) {
        return ResponseEntity.ok(endpointService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        endpointService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Lich su phien ban (xem EndpointVersionService) ----

    @GetMapping("/{id}/versions")
    public ResponseEntity<List<EndpointVersionSummaryDto>> listVersions(@PathVariable String id) {
        return ResponseEntity.ok(versionService.listVersions(id));
    }

    /** Xem truoc noi dung 1 phien ban cu (chua ap dung gi) - dung truoc khi bam Khoi phuc. */
    @GetMapping("/{id}/versions/{versionId}")
    public ResponseEntity<EndpointResponseDto> getVersion(@PathVariable String id, @PathVariable String versionId) {
        return ResponseEntity.ok(versionService.getVersionDetail(id, versionId));
    }

    /** Khoi phuc endpoint id ve noi dung cua versionId - chay qua dung validate/cycle-check nhu sua tay. */
    @PostMapping("/{id}/versions/{versionId}/rollback")
    public ResponseEntity<EndpointResponseDto> rollback(@PathVariable String id, @PathVariable String versionId) {
        return ResponseEntity.ok(endpointService.rollback(id, versionId));
    }

    /**
     * "Thu ngay" (P1) - goi endpoint composite that qua Control Plane (khong qua
     * Data Plane nen khong bi RateLimitFilter/CORS), dung dung
     * CompositeOrchestratorEngine nhu client that se trai qua. Loi (business/upstream/
     * circuit-breaker...) duoc de GlobalExceptionHandler xu ly binh thuong.
     */
    @PostMapping("/{id}/try")
    public ResponseEntity<JsonNode> tryEndpoint(@PathVariable String id, @RequestBody EndpointTryRequestDto req) {
        JsonNode result = tryService.tryCall(id, req.pathVariables(), req.queryParams(), req.body());
        return ResponseEntity.ok(result);
    }

    /** Tu sinh tai lieu OpenAPI 3.0.3 (JSON) cho endpoint nay - xem OpenApiGeneratorService cho gioi han "best-effort". */
    @GetMapping("/{id}/openapi")
    public ResponseEntity<Map<String, Object>> openApi(@PathVariable String id) {
        return ResponseEntity.ok(openApiGeneratorService.generate(endpointService.get(id)));
    }
}
