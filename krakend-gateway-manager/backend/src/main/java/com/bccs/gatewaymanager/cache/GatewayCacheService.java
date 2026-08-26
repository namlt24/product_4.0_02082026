package com.bccs.gatewaymanager.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Cache-aside mong cho cac lan goi GET toi Upstream (xem CompositeOrchestratorEngine).
 * Dung truc tiep StringRedisTemplate (khong qua @Cacheable) vi key duoc dung
 * DONG theo tung request (ten upstream + URL + query da resolve) - khong phai
 * theo 1 chu ky method co dinh nhu cach @Cacheable hoat dong.
 *
 * TTL duoc jitter +-15% moi lan ghi de tranh "cache stampede" (nhieu key cung
 * het han dong thoi, don dap don Upstream cung luc).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayCacheService {

    private static final double JITTER_RATIO = 0.15;

    private final StringRedisTemplate redisTemplate;

    public Optional<String> get(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            return Optional.ofNullable(value);
        } catch (RuntimeException e) {
            // Redis loi tam thoi khong duoc lam sap request that - coi nhu cache-miss.
            log.warn("Loi doc cache Redis cho key={}, coi nhu cache-miss: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    public void put(String key, String value, int ttlSeconds) {
        try {
            long jitterMillis = (long) (ttlSeconds * 1000L * JITTER_RATIO
                    * (ThreadLocalRandom.current().nextDouble() * 2 - 1));
            long effectiveMillis = Math.max(1000L, ttlSeconds * 1000L + jitterMillis);
            redisTemplate.opsForValue().set(key, value, Duration.ofMillis(effectiveMillis));
        } catch (RuntimeException e) {
            log.warn("Loi ghi cache Redis cho key={}, bo qua (khong chan request): {}", key, e.getMessage());
        }
    }

    public static String buildKey(String upstreamName, String method, String resolvedUrl) {
        return "gwm:cache:" + upstreamName + ":" + method + ":" + resolvedUrl;
    }
}
