# Hướng dẫn dựng hạ tầng local cho Gateway Manager

Tài liệu này hướng dẫn dựng **toàn bộ hệ thống chạy được trên máy local**:
Gateway Manager (UI + Control Plane + engine thực thi) **và** ít nhất 1
service BCCS thật để có dữ liệu thật khai báo composite API — đúng những gì
đã build/test thành công trong repo này.

## 1. Kiến trúc tổng quan

```
┌──────────────────────────────────────────────────────┐
│  Docker: krakend-gateway-manager/docker-compose.yml    │
│                                                          │
│  frontend (Angular/Nginx :4200) → backend (Spring :8080)│
│                            │              │ (JDBC)       │
│                          redis :6379      │              │
└────────────────────────────┼──────────────┼─────────────┘
                              │              │ host.docker.internal:1521
                (host.docker.internal:PORT)  ▼
                              ▼        Oracle CHUNG voi BCCS (db-local),
                 ┌────────────────────┐ schema BCCS_PRODUCT
                 │ Service BCCS chạy  │
                 │ TRỰC TIẾP trên máy │
                 │ host (vd organiza- │
                 │ tion-resource-     │
                 │ service :8004,     │
                 │ dùng mvnw)         │
                 └────────────────────┘
```

**Lưu ý quan trọng**:
- Gateway Manager chạy trong Docker, còn service BCCS thật (để test) chạy
  **ngoài Docker** trên máy host qua Maven — setup "dev thường ngày" đã verify
  hoạt động đúng.
- Gateway Manager **dùng chung Oracle** với các service BCCS (không còn
  Postgres riêng) — bắt buộc phải dựng `db-local` trước (xem mục 2).

## 2. Yêu cầu hệ thống + hạ tầng dùng chung

| Công cụ | Ghi chú |
|---|---|
| Docker Desktop | Bắt buộc |
| JDK 21+ | Cần cho service BCCS chạy qua `mvnw` (JDK 25 cũng chạy được nhờ `annotationProcessorPaths` khai rõ trong `pom.xml` — xem mục 7) |
| Node.js 20+ | Chỉ cần nếu muốn `npm install`/`ng build` frontend ngoài Docker |

### Dựng Oracle + Redis dùng chung trước (bắt buộc)

```powershell
cd db-local
docker compose up -d
```

Đợi Oracle `healthy` (1-2 phút lần đầu):

```powershell
docker ps --filter "name=bccs-oracle" --format "table {{.Names}}\t{{.Status}}"
```

Chi tiết đầy đủ: xem [`../db-local/README.md`](../db-local/README.md).

## 3. Bước 1 — Dựng 1 service BCCS thật để test cùng

```powershell
cd organization-resource-service
copy .env.example .env
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--bccs.kafka.enabled=false --bccs.cache.mode=memory-only"
```

Chờ log in ra `Started OrganizationResourceServiceApplication...`. Mặc định
service chạy ở **port 8004**.

Verify:

```powershell
curl http://localhost:8004/organization-resource-service/v1/staff/getActiveById/1
```

Nhận về JSON (kể cả lỗi "không tìm thấy" cũng OK, miễn không phải connection
refused) nghĩa là service đã chạy đúng.

## 4. Bước 2 — Dựng Gateway Manager

```powershell
cd krakend-gateway-manager
docker compose up -d --build
```

Mặc định `docker-compose.yml` đã trỏ sẵn Oracle qua
`host.docker.internal:1521`, schema `BCCS_PRODUCT` (đúng credential dùng
chung với BCCS) — không cần tạo `.env` riêng trừ khi muốn đổi khác.

### 4.1. Verify hệ thống đã lên đủ 3 container

```powershell
docker compose ps
```

Kỳ vọng thấy `gwm-redis` (healthy), `gwm-backend`, `gwm-frontend` đều `Up`.

### 4.2. Verify từng lớp

```powershell
# Control Plane + Data Plane API (backend, port 8080)
curl http://localhost:8080/api/endpoints

# Frontend + proxy /api sang backend
curl http://localhost:4200/api/endpoints
```

Mở trình duyệt: **http://localhost:4200**

## 5. Khai báo endpoint đầu tiên gọi vào service ở Bước 1

### 5.1. Đăng ký Upstream Service (1 lần)

Vào trang **Upstream Services** trên UI, tạo mới:

| Field | Giá trị |
|---|---|
| Tên | `organization-resource-service` |
| Base host | `http://host.docker.internal:8004` |

**Vì sao là `host.docker.internal` chứ không phải `localhost`**: backend chạy
**trong container Docker**, còn service BCCS chạy **trực tiếp trên máy
Windows** (Bước 1). Bên trong 1 container, `localhost` luôn trỏ về chính
container đó — không phải máy host. `host.docker.internal` là DNS đặc biệt
Docker Desktop cung cấp để container gọi ngược ra máy host.

### 5.2. Khai báo Endpoint

Bấm **"Endpoint mới"**:

| Field | Giá trị |
|---|---|
| Path | `/v1/staff/{staffId}` |
| Method | `GET` |
| Upstream Service (step 1) | `organization-resource-service` |
| URL pattern (step 1) | `/organization-resource-service/v1/staff/getActiveById/{staffId}` |
| Target | `data` (API BCCS luôn bọc kết quả trong field `data`) |

Bấm **Lưu** — có hiệu lực ngay, không còn bước "Deploy" riêng.

Test qua gateway thật:

```powershell
curl http://localhost:8080/v1/staff/102137
```

Xem thêm ví dụ composite nhiều tầng, gộp mảng, forward body client trong
[README.md](README.md).

## 6. Lệnh vận hành thường dùng

```powershell
# Xem log
docker compose logs backend --tail 50

# Rebuild + restart 1 container sau khi sửa code
docker compose build backend
docker compose up -d backend

# Dừng toàn bộ (giữ dữ liệu Oracle/Redis)
docker compose down

# Dừng va xoa du lieu Redis (Oracle KHONG bi anh huong vi la hạ tầng dung chung db-local)
docker compose down -v
```

## 7. Các lỗi thường gặp

| Triệu chứng | Nguyên nhân | Cách xử lý |
|---|---|---|
| `docker: Docker Desktop is unable to start` | Docker Desktop chưa khởi động xong | Mở app, đợi "Running", thử lại |
| Backend không kết nối được Oracle (`Connection refused`/timeout) | `db-local` chưa chạy hoặc Oracle chưa `healthy` | Dựng `db-local` trước (mục 2), đợi `healthy` |
| Lombok không sinh code (`cannot find symbol: getX()`, biến `log` không tồn tại) khi build local bằng `mvnw` trên JDK mới (vd JDK 25) | JDK 25 bỏ hẳn cơ chế tự dò annotation processor qua classpath (JDK 21 chỉ cảnh báo) | Đã fix trong `backend/pom.xml` bằng `annotationProcessorPaths` khai rõ Lombok — không phụ thuộc hành vi tự dò của javac |
| Build package thất bại `Unsupported class file major version 69` | `spring-boot-maven-plugin` 3.3.4 chưa hỗ trợ repackage bytecode JDK 25 | `pom.xml` đã dịch bytecode target về JDK 21 (`java.version`) dù compiler chạy bằng JDK 25 vẫn được — Dockerfile dùng đúng JDK 21 để build/runtime |
| Endpoint composite gọi ra `connection refused` tới `host.docker.internal:PORT` | Service BCCS thật (Bước 1) chưa chạy hoặc đã bị tắt | Khởi động lại theo Bước 3, verify bằng `curl localhost:PORT/...` từ máy host trước |
| `LazyInitializationException` khi backend khởi động lại (có dữ liệu thật trong DB) | `@PostConstruct` tự gọi method `@Transactional` trong cùng class (self-invocation) khiến Spring bỏ qua proxy transaction | Đã fix bằng `TransactionTemplate` trong `EndpointRegistryCache`/`UpstreamRegistryCache` (không phụ thuộc AOP proxy) |
| `POST /api/config/deploy` trả 400 `GW-CYCLE` | Có 2 endpoint composite gọi vòng vào nhau (A→B→A) | Mở trang **Sơ đồ phụ thuộc** trên UI để xem chính xác vòng lặp |

## 8. Dừng toàn bộ hệ thống

```powershell
# Gateway Manager
cd krakend-gateway-manager
docker compose down

# Service BCCS
# Ctrl+C trong terminal dang chay mvnw

# Ha tang dung chung (Oracle/Redis/Elasticsearch) - dung neu khong con service BCCS nao can
cd db-local
docker compose down
```
