---
title: "Software Requirements Specification — BCCS Gateway Manager"
---

# SOFTWARE REQUIREMENTS SPECIFICATION (SRS)
# Hệ thống Gateway Manager (BCCS Composite API Gateway)

| | |
|---|---|
| **Mã tài liệu** | SRS-GWM-001 |
| **Phiên bản** | 1.0 |
| **Ngày phát hành** | 2026-09-06 |
| **Trạng thái** | Draft |
| **Tài liệu gốc** | BRD-GWM-001 |
| **Đối tượng đọc** | Kiến trúc sư hệ thống, Lập trình viên, Kiểm thử viên |

---

## 1. Giới thiệu

### 1.1. Mục đích

Tài liệu này đặc tả chi tiết các yêu cầu chức năng và phi chức năng của hệ
thống Gateway Manager ở mức **hệ thống** (system-level) — mô tả hệ thống PHẢI
làm gì, với input/output/điều kiện cụ thể — làm cơ sở cho thiết kế kỹ thuật
(FDS), phát triển, và kiểm thử. Mỗi yêu cầu được truy vết ngược về mã yêu cầu
nghiệp vụ tương ứng trong BRD-GWM-001.

### 1.2. Phạm vi sản phẩm

Gateway Manager là 1 nền tảng gồm 2 thành phần chính:

- **Control Plane**: giao diện web + API quản trị, dùng để khai báo/quản lý
  cấu hình (Upstream Service, Endpoint, Backend Step, Field Mapping), tra cứu
  nhật ký, xem lịch sử phiên bản.
- **Data Plane**: thành phần thực thi traffic thật — nhận request từ client
  tại đường dẫn (path) đã khai báo, điều phối gọi các Upstream Service theo
  đúng cấu hình, trả kết quả tổng hợp về client.

Cả 2 thành phần chạy trong **cùng 1 tiến trình backend** (không phải 2 service
tách biệt) — Control Plane thay đổi cấu hình có hiệu lực ngay cho Data Plane
mà không cần khởi động lại.

### 1.3. Định nghĩa, từ viết tắt

Xem mục "Thuật ngữ" trong BRD-GWM-001. Bổ sung riêng cho tài liệu này:

| Từ viết tắt | Giải thích |
|---|---|
| DTO | Data Transfer Object |
| CRUD | Create/Read/Update/Delete |
| SLA | Service Level Agreement |
| FR | Functional Requirement (yêu cầu chức năng, dùng trong tài liệu này) |
| NFR | Non-Functional Requirement |

### 1.4. Tài liệu tham khảo

BRD-GWM-001, FDS-GWM-001, `README.md`, `DEPLOYMENT_GUIDE.md`.

---

## 2. Mô tả tổng quan

### 2.1. Bối cảnh sản phẩm

Gateway Manager là 1 hệ thống độc lập trong nền tảng BCCS, giao tiếp với:

- **Cơ sở dữ liệu Oracle** — lưu trữ cấu hình (Upstream Service, Endpoint,
  Backend Step, Field Mapping) và lịch sử phiên bản.
- **Redis** — cache theo bước/toàn bộ response, và bộ đếm giới hạn tần suất
  (rate limit).
- **Elasticsearch** (tuỳ chọn) — lưu trữ nhật ký request/hop phục vụ tra cứu.
- **Elastic APM Server** (tuỳ chọn) — thu thập số liệu giám sát hiệu năng.
- **Các Upstream Service** — backend HTTP/REST thật mà Endpoint tổng hợp gọi
  tới, không thuộc phạm vi hệ thống này.

### 2.2. Chức năng tổng quan của sản phẩm

1. Quản lý Upstream Service (CRUD + cấu hình resilience).
2. Quản lý Endpoint tổng hợp (CRUD qua form hoặc canvas kéo-thả).
3. Điều phối thực thi (composite orchestration engine).
4. Cache (per-step, response toàn endpoint).
5. Idempotency-Key.
6. Rate limiting.
7. Audit log & tra cứu.
8. Quản lý phiên bản & rollback.
9. Xem trước (preview) — cho endpoint đã lưu và bản nháp chưa lưu.
10. Tự sinh OpenAPI.
11. Export/Import cấu hình.
12. Dashboard sức khoẻ Upstream.

### 2.3. Đặc điểm người dùng

