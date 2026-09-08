# Hướng dẫn 1 đội BCCS tự triển khai Gateway Manager riêng

Tài liệu này dành cho **1 đội BCCS bất kỳ** muốn tự chạy **1 instance Gateway
Manager của riêng mình** (backend composite engine + frontend), trên **hạ tầng
riêng của đội đó** (Oracle 19c riêng, Redis riêng, Elasticsearch riêng - không
dùng chung với đội khác). Khác với [`LOCAL_SETUP.md`](LOCAL_SETUP.md) (dành cho
người PHÁT TRIỂN Gateway Manager, dùng chung hạ tầng `db-local`).

## 1. Yêu cầu

**Topology triển khai chính thức: Kubernetes** (YAML thuần, `kubectl apply`,
không Helm - xem mục 4a). Docker Compose (mục 4b) vẫn được giữ trong repo và
dùng tốt cho máy dev cá nhân/demo nhanh 1 host, nhưng KHÔNG phải hướng production
khuyến nghị cho các đội.

| Hạ tầng | Bắt buộc? | Ghi chú |
|---|---|---|
| Cụm Kubernetes + `kubectl` | Bắt buộc (production) | Đã cài sẵn **NGINX Ingress Controller** trong cụm - xem mục 4a |
| Docker + Docker Compose | Thay thế cho k8s (dev/demo 1 host) | Xem mục 4b |
| Oracle 19c (hoặc mới hơn) | Bắt buộc | Schema riêng của đội, KHÔNG dùng chung schema `BCCS_PRODUCT` với đội khác. Chạy NGOÀI cụm k8s/Docker (hạ tầng có sẵn của đội) |
| Redis | Bắt buộc | Trên k8s: 1 Deployment thường trong cụm, KHÔNG cần PersistentVolume (cache-aside + bộ đếm rate-limit đều fail-open) |
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

## 3. Cấu hình các biến môi trường

Cùng 1 bộ biến (`TEAM_CODE`, `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USER`/
`DB_PASSWORD`, `GATEWAY_ADMIN_API_KEY`, `GATEWAY_AUDIT_*`, `APM_*`...) áp dụng
cho cả 2 topology - chỉ khác **nơi khai báo**: file `.env` (Docker Compose) hay
ConfigMap/Secret (Kubernetes). Xem giải thích đầy đủ từng biến trong
`.env.example` (Docker Compose) hoặc comment trong `k8s/00-configmap.yaml`
(Kubernetes) - nội dung giải thích giống nhau, chỉ định dạng file khác.

## 4a. Chạy trên Kubernetes (khuyến nghị cho production)

Toàn bộ manifest nằm trong [`k8s/`](k8s/) (YAML thuần, không Helm) - xem
[`k8s/README.md`](k8s/README.md) để biết chi tiết từng file. Tóm tắt:

```bash
cd krakend-gateway-manager/k8s

# 1. Sua gia tri trong 00-configmap.yaml cho dung ha tang cua doi (DB_HOST,
#    REDIS_HOST da dung san "gwm-redis" khop voi 30-redis.yaml, GATEWAY_AUDIT_*,
#    APM_*...).

# 2. Tao Secret THAT (KHONG dung 01-secret.yaml.example truc tiep - file do
#    chi la mau tham khao, khong duoc apply):
kubectl create secret generic gwm-secret -n <namespace-cua-doi> \
  --from-literal=DB_USER='GATEWAY_MANAGER' \
  --from-literal=DB_PASSWORD='<mat-khau-that>' \
  --from-literal=GATEWAY_ADMIN_API_KEY='<tu-sinh-1-key-manh>'

# 3. Sua "image:" trong 40-backend.yaml va 50-frontend.yaml thanh dung
#    registry/tag noi bo cua doi (build tu backend/Dockerfile va
#    frontend/Dockerfile, gan version theo git tag).

# 4. Sua 2 "host:" trong 60-ingress.yaml thanh dung domain noi bo cua doi.

# 5. Apply (dam bao da cai san NGINX Ingress Controller trong cum):
kubectl apply -f 00-configmap.yaml -n <namespace-cua-doi>
kubectl apply -f 30-redis.yaml     -n <namespace-cua-doi>
kubectl apply -f 40-backend.yaml   -n <namespace-cua-doi>
kubectl apply -f 50-frontend.yaml  -n <namespace-cua-doi>
kubectl apply -f 60-ingress.yaml   -n <namespace-cua-doi>
```

