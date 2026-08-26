package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.dto.UpstreamServiceDto;
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

    @GetMapping
    public ResponseEntity<List<UpstreamServiceDto>> list() {
        return ResponseEntity.ok(service.list());
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
