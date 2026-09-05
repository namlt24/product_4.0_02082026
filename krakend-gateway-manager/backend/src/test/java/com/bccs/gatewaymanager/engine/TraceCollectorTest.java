package com.bccs.gatewaymanager.engine;

import com.bccs.gatewaymanager.dto.StepTraceDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test rieng cho TraceCollector (ThreadLocal don gian) - xac nhan dung ban
 * chat "no-op ngoai che do Thu nhanh" (record() truoc start() khong lam gi)
 * va vong doi start()/record()/stop()/attach() dung nhu javadoc mo ta.
 */
class TraceCollectorTest {

    private StepTraceDto entry(int order) {
        return new StepTraceDto(order, "step" + order, "up", "GET", "http://x", null, false,
                200, "{}", false, 10L, false, true, null);
    }

    @AfterEach
    void cleanup() {
        // Tranh ro ri ThreadLocal sang test khac chay cung thread (JUnit co the tai su dung thread).
        TraceCollector.stop();
    }

    @Test
    void recordTruocStart_khongLamGi() {
        assertThat(TraceCollector.current()).isNull();
        TraceCollector.record(entry(1)); // khong throw, khong tao list nao
        assertThat(TraceCollector.current()).isNull();
    }

    @Test
    void start_record_traVeDungList() {
        List<StepTraceDto> list = TraceCollector.start();
        TraceCollector.record(entry(1));
        TraceCollector.record(entry(2));

        assertThat(list).hasSize(2);
        assertThat(TraceCollector.current()).isSameAs(list);
    }

    @Test
    void stop_xoaHetKhoiThreadHienTai() {
        TraceCollector.start();
        TraceCollector.record(entry(1));

        TraceCollector.stop();

        assertThat(TraceCollector.current()).isNull();
        TraceCollector.record(entry(2)); // sau stop() lai la no-op
        assertThat(TraceCollector.current()).isNull();
    }

    @Test
    void attach_ganChungListChoThreadHienTai_ghiVaoDungListDoTruyenVao() {
        List<StepTraceDto> shared = TraceCollector.start();
        TraceCollector.stop(); // gia lap thread khac - thread hien tai dang khong co gi

        TraceCollector.attach(shared);
        TraceCollector.record(entry(9));

        assertThat(shared).hasSize(1);
        assertThat(TraceCollector.current()).isSameAs(shared);
    }

    @Test
    void attachNull_goBoKhoiThreadHienTai() {
        TraceCollector.start();
        TraceCollector.attach(null);

        assertThat(TraceCollector.current()).isNull();
    }
}
