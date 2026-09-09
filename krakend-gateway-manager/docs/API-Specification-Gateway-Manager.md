---
title: "API Specification — BCCS Gateway Manager"
---

# API SPECIFICATION
# Hệ thống Gateway Manager (BCCS Composite API Gateway)

| | |
|---|---|
| **Mã tài liệu** | API-GWM-001 |
| **Phiên bản** | 2.0 — thêm `/api/teams` (Quản lý đội), 2 loại `X-Gateway-Admin-Key` (platform-admin/từng đội), `path`/`name` chỉ duy nhất TRONG 1 đội (2026-09-09) |
| **Ngày phát hành** | 2026-09-06 |
| **Trạng thái** | Draft |
| **Định dạng dữ liệu** | JSON (`application/json`) |
| **Tài liệu liên quan** | FDS-GWM-001 (mục 4), DBDD-GWM-001 |

---

## 1. Tổng quan & Quy ước chung

Hệ thống có **2 nhóm API tách biệt hoàn toàn** về mục đích và cơ chế xác
thực:

| Nhóm | Tiền tố | Xác thực | Mục đích |
|---|---|---|---|
| **Control Plane** | `/api/**` | Header `X-Gateway-Admin-Key` | Quản trị cấu hình, tra cứu log, xem trước |
| **Data Plane** | Tuỳ ý (do người dùng khai báo khi tạo Endpoint) | Không có ở tầng gateway | Traffic nghiệp vụ thật của client |

Mỗi service tự sinh thêm 1 tài liệu **OpenAPI 3.0.3 riêng cho từng Endpoint**
qua `GET /api/endpoints/{id}/openapi` (xem mục 5) — tài liệu này mô tả các API
**quản trị Control Plane cố định** của chính nền tảng Gateway Manager, khác
với OpenAPI tự sinh (mô tả 1 Endpoint composite CỤ THỂ do người dùng khai
báo).

**Quy ước chung**:
- Toàn bộ request/response Control Plane dùng `Content-Type: application/json`.
- Trường thời gian dùng định dạng ISO-8601 (`Instant`, UTC).
- ID là chuỗi UUID (`String`).
- Danh sách trong response luôn là mảng JSON (không bọc thêm object phân
  trang trừ API tra cứu log — xem mục 4).

---

## 2. Xác thực (Authentication)

```
X-Gateway-Admin-Key: <api-key>
```

Bắt buộc cho MỌI request tới `/api/**` (Control Plane) — thiếu hoặc sai giá
trị → HTTP 401, không thực thi bất kỳ logic nghiệp vụ nào (chặn ở tầng
Servlet Filter, trước khi vào Controller). **2 LOẠI key khác nhau** (từ
2026-09, xem `SAD-Gateway-Manager.md` ADR-07):

| Loại key | Dùng cho | Cấu hình |
|---|---|---|
| **platform-admin key** | CHỈ `/api/teams/**` (mục 3.5) | `GATEWAY_ADMIN_API_KEY` — đội nền tảng giữ |
| **api_key riêng của từng đội** | MỌI API `/api/**` còn lại (mục 3.1–3.4) — endpoint tự động chỉ trả về/chỉ sửa được dữ liệu của ĐÚNG đội sở hữu key | Sinh tự động trong bảng `gwm_team` khi tạo đội qua mục 3.5, hiển thị **đúng 1 lần** |

Dùng nhầm loại key (vd platform-admin key gọi `/api/endpoints`) → HTTP 401
giống hệt key sai hoàn toàn — 2 loại key không thể hoán đổi cho nhau.

Data Plane (traffic thật qua Endpoint đã khai báo) KHÔNG áp dụng cơ chế xác
thực này — nếu 1 Endpoint composite cần forward xác thực từ client sang
Upstream Service, khai báo qua Field Mapping (`targetType=HEADER`, nguồn tuỳ
chọn).

---

## 3. Control Plane — Danh sách API đầy đủ

### 3.1. Upstream Service — `/api/upstreams`

