package com.bccs.gatewaymanager.exception;

import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor.UpstreamHttpErrorException;
import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor.UpstreamTimeoutException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception ex) {
        log.error("Unhandled error", ex);
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.of("UNKNOWN_ERROR", "Da co loi khong xac dinh xay ra"));
    }
}
