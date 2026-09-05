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

## 2. Chuẩn bị Oracle 19c

Tạo 1 schema/user trống (ví dụ tên `GATEWAY_MANAGER`, tuỳ đội đặt) với quyền
`CREATE TABLE`/`CREATE INDEX`/`CREATE SEQUENCE` bình thường. **Không cần chạy
tay bất kỳ script SQL nào trước** - từ phiên bản này, schema được tạo tự động
bởi **Flyway** ngay lần khởi động đầu tiên của backend (đọc
`backend/src/main/resources/db/migration/V1__baseline.sql` - chỉ tạo 8 bảng
trống, không có dữ liệu mẫu nào cả).

> Vì sao không còn dùng `ddl-auto=update` (Hibernate tự sinh schema) như trước
> đây: cách đó chưa từng được xác nhận đúng trên Oracle 19c thật (chỉ test trên
> 23c), và có lỗi đã biết - không tự nới được `CHECK` constraint khi thêm giá
> trị enum mới vào bảng đã tồn tại. Flyway migration (SQL viết tay, review được)
> tránh hẳn 2 rủi ro này.

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

Lần đầu, backend sẽ:
1. Kết nối Oracle, thấy schema trống → Flyway chạy `V1__baseline.sql`, tạo 8
   bảng.
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
docker compose up -d --build
```

Flyway tự động áp các migration mới (`V2__...sql`, `V3__...sql`...) chưa từng
chạy trên schema của đội, theo đúng thứ tự - không cần thao tác tay, không mất
dữ liệu đã khai báo.

## 7. Sự cố thường gặp

| Triệu chứng | Nguyên nhân khả dĩ |
|---|---|
| Backend không lên, log `FlywayException`/`SchemaManagementException` | Schema Oracle không trống VÀ không phải instance đã từng chạy Flyway trước đó (vd đội tái sử dụng 1 schema cũ có bảng trùng tên) - dọn sạch schema hoặc đổi `DB_NAME`/user khác |
| `401 Unauthorized` khi gọi `/api/**` | Thiếu/sai header `X-Gateway-Admin-Key` - phải khớp đúng `GATEWAY_ADMIN_API_KEY` đã đặt trong `.env` |
| Trang "Tra cứu Log" trống/lỗi | Elasticsearch chưa cấu hình/không kết nối được - không ảnh hưởng chức năng chính, chỉ tính năng xem log bị tắt |
| Endpoint gọi ra Upstream bị `BulkheadFullException`/timeout | Kiểm tra `maxConcurrentCalls`/`connectTimeoutMs` của Upstream Service tương ứng - có thể cần tăng nếu backend thật của đội chậm/tải cao |
