# Hạ tầng Docker local dùng chung cho các service BCCS

Tài liệu này mô tả **đúng hạ tầng Docker đang chạy thật trên máy hiện tại**
(Oracle, Elasticsearch, Redis) — dùng chung cho 6 service BCCS
(`organization-resource-service`, `product-catalog-service`,
`product-policy-service`, `product-area-service`, `product-price-service`,
`spec-common-service`), không phải setup riêng cho từng service.

## 1. Hạ tầng đang chạy (kiểm tra bằng `docker ps`)

| Container | Image | Port host | Trạng thái |
|---|---|---|---|
| `bccs-oracle` | `gvenzl/oracle-free:23-slim` | `1521:1521` | healthy |
| `bccs-elasticsearch` | `docker.elastic.co/elasticsearch/elasticsearch:8.15.3` | `9200:9200` | healthy |
| `bccs-redis` | `redis:7-alpine` | `6379:6379` | up |

Cả 3 nằm chung network Docker `bccs-db-local_default`.

## 2. Dựng Oracle + Elasticsearch

Compose file: [`docker-compose.yml`](docker-compose.yml) trong chính thư mục này
(project name `bccs-db-local`).

```powershell
cd db-local
docker compose up -d
```

Đợi Oracle chuyển `healthy` (thường 1-2 phút lần đầu):

```powershell
docker ps --filter "name=bccs-oracle" --format "table {{.Names}}\t{{.Status}}"
```

### Credential mặc định (đọc từ `environment` trong compose file)

| Biến `.env` (tuỳ chọn override) | Giá trị mặc định |
|---|---|
| `DB_USERNAME` | `BCCS_PRODUCT` |
| `DB_PASSWORD` | `BCCS_PRODUCT123` |
| `DB_ROOT_PASSWORD` | `bccs_root` |
| `DB_PORT` | `1521` |
| `ES_PORT` | `9200` |

Muốn đổi, tạo file `db-local/.env` với các biến trên trước khi `docker compose up -d`.

### Tự động seed schema + dữ liệu mẫu lần đầu

Thư mục [`init/`](init/) được mount vào `/container-entrypoint-initdb.d` của
image `gvenzl/oracle-free` — **Oracle tự động chạy các script SQL này theo thứ
tự tên file khi khởi tạo DB lần đầu** (chỉ chạy 1 lần, khi volume `oradata`
còn trống):

| File | Nội dung |
|---|---|
| `01_schema.sql` | Tạo toàn bộ bảng (STAFF, SHOP, CHANNEL_TYPE...) |
| `02_sample_data.sql` | Dữ liệu mẫu thật (ví dụ bảng STAFF có ~182.000 dòng, SHOP ~109.000 dòng) |
| `03_vas_exclusive_group.sql` | Dữ liệu nhóm VAS |

> Nếu muốn seed lại từ đầu: `docker compose down -v` (xoá volume `oradata`)
> rồi `docker compose up -d` lại — Oracle sẽ chạy lại toàn bộ script trong `init/`.

## 3. Dựng Redis

**Container `bccs-redis` đang chạy KHÔNG dùng file compose nào có sẵn trong
repo** (không phải `docker-compose.redis.local.yml` của
`organization-resource-service` — file đó đặt tên container khác là
`bccs-redis-local`). Để dựng lại đúng y hệt container Redis đang chạy thật:

```powershell
docker run -d --name bccs-redis --network bccs-db-local_default -p 6379:6379 redis:7-alpine redis-server --appendonly yes
```

> Lưu ý: lệnh trên nối Redis vào cùng network `bccs-db-local_default` với
> Oracle/Elasticsearch (network này được tạo tự động khi bạn `docker compose
> up` ở Bước 2) — nên **phải dựng Oracle/Elasticsearch trước** rồi mới chạy
> lệnh Redis này.

## 4. Trỏ service BCCS vào hạ tầng trên

Trong `.env` của từng service (ví dụ `organization-resource-service/.env`,
copy từ `.env.example`):

```env
DB_HOST=localhost
DB_PORT=1521
DB_NAME=FREEPDB1
DB_USERNAME=BCCS_PRODUCT
DB_PASSWORD=BCCS_PRODUCT123

REDIS_HOST=localhost
REDIS_PORT=6379
```

Chạy service (không cần Kafka lúc dev để đỡ tốn tài nguyên):

```powershell
cd organization-resource-service
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--bccs.kafka.enabled=false --bccs.cache.mode=memory-only"
```

Verify:

```powershell
curl http://localhost:8004/organization-resource-service/v1/staff/getActiveById/102137
```

## 5. Kiểm tra nhanh toàn bộ hạ tầng

```powershell
docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}"
```

Kỳ vọng thấy `bccs-oracle`, `bccs-elasticsearch`, `bccs-redis` đều `Up`
(Oracle/ES thêm `(healthy)`).

## 6. Dừng hạ tầng

```powershell
# Dung Oracle + Elasticsearch (giu du lieu)
cd db-local
docker compose down

# Dung Redis
docker stop bccs-redis && docker rm bccs-redis

# XOA SACH du lieu Oracle/ES (reset seed data ve trang thai ban dau) - CAN THAN
cd db-local
docker compose down -v
```

## 7. Liên quan

- Hướng dẫn dựng riêng **Gateway Manager** (Angular UI + Control Plane +
  KrakenD) chạy trên nền hạ tầng này: xem
  [`../krakend-gateway-manager/LOCAL_SETUP.md`](../krakend-gateway-manager/LOCAL_SETUP.md).
