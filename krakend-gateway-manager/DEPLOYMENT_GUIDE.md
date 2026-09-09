# Hướng dẫn triển khai Gateway Manager (Control Plane dùng chung / Data Plane từng đội)

Tài liệu này mô tả quy trình triển khai **2 vai trò riêng biệt** (từ 2026-09,
xem `docs/SAD-Gateway-Manager.md` ADR-07) — khác hẳn mô hình cũ (mỗi đội tự
chạy 1 instance đầy đủ, độc lập hoàn toàn). Khác với
[`LOCAL_SETUP.md`](LOCAL_SETUP.md) (dành cho người PHÁT TRIỂN Gateway
Manager, dùng chung hạ tầng `db-local`).

## 0. Mô hình tổng quan — 2 vai trò, cùng 1 image, chọn qua `SPRING_PROFILES_ACTIVE`

| | **Control Plane** | **Data Plane** |
|---|---|---|
| Ai triển khai | **Đội nền tảng** (chúng ta) — 1 lần duy nhất | **Mỗi đội BCCS** — tự triển khai riêng, N bản độc lập |
| Gồm | UI Angular + backend (`SPRING_PROFILES_ACTIVE=control-plane`) | Backend (`SPRING_PROFILES_ACTIVE=data-plane`) + Redis riêng |
| Làm gì | CRUD Endpoint/Upstream/Team (`/api/**`), đọc/ghi **trực tiếp** Oracle trung tâm | Thực thi traffic thật qua `DynamicDispatcherController` |
| Oracle | Kết nối trực tiếp, **dùng chung** cho mọi đội (cách ly qua cột `team_code`) | **Không kết nối** — tự đồng bộ Endpoint/Upstream của **chính đội mình** qua HTTP định kỳ (`RemoteConfigSyncService`) |
| Redis | Tuỳ chọn (chỉ phục vụ cache tạm khi dùng "Thử ngay"/"Thử nhanh" trên UI) | Bắt buộc — cache-aside + bộ đếm rate-limit, riêng của từng đội |

Chỉ **1 image Docker duy nhất** (đúng ADR-02: 1 nguồn code, build 1 lần) —
vai trò được chọn lúc khởi động qua biến môi trường `SPRING_PROFILES_ACTIVE`,
không phải 2 image khác nhau.

**Thứ tự bắt buộc**: Control Plane phải lên **trước**, và phải có ít nhất 1
đội được tạo qua màn hình **"Quản lý đội"** (cần `team_code` + `api_key`)
trước khi bất kỳ đội nào triển khai được Data Plane của mình.

## 1. Yêu cầu hạ tầng

| Hạ tầng | Control Plane | Data Plane |
|---|---|---|
| Cụm Kubernetes + `kubectl` + NGINX Ingress Controller | Khuyến nghị production | Khuyến nghị production |
| Docker + Docker Compose | Thay thế cho k8s (dev/demo 1 host) | Thay thế cho k8s (dev/demo 1 host) |
| Oracle 19c+ | **Bắt buộc**, 1 bản trung tâm dùng chung mọi đội | Không cần |
| Redis | Tuỳ chọn | **Bắt buộc**, riêng của từng đội |
| Elasticsearch | Tuỳ chọn (đọc log do chính CP ghi qua "Thử ngay") | Tuỳ chọn (ghi log traffic thật, fail-open) |
| Elastic APM Server | Tuỳ chọn | Tuỳ chọn |

## 2. Chuẩn bị Oracle trung tâm (đội nền tảng làm 1 lần) — DBA tự chạy DDL

**Ứng dụng không có khả năng tự tạo/sửa schema** (`hibernate.ddl-auto=validate`
tuyệt đối, không Flyway/tự sinh bảng — xem `docs/SAD-Gateway-Manager.md`
ADR-03). Quy trình bắt buộc trước khi chạy Control Plane lần đầu:

1. Gửi **2 file** `backend/src/main/resources/db/team-schema/V1__baseline.sql`
   và `V2__team_code.sql` cho DBA/người quản trị Oracle trung tâm.
2. DBA chạy lần lượt cả 2 file (nguyên vẹn, theo đúng quy trình
   change-management nội bộ) — `V1` tạo 8 bảng, `V2` thêm cột `team_code` +
   bảng `gwm_team` + đổi 2 ràng buộc UNIQUE (xem chi tiết trong chính file
   `V2__team_code.sql` — **cần điền `GATEWAY_ADMIN_API_KEY` thật vào câu
   `INSERT` tạo đội "default"** trước khi chạy nếu instance này đã có dữ liệu
   cũ từ trước 2026-09).
