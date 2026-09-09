# Hướng dẫn dựng hạ tầng local cho Gateway Manager

Tài liệu này hướng dẫn dựng **toàn bộ hệ thống chạy được trên máy local**:
Gateway Manager (UI + Control Plane + engine thực thi) **và** ít nhất 1
service BCCS thật để có dữ liệu thật khai báo composite API — đúng những gì
đã build/test thành công trong repo này.

## 1. Kiến trúc tổng quan

**TỪ 2026-09: Gateway Manager tách 2 vai trò (Control Plane/Data Plane, xem
`docs/SAD-Gateway-Manager.md` ADR-07)** — để test end-to-end trên local cần
chạy **CẢ 2** container backend (2 file docker-compose riêng), không còn chỉ
1 container backend như trước.

```
┌───────────────────────────────┐      ┌─────────────────────────────────┐
│ docker-compose.control-plane.yml│      │ docker-compose.data-plane.yml    │
│                                  │      │                                   │
│ frontend :4200 → backend :8080  │◄─────┤ backend :8081 (poll dinh ky       │
│ (control-plane)     │ (JDBC)     │ HTTP │  GET /api/config/export)         │
│                      ▼           │      │        │                          │
└──────────────────────┼───────────┘      │      redis :6379 (rieng)         │
         host.docker.internal:1521        └───────────────┼──────────────────┘
                       ▼                        host.docker.internal:PORT
              Oracle CHUNG voi BCCS                       ▼
              (db-local), schema             ┌────────────────────┐
              BCCS_PRODUCT                   │ Service BCCS chạy  │
                                              │ TRỰC TIẾP trên máy │
                                              │ host (vd organiza- │
                                              │ tion-resource-     │
                                              │ service :8004,     │
                                              │ dùng mvnw)         │
                                              └────────────────────┘
```

**Lưu ý quan trọng**:
- **Control Plane** (port 8080) chạy trong Docker, kết nối Oracle CHUNG với
  các service BCCS (`db-local`) — bắt buộc dựng `db-local` trước (mục 2).
- **Data Plane** (port 8081, KHÔNG kết nối Oracle) tự đồng bộ Endpoint/Upstream
  từ Control Plane qua HTTP — cần tạo 1 "đội" qua màn hình **"Quản lý đội"**
  trên UI Control Plane trước (xem mục 4.3), lấy `team_code`+`api_key` điền
  vào `.env` của Data Plane.
- Service BCCS thật (để test) chạy **ngoài Docker** trên máy host qua Maven —
  setup "dev thường ngày" đã verify hoạt động đúng.

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

## 4. Bước 2 — Dựng Gateway Manager (Control Plane + Data Plane)

### 4.1. Dựng Control Plane trước

```powershell
cd krakend-gateway-manager
cp .env.control-plane.example .env
docker compose -f docker-compose.control-plane.yml up -d --build
```

Mặc định `.env.control-plane.example` đã trỏ sẵn Oracle qua
`host.docker.internal:1521`, schema `BCCS_PRODUCT` (đúng credential dùng
chung với BCCS) — không cần sửa gì thêm.

```powershell
docker compose -f docker-compose.control-plane.yml ps
```

Kỳ vọng thấy `gwm-cp-redis`, `gwm-backend`, `gwm-frontend` đều `Up`. Mở trình
duyệt: **http://localhost:4200**, đăng nhập bằng `GATEWAY_ADMIN_API_KEY`
trong `.env`.

### 4.2. Tạo 1 "đội" cho local dev (chỉ cần làm 1 lần)

Trên UI, vào **"Quản lý đội"** → tạo 1 đội (vd `teamCode=local`,
`teamName=Dev local`) → **lưu lại `apiKey` hiện ra** (chỉ hiện đúng 1 lần).

### 4.3. Dựng Data Plane

```powershell
cp .env.data-plane.example .env.dataplane
# Sua .env.dataplane: TEAM_CODE=local, CONTROL_PLANE_BASE_URL=http://host.docker.internal:8080,
# CONTROL_PLANE_SYNC_API_KEY=<apiKey vua tao o 4.2>
docker compose -f docker-compose.data-plane.yml --env-file .env.dataplane up -d --build
```

Verify đã đồng bộ được cấu hình từ Control Plane:

```powershell
docker compose -f docker-compose.data-plane.yml logs backend | Select-String "Da dong bo"
curl http://localhost:8081/actuator/health/readiness   # phai {"status":"UP"}
```

### 4.4. Verify từng lớp

```powershell
# Control Plane CRUD (port 8080)
curl http://localhost:8080/api/endpoints -H "X-Gateway-Admin-Key: <apiKey doi local>"

# Frontend + proxy /api sang Control Plane
curl http://localhost:4200/api/endpoints -H "X-Gateway-Admin-Key: <apiKey doi local>"

# Data Plane - traffic that (port 8081, KHONG can header)
curl http://localhost:8081/v1/khong-ton-tai   # ky vong 404 (chua khai bao endpoint nao)
```

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

Bấm **Lưu** — có hiệu lực trên Control Plane ngay, không còn bước "Deploy"
riêng. Có hiệu lực trên **Data Plane** (nơi traffic thật thực sự chạy) trong
tối đa `CONTROL_PLANE_SYNC_INTERVAL_SECONDS` (mặc định 15s, xem mục 4.3).

Test qua gateway thật — **port 8081 (Data Plane), KHÔNG phải 8080** (8080 là
Control Plane, chỉ phục vụ CRUD/UI, không còn dispatch traffic từ 2026-09):

```powershell
curl http://localhost:8081/v1/staff/102137
```

Xem thêm ví dụ composite nhiều tầng, gộp mảng, forward body client trong
[README.md](README.md).

