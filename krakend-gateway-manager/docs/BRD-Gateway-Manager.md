---
title: "Business Requirement Document — BCCS Gateway Manager"
subtitle: "Composite API Orchestration Platform"
---

# BUSINESS REQUIREMENT DOCUMENT
# Hệ thống Gateway Manager (BCCS Composite API Gateway)

| | |
|---|---|
| **Mã tài liệu** | BRD-GWM-001 |
| **Phiên bản** | 1.0 |
| **Ngày phát hành** | 2026-09-06 |
| **Trạng thái** | Draft |
| **Phạm vi hệ thống** | krakend-gateway-manager (nền tảng BCCS) |
| **Đối tượng đọc** | Ban quản lý dự án, Trưởng nhóm nghiệp vụ các đội BCCS, Kiến trúc sư giải pháp |

---

## 1. Lịch sử thay đổi tài liệu

| Phiên bản | Ngày | Người soạn | Mô tả thay đổi |
|---|---|---|---|
| 1.0 | 2026-09-06 | Đội phát triển Gateway Manager | Khởi tạo tài liệu |

---

## 2. Tóm tắt tổng quan (Executive Summary)

Các đội nghiệp vụ trong hệ sinh thái BCCS (Viettel) thường xuyên cần xây dựng
**API tổng hợp** (composite API) — tức 1 API duy nhất mà client gọi vào, nhưng
đứng sau đó là nhiều lệnh gọi tuần tự/song song tới nhiều service nội bộ khác
nhau (ví dụ: tra thông tin khách hàng từ service A, dùng kết quả đó gọi tiếp
service B để lấy danh sách gói cước, gộp lại thành 1 response duy nhất trả về
client).

Cách làm truyền thống — mỗi đội tự viết code Java/Spring Boot riêng cho từng
API tổng hợp — tốn thời gian phát triển, khó bảo trì, và không nhất quán về
các khía cạnh vận hành quan trọng (cache, circuit breaker, retry, rate limit,
audit log, khả năng rollback khi 1 bước giữa chừng thất bại...) vì mỗi đội tự
tay cài đặt lại từ đầu.

**Gateway Manager** là 1 nền tảng cho phép **khai báo** (không cần viết code)
1 API tổng hợp bằng cách cấu hình qua giao diện web: định nghĩa các bước gọi
service (Backend Step), cách dữ liệu chảy giữa các bước (Field Mapping), điều
kiện rẽ nhánh, và các chính sách vận hành (cache, resilience, idempotency).
Hệ thống thực thi trực tiếp các API đã khai báo tại thời điểm client gọi vào,
không cần build/deploy lại code mỗi khi thêm hoặc sửa 1 API tổng hợp.

Tài liệu này mô tả **yêu cầu nghiệp vụ** làm nền tảng cho việc xây dựng và vận
hành hệ thống, phục vụ nhiều đội BCCS sử dụng độc lập trên hạ tầng riêng của
từng đội.

---

## 3. Bối cảnh & Vấn đề cần giải quyết

### 3.1. Hiện trạng trước khi có Gateway Manager

- Mỗi đội BCCS khi cần 1 API tổng hợp phải **tự viết code** gọi tuần tự/song
  song nhiều service, tự xử lý lỗi, tự viết cache, tự tích hợp circuit
  breaker/retry — lặp lại hạ tầng vận hành giống nhau ở nhiều service khác
  nhau.
- Thay đổi logic tổng hợp (thêm 1 bước gọi mới, đổi field mapping, thêm điều
  kiện rẽ nhánh) đòi hỏi **sửa code, build, deploy lại** toàn bộ service —
  chậm, rủi ro ảnh hưởng các API khác cùng service.
- Không có công cụ chung để **xem trước** (preview) hành vi 1 API tổng hợp
  trước khi đưa vào vận hành thật, cũng như **tra cứu** chi tiết từng bước gọi
  (request/response, thời gian, cache-hit) khi cần điều tra sự cố.
- Cấu hình resilience (circuit breaker, retry, giới hạn đồng thời) thường bị
  làm tắt/qua loa hoặc thiếu nhất quán giữa các service do mỗi đội tự cài đặt
  theo cách riêng.

### 3.2. Vấn đề cần Gateway Manager giải quyết

