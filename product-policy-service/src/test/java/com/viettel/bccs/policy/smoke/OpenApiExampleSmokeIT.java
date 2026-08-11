package com.viettel.bccs.policy.smoke;

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
 * Integration test THẬT cho product-policy-service — gọi trực tiếp từng endpoint bằng dữ liệu
 * thật lấy từ Oracle local (không mock), xác nhận HTTP 200 và code=SUCCESS.
 *
 * PHẠM VI: bao phủ các controller/endpoint TỰ ĐÓNG GÓI (chỉ truy vấn DB trực tiếp của chính
 * service này), CỘNG THÊM {@code MapActiveInfoQuerryController.getChanelTypeIdMapActiveInfo}
 * (chỉ phụ thuộc organization-resource-service qua Feign, không cần product-catalog-service —
 * chạy được với 2 service: policy + organization). KHÔNG bao gồm trong lần này (lý do nêu cụ
 * thể, không phải bỏ sót):
 *   - mapactiveinfo/* còn lại (MapActiveInfoProductController, MapActiveInfoValidateController,
 *     các endpoint khác của MapActiveInfoQuerryController): nhiều endpoint gọi cross-service qua
 *     Feign sang CẢ product-catalog-service lẫn organization-resource-service (RequestMbccs 169
 *     field) — cần cả 2 service kia chạy thật đồng thời, không chỉ Oracle; để lại cho lượt
 *     kiểm thử tích hợp đa-service riêng.
 *   - MapBusinessSkipDebtController: entity ánh xạ tới bảng MAP_BUSINESS_SKIP_DEBT nhưng bảng
 *     này KHÔNG TỒN TẠI trong schema Oracle local hiện tại (không có trong db-local/init/) — mọi
 *     endpoint của controller này sẽ lỗi ORA-00942 bất kể code đúng hay sai. Đây là gap hạ tầng
 *     DB cần bổ sung DDL riêng, ngoài phạm vi chuẩn hoá OpenAPI lần này.
 *   - ReasonController.getReasonFull (RequestMbccs 169 field, business logic phức tạp).
 *
 * {@code getChanelTypeIdMapActiveInfo} cần organization-resource-service đang chạy thật (mặc
 * định local port 8004, override qua {@code ORGANIZATION_SERVICE_URL}) — nếu service đó không
 * chạy, test method tương ứng sẽ fail rõ ràng (Feign timeout/connection refused) thay vì bị bỏ
 * qua âm thầm.
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

    private Long reasonId;
    private String reasonTelService;
    private String reasonPayType;
    private Long discountPromotionId;
    private Long discountPromotionCharUseId;
    private Long mappingReasonId;
    private Long mappingSaleServiceId;
    private Long refProdPackTypeId;
    private Long refProductPackageId;
    private Long refProductOfferTypeId;
    private Long chanelTypeStaffId;
    private Long chanelTypeShopId;

    @BeforeAll
    void loadRealSampleData() {
        var reason = jdbcTemplate.queryForMap(
                "SELECT REASON_ID, TEL_SERVICE, PAY_TYPE FROM REASON WHERE STATUS='1' AND ROWNUM <= 1");
        reasonId = ((Number) reason.get("REASON_ID")).longValue();
        reasonTelService = String.valueOf(reason.get("TEL_SERVICE"));
        reasonPayType = String.valueOf(reason.get("PAY_TYPE"));

        var promo = jdbcTemplate.queryForMap(
                "SELECT DISCOUNT_PROMOTION_ID FROM DISCOUNT_PROMOTION WHERE STATUS='1' AND ROWNUM <= 1");
        discountPromotionId = ((Number) promo.get("DISCOUNT_PROMOTION_ID")).longValue();

        discountPromotionCharUseId = jdbcTemplate.queryForObject(
                "SELECT DISCOUNT_PROMOTION_CHAR_USE_ID FROM DISCOUNT_PROMOTION_CHAR_USE WHERE ROWNUM <= 1", Long.class);

        mappingReasonId = jdbcTemplate.queryForObject(
                "SELECT REASON_ID FROM MAPPING WHERE ROWNUM <= 1", Long.class);
        mappingSaleServiceId = jdbcTemplate.queryForObject(
                "SELECT SALE_SERVICE_ID FROM MAPPING WHERE SALE_SERVICE_ID IS NOT NULL AND ROWNUM <= 1", Long.class);

        var refRow = jdbcTemplate.queryForMap(
                "SELECT PROD_PACK_TYPE_ID, PRODUCT_PACKAGE_ID, PRODUCT_OFFER_TYPE_ID FROM REF_PROD_PACK_PO_TYPE "
                        + "WHERE STATUS='1' AND ROWNUM <= 1");
        refProdPackTypeId = ((Number) refRow.get("PROD_PACK_TYPE_ID")).longValue();
        refProductPackageId = ((Number) refRow.get("PRODUCT_PACKAGE_ID")).longValue();
        refProductOfferTypeId = ((Number) refRow.get("PRODUCT_OFFER_TYPE_ID")).longValue();

        // STAFF không thuộc entity của policy-service, nhưng cùng schema Oracle chia sẻ với
        // organization-resource-service (xem [[bccs_local_dev_setup]]) — JdbcTemplate query thẳng
        // để lấy 1 staffId thật, active, đúng như getChanelTypeIdMapActiveInfo cần resolve qua Feign.
        var staff = jdbcTemplate.queryForMap(
                "SELECT STAFF_ID, SHOP_ID FROM STAFF WHERE STATUS='1' AND ROWNUM <= 1");
        chanelTypeStaffId = ((Number) staff.get("STAFF_ID")).longValue();
        chanelTypeShopId = ((Number) staff.get("SHOP_ID")).longValue();
    }

    private String baseUrl() {
        return "http://localhost:" + port + "/product-policy-service/v1";
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

    // ---------------------------------------------------------------- Reason

    @Test
    void reason_findById_returnsSuccess() throws Exception {
        JsonNode body = getJson("/reason/findById/" + reasonId);
        assertSuccess(body);
        assertEquals(reasonId.longValue(), body.path("data").path("reasonId").asLong());
    }

    @Test
    void reason_checkAttReason_returnsSuccess() throws Exception {
        JsonNode body = getJson("/reason/checkAttReason?reasonId=" + reasonId + "&attributeCode=TEST_ATTR");
        assertSuccess(body);
    }

    @Test
    void reason_getListReasonByActionCodeAndTelServiceForAudit_returnsSuccess() throws Exception {
        JsonNode body = getJson("/reason/getListReasonByActionCodeAndTelServiceForAudit?actionCode=NEW&telServiceId="
                + reasonTelService + "&payType=" + reasonPayType);
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    // ---------------------------------------------------------------- ReasonPause

    @Test
    void reasonPause_getReasonPauseByReasonId_returnsSuccess() throws Exception {
        // Bảng REASON_PAUSE hiện rỗng ở DB local — chỉ xác nhận endpoint chạy thành công với
        // danh sách rỗng, không assert có dữ liệu.
        JsonNode body = getJson("/reason-pause/getReasonPauseByReasonId/" + reasonId);
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    // ---------------------------------------------------------------- DiscountPromotion

    @Test
    void discountPromotion_findById_returnsSuccess() throws Exception {
        JsonNode body = getJson("/discountpromotion/findById/" + discountPromotionId);
        assertSuccess(body);
        assertEquals(discountPromotionId.longValue(), body.path("data").path("discountPromotionId").asLong());
    }

    @Test
    void discountPromotion_getPromotionList_returnsSuccess() throws Exception {
        JsonNode body = getJson("/discountpromotion/getPromotionList");
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    // ---------------------------------------------------------------- DiscountPromotionCharUse

    @Test
    void discountPromotionCharUse_findById_returnsSuccess() throws Exception {
        JsonNode body = getJson("/discount-promotion-char-use/findById/" + discountPromotionCharUseId);
        assertSuccess(body);
        assertEquals(discountPromotionCharUseId.longValue(), body.path("data").path("discountPromotionCharUseId").asLong());
    }

    // ---------------------------------------------------------------- FreeCamEquipment

    @Test
    void freeCamEquipment_checkReasonFreeCam_returnsSuccess() throws Exception {
        // checkReasonFreeCam khớp theo MAPPING.SALE_SERVICE_ID — dữ liệu thật hiện có thể không
        // có bản ghi FREE_CAM_EQUIPMENT tương ứng, chỉ xác nhận endpoint chạy thành công.
        JsonNode body = getJson("/free-cam-equipment/checkReasonFreeCam/" + mappingSaleServiceId);
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    // ---------------------------------------------------------------- Mapping

    @Test
    void mapping_findSaleServiceCodeByReason_returnsSuccess() throws Exception {
        JsonNode body = getJson("/mapping/findSaleServiceCodeByReason/" + mappingReasonId);
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    @Test
    void mapping_getMappingReasonProductOfferPrice_returnsSuccess() throws Exception {
        JsonNode body = getJson("/mapping/getMappingReasonProductOfferPrice/" + mappingSaleServiceId);
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    // ---------------------------------------------------------------- RefProdPackPoType

    @Test
    void refProdPackPoType_findAllActive_returnsSuccess() throws Exception {
        JsonNode body = getJson("/ref-prod-pack-po-type/findAllActive");
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    @Test
    void refProdPackPoType_findByProductPackageId_returnsSuccess() throws Exception {
        JsonNode body = getJson("/ref-prod-pack-po-type/findByProductPackageId/" + refProductPackageId);
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
        assertTrue(body.path("data").size() > 0);
    }

    @Test
    void refProdPackPoType_findByProductOfferTypeId_returnsSuccess() throws Exception {
        JsonNode body = getJson("/ref-prod-pack-po-type/findByProductOfferTypeId/" + refProductOfferTypeId);
        assertSuccess(body);
        assertTrue(body.path("data").isArray());
    }

    // ---------------------------------------------------------------- MapActiveInfoQuerry

    @Test
    void mapActiveInfoQuerry_getChanelTypeIdMapActiveInfo_returnsSuccess() throws Exception {
        // Cần organization-resource-service chạy thật (Feign) — xem Javadoc đầu class.
        String requestBody = "{\"staffId\": " + chanelTypeStaffId + ", \"shopId\": " + chanelTypeShopId + "}";
        JsonNode body = postJson("/map-active-info/getChanelTypeIdMapActiveInfo", requestBody);
        assertSuccess(body);
    }
}
