# Gateway Manager — Dynamic Composite API Orchestrator

Web UI + engine tự thực thi cho việc khai báo API composite (gọi nhiều backend,
chain dữ liệu giữa các bước) **hoàn toàn bằng cấu hình, không viết code cho
từng API mới**. Không phụ thuộc KrakenD/Gravitee hay bất kỳ gateway bên thứ 3
nào — chính service Spring Boot này vừa là nơi quản trị cấu hình (Control
Plane) vừa là gateway thực thi traffic thật (Data Plane).

## 1. Kiến trúc tổng thể

```
Browser (UI, port 4200) ──/api/**──► frontend (Angular/Nginx) ──/api/**──► backend :8080
App/Client (traffic thật) ─────────────────────────────────────────────► backend :8080
                                                                              │
                                                        ┌─────────────────────┼─────────────────────┐
                                                        │                     │                     │
                                                  /api/**                  /**                    (JDBC)
                                                  Control Plane      Data Plane                     │
                                                  (CRUD Endpoint/    (DynamicDispatcherController    v
                                                   Upstream)          + CompositeOrchestratorEngine) Oracle
                                                                              │                (CHUNG voi BCCS,
                                                                              │                 schema BCCS_PRODUCT)
                                                                    ┌─────────┴─────────┐
                                                                    │                   │
                                                              Redis (cache)      Resilience4j
                                                              (GET theo Upstream)  (circuit breaker/
                                                                    │              retry/bulkhead
                                                                    v              theo Upstream)
                                                              Upstream Service thật
                                                              (organization-resource-service,
                                                               product-catalog-service, ...)
```

### Không còn bước "Deploy" theo nghĩa cũ

Trước đây (KrakenD/Gravitee) cần ghi file config + restart container để áp
dụng thay đổi. Giờ **Lưu = có hiệu lực ngay** cho lần request tiếp theo:
`EndpointRegistryCache`/`UpstreamRegistryCache` (cache trong-process) tự nạp
lại ngay sau mỗi lần tạo/sửa/xoá qua Control Plane API. Nút "Validate toàn bộ"
trên UI chỉ còn ý nghĩa kiểm tra vòng lặp phụ thuộc + nạp lại thủ công (tuỳ
chọn), không phải bước bắt buộc.

## 2. Cấu trúc thư mục

```
krakend-gateway-manager/
├── docker-compose.yml       # redis + backend + frontend (KHONG con postgres/krakend)
├── backend/                 # Control Plane + Gateway thuc thi (Spring Boot)
│   ├── src/main/resources/db/ddl-gateway-manager.sql  # DDL 8 bang (ban chup, xem muc 8)
│   └── src/main/java/com/bccs/gatewaymanager/
│       ├── entity/           # EndpointConfig, BackendStep, FieldMapping, UpstreamService,
│       │                     # EndpointConfigVersion (lich su phien ban)
│       ├── engine/           # CompositeOrchestratorEngine, UpstreamHttpExecutor, JsonPathUtil...
│       ├── controller/       # EndpointController, UpstreamServiceController,
│       │                     # ConfigController, DynamicDispatcherController (catch-all "/**"),
│       │                     # LogSearchController (tra cuu log - xem muc 7)
│       ├── service/          # EndpointService, UpstreamServiceService, *RegistryCache
│       ├── cache/            # GatewayCacheService (Redis cache-aside)
│       ├── audit/            # AuditLogService/LogSearchService - ghi/doc log vao Elasticsearch
│       │                     # (gwm-requests-*/gwm-hops-*, xem muc 7)
│       └── dto/, repository/, exception/, config/  # config/ElasticsearchConfig, ApiKeyAuthFilter...
└── frontend/                 # Angular 18 standalone
    └── src/app/pages/
        ├── endpoint-list/, endpoint-form/   # khai báo Endpoint + Field Mapping
        ├── endpoint-canvas/                  # khai báo Endpoint kiểu kéo-thả trực quan
        ├── endpoint-versions/                 # lịch sử phiên bản + rollback
        ├── upstream-services/, upstream-health/  # đăng ký + dashboard sức khoẻ backend thật
        └── log-search/                        # Tra cứu log request/hop (xem mục 7)
```

## 3. Chạy hệ thống

Yêu cầu: Oracle + Redis chung với BCCS đã chạy (xem `../db-local/README.md`).

```bash
cd krakend-gateway-manager
docker compose up -d --build
```

- Web UI: http://localhost:4200
- Gateway thực thi (client thật gọi vào): http://localhost:8080
- Control Plane API: http://localhost:8080/api

## 4. 2 khái niệm nền tảng

### `UpstreamService` — đăng ký backend thật, dùng 1 lần

Mỗi backend thật (`organization-resource-service`, `product-catalog-service`...)
đăng ký 1 lần qua trang **Upstream Services**: `baseHost`, timeout, circuit
breaker (bật/ngưỡng lỗi), retry (chỉ nên bật nếu backend chỉ đọc dữ liệu).
Cache Redis **không** cấu hình ở đây — xem `BackendStep` bên dưới.

