package com.viettel.bccs.productcatalog.smoke;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test THẬT cho product-catalog-service — gọi trực tiếp từng endpoint bằng dữ liệu
 * thật lấy từ Oracle local (không mock), xác nhận HTTP 200 và code=SUCCESS.
 *
 * PHẠM VI: bao phủ mỗi controller có endpoint thật với 1-3 endpoint tiêu biểu (tra cứu chính) —
 * không lặp lại tất cả biến thể tham số của mọi endpoint (project có 53 endpoint trên 16
 * controller; OpenApiComplianceTest đã đảm bảo tĩnh 100% endpoint có @Operation +
 * @ExampleObject hợp lệ, lớp này xác nhận thêm một lượt gọi thật has-data cho phần lớn feature).
 * 5 controller không có endpoint nào (stub, chỉ inject service, chưa implement) không xuất hiện ở
 * đây: ProductOfferRelationController, ProductOfferRelationDetailController,
 * ProductOfferTypeController, ProdPackProductOfferTypeController, ProdPackShopController,
 * ProductPackageCharUseController.
 *
 * KHÔNG chạy trong `mvn test` mặc định (tên hậu tố *IT) — chỉ chạy khi gọi tường minh, cần Oracle
 * local (docker bccs-oracle) đang chạy:
 *   .\mvnw.cmd "-Dtest=OpenApiExampleSmokeIT" "-Dspring.profiles.active=local" test
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "bccs.cache.mode=memory-only")
@ActiveProfiles("local")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpenApiExampleSmokeIT {

    private static final String SUCCESS_CODE = "SUCCESS";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private String productCode;
    private Long productOfferingId;
    private Long optionSetId;
    private String optionSetCode;
    private String telecomAlias;
    private Long productPackageId;
    private String productPackageCode;
    private String productPackageStatus;
    private String productPackageType;
    private Long telecomServiceId;
    private Long productPackageFeeId;
    private Long productPackageIdForFee;
    private Long productSpecCharId;
    private String productSpecCharCode;
    private Long productSpecCharValueId;

    @BeforeAll
    void loadRealSampleData() {
        var product = jdbcTemplate.queryForMap(
                "SELECT PRODUCT_OFFERING_ID, CODE FROM PRODUCT_OFFERING WHERE STATUS='1' AND ROWNUM <= 1");
        productOfferingId = ((Number) product.get("PRODUCT_OFFERING_ID")).longValue();
        productCode = (String) product.get("CODE");

        var optionSet = jdbcTemplate.queryForMap(
                "SELECT OPTION_SET_ID, CODE FROM OPTION_SET WHERE STATUS='1' AND ROWNUM <= 1");
        optionSetId = ((Number) optionSet.get("OPTION_SET_ID")).longValue();
        optionSetCode = (String) optionSet.get("CODE");

        telecomAlias = jdbcTemplate.queryForObject(
                "SELECT SERVICE_ALIAS FROM TELECOM_SERVICE WHERE STATUS='1' AND ROWNUM <= 1", String.class);

        var pack = jdbcTemplate.queryForMap(
                "SELECT PRODUCT_PACKAGE_ID, CODE, STATUS, TYPE, TELECOM_SERVICE_ID FROM PRODUCT_PACKAGE "
                        + "WHERE STATUS='1' AND ROWNUM <= 1");
        productPackageId = ((Number) pack.get("PRODUCT_PACKAGE_ID")).longValue();
        productPackageCode = (String) pack.get("CODE");
        productPackageStatus = (String) pack.get("STATUS");
        productPackageType = (String) pack.get("TYPE");
        telecomServiceId = ((Number) pack.get("TELECOM_SERVICE_ID")).longValue();

        var fee = jdbcTemplate.queryForMap(
                "SELECT PRODUCT_PACKAGE_FEE_ID, PRODUCT_PACKAGE_ID FROM PRODUCT_PACKAGE_FEE WHERE ROWNUM <= 1");
        productPackageFeeId = ((Number) fee.get("PRODUCT_PACKAGE_FEE_ID")).longValue();
        productPackageIdForFee = ((Number) fee.get("PRODUCT_PACKAGE_ID")).longValue();

        var specChar = jdbcTemplate.queryForMap(
                "SELECT PRODUCT_SPEC_CHAR_ID, CODE FROM PRODUCT_SPEC_CHAR WHERE STATUS='1' AND CODE IS NOT NULL AND ROWNUM <= 1");
        productSpecCharId = ((Number) specChar.get("PRODUCT_SPEC_CHAR_ID")).longValue();
        productSpecCharCode = (String) specChar.get("CODE");

        productSpecCharValueId = jdbcTemplate.queryForObject(
                "SELECT PRODUCT_SPEC_CHAR_VALUE_ID FROM PRODUCT_SPEC_CHAR_VALUE WHERE ROWNUM <= 1", Long.class);
    }

    private String baseUrl() {
        return "http://localhost:" + port + "/product-catalog-service/v1";
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

    private JsonNode postJson(String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, response.statusCode(), "HTTP status không phải 200 cho POST " + path + ": " + response.body());
        return objectMapper.readTree(response.body());
    }

    private void assertSuccess(JsonNode body) {
        assertEquals(SUCCESS_CODE, body.path("code").asText());
    }

    // ---------------------------------------------------------------- ProductOffering

    @Test
    void product_getByProductCode_returnsSuccess() throws Exception {
        JsonNode body = getJson("/product/getByProductCode?productCode=" + productCode);
        assertSuccess(body);
        assertEquals(productCode, body.path("data").path("code").asText());
    }

    // ---------------------------------------------------------------- OptionSet

    @Test
    void optionSet_getAll_returnsSuccess() throws Exception {
        JsonNode body = getJson("/optionset");
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    @Test
    void optionSet_getById_returnsSuccess() throws Exception {
        JsonNode body = getJson("/optionset/getById/" + optionSetId);
        assertSuccess(body);
        assertEquals(optionSetId.longValue(), body.path("data").path("optionSetId").asLong());
    }

    @Test
    void optionSet_getByCode_returnsSuccess() throws Exception {
        JsonNode body = getJson("/optionset/getByCode/" + optionSetCode);
        assertSuccess(body);
        assertEquals(optionSetCode, body.path("data").path("code").asText());
    }

    // ---------------------------------------------------------------- OptionSetValue

    @Test
    void optionSetValue_getByOptionSetId_returnsSuccess() throws Exception {
        JsonNode body = getJson("/optionsetvalue/getByOptionSetId?optionSetId=" + optionSetId);
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    @Test
    void optionSetValue_findByOptionSetCode_returnsSuccess() throws Exception {
        JsonNode body = getJson("/optionsetvalue/findByOptionSetCode/" + optionSetCode);
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    @Test
    void optionSetValue_getAllGroupCustType_returnsSuccess() throws Exception {
        JsonNode body = getJson("/optionsetvalue/getAllGroupCustType");
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    // ---------------------------------------------------------------- TelecomService

    @Test
    void telecomService_getTelServiceByAlias_returnsSuccess() throws Exception {
        JsonNode body = getJson("/telecom-service/getTelServiceByAlias?alias=" + telecomAlias);
        assertSuccess(body);
        assertEquals(telecomAlias, body.path("data").path("serviceAlias").asText());
    }

    // ---------------------------------------------------------------- ProductOfferCharUse

    @Test
    void productOfferCharUse_getProductSpecCharByOfferingIds_returnsSuccess() throws Exception {
        JsonNode body = postJson("/product-offer-char-use/getProductSpecCharByOfferingIds", "[\"" + productOfferingId + "\"]");
        assertSuccess(body);
        assertTrue(body.path("data").isObject());
    }

    // ---------------------------------------------------------------- ProductOfferPrice

    @Test
    void productOfferPrice_getPriceInServices_returnsSuccess() throws Exception {
        // Gọi không truyền tham số nào (tất cả optional) -> service trả sớm data=null (đã xác
        // nhận đúng hành vi migrate từ hệ thống mono ở phiên làm việc trước) - vẫn là SUCCESS.
        JsonNode body = getJson("/productofferprice/getPriceInServices");
        assertSuccess(body);
    }

    // ---------------------------------------------------------------- ProductPackage

    @Test
    void productPackage_findById_returnsSuccess() throws Exception {
        JsonNode body = getJson("/product-package/findById/" + productPackageId);
        assertSuccess(body);
        assertEquals(productPackageId.longValue(), body.path("data").path("productPackageId").asLong());
    }

    @Test
    void productPackage_getByCode_returnsSuccess() throws Exception {
        JsonNode body = getJson("/product-package/getByCode/" + productPackageCode);
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    @Test
    void productPackage_getByStatus_returnsSuccess() throws Exception {
        JsonNode body = getJson("/product-package/getByStatus?status=" + productPackageStatus);
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    @Test
    void productPackage_getByType_returnsSuccess() throws Exception {
        JsonNode body = getJson("/product-package/getByType?type=" + productPackageType);
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    @Test
    void productPackage_getByTelecomServiceId_returnsSuccess() throws Exception {
        JsonNode body = getJson("/product-package/getByTelecomServiceId?telecomServiceId=" + telecomServiceId);
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    // ---------------------------------------------------------------- ProductPackageFee

    @Test
    void productPackageFee_findById_returnsSuccess() throws Exception {
        JsonNode body = getJson("/product-package-fee/findById/" + productPackageFeeId);
        assertSuccess(body);
        assertEquals(productPackageFeeId.longValue(), body.path("data").path("productPackageFeeId").asLong());
    }

    @Test
    void productPackageFee_getByProductPackageId_returnsSuccess() throws Exception {
        JsonNode body = getJson("/product-package-fee/getByProductPackageId/" + productPackageIdForFee);
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    // ---------------------------------------------------------------- ProductSpecChar

    @Test
    void productSpecChar_getAll_returnsSuccess() throws Exception {
        JsonNode body = getJson("/productspecchar/getAll");
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    @Test
    void productSpecChar_getById_returnsSuccess() throws Exception {
        JsonNode body = getJson("/productspecchar/getById/" + productSpecCharId);
        assertSuccess(body);
        assertEquals(productSpecCharId.longValue(), body.path("data").path("productSpecCharId").asLong());
    }

    @Test
    void productSpecChar_getByCode_returnsSuccess() throws Exception {
        JsonNode body = getJson("/productspecchar/getByCode/" + productSpecCharCode);
        assertSuccess(body);
        assertEquals(productSpecCharCode, body.path("data").path("code").asText());
    }

    @Test
    void productSpecChar_findByIds_returnsSuccess() throws Exception {
        JsonNode body = postJson("/productspecchar/findByIds", "[" + productSpecCharId + "]");
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    // ---------------------------------------------------------------- ProductSpecCharValue

    @Test
    void productSpecCharValue_findByIds_returnsSuccess() throws Exception {
        JsonNode body = postJson("/productspectcharvalue/findByIds", "[" + productSpecCharValueId + "]");
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }
}
