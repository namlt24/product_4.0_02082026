package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.dto.UpstreamHealthDto;
import com.bccs.gatewaymanager.dto.UpstreamServiceDto;
import com.bccs.gatewaymanager.service.UpstreamHealthService;
import com.bccs.gatewaymanager.service.UpstreamServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CRUD dang ky Upstream Service (backend that) - dung chung cho nhieu Endpoint/BackendStep. */
@RestController
@RequestMapping("/api/upstreams")
@RequiredArgsConstructor
public class UpstreamServiceController {

    private final UpstreamServiceService service;
    private final UpstreamHealthService healthService;

    @GetMapping
    public ResponseEntity<List<UpstreamServiceDto>> list() {
        return ResponseEntity.ok(service.list());
    }

    /** Dashboard suc khoe: trang thai circuit breaker + hit-rate cache Redis cua tung Upstream. */
    @GetMapping("/health")
    public ResponseEntity<List<UpstreamHealthDto>> health() {
        return ResponseEntity.ok(healthService.healthSnapshot());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UpstreamServiceDto> get(@PathVariable String id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<UpstreamServiceDto> create(@Valid @RequestBody UpstreamServiceDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpstreamServiceDto> update(@PathVariable String id, @Valid @RequestBody UpstreamServiceDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
