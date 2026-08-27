package com.bccs.gatewaymanager.exception;

import com.bccs.gatewaymanager.engine.UpstreamHttpExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBusiness_returns400WithErrorCode() {
        ResponseEntity<ErrorResponse> response = handler.handleBusiness(new BusinessException("GW-003", "loi"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().errorCode()).isEqualTo("GW-003");
    }

    @Test
    void handleSystem_returns500WithGenericCode() {
        ResponseEntity<ErrorResponse> response = handler.handleSystem(new SystemException("boom"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().errorCode()).isEqualTo("SYSTEM_ERROR");
    }

    // ---- Finding #8: upstream 4xx/5xx va timeout phai co ma loi rieng, khong gop chung SYSTEM_ERROR ----

    @Test
    void handleUpstreamHttpError_4xx_returns502WithGwUpstream4xx() {
        var ex = new UpstreamHttpExecutor.UpstreamHttpErrorException("svc", 404, "not found");
        ResponseEntity<ErrorResponse> response = handler.handleUpstreamHttpError(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().errorCode()).isEqualTo("GW-UPSTREAM-4XX");
    }

    @Test
    void handleUpstreamHttpError_5xx_returns502WithGwUpstream5xx() {
        var ex = new UpstreamHttpExecutor.UpstreamHttpErrorException("svc", 500, "internal error");
        ResponseEntity<ErrorResponse> response = handler.handleUpstreamHttpError(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().errorCode()).isEqualTo("GW-UPSTREAM-5XX");
    }

    @Test
    void handleUpstreamTimeout_returns504WithGwUpstreamTimeout() {
        var ex = new UpstreamHttpExecutor.UpstreamTimeoutException("svc", new RuntimeException("connection refused"));
        ResponseEntity<ErrorResponse> response = handler.handleUpstreamTimeout(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        assertThat(response.getBody().errorCode()).isEqualTo("GW-UPSTREAM-TIMEOUT");
    }

    @Test
    void handleUnknown_returns500WithUnknownErrorCode() {
        ResponseEntity<ErrorResponse> response = handler.handleUnknown(new RuntimeException("gi do la"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().errorCode()).isEqualTo("UNKNOWN_ERROR");
    }

    // ---- Senior review finding #4: loi CLIENT gui sai body/param truoc day roi
    // xuong handleUnknown() -> 500 sai, gio phai la 400 rieng. ----

    @Test
    void handleBodyNotReadable_returns400WithInvalidRequestBody() {
        var ex = new HttpMessageNotReadableException("JSON parse error", (HttpInputMessage) null);
        ResponseEntity<ErrorResponse> response = handler.handleBodyNotReadable(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().errorCode()).isEqualTo("INVALID_REQUEST_BODY");
    }

    @Test
    void handleMissingParam_returns400WithParamNameTrongMessage() {
        var ex = new MissingServletRequestParameterException("staffId", "String");
        ResponseEntity<ErrorResponse> response = handler.handleMissingParam(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().errorCode()).isEqualTo("MISSING_PARAMETER");
        assertThat(response.getBody().message()).contains("staffId");
    }

    @Test
    void handleTypeMismatch_returns400WithTenThamSoVaKieuCanTrongMessage() throws NoSuchMethodException {
        MethodParameter param = new MethodParameter(DummyController.class.getDeclaredMethod("dummy", Instant.class), 0);
        var ex = new MethodArgumentTypeMismatchException("abc", Instant.class, "from", param, new IllegalArgumentException("bad"));

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().errorCode()).isEqualTo("INVALID_PARAMETER_TYPE");
        assertThat(response.getBody().message()).contains("from").contains("Instant");
    }

    @SuppressWarnings("unused")
    private static class DummyController {
        void dummy(Instant from) {
        }
    }
}