### `EndpointConfig` → nhiều `BackendStep` + nhiều `FieldMapping`

- Mỗi `BackendStep` = 1 lần gọi ra `UpstreamService` đã đăng ký, theo thứ tự
  `stepOrder`. `target` để "bóc vỏ" field (vd `data` — chuẩn `StandardResponse`
  của BCCS). `forwardOriginalBody` = lấy nguyên body client làm nền cho body
  gửi đi step này. `cacheEnabled`/`cacheTtlSeconds` (chỉ GET) cache **riêng
  cho step này** — 1 Upstream có thể bị nhiều step gọi tới nhiều hàm/path
  khác nhau, không phải hàm nào cũng nên cache.
- Mỗi `FieldMapping` = "lấy 1 giá trị từ đâu, bơm vào đâu":
  - **Nguồn** (`sourceType`): `STEP_RESPONSE` (response step trước, hỗ trợ
    dot-notation lồng nhau vd `shop.channelTypeId`), `REQUEST_BODY` (đọc thẳng
    field từ body client), `STEP_RESPONSE_ARRAY_AGGREGATE` (gộp 1 field của
    TỪNG phần tử trong 1 mảng response thành mảng mới).
  - **Đích** (`targetType`): `PATH`/`QUERY`/`HEADER`, hoặc `BODY_FIELD` (thêm
    field vào JSON body gửi đi của step đích — riêng nếu đặt tên field đích là
    `$body` thì giá trị đó **thay thế toàn bộ body**, dùng khi backend nhận
    thẳng 1 mảng/giá trị làm body, không bọc trong field nào).

## 5. Ví dụ thật đã kiểm chứng

### a) Chain 2 bước cùng 1 service — `/v1/staff/full-info/{staffId}`

```
Step 1: GET organization-resource-service /v1/staff/findActiveByStaffCode/{staffCode}
         (staffCode lấy tự động từ path-param cùng tên của endpoint), target=data
Step 2: GET organization-resource-service /v1/channel-type/getActiveById/{channelTypeId}
         (channelTypeId lấy từ response Step 1 qua FieldMapping PATH), target=data
```

### b) Gộp mảng + body thô — `/v1/shops-from-stock-mapping`

```
Step 1: GET stock-channel-mapping/findActive → trả N record
FieldMapping: STEP_RESPONSE_ARRAY_AGGREGATE, sourceArrayField="data",
              sourceElementField="stockShopId" → BODY_FIELD, targetParamName="$body"
Step 2: POST shop/findActiveByShopIds nhận THẲNG mảng ID làm body (không bọc field)
```

### c) Forward body client + merge qua 2 service khác nhau — `/v1/staff-enrich`

```
Client gửi: {"staffCode":"...", "ruleType":"..."}
Step 1+2: organization-resource-service, chain staffCode → channelTypeId → code
Step 3: product-catalog-service /v1/product/checkProductAttByRuleType
        BODY_FIELD productCode ← response Step 2 (code)
        BODY_FIELD ruleType ← body client (REQUEST_BODY)
```

## 6. Resilience & hiệu năng

- **Redis cache-aside**: chỉ cache GET khi `BackendStep.cacheEnabled=true`
  (cấu hình riêng theo từng step, không phải theo Upstream — 1 Upstream có
  thể bị nhiều step gọi tới nhiều hàm khác nhau), key = tên Upstream + URL +
  query đã resolve, TTL (từ `BackendStep.cacheTtlSeconds`) jitter ±15% chống
  cache stampede.
- **Resilience4j** (circuit breaker/retry/bulkhead): tạo động theo **tên**
  từng Upstream Service tại runtime (không dùng annotation tĩnh vì backend
  đích được chọn động theo cấu hình DB). Xem trạng thái qua
  `GET /actuator/circuitbreakers`.
- Chi tiết thiết kế đầy đủ (timeout budget, idempotency, connection pool...):
  xem lịch sử trao đổi trong phiên làm việc — engine được thiết kế theo đúng
  checklist "production-grade composition service" (API Composition pattern,
  Circuit Breaker pattern, Cache-Aside pattern).

## 7. Giám sát: audit log (Elasticsearch) + Elastic APM + trang Tra cứu Log

Dùng **chung** Elasticsearch/Kibana đã có sẵn của hạ tầng BCCS
(`db-local/docker-compose.yml`, container `bccs-elasticsearch`/`bccs-kibana`)
— không dựng ES riêng. **Không dùng Logstash/Filebeat**: log có cấu trúc được
ghi **thẳng vào Elasticsearch bằng code** (`audit/AuditLogService`, dùng
`co.elastic.clients:elasticsearch-java`), tại đúng 2 điểm:
`DynamicDispatcherController.dispatch()` (tổng 1 request client) và
`UpstreamHttpExecutor.call()` (từng hop gọi 1 Upstream).

