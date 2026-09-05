package com.bccs.gatewaymanager.engine;

import com.bccs.gatewaymanager.dto.StepTraceDto;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thu thap chi tiet TUNG STEP (request/response da resolve) ngay TRONG tien
 * trinh xu ly - dung cho tinh nang "Thu nhanh" (xem EndpointTryService) de
 * tra ve waterfall tung step cho client MA KHONG phu thuoc pipeline audit
 * Elasticsearch (ghi bat dong bo qua queue + flush 1 giay/lan + refresh_interval
 * rieng cua ES - qua cham/khong dam bao co du lieu ngay khi tryCall() vua tra ve,
 * va "/try" cung khong he set requestId vao MDC de join lai duoc).
 *
 * Thiet ke y HET pattern MDC("requestId") da co san trong
 * CompositeOrchestratorEngine.executeStepsInParallel() (xem javadoc o do) -
 * ThreadLocal, PHAI duoc lan truyen tay sang worker thread cua parallelStepExecutor,
 * KHONG tu dong ke thua. Khac MDC (chi luu String), o day gan CHUNG 1 list
 * (khong copy) cho ca thread goi va worker thread - de nhieu step chay song
 * song cung ghi duoc vao 1 ket qua duy nhat.
 *
 * `record()` la 1 lan doc ThreadLocal + null-check - KHONG doi hanh vi/hieu
 * nang Data Plane that (khong bao gio goi start(), current() luon null).
 */
public final class TraceCollector {

    private static final ThreadLocal<List<StepTraceDto>> HOLDER = new ThreadLocal<>();

    private TraceCollector() {
    }

    /** Bat dau thu thap tren thread hien tai - goi 1 lan o dau 1 lan "Thu nhanh". Tra ve chinh list se duoc dien du lieu. */
    public static List<StepTraceDto> start() {
        List<StepTraceDto> list = new CopyOnWriteArrayList<>();
        HOLDER.set(list);
        return list;
    }

    /** PHAI goi trong finally sau khi thu xong - tranh ro ri sang request Data Plane khac tai su dung CUNG thread cua pool sau nay. */
    public static void stop() {
        HOLDER.remove();
    }

    /**
     * Gan (khong copy) 1 list co san vao thread hien tai - dung de lan truyen
     * sang worker thread cua parallelStepExecutor (xem
     * CompositeOrchestratorEngine.executeStepsInParallel()), mirror
     * MDC.setContextMap() nhung CHIA SE cung 1 list de moi step song song
     * ghi vao CHUNG 1 ket qua. null = go bo (dung khi khoi phuc worker thread
     * ve trang thai truoc do, tranh ro ri sang task khac tai su dung cung thread).
     */
    public static void attach(List<StepTraceDto> shared) {
        if (shared == null) {
            HOLDER.remove();
        } else {
            HOLDER.set(shared);
        }
    }

    /** null = khong o che do "Thu nhanh" (Data Plane that) - dung de chup truoc khi submit sang thread pool. */
    public static List<StepTraceDto> current() {
        return HOLDER.get();
    }

    /** UpstreamHttpExecutor goi sau MOI hop, trong finally cua call() - no-op ngoai che do "Thu nhanh". */
    public static void record(StepTraceDto entry) {
        List<StepTraceDto> list = HOLDER.get();
        if (list != null) {
            list.add(entry);
        }
    }
}
