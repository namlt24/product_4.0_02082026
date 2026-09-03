package com.bccs.gatewaymanager.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
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

    /**
     * @param body chi co y nghia voi method co request body (vi du POST dung lam API
     *        tim kiem/tra cuu - filter nam trong body, khong phai query string). GET
     *        hau nhu luon truyen null - giu KHONG doi dinh dang key cu (khong them
     *        hau to) de khong lam "rong" toan bo cache GET dang chay khi trien khai
     *        thay doi nay. Voi POST, 2 request cung URL nhung KHAC body PHAI la 2 key
     *        khac nhau - neu khong se tra nham response cua 1 filter khac cho client.
     */
    public static String buildKey(String upstreamName, String method, String resolvedUrl, JsonNode body) {
        String base = "gwm:cache:" + upstreamName + ":" + method + ":" + resolvedUrl;
        if (body == null || body.isNull()) {
            return base;
        }
        return base + ":" + sha256Hex(body.toString());
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            // 16 hex dau (64 bit) du de tranh dung do trong pham vi 1 URL, khong can
            // full 64 hex ky tu - giu key Redis gon.
            return HexFormat.of().formatHex(hash, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 luon co san tren moi JVM chuan - nhanh nay thuc te khong bao gio xay ra.
            throw new IllegalStateException("SHA-256 khong kha dung", e);
        }
    }
}