| Nhóm người dùng | Đặc điểm | Tương tác chính |
|---|---|---|
| Người khai báo nghiệp vụ | Hiểu nghiệp vụ, không nhất thiết biết lập trình | Giao diện web (form/canvas), xem trước, tra cứu log |
| Lập trình viên tích hợp | Cần tích hợp hệ thống khác gọi vào Endpoint | Tài liệu OpenAPI tự sinh, gọi API qua Data Plane |
| Quản trị viên hệ thống | Vận hành, giám sát, xử lý sự cố | Dashboard sức khoẻ Upstream, tra cứu log, export/import |

### 2.4. Ràng buộc thiết kế chung

- Toàn bộ xử lý là **đồng bộ** (không dùng lập trình bất đồng bộ/reactive).
- Mọi lệnh gọi ra Upstream Service là HTTP/REST.
- Control Plane (`/api/**`) được bảo vệ bởi 1 khoá API (API key) dùng chung;
  Data Plane (traffic thật của client) không yêu cầu khoá này.
- Cấu hình được lưu trong Oracle; mỗi instance triển khai độc lập kết nối tới
  1 cơ sở dữ liệu Oracle riêng.

---

## 3. Yêu cầu chức năng (Functional Requirements)

### FR-1. Quản lý Upstream Service — (tham chiếu BR-UP-*)

**FR-1.1** Hệ thống phải cung cấp API để tạo/xem/sửa/xoá 1 Upstream Service
với tối thiểu các thuộc tính: tên (duy nhất), địa chỉ gốc (base host), thời
gian chờ kết nối (connect timeout), thời gian chờ phản hồi (read timeout).

**FR-1.2** Hệ thống phải cho phép bật/tắt và cấu hình riêng cho từng Upstream
Service:
- Circuit breaker: ngưỡng tỷ lệ lỗi kích hoạt ngắt (%).
- Retry: bật/tắt (số lần thử lại tối đa là tham số hệ thống dùng chung).
- Bulkhead: số lệnh gọi đồng thời tối đa, thời gian chờ tối đa trước khi bị từ
  chối.

**FR-1.3** Khi retry được bật, hệ thống KHÔNG được thử lại nếu lỗi là lỗi
HTTP 4xx (lỗi phía client/yêu cầu sai) — chỉ thử lại với lỗi hạ tầng (timeout,
connection refused) hoặc lỗi HTTP 5xx.

**FR-1.4** Hệ thống phải từ chối (trả lỗi rõ ràng) yêu cầu xoá 1 Upstream
Service đang được tham chiếu bởi ít nhất 1 Backend Step của bất kỳ Endpoint
nào.

**FR-1.5** Hệ thống phải cung cấp API trả về tình trạng sức khoẻ hiện tại của
TẤT CẢ Upstream Service: tên, trạng thái circuit breaker (đóng/mở/nửa mở), tỷ
lệ lỗi hiện tại, số lệnh gọi đang chờ (bulkhead), tỷ lệ cache-hit.

### FR-2. Quản lý Endpoint tổng hợp — (tham chiếu BR-EP-*)

**FR-2.1** Hệ thống phải cung cấp API để tạo/xem/sửa/xoá 1 Endpoint với tối
thiểu: đường dẫn (path, duy nhất trong hệ thống, có thể chứa tham số dạng
`{token}`), phương thức HTTP (GET/POST/PUT/DELETE/PATCH), và danh sách Backend
Step.

**FR-2.2** Mỗi Backend Step phải có: thứ tự (stepOrder, duy nhất trong 1
Endpoint), tên, phương thức HTTP, mẫu đường dẫn URL (có thể chứa `{token}`),
tham chiếu tới 1 Upstream Service.

**FR-2.3** Hệ thống phải cung cấp giao diện canvas kéo-thả: hiển thị từng
Backend Step dưới dạng 1 khối trực quan, đường nối biểu diễn Field Mapping
giữa các step, và cho phép người dùng sắp xếp vị trí các khối tuỳ ý (vị trí
được lưu lại cùng cấu hình).

**FR-2.4** Hệ thống phải hỗ trợ 2 chế độ chạy các Backend Step khi
`sequential = false`:
- Chạy tuần tự theo vòng lặp thường (mặc định).
- Chạy song song thật sự qua thread pool riêng (khi bật `parallelExecution`),
  CHỈ áp dụng được khi `sequential = false`. Hệ thống phải từ chối lưu cấu
  hình nếu `sequential = true` và `parallelExecution = true` đồng thời (mâu
  thuẫn logic).

**FR-2.5** Khi `sequential = true`, hệ thống phải hỗ trợ rẽ nhánh có điều
kiện trên MỖI Backend Step, gồm:
- Nguồn giá trị so sánh: response của 1 step đã chạy trước đó, hoặc body
  request gốc của client.