| # | Vấn đề | Tác động nếu không giải quyết |
|---|---|---|
| P1 | Thời gian phát triển 1 API tổng hợp mới quá dài (phải code, test, deploy) | Chậm đưa tính năng ra thị trường |
| P2 | Không nhất quán về resilience/cache/audit giữa các đội | Sự cố dây chuyền khi 1 service con chậm/lỗi, khó điều tra khi có sự cố |
| P3 | Sửa đổi nhỏ (thêm field, đổi thứ tự bước) vẫn cần build/deploy lại | Rủi ro triển khai không cần thiết cho thay đổi cấu hình đơn thuần |
| P4 | Không có khả năng rollback nghiệp vụ khi 1 bước giữa chừng của giao dịch tổng hợp thất bại | Dữ liệu không nhất quán giữa các service sau khi 1 giao dịch nhiều bước thất bại nửa chừng |
| P5 | Mỗi đội hiện phải tự lo hạ tầng vận hành riêng (Oracle, Redis, Elasticsearch) khi áp dụng | Cần hướng dẫn/công cụ triển khai độc lập theo từng đội, không phụ thuộc hạ tầng dùng chung |

---

## 4. Mục tiêu nghiệp vụ (Business Objectives)

- **BO1 — Rút ngắn thời gian đưa API tổng hợp vào vận hành**: từ "viết code +
  build + deploy" (thường mất nhiều ngày) xuống "khai báo qua giao diện + lưu"
  (có hiệu lực ngay lập tức).
- **BO2 — Chuẩn hoá vận hành**: mọi API tổng hợp khai báo qua hệ thống đều tự
  động có cache, circuit breaker, retry, rate limit, audit log, idempotency —
  không phụ thuộc việc đội phát triển có tự nhớ cài đặt hay không.
- **BO3 — Giảm rủi ro thay đổi cấu hình**: cho phép **xem trước** (preview)
  hành vi 1 API tổng hợp — kể cả khi đang khai báo, chưa lưu — trước khi đưa
  vào vận hành thật; giữ lại **lịch sử phiên bản** để khôi phục khi cấu hình
  mới gây lỗi.
- **BO4 — Đảm bảo tính toàn vẹn dữ liệu nghiệp vụ nhiều bước**: hỗ trợ cơ chế
  bù trừ (compensation/rollback) khi 1 chuỗi gọi nhiều bước thất bại giữa
  chừng.
- **BO5 — Cho phép mỗi đội BCCS tự triển khai độc lập**: mỗi đội chạy 1
  instance Gateway Manager riêng trên hạ tầng (Oracle, Redis, Elasticsearch)
  của chính đội đó, không tranh chấp tài nguyên với đội khác, nhận được bản
  cập nhật/tính năng mới của nền tảng dùng chung.

---

## 5. Phạm vi (Scope)

### 5.1. Trong phạm vi (In-scope)

- Khai báo, quản lý (CRUD), và thực thi API tổng hợp (Endpoint composite) gồm
  nhiều bước gọi (Backend Step) tới các service nội bộ (Upstream Service).
- 2 giao diện khai báo: form truyền thống và canvas kéo-thả trực quan.
- Điều phối thực thi: tuần tự, song song (toàn bộ hoặc theo từng "wave" trong
  1 chuỗi tuần tự), rẽ nhánh có điều kiện, fallback khi lỗi.
- Ánh xạ dữ liệu (Field Mapping) giữa các bước: từ body/query của client, từ
  response bước trước, hoặc giá trị hằng số.
- Cơ chế bù trừ nghiệp vụ (compensation) khi chuỗi gọi thất bại giữa chừng.
- Cache: theo từng bước gọi (per-step) và cache toàn bộ response của 1
  endpoint dùng chung cho mọi client.
- Chống trùng lặp khi client tự động gọi lại (Idempotency-Key).
- Khả năng chịu lỗi: circuit breaker, retry, giới hạn số lệnh gọi đồng thời
  (bulkhead) theo từng Upstream Service.
- Giới hạn tần suất gọi (rate limit) bảo vệ hệ thống backend thật phía sau.
- Ghi nhận và tra cứu nhật ký (audit log) chi tiết từng request/từng bước gọi.
- Lưu lịch sử phiên bản cấu hình mỗi endpoint, khôi phục (rollback) về phiên
  bản cũ.