| Method | Path | Mô tả | Request body | Response |
|---|---|---|---|---|
| GET | `/api/upstreams` | Danh sách toàn bộ Upstream Service | — | `UpstreamServiceDto[]` |
| POST | `/api/upstreams` | Tạo mới | `UpstreamServiceRequestDto` | `UpstreamServiceDto` (201) |
| PUT | `/api/upstreams/{id}` | Sửa | `UpstreamServiceRequestDto` | `UpstreamServiceDto` |
| DELETE | `/api/upstreams/{id}` | Xoá | — | 204, hoặc lỗi `GW-UP-INUSE` nếu còn Step tham chiếu |
| GET | `/api/upstreams/health` | Sức khoẻ tất cả Upstream | — | `UpstreamHealthDto[]` |

**`UpstreamServiceRequestDto` / `UpstreamServiceDto`**:

| Field | Kiểu | Bắt buộc | Ghi chú |
|---|---|---|---|
| id | String | — | Chỉ có ở response |
| name | String | Có | Duy nhất TRONG đội của key gọi (2 đội khác nhau được phép trùng tên, xem mục 2) |
| description | String | Không | |
| baseHost | String | Có | vd `http://10.x.x.x:8045` |
| connectTimeoutMs, readTimeoutMs | int | Có | |
| circuitBreakerEnabled | boolean | Có | |
| failureRateThreshold | int | Có | % |
| retryEnabled | boolean | Có | |
| maxConcurrentCalls | int | Không (mặc định 20) | Bulkhead |
| maxWaitDurationMs | int | Không (mặc định 500) | Bulkhead |

**`UpstreamHealthDto`** (chỉ đọc): `id, name, baseHost, circuitBreakerEnabled,
circuitState, failureRatePercent, bufferedCalls, cacheHits, cacheMisses,
cacheHitRate` (`cacheHitRate=-1` nghĩa là chưa có dữ liệu, chưa từng gọi qua
step nào bật cache).

### 3.2. Endpoint — `/api/endpoints`

| Method | Path | Mô tả |
|---|---|---|
| GET | `/api/endpoints?q=` | Danh sách, tuỳ chọn tìm kiếm |
| GET | `/api/endpoints/{id}` | Chi tiết |
| POST | `/api/endpoints` | Tạo mới |
| PUT | `/api/endpoints/{id}` | Sửa |
| DELETE | `/api/endpoints/{id}` | Xoá (kèm lịch sử phiên bản) |
| GET | `/api/endpoints/dependency-graph` | Sơ đồ endpoint gọi lẫn nhau qua chính gateway |
| GET | `/api/endpoints/{id}/versions` | Lịch sử phiên bản |
| GET | `/api/endpoints/{id}/versions/{versionId}` | Chi tiết 1 phiên bản |
| POST | `/api/endpoints/{id}/versions/{versionId}/rollback` | Khôi phục |
| POST | `/api/endpoints/{id}/try` | "Thử ngay" (đã lưu) |
| POST | `/api/endpoints/try-adhoc` | "Thử nhanh" (chưa lưu) |
| GET | `/api/endpoints/{id}/openapi` | Tự sinh OpenAPI |

**`EndpointRequestDto`** (POST/PUT):

| Field | Kiểu | Bắt buộc | Ghi chú |
|---|---|---|---|
| name | String | Có | |
| description | String | Không | |
| path | String | Có | Phải bắt đầu bằng `/`, duy nhất TRONG đội của key gọi |
| method | enum | Có | GET/POST/PUT/DELETE/PATCH |
| sequential | boolean | Có | |
| outputEncoding | String | Không | Mặc định `json` |
| steps | `BackendStepDto[]` | Có (≥1) | |
| mappings | `FieldMappingDto[]` | Không | |
| idempotencyEnabled | boolean | Không | Mặc định false |
| idempotencyTtlSeconds | Integer | Không | Mặc định 86400 |
| parallelExecution | boolean | Không | Mặc định false |
| responseCacheEnabled | boolean | Không | Mặc định false |
| responseCacheTtlSeconds | Integer | Không | Mặc định 300 |

`EndpointResponseDto` giống hệt trên, cộng thêm `id, createdAt, updatedAt`.

**`BackendStepDto`**: `id, stepOrder, name, method, urlPattern,
upstreamServiceId, upstreamServiceName (chỉ đọc), forwardOriginalBody,
cacheEnabled, cacheTtlSeconds, group, target, allowFields[], denyFields[],
fieldRenameMapping{}, canvasX, canvasY, connectTimeoutMs, readTimeoutMs,
conditionSourceType, conditionSourceStepOrder, conditionSourceField,
conditionOperator, conditionExpectedValue, nextStepOrderIfTrue,
nextStepOrderIfFalse, onErrorStepOrder, parallelGroup,
compensationUpstreamServiceId, compensationMethod, compensationUrlPattern`.

