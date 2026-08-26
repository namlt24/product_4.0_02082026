package com.bccs.gatewaymanager.audit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BodyTruncatorTest {

    @Test
    void nullInput_traVeNullKhongTruncated() {
        var result = BodyTruncator.truncate(null);
        assertThat(result.body()).isNull();
        assertThat(result.truncated()).isFalse();
    }

    @Test
    void ngangGioiHan_giuNguyenKhongTruncated() {
        String body = "x".repeat(BodyTruncator.MAX_LENGTH);
        var result = BodyTruncator.truncate(body);
        assertThat(result.body()).hasSize(BodyTruncator.MAX_LENGTH);
        assertThat(result.truncated()).isFalse();
    }

    @Test
    void vuotGioiHan_catDungDoDaiVaDanhDauTruncated() {
        String body = "x".repeat(BodyTruncator.MAX_LENGTH + 500);
        var result = BodyTruncator.truncate(body);
        assertThat(result.body()).hasSize(BodyTruncator.MAX_LENGTH);
        assertThat(result.truncated()).isTrue();
    }
}
