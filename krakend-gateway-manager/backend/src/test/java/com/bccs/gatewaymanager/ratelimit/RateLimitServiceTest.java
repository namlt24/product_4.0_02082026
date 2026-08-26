package com.bccs.gatewaymanager.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    private RateLimitService service(boolean enabled, int windowSeconds, int maxRequests) {
        return new RateLimitService(redisTemplate, enabled, windowSeconds, maxRequests);
    }

    @Test
    void disabled_alwaysAllows_khongDungToiRedis() {
        RateLimitService svc = service(false, 60, 5);
        var decision = svc.checkAndIncrement("1.2.3.4");
        assertThat(decision.allowed()).isTrue();
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void requestDauTien_choPhep_vaMoTTL() {
        when(valueOperations.increment(anyString())).thenReturn(1L);
        RateLimitService svc = service(true, 60, 5);

        var decision = svc.checkAndIncrement("1.2.3.4");

        assertThat(decision.allowed()).isTrue();
        verify(redisTemplate).expire(eq("gwm:ratelimit:1.2.3.4"), eq(Duration.ofSeconds(60)));
    }

    @Test
    void requestSauKhongPhaiDauTien_khongMoLaiTTL() {
        when(valueOperations.increment(anyString())).thenReturn(3L);
        RateLimitService svc = service(true, 60, 5);

        svc.checkAndIncrement("1.2.3.4");

        verify(redisTemplate, never()).expire(any(), any());
    }

    @Test
    void duoiNguong_choPhep() {
        when(valueOperations.increment(anyString())).thenReturn(5L);
        RateLimitService svc = service(true, 60, 5);

        assertThat(svc.checkAndIncrement("1.2.3.4").allowed()).isTrue();
    }

    @Test
    void vuotNguong_tuChoi_vaTraVeRetryAfterTheoTTLThat() {
        when(valueOperations.increment(anyString())).thenReturn(6L);
        when(redisTemplate.getExpire("gwm:ratelimit:1.2.3.4")).thenReturn(42L);
        RateLimitService svc = service(true, 60, 5);

        var decision = svc.checkAndIncrement("1.2.3.4");

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.retryAfterSeconds()).isEqualTo(42L);
    }

    @Test
    void vuotNguong_TTLKhongDocDuoc_fallbackVeWindowSeconds() {
        when(valueOperations.increment(anyString())).thenReturn(6L);
        when(redisTemplate.getExpire("gwm:ratelimit:1.2.3.4")).thenReturn(-1L);
        RateLimitService svc = service(true, 60, 5);

        var decision = svc.checkAndIncrement("1.2.3.4");

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.retryAfterSeconds()).isEqualTo(60L);
    }

    @Test
    void loiRedis_failOpen_choPhepQua() {
        when(valueOperations.increment(anyString())).thenThrow(new RuntimeException("Redis down"));
        RateLimitService svc = service(true, 60, 5);

        assertThat(svc.checkAndIncrement("1.2.3.4").allowed()).isTrue();
    }

    @Test
    void moiClientKey_ratelimitDocLap() {
        when(valueOperations.increment("gwm:ratelimit:1.2.3.4")).thenReturn(6L);
        when(valueOperations.increment("gwm:ratelimit:5.6.7.8")).thenReturn(1L);
        RateLimitService svc = service(true, 60, 5);

        assertThat(svc.checkAndIncrement("1.2.3.4").allowed()).isFalse();
        assertThat(svc.checkAndIncrement("5.6.7.8").allowed()).isTrue();
    }
}
