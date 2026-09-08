# Hướng dẫn 1 đội BCCS tự triển khai Gateway Manager riêng

Tài liệu này dành cho **1 đội BCCS bất kỳ** muốn tự chạy **1 instance Gateway
Manager của riêng mình** (backend composite engine + frontend), trên **hạ tầng
riêng của đội đó** (Oracle 19c riêng, Redis riêng, Elasticsearch riêng - không
dùng chung với đội khác). Khác với [`LOCAL_SETUP.md`](LOCAL_SETUP.md) (dành cho
người PHÁT TRIỂN Gateway Manager, dùng chung hạ tầng `db-local`).

## 1. Yêu cầu

| Hạ tầng | Bắt buộc? | Ghi chú |
|---|---|---|
| Docker + Docker Compose | Bắt buộc | Chạy backend + frontend + Redis |
| Oracle 19c (hoặc mới hơn) | Bắt buộc | Schema riêng của đội, KHÔNG dùng chung schema `BCCS_PRODUCT` với đội khác |
| Elasticsearch | Tuỳ chọn | Chỉ phục vụ trang "Tra cứu Log" - không có vẫn chạy bình thường (fail-open) |
| Elastic APM Server | Tuỳ chọn | Theo dõi hiệu năng - không có agent tự tắt, không chặn traffic thật |

## 2. Chuẩn bị Oracle 19c — DBA đội tự chạy DDL, ứng dụng KHÔNG tự tạo bảng

**Ứng dụng không có khả năng tự tạo/sửa schema.** Lý do: Oracle 19c của từng
đội BCCS thường là **hạ tầng đã vận hành từ trước, do DBA quản trị** — user
cấp cho ứng dụng chạy thường CHỈ có quyền DML (SELECT/INSERT/UPDATE/DELETE),
KHÔNG có quyền DDL (`CREATE TABLE`). Từng thử để ứng dụng tự động tạo schema
(qua Flyway) lúc khởi động, nhưng cách đó không phù hợp thực tế này nên đã bỏ
— xem `docs/SAD-Gateway-Manager.md` (ADR-03, bản sửa lại).

**Quy trình bắt buộc trước khi chạy ứng dụng lần đầu**:

1. Gửi file `backend/src/main/resources/db/team-schema/V1__baseline.sql` cho
   **DBA/người quản trị Oracle của đội**.
2. DBA đối chiếu 8 tên bảng trong file (`UPSTREAM_SERVICE`, `ENDPOINT_CONFIG`,
   `BACKEND_STEP`, `BACKEND_STEP_ALLOW`, `BACKEND_STEP_DENY`,
   `BACKEND_STEP_MAPPING`, `FIELD_MAPPING`, `ENDPOINT_CONFIG_VERSION`) với các
   bảng ĐÃ CÓ trong schema định dùng — **nếu là schema dùng chung với hệ thống
   khác của đội, phải đảm bảo không trùng tên** trước khi chạy.
3. DBA tự chạy file này (nguyên vẹn, theo đúng quy trình change-management nội
   bộ của đội) trên schema/user dành cho Gateway Manager.
4. Sau khi 8 bảng đã có, DBA cấp cho user **RUNTIME** của ứng dụng CHỈ quyền
   DML trên đúng 8 bảng này (không cần quyền DDL) — đây chính là user điền vào
   `DB_USER`/`DB_PASSWORD` ở mục 3 dưới đây.
5. Ứng dụng khi khởi động chỉ **đối chiếu** (`hibernate.ddl-auto=validate`)
   entity Java với schema thật — nếu thiếu bảng/cột hoặc sai kiểu dữ liệu, ứng
   dụng sẽ **không khởi động được** kèm thông báo lỗi rõ ràng (an toàn — không
   bao giờ tự ý sửa schema).

> Vì sao trước đây định dùng Flyway/`ddl-auto=update` tự sinh schema nhưng đã
> bỏ: `ddl-auto=update` chưa từng được xác nhận đúng trên Oracle 19c thật (chỉ
> test 23c) và có lỗi đã biết (không tự nới `CHECK` constraint khi thêm enum
> mới). Flyway tự động migrate lúc khởi động thì lại giả định user runtime có
> quyền DDL — **không đúng với thực tế hạ tầng Oracle 19c của các đội BCCS**
> (do DBA quản trị, chỉ cấp DML). Giải pháp cuối cùng: tách hẳn việc TẠO SCHEMA
> (DBA tự làm, 1 lần, ngoài vòng đời ứng dụng) khỏi việc CHẠY ứng dụng.

## 3. Cấu hình `.env`

```bash
cd krakend-gateway-manager
cp .env.example .env
```

Mở `.env`, điền theo hạ tầng của đội:

```dotenv
TEAM_CODE=ten-doi-ban              # vd "vcom", "billing"... - hien trong ten service/APM
DB_HOST=oracle.noi-bo-doi-ban.local
DB_PORT=1521
DB_NAME=ten-service-hoac-pdb-cua-ban
DB_USER=GATEWAY_MANAGER
DB_PASSWORD=<mat-khau-that>
GATEWAY_ADMIN_API_KEY=<tu-sinh-1-key-manh>   # KHONG duoc de mac dinh "changeme-local-dev"
```

Elasticsearch/APM: nếu đội **chưa có**, để nguyên mặc định là đủ (tính năng tự
tắt an toàn, không chặn traffic thật) - hoặc set `GATEWAY_AUDIT_ENABLED=false`
để tắt hẳn phần ghi log. Nếu đội **có sẵn** ES/APM riêng, điền
`GATEWAY_AUDIT_ES_HOST`/`ES_PORT`/`APM_SERVER_HOST`/`APM_SERVER_PORT`. Xem đầy
đủ giải thích từng biến trong chính file `.env.example`.

## 4. Chạy

```bash
docker compose up -d --build
```

**Điều kiện tiên quyết**: 8 bảng đã được DBA tạo xong theo đúng mục 2 — nếu
chưa, backend sẽ báo lỗi `SchemaManagementException` (thiếu bảng/cột) ngay khi
khởi động và dừng lại, KHÔNG tự tạo gì cả.

Lần đầu, backend sẽ:
1. Kết nối Oracle bằng user RUNTIME (chỉ quyền DML), đối chiếu entity với 8
   bảng đã có sẵn (`hibernate.ddl-auto=validate`).
2. `DataSeeder` seed **1 endpoint mẫu** (`GET /v1/user-orders/{userId}`) để có
   ngay 1 ví dụ tham khảo cấu trúc - xoá được qua UI nếu không cần.
3. Backend + frontend lên, UI ở `http://localhost:4200`.

Kiểm tra nhanh:
```bash
curl http://localhost:4200/api/endpoints -H "X-Gateway-Admin-Key: <key-ban-vua-dat>"
```

## 5. Khai báo nghiệp vụ đầu tiên

1. Vào **Upstream Services** → đăng ký các backend thật đội cần gọi (host,
   timeout, circuit breaker...).
2. Vào **Endpoints** (hoặc trang **Canvas** kéo-thả) → khai báo endpoint
   composite đầu tiên, tham chiếu tới Upstream Service vừa tạo.
3. Dùng nút **"Thử nhanh"** (trên Canvas) để xem trước request/response từng
   step ngay khi đang cấu hình, chưa cần lưu.

## 6. Nâng cấp lên version mới

```bash
git pull            # hoặc doi tag image neu dung registry rieng
```

Nếu bản mới có thay đổi cấu trúc dữ liệu, đội phát triển nền tảng sẽ ban hành
kèm 1 file DDL tăng dần mới (vd `V2__...sql`, đặt cùng thư mục
`db/team-schema/`) — **lặp lại đúng quy trình mục 2** (gửi cho DBA, DBA tự chạy
trên schema đã có), rồi mới `docker compose up -d --build`. KHÔNG tự khởi động
lại ứng dụng trước khi DBA đã áp dụng DDL mới — `ddl-auto=validate` sẽ chặn
khởi động và báo lỗi rõ ràng nếu chạy nhầm thứ tự.

## 7. Sự cố thường gặp

| Triệu chứng | Nguyên nhân khả dĩ |
|---|---|
| Backend không lên, log `SchemaManagementException` (`Schema validation: missing table`/`wrong column type`) | DBA chưa chạy `V1__baseline.sql` (hoặc bản DDL nâng cấp mới nhất), hoặc user RUNTIME trỏ nhầm schema chưa có bảng — quay lại mục 2 |
| DBA báo lỗi thiếu quyền khi chạy `V1__baseline.sql` | User DBA dùng để chạy DDL cần quyền `CREATE TABLE`/`CREATE INDEX` trên schema đích - khác với user RUNTIME (chỉ cần DML) điền trong `.env` |
| `401 Unauthorized` khi gọi `/api/**` | Thiếu/sai header `X-Gateway-Admin-Key` - phải khớp đúng `GATEWAY_ADMIN_API_KEY` đã đặt trong `.env` |
| Trang "Tra cứu Log" trống/lỗi | Elasticsearch chưa cấu hình/không kết nối được - không ảnh hưởng chức năng chính, chỉ tính năng xem log bị tắt |
| Endpoint gọi ra Upstream bị `BulkheadFullException`/timeout | Kiểm tra `maxConcurrentCalls`/`connectTimeoutMs` của Upstream Service tương ứng - có thể cần tăng nếu backend thật của đội chậm/tải cao |