- Toán tử so sánh: bằng, khác, tồn tại, không tồn tại, lớn hơn, lớn hơn hoặc
  bằng, nhỏ hơn, nhỏ hơn hoặc bằng.
- Kết quả: chỉ định step tiếp theo chạy khi điều kiện đúng, step tiếp theo
  chạy khi điều kiện sai (hoặc kết thúc chuỗi nếu không chỉ định).
- Với 4 toán tử so sánh số (lớn hơn/nhỏ hơn...), giá trị so sánh khai báo
  phải parse được thành số — nếu không, hệ thống phải báo lỗi rõ ràng.

**FR-2.6** Hệ thống phải hỗ trợ khai báo `onErrorStepOrder` cho 1 Backend
Step: khi step đó thất bại (Upstream lỗi/timeout), hệ thống tự động chuyển
sang chạy step được chỉ định thay vì làm hỏng toàn bộ chuỗi — độc lập với cơ
chế rẽ nhánh điều kiện.

**FR-2.7** Hệ thống phải hỗ trợ nhóm nhiều Backend Step (trong 1 chuỗi
`sequential = true`) bằng 1 mã nhóm chung (`parallelGroup`) để chạy song song
với nhau như 1 "đợt" (wave), trong khi vẫn giữ thứ tự tuần tự tổng thể của
chuỗi (step sau wave chờ TOÀN BỘ wave hoàn tất mới chạy). Hệ thống KHÔNG được
cho phép 1 bước nhảy điều kiện/fallback lỗi nhảy trực tiếp VÀO giữa 1 wave —
wave chỉ được vào theo đúng thứ tự tuần tự tự nhiên.

**FR-2.8** Hệ thống phải hỗ trợ lọc field ("allow list"/"deny list") và đổi
tên field trên response của từng Backend Step trước khi gộp vào response cuối
cùng.

**FR-2.9** Với chuỗi có từ 2 Backend Step độc lập trở lên (`sequential =
false`, không bật song song), hệ thống phải gộp response của TẤT CẢ step
thành 1 object duy nhất (theo nhóm `group` nếu có khai báo, hoặc gộp phẳng
field-by-field) — với chuỗi tuần tự, response trả về là response của step
CUỐI CÙNG thực sự đã chạy (không phải luôn là step có stepOrder lớn nhất, vì
rẽ nhánh có thể kết thúc sớm).

**FR-2.10** Mọi thao tác tạo/sửa Endpoint qua API phải cập nhật ngay bộ nhớ
đệm định tuyến trong tiến trình (routing cache) để có hiệu lực thực thi ngay
lập tức cho request tiếp theo.

### FR-3. Field Mapping (ánh xạ dữ liệu) — (tham chiếu BR-FM-*)

**FR-3.1** Mỗi Field Mapping phải xác định: step đích (targetStepOrder), vị
trí đích trong lệnh gọi (targetType: PATH/QUERY/HEADER/BODY_FIELD), tên tham
số đích, và nguồn dữ liệu (sourceType).

**FR-3.2** Hệ thống phải hỗ trợ các loại nguồn dữ liệu (sourceType) sau:
- `REQUEST_BODY` — lấy từ body request gốc của client.
- `QUERY_PARAM` — lấy từ query string request gốc của client.
- `STEP_RESPONSE` — lấy từ response của 1 step đã chạy trước đó (chỉ định
  qua `sourceStepOrder` + đường dẫn field `sourceField`).
- `STEP_RESPONSE_ARRAY_AGGREGATE` — lấy 1 field từ TỪNG phần tử của 1 mảng
  trong response step trước, gộp thành 1 danh sách.
- `STEP_RESPONSE_ARRAY_MERGE` — gộp field của TỪNG phần tử (object) trong 1
  mảng thành 1 object duy nhất (key trùng nhau: phần tử sau ghi đè phần tử
  trước theo đúng thứ tự mảng).
- `CONSTANT` — giá trị hằng số cố định khai báo sẵn, không phụ thuộc dữ liệu
  request/response.

**FR-3.3** Đường dẫn field (dùng cho `sourceField` khi đọc từ JSON) phải hỗ
trợ cú pháp dot-notation kèm chỉ số mảng, ví dụ `data[0].name` (lấy field
`name` của phần tử đầu tiên trong mảng `data`) hoặc `data[0]` (lấy nguyên
phần tử). Nếu đường dẫn không tồn tại/sai kiểu tại bất kỳ bước nào, hệ thống
phải trả về rỗng (không throw lỗi), vì dữ liệu có thể hợp lệ chỉ chưa từng
chạy qua nhánh đó.

