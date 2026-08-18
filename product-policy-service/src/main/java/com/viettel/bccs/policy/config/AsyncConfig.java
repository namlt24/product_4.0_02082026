package com.viettel.bccs.policy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Size pool mặc định tính theo {@link Runtime#availableProcessors()} - trong k8s con số này có
     * thể lệch với CPU limit thật của pod (cgroup) nên KHÔNG đáng tin tuyệt đối. 2 property dưới cho
     * phép ops ghi đè trực tiếp qua biến môi trường mà không cần build lại image; giá trị {@code -1}
     * nghĩa là "chưa set, dùng công thức cũ theo CPU" (giữ nguyên hành vi mặc định hôm nay).
     */
    @Value("${app.async.core-pool-size:-1}")
    private int configuredCorePoolSize;

    @Value("${app.async.max-pool-size:-1}")
    private int configuredMaxPoolSize;

    @Value("${app.async.queue-capacity:500}")
    private int queueCapacity;

    @Override
    @Bean(name = "taskExecutor")
    @Primary
    public Executor getAsyncExecutor() {
        return createExecutor("taskExecutor");
    }

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        return createExecutor("asyncExecutor");
    }

    private Executor createExecutor(String name) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int cores = Runtime.getRuntime().availableProcessors();
        int corePoolSize = configuredCorePoolSize > 0 ? configuredCorePoolSize : Math.max(4, cores * 2);
        int maxPoolSize = configuredMaxPoolSize > 0 ? configuredMaxPoolSize : Math.max(8, cores * 5);

        log.info("AsyncConfig[{}]: detected {} CPU (Runtime.availableProcessors), corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                name, cores, corePoolSize, maxPoolSize, queueCapacity);

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix(name + "-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable throwable, @NonNull java.lang.reflect.Method method, @NonNull Object... params) -> {
            log.error("[AsyncError] method={} -> {}", method.getName(), throwable.getMessage(), throwable);
        };
    }
}