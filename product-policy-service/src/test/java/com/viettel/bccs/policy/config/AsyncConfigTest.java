package com.viettel.bccs.policy.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test hồi quy cho việc size pool có thể cấu hình qua {@code app.async.core-pool-size}/
 * {@code max-pool-size} thay vì luôn tính cứng theo {@link Runtime#availableProcessors()} - xem
 * ghi chú trong {@link AsyncConfig} về rủi ro con số này lệch với CPU limit thật của pod trên k8s.
 */
class AsyncConfigTest {

    @Test
    void createExecutor_propertiesUnset_fallsBackToCpuBasedFormula() {
        AsyncConfig config = new AsyncConfig();
        ReflectionTestUtils.setField(config, "configuredCorePoolSize", -1);
        ReflectionTestUtils.setField(config, "configuredMaxPoolSize", -1);
        ReflectionTestUtils.setField(config, "queueCapacity", 500);

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.asyncExecutor();

        int cores = Runtime.getRuntime().availableProcessors();
        assertThat(executor.getCorePoolSize()).isEqualTo(Math.max(4, cores * 2));
        assertThat(executor.getMaxPoolSize()).isEqualTo(Math.max(8, cores * 5));
    }

    @Test
    void createExecutor_propertiesSet_overridesCpuBasedFormula() {
        AsyncConfig config = new AsyncConfig();
        ReflectionTestUtils.setField(config, "configuredCorePoolSize", 2);
        ReflectionTestUtils.setField(config, "configuredMaxPoolSize", 3);
        ReflectionTestUtils.setField(config, "queueCapacity", 50);

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.asyncExecutor();

        assertThat(executor.getCorePoolSize()).isEqualTo(2);
        assertThat(executor.getMaxPoolSize()).isEqualTo(3);
    }

    @Test
    void taskExecutor_andAsyncExecutor_bothHonorConfiguredPoolSize() {
        AsyncConfig config = new AsyncConfig();
        ReflectionTestUtils.setField(config, "configuredCorePoolSize", 7);
        ReflectionTestUtils.setField(config, "configuredMaxPoolSize", 9);
        ReflectionTestUtils.setField(config, "queueCapacity", 500);

        Executor taskExecutor = config.getAsyncExecutor();
        Executor asyncExecutor = config.asyncExecutor();

        assertThat(((ThreadPoolTaskExecutor) taskExecutor).getCorePoolSize()).isEqualTo(7);
        assertThat(((ThreadPoolTaskExecutor) asyncExecutor).getCorePoolSize()).isEqualTo(7);
    }
}