**FR-3.4** Khi targetType = BODY_FIELD và targetParamName = `"$body"`, giá
trị mapping phải THAY THẾ TOÀN BỘ body gửi đi (không bọc trong field nào),
phục vụ trường hợp Upstream Service nhận thẳng 1 mảng/giá trị làm body.

### FR-4. Cơ chế bù trừ nghiệp vụ (Compensation) — (tham chiếu BR-CP-*)

**FR-4.1** Hệ thống chỉ áp dụng cơ chế bù trừ cho chuỗi `sequential = true`.
Mỗi Backend Step có thể khai báo (tuỳ chọn, đủ cả 3 hoặc không khai báo cái
nào): Upstream Service bù trừ, phương thức HTTP bù trừ, mẫu URL bù trừ.

**FR-4.2** Khi 1 step trong chuỗi thất bại (và không có `onErrorStepOrder` xử
lý được), hệ thống phải tự động gọi lệnh bù trừ cho TỪNG step đã chạy THÀNH
CÔNG trước đó, theo đúng THỨ TỰ NGƯỢC với thứ tự hoàn tất thực tế (không phải
thứ tự khai báo `stepOrder`).

**FR-4.3** Với 1 "wave" song song (`parallelGroup`), hệ thống phải ghi nhận
từng thành viên đã hoàn tất RIÊNG LẺ ngay khi thành viên đó xong (không chờ cả
wave), để nếu chuỗi thất bại sau đó, TỪNG thành viên đã thành công trong wave
đều được bù trừ đúng, kể cả khi 1 thành viên khác trong CÙNG wave đó thất bại.

**FR-4.4** Nếu 1 Backend Step chưa khai báo đủ cả 3 trường bù trừ (thiếu 1
hoặc 2 trong số Upstream/phương thức/URL bù trừ), hệ thống phải từ chối lưu
cấu hình với thông báo lỗi rõ ràng — không được để lọt tới lúc chạy thật.

**FR-4.5** Nếu chính lệnh gọi bù trừ thất bại, hệ thống phải ghi nhận lỗi đó
(không throw làm crash tiến trình bù trừ các step còn lại) và tiếp tục bù trừ
các step khác theo đúng thứ tự.

### FR-5. Cache — (tham chiếu BR-CH-*)

**FR-5.1** Mỗi Backend Step có thể bật cache riêng (mặc định tắt) với thời
gian sống (TTL) tự chọn. Cache CHỈ áp dụng khi phương thức của step là GET
hoặc POST — các phương thức PUT/PATCH/DELETE không bao giờ được cache dù có
bật cấu hình, vì gần như chắc chắn là lệnh làm thay đổi dữ liệu.

**FR-5.2** Khoá cache cho 1 step phải được xác định bởi: tên Upstream Service
+ phương thức + URL đã resolve đầy đủ (path + query), và với phương thức
POST, PHẢI bao gồm thêm giá trị băm (hash) của request body — 2 request POST
khác body (ví dụ 2 bộ lọc tìm kiếm khác nhau) gửi tới cùng URL phải nhận 2
khoá cache khác nhau.

**FR-5.3** Hệ thống phải cho phép bật cache cho TOÀN BỘ response cuối cùng
của 1 Endpoint, dùng CHUNG cho mọi client gọi cùng tham số. Khoá cache phải
dựa trên: định danh endpoint + đường dẫn + query đã sắp xếp theo tên (không
phụ thuộc thứ tự client gửi) + (nếu có body) giá trị băm của body.

**FR-5.4** Hệ thống PHẢI TỪ CHỐI lưu cấu hình bật cache toàn bộ response nếu
Endpoint hoặc BẤT KỲ Backend Step nào có phương thức khác GET/POST.

**FR-5.5** Cache toàn bộ response chỉ được ghi khi lệnh gọi THÀNH CÔNG — kết
quả lỗi không được cache.

**FR-5.6** Khi hạ tầng cache (Redis) gặp lỗi (đọc hoặc ghi), hệ thống phải
coi như cache-miss/bỏ qua việc ghi, và tiếp tục xử lý request bình thường —
không được throw lỗi ra client.

### FR-6. Idempotency-Key — (tham chiếu BR-ID-*)

**FR-6.1** Mỗi Endpoint có thể bật tính năng Idempotency-Key (mặc định tắt)
với thời gian sống (TTL) tự chọn.