## 6. Lệnh vận hành thường dùng

Luôn cần `-f docker-compose.control-plane.yml` hoặc `-f docker-compose.data-plane.yml
--env-file .env.dataplane` (tuỳ container nào) — không còn 1 file mặc định
duy nhất từ 2026-09.

```powershell
# Xem log
docker compose -f docker-compose.control-plane.yml logs backend --tail 50
docker compose -f docker-compose.data-plane.yml --env-file .env.dataplane logs backend --tail 50

# Rebuild + restart Control Plane sau khi sửa code
docker compose -f docker-compose.control-plane.yml build backend
docker compose -f docker-compose.control-plane.yml up -d backend

# Dừng toàn bộ (giữ dữ liệu Oracle/Redis)
docker compose -f docker-compose.control-plane.yml down
docker compose -f docker-compose.data-plane.yml down

# Dừng va xoa du lieu Redis rieng cua Data Plane (Oracle KHONG bi anh huong vi la ha tang dung chung db-local)
docker compose -f docker-compose.data-plane.yml down -v
```

## 7. Các lỗi thường gặp

| Triệu chứng | Nguyên nhân | Cách xử lý |
|---|---|---|
| `docker: Docker Desktop is unable to start` | Docker Desktop chưa khởi động xong | Mở app, đợi "Running", thử lại |
| Backend không kết nối được Oracle (`Connection refused`/timeout) | `db-local` chưa chạy hoặc Oracle chưa `healthy` | Dựng `db-local` trước (mục 2), đợi `healthy` |
| Lombok không sinh code (`cannot find symbol: getX()`, biến `log` không tồn tại) khi build local bằng `mvnw` trên JDK mới (vd JDK 25) | JDK 25 bỏ hẳn cơ chế tự dò annotation processor qua classpath (JDK 21 chỉ cảnh báo) | Đã fix trong `backend/pom.xml` bằng `annotationProcessorPaths` khai rõ Lombok — không phụ thuộc hành vi tự dò của javac |
| Build package thất bại `Unsupported class file major version 69` | `spring-boot-maven-plugin` 3.3.4 chưa hỗ trợ repackage bytecode JDK 25 | `pom.xml` đã dịch bytecode target về JDK 21 (`java.version`) dù compiler chạy bằng JDK 25 vẫn được — Dockerfile dùng đúng JDK 21 để build/runtime |
| Endpoint composite gọi ra `connection refused` tới `host.docker.internal:PORT` | Service BCCS thật (Bước 1) chưa chạy hoặc đã bị tắt | Khởi động lại theo Bước 3, verify bằng `curl localhost:PORT/...` từ máy host trước |
| `LazyInitializationException` khi Control Plane khởi động lại (có dữ liệu thật trong DB) | `@PostConstruct` tự gọi method `@Transactional` trong cùng class (self-invocation) khiến Spring bỏ qua proxy transaction | Đã fix ở bản gốc; từ 2026-09 `EndpointRegistryCache`/`UpstreamRegistryCache` không còn tự đọc JPA nữa (xem SAD ADR-07) — chỉ còn `UpstreamServiceService.loadUpstreamRegistryCacheAtStartup()` (Control Plane) gọi `repository.findAll()` trực tiếp, tự transactional sẵn (`SimpleJpaRepository`), không cần `TransactionTemplate` nữa |
| `POST /api/config/deploy` trả 400 `GW-CYCLE` | Có 2 endpoint composite gọi vòng vào nhau (A→B→A) | Xem cột **"Phụ thuộc"** trên trang **Endpoints** (badge "Vòng lặp" đỏ) — trang "Sơ đồ phụ thuộc" riêng đã bị gỡ khỏi UI, thông tin vòng lặp giờ hiển thị ngay trong bảng |
| Trang **Tra cứu Log** (`/logs`) báo lỗi/không thấy log mới | Elasticsearch (`bccs-elasticsearch`, `db-local`) chưa chạy, hoặc `GATEWAY_AUDIT_ENABLED=false` | Dựng `db-local` trước (mục 2), verify `curl http://localhost:9200`; xem mục 8 |

## 8. Giám sát local: Elasticsearch/Kibana + Elastic APM (đã có sẵn, không cần dựng thêm)

`db-local` (mục 2) đã kèm sẵn `bccs-elasticsearch`, `bccs-kibana` **và**
`apm-server` — không cần cài đặt gì riêng, chỉ cần `db-local` đang chạy là
cả 2 file `docker-compose.control-plane.yml`/`docker-compose.data-plane.yml`
của Gateway Manager (mục 4) tự trỏ tới qua `host.docker.internal` (đúng cách
Oracle đang làm ở riêng Control Plane).

```powershell
# Verify Elasticsearch (audit log)
curl http://localhost:9200

# Verify APM Server
curl http://localhost:8200
```

Verify audit log thật đang chảy vào ES (sau khi gọi thử 1 endpoint composite
qua `http://localhost:8080`):

```powershell
curl "http://localhost:9200/gwm-requests-*/_count"
```

Mở UI **http://localhost:4200/logs** để lọc/xem log bằng trang Tra cứu Log,
hoặc mở **http://localhost:5601** (Kibana) để khám phá thô/xem APM trace.
Chi tiết field từng index, cách tắt (`GATEWAY_AUDIT_ENABLED=false`): xem mục
7 trong [README.md](README.md).

## 9. Dừng toàn bộ hệ thống

```powershell
# Gateway Manager
cd krakend-gateway-manager
docker compose down

# Service BCCS
# Ctrl+C trong terminal dang chay mvnw

# Ha tang dung chung (Oracle/Redis/Elasticsearch/Kibana/APM Server) - dung neu khong con service BCCS nao can
cd db-local
docker compose down
```
