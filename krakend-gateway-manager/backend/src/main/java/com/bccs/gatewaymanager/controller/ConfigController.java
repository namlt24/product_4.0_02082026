package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.dto.DeployResultDto;
import com.bccs.gatewaymanager.dto.GatewayInfoDto;
import com.bccs.gatewaymanager.dto.PreviewResponseDto;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.service.ConfigFileWriterService;
import com.bccs.gatewaymanager.service.DependencyAnalyzer;
import com.bccs.gatewaymanager.service.DockerReloadService;
import com.bccs.gatewaymanager.service.GeneratedConfig;
import com.bccs.gatewaymanager.service.KrakendConfigGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Preview toan bo krakend.json (gop tat ca endpoint dang luu) va thao tac Deploy:
 * ghi file + reload container KrakenD.
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final KrakendConfigGenerator generator;
    private final ConfigFileWriterService fileWriter;
    private final DockerReloadService dockerReloadService;
    private final DependencyAnalyzer dependencyAnalyzer;

    @Value("${gatewaymanager.krakend.port:8080}")
    private int krakendPort;

    @Value("#{'${gatewaymanager.krakend.self-host-aliases:localhost,127.0.0.1,krakend-gateway,krakend}'.split(',')}")
    private List<String> selfHostAliases;

    /** Xem truoc krakend.json day du se duoc sinh ra tu TAT CA endpoint dang luu. */
    @GetMapping("/preview")
    public ResponseEntity<PreviewResponseDto> previewFull() {
        GeneratedConfig generated = generator.generateFullConfig();
        List<String> warnings = mergeWithCycleWarnings(generated.warnings());
        return ResponseEntity.ok(new PreviewResponseDto(generated.json(), warnings));
    }

    /**
     * Deploy: sinh krakend.json tu DB -> ghi ra volume dung chung (atomic) ->
     * goi Docker Engine API restart container KrakenD.
     *
     * Warning thong thuong (vi du sequential=true nhung chi 1 step) KHONG chan
     * deploy - FE co trach nhiem hoi xac nhan lai nguoi dung. Rieng VONG LAP
     * phu thuoc (endpoint A goi nguoc endpoint B, B goi lai A...) THI CHAN
     * HAN, vi day khong phai rui ro-nguoi-dung-tu-quyet-dinh ma la config chac
     * chan hong (goi vo han lan / stack overflow khi chay that).
     */
    @PostMapping("/deploy")
    public ResponseEntity<DeployResultDto> deploy() {
        List<String> cycleWarnings = dependencyAnalyzer.detectCycleWarningsOnly();
        if (!cycleWarnings.isEmpty()) {
            throw new BusinessException("GW-CYCLE",
                    "Khong the Deploy: phat hien vong lap phu thuoc giua cac endpoint. "
                            + String.join(" | ", cycleWarnings));
        }

        GeneratedConfig generated = generator.generateFullConfig();
        fileWriter.write(generated.json());
        dockerReloadService.restartKrakend();
        log.info("Deploy krakend.json thanh cong, {} canh bao.", generated.warnings().size());
        return ResponseEntity.ok(new DeployResultDto(true, "Deploy thanh cong - KrakenD da duoc reload.", generated.warnings()));
    }

    /**
     * Thong tin ve chinh KrakenD gateway - FE dung de tu dong dien host khi
     * nguoi dung chon "Goi mot endpoint KrakenD khac" (Endpoint Picker) thay vi
     * phai go tay "http://localhost:8080".
     */
    @GetMapping("/gateway-info")
    public ResponseEntity<GatewayInfoDto> gatewayInfo() {
        String selfBaseUrl = "http://localhost:" + krakendPort;
        List<String> aliases = selfHostAliases.stream().map(String::trim).collect(Collectors.toList());
        return ResponseEntity.ok(new GatewayInfoDto(krakendPort, selfBaseUrl, aliases));
    }

    private List<String> mergeWithCycleWarnings(List<String> generatorWarnings) {
        List<String> cycleWarnings = dependencyAnalyzer.detectCycleWarningsOnly();
        if (cycleWarnings.isEmpty()) {
            return generatorWarnings;
        }
        List<String> merged = new ArrayList<>(cycleWarnings);
        merged.addAll(generatorWarnings);
        return merged;
    }
}
