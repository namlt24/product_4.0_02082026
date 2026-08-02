package com.viettel.bccs.policy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
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

        executor.setCorePoolSize(Math.max(4, cores * 2));
        executor.setMaxPoolSize(Math.max(8, cores * 5));
        executor.setQueueCapacity(500);
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