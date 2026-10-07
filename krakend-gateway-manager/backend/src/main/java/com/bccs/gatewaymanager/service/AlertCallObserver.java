package com.bccs.gatewaymanager.service;

/**
 * Quan sat MOI lan goi HTTP toi Upstream (tu UpstreamHttpExecutor.call()) va
 * MOI request composite hoan thanh (tu DynamicDispatcherController.recordAudit())
 * de gom so lieu cho canh bao. Thiet ke interface thay vi goi truc tiep
 * AlertDetector de:
 * - Control Plane khong co detector -> Spring inject bean no-op (xem
 *   AlertCallObserverConfig) ma khong phai "if (profile==data-plane)" roi rac
 *   trong UpstreamHttpExecutor.
 * - De test: test goi 1 fake observer thay vi dung detector that.
 *
 * KHONG BAO GIO throw - implementation phai tu nuot exception (so lieu canh
 * bao la passive, khong bao gio anh huong traffic that; mirror contract
 * fail-open cua AuditLogService).
 */
public interface AlertCallObserver {

    /** 1 hop HTTP ket thuc (success hoac error) - goi trong finally cua UpstreamHttpExecutor.call(). */
    void recordUpstreamCall(String upstreamName, boolean success);

    /** 1 request composite hoan thanh (status HTTP cuoi cung tra cho client). */
    void recordEndpointRequest(String endpointId, String endpointPath, boolean success);
}