**Điều kiện tiên quyết**: 8 bảng đã được DBA tạo xong theo đúng mục 2 — nếu
chưa, Pod `gwm-backend` sẽ `CrashLoopBackOff` với log `SchemaManagementException`
(thiếu bảng/cột) ngay khi khởi động, KHÔNG tự tạo gì cả.

Kiểm tra nhanh:
```bash
kubectl get pods -n <namespace-cua-doi>          # ca 3 Pod (redis/backend/frontend) phai Running
curl http://<host-uu-tra-ingress-backend>/api/endpoints -H "X-Gateway-Admin-Key: <key-ban-vua-dat>"
```

## 4b. Chạy bằng Docker Compose (dev/demo 1 host, thay thế cho k8s)

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
`GATEWAY_AUDIT_ES_HOST`/`ES_PORT`/`APM_SERVER_HOST`/`APM_SERVER_PORT`.

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

**Kubernetes**: đổi tag `image:` trong `k8s/40-backend.yaml`/`50-frontend.yaml`
sang version mới rồi `kubectl apply -f k8s/40-backend.yaml -f k8s/50-frontend.yaml
-n <namespace-cua-doi>` (k8s tự rolling-update Pod).

**Docker Compose**:
```bash
git pull            # hoặc doi tag image neu dung registry rieng
docker compose up -d --build
```

Cả 2 trường hợp: nếu bản mới có thay đổi cấu trúc dữ liệu, đội phát triển nền
tảng sẽ ban hành kèm 1 file DDL tăng dần mới (vd `V2__...sql`, đặt cùng thư mục
`db/team-schema/`) — **lặp lại đúng quy trình mục 2** (gửi cho DBA, DBA tự chạy
trên schema đã có) **TRƯỚC** khi nâng cấp ứng dụng. KHÔNG tự khởi động lại ứng
dụng trước khi DBA đã áp dụng DDL mới — `ddl-auto=validate` sẽ chặn khởi động
và báo lỗi rõ ràng nếu chạy nhầm thứ tự.

## 7. Sự cố thường gặp

| Triệu chứng | Nguyên nhân khả dĩ |
|---|---|
| Backend không lên, log `SchemaManagementException` (`Schema validation: missing table`/`wrong column type`) | DBA chưa chạy `V1__baseline.sql` (hoặc bản DDL nâng cấp mới nhất), hoặc user RUNTIME trỏ nhầm schema chưa có bảng — quay lại mục 2 |
| DBA báo lỗi thiếu quyền khi chạy `V1__baseline.sql` | User DBA dùng để chạy DDL cần quyền `CREATE TABLE`/`CREATE INDEX` trên schema đích - khác với user RUNTIME (chỉ cần DML) điền trong `.env`/Secret |
| `401 Unauthorized` khi gọi `/api/**` | Thiếu/sai header `X-Gateway-Admin-Key` - phải khớp đúng `GATEWAY_ADMIN_API_KEY` đã đặt trong `.env` (Compose) hoặc Secret `gwm-secret` (k8s) |
| Trang "Tra cứu Log" trống/lỗi | Elasticsearch chưa cấu hình/không kết nối được - không ảnh hưởng chức năng chính, chỉ tính năng xem log bị tắt |
| Endpoint gọi ra Upstream bị `BulkheadFullException`/timeout | Kiểm tra `maxConcurrentCalls`/`connectTimeoutMs` của Upstream Service tương ứng - có thể cần tăng nếu backend thật của đội chậm/tải cao |
| (k8s) Pod `gwm-backend`/`gwm-frontend` mãi `0/1 Ready` | Xem `kubectl describe pod`/`kubectl logs` - thường là readiness probe `/actuator/health` fail do Oracle chưa kết nối được (`DB_HOST` sai) hoặc image chưa build đúng version |
| (k8s) UI trả `502/504` qua Ingress dù Pod đều Running | Kiểm tra Ingress Controller đã trỏ đúng `ingressClassName: nginx`, và `frontend` Pod log có báo lỗi resolve `gwm-backend` không (xem `frontend/nginx.conf.template`) |
| (k8s) Frontend lên nhưng gọi `/api/**` bị lỗi kết nối | Service `gwm-backend` phải cùng namespace với `gwm-frontend` (DNS ngắn `gwm-backend` chỉ resolve trong cùng namespace) - nếu khác namespace phải sửa thành `gwm-backend.<namespace-backend>.svc.cluster.local` trong `nginx.conf.template` |
