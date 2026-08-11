package com.viettel.bccs.organization.smoke;

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
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test THẬT cho organization-resource-service — gọi trực tiếp từng endpoint bằng dữ
 * liệu thật lấy từ Oracle local (không mock), xác nhận HTTP 200 và code=SUCCESS (đúng envelope
 * thật của StandardResponse — code/message/data/timestamp, KHÔNG có field boolean "success").
 *
 * KHÔNG chạy trong `mvn test`/`mvn clean install` mặc định (tên hậu tố *IT không khớp pattern
 * Surefire *Test/*Tests/*TestCase) — chỉ chạy khi gọi tường minh, cần Oracle local (docker
 * `bccs-oracle`) đang chạy:
 *   .\mvnw.cmd "-Dtest=OpenApiExampleSmokeIT" "-Dspring.profiles.active=local" test
 */
/**
 * bccs.cache.mode override: application-local.yml mặc định redis-only, nhưng máy dev không chạy
 * Redis (chỉ có Oracle docker) — dùng memory-only đúng theo hướng dẫn "Run with minimal
 * dependencies" trong CLAUDE.md, tránh mọi @Cacheable bị treo hàng chục giây chờ kết nối Redis.
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

    private Long shopId;
    private String shopCode;
    private Long shopChannelTypeId;
    private Long staffId;
    private String staffCode;
    private Long channelTypeId;
    private String channelTypeCode;
    private String custType;
    private String idType;
    private Long custChannelTypeMapId;
    private Long staffExtId;
    private Long staffExtStaffId;
    private String staffExtKey;

    @BeforeAll
    void loadRealSampleData() {
        var shop = jdbcTemplate.queryForMap(
                "SELECT SHOP_ID, SHOP_CODE, CHANNEL_TYPE_ID FROM SHOP WHERE STATUS='1' AND ROWNUM <= 1");
        shopId = ((Number) shop.get("SHOP_ID")).longValue();
        shopCode = (String) shop.get("SHOP_CODE");
        shopChannelTypeId = ((Number) shop.get("CHANNEL_TYPE_ID")).longValue();

        var staff = jdbcTemplate.queryForMap(
                "SELECT STAFF_ID, STAFF_CODE FROM STAFF WHERE ROWNUM <= 1");
        staffId = ((Number) staff.get("STAFF_ID")).longValue();
        staffCode = (String) staff.get("STAFF_CODE");

        var channelType = jdbcTemplate.queryForMap(
                "SELECT CHANNEL_TYPE_ID, CODE FROM CHANNEL_TYPE WHERE STATUS='1' AND ROWNUM <= 1");
        channelTypeId = ((Number) channelType.get("CHANNEL_TYPE_ID")).longValue();
        channelTypeCode = (String) channelType.get("CODE");

        // Bảng có vài bản ghi dữ liệu rác dạng dấu câu (vd "...") không đúng format nghiệp vụ thật
        // — lọc theo đúng pattern đã áp cho CustTypeDTO.custType để lấy 1 bản ghi hợp lệ thật.
        custType = jdbcTemplate.queryForObject(
                "SELECT CUST_TYPE FROM CUST_TYPE WHERE REGEXP_LIKE(CUST_TYPE, '^[A-Za-z0-9_-]+$') AND ROWNUM <= 1",
                String.class);

        idType = jdbcTemplate.queryForObject(
                "SELECT ID_TYPE FROM IDENTITY_TYPE WHERE STATUS='1' AND ROWNUM <= 1", String.class);

        var mapping = jdbcTemplate.queryForMap(
                "SELECT CUST_CHANNEL_TYPE_MAP_ID, CHANNEL_TYPE_ID FROM CUST_CHANNEL_TYPE_MAPPING WHERE STATUS='1' AND ROWNUM <= 1");
        custChannelTypeMapId = ((Number) mapping.get("CUST_CHANNEL_TYPE_MAP_ID")).longValue();

        var staffExt = jdbcTemplate.queryForMap(
                "SELECT STAFF_EXT_ID, STAFF_ID, \"KEY\" FROM STAFF_EXT WHERE ROWNUM <= 1");
        staffExtId = ((Number) staffExt.get("STAFF_EXT_ID")).longValue();
        staffExtStaffId = ((Number) staffExt.get("STAFF_ID")).longValue();
        staffExtKey = (String) staffExt.get("KEY");
    }

    private String baseUrl() {
        return "http://localhost:" + port + "/organization-resource-service/v1";
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

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void assertSuccess(JsonNode body) {
        assertEquals(SUCCESS_CODE, body.path("code").asText());
    }

    // ---------------------------------------------------------------- ChannelType

    @Test
    void channelType_getActiveById_returnsSuccess() throws Exception {
        JsonNode body = getJson("/channel-type/getActiveById/" + channelTypeId);
        assertSuccess(body);
        assertEquals(channelTypeId.longValue(), body.path("data").path("channelTypeId").asLong());
    }

    // ---------------------------------------------------------------- CustChannelTypeMapping

    @Test
    void custChannelTypeMapping_getAllActive_returnsSuccess() throws Exception {
        JsonNode body = getJson("/cust-channel-type-mapping/getAllActive");
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    @Test
    void custChannelTypeMapping_getByChannelTypeId_returnsSuccess() throws Exception {
        JsonNode body = getJson("/cust-channel-type-mapping/getByChannelTypeId/" + channelTypeId);
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    @Test
    void custChannelTypeMapping_getByCustTypeAndChannelType_returnsSuccess() throws Exception {
        var mapping = jdbcTemplate.queryForMap(
                "SELECT CUST_TYPE, CHANNEL_TYPE_ID FROM CUST_CHANNEL_TYPE_MAPPING WHERE STATUS='1' AND ROWNUM <= 1");
        String mapCustType = (String) mapping.get("CUST_TYPE");
        long mapChannelTypeId = ((Number) mapping.get("CHANNEL_TYPE_ID")).longValue();
        JsonNode body = getJson("/cust-channel-type-mapping/getByCustTypeAndChannelType?custType="
                + enc(mapCustType) + "&channelTypeId=" + mapChannelTypeId);
        assertSuccess(body);
    }

    // ---------------------------------------------------------------- CustType

    @Test
    void custType_findActiveByCustType_returnsSuccess() throws Exception {
        JsonNode body = getJson("/cust-type/findActiveByCustType/" + enc(custType));
        assertSuccess(body);
        assertEquals(custType, body.path("data").path("custType").asText());
    }

    @Test
    void custType_getAllActive_returnsSuccess() throws Exception {
        JsonNode body = getJson("/cust-type/getAllActive");
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    @Test
    void custType_getMappingChannelCustType_returnsSuccess() throws Exception {
        var mapping = jdbcTemplate.queryForMap(
                "SELECT CHANNEL_TYPE_ID, CT.GROUP_TYPE FROM CUST_CHANNEL_TYPE_MAPPING M "
                        + "JOIN CUST_TYPE CT ON CT.CUST_TYPE = M.CUST_TYPE "
                        + "WHERE M.STATUS='1' AND CT.GROUP_TYPE IS NOT NULL AND ROWNUM <= 1");
        long mapChannelTypeId = ((Number) mapping.get("CHANNEL_TYPE_ID")).longValue();
        String groupType = (String) mapping.get("GROUP_TYPE");
        JsonNode body = getJson("/cust-type/getMappingChannelCustType?channelTypeId="
                + mapChannelTypeId + "&groupType=" + enc(groupType));
        assertSuccess(body);
    }

    // ---------------------------------------------------------------- IdentityType

    @Test
    void identityType_getListIdentityType_returnsSuccess() throws Exception {
        // LƯU Ý: @RequestParam(required = false) custType nhưng IdentityTypeService.getListIdentityType
        // gọi custTypeService.findActiveByCustType(custType) vô điều kiện — nếu bỏ trống custType sẽ
        // luôn ném BCCS-ORGANIZATION-CUSTTYPE-0001 (bug nghiệp vụ có sẵn, ngoài phạm vi chuẩn hoá
        // OpenAPI lần này) — dùng custType thật để endpoint chạy đúng như cách nó thực sự được dùng.
        JsonNode body = getJson("/identity-type/getListIdentityType?custType=" + enc(custType));
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    @Test
    void identityType_findByIdType_returnsSuccess() throws Exception {
        JsonNode body = getJson("/identity-type/findByIdType?idType=" + enc(idType));
        assertSuccess(body);
        assertEquals(idType, body.path("data").path("idType").asText());
    }

    // ---------------------------------------------------------------- Shop

    @Test
    void shop_getActiveById_returnsSuccess() throws Exception {
        JsonNode body = getJson("/shop/getActiveById/" + shopId);
        assertSuccess(body);
        assertEquals(shopId.longValue(), body.path("data").path("shopId").asLong());
    }

    @Test
    void shop_getActiveByShopCode_returnsSuccess() throws Exception {
        JsonNode body = getJson("/shop/getActiveByShopCode/" + enc(shopCode));
        assertSuccess(body);
        assertEquals(shopCode, body.path("data").path("shopCode").asText());
    }

    @Test
    void shop_getStockCode_forShopOwner_returnsSuccess() throws Exception {
        JsonNode body = getJson("/shop/getStockCode?ownerId=" + shopId + "&ownerType=1");
        assertSuccess(body);
        assertEquals(shopCode, body.path("data").path("stockCode").asText());
    }

    @Test
    void shop_findActiveByShopIds_returnsSuccess() throws Exception {
        JsonNode body = postJson("/shop/findActiveByShopIds", "[" + shopId + "]");
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    // ---------------------------------------------------------------- Staff

    @Test
    void staff_getActiveById_returnsSuccess() throws Exception {
        JsonNode body = getJson("/staff/getActiveById/" + staffId);
        assertSuccess(body);
        assertEquals(staffId.longValue(), body.path("data").path("staffId").asLong());
    }

    @Test
    void staff_findActiveByStaffCode_returnsSuccess() throws Exception {
        JsonNode body = getJson("/staff/findActiveByStaffCode/" + enc(staffCode));
        assertSuccess(body);
        assertEquals(staffCode, body.path("data").path("staffCode").asText());
    }

    @Test
    void staff_getListStockByStaffCode_returnsSuccess() throws Exception {
        JsonNode body = getJson("/staff/getListStockByStaffCode/" + enc(staffCode));
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    @Test
    void staff_getStaffShopFullInfo_returnsSuccess() throws Exception {
        JsonNode body = getJson("/staff/getStaffShopFullInfo/" + enc(staffCode));
        assertSuccess(body);
        assertEquals(staffCode, body.path("data").path("staffCode").asText());
    }

    @Test
    void staff_getMappingChannelCustTypeV2_returnsSuccess() throws Exception {
        // data có thể null (SUCCESS) nếu nhân viên được chọn ngẫu nhiên không có mapping loại kênh -
        // loại khách hàng tương ứng — chỉ cần xác nhận endpoint trả về thành công (không lỗi 5xx),
        // không ép data phải là mảng khác null (khác các endpoint list khác luôn khởi tạo rỗng).
        JsonNode body = getJson("/staff/getMappingChannelCustTypeV2?staffCode=" + enc(staffCode));
        assertSuccess(body);
    }

    // ---------------------------------------------------------------- StaffExt

    @Test
    void staffExt_getByStaffId_returnsSuccess() throws Exception {
        JsonNode body = getJson("/staffext/getByStaffId/" + staffExtStaffId);
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    @Test
    void staffExt_getByStaffIdAndStatus_returnsSuccess() throws Exception {
        JsonNode body = getJson("/staffext/getByStaffIdAndStatus?staffId=" + staffExtStaffId + "&status=1");
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    @Test
    void staffExt_getStaffExtByStaffIDAndKey_returnsSuccess() throws Exception {
        JsonNode body = getJson("/staffext/getStaffExtByStaffIDAndKey?staffId=" + staffExtStaffId
                + "&key=" + enc(staffExtKey));
        assertSuccess(body);
    }
}
