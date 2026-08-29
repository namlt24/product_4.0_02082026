package com.bccs.gatewaymanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Thread pool RIENG cho thuc thi song song step doc lap khi EndpointConfig.parallelExecution=true
 * (xem CompositeOrchestratorEngine.executeStepsInParallel()) - tach biet hoan toan khoi Tomcat
 * request thread pool, khong dung chung voi bat ky co che nao khac trong module (AuditLogService
 * tu tao ScheduledExecutorService rieng cua no, khong lien quan).
 *
 * Kich thuoc de dat: core=8, max=16 - KHONG can lon hon cac gioi han da co san cua he thong
 * (HikariCP maximum-pool-size=10, Resilience4j Bulkhead per-upstream max-concurrent-calls=20),
 * chi can du de khong tu tao nghen truoc khi cac gioi han do phat huy tac dung.
 *
 * Queue bounded (200) + CallerRunsPolicy: neu pool full VA queue day (rat hiem, vi Bulkhead
 * da chan tai truoc do), thread GOI (chinh request thread dang xu ly HTTP) se tu chay task
 * do thay vi bi tu choi - fail-open ve throughput, khong bao gio mat/reject request vi thread
 * pool day cho.
 */
@Configuration
public class ParallelExecutionConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService parallelStepExecutor() {
        return new ThreadPoolExecutor(
                8, 16,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                r -> {
                    Thread t = new Thread(r, "gwm-parallel-step");
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
