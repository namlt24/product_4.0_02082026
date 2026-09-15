# RUNBOOK TRIỂN KHAI CONTROL PLANE — HẠ TẦNG PRODUCT THẬT

> Tài liệu "cầm tay chỉ việc" cho đội nền tảng: triển khai Control Plane
> (multi-tenant, ADR-07) lên 1 cụm Kubernetes **môi trường thật** — người dùng
> thật, request thật. Biên soạn 2026-09-10, dựa trên deploy thực tế đã thành
> công trên cụm testlab-c2 (mọi lỗi/bẫy ghi trong tài liệu đều đã gặp THẬT).
>
> Phân vai: **CHỈ đội nền tảng làm** (1 lần duy nhất). Mỗi đội BCCS sau đó tự
> triển khai Data Plane của mình theo `../data-plane/README.md`.

---

## 0. Điều kiện đầu vào (kiểm tra TRƯỚC khi làm)

Đánh dấu ✅ từng mục, thiếu mục nào xử lý mục đó, KHÔNG bỏ qua:

- [ ] **Cụm k8s** có namespace riêng cho Control Plane (ví dụ `product`), kubeconfig
      với quyền tối thiểu: deploy/svc/configmap/secret/pod trong namespace đó.
- [ ] **Oracle trung tâm** (19c+): DBA đã cấp schema + user runtime. Lưu ý quy tắc
      quyền: user runtime thường **CHỈ có DML (SELECT/INSERT/UPDATE/DELETE),
      KHÔNG có DDL** — schema do DBA tạo (bước 1 dưới).
