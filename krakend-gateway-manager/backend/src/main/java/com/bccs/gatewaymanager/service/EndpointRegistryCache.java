package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache dinh tuyen trong-process: giu toan bo EndpointConfig (da map san
 * thanh DTO) trong bo nho, de DynamicDispatcherController tra cuu (method,
 * path) cho MOI request that - khong query DB/goi API tren duong di nong
 * (hot path) cua traffic.
 *
 * TU 2026-09 (tach Control Plane dung chung / Data Plane rieng tung doi):
 * class nay KHONG con tu doc JPA nua - reload(List) nhan du lieu da san
 * sang tu BEN NGOAI, nguon du lieu khac nhau tuy profile dang chay:
 * - profile "control-plane": EndpointService goi reload() voi ket qua tu
 *   EndpointConfigRepository.findAll() (JPA truc tiep, TAT CA doi - an toan
 *   vi class nay chi phuc vu "Thu ngay"/"Thu nhanh", noi quyen so huu Upstream
 *   da duoc kiem tra RIENG truoc do, xem EndpointMapper/EndpointTryService).
 * - profile "data-plane": RemoteConfigSyncService goi reload() voi ket qua
 *   tu goi HTTP toi Control Plane (GET /api/config/export bang api_key CUA
 *   CHINH DOI MINH - tu dong chi tra ve config cua doi do, KHONG can loc gi
 *   them o day).
 * Nho vay class nay khong con phu thuoc EndpointConfigRepository/EndpointMapper/
 * TransactionTemplate nua - chi la 1 bo nho dem thuan tuy, dung duoc trong ca
 * 2 profile ma khong can @Profile rieng.
 */
@Slf4j
@Component
public class EndpointRegistryCache {

    private volatile List<EndpointResponseDto> compiled = List.of();

    // Router 2 tang: da so endpoint thuc te la path "tinh" (khong co {param}) -
    // tra O(1) qua HashMap thay vi quet tuyen tinh toan bo danh sach nhu truoc
    // (dispatch() cu goi all() roi lap tung phan tu, O(n) moi request). Chi con
    // nhom co {param} (thuong it hon han) moi phai roi qua PathPattern.matches()
    // tuan tu - xem DynamicDispatcherController.dispatch().
    private volatile Map<String, EndpointResponseDto> exactIndex = Map.of();
    private volatile List<EndpointResponseDto> patternEndpoints = List.of();

    /** Thay toan bo noi dung cache bang danh sach da fetch san (khong tu query gi ca) - xem javadoc class ve nguon du lieu tuy profile. */
    public synchronized void reload(List<EndpointResponseDto> fresh) {
        this.compiled = fresh;

        Map<String, EndpointResponseDto> exact = new HashMap<>();
        List<EndpointResponseDto> patterns = new ArrayList<>();
        for (EndpointResponseDto ep : fresh) {
            if (hasPathVariable(ep.path())) {
                patterns.add(ep);
            } else {
                exact.put(exactKey(ep.method().name(), ep.path()), ep);
            }
        }
        this.exactIndex = Map.copyOf(exact);
        this.patternEndpoints = List.copyOf(patterns);

        log.info("Da nap lai {} endpoint vao cache dinh tuyen trong-process ({} path tinh - O(1), {} path co {{param}} - quet tuan tu).",
                fresh.size(), exact.size(), patterns.size());
    }

    public List<EndpointResponseDto> all() {
        return compiled;
    }

    /** Tra O(1) cho path "tinh" (khong co {param}) - da so endpoint thuc te. Null neu khong khop hoac path co {param}. */
    public EndpointResponseDto findExact(String method, String path) {
        return exactIndex.get(exactKey(method, path));
    }

    /** Chi nhom endpoint co {param} trong path - can PathPattern.matches() quet tuan tu (danh sach nay nho hon han "all()"). */
    public List<EndpointResponseDto> patternEndpoints() {
        return patternEndpoints;
    }

    private static boolean hasPathVariable(String path) {
        return path.indexOf('{') >= 0;
    }

    private static String exactKey(String method, String path) {
        return method + " " + path;
    }
}
