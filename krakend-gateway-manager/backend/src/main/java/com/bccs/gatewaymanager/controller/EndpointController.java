package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.dto.DependencyGraphDto;
import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.service.DependencyAnalyzer;
import com.bccs.gatewaymanager.service.EndpointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CRUD cho dinh nghia Endpoint Gateway. Luu = co hieu luc ngay (xem EndpointRegistryCache). */
@RestController
@RequestMapping("/api/endpoints")
@RequiredArgsConstructor
public class EndpointController {

    private final EndpointService endpointService;
    private final DependencyAnalyzer dependencyAnalyzer;

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
}
