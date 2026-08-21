package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.dto.DependencyGraphDto;
import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.PreviewResponseDto;
import com.bccs.gatewaymanager.entity.EndpointConfig;
import com.bccs.gatewaymanager.service.DependencyAnalyzer;
import com.bccs.gatewaymanager.service.EndpointMapper;
import com.bccs.gatewaymanager.service.EndpointService;
import com.bccs.gatewaymanager.service.GeneratedConfig;
import com.bccs.gatewaymanager.service.KrakendConfigGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD cho dinh nghia Endpoint Gateway + preview JSON cho 1 endpoint
 * (bao gom ca preview "nhap" khi dang tao moi, chua luu DB).
 */
@RestController
@RequestMapping("/api/endpoints")
@RequiredArgsConstructor
public class EndpointController {

    private final EndpointService endpointService;
    private final EndpointMapper endpointMapper;
    private final KrakendConfigGenerator generator;
    private final DependencyAnalyzer dependencyAnalyzer;

    @GetMapping
    public ResponseEntity<List<EndpointResponseDto>> list(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(endpointService.list(q));
    }

    /**
     * So do phu thuoc giua cac endpoint - suy ra tu cac BackendStep co host tro
     * nguoc ve chinh KrakenD (xem DependencyAnalyzer). Dung de ve dependency
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

    /**
     * Preview JSON cho 1 endpoint DA LUU trong DB.
     * Lay lai duoi dang DTO roi build entity tam (khong dung Hibernate lazy-loaded
     * collection truc tiep) de tai su dung dung 1 duong sinh JSON voi previewDraft().
     */
    @GetMapping("/{id}/preview")
    public ResponseEntity<PreviewResponseDto> previewSaved(@PathVariable String id) {
        var dto = endpointService.get(id);
        EndpointConfig transientEntity = endpointMapper.toTransientEntity(new EndpointRequestDto(
                dto.name(), dto.description(), dto.path(), dto.method(), dto.sequential(),
                dto.outputEncoding(), dto.steps(), dto.mappings()));
        GeneratedConfig generated = generator.previewOne(transientEntity);
        return ResponseEntity.ok(new PreviewResponseDto(generated.json(), generated.warnings()));
    }

    /**
     * Preview JSON cho endpoint CHUA LUU - dung khi nguoi dung dang go form tao moi
     * va muon xem truoc krakend.json se nhu the nao truoc khi bam Luu.
     */
    @PostMapping("/preview")
    public ResponseEntity<PreviewResponseDto> previewDraft(@Valid @RequestBody EndpointRequestDto dto) {
        EndpointConfig transientEntity = endpointMapper.toTransientEntity(dto);
        GeneratedConfig generated = generator.previewOne(transientEntity);
        return ResponseEntity.ok(new PreviewResponseDto(generated.json(), generated.warnings()));
    }
}