3. DBA cấp cho user **RUNTIME** của Control Plane quyền DML (SELECT/INSERT/
   UPDATE/DELETE) trên toàn bộ bảng — đây là user điền vào `DB_USER`/
   `DB_PASSWORD`.
4. Control Plane khi khởi động chỉ **đối chiếu** schema — thiếu bảng/cột/sai
   kiểu sẽ **không khởi động được** kèm lỗi rõ ràng.

**Data Plane của từng đội KHÔNG cần bước này** — không kết nối Oracle.

## 3. Triển khai Control Plane (đội nền tảng, 1 lần)

### 3a. Kubernetes

```bash
cd krakend-gateway-manager/k8s/control-plane
```
1. Sửa giá trị trong `00-configmap.yaml` (Oracle trung tâm, ES/APM nếu có).
2. Tạo Secret thật (KHÔNG dùng `01-secret.yaml.example` trực tiếp):
   ```bash
   kubectl create secret generic gwm-secret -n <namespace-control-plane> \
     --from-literal=DB_USER='GATEWAY_MANAGER' \
     --from-literal=DB_PASSWORD='<mat-khau-that>' \
     --from-literal=GATEWAY_ADMIN_API_KEY='<tu-sinh-1-key-manh>'
   ```
3. Sửa `image:` trong `40-backend.yaml`/`50-frontend.yaml`.
4. Sửa 2 `host:` trong `60-ingress.yaml`.
5. Apply: `kubectl apply -f . -n <namespace-control-plane>`.

### 3b. Docker Compose (dev/demo)

```bash
cd krakend-gateway-manager
cp .env.control-plane.example .env
# Điền DB_HOST/PORT/NAME/USER/PASSWORD + GATEWAY_ADMIN_API_KEY thật
docker compose -f docker-compose.control-plane.yml up -d --build
```
UI: `http://localhost:4200`.

### 3c. Tạo các đội (bắt buộc trước khi đội nào triển khai Data Plane)

Đăng nhập UI bằng **platform-admin key** (`GATEWAY_ADMIN_API_KEY` vừa tạo) →
vào màn hình **"Quản lý đội"** → tạo 1 dòng cho mỗi đội BCCS sẽ dùng Gateway
Manager. `api_key` sinh ra **hiển thị đúng 1 lần** — lưu lại ngay và gửi cho
đội tương ứng cùng `team_code`.

## 4. Triển khai Data Plane (từng đội BCCS tự làm)

**Điều kiện tiên quyết**: đã nhận `team_code` + `api_key` từ đội nền tảng
(mục 3c).

### 4a. Kubernetes

```bash
cd krakend-gateway-manager/k8s/data-plane
```
1. Sửa `00-configmap.yaml` (đặc biệt `TEAM_CODE`, `CONTROL_PLANE_BASE_URL`).
2. Tạo Secret thật:
   ```bash
   kubectl create secret generic gwm-secret -n <namespace-cua-doi> \
     --from-literal=CONTROL_PLANE_SYNC_API_KEY='<api_key-doi-nen-tang-da-cap>'
   ```
3. Sửa `image:` trong `40-backend.yaml`, `host:` trong `60-ingress.yaml`.
4. Apply: `kubectl apply -f . -n <namespace-cua-doi>`.

### 4b. Docker Compose (dev/demo)

```bash
cd krakend-gateway-manager
cp .env.data-plane.example .env
# Điền TEAM_CODE, CONTROL_PLANE_BASE_URL, CONTROL_PLANE_SYNC_API_KEY
docker compose -f docker-compose.data-plane.yml up -d --build
```

Kiểm tra đồng bộ thành công:
```bash
docker compose -f docker-compose.data-plane.yml logs backend | grep "Da dong bo"
curl http://localhost:8081/actuator/health/readiness   # phải {"status":"UP"} sau khi dong bo lan dau
# (host port 8081 mac dinh cua docker-compose.data-plane.yml khi chay TREN CUNG
# may voi Control Plane dev - doi HOST_PORT trong .env neu muon dung 8080)
```

## 5. Khai báo nghiệp vụ đầu tiên (trên UI Control Plane)

