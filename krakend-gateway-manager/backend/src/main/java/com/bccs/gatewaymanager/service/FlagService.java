package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.config.CurrentTeamContext;
import com.bccs.gatewaymanager.entity.GwmFlag;
import com.bccs.gatewaymanager.entity.GwmFlagHistory;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import com.bccs.gatewaymanager.repository.GwmFlagHistoryRepository;
import com.bccs.gatewaymanager.repository.GwmFlagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Control-Plane-only - quan ly CỜ NGHIỆP VỤ cua doi (bang GWM_FLAG + lich su
 * GWM_FLAG_HISTORY - DDL V5). Doi tu quan ly bang team key qua API /api/flags;
 * gia tri hien tai (enabled) duoc DP sync qua /api/config/export (ConfigExportDto.flags).
 *
 * Cờ la doi tuong khai bao tren he thong - KHONG doc tu request client. Engine
 * (FlagRuleEvaluator/evaluateCondition SYSTEM_FLAG) doc qua FlagRegistryCache
 * (DP) hoac repository nay (CP - "Thu ngay").
 *
 * Multi-tenant: team code LUON lay tu CurrentTeamContext (ApiKeyAuthFilter da
 * xac thuc) - mirror AlertEmailRecipientService; KHONG nhan tu path/body.
 */
@Slf4j
@Service
@Profile("control-plane")
@RequiredArgsConstructor
public class FlagService {

    /** flag_code: chu hoa/so/gach duoi - de nhat quan trong Rule + dieu kien step (vd VIP, KYC_DONE). */
    private static final Pattern FLAG_CODE_PATTERN = Pattern.compile("^[A-Z0-9_]{2,50}$");

    /** Chan 1 doi khai qua nhieu cờ (khac AlertEmailRecipientService - cờ thuong it va on dinh hon email ca nhan). */
    static final int MAX_FLAGS_PER_TEAM = 100;

    /** Nguon thao tac ghi vao GWM_FLAG_HISTORY - mirror nguon trong entity. */
    public static final String SOURCE_CREATE = "CREATE";
    public static final String SOURCE_TOGGLE = "TOGGLE";
    public static final String SOURCE_UPDATE = "UPDATE";
    public static final String SOURCE_DELETE = "DELETE";

    private final GwmFlagRepository repository;
    private final GwmFlagHistoryRepository historyRepository;
    private final EndpointConfigRepository endpointRepository;
    private final FlagRegistryCache flagRegistryCache;

    /** Danh sach cờ cua doi hien tai. */
    public List<GwmFlag> list() {
        String teamCode = CurrentTeamContext.require();
        return repository.findByTeamCode(teamCode);
    }

    public GwmFlag create(String flagCode, String displayName, String description, boolean enabled) {
        String teamCode = CurrentTeamContext.require();
        String normalizedCode = normalizeFlagCode(flagCode);
        if (repository.existsByTeamCodeAndFlagCodeIgnoreCase(teamCode, normalizedCode)) {
            throw new BusinessException("GW-FLAG-DUPLICATE",
                    "Cờ '" + normalizedCode + "' da ton tai trong doi " + teamCode + ".");
        }
        if (repository.findByTeamCode(teamCode).size() >= MAX_FLAGS_PER_TEAM) {
            throw new BusinessException("GW-FLAG-MAX",
                    "Doi " + teamCode + " da khai toi da " + MAX_FLAGS_PER_TEAM + " cờ nghiệp vụ.");
        }
        GwmFlag saved = repository.save(GwmFlag.builder()
                .teamCode(teamCode)
                .flagCode(normalizedCode)
                .displayName(displayName == null || displayName.isBlank() ? normalizedCode : displayName.trim())
                .description(description)
                .enabled(enabled)
                .build());
        recordHistory(saved, null, saved.isEnabled(), SOURCE_CREATE);
        reloadCache(teamCode);
        log.info("Doi {} tao cờ nghiệp vụ {} (enabled={})", teamCode, normalizedCode, enabled);
        return saved;
    }

    public GwmFlag update(String id, String displayName, String description) {
        GwmFlag flag = requireOwnedFlag(id);
        if (displayName != null) {
            flag.setDisplayName(displayName.trim());
        }
        if (description != null) {
            flag.setDescription(description);
        }
        GwmFlag saved = repository.save(flag);
        reloadCache(flag.getTeamCode());
        log.info("Doi {} cap nhat thong tin cờ {}", flag.getTeamCode(), flag.getFlagCode());
        return saved;
    }

    /**
     * Toggle gia tri cờ - THAO TAC CHINH khi chay that (doi luong theo cờ).
     * Ghi history ngay cung transaction (rollback toggle = rollback history).
     */
    @Transactional
    public GwmFlag toggle(String id) {
        GwmFlag flag = requireOwnedFlag(id);
        boolean oldValue = flag.isEnabled();
        flag.setEnabled(!oldValue);
        GwmFlag saved = repository.save(flag);
        recordHistory(saved, oldValue, saved.isEnabled(), SOURCE_TOGGLE);
        reloadCache(flag.getTeamCode());
        log.info("Doi {} toggle cờ {} : {} -> {}", flag.getTeamCode(), flag.getFlagCode(), oldValue, saved.isEnabled());
        return saved;
    }

