package com.bccs.gatewaymanager.controller;

import com.bccs.gatewaymanager.dto.ConfigExportDto;
import com.bccs.gatewaymanager.dto.ConfigImportResultDto;
import com.bccs.gatewaymanager.dto.DeployResultDto;
import com.bccs.gatewaymanager.dto.GatewayInfoDto;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.service.ConfigExportImportService;
import com.bccs.gatewaymanager.service.DependencyAnalyzer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Control-Plane-only (@Profile) - validate cau hinh (canh bao vong lap phu
 * thuoc). TU 2026-09 (tach Control Plane dung chung/Data Plane rieng tung
 * doi): endpoint "/deploy" KHONG con nap lai cache dinh tuyen nao ca -
 * EndpointRegistryCache/UpstreamRegistryCache phuc vu traffic that gio thuoc
 * ve Data Plane RIENG (tu dong bo qua RemoteConfigSyncService, doc dinh ky,
 * xem SAD ADR-07), Control Plane nay khong con giu cache do nua. "/deploy" o
 * day gio CHI con y nghia validate (chan luu cau hinh gay vong lap) - khong
 * con lam gi khac.
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@Profile("control-plane")
@RequiredArgsConstructor
public class ConfigController {

    private final DependencyAnalyzer dependencyAnalyzer;
    private final ConfigExportImportService exportImportService;

    @Value("${server.port}")
    private int gatewayPort;

    @Value("#{'${gatewaymanager.gateway.self-host-aliases:localhost,127.0.0.1}'.split(',')}")
    private List<String> selfHostAliases;

    /**
     * Validate toan bo cau hinh dang luu (hien tai chi kiem tra vong lap phu
     * thuoc giua cac endpoint). VONG LAP phu thuoc (endpoint A goi nguoc
     * endpoint B, B goi lai A...) BI CHAN HAN, vi day khong phai rui
     * ro-nguoi-dung-tu-quyet-dinh ma la config chac chan hong (goi vo han
     * lan) khi chay that - dieu nay da duoc chan ngay luc Luu (xem
     * EndpointService.rejectIfCyclic()), "/deploy" o day chi la 1 cong cu
     * kiem tra thu cong tuy chon.
     */
    @PostMapping("/deploy")
    public ResponseEntity<DeployResultDto> deploy() {
        List<String> cycleWarnings = dependencyAnalyzer.detectCycleWarningsOnly();
        if (!cycleWarnings.isEmpty()) {
            throw new BusinessException("GW-CYCLE",
                    "Phat hien vong lap phu thuoc giua cac endpoint. " + String.join(" | ", cycleWarnings));
        }
        return ResponseEntity.ok(new DeployResultDto(true, "Validate thanh cong - khong phat hien vong lap phu thuoc.", List.of()));
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
