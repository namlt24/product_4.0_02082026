package com.bccs.gatewaymanager.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Rate limit "fixed window counter" qua Redis cho Data Plane (traffic that qua
 * DynamicDispatcherController) - bao ve cac Upstream Service THAT phia sau
 * khoi 1 client goi loi vong lap hoac spam co tinh. Control Plane (/api/**)
 * KHONG bi gioi han o day (da co API key chan nguoi la - xem ApiKeyAuthFilter).
 *
 * Thuat toan: 1 key Redis "gwm:ratelimit:{clientKey}" duoc INCR moi request;
 * lan INCR dau tien (ket qua = 1) set TTL = window-seconds. Neu count vuot
 * max-requests trong 1 window con hieu luc -> tu choi. Dung INCR (atomic o
 * Redis) nen khong co race-condition giua nhieu request/thread cung luc, va
 * TTL cua Redis tu don dep key cu - khong can vong lap don dep thu cong nhu
 * cach lam voi 1 ConcurrentHashMap trong-process (se ro ri bo nho neu khong
 * co co che het han).
 *
 * Fail-open dung y het GatewayCacheService: Redis loi tam thoi KHONG duoc lam
 * sap toan bo Data Plane (mat rate-limit tam thoi con chap nhan duoc hon la
 * tu choi het moi request that vi 1 dependency phu bi loi).
 */
@Slf4j
@Service
public class RateLimitService {

    private static final String KEY_PREFIX = "gwm:ratelimit:";

    private final StringRedisTemplate redisTemplate;
    private final boolean enabled;
    private final int windowSeconds;
    private final int maxRequests;

    public RateLimitService(
            StringRedisTemplate redisTemplate,
            @Value("${gatewaymanager.rate-limit.enabled:true}") boolean enabled,
            @Value("${gatewaymanager.rate-limit.window-seconds:60}") int windowSeconds,
            @Value("${gatewaymanager.rate-limit.max-requests:300}") int maxRequests) {
        this.redisTemplate = redisTemplate;
        this.enabled = enabled;
        this.windowSeconds = windowSeconds;
        this.maxRequests = maxRequests;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Ket qua 1 lan kiem tra: co cho phep khong, va con bao nhieu giay nua window reset (dung cho header Retry-After). */
    public record Decision(boolean allowed, long retryAfterSeconds) {
        static Decision allow() {
            return new Decision(true, 0);
        }
    }

    /**
     * @param clientKey dinh danh client (thuong la IP) - xem RateLimitFilter.resolveClientKey().
     */
    public Decision checkAndIncrement(String clientKey) {
        if (!enabled) {
            return Decision.allow();
        }
        String redisKey = KEY_PREFIX + clientKey;
        try {
            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count == null) {
                // increment() tra ve null chi khi co loi driver bat thuong - fail-open.
                return Decision.allow();
            }
            if (count == 1L) {
                // Lan dau tien key nay xuat hien trong window hien tai - mo TTL.
                redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
            }
            if (count > maxRequests) {
                Long ttl = redisTemplate.getExpire(redisKey);
                long retryAfter = (ttl != null && ttl > 0) ? ttl : windowSeconds;
                return new Decision(false, retryAfter);
            }
            return Decision.allow();
        } catch (RuntimeException e) {
            log.warn("Loi Redis khi kiem tra rate limit cho client={}, fail-open (cho qua): {}", clientKey, e.getMessage());
            return Decision.allow();
        }
    }
}