**FR-6.2** Khi bật, nếu client gửi kèm header `Idempotency-Key`: lần gọi đầu
tiên với 1 giá trị khoá được thực thi bình thường và kết quả THÀNH CÔNG được
lưu lại; các lần gọi tiếp theo với CÙNG khoá trong thời gian TTL phải trả
thẳng kết quả đã lưu, KHÔNG được gọi lại Upstream Service thật (tránh side-
effect lặp khi client tự động gửi lại do timeout mạng).

**FR-6.3** Nếu client không gửi header `Idempotency-Key` (dù Endpoint đã bật
tính năng), hệ thống phải xử lý request bình thường như khi tính năng tắt.

**FR-6.4** Kết quả lỗi không được lưu vào cơ chế Idempotency-Key.

### FR-7. Rate Limiting — (tham chiếu BR-RS-04)

**FR-7.1** Hệ thống phải giới hạn số request tối đa từ 1 địa chỉ IP client
trong 1 khoảng thời gian (window) có thể cấu hình, áp dụng cho toàn bộ Data
Plane.

**FR-7.2** Khi vượt giới hạn, hệ thống phải từ chối request với mã trạng thái
HTTP 429, và tiếp tục nhận request bình thường sau khi window mới bắt đầu.

**FR-7.3** Khi hạ tầng lưu bộ đếm rate limit (Redis) gặp lỗi, hệ thống phải
CHO QUA (fail-open) thay vì chặn toàn bộ traffic.

### FR-8. Audit Log — (tham chiếu BR-AU-*)

**FR-8.1** Với MỖI request client gọi vào Data Plane, hệ thống phải ghi nhận:
thời điểm, endpoint đích, phương thức, đường dẫn, trạng thái (thành công/lỗi),
mã lỗi (nếu có), thời gian xử lý tổng, nội dung body request (có giới hạn độ
dài, đánh dấu rõ nếu bị cắt bớt).

**FR-8.2** Với MỖI lệnh gọi tới 1 Upstream Service bên trong 1 request (1
"hop"), hệ thống phải ghi nhận: thứ tự step, tên step, Upstream Service, URL
đã resolve, request/response (có giới hạn độ dài), trạng thái HTTP, thời gian
xử lý, có lấy từ cache hay không, thành công/thất bại.

**FR-8.3** Hệ thống phải cung cấp API tra cứu nhật ký request theo khoảng
thời gian, trạng thái, đường dẫn endpoint (khớp 1 phần), nội dung body (tìm
kiếm toàn văn), có phân trang.

**FR-8.4** Hệ thống phải cung cấp API lấy toàn bộ chi tiết từng "hop" của 1
request cụ thể theo định danh request đó, sắp xếp theo đúng thứ tự step.

**FR-8.5** Việc ghi nhật ký phải KHÔNG đồng bộ với việc xử lý request (không
được làm chậm response trả về client) và phải fail-open — nếu hạ tầng lưu
nhật ký (Elasticsearch) không khả dụng, request thật vẫn phải được xử lý và
trả kết quả bình thường, chỉ mất phần ghi log (không throw lỗi).

### FR-9. Quản lý phiên bản (Versioning) — (tham chiếu BR-VS-*)

**FR-9.1** Mỗi lần tạo mới, sửa, hoặc khôi phục (rollback) 1 Endpoint, hệ
thống phải tự động lưu 1 bản ghi phiên bản mới chứa toàn bộ nội dung cấu hình
tại thời điểm đó (dạng snapshot đầy đủ), đánh số thứ tự tăng dần riêng cho
từng Endpoint, kèm loại thay đổi (CREATED/UPDATED/ROLLED_BACK).

**FR-9.2** Hệ thống phải cung cấp API liệt kê tất cả phiên bản của 1
Endpoint, và API lấy chi tiết 1 phiên bản cụ thể.

**FR-9.3** Hệ thống phải cung cấp API khôi phục (rollback) 1 Endpoint về
đúng nội dung của 1 phiên bản đã chọn — thao tác này phải đi qua ĐÚNG luồng
validate/lưu như khi sửa cấu hình bình thường (không phải ghi đè trực tiếp dữ
liệu, tránh khôi phục về 1 trạng thái không còn hợp lệ với ràng buộc hiện
tại).

**FR-9.4** Khi xoá 1 Endpoint, toàn bộ lịch sử phiên bản của Endpoint đó phải
được xoá theo (không để lại bản ghi mồ côi).

### FR-10. Xem trước (Preview) — (tham chiếu BR-PV-*)

**FR-10.1** Hệ thống phải cung cấp API gọi thử 1 Endpoint ĐÃ LƯU, nhận vào
giá trị path variable/query param/body do người dùng tự nhập, thực thi
THẬT (không giả lập) qua đúng cơ chế điều phối dùng cho traffic thật, và trả
về kết quả.

