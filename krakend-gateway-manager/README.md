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
│   └── src/main/java/com/bccs/gatewaymanager/
│       ├── entity/           # EndpointConfig, BackendStep, FieldMapping, UpstreamService
│       ├── engine/           # CompositeOrchestratorEngine, UpstreamHttpExecutor, JsonPathUtil...
│       ├── controller/       # EndpointController, UpstreamServiceController,
│       │                     # ConfigController, DynamicDispatcherController (catch-all "/**")
│       ├── service/          # EndpointService, UpstreamServiceService, *RegistryCache
│       ├── cache/            # GatewayCacheService (Redis cache-aside)
│       └── dto/, repository/, exception/, config/
└── frontend/                 # Angular 18 standalone
    └── src/app/pages/
        ├── endpoint-list/, endpoint-form/   # khai báo Endpoint + Field Mapping
        ├── upstream-services/                # đăng ký backend thật (host/timeout/resilience)
        └── dependency-graph/                 # sơ đồ endpoint nào gọi endpoint nào
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

## 7. Giới hạn hiện tại

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
