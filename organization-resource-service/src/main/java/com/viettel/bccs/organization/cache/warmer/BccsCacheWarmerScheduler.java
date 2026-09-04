package com.viettel.bccs.organization.cache.warmer;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "bccs.cache.warmer", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BccsCacheWarmerScheduler {

    private final List<BccsCacheWarmTask> tasks;

    private final Map<String, Instant> lastRun = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void warmAllOnStartup() {
        tasks.forEach(this::runTask);
    }

    @Scheduled(initialDelayString = "${bccs.cache.warmer.tick-interval-ms:60000}",
            fixedRateString = "${bccs.cache.warmer.tick-interval-ms:60000}")
    public void tick() {
        Instant now = Instant.now();
        for (BccsCacheWarmTask task : tasks) {
            Instant last = lastRun.getOrDefault(task.cacheName(), Instant.EPOCH);
            if (Duration.between(last, now).compareTo(task.interval()) >= 0) {
                runTask(task);
            }
        }
    }

    private void runTask(BccsCacheWarmTask task) {
        long start = System.currentTimeMillis();
        try {
            task.warm();
            lastRun.put(task.cacheName(), Instant.now());
            log.info("✅ warm cache '{}' trong {}ms", task.cacheName(), System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.warn("Warm cache '{}' lỗi: {}", task.cacheName(), e.getMessage(), e);
        }
    }
}