**FR-10.2** Hệ thống phải cung cấp API gọi thử 1 draft Endpoint CHƯA LƯU
(nhận toàn bộ nội dung cấu hình nháp trực tiếp trong yêu cầu gọi thử) —
không được ghi bất kỳ dữ liệu nào vào cơ sở dữ liệu, không ảnh hưởng routing
cache đang phục vụ traffic thật.

**FR-10.3** Trước khi thực thi 1 draft (FR-10.2), hệ thống phải áp dụng ĐÚNG
các quy tắc validate giống hệt lúc lưu thật (thứ tự step, rẽ nhánh, bù trừ,
cache toàn bộ response...) — nếu draft không hợp lệ, phải báo lỗi ngay và
KHÔNG được gọi ra bất kỳ Upstream Service nào.

**FR-10.4** Kết quả trả về từ API xem trước (cả FR-10.1 và FR-10.2) phải bao
gồm: kết quả cuối cùng (hoặc thông tin lỗi), VÀ danh sách chi tiết từng bước
đã thực thi (URL, request/response, trạng thái, thời gian, cache-hit) —
KỂ CẢ khi có lỗi giữa chừng, các bước đã chạy THÀNH CÔNG trước lỗi vẫn phải
xuất hiện đầy đủ trong danh sách, không bị mất.

**FR-10.5** API xem trước phải luôn trả về HTTP 200 kèm 1 cờ thành công/thất
bại trong nội dung response (không phân biệt qua mã trạng thái HTTP) đối với
lỗi xảy ra TRONG QUÁ TRÌNH thực thi/validate — chỉ lỗi liên quan tới việc gọi
sai chính API xem trước (ví dụ endpoint không tồn tại) mới trả mã lỗi HTTP
thông thường.

### FR-11. Tự sinh OpenAPI & Export/Import — (tham chiếu BR-DC-*)

**FR-11.1** Hệ thống phải cung cấp API tự sinh đặc tả OpenAPI cho 1 Endpoint
cụ thể, suy luận được path parameter/request body từ cấu hình Field Mapping
hiện có ở mức tốt nhất có thể (best-effort) — không bắt buộc chính xác tuyệt
đối với response body (do phụ thuộc dữ liệu thật của Upstream Service).

**FR-11.2** Hệ thống phải cung cấp API xuất toàn bộ cấu hình (Upstream
Service + Endpoint + Backend Step + Field Mapping) hiện có thành 1 gói dữ
liệu duy nhất.

**FR-11.3** Hệ thống phải cung cấp API nhập 1 gói dữ liệu đã xuất (FR-11.2)
vào hệ thống, phân biệt rõ: bản ghi mới được tạo, bản ghi đã có được cập
nhật, và các cảnh báo nếu có xung đột (ví dụ trùng tên/đường dẫn).

### FR-12. Bảo mật Control Plane

**FR-12.1** Mọi request tới các API dưới tiền tố `/api/**` (Control Plane)
phải mang đúng header khoá API đã cấu hình — thiếu hoặc sai phải bị từ chối
với mã lỗi xác thực, không thực thi bất kỳ logic nghiệp vụ nào.

**FR-12.2** Request tới Data Plane (đường dẫn client tự khai báo cho Endpoint,
KHÔNG nằm dưới `/api/**`) không được áp dụng cơ chế xác thực của FR-12.1.

**FR-12.3** Hệ thống phải ngăn người dùng khai báo 1 Endpoint có đường dẫn
trùng tiền tố dành riêng cho Control Plane (`/api`) hoặc endpoint giám sát hệ
thống (`/actuator`), tránh xung đột định tuyến.

---

## 4. Yêu cầu giao diện ngoài (External Interface Requirements)

### 4.1. Giao diện người dùng (UI)

- Ứng dụng web (trình duyệt), giao diện tiếng Việt.
- 2 luồng khai báo Endpoint: biểu mẫu tuần tự (form nhiều bước) và canvas
  kéo-thả trực quan (hiển thị step dưới dạng khối, Field Mapping dưới dạng
  đường nối SVG).
- Trang tra cứu log dạng bảng, có bộ lọc và mở rộng xem chi tiết từng dòng.
- Trang dashboard sức khoẻ Upstream Service.

### 4.2. Giao diện lập trình (API)

- Control Plane: REST API, JSON, dưới tiền tố `/api/**`, xác thực bằng header
  khoá API riêng của hệ thống.