- Xem trước (preview) hành vi 1 endpoint — cả khi đã lưu và khi đang khai báo
  chưa lưu — bao gồm chi tiết từng bước (request/response/thời gian).
- Tự sinh tài liệu OpenAPI cho từng endpoint.
- Xuất/nhập (export/import) cấu hình giữa các môi trường.
- Bảng theo dõi sức khoẻ (health) của từng Upstream Service.
- Đóng gói để mỗi đội BCCS tự triển khai 1 instance độc lập trên hạ tầng riêng
  (Oracle, Redis, Elasticsearch riêng của từng đội).

### 5.2. Ngoài phạm vi (Out-of-scope)

- Không phải 1 API Gateway biên (edge gateway) làm nhiệm vụ định tuyến/an
  ninh cho TOÀN BỘ traffic vào hệ thống BCCS (không thay thế các gateway biên
  hiện có) — phạm vi là **tổng hợp nghiệp vụ nhiều service** (composition),
  không phải routing/security biên.
- Không hỗ trợ giao thức bất đồng bộ/streaming (message queue, WebSocket,
  gRPC streaming) — chỉ HTTP/REST đồng bộ.
- Không tự động phát hiện/tạo cấu hình từ code có sẵn (không có công cụ
  "import từ OpenAPI của service khác để tự sinh Endpoint") — việc khai báo
  Backend Step/Field Mapping vẫn do người dùng thực hiện qua giao diện.
- Không cung cấp cơ chế đa người dùng/phân quyền chi tiết theo vai trò (hiện
  bảo vệ Control Plane bằng 1 API key chung, không phải hệ thống người dùng
  đầy đủ với vai trò/quyền hạn khác nhau).
- Không đảm nhiệm việc giám sát hạ tầng (CPU/RAM/network) của các service
  backend thật phía sau — chỉ giám sát ở góc độ lệnh gọi (số lỗi, thời gian
  phản hồi, trạng thái circuit breaker).

---

## 6. Đối tượng liên quan (Stakeholders)

| Vai trò | Mô tả | Lợi ích từ hệ thống |
|---|---|---|
| Đội nghiệp vụ BCCS (business developer) | Người khai báo/quản lý API tổng hợp cho nghiệp vụ của đội mình | Không cần viết code cho logic tổng hợp; tự phục vụ (self-service) |
| Kiến trúc sư giải pháp | Chịu trách nhiệm chuẩn hoá cách các service BCCS giao tiếp với nhau | Đảm bảo mọi API tổng hợp tuân thủ chuẩn resilience/audit chung |
| Đội vận hành (DevOps/Hạ tầng) | Triển khai và giám sát instance Gateway Manager của từng đội | Có công cụ theo dõi sức khoẻ Upstream, tra cứu log tập trung |
| Client tiêu thụ API (ứng dụng/service khác) | Gọi vào API tổng hợp đã khai báo | Nhận 1 API duy nhất, ổn định, có cache/chống trùng lặp, không cần biết logic tổng hợp phía sau |
| Đội phát triển nền tảng Gateway Manager | Xây dựng, bảo trì, phát hành phiên bản mới của chính hệ thống | Codebase dùng chung, mỗi đội tự cập nhật độc lập |

---

## 7. Yêu cầu nghiệp vụ chi tiết (Business Requirements)

Mỗi yêu cầu được đánh mã `BR-<nhóm>-<số thứ tự>` để tiện tham chiếu chéo sang
tài liệu SRS/FDS.

### 7.1. Quản lý Upstream Service (backend thật phía sau)

| Mã | Yêu cầu |
|---|---|
| BR-UP-01 | Hệ thống phải cho phép đăng ký 1 lần thông tin 1 backend thật (tên, host, timeout) để nhiều Endpoint tham chiếu dùng lại, không phải khai host thủ công lặp lại ở từng nơi. |
| BR-UP-02 | Hệ thống phải cho phép bật/tắt và cấu hình circuit breaker (ngưỡng tỷ lệ lỗi), retry (số lần thử lại), và giới hạn số lệnh gọi đồng thời (bulkhead) riêng cho từng Upstream Service. |
| BR-UP-03 | Hệ thống phải hiển thị được tình trạng sức khoẻ hiện tại (trạng thái circuit breaker, tỷ lệ cache hit, số lệnh đang chờ) của từng Upstream Service. |
| BR-UP-04 | Hệ thống phải ngăn xoá 1 Upstream Service đang được ít nhất 1 Endpoint tham chiếu, tránh cấu hình treo/lỗi ngầm. |

