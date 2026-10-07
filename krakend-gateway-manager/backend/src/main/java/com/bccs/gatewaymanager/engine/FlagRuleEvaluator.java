package com.bccs.gatewaymanager.engine;

import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.FlagRuleDto;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.service.FlagRegistryCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Danh gia RULE CỜ (V5) cua 1 endpoint NGAY DAU 1 request - truoc khi bat dau
 * bat ky step nao. Input = flagRules cua EndpointResponseDto (da sync kem
 * endpoint) + gia tri hien tai cua cờ (FlagRegistryCache, sync tu CP cung chu
 * ky 15s - xem RemoteConfigSyncService).
 *
 * NGU PHAP KHOP: rule khop neu CO IT NHAT 1 nhom trong conditions ma TAT CA
 * dieu kien trong nhom do dung (trong nhom AND, giua nhom OR):
 *   (A=true ∧ B=false) ∨ (C=true).
 * Duyet theo priority TANG DAN, khop rule dau tien la DUNG - tra ve ket qua:
 *  - START_STEP -> initialStepOrder cho engine (chuoi bat dau tu step do);
 *  - BLOCK -> throw BusinessException(blockErrorCode, blockMessage) NGAY - loi
 *    di qua dung luong error/audit co san cua DynamicDispatcherController,
 *    KHONG goi upstream nao.
 * KHONG rule khop (hoac endpoint khong khai rule) -> ket qua rong = hanh vi
 * cu (chay tu step nho nhat) - 100% endpoint hien co khong doi.
 *
 * FAIL-SAFE (triet ly giong RemoteConfigSyncService): cờ khong ton tai trong
 * cache (chua sync/da xoa goi sau) COI LA false + log.warn 1 lan moi request
 * - KHONG chan traffic, KHONG throw. Flag mismatch giua rule va cờ da xoa
 * chi lam rule khong khop (hoac khop theo gia tri false), khong lam hong
 * request.
 */
@Slf4j
@Component
public class FlagRuleEvaluator {

    private final FlagRegistryCache flagRegistryCache;

    public FlagRuleEvaluator(FlagRegistryCache flagRegistryCache) {
        this.flagRegistryCache = flagRegistryCache;
    }

    /**
     * Ket qua danh gia rule - null khong xay ra, khong khop tra ket qua rong
     * (matchedRule=null, initialStepOrder=null) = hanh vi cu.
     */
    public record Result(FlagRuleDto matchedRule, Integer initialStepOrder) {
        public static Result none() {
            return new Result(null, null);
        }
    }

    /**
     * @param flagOverrides gia tri cờ GHÉ ĐÈ (chi cho "Thu ngay"/"Thu nhanh" -
     *                      EndpointTryService truyen tu try-panel; request that
     *                      luon null = doc dung gia tri sync tu cache). Key la
     *                      flagCode, value la gia tri mo phong.
     */
    public Result evaluate(EndpointResponseDto config, Map<String, Boolean> flagOverrides) {
        List<FlagRuleDto> rules = config.flagRules();
        if (rules == null || rules.isEmpty()) {
            return Result.none();
        }
        for (FlagRuleDto rule : rules.stream().sorted(java.util.Comparator.comparingInt(FlagRuleDto::priority)).toList()) {
            if (matches(rule, flagOverrides)) {
                if (FlagRuleDto.ACTION_BLOCK.equals(rule.actionType())) {
                    log.info("Endpoint '{}': Rule cờ '{}' (priority {}) khop -> CHAN request ({})",
                            config.name(), rule.name(), rule.priority(), rule.blockErrorCode());
                    throw new BusinessException(rule.blockErrorCode(),
                            rule.blockMessage() == null || rule.blockMessage().isBlank()
                                    ? "Request bi chan boi Rule cờ '" + rule.name() + "'."
                                    : rule.blockMessage());
                }
                // START_STEP (duy nhat con lai - validate luc luu chi cho 2 loai)
                log.info("Endpoint '{}': Rule cờ '{}' (priority {}) khop -> bat dau tu step {}",
                        config.name(), rule.name(), rule.priority(), rule.startStepOrder());
                return new Result(rule, rule.startStepOrder());
            }
        }
        return Result.none();
    }

    /** Rule khop neu ton tai it nhat 1 nhom ma moi dieu kien deu dung (AND trong nhom, OR giua nhom). */
    private boolean matches(FlagRuleDto rule, Map<String, Boolean> flagOverrides) {
        for (List<FlagRuleDto.FlagConditionDto> group : rule.conditions()) {
            boolean allTrue = true;
            for (FlagRuleDto.FlagConditionDto cond : group) {
                if (currentFlagValue(cond.flagCode(), flagOverrides) != cond.expected()) {
                    allTrue = false;
                    break;
                }
            }
            if (allTrue) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gia tri hien tai cua 1 cờ: override (try-panel) uu tien; khong co thi doc
     * FlagRegistryCache. Khong tim thay trong cache -> false + log.warn (fail-safe
     * - endpoint van tham chieu 1 cờ da mat; validate luc luu da chan khoi tao
     * cau hinh nhu vay, day la truong hop cờ BI XOA SAU khi endpoint da luu).
     */
    private boolean currentFlagValue(String flagCode, Map<String, Boolean> flagOverrides) {
        if (flagOverrides != null && flagOverrides.containsKey(flagCode)) {
            return flagOverrides.get(flagCode);
        }
        if (!flagRegistryCache.exists(flagCode)) {
            log.warn("Cờ '{}' khong ton tai trong cache (chua sync/da bi xoa) - coi gia tri la FALSE (fail-safe).", flagCode);
        }
        return flagRegistryCache.get(flagCode);
    }
}
