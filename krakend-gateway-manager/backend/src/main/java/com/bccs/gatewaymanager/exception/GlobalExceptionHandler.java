package com.bccs.gatewaymanager.exception;

import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor.UpstreamHttpErrorException;
import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor.UpstreamTimeoutException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Circuit breaker dang mo cho 1 Upstream - fail fast, khong con doi timeout day du. */
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponse> handleCircuitOpen(CallNotPermittedException ex) {
        log.warn("Circuit breaker dang mo, tu choi goi: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.of("GW-CIRCUIT-OPEN", "Backend tam thoi khong kha dung (circuit breaker dang mo): " + ex.getMessage()));
    }

    /** Bulkhead day (qua nhieu request dong thoi toi cung 1 Upstream). */
    @ExceptionHandler(BulkheadFullException.class)
    public ResponseEntity<ErrorResponse> handleBulkheadFull(BulkheadFullException ex) {
        log.warn("Bulkhead day, tu choi goi: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.of("GW-BULKHEAD-FULL", "Qua nhieu request dong thoi toi Upstream: " + ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        log.warn("Business error [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.badRequest().body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    /** Upstream that su tra ve HTTP loi (4xx/5xx) - phan biet voi loi ha tang chung, tra 502 kem ma loi cu the. */
    @ExceptionHandler(UpstreamHttpErrorException.class)
    public ResponseEntity<ErrorResponse> handleUpstreamHttpError(UpstreamHttpErrorException ex) {
        log.warn("Upstream HTTP error: {}", ex.getMessage());
        String errorCode = ex.httpStatus() >= 500 ? "GW-UPSTREAM-5XX" : "GW-UPSTREAM-4XX";
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ErrorResponse.of(errorCode, ex.getMessage()));
    }

    /** Upstream khong phan hoi kip (timeout/connection refused) - khac loi nghiep vu upstream tra ve. */
    @ExceptionHandler(UpstreamTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleUpstreamTimeout(UpstreamTimeoutException ex) {
        log.warn("Upstream timeout: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(ErrorResponse.of("GW-UPSTREAM-TIMEOUT", ex.getMessage()));
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ErrorResponse> handleSystem(SystemException ex) {
        log.error("System error: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.of("SYSTEM_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("VALIDATION_ERROR", message));
    }

    // ---- Loi do CLIENT tu gui sai (body/param) - truoc day KHONG co handler
    // rieng nen roi thang xuong handleUnknown() ben duoi -> tra ve 500
    // UNKNOWN_ERROR du 100% la loi phia client gui sai, khong phai loi he
    // thong. Ap dung cho ca Control Plane (/api/**, vi du EndpointController
    // nhan @RequestBody sai JSON) lan cac tham so @RequestParam/@PathVariable
    // co kieu (vi du LogSearchController.searchRequests nhan "from"/"to" kieu
    // Instant - client truyen chuoi khong phai ngay thang hop le se roi vao day). ----

    /** Body request khong doc duoc thanh JSON (sai cu phap, thieu dau ngoac...). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBodyNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Body request khong hop le: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("INVALID_REQUEST_BODY", "Body request khong hop le (JSON sai cu phap hoac thieu truong)."));
    }

    /** Thieu 1 @RequestParam bat buoc (khong co "required = false"/defaultValue). */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Thieu tham so bat buoc: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("MISSING_PARAMETER", "Thieu tham so bat buoc: " + ex.getParameterName()));
    }

    /** @RequestParam/@PathVariable dung kieu nhung gia tri client gui khong ep duoc (vi du "from=abc" cho kieu Instant). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Tham so sai kieu du lieu: {}", ex.getMessage());
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "?";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("INVALID_PARAMETER_TYPE",
                        "Tham so '" + ex.getName() + "' sai kieu du lieu (can kieu " + requiredType + ")."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception ex) {
        log.error("Unhandled error", ex);
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.of("UNKNOWN_ERROR", "Da co loi khong xac dinh xay ra"));
    }
}