**`FieldMappingDto`**: `id, sourceType, sourceStepOrder, sourceField,
sourceArrayField, sourceElementField, constantValue, targetStepOrder,
targetType, targetParamName, mappingOrder, targetContext`.

**`TryResultDto`** (response của cả `/try` và `/try-adhoc`, LUÔN HTTP 200 cho
lỗi xảy ra trong lúc thực thi/validate):

```json
{
  "success": true,
  "result": { },
  "errorCode": null,
  "errorMessage": null,
  "hops": [
    {
      "stepOrder": 1, "stepName": "string", "upstreamName": "string",
      "method": "GET", "resolvedUrl": "string",
      "requestBody": null, "requestBodyTruncated": false,
      "responseStatus": 200, "responseBody": "string", "responseBodyTruncated": false,
      "durationMs": 0, "cacheHit": false, "success": true, "errorMessage": null
    }
  ]
}
```

Request body của `/try`: `{ "pathVariables": {}, "queryParams": {}, "body": null }`.
Request body của `/try-adhoc`: `{ "endpoint": <EndpointRequestDto>, "pathVariables": {}, "queryParams": {}, "body": null }`.

### 3.3. Cấu hình hệ thống — `/api/config`

| Method | Path | Mô tả |
|---|---|---|
| GET | `/api/config/export` | Xuất toàn bộ cấu hình **CỦA ĐÚNG ĐỘI SỞ HỮU key gọi** (tự động lọc theo `team_code`) — cũng chính là API mà `RemoteConfigSyncService` của Data Plane từng đội gọi định kỳ để tự đồng bộ, không có API riêng nào khác cho việc đó |
| POST | `/api/config/import` | Nhập cấu hình (UPSERT, luôn gán vào đội của key gọi) |
| POST | `/api/config/deploy` | Chỉ còn validate vòng lặp phụ thuộc (từ 2026-09, KHÔNG còn reload registry cache — cache đó đã chuyển hẳn sang Data Plane, xem `SAD-Gateway-Manager.md` ADR-07) |
| GET | `/api/config/gateway-info` | Thông tin gateway (port, host alias) |

### 3.4. Tra cứu Log — `/api/logs`

| Method | Path | Query param | Mô tả |
|---|---|---|---|
| GET | `/api/logs/requests` | `from, to` (Instant, ISO-8601), `status`, `endpointPath`, `bodyContains`, `page` (mặc định 0), `size` (mặc định 20) | Tìm kiếm request, có phân trang |
| GET | `/api/logs/requests/{requestId}/hops` | — | Chi tiết từng hop của 1 request |

### 3.5. Quản lý đội — `/api/teams` (mới, 2026-09 — CHỈ nhận platform-admin key, xem mục 2)

| Method | Path | Mô tả | Response |
|---|---|---|---|
| GET | `/api/teams` | Danh sách đội (KHÔNG kèm `apiKey`) | `TeamDto[]` |
| POST | `/api/teams` | Tạo đội mới, tự sinh `apiKey` ngẫu nhiên 256-bit | `TeamCreatedDto` (kèm `apiKey` plaintext — **CHỈ hiện đúng 1 lần trong response này**) |
| DELETE | `/api/teams/{teamCode}` | Xoá đội (KHÔNG xoá dữ liệu Endpoint/Upstream đã có của đội đó — chỉ đội đó không còn đăng nhập được nữa) | 204 |

**`TeamCreateRequestDto`** (POST): `teamCode` (String, bắt buộc, chỉ gồm chữ/số/gạch ngang/gạch dưới, 2-50 ký tự, PHẢI chưa tồn tại), `teamName` (String, bắt buộc).

**`TeamDto`** (GET, KHÔNG có `apiKey`): `teamCode, teamName, createdAt`.

**`TeamCreatedDto`** (response CỦA POST, DUY NHẤT có `apiKey`): `teamCode, teamName, apiKey, createdAt`.

`GET /api/logs/requests` trả về:
```json
{ "items": [ /* RequestAuditEvent[] */ ], "total": 0, "page": 0, "size": 20 }
```

`RequestAuditEvent`: `requestId, timestamp, endpointId, endpointName,
clientMethod, clientPath, status (SUCCESS|ERROR), httpStatus, errorCode,
errorMessage, durationMs, requestBody, requestBodyTruncated, traceId`.