### 7.2. Khai báo Endpoint tổng hợp (composite)

| Mã | Yêu cầu |
|---|---|
| BR-EP-01 | Hệ thống phải cho phép khai báo 1 Endpoint tổng hợp gồm: đường dẫn (path) + method HTTP client sẽ gọi vào, và 1 hoặc nhiều Backend Step thực thi phía sau. |
| BR-EP-02 | Hệ thống phải cung cấp 2 hình thức khai báo: (a) biểu mẫu (form) truyền thống, (b) giao diện canvas kéo-thả trực quan thể hiện các bước và luồng dữ liệu bằng sơ đồ. |
| BR-EP-03 | Hệ thống phải cho phép chọn 1 trong 2 chế độ thực thi cho các Backend Step: tuần tự (step sau chờ step trước) hoặc song song (các step độc lập chạy đồng thời). |
| BR-EP-04 | Với chế độ tuần tự, hệ thống phải cho phép khai báo **rẽ nhánh có điều kiện**: dựa vào giá trị lấy được từ response 1 step hoặc body request gốc, quyết định step tiếp theo nào sẽ chạy, hoặc kết thúc chuỗi sớm. |
| BR-EP-05 | Hệ thống phải cho phép khai báo **fallback khi lỗi**: nếu 1 step thất bại (backend thật trả lỗi/timeout), tự động chuyển sang chạy 1 step dự phòng thay vì làm hỏng cả chuỗi. |
| BR-EP-06 | Hệ thống phải cho phép nhóm 1 số step (trong chuỗi tuần tự) thành 1 "đợt" (wave) chạy song song với nhau, trong khi các step khác trong cùng chuỗi vẫn chạy tuần tự bình thường. |
| BR-EP-07 | Hệ thống phải cho phép lọc/đổi tên field trong response của từng step (chỉ giữ 1 số field, loại bỏ 1 số field, đổi tên field) trước khi đưa vào response cuối cùng trả về client. |
| BR-EP-08 | Việc lưu 1 Endpoint mới hoặc sửa Endpoint đã có phải có hiệu lực thực thi **ngay lập tức** cho các lệnh gọi tiếp theo, không cần khởi động lại hệ thống. |

### 7.3. Truyền/ánh xạ dữ liệu giữa các bước (Field Mapping)

| Mã | Yêu cầu |
|---|---|
| BR-FM-01 | Hệ thống phải cho phép 1 step lấy dữ liệu đầu vào (path/query/header/body) từ: dữ liệu client gửi lên (body gốc hoặc query param), response của 1 step đã chạy trước đó, hoặc 1 giá trị hằng số cấu hình sẵn. |
| BR-FM-02 | Hệ thống phải cho phép gộp 1 field lấy ra từ TỪNG phần tử của 1 mảng trong response step trước thành 1 danh sách duy nhất để truyền cho step sau (ví dụ: gộp danh sách mã sản phẩm từ danh sách object trả về ở step trước). |
| BR-FM-03 | Hệ thống phải cho phép truy cập dữ liệu lồng sâu (nested object, phần tử theo vị trí trong mảng) bằng cú pháp đường dẫn đơn giản, không yêu cầu người dùng biết lập trình. |

### 7.4. Tính toàn vẹn giao dịch nhiều bước (Compensation/Rollback)

| Mã | Yêu cầu |
|---|---|
| BR-CP-01 | Với chuỗi tuần tự, hệ thống phải cho phép khai báo 1 lệnh gọi "bù trừ" (undo) riêng cho từng step, được tự động kích hoạt khi có 1 step SAU đó trong cùng chuỗi thất bại — nhằm hoàn tác các thay đổi dữ liệu đã thực hiện thành công trước đó. |
| BR-CP-02 | Việc bù trừ phải được thực hiện theo đúng thứ tự ngược lại với thứ tự các step đã thực thi thành công (step chạy sau cùng được bù trừ trước). |
| BR-CP-03 | Thất bại của chính lệnh bù trừ không được làm hệ thống crash hay che mất lỗi gốc — phải ghi nhận đầy đủ để đội vận hành xử lý thủ công nếu cần. |