    public void delete(String id) {
        GwmFlag flag = requireOwnedFlag(id);
        // Chan xoa cờ dang duoc tham chieu boi Rule/dieu kien step cua endpoint nao do -
        // neu khong, endpoint se tiep tuc tham chieu cờ mat (engine fail-safe coi la false,
        // nhung do BA khong the biet khi cau hinh). Bao loi ro rang de user go tham chieu truoc.
        // Doc day trong Java (KHONG JPQL LIKE tren CLOB - Oracle ORA-00932, su co image 31).
        String marker = "\"" + flag.getFlagCode() + "\"";
        long referenced = endpointRepository.findAllByTeamCodeOrderByUpdatedAtDesc(flag.getTeamCode()).stream()
                .filter(e -> e.getFlagRulesJson() != null && e.getFlagRulesJson().contains(marker))
                .count();
        if (referenced > 0) {
            throw new BusinessException("GW-FLAG-IN-USE",
                    "Cờ '" + flag.getFlagCode() + "' dang duoc tham chieu boi " + referenced
                            + " endpoint - hay xoa/doi tham chieu trong endpoint truoc khi xoa cờ.");
        }
        recordHistory(GwmFlag.builder()
                        .id(flag.getId()).teamCode(flag.getTeamCode()).flagCode(flag.getFlagCode())
                        .enabled(flag.isEnabled()).build(),
                flag.isEnabled(), flag.isEnabled(), SOURCE_DELETE);
        repository.delete(flag);
        reloadCache(flag.getTeamCode());
        log.info("Doi {} xoa cờ {}", flag.getTeamCode(), flag.getFlagCode());
    }

    /** Lich su 1 cờ (moi nhat truoc). */
    public List<GwmFlagHistory> history(String flagId) {
        requireOwnedFlag(flagId);
        return historyRepository.findByFlagIdOrderByChangedAtDesc(flagId);
    }

    /** Cờ phai ton tai VA thuoc doi hien tai - KHONG cho doi thao tac cờ cua doi khac qua id. */
    private GwmFlag requireOwnedFlag(String id) {
        String teamCode = CurrentTeamContext.require();
        return repository.findById(id)
                .filter(f -> f.getTeamCode().equals(teamCode))
                .orElseThrow(() -> new BusinessException("GW-FLAG-NOT-FOUND",
                        "Khong tim thay cờ id=" + id + " thuoc doi " + teamCode + "."));
    }

    /**
     * Nap lai FlagRegistryCache (dung chung CP) ngay sau moi thao tac ghi -
     * mirror mo hinh EndpointService -> EndpointRegistryCache.reload() sau CRUD:
     * "Thu ngay"/"Thu nhanh" tren CP doc gia tri cờ MOI ngay tai request ke
     * tiep, khong phai doi sync nao (CP cung khong co sync). Reload doc TOAN BO
     * cờ cua doi (query lai) thay vi sua 1 entry - don gian va luon nhat quan.
     */
    private void reloadCache(String teamCode) {
        flagRegistryCache.reload(repository.findByTeamCode(teamCode).stream()
                .map(f -> new com.bccs.gatewaymanager.dto.FlagDto(f.getId(), f.getTeamCode(), f.getFlagCode(),
                        f.getDisplayName(), f.getDescription(), f.isEnabled(), f.getCreatedAt(), f.getUpdatedAt()))
                .toList());
    }

    private void recordHistory(GwmFlag flag, Boolean oldValue, boolean newValue, String source) {
        try {
            historyRepository.save(GwmFlagHistory.builder()
                    .flagId(flag.getId())
                    .teamCode(flag.getTeamCode())
                    .flagCode(flag.getFlagCode())
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .source(source)
                    .build());
        } catch (RuntimeException e) {
            // Fail-open: lich su KHONG BAO GIO lam hong thao tac cờ chinh (mirror fail-open AuditLogService).
            log.warn("Khong ghi duoc lich su cờ {}: {}", flag.getFlagCode(), e.getMessage());
        }
    }

    /** flag_code chuan hoa: trim + UPPER; sai dinh dang (chu cai/so/gach duoi, 2-50 ky tu) bao loi ro rang. */
    static String normalizeFlagCode(String flagCode) {
        String normalized = flagCode == null ? "" : flagCode.trim().toUpperCase(Locale.ROOT);
        if (!FLAG_CODE_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException("GW-FLAG-CODE-INVALID",
                    "Ma cờ '" + flagCode + "' khong dung dinh dang - chi chu HOA/so/gach duoi, do dai 2-50 (vd: VIP, KYC_DONE).");
        }
        return normalized;
    }
}