### 2 index mới (daily index, UTC, KHÔNG lẫn với `bccs-logs-*` của K8s)

| Index | 1 document = | Field chính |
|---|---|---|
| `gwm-requests-*` | 1 request client thật gọi vào gateway | `requestId`, `timestamp`, `endpointName`, `clientMethod`/`clientPath`, `status` (SUCCESS/ERROR), `httpStatus`, `errorCode`/`errorMessage`, `durationMs`, `requestBody` (cắt 8KB), `traceId` (link sang APM) |
| `gwm-hops-*` | 1 lần `BackendStep` gọi ra Upstream thật | `requestId` (join key), `stepOrder`, `stepName`, `upstreamName`, `resolvedUrl`, `requestBody`/`responseBody` (cắt 8KB), `responseStatus`, `durationMs`, `cacheHit`, `success` |

**Fail-open**: `AuditLogService` ghi qua hàng đợi trong bộ nhớ (bounded, 5000
phần tử) + flush định kỳ 1 giây bằng Bulk API — lỗi Elasticsearch (mất kết
nối, index lỗi...) chỉ log cảnh báo rồi bỏ qua, **không bao giờ làm hỏng
traffic thật**. Ngược lại, API tra cứu (`LogSearchService`, đọc) throw lỗi rõ
ràng nếu ES không trả lời được — người dùng đang chủ động bấm "Tìm kiếm" nên
cần biết ngay thay vì hiểu nhầm "không có log nào".

### Bật/tắt + cấu hình

```yaml
gatewaymanager.audit.enabled: ${GATEWAY_AUDIT_ENABLED:true}
gatewaymanager.audit.elasticsearch.host: ${GATEWAY_AUDIT_ES_HOST:localhost}
gatewaymanager.audit.elasticsearch.port: ${GATEWAY_AUDIT_ES_PORT:9200}
```

### Elastic APM

Java agent (`elastic-apm-agent-1.56.0.jar`, tải qua `Dockerfile` lúc build
image, gắn qua `-javaagent`) tự động instrument `RestTemplate` — không cần
sửa code thêm. Trỏ tới `apm-server` cũng đặt tại `db-local/docker-compose.yml`
(cạnh Elasticsearch/Kibana, port `8200`). `traceId` của mỗi request được đọc
qua `co.elastic.apm.api.ElasticApm` và lưu vào `gwm-requests-*` — đối chiếu
1-1 sang trace/span thật trên Kibana APM UI.

### Trang UI "Tra cứu Log" (`/logs`)

Lọc theo khoảng thời gian, trạng thái (SUCCESS/ERROR), endpoint path, nội
dung request body — bấm 1 dòng để xem **waterfall từng hop** (request/response
mỗi bước, cache hit, lỗi). API: `GET /api/logs/requests`, `GET
/api/logs/requests/{requestId}/hops` (nằm dưới `/api/**` nên tự động được
`ApiKeyAuthFilter` bảo vệ như mọi API Control Plane khác).

> **Chưa làm (P2, không chặn dùng V1)**: chưa redact PII (idNo, tel, email...)
> trước khi ghi vào ES, chưa có chính sách ILM tự xoá index cũ.

## 8. Giới hạn hiện tại

- Các step luôn thực thi **tuần tự trong 1 vòng lặp Java** (chưa có gọi song
  song thật cho các step độc lập không phụ thuộc dữ liệu lẫn nhau) — tối ưu
  hoá khả thi sau này qua `CompletableFuture`, không chặn việc dùng hiện tại.
- Giá trị gộp mảng (`STEP_RESPONSE_ARRAY_AGGREGATE`) hiện luôn là chuỗi JSON
  (không giữ nguyên kiểu số/boolean gốc) — hoạt động tốt với hầu hết backend
  nhờ Jackson tự ép kiểu lỏng, nhưng có thể cần sửa nếu gặp backend nghiêm
  ngặt hơn.
- Đổi cấu hình resilience (timeout/circuit-breaker) của 1 Upstream sau khi đã
  có request đầu tiên chưa áp dụng ngay (registry Resilience4j giữ config lúc
  khởi tạo lần đầu) — cần cải tiến nếu cần đổi động không restart.
- Query param của client **chưa** dùng được làm nguồn `FieldMapping` (chỉ mới
  hỗ trợ `pathVariables`/`requestBody`) — xem comment trong
  `service/OpenApiGeneratorService.java`.
- Không có auth theo từng Endpoint ở Data Plane (chỉ Control Plane `/api/**`
  có `X-Gateway-Admin-Key`) — quyết định đã chốt, không phải thiếu sót.
- Schema DB dùng `ddl-auto: update` (tự tạo/cập nhật bảng) — bản chụp DDL 8
  bảng để dựng thủ công trên máy khác (nếu không muốn dựa vào auto-DDL): xem
  [`backend/src/main/resources/db/ddl-gateway-manager.sql`](backend/src/main/resources/db/ddl-gateway-manager.sql).