`HopAuditEvent`: `requestId, stepOrder, stepName, upstreamName, method,
resolvedUrl, requestBody, requestBodyTruncated, responseStatus, responseBody,
responseBodyTruncated, durationMs, cacheHit, success, errorMessage, timestamp`.

---

## 4. Data Plane — Hợp đồng chung cho Endpoint đã khai báo

- Đường dẫn/phương thức HTTP: theo đúng `path`/`method` người dùng đã khai
  báo trong `EndpointConfig` (KHÔNG có tiền tố cố định).
- Path variable trong `path` (dạng `{token}`) được trích từ URL client gọi
  vào, dùng làm nguồn cho Field Mapping hoặc truyền thẳng cho step trùng tên
  token.
- Query string và body của client được đưa vào làm nguồn cho Field Mapping
  (`sourceType=QUERY_PARAM`/`REQUEST_BODY`).
- Response trả về client là kết quả cuối cùng sau khi engine điều phối xong
  toàn bộ Backend Step (xem FDS-GWM-001 mục 5) — hình dạng response phụ
  thuộc HOÀN TOÀN vào cấu hình Endpoint, không cố định theo 1 schema chung.
- Không yêu cầu header xác thực từ tầng gateway (xem mục 2).

---

## 5. OpenAPI tự sinh cho từng Endpoint

`GET /api/endpoints/{id}/openapi` trả về 1 tài liệu OpenAPI 3.0.3 (JSON) mô
tả RIÊNG 1 Endpoint composite đã khai báo — suy luận path parameter/request
body ở mức tốt nhất có thể (best-effort) từ Field Mapping hiện có; response
body luôn là `type: object` chung (không suy luận được cấu trúc chính xác vì
phụ thuộc dữ liệu THẬT trả về từ Upstream Service lúc chạy).

---

## 6. Xử lý lỗi & Mã lỗi (Error Handling)

### 6.1. Khuôn dạng lỗi chuẩn

Phần lớn lỗi Control Plane trả về (`ErrorResponse`):

```json
{
  "errorCode": "GW-003",
  "message": "Mo ta loi bang tieng Viet, cu the nguyen nhan",
  "timestamp": "2026-09-06T10:00:00Z"
}
```

**Ngoại lệ cần lưu ý**: khi Data Plane không khớp được bất kỳ Endpoint nào
với path/method client gọi vào, response lỗi có khuôn dạng KHÁC (không phải
`ErrorResponse`):
```json
{ "error": "GW-NOT-FOUND", "message": "Khong co endpoint nao khop voi GET /..." }
```

### 6.2. Bảng mã lỗi