- [ ] **Registry Docker nội bộ** mà cụm có thể pull được (tên DNS phân giải được
      TỪ TRONG POD — xem bẫy #B1).
- [ ] **Máy build** có: JDK 25 + Maven wrapper, Node 20+, Docker (push được
      registry), kubectl + kubeconfig của cụm đích.
- [ ] **Port truy cập từ bên ngoài**: xác nhận cụm dùng NodePort hay Ingress.
      Nếu NodePort: hỏi admin dải NodePort cho phép + **firewall mở TCP tới IP
      node trên các port đó TỪ MẠNG NGƯỜI DÙNG** (bẫy #B7 — testlab-c2 chặn
      TCP tới node từ ngoài, ping được vẫn vô nghĩa).

Giá trị cần thống nhất trước (điền vào bảng này, dùng xuyên suốt):

| Biến | Ví dụ | Ghi chú |
|---|---|---|
| `NS` | `product` | namespace Control Plane |
| `DB_HOST/PORT/SID` | `10.207.222.170 / 1521 / DB170` | Oracle trung tâm |
| `DB_SCHEMA` | `BCCS_PRODUCT` | schema chứa 9 bảng |
| `DB_USER/DB_PASSWORD` | do DBA cấp | user RUNTIME, chỉ DML |
| `REGISTRY` | `registry-lab.kcntt.net/bccs` | registry cụm PULL được |
| `REGISTRY_PUSH` | `reg-hosted-lab.kcntt.net/bccs` | registry máy build PUSH được |
| NodePort UI / API | `8837 / 8051` | kiểm tra trống: `kubectl get svc -n NS` |

---

## 1. Chuẩn bị schema Oracle (DBA chạy — trước khi apply k8s)

App dùng `ddl-auto: validate` — **KHÔNG tự tạo bảng**, thiếu bảng = crash lúc
khởi động. Gửi cho DBA 2 file, chạy đúng thứ tự:

```
backend/src/main/resources/db/team-schema/V1__baseline.sql   (8 bảng + 1 index)
backend/src/main/resources/db/team-schema/V2__team_code.sql  (bảng GWM_TEAM +
                                                              cột TEAM_CODE +
                                                              đổi UNIQUE)
```

Chi tiết quan trọng khi bàn giao (đã ghi trong header từng file SQL):

1. V1 tạo 8 bảng: `UPSTREAM_SERVICE, ENDPOINT_CONFIG, BACKEND_STEP,
   BACKEND_STEP_ALLOW, BACKEND_STEP_DENY, BACKEND_STEP_MAPPING,
   FIELD_MAPPING, ENDPOINT_CONFIG_VERSION` (+ index `IDX_ECV_ENDPOINT_ID`).
2. V2 thêm bảng `GWM_TEAM`, cột `TEAM_CODE` (backfill `'default'` cho dữ liệu
   cũ), **xóa UNIQUE(NAME)/UNIQUE(PATH) toàn cục** thay bằng
   `UNIQUE(TEAM_CODE, NAME)` / `UNIQUE(TEAM_CODE, PATH)` — đây là chủ ý
   (multi-tenant cho phép 2 đội trùng tên/path).
3. Sau khi DBA chạy xong: DBA cấp cho `DB_USER` quyền **DML** trên 9 bảng +
   bảng GWM_TEAM (không cần DDL).
4. **Oracle 19c bắt buộc**: boolean dùng `NUMBER(1,0)` + CHECK, KHÔNG dùng
   kiểu BOOLEAN native (file V1 đã tuân thủ — không sửa kiểu cột).

### 1.1. Kiểm tra DBA đã chạy xong (5 phút, tự chạy được)

```bash
# Từ bất kỳ pod nào trong cụm (hoặc máy có sqlplus + route tới DB):
# Cách không cần sqlplus local: dùng image oracle-free làm sqlplus client tạm
kubectl run dbcheck --rm -i --restart=Never --image=<REGISTRY>/oracle-free:23.9-slim \
  --command -- bash -c 'cat > /tmp/v.sql << "EOSQL"
SET PAGESIZE 0
SET LINESIZE 200
SELECT "TBL=" || table_name FROM all_tables WHERE owner = '"'"'BCCS_PRODUCT'"'"' AND table_name IN
  ("UPSTREAM_SERVICE","ENDPOINT_CONFIG","BACKEND_STEP","BACKEND_STEP_ALLOW",
   "BACKEND_STEP_DENY","BACKEND_STEP_MAPPING","FIELD_MAPPING",
   "ENDPOINT_CONFIG_VERSION","GWM_TEAM") ORDER BY 1;
SELECT "UK_TEAM=" || constraint_name FROM all_constraints WHERE owner = '"'"'BCCS_PRODUCT'"'"'
  AND constraint_name IN ("UPSTREAM_SERVICE_TEAM_NAME_UK","ENDPOINT_CONFIG_TEAM_PATH_UK") AND status="ENABLED";
EXIT;
EOSQL
sqlplus -s DB_USER/DB_PASSWORD@//DB_HOST:1521/DB_SID @/tmp/v.sql'
```

**Đạt** khi: đủ 9 dòng `TBL=` và 2 dòng `UK_TEAM=`. Thiếu bảng nào → quay lại
bước 1, KHÔNG deploy (backend sẽ crash `ORA-00942` khi validate).

---

## 2. Build & push image (máy build)

Cùng 1 image dùng cho cả Control/Data Plane (`SPRING_PROFILES_ACTIVE` quyết
định vai trò). Tag đặt có hậu tố `-cp.N` cho Control Plane để quản lý version:

```bash
# 2.1 Backend: build jar (BẮT BUỘC -DskipTests: test cũ có thể fail compile)
cd backend
./mvnw.cmd clean package -DskipTests -Dmaven.test.skip=true
cp target/gateway-manager.jar ../k8s-deploy/backend/gateway-manager.jar

cd ../k8s-deploy/backend
docker build -t <REGISTRY_PUSH>/bccs/gateway-manager:2.0.0-cp.1 .
docker push <REGISTRY_PUSH>/bccs/gateway-manager:2.0.0-cp.1

# 2.2 Frontend: build Angular rồi copy dist vào k8s-deploy/frontend/browser/
cd ../../frontend
npm install && npm run build
# → dist/krakend-gateway-manager-ui/browser/*
# (build production cần giữ nguyên 4 <link> Google Fonts trong index.html và
#  angular.json đã set inlineCritical:false + fonts.inline:false — mặc định đã vậy)

cd ../k8s-deploy/frontend
docker build -t <REGISTRY_PUSH>/bccs/gateway-manager-ui:2.0.0-cp.1 .
docker push <REGISTRY_PUSH>/bccs/gateway-manager-ui:2.0.0-cp.1
```

### Kiểm tra registry cụm pull được (BẮT BUỘC, bẫy #B1)

Cụm đích có thể chỉ pull được **tên registry khác** tên máy push (vd: push
`reg-hosted-lab...`, cụm chỉ pull được `registry-lab...` — cùng storage backend).
Kiểm chứng 1 lần bằng cách so digest hai tên cùng tag:

```bash
docker buildx imagetools inspect <REGISTRY_PUSH>/bccs/gateway-manager:2.0.0-cp.1 | grep Digest
docker build -t <REGISTRY>/bccs/gateway-manager:2.0.0-cp.1 . && docker push <REGISTRY>/bccs/gateway-manager:2.0.0-cp.1
# Trong manifest 40-backend.yaml/50-frontend.yaml: image: phải là tên cụm PULL được
```

Nếu cụm pull thất bại → pod `ImagePullBackOff` → đổi `image:` sang tên registry
cụm pull được, KHÔNG cần push lại nếu 2 tên registry chung storage.

---

## 3. Soạn cấu hình (sửa file manifest trước khi apply)

Sửa trong thư mục `k8s/control-plane/`:

### 3.1 `00-configmap.yaml` — cấu hình không bảo mật

| Khóa | Giá trị product | Lưu ý |
|---|---|---|
| `DB_HOST/DB_PORT/DB_NAME` | Oracle trung tâm | vd `10.207.222.170 / 1521 / DB170` |
| `REDIS_HOST/REDIS_PORT` | `gwm-redis / 6379` | giữ nguyên — phải chạy `30-redis.yaml` cùng namespace (bẫy #B4) |
| `GATEWAY_AUDIT_ENABLED` | `"false"` nếu CHƯA có ES; `"true"` khi có | bật sau khi có ES, chỉ cần sửa ConfigMap + restart |
| `GATEWAY_AUDIT_ES_HOST/ES_PORT` | host ES audit | |
| `GATEWAY_AUDIT_RETENTION_DAYS` | `30` (product) | quá hạn tự xóa index mỗi 24h |
| `APM_*` | theo hạ tầng APM | agent tự tắt nếu không kết nối được |

### 3.2 Secret (KHÔNG ghi vào file YAML, KHÔNG commit — dùng lệnh)

```bash
# 3.2.1 Sinh platform-admin key (32 byte ngẫu nhiên, Base64url)
openssl rand -base64 32 | tr '+/' '-_' | tr -d '='
#   → in ra màn hình, LƯU NGAY vào password manager của đội — dùng đăng nhập UI

kubectl create secret generic gwm-secret -n $NS \
  --from-literal=DB_USER='<DB_USER>' \
  --from-literal=DB_PASSWORD='<DB_PASSWORD>' \
  --from-literal=GATEWAY_ADMIN_API_KEY='<key-vừa-sinh>'
```

> Ý nghĩa 2 LOẠI key (xem `ApiKeyAuthFilter.java`):
> - `GATEWAY_ADMIN_API_KEY` = **platform-admin key**: CHỈ dùng cho
>   `/api/teams/**` (màn hình "Quản lý đội").
> - api_key của từng đội = nằm trong bảng `GWM_TEAM`, do hệ thống tự sinh khi
>   tạo đội, dùng cho mọi `/api/**` còn lại + Data Plane đồng bộ config.

### 3.3 `40-backend.yaml` — bắt buộc sửa cho product

```yaml
image: <REGISTRY>/bccs/gateway-manager:2.0.0-cp.1
```

**Thêm `startupProbe`** (bẫy #B5 — đã bị CrashLoopBackOff thực tế vì Hibernate
validate schema qua WAN mất ~6.5 phút > thời gian liveness probe 2ph15s):

```yaml
          startupProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 20
            periodSeconds: 10
            failureThreshold: 60   # cho phép start chậm tới ~10 phút
            timeoutSeconds: 2
```

Khi có `startupProbe`, kubelet KHÔNG chạy liveness/readiness cho tới khi nó
pass — giữ nguyên 2 probe cũ.

**Product thật với người dùng thật, cân nhắc thêm** (không bắt buộc để chạy):

```yaml
spec:
  replicas: 2            # + podAntiAffinity theo hostname nếu node đủ
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 0  # không gián đoạn CRUD khi rolling
```

### 3.4 `50-frontend.yaml`

```yaml
image: <REGISTRY>/bccs/gateway-manager-ui:2.0.0-cp.1
```

### 3.5 Expose — chọn 1 trong 2 theo hạ tầng cụm

- **Có NGINX Ingress Controller**: dùng 2 Ingress cho `gwm-ui.<domain>` và
  `gwm-api.<domain>` trỏ tới Service `gwm-frontend:80` / `gwm-backend:8080`
  (repo giữ mẫu — xem git history trước 2026-09 nếu cần).
- **Chỉ NodePort** (như cụm test đã làm): giữ `60-ingress.yaml` hiện tại —
  2 Service NodePort `gwm-frontend-nodeport` (UI) và `gwm-backend-nodeport`
  (API). Sửa 2 giá trị `nodePort:` cho khớp dải được cấp.

---

## 4. Apply theo thứ tự

```bash
export KUBECONFIG=<kubeconfig-cụm>

# 4.1 Secret TRƯỚC (deployment ref Optional:false — thiếu = pod không start)
#     (đã làm ở bước 3.2)

# 4.2 Toàn bộ manifest còn lại
kubectl apply -f k8s/control-plane/ -n $NS

# 4.3 Theo dõi rollout
kubectl rollout status deploy/gwm-frontend -n $NS --timeout=120s
kubectl rollout status deploy/gwm-backend  -n $NS --timeout=600s   # đậm thời gian: start có thể 5–10 phút
kubectl get pods -n $NS -o wide
```

**Nếu backend bị kill nhiều lần (exit 137, CrashLoopBackOff) ngay từ lần đầu**:
nghiễm nhiên là bẫy #B5 — kiểm tra `kubectl logs deploy/gwm-backend -n $NS
--previous | tail -5`: nếu dòng cuối là `HHH000489 No JTA platform available`
(và trước đó là Database info + JDBC URL ĐÚNG) thì JVM đang chạy Hibernate
validate và bị kubelet kill do probe — thêm `startupProbe` như 3.3 rồi
`kubectl rollout restart deploy/gwm-backend -n $NS`. KHÔNG tăng RAM/CPU
(không phải OOM), KHÔNG sửa DB.

---

## 5. Verify trước khi đưa người dùng vào (checklist)

Chạy từng lệnh, tất cả phải đạt:

```bash
# 5.1 Pods + health
kubectl get pods -n $NS                       # 2/2 Running (backend, frontend)
kubectl exec -n $NS deploy/gwm-backend -- curl -s localhost:8080/actuator/health
#   → {"status":"UP"}   (DOWN = kiểm tra DB/Redis connectivity)

# 5.2 Log khởi động sạch
kubectl logs deploy/gwm-backend -n $NS --tail=50 | grep -E 'Started GatewayManagerApplication|Da nap lai|ERROR'
#   → "Started GatewayManagerApplication in N seconds"
#   → "Da nap lai N Upstream Service vao cache" (N = số upstream trong DB)
#   → không có ERROR

# 5.3 Auth 2 lớp key (bẫy #B6 — sai loại key là 401 GW-UNAUTHORIZED)
PLATFORM_KEY=<GATEWAY_ADMIN_API_KEY>
kubectl exec -n $NS deploy/gwm-backend -- curl -s -o /dev/null -w '%{http_code}\n' \
  -H "X-Gateway-Admin-Key: $PLATFORM_KEY" localhost:8080/api/teams
#   → 200 (platform-admin key mở /api/teams/**)

# 5.4 UI load được từ máy người dùng
curl -o /dev/null -w '%{http_code}\n' http://<node-ip>:<UI-nodePort>/    # 200
#   (NodePort KHÔNG mở trên ClusterIP của Service — bẫy #B8: phải test qua IP node)

# 5.5 Sau khi tạo đội đầu tiên (bước 6), test bằng team key đó
TEAM_KEY=<api_key-của-đội>
curl -s -H "X-Gateway-Admin-Key: $TEAM_KEY" http://<node-ip>:<API-nodePort>/api/upstreams
#   → 200 + JSON rỗng []  (đội mới, chưa có cấu hình)
```

Đồng thời kiểm tra **từ máy của 1 người dùng thật** (không phải máy build):
browser mở UI, đăng nhập bằng platform-admin key, thấy màn hình Quản lý đội.

---

## 6. Tạo đội cho từng đơn vị BCCS (bước chốt — làm TRƯỚC khi bàn giao Data Plane)

1. Mở UI → **Quản lý đội** (đăng nhập platform-admin key) → Tạo đội.
   Hoặc bằng API:
   ```bash
   curl -X POST http://<api-url>/api/teams \
     -H "X-Gateway-Admin-Key: $PLATFORM_KEY" \
     -H "Content-Type: application/json" \
     -d '{"teamCode":"<ma-doi>","teamName":"<ten-don-vi>"}'
   ```
2. **api_key trả về HIỆN ĐÚNG 1 LẦN duy nhất** — không lưu lại được, không xem
   lại được ở đâu khác (TeamCreatedDto). Copy ngay, gửi cho đội đó qua kênh
   bảo mật, kèm `team_code`.
3. Đội dùng 2 giá trị này để điền vào bộ `../data-plane/` của họ.

> Nếu đội lỡ làm mất key: không có API rotate — phải xoá đội (`DELETE
> /api/teams/{teamCode}` — dữ liệu cấu hình cũ vẫn giữ nguyên trong DB) và tạo
> lại đội mới để lấy key. **Nên lưu key đội ngay lúc tạo.**

---

## 7. Vận hành hằng ngày / xử lý sự cố nhanh

| Triệu chứng | Kiểm tra | Xử lý |
|---|---|---|
| Pod backend `CrashLoopBackOff` exit 137 | `logs --previous`, dòng cuối `HHH000489` | startupProbe thiếu/sai — xem 3.3 |
| Pod backend `CrashLoopBackOff` exit 1 | `logs --previous` có `ORA-00942`/schema error | thiếu bảng/cột — chạy lại DDL bước 1 |
| Pod backend `CrashLoopBackOff` exit 1, log `ORA-01017` | sai DB_USER/DB_PASSWORD | sửa Secret `gwm-secret`, `rollout restart` |
| Readiness 503 kéo dài, health DOWN | health trả `redisDown`? | thiếu Redis cùng namespace — apply `30-redis.yaml` (bẫy #B4) |
| `ImagePullBackOff` | lỗi lookup registry | đổi `image:` sang tên registry cụm pull được (bẫy #B1) |
| UI 502 khi backend vừa restart | nginx resolve DNS | frontend phải dùng FQDN `gwm-backend.$NS.svc.cluster.local` + `resolver <kube-dns-ip>` (bẫy #B6) |
| 401 `GW-UNAUTHORIZED` khi gọi /api/endpoints | đang dùng platform-admin key | phải dùng **team key** (bẫy #B6) |
| Đổi ConfigMap không có tác dụng | env không tự reload | `kubectl rollout restart deploy/gwm-backend -n $NS` |

Câu lệnh tra cứu nhanh:

```bash
kubectl get pods -n $NS -o wide                                    # ai đang ở node nào
kubectl logs deploy/gwm-backend -n $NS --tail=100 -f               # tail log
kubectl describe pod -n $NS -l app.kubernetes.io/component=control-plane | grep -A5 Events
kubectl exec -n $NS deploy/gwm-backend -- curl -s localhost:8080/actuator/health
```

---

## 8. SỔ TAY BẪY (tất cả đã gặp THẬT ngày 2026-09-10 trên testlab-c2)

**B1. Registry cụm không phân giải được tên máy push.**
Triệu chứng: `ImagePullBackOff`, event `lookup reg-hosted-lab... i/o timeout`.
Xử lý: xác định tên registry cụm pull được (hỏi admin hoặc xem deployment của
dịch vụ khác trong cụm), retag + push đè, sửa `image:`. Kiểm chứng 2 tên registry
chung storage bằng so sánh `docker buildx imagetools inspect` digest.

**B2. Oracle cài trong cụm — KHÔNG dùng cho product.**
Bản test từng chạy `gvenzl/oracle-free` in-cluster. 4 bẫy nếu vẫn phải dựng
(ENV test): (1) tag `:23-slim` nay = 23.26 crash `ORA-65012`/`ORA-01081` khi
restart — dùng `23.9-slim`; (2) node chỉ cấp `/dev/shm` 64MB → Oracle chết ngay
— mount emptyDir `medium: Memory, sizeLimit: 2Gi` vào `/dev/shm`; (3) KHÔNG đặt
env `ORACLE_DATABASE` (sẽ CREATE PDB đã tồn tại → crash); (4) init SQL chạy
`as sysdba` trong CDB$ROOT — file init phải bắt đầu bằng
`ALTER SESSION SET CONTAINER = <PDB>;` rồi mới `CURRENT_SCHEMA = <USER>;` nếu
không bảng rơi vào SYS. Product thật: **dùng Oracle trung tâm, cấm in-cluster**.

**B3. Cụm không có StorageClass default.**
PVC Pending vĩnh viễn (`no persistent volumes available`). Ở môi trường product
nếu bắt buộc PVC (không khuyến nghị cho Control Plane — stateless) phải nhờ
admin cấu hình StorageClass. Control Plane đúng chuẩn là stateless — toàn bộ
state nằm ở Oracle.

**B4. Spring Boot health gồm Redis → fail-closed.**
Không có Redis trong namespace ⇒ `/actuator/health` DOWN ⇒ readiness fail ⇒
pod không bao giờ Ready. Bắt buộc chạy `30-redis.yaml` cùng namespace với
Control Plane. (Data Plane thì Redis còn là bắt buộc nghiệp vụ.)

**B5. Hibernate validate qua WAN chậm hơn liveness probe (đã CrashLoopBackOff
thực tế).** Triệu chứng + xử lý: xem mục 3.3 và 4.startupProbe. Thời gian
validate thực tế đo được: ~6.5 phút (Oracle 19c, JDBC `getColumns` trên WAN).

**B6. 2 lớp key + nginx FQDN.**
- platform-admin key CHỈ `/api/teams/**`; team key mới mở `/api/endpoints`,
  `/api/upstreams`, `/api/config/export`... — sai loại key nhận
  `401 GW-UNAUTHORIZED "Thieu hoac sai header"` (không phân biệt thêm).
- nginx trong frontend proxy `/api/` phải ghi **FQDN đầy đủ**
  `gwm-backend.<namespace>.svc.cluster.local` — short-name bị CoreDNS refuse
  (`Operation refused` → 502), và cần `resolver <ClusterIP kube-dns>` vì nginx
  không dùng search-domain của `/etc/resolv.conf`.

**B7. NodePort không truy cập được từ ngoài (firewall hạ tầng).**
Ping node được KHÔNG có nghĩa là TCP mở. Verify bằng cách test từ 1 pod khác
trong cụm; và kiểm tra firewall/network policy của hạ tầng cho dải NodePort
trước khi hẹn giờ bàn giao người dùng.

**B8. NodePort không mở trên ClusterIP của Service.**
`nc <clusterIP> 8051` luôn fail dù service hoạt động đúng — NodePort chỉ mở
trên IP node. Test nhầm sẽ chẩn đoán sai "service hỏng".

**B9. SA của cụm thường không có quyền `get nodes`.**
Cần IP node để test NodePort: đọc `/proc/net/arp` TRONG pod (Calico proxy-arp
lộ IP node: 10.29.1.x), map với tên node từ `kubectl get pods -o wide`.

**B10. Quirks kubectl cần nhớ.**
- `kubectl run ... -- sh -c "..."` phải kèm `--command --`, không phải chỉ
  `--` (báo lỗi `can't create : nonexistent directory`).
- Pod backend image có `curl` (không có `wget`); pod redis:7-alpine có sẵn
  `wget`/`nc` (curl phải `apk add`).
- Log lệnh kubectl đôi khi bị nuốt output — thêm marker `echo T1` + `grep`.

---

## 9. Bản đồ cấu hình cuối cùng (để đối soát)

```
Namespace $NS:
  Secret    gwm-secret                DB_USER, DB_PASSWORD, GATEWAY_ADMIN_API_KEY   (kubectl create secret)
  ConfigMap gwm-config               DB_HOST/PORT/NAME, REDIS_*, GATEWAY_AUDIT_*, APM_*
  Deploy    gwm-backend  (control-plane)  → Service gwm-backend  :8080   (ClusterIP, cho nginx FE + Data Plane dùng nếu cần)
  Deploy    gwm-frontend (control-plane-ui)→ Service gwm-frontend :80
  Deploy    gwm-redis    (control-plane-redis) → Service gwm-redis   :6379
  Expose    gwm-frontend-nodeport :80  → nodePort 8837  (hoặc Ingress gwm-ui.<domain>)
            gwm-backend-nodeport  :8080→ nodePort 8051  (hoặc Ingress gwm-api.<domain>)

DB: 9 bảng + GWM_TEAM trong schema $DB_SCHEMA (V1 + V2 do DBA chạy)
Keys: 1 platform-admin (Secret) + N team key (bảng GWM_TEAM, sinh khi tạo đội)
```

Bàn giao cho đội BCCS: gửi cho mỗi đội 2 giá trị `team_code` + `api_key`
(bước 6) và bộ `../data-plane/` — họ tự triển khai Data Plane của mình.