### 7.5. Hiệu năng & Giảm tải Upstream (Cache)

| Mã | Yêu cầu |
|---|---|
| BR-CH-01 | Hệ thống phải cho phép bật cache riêng cho từng Backend Step (theo thời gian sống tự chọn), tránh gọi lặp lại backend thật với cùng 1 tham số trong khoảng thời gian ngắn. |
| BR-CH-02 | Hệ thống phải cho phép bật cache cho TOÀN BỘ response cuối cùng của 1 Endpoint, dùng chung cho MỌI client gọi cùng tham số — dành cho các API tra cứu, ít thay đổi dữ liệu. |
| BR-CH-03 | Hệ thống phải ngăn (validate chặn khi lưu cấu hình) việc bật cache toàn bộ response cho các Endpoint có khả năng làm thay đổi dữ liệu thật (không phải endpoint tra cứu thuần tuý), tránh rủi ro 2 client khác nhau nhận nhầm kết quả của nhau. |
| BR-CH-04 | Khi hạ tầng cache (Redis) gặp sự cố, hệ thống phải tiếp tục phục vụ request bình thường (không cache), không được làm gián đoạn traffic thật. |

### 7.6. Chống trùng lặp khi client tự động thử lại (Idempotency)

| Mã | Yêu cầu |
|---|---|
| BR-ID-01 | Hệ thống phải cho phép client tự khai báo 1 khoá định danh riêng cho 1 lần gọi (ví dụ khi hệ thống client tự động gửi lại do timeout mạng) để đảm bảo backend thật chỉ thực sự được gọi 1 lần, các lần gọi lại tiếp theo (cùng khoá) nhận đúng kết quả của lần đầu tiên. |
| BR-ID-02 | Kết quả lỗi không được lưu vào cơ chế chống trùng lặp — client vẫn phải gọi lại thành công được sau khi nguyên nhân lỗi thật đã được khắc phục. |

### 7.7. Khả năng chịu lỗi & Bảo vệ hệ thống (Resilience)

| Mã | Yêu cầu |
|---|---|
| BR-RS-01 | Hệ thống phải tự động ngắt tạm thời (circuit breaker) việc gọi tới 1 Upstream Service đang có tỷ lệ lỗi cao, tránh dồn thêm tải lên 1 backend đang gặp sự cố. |
| BR-RS-02 | Hệ thống phải hỗ trợ tự động thử lại (retry) khi gặp lỗi tạm thời (timeout/lỗi hạ tầng), nhưng KHÔNG được thử lại với lỗi nghiệp vụ rõ ràng (ví dụ 400 Bad Request) vì thử lại chắc chắn không thành công. |
| BR-RS-02b | Khi thực thi song song, 1 step thất bại không được ngăn các step độc lập khác đã kịp thực thi trước đó — hệ thống phải chờ TẤT CẢ hoàn tất rồi mới báo lỗi tổng hợp, đồng thời phải cảnh báo rõ cho người khai báo về rủi ro side-effect nếu step có ghi/sửa dữ liệu. |
| BR-RS-03 | Hệ thống phải giới hạn số lệnh gọi đồng thời tối đa tới 1 Upstream Service, tránh 1 Upstream chậm/quá tải kéo cạn tài nguyên (thread) của toàn hệ thống. |
| BR-RS-04 | Hệ thống phải giới hạn tần suất gọi (rate limit) từ phía client vào từng Endpoint, bảo vệ các backend thật phía sau khỏi bị spam/tấn công. |

### 7.8. Nhật ký & Truy vết (Audit & Traceability)

| Mã | Yêu cầu |
|---|---|
| BR-AU-01 | Hệ thống phải ghi nhận đầy đủ mỗi request client gọi vào (thời điểm, endpoint, trạng thái thành công/lỗi, thời gian xử lý). |
| BR-AU-02 | Hệ thống phải ghi nhận chi tiết từng bước gọi Upstream bên trong 1 request (URL đã tạo, request/response, trạng thái, thời gian, có lấy từ cache hay không), phục vụ điều tra sự cố. |
| BR-AU-03 | Hệ thống phải cung cấp giao diện tra cứu nhật ký theo thời gian, trạng thái, đường dẫn endpoint, nội dung request. |
| BR-AU-04 | Việc ghi nhật ký không được làm chậm hoặc chặn traffic thật — nếu hạ tầng ghi log gặp sự cố, request của client vẫn phải được xử lý bình thường. |

### 7.9. Quản lý phiên bản cấu hình (Versioning)

| Mã | Yêu cầu |
|---|---|
| BR-VS-01 | Mỗi lần tạo mới/sửa 1 Endpoint, hệ thống phải tự động lưu lại 1 bản ghi lịch sử (ai/khi nào/nội dung cấu hình đầy đủ tại thời điểm đó). |
| BR-VS-02 | Hệ thống phải cho phép xem lại và khôi phục (rollback) 1 Endpoint về đúng nội dung của 1 phiên bản lịch sử bất kỳ. |

### 7.10. Xem trước trước khi vận hành thật (Preview)

| Mã | Yêu cầu |
|---|---|
| BR-PV-01 | Hệ thống phải cho phép gọi thử 1 Endpoint đã lưu với dữ liệu đầu vào tự nhập, xem kết quả thật (không phải giả lập), phục vụ kiểm thử trước khi đưa vào sử dụng chính thức. |
| BR-PV-02 | Hệ thống phải cho phép xem trước hành vi 1 Endpoint đang trong quá trình khai báo (CHƯA lưu), giúp người dùng phát hiện lỗi cấu hình ngay khi đang thao tác trên giao diện kéo-thả, không phải lưu xong mới phát hiện ra sai. |
| BR-PV-03 | Kết quả xem trước phải hiển thị chi tiết từng bước gọi (không chỉ kết quả cuối cùng) để người dùng biết chính xác bước nào chạy đúng/sai, không cần mở thêm công cụ tra cứu log riêng. |

### 7.11. Tài liệu hoá & Tích hợp (Documentation & Integration)

| Mã | Yêu cầu |
|---|---|
| BR-DC-01 | Hệ thống phải tự sinh được tài liệu đặc tả API (OpenAPI) cho mỗi Endpoint đã khai báo, phục vụ đội khác tích hợp mà không cần hỏi lại đội chủ quản. |
| BR-DC-02 | Hệ thống phải cho phép xuất toàn bộ cấu hình (Upstream Service + Endpoint) ra 1 tệp, và nhập lại tệp đó vào 1 môi trường khác — phục vụ di chuyển cấu hình giữa các môi trường (dev/staging/production). |

### 7.12. Triển khai độc lập theo từng đội (Multi-team Deployment)

| Mã | Yêu cầu |
|---|---|
| BR-DP-01 | Hệ thống phải cho phép 1 đội BCCS bất kỳ tự triển khai 1 instance độc lập, kết nối tới cơ sở dữ liệu (Oracle), cache (Redis), và hệ thống nhật ký (Elasticsearch) RIÊNG của đội đó — không bắt buộc dùng chung hạ tầng với đội khác. |
| BR-DP-02 | Việc khởi tạo cấu trúc dữ liệu (schema) cho 1 instance mới phải tự động, không yêu cầu đội triển khai phải tự chạy tay các câu lệnh SQL phức tạp. |
| BR-DP-03 | Việc nâng cấp lên phiên bản mới của nền tảng không được làm mất cấu hình/dữ liệu đã khai báo của đội đang sử dụng. |

---

## 8. Yêu cầu phi chức năng (Non-Functional Requirements)

| Mã | Hạng mục | Yêu cầu |
|---|---|---|
| NFR-01 | Hiệu năng | Chi phí xử lý nội bộ (thông dịch cấu hình, ánh xạ dữ liệu) của hệ thống phải không đáng kể so với thời gian gọi mạng thật tới các Upstream Service — không trở thành điểm nghẽn hiệu năng so với cách viết code tay tương đương. |
| NFR-02 | Khả dụng | Sự cố ở các hệ thống hỗ trợ không thiết yếu (cache Redis, nhật ký Elasticsearch, giám sát APM) không được làm gián đoạn khả năng phục vụ traffic thật (nguyên tắc "fail-open"). |
| NFR-03 | Bảo mật | Các thao tác quản trị cấu hình (tạo/sửa/xoá Endpoint, Upstream Service) phải được xác thực; traffic nghiệp vụ thật (client gọi API tổng hợp) không bị áp cùng cơ chế xác thực quản trị. |
| NFR-04 | Khả năng mở rộng | Kiến trúc phải cho phép nhiều instance độc lập (nhiều đội) hoạt động song song mà không có trạng thái dùng chung giữa các instance. |
| NFR-05 | Khả năng bảo trì | Việc thêm 1 khả năng/nguồn dữ liệu mới cho Field Mapping hoặc 1 loại điều kiện rẽ nhánh mới không được yêu cầu thay đổi kiến trúc lõi của engine thực thi. |
| NFR-06 | Khả năng vận hành | Đội vận hành phải có đủ công cụ (nhật ký, dashboard sức khoẻ Upstream) để tự chẩn đoán phần lớn sự cố mà không cần truy cập trực tiếp vào code nguồn. |
| NFR-07 | Tương thích hạ tầng | Hệ thống phải hoạt động đúng trên phiên bản cơ sở dữ liệu Oracle mà các đội BCCS đang sử dụng phổ biến (bao gồm phiên bản cũ hơn phiên bản dùng ở môi trường phát triển nội bộ), không phụ thuộc tính năng cơ sở dữ liệu chỉ có ở phiên bản mới nhất. |

---

## 9. Giả định & Ràng buộc (Assumptions & Constraints)

- **Giả định**: các Upstream Service (backend thật) mà Endpoint tổng hợp gọi
  tới đã tồn tại và có API HTTP/REST đồng bộ sẵn sàng — hệ thống không tạo ra
  backend mới, chỉ tổng hợp lời gọi tới backend đã có.
- **Giả định**: người khai báo Endpoint hiểu rõ nghiệp vụ (biết chuỗi bước cần
  gọi, dữ liệu nào cần truyền đi đâu) nhưng không nhất thiết biết lập trình.
- **Ràng buộc**: chỉ hỗ trợ giao tiếp HTTP/REST đồng bộ (không hỗ trợ
  messaging bất đồng bộ, gRPC, WebSocket).
- **Ràng buộc**: việc bật cache toàn bộ response hoặc cache theo từng bước cho
  các lệnh gọi có khả năng làm thay đổi dữ liệu (không phải endpoint/step
  thuần tra cứu) là trách nhiệm của người cấu hình xác nhận đúng bản chất
  nghiệp vụ — hệ thống chỉ chặn được trường hợp rõ ràng nhất (cache toàn bộ
  response cho endpoint/step không phải các phương thức tra cứu quy ước), còn
  lại dựa vào đánh giá nghiệp vụ của người khai báo.
- **Ràng buộc**: cơ chế bù trừ (compensation) là "best-effort" — hệ thống cố
  gắng hoàn tác nhưng không đảm bảo tuyệt đối 100% (ví dụ chính lệnh bù trừ
  cũng có thể thất bại) — không thay thế được giao dịch phân tán có đảm bảo
  toàn vẹn tuyệt đối (2-phase commit).
- **Ràng buộc**: mỗi đội triển khai độc lập tự chịu trách nhiệm về hạ tầng
  (Oracle/Redis/Elasticsearch) của chính đội mình; nền tảng chỉ đảm bảo phần
  mềm hoạt động đúng khi hạ tầng đáp ứng yêu cầu tối thiểu.

---

## 10. Tiêu chí thành công (Success Criteria)

| Mã | Tiêu chí |
|---|---|
| SC-01 | 1 đội nghiệp vụ có thể tự khai báo và đưa vào sử dụng 1 API tổng hợp mới (2-3 bước gọi) trong vòng dưới 30 phút, không cần hỗ trợ từ đội phát triển nền tảng. |
| SC-02 | Mọi Endpoint khai báo qua hệ thống đều có sẵn cache/circuit breaker/audit log mà không cần cấu hình thêm bất kỳ thư viện nào ngoài giao diện quản trị. |
| SC-03 | Khi 1 thay đổi cấu hình (thêm step, đổi field mapping) gây lỗi, đội vận hành có thể tự khôi phục về phiên bản cũ trong vòng vài phút, không cần can thiệp mã nguồn. |
| SC-04 | Ít nhất 1 đội BCCS ngoài đội phát triển nền tảng triển khai thành công 1 instance độc lập trên hạ tầng riêng, làm theo đúng tài liệu hướng dẫn triển khai mà không cần hỏi thêm đội phát triển nền tảng. |