- Data Plane: REST API, JSON, đường dẫn do người dùng tự khai báo khi tạo
  Endpoint, không xác thực bởi Control Plane (Endpoint tự chịu trách nhiệm
  bảo mật riêng nếu cần, ví dụ chuyển tiếp header xác thực từ client sang
  Upstream Service qua Field Mapping).

### 4.3. Giao diện phần mềm khác

| Hệ thống | Giao thức | Vai trò |
|---|---|---|
| Oracle Database | JDBC | Lưu trữ cấu hình, lịch sử phiên bản |
| Redis | Giao thức Redis (qua thư viện client chuẩn) | Cache, bộ đếm rate limit |
| Elasticsearch | HTTP REST | Lưu trữ và truy vấn nhật ký |
| Elastic APM Server | HTTP (giao thức APM) | Thu thập số liệu giám sát hiệu năng |
| Upstream Service (bất kỳ) | HTTP/REST | Backend thật được Endpoint gọi tới |

---

## 5. Yêu cầu phi chức năng chi tiết

Kế thừa từ mục 8 của BRD-GWM-001, chi tiết hoá thành yêu cầu đo lường được:

| Mã | Yêu cầu |
|---|---|
| NFR-01.1 | Thời gian xử lý nội bộ (không tính thời gian mạng gọi Upstream) cho việc thông dịch cấu hình + ánh xạ dữ liệu của 1 request phải ở mức micro-giây, không tạo ra độ trễ cảm nhận được so với tổng thời gian phản hồi (vốn thường được quyết định bởi thời gian mạng gọi Upstream, ở mức mili-giây). |
| NFR-02.1 | Khi Redis không khả dụng: chức năng cache và rate-limit phải tự động chuyển sang trạng thái "bỏ qua" (không cache/không giới hạn), KHÔNG được trả lỗi cho client. |
| NFR-02.2 | Khi Elasticsearch không khả dụng: chức năng ghi nhật ký phải tự động bỏ qua (không throw lỗi), riêng chức năng TRA CỨU nhật ký được phép báo lỗi rõ ràng cho người dùng (vì bản chất phụ thuộc trực tiếp Elasticsearch). |
| NFR-03.1 | Header xác thực Control Plane phải là giá trị bí mật, có khả năng thay đổi qua cấu hình môi trường, không hard-code trong mã nguồn. |
| NFR-04.1 | Bộ nhớ đệm cấu hình trong tiến trình (routing cache) không được có bất kỳ giả định nào về việc chia sẻ trạng thái với 1 tiến trình khác — mỗi tiến trình độc lập tự tải lại cấu hình từ cơ sở dữ liệu của chính nó. |
| NFR-05.1 | Việc thêm 1 giá trị `sourceType` mới cho Field Mapping không được yêu cầu sửa đổi cấu trúc dữ liệu của các `sourceType` đã có. |
| NFR-06.1 | Mọi bản ghi nhật ký hop phải cho phép truy vết ngược về đúng 1 request cụ thể đã sinh ra nó. |
| NFR-07.1 | Cấu trúc dữ liệu (schema) của hệ thống không được sử dụng bất kỳ kiểu dữ liệu nào chỉ khả dụng từ phiên bản Oracle mới nhất — phải tương thích với phiên bản Oracle cũ hơn đang được nhiều đội BCCS sử dụng phổ biến trong thực tế. |

---

## 6. Mô tả Use Case chính

### UC-01: Khai báo 1 Endpoint tổng hợp mới qua Canvas

- **Tác nhân**: Người khai báo nghiệp vụ.
- **Điều kiện tiên quyết**: đã có sẵn (các) Upstream Service cần dùng.
- **Luồng chính**:
  1. Người dùng mở màn hình Canvas, khai báo tên/đường dẫn/phương thức
     Endpoint.
  2. Thêm từng Backend Step, chọn Upstream Service, khai báo URL/phương thức
     cho từng step.
  3. Thêm Field Mapping nối dữ liệu giữa các step (kéo-thả trên canvas).
  4. Bấm "Thử nhanh" để xem trước kết quả thật (chưa lưu) — xem FR-10.2 tới
     FR-10.4.
  5. Điều chỉnh cấu hình nếu kết quả xem trước chưa đúng, lặp lại bước 4.
  6. Bấm Lưu — hệ thống validate và lưu, có hiệu lực ngay.
- **Luồng thay thế**: nếu validate thất bại ở bước 6 (ví dụ trùng đường dẫn,
  rẽ nhánh nhảy sai chỗ), hệ thống báo lỗi cụ thể, không lưu.

