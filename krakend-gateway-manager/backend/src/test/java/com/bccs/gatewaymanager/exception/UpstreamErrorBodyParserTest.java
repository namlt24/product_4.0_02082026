package com.bccs.gatewaymanager.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpstreamErrorBodyParserTest {

    @Test
    void bccsStandardResponse_tachDayDuCacTruong() {
        String body = """
                {"code":"BCCS-GLOBAL-VALIDATION-0000","data":null,
                 "message":"Trường productCode không được để trống","messageArgs":["productCode"],
                 "requestId":"r-1","shortTraceId":"st-1","timestamp":"2026-10-05T02:33:26Z","traceId":"t-1"}
                """;

        ErrorResponse rs = UpstreamErrorBodyParser.parse("GW-UPSTREAM-4XX", "svc", 400, body);

        assertThat(rs.errorCode()).isEqualTo("GW-UPSTREAM-4XX");
        assertThat(rs.message()).isEqualTo("Upstream 'svc' tra ve HTTP 400: Trường productCode không được để trống");
        assertThat(rs.code()).isEqualTo("BCCS-GLOBAL-VALIDATION-0000");
        assertThat(rs.data()).isNull();
        assertThat(rs.requestId()).isEqualTo("r-1");
        assertThat(rs.shortTraceId()).isEqualTo("st-1");
        assertThat(rs.traceId()).isEqualTo("t-1");
    }

    @Test
    void bccsCoDataKhacNull_giuNguyenData() {
        String body = """
                {"code":"BCCS-E-0001","data":{"productCode":["dup"]},"message":"Du lieu trung",
                 "requestId":"r-2","shortTraceId":"st-2","traceId":"t-2"}
                """;

        ErrorResponse rs = UpstreamErrorBodyParser.parse("GW-UPSTREAM-4XX", "svc", 400, body);

        assertThat(rs.data()).isNotNull();
        assertThat(rs.data().toString()).contains("dup");
        assertThat(rs.message()).isEqualTo("Upstream 'svc' tra ve HTTP 400: Du lieu trung");
    }

    @Test
    void bodyThieuMessage_dungRawBodyLamPhanMessage() {
        String body = "{\"code\":\"X-1\",\"data\":null}";

        ErrorResponse rs = UpstreamErrorBodyParser.parse("GW-UPSTREAM-4XX", "svc", 400, body);

        assertThat(rs.message()).isEqualTo("Upstream 'svc' tra ve HTTP 400: " + body);
        assertThat(rs.code()).isEqualTo("X-1");
    }

    @Test
    void bodyKhongPhaiJson_fallbackGomRawVaoMessageKhongCoTruongPhu() {
        ErrorResponse rs = UpstreamErrorBodyParser.parse("GW-UPSTREAM-5XX", "svc", 503, "plain text error");

        assertThat(rs.message()).isEqualTo("Upstream 'svc' tra ve HTTP 503: plain text error".replace("plain text error", "plain text error"));
        assertThat(rs.code()).isNull();
        assertThat(rs.data()).isNull();
        assertThat(rs.requestId()).isNull();
    }

    @Test
    void bodyJsonArray_fallbackGomRawVaoMessage() {
        ErrorResponse rs = UpstreamErrorBodyParser.parse("GW-UPSTREAM-4XX", "svc", 400, "[1,2,3]");

        assertThat(rs.message()).isEqualTo("Upstream 'svc' tra ve HTTP 400: [1,2,3]");
        assertThat(rs.code()).isNull();
    }

    @Test
    void bodyNullOrBlank_fallback() {
        ErrorResponse rs1 = UpstreamErrorBodyParser.parse("GW-UPSTREAM-4XX", "svc", 400, null);
        ErrorResponse rs2 = UpstreamErrorBodyParser.parse("GW-UPSTREAM-4XX", "svc", 400, "   ");

        assertThat(rs1.message()).isEqualTo("Upstream 'svc' tra ve HTTP 400: null");
        assertThat(rs2.message()).isEqualTo("Upstream 'svc' tra ve HTTP 400:    ");
        assertThat(rs1.code()).isNull();
        assertThat(rs2.code()).isNull();
    }
}