1. Vào **Upstream Services** → đăng ký các backend thật đội cần gọi (host,
   timeout, circuit breaker...) — mỗi đội chỉ thấy Upstream của chính mình.
2. Vào **Endpoints** (hoặc trang **Canvas** kéo-thả) → khai báo endpoint
   composite đầu tiên.
3. Dùng nút **"Thử nhanh"** để xem trước request/response từng step ngay khi
   đang cấu hình, chưa cần lưu.
4. Trong vòng tối đa `CONTROL_PLANE_SYNC_INTERVAL_SECONDS` (mặc định 15s),
   endpoint mới sẽ có hiệu lực trên Data Plane của chính đội đó — **không cần
   khởi động lại** Data Plane.

## 6. Nâng cấp lên version mới

**Control Plane** (Kubernetes): đổi tag `image:` trong `k8s/control-plane/
40-backend.yaml`/`50-frontend.yaml` rồi `kubectl apply` lại.

**Data Plane** (mỗi đội, Kubernetes): đổi tag `image:` trong
`k8s/data-plane/40-backend.yaml` rồi `kubectl apply` lại — **không phụ thuộc
lịch nâng cấp của các đội khác**.

Nếu bản mới thay đổi cấu trúc dữ liệu, đội phát triển nền tảng ban hành kèm 1
file DDL tăng dần mới (`V3__...sql`...) — DBA trung tâm chạy **trước** khi
nâng cấp Control Plane (lặp lại quy trình mục 2). Data Plane không bị ảnh
hưởng bởi thay đổi schema (không kết nối Oracle).

## 7. Sự cố thường gặp

| Triệu chứng | Nguyên nhân khả dĩ |
|---|---|
| Control Plane không lên, log `SchemaManagementException` | DBA chưa chạy `V1__baseline.sql`/`V2__team_code.sql`, hoặc user RUNTIME trỏ nhầm schema — quay lại mục 2 |
| `401 Unauthorized` khi gọi `/api/teams/**` | Phải dùng **platform-admin key** (`GATEWAY_ADMIN_API_KEY`), không phải api_key của 1 đội |
| `401 Unauthorized` khi gọi `/api/endpoints`, `/api/upstreams`... | Phải dùng api_key **của đúng đội** (bảng `gwm_team`) — platform-admin key KHÔNG dùng được cho các API này |
| Data Plane khởi động nhưng `/actuator/health/readiness` mãi `DOWN` | Chưa đồng bộ thành công lần nào — kiểm tra log `RemoteConfigSyncService`, xác nhận `CONTROL_PLANE_BASE_URL` mạng tới được và `CONTROL_PLANE_SYNC_API_KEY` đúng |
| Data Plane đồng bộ báo lỗi 401 | `CONTROL_PLANE_SYNC_API_KEY` sai hoặc đội đã bị xoá khỏi `gwm_team` (xem "Quản lý đội") |
| Endpoint mới tạo trên Control Plane chưa thấy hiệu lực ở Data Plane | Đợi tối đa 1 chu kỳ `CONTROL_PLANE_SYNC_INTERVAL_SECONDS` (mặc định 15s) — nếu lâu hơn, kiểm tra log đồng bộ có lỗi liên tục không |
| Trang "Tra cứu Log" trống/lỗi trên UI Control Plane | Elasticsearch của Control Plane không cấu hình/không kết nối được, HOẶC Control Plane và Data Plane trỏ 2 cụm ES khác nhau (mặc định — chỉ đọc được log do chính CP ghi qua "Thử ngay", không tự động thấy log traffic thật của các đội trừ khi cả 2 bên chủ động trỏ chung 1 cụm ES) |
| Endpoint gọi ra Upstream bị `BulkheadFullException`/timeout | Kiểm tra `maxConcurrentCalls`/`connectTimeoutMs` của Upstream Service tương ứng |
| (k8s) Pod `gwm-backend` (Data Plane) mãi `0/1 Ready` | Xem `kubectl logs` — thường là chưa đồng bộ được (readiness dùng `/actuator/health/readiness`, gồm cả điều kiện đã sync ít nhất 1 lần) |
| (k8s) UI trả `502/504` qua Ingress dù Pod đều Running | Kiểm tra `frontend` Pod log có báo lỗi resolve `gwm-backend` không (xem `frontend/nginx.conf.template`) — `gwm-backend` phải cùng namespace với `gwm-frontend` |