### UC-02: Client gọi vào 1 Endpoint tổng hợp (Data Plane)

- **Tác nhân**: Hệ thống/ứng dụng bên ngoài (client).
- **Luồng chính**:
  1. Client gửi request HTTP tới đường dẫn đã khai báo.
  2. Hệ thống khớp đường dẫn với 1 Endpoint đã cấu hình.
  3. (Tuỳ chọn) Kiểm tra Idempotency-Key/cache toàn bộ response — nếu trúng,
     trả kết quả cũ ngay, kết thúc.
  4. Điều phối thực thi các Backend Step theo đúng cấu hình (tuần tự/song
     song/rẽ nhánh), áp dụng cache/circuit breaker/retry/bulkhead riêng từng
     step.
  5. Gộp kết quả, trả response cho client.
  6. Ghi nhận nhật ký (bất đồng bộ, không chặn bước 5).
- **Luồng thay thế**: 1 step thất bại → theo `onErrorStepOrder` nếu có, hoặc
  kết thúc lỗi + kích hoạt bù trừ các step đã thành công (nếu chuỗi tuần tự
  có khai báo bù trừ).

### UC-03: Điều tra sự cố qua Tra cứu Log

- **Tác nhân**: Quản trị viên/Người khai báo nghiệp vụ.
- **Luồng chính**:
  1. Người dùng lọc danh sách request theo thời gian/trạng thái/đường dẫn.
  2. Chọn 1 request lỗi, mở rộng xem chi tiết.
  3. Hệ thống hiển thị toàn bộ "hop" (từng bước gọi Upstream) của request đó
     theo đúng thứ tự, xác định chính xác bước nào gây lỗi.

### UC-04: Khôi phục cấu hình về phiên bản cũ

- **Tác nhân**: Người khai báo nghiệp vụ.
- **Luồng chính**:
  1. Người dùng mở lịch sử phiên bản của 1 Endpoint.
  2. Chọn 1 phiên bản cũ, xem trước nội dung.
  3. Xác nhận khôi phục — hệ thống validate lại nội dung phiên bản đó như 1
     lần sửa bình thường, lưu thành phiên bản mới (đánh dấu ROLLED_BACK).

### UC-05: Triển khai 1 instance Gateway Manager mới cho 1 đội

- **Tác nhân**: Quản trị viên hạ tầng của 1 đội BCCS.
- **Luồng chính**: xem chi tiết trong `DEPLOYMENT_GUIDE.md` — tóm tắt: chuẩn
  bị Oracle/Redis (Elasticsearch/APM tuỳ chọn), cấu hình thông số kết nối,
  khởi động — hệ thống tự khởi tạo cấu trúc dữ liệu ở lần chạy đầu tiên.

---

## 7. Yêu cầu dữ liệu (mức khái niệm)

| Thực thể | Mô tả | Quan hệ chính |
|---|---|---|
| UpstreamService | 1 backend thật đã đăng ký | 1—N với BackendStep |
| EndpointConfig | 1 Endpoint tổng hợp | 1—N với BackendStep, 1—N với FieldMapping, 1—N với EndpointConfigVersion |
| BackendStep | 1 bước gọi trong 1 Endpoint | N—1 với EndpointConfig, N—1 với UpstreamService (gọi chính + gọi bù trừ) |
| FieldMapping | 1 quy tắc ánh xạ dữ liệu | N—1 với EndpointConfig, tham chiếu tới 1 step nguồn/1 step đích theo số thứ tự |
| EndpointConfigVersion | 1 bản ghi lịch sử phiên bản | N—1 với EndpointConfig (snapshot toàn bộ nội dung tại thời điểm lưu) |

Chi tiết cấu trúc bảng/cột cụ thể được đặc tả trong tài liệu **FDS-GWM-001**.

---

## 8. Ma trận truy vết yêu cầu (Traceability Matrix — trích lược)

| Yêu cầu nghiệp vụ (BRD) | Yêu cầu chức năng (SRS) |
|---|---|
| BR-EP-04 (rẽ nhánh điều kiện) | FR-2.5 |
| BR-EP-06 (wave song song) | FR-2.7 |
| BR-CP-01..03 (bù trừ) | FR-4.1 .. FR-4.5 |
| BR-CH-02, BR-CH-03 (cache toàn bộ response) | FR-5.3, FR-5.4 |
| BR-PV-02, BR-PV-03 (xem trước chưa lưu) | FR-10.2 .. FR-10.5 |
| BR-DP-01..03 (triển khai đa đội) | Mục 4.3, UC-05, và FDS-GWM-001 |