---

## 11. Rủi ro nghiệp vụ đã biết (Known Business Risks)

| # | Rủi ro | Mức ảnh hưởng | Biện pháp giảm thiểu hiện có |
|---|---|---|---|
| R1 | Bật cache toàn bộ response hoặc cache theo bước cho 1 lệnh gọi thực ra có làm thay đổi dữ liệu → 2 client nhận nhầm kết quả của nhau | Cao | Chặn cứng khi lưu nếu endpoint/step không phải phương thức tra cứu quy ước; các trường hợp còn lại cần người cấu hình tự đánh giá đúng |
| R2 | Chạy song song các step trong khi 1 số step có side-effect thật (ghi/sửa dữ liệu) → step khác có thể đã chạy xong trước khi phát hiện lỗi, gây dữ liệu không nhất quán | Trung bình-Cao | Cảnh báo rõ ràng ngay trên giao diện khi bật tính năng chạy song song; khuyến nghị chỉ dùng cho step độc lập/không side-effect |
| R3 | Cơ chế bù trừ (compensation) không đảm bảo tuyệt đối thành công | Trung bình | Ghi nhận đầy đủ khi bù trừ thất bại để xử lý thủ công; không dùng cho nghiệp vụ bắt buộc toàn vẹn tuyệt đối |
| R4 | Mỗi đội tự triển khai độc lập có thể lệch phiên bản cơ sở dữ liệu (Oracle) so với môi trường phát triển nội bộ | Trung bình | Quản lý thay đổi cấu trúc dữ liệu qua công cụ migration có kiểm soát phiên bản, tương thích ngược |

---

## 12. Thuật ngữ (Glossary)

| Thuật ngữ | Giải thích |
|---|---|
| **Endpoint (composite)** | 1 API tổng hợp do người dùng khai báo, gồm 1 hoặc nhiều Backend Step |
| **Upstream Service** | 1 backend thật (service nội bộ BCCS) mà Endpoint gọi tới |
| **Backend Step** | 1 bước gọi cụ thể tới 1 Upstream Service, là thành phần cấu thành 1 Endpoint |
| **Field Mapping** | Quy tắc lấy 1 giá trị từ nguồn nào đó (body/query client, response step trước, hằng số) để truyền cho 1 step |
| **Compensation (bù trừ)** | Lệnh gọi "hoàn tác" tương ứng với 1 step, kích hoạt khi có step sau đó thất bại |
| **Idempotency-Key** | Khoá client tự khai báo để đảm bảo 1 giao dịch chỉ được thực thi đúng 1 lần dù bị gọi lại nhiều lần |
| **Circuit Breaker** | Cơ chế tự động ngắt tạm thời việc gọi tới 1 backend đang có tỷ lệ lỗi cao |
| **Bulkhead** | Cơ chế giới hạn số lệnh gọi đồng thời tới 1 backend |
| **Control Plane** | Phần giao diện/API quản trị cấu hình (tạo/sửa/xoá Endpoint, Upstream Service) |
| **Data Plane** | Phần xử lý traffic thật của client khi gọi vào 1 Endpoint đã khai báo |

---

## 13. Tài liệu tham khảo

- `README.md` — tổng quan kiến trúc và tính năng chi tiết của hệ thống.
- `LOCAL_SETUP.md` — hướng dẫn dựng môi trường phát triển.
- `DEPLOYMENT_GUIDE.md` — hướng dẫn 1 đội BCCS tự triển khai instance riêng.
- Tài liệu **SRS** (Software Requirements Specification) và **FDS**
  (Functional Design Specification) đi kèm — mô tả chi tiết kỹ thuật hiện thực
  hoá các yêu cầu nghiệp vụ trong tài liệu này.
