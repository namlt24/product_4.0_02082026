package com.viettel.bccs.area.smoke;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test THẬT cho product-area-service — gọi trực tiếp từng endpoint bằng dữ liệu thật
 * lấy từ Oracle local (không mock), xác nhận HTTP 200 và code=SUCCESS (đúng envelope thật của
 * StandardResponse — {@code code}/{@code message}/{@code data}/{@code timestamp}, KHÔNG có field
 * boolean "success"; xem javap com.viettel.bccs.common.api.response.StandardResponse).
 *
 * Dùng JDK HttpClient + Jackson thuần thay vì TestRestTemplate vì TestRestTemplate không còn tồn
 * tại trong Spring Boot 4 (org.springframework.boot.test.web.client package đã bị gỡ bỏ cùng với
 * việc RestTemplate chuyển sang maintenance mode).
 *
 * KHÔNG chạy trong `mvn test`/`mvn clean install` mặc định vì tên lớp hậu tố *IT không khớp
 * pattern mặc định của Surefire (*Test/*Tests/*TestCase) — chỉ chạy khi gọi tường minh, và cần
 * DB Oracle local (docker `bccs-oracle`, xem docker-compose.local.yml) đang chạy:
 *   .\mvnw.cmd "-Dtest=OpenApiExampleSmokeIT" "-Dspring.profiles.active=local" test
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpenApiExampleSmokeIT {

    private static final String SUCCESS_CODE = "SUCCESS";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ObjectMapper trần, không autowire bean của app — test chỉ đọc field theo path chung
    // (code/data), không cần theo đúng cấu hình Jackson tuỳ biến của app.
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private String parentAreaCode;
    private String childAreaCode;
    private String province;

    @BeforeAll
    void loadRealSampleData() {
        parentAreaCode = jdbcTemplate.queryForObject(
                "SELECT AREA_CODE FROM AREA WHERE PARENT_CODE IS NULL FETCH FIRST 1 ROWS ONLY", String.class);
        childAreaCode = jdbcTemplate.queryForObject(
                "SELECT AREA_CODE FROM AREA WHERE PARENT_CODE = ? FETCH FIRST 1 ROWS ONLY",
                String.class, parentAreaCode);
        province = jdbcTemplate.queryForObject(
                "SELECT PROVINCE FROM AREA WHERE AREA_CODE = ?", String.class, parentAreaCode);
    }

    private String baseUrl() {
        return "http://localhost:" + port + "/product-area-service/v1/area";
    }

    private JsonNode getJson(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, response.statusCode(), "HTTP status không phải 200 cho " + path + ": " + response.body());
        return objectMapper.readTree(response.body());
    }

    @Test
    void getAll_returnsSuccessWithData() throws Exception {
        JsonNode body = getJson("/getAll");
        assertEquals(SUCCESS_CODE, body.path("code").asText());
        assertTrue(body.path("data").isArray());
        assertFalse(body.path("data").isEmpty(), "getAll phải trả về ít nhất 1 địa bàn từ dữ liệu seed");
    }

    @Test
    void getByAreaCode_withRealData_returnsSuccess() throws Exception {
        JsonNode body = getJson("/getByAreaCode/" + parentAreaCode);
        assertEquals(SUCCESS_CODE, body.path("code").asText());
        assertEquals(parentAreaCode, body.path("data").path("areaCode").asText());
    }

    @Test
    void getByParentCode_withRealData_returnsSuccess() throws Exception {
        JsonNode body = getJson("/getByParentCode/" + parentAreaCode);
        assertEquals(SUCCESS_CODE, body.path("code").asText());
        assertTrue(body.path("data").isArray());
        assertFalse(body.path("data").isEmpty(),
                "getByParentCode(" + parentAreaCode + ") phải trả về ít nhất child area '" + childAreaCode + "'");
    }

    @Test
    void getByProvince_withRealData_returnsSuccess() throws Exception {
        JsonNode body = getJson("/getByProvince?province=" + province);
        assertEquals(SUCCESS_CODE, body.path("code").asText());
        assertTrue(body.path("data").isArray());
        assertFalse(body.path("data").isEmpty(), "getByProvince(" + province + ") phải trả về ít nhất 1 địa bàn");
    }
}