| Mã lỗi | HTTP Status | Nhóm | Ý nghĩa |
|---|---|---|---|
| `GW-CIRCUIT-OPEN` | 503 | Resilience | Circuit breaker đang mở cho Upstream liên quan |
| `GW-BULKHEAD-FULL` | 503 | Resilience | Vượt giới hạn số lệnh gọi đồng thời tới Upstream |
| `GW-UPSTREAM-5XX` | 502 | Upstream | Upstream Service trả về lỗi HTTP 5xx |
| `GW-UPSTREAM-4XX` | 502 | Upstream | Upstream Service trả về lỗi HTTP 4xx |
| `GW-UPSTREAM-TIMEOUT` | 504 | Upstream | Upstream không phản hồi kịp (timeout/connection refused) |
| `SYSTEM_ERROR` | 500 | Hệ thống | Lỗi hạ tầng nội bộ (`SystemException`) |
| `VALIDATION_ERROR` | 400 | Đầu vào | Vi phạm ràng buộc `@Valid` trên request DTO |
| `INVALID_REQUEST_BODY` | 400 | Đầu vào | Body không phải JSON hợp lệ |
| `MISSING_PARAMETER` | 400 | Đầu vào | Thiếu query/path param bắt buộc |
| `INVALID_PARAMETER_TYPE` | 400 | Đầu vào | Sai kiểu dữ liệu tham số (vd `from` không parse được thành `Instant`) |
| `UNKNOWN_ERROR` | 500 | Hệ thống | Lỗi không xác định, chưa có handler riêng |
| `GW-NOT-FOUND` | 404 | Data Plane | Không khớp Endpoint nào với path/method client gọi (khuôn dạng response khác — xem §6.1) |
| `GW-001` | 400 | Cấu hình Endpoint | Path đã tồn tại, hoặc trùng tiền tố dành riêng `/api`/`/actuator` |
| `GW-002` | 400 | Cấu hình Endpoint | `stepOrder` trùng lặp giữa các Backend Step |
| `GW-003` | 400 | Cấu hình Endpoint | Nhóm lỗi validate cấu hình đa dạng: rẽ nhánh (`nextStepOrderIfTrue/False` sai target), `parallelExecution` mâu thuẫn `sequential`, wave (`parallelGroup`) cấu hình sai, response-cache bật trên endpoint/step không phải GET/POST, bù trừ thiếu trường... — xem thông điệp `message` cụ thể để biết chính xác nguyên nhân |
| `GW-404` | 400 | Cấu hình Endpoint | Không tìm thấy Endpoint theo id (lưu ý: trả 400, không phải 404, do đi qua `BusinessException` handler dùng chung) |
| `GW-CYCLE` | 400 | Cấu hình Endpoint | Phát hiện vòng lặp phụ thuộc giữa các Endpoint (endpoint A gọi ngược lại B, B gọi lại A qua chính gateway) |
| `GW-BRANCH-LOOP`, `GW-BRANCH-TARGET-404`, `GW-BRANCH-CYCLE` | 400 | Cấu hình rẽ nhánh | Rẽ nhánh trỏ tới step không tồn tại, hoặc tạo vòng lặp vô hạn giữa các step |
| `GW-CONDITION-NOT-NUMERIC` | 400 | Runtime (Data Plane) | Giá trị so sánh của toán tử số (GREATER_THAN...) không parse được thành số |
| `GW-UPSTREAM-404` | 400 | Cấu hình Endpoint | Step (chính hoặc bù trừ) tham chiếu Upstream Service không tồn tại |
| `GW-PATH-TOKEN-MISSING` | 400 | Runtime (Data Plane) | Thiếu giá trị cho 1 `{token}` trong URL pattern lúc thực thi |
| `GW-COMPENSATION-CONFIG-INVALID` | 400 | Cấu hình bù trừ | Cấu hình bù trừ không toàn vẹn (dữ liệu cũ/sửa tay lọt qua validate lúc lưu) |
| `GW-INVALID-BODY` | 400 | Runtime (Data Plane) | Body client gửi không phải JSON hợp lệ khi engine cần parse |
| `GW-PARALLEL-STEP-ERROR`, `GW-PARALLEL-INTERRUPTED` | 400 | Runtime (song song) | Lỗi không xác định/bị ngắt khi thực thi step song song |
| `GW-VERSION-404` | 400 | Lịch sử phiên bản | Không tìm thấy phiên bản theo id |
| `GW-UP-001` | 400 | Cấu hình Upstream | Tên Upstream Service đã tồn tại |
| `GW-UP-404` | 400 | Cấu hình Upstream | Không tìm thấy Upstream Service theo id |
| `GW-UP-INUSE` | 400 | Cấu hình Upstream | Không xoá được — đang có Step tham chiếu |
| `GW-UNAUTHORIZED` | 401 | Xác thực | Thiếu hoặc sai `X-Gateway-Admin-Key` (xem mục 2 — cả 2 loại key đều trả mã này khi sai) |
| `GW-TEAM-001` | 400 | Quản lý đội | `teamCode` đã tồn tại (POST `/api/teams`) |
| `GW-TEAM-404` | 400 | Quản lý đội | Không tìm thấy đội theo `teamCode` (DELETE `/api/teams/{teamCode}`) |

**Lưu ý cho lập trình viên tích hợp**: mọi lỗi phát sinh từ `BusinessException`
(phần lớn bảng trên) đều trả **HTTP 400** dù ý nghĩa ngữ nghĩa có thể là
"không tìm thấy" (404) — phải dựa vào **`errorCode`** để phân biệt chính xác
loại lỗi, không nên chỉ dựa vào HTTP status code.

---

## 7. Phụ lục

### 7.1. Danh sách giá trị Enum dùng trong API

Xem FDS-GWM-001 mục 12.1 (đầy đủ, không lặp lại ở đây).

### 7.2. Tài liệu liên quan

FDS-GWM-001, DBDD-GWM-001, SAD-GWM-001.
