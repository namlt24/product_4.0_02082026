# Cải tiến: Làm lộ rõ "bẫy Bulkhead mặc định" thay vì để nó âm thầm chặn RPS ✅

> **Đã triển khai (2026-09-14)** — phát sinh từ buổi review sizing "1000 RPS"
> của Data Plane. Xem mục "Files đã sửa" bên dưới cho danh sách chính xác.
> Ghi chú: tài liệu này được viết TRƯỚC khi tính năng "override Bulkhead
> riêng theo Step" ra đời — phần đếm/hiển thị dưới đây CHỈ áp dụng cho
> Bulkhead dùng chung theo Upstream, chưa gồm các Bulkhead riêng theo step
> (khoá `upstreamName#step:stepId`), dù log WARN (mục A) đã tự động bắt được
> CẢ 2 loại vì nó lắng nghe toàn bộ `BulkheadRegistry`.

## Context

Trong buổi review "sizing 1000 RPS" của `krakend-gateway-manager` (Tomcat
threads, HttpClient5 pool, JVM heap — tất cả đã tính đúng theo Little's Law),
phát hiện rủi ro lớn nhất **không nằm ở các con số sizing đó** mà ở
`UpstreamService.maxConcurrentCalls` (Bulkhead Resilience4j) — mặc định = 20
([UpstreamService.java:88](../backend/src/main/java/com/bccs/gatewaymanager/entity/UpstreamService.java#L88)).
Với 20 lệnh gọi đồng thời/pod × 3 pod ÷ mean 1.5s ≈ chỉ ~40 RPS — bất kể
Tomcat/HTTP pool có rộng đến đâu. Bước "phải tăng giá trị này trước khi nhận
tải thật" đã được ghi trong runbook
([HƯỚNG_DẪN_PRODUCTION.md mục 8](../k8s/data-plane/production/HƯỚNG_DẪN_PRODUCTION.md))
nhưng là **thao tác tay, không có gì trong code nhắc/cảnh báo** nếu người vận
hành quên — lỗi sẽ chỉ lộ ra khi khách hàng báo bị 429/503 hàng loạt lúc tải
thật lên cao, khó truy ngược nguyên nhân.

Mục tiêu cải tiến: biến rủi ro "âm thầm" này thành **thấy được** — trong log
vận hành (real-time) và trên chính "Dashboard sức khoẻ Upstream" đã có sẵn
(P1) — mà không đổi hành vi Bulkhead hiện tại (không tự ý tăng giá trị,
tránh thay đổi hành vi production ngoài ý muốn của đội vận hành).

## Thiết kế — tái dùng đúng 2 pattern đã có sẵn trong repo

### A. Log WARN real-time khi Bulkhead từ chối 1 lệnh gọi

File: `backend/.../config/ResilienceConfig.java`

Class này đã làm đúng việc này cho CircuitBreaker (lắng nghe
`circuitBreakerRegistry.getEventPublisher().onEntryAdded(...)` rồi
`cb.getEventPublisher().onStateTransition(...)` → `log.warn`). Thêm y hệt
cho `BulkheadRegistry`:
- Inject thêm `BulkheadRegistry bulkheadRegistry` và
  `UpstreamHttpExecutor upstreamHttpExecutor` (để ghi counter, xem mục B).
- `bulkheadRegistry.getEventPublisher().onEntryAdded(e -> { Bulkhead bh =
  e.getAddedEntry(); bh.getEventPublisher().onCallRejected(ev -> {
  log.warn(...); upstreamHttpExecutor.recordBulkheadRejection(bh.getName());
  }); })`.
- Cập nhật javadoc class (hiện chỉ nói về CircuitBreaker) để nói rõ nay lắng
  nghe cả Bulkhead.

Kết quả: mỗi lần 1 request bị Bulkhead chặn (đầy chỗ, hết `maxWaitDurationMs`),
log WARN xuất hiện ngay — grep được / gắn alert được, thay vì chỉ là 1 response
lỗi phía client mà vận hành không nhìn thấy.

### B. Đếm dồn số lần bị từ chối theo từng Upstream (mirror cacheHits/cacheMisses)

File: `backend/.../engine/UpstreamHttpExecutor.java`

Đã có sẵn đúng pattern này cho cache (`Map<String, LongAdder> cacheHits`/
`cacheMisses` + `cacheHitCount()`/`cacheMissCount()` — dùng cho Dashboard sức
khoẻ Upstream). Thêm tương tự:
- `Map<String, LongAdder> bulkheadRejections = new ConcurrentHashMap<>();`
- `public void recordBulkheadRejection(String upstreamName)` — gọi từ
  `ResilienceConfig` (mục A) khi có sự kiện `onCallRejected`.
- `public long bulkheadRejectionCount(String upstreamName)` — mirror
  `cacheHitCount()`.

Không đổi gì trong `call()` — vẫn đúng nguyên luồng cache/CB/Retry/Bulkhead
hiện có, chỉ thêm 1 kênh quan sát độc lập.

### C. Hiện lên "Dashboard sức khoẻ Upstream" đã có (Control Plane, P1)

Backend — mirror đúng cách `UpstreamHealthService`/`UpstreamHealthDto` đã làm
với CircuitBreaker (dùng `registry.find(name)`, KHÔNG dùng
`registry.bulkhead(name, supplier)` — tránh tạo mới 1 bulkhead chỉ vì đang
xem màn hình health, đúng comment đã có sẵn ở `toHealth()`):

- `UpstreamHealthDto` — thêm 4 field:
  - `int maxConcurrentCalls` — từ `u.getMaxConcurrentCalls()` (đã có sẵn
    trên entity, không cần gọi gì thêm).
  - `int availableConcurrentCalls` — từ
    `bulkheadRegistry.find(u.getName())`; nếu rỗng (chưa từng gọi lần nào)
    thì bulkhead thật sự chưa tồn tại nên toàn bộ permit còn nguyên → trả
    thẳng `u.getMaxConcurrentCalls()` (khác cách CB đang làm với `-1`, vì ở
    đây "chưa gọi lần nào" có 1 giá trị đúng rõ ràng, không cần sentinel).
  - `long bulkheadRejectedCount` — từ
    `upstreamHttpExecutor.bulkheadRejectionCount(u.getName())`.
  - `boolean usingDefaultBulkheadSizing` — `maxConcurrentCalls ==
    UpstreamService.DEFAULT_MAX_CONCURRENT_CALLS` (xem refactor nhỏ dưới).
- `UpstreamHealthService` — inject thêm `BulkheadRegistry`, tính 4 field
  trên trong `toHealth()`.

Refactor nhỏ đi kèm (dọn 1 số "20" đang bị lặp lại 2 nơi):
- Thêm `public static final int DEFAULT_MAX_CONCURRENT_CALLS = 20;` vào
  `UpstreamService.java`, dùng lại ở field default hiện có VÀ ở
  `UpstreamServiceDto.java` dòng `if (maxConcurrentCalls <= 0)
  maxConcurrentCalls = 20;` — để "usingDefaultBulkheadSizing" ở trên so
  sánh với đúng 1 nguồn sự thật duy nhất, không phải hardcode số `20` lần
  thứ 3.

Frontend — mirror đúng khối `metric-row` đã có trong
`upstream-health.component.html` (cache hit/miss/rate):
- `endpoint.model.ts` — thêm 4 field trên vào interface `UpstreamHealth`.
- `upstream-health.component.html` — thêm 1 `metric-row` thứ 3: "Bulkhead
  tối đa" / "Đang dùng" (`maxConcurrentCalls - availableConcurrentCalls`) /
  "Bị từ chối" (`bulkheadRejectedCount`).
- Khi `usingDefaultBulkheadSizing === true`: thêm 1 chip cảnh báo nhỏ trên
  `health-card-header` (cạnh `state-chip` đã có), ví dụ "⚠ Bulkhead mặc
  định" kèm `matTooltip` trỏ tới công thức đã có trong
  `HƯỚNG_DẪN_PRODUCTION.md` mục 8 ("maxConcurrentCalls ≥ P99(giây) ×
  rps-đi-lên-upstream-đó × 1.2 per pod") — để người xem thấy ngay TRƯỚC khi
  xảy ra sự cố, không phải sau.
- `upstream-health.component.ts` — không cần logic mới (chip điều kiện xử lý
  thẳng trong template qua `@if`), trừ khi cần format số.

## Files sẽ sửa

- `backend/.../config/ResilienceConfig.java` — thêm listener Bulkhead.
- `backend/.../engine/UpstreamHttpExecutor.java` — thêm counter +
  `recordBulkheadRejection()`/`bulkheadRejectionCount()`.
- `backend/.../entity/UpstreamService.java` — thêm hằng số
  `DEFAULT_MAX_CONCURRENT_CALLS`.
- `backend/.../dto/UpstreamServiceDto.java` — dùng lại hằng số thay vì
  hardcode `20`.
- `backend/.../dto/UpstreamHealthDto.java` — thêm 4 field.
- `backend/.../service/UpstreamHealthService.java` — tính 4 field, inject
  `BulkheadRegistry`.
- `frontend/src/app/models/endpoint.model.ts` — thêm field vào
  `UpstreamHealth`.
- `frontend/src/app/pages/upstream-health/upstream-health.component.html`
  (+ `.scss` nếu cần style chip cảnh báo) — hiện field mới + chip cảnh báo.
- Test mới/cập nhật:
  - `ResilienceConfigTest.java` (mới) — verify `onCallRejected` gọi
    `upstreamHttpExecutor.recordBulkheadRejection(name)` + log WARN (dựng
    `BulkheadRegistry.ofDefaults()` thật + `bulkheadRegistry.bulkhead("x",
    BulkheadConfig.custom().maxConcurrentCalls(1)...)`, bắn 2 lệnh gọi song
    song để ép 1 lệnh bị từ chối thật, không mock registry).
  - `UpstreamHttpExecutorTest.java` — thêm test cho
    `recordBulkheadRejection()`/`bulkheadRejectionCount()`.
  - `UpstreamHealthServiceTest.java` — thêm case: bulkhead chưa từng gọi
    (available = max), đã có lệnh bị từ chối (rejectedCount > 0), và
    `usingDefaultBulkheadSizing` đúng/sai theo giá trị `maxConcurrentCalls`.

## Ngoài phạm vi

- Không tự động tăng `maxConcurrentCalls` — chỉ làm rủi ro **thấy được**,
  quyết định sizing vẫn do người vận hành (đúng công thức đã có trong
  runbook).
- Không thêm alert/metric xuất ra Prometheus/Elastic — chỉ log WARN + hiện
  trên UI hiện có (P1). Có thể làm thêm ở lần sau nếu cần.
- Không đổi hành vi `maxWaitDurationMs`/thuật toán Bulkhead hiện tại.

## Verification (khi triển khai)

1. `mvn test` xanh toàn bộ (gồm 3 test file mới/sửa ở trên).
2. `mvn clean install` (build production jar) không lỗi.
3. Test tay: đặt 1 Upstream `maxConcurrentCalls=1`, `maxWaitDurationMs=100`,
   bắn 2 request "Thử nhanh" đồng thời tới cùng step dùng upstream đó →
   thấy log WARN "Bulkhead ... từ chối", mở `/upstreams/health` thấy
   "Bị từ chối" tăng, "Đang dùng"/"Bulkhead tối đa" đúng số.
4. Tạo 1 Upstream mới (giữ nguyên mặc định `maxConcurrentCalls=20`) → xác
   nhận chip cảnh báo "Bulkhead mặc định" hiện trên Dashboard.
