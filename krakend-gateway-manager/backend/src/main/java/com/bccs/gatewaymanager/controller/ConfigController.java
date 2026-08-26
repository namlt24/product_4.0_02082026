package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.dto.ConfigExportDto;
import com.bccs.gatewaymanager.dto.ConfigImportResultDto;
import com.bccs.gatewaymanager.dto.DeployResultDto;
import com.bccs.gatewaymanager.dto.GatewayInfoDto;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.service.ConfigExportImportService;
import com.bccs.gatewaymanager.service.DependencyAnalyzer;
import com.bccs.gatewaymanager.service.EndpointRegistryCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Validate cau hinh (canh bao vong lap phu thuoc) va nap lai cache dinh tuyen
 * trong-process theo yeu cau. Khac voi truoc day (khi con dung KrakenD/Gravitee
 * lam runtime rieng biet): moi thay doi qua EndpointController/UpstreamServiceController
 * DA TU DONG co hieu luc ngay (xem EndpointRegistryCache/UpstreamRegistryCache) -
 * endpoint "/deploy" o day chi con la buoc validate + nap-lai-thu-cong tuy chon,
 * khong con y nghia "ghi file + restart container" nhu truoc.
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final DependencyAnalyzer dependencyAnalyzer;
    private final EndpointRegistryCache registryCache;
    private final ConfigExportImportService exportImportService;

    @Value("${server.port}")
    private int gatewayPort;

    @Value("#{'${gatewaymanager.gateway.self-host-aliases:localhost,127.0.0.1}'.split(',')}")
    private List<String> selfHostAliases;

    /**
     * Validate toan bo cau hinh dang luu (hien tai chi kiem tra vong lap phu
     * thuoc giua cac endpoint) roi nap lai cache dinh tuyen trong-process.
     * VONG LAP phu thuoc (endpoint A goi nguoc endpoint B, B goi lai A...) BI
     * CHAN HAN, vi day khong phai rui ro-nguoi-dung-tu-quyet-dinh ma la config
     * chac chan hong (goi vo han lan) khi chay that.
     */
    @PostMapping("/deploy")
    public ResponseEntity<DeployResultDto> deploy() {
        List<String> cycleWarnings = dependencyAnalyzer.detectCycleWarningsOnly();
        if (!cycleWarnings.isEmpty()) {
            throw new BusinessException("GW-CYCLE",
                    "Phat hien vong lap phu thuoc giua cac endpoint. " + String.join(" | ", cycleWarnings));
        }
        registryCache.reload();
        log.info("Da validate + nap lai cache dinh tuyen - {} endpoint dang hoat dong.", registryCache.all().size());
        return ResponseEntity.ok(new DeployResultDto(true, "Validate thanh cong - cau hinh da co hieu luc.", List.of()));
    }

    /**
     * Thong tin ve chinh gateway nay - FE dung de tu dong dien host khi nguoi
     * dung chon "Goi mot endpoint gateway khac" (Endpoint Picker) thay vi phai
     * go tay host.
     */
    @GetMapping("/gateway-info")
    public ResponseEntity<GatewayInfoDto> gatewayInfo() {
        String selfBaseUrl = "http://localhost:" + gatewayPort;
        List<String> aliases = selfHostAliases.stream().map(String::trim).collect(Collectors.toList());
        return ResponseEntity.ok(new GatewayInfoDto(gatewayPort, selfBaseUrl, aliases));
    }

    /** Xuat toan bo cau hinh (Upstream + Endpoint) - dung backup hoac review qua Pull Request. */
    @GetMapping("/export")
    public ResponseEntity<ConfigExportDto> export() {
        return ResponseEntity.ok(exportImportService.export());
    }

    /** Nap lai 1 bundle da xuat - UPSERT (khop Upstream theo ten, Endpoint theo path), khong bao gio xoa gi. */
    @PostMapping("/import")
    public ResponseEntity<ConfigImportResultDto> importConfig(@RequestBody ConfigExportDto bundle) {
        return ResponseEntity.ok(exportImportService.importConfig(bundle));
    }
}
