package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.entity.UpstreamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache trong-process cho UpstreamService (khoa theo id) - dung boi
 * CompositeOrchestratorEngine tren duong di nong (hot path) cua traffic THAT
 * (Data Plane) LAN "Thu ngay"/"Thu nhanh" (Control Plane, cung dung chung 1
 * engine - xem EndpointTryService).
 *
 * TU 2026-09 (tach Control Plane dung chung / Data Plane rieng tung doi):
 * class nay KHONG con tu doc JPA nua - reload(List) nhan du lieu da san sang
 * tu BEN NGOAI, giong het EndpointRegistryCache (xem javadoc class do de biet
 * nguon du lieu khac nhau tuy profile: control-plane doc JPA truc tiep TAT
 * CA doi, data-plane fetch qua HTTP CHI doi cua chinh minh).
 */
@Slf4j
@Component
public class UpstreamRegistryCache {

    private volatile Map<String, UpstreamService> byId = Map.of();

    /** Thay toan bo noi dung cache bang danh sach da fetch san (khong tu query gi ca) - xem javadoc class ve nguon du lieu tuy profile. */
    public synchronized void reload(List<UpstreamService> fresh) {
        Map<String, UpstreamService> map = new ConcurrentHashMap<>();
        fresh.forEach(u -> map.put(u.getId(), u));
        this.byId = Map.copyOf(map);
        log.info("Da nap lai {} Upstream Service vao cache trong-process.", fresh.size());
    }

    public UpstreamService getById(String id) {
        return byId.get(id);
    }
}
