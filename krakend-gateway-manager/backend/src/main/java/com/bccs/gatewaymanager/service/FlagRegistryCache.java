package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.FlagDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache gia tri cờ trong-process (mirror EndpointRegistryCache/UpstreamRegistryCache):
 * KHONG tu doc JPA nua - reload(List) nhan du lieu san tu ben ngoai tuy profile:
 * - profile "control-plane": FlagService goi reload() sau moi create/update/toggle/
 *   delete cờ ("Thu ngay"/"Thu nhanh" tren CP doc gia tri MOI ngay, khong cho sync).
 * - profile "data-plane": RemoteConfigSyncService goi reload() voi bundle.flags()
 *   tu /api/config/export (sync dinh ky 15s, ETag/304 - xem RemoteConfigSyncService).
 *
 * Key la flagCode (KHONG phai id) vi Rule/dieu kien step tham chieu cờ qua MA CỜ
 * (flagCode nhan lo de doc cho BA - "VIP" thay vi UUID). Gia tri duy nhat boolean
 * enabled: cờ KHONG ton tai / cache rong / chua sync -> get() tra FALSE (fail-safe,
 * khong bao gio chan traffic chi vi dong bo lag - giong triet ly fail-open cua
 * RemoteConfigSyncService). FlagRuleEvaluator log.warn rieng khi khong tim thay de
 * de bao (khong bam im gia tri false gay hieu sai luong).
 */
@Slf4j
@Component
public class FlagRegistryCache {

    private volatile Map<String, Boolean> flagsByCode = Map.of();

    /** Thay toan bo cache bang danh sach cờ da fetch san (khong tu query gi ca) - xem javadoc class ve nguon du lieu tuy profile. */
    public synchronized void reload(List<FlagDto> fresh) {
        Map<String, Boolean> map = new HashMap<>();
        for (FlagDto f : fresh) {
            map.put(f.flagCode(), f.enabled());
        }
        this.flagsByCode = Map.copyOf(map);
        long on = map.values().stream().filter(Boolean::booleanValue).count();
        log.info("Da nap {} cờ nghiệp vụ vao cache trong-process ({} BAT, {} TAT).", map.size(), on, map.size() - on);
    }

    /**
     * Gia tri hien tai cua cờ theo ma (KHONG phan biet hoa/thuong - flag_code
     * da duoc chuan hoa UPPER khi tao, nhung du phong cho du lieu cu).
     * Khong tim thay -> false (fail-safe) - caller (FlagRuleEvaluator) phai
     * log.warn de BA biet cờ dang duoc tham chieu nhung mat gia tri.
     */
    public boolean get(String flagCode) {
        if (flagCode == null) {
            return false;
        }
        return flagsByCode.getOrDefault(flagCode.toUpperCase(java.util.Locale.ROOT), false);
    }

    /** Cờ co ton tai trong cache khong (dung de log.warn chinh xac: "khong tim thay" thay vi "gia tri false"). */
    public boolean exists(String flagCode) {
        if (flagCode == null) {
            return false;
        }
        return flagsByCode.containsKey(flagCode.toUpperCase(java.util.Locale.ROOT));
    }
}
