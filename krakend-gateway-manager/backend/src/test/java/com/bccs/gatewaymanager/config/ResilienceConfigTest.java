package com.bccs.gatewaymanager.config;

import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * ResilienceConfig lang nghe MOI Bulkhead duoc dang ky dong (theo ten Upstream hoac theo
 * step, xem UpstreamHttpExecutor.bulkheadFor()) va ghi lai (qua
 * UpstreamHttpExecutor.recordBulkheadRejection()) moi lan bi tu choi - day la phan "lam lo
 * ro bay Bulkhead mac dinh" (xem docs/IMPROVEMENT-Bulkhead-Visibility-Gateway-Manager.md):
 * mot request bi 429/503 do Bulkhead qua chat gio phai ghi lai duoc, khong con am tham.
 *
 * Dung BulkheadRegistry.ofDefaults() THAT (khong mock registry - can dung luong su kien
 * onEntryAdded/onCallRejected THAT su chay, mirror dung cach UpstreamHttpExecutorTest dang
 * test finding #4), UpstreamHttpExecutor chi MOCK de verify co goi
 * recordBulkheadRejection() hay khong (khong can HTTP that, class nay khong tu goi HTTP).
 */
class ResilienceConfigTest {

    private final CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
    private final BulkheadRegistry bulkheadRegistry = BulkheadRegistry.ofDefaults();
    private final UpstreamHttpExecutor upstreamHttpExecutor = Mockito.mock(UpstreamHttpExecutor.class);
    private final ResilienceConfig config = new ResilienceConfig(circuitBreakerRegistry, bulkheadRegistry, upstreamHttpExecutor, java.util.List.of());

    @Test
    void bulkheadTuChoiLenhGoi_goiRecordBulkheadRejectionDungTenBulkhead() throws Exception {
        // PHAI dang ky listener TRUOC khi tao bulkhead - onEntryAdded() chi bat duoc entry
        // them SAU thoi diem dang ky, dung y het thu tu that trong production: app da goi
        // registerEventListeners() qua @PostConstruct LUC KHOI DONG, truoc khi
        // UpstreamHttpExecutor.bulkheadFor() tao bulkhead dong theo ten Upstream/step tai
        // request-time.
        config.registerEventListeners();
        Bulkhead bh = bulkheadRegistry.bulkhead("test-upstream", BulkheadConfig.custom()
                .maxConcurrentCalls(1).maxWaitDuration(Duration.ofMillis(50)).build());

        Supplier<String> slow = () -> {
            try {
                Thread.sleep(300); // du lau de lenh goi thu 2 chac chan con dang giu bulkhead luc lenh goi 1 goi toi
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            return "ok";
        };

        CountDownLatch firstCallStarted = new CountDownLatch(1);
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            pool.submit(() -> {
                firstCallStarted.countDown();
                Bulkhead.decorateSupplier(bh, slow).get();
            });
            assertThat(firstCallStarted.await(2, TimeUnit.SECONDS)).isTrue();
            Thread.sleep(30); // dam bao lenh goi 1 da thuc su chiem duoc slot bulkhead truoc khi lenh goi 2 toi

            assertThatThrownBy(() -> Bulkhead.decorateSupplier(bh, slow).get())
                    .isInstanceOf(BulkheadFullException.class);
        } finally {
            pool.shutdown();
            pool.awaitTermination(2, TimeUnit.SECONDS);
        }

        verify(upstreamHttpExecutor).recordBulkheadRejection("test-upstream");
    }

    @Test
    void bulkheadKhongBiDay_khongGoiRecordBulkheadRejection() {
        config.registerEventListeners();
        Bulkhead bh = bulkheadRegistry.bulkhead("test-upstream-2", BulkheadConfig.custom()
                .maxConcurrentCalls(5).build());

        Bulkhead.decorateSupplier(bh, () -> "ok").get();

        verify(upstreamHttpExecutor, never()).recordBulkheadRejection(ArgumentMatchers.any());
    }
}
