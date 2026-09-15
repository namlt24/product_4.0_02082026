# HƯỚNG DẪN THỰC HÀNH DEPLOY LẠI GATEWAY MANAGER TỪ ĐẦU (sau khi teardown)

> Tài liệu thực hành cho bạn dep: dựng lại **toàn bộ stack Gateway Manager 2.0**
> sau khi đã tắt sạch (teardown 2026-09-12), đi **từng bước theo đúng trình tự
> runbook product đã chạy thành công 2026-09-10/11**, kèm giải thích **việc
> đó để làm gì** và bẫy thực tế đã gặp ở từng bước.
>
> **Trình tự đã chốt**: dựng đúng theo runbook (mức test) TRƯỚC → verify đủ
> checklist → SAU ĐÓ mới nâng HA (backend CP replicas=2 + antiAffinity, TLS
> cho CP 8051/8837, bật audit) như "bài nâng cao" — xem `PHẦN 3` cuối file.
>
> Trạng thái hiện tại (2026-09-12):
> - **testlab-c2 / ns `product`**: đã xoá sạch gwm-* (configmap, redis, backend,
>   frontend, 2 NodePort svc, oracle, init-sql cm, 2 secrets). Namespace, SA
>   `team-sa` và 5 deployment BCCS service hàng xóm GIỮ NGUYÊN.
> - **myviettel / ns `myviettel`**: đã xoá sạch gwm-* (configmap, redis, backend
>   + tls-proxy, nodeport, tls-nginx-conf, gwm-secret, gwm-tls). App khác
>   (payment-service, gateway 1.1.0 cũ...) GIỮ NGUYÊN.
> - **Backup secrets trước khi xoá**: `E:\kubeconfigs\gwm-secrets-backup-20260912\`
>   (`cp-product-secrets.json` + `dp-myviettel-secrets.json`) — dùng để lấy lại
>   password/key cũ, không phải sinh key mới (xem Bước 5.1).

---

## Bản đồ tổng thể — bạn sắp dựng gì

Cùng 1 image backend, vai trò do biến `SPRING_PROFILES_ACTIVE` quyết định
(không phải 2 image khác nhau):

```
PHẦN 1 — CONTROL PLANE (cụm testlab-c2, ns product)     ← đội nền tảng, 1 lần duy nhất
  UI Angular + backend CRUD (/api/**) + Redis + (Oracle tạm) + 2 NodePort
        |
        |  cấp team_code + api_key cho đội (bảng GWM_TEAM, DB170)
        v
PHẦN 2 — DATA PLANE (cụm myviettel, ns myviettel)       ← mỗi đội tự làm
  backend (thực thi traffic thật) + Redis + nginx TLS sidecar + NodePort 8052/8053
  KHÔNG có DB — tự sync cấu hình từ Control Plane qua HTTP mỗi 15s
```

Trình tự bắt buộc: **CP lên trước** (có team + key), rồi DP mới sync được.

Tài nguyên sẽ dựng theo từng file manifest:

| File | Tài nguyên | Vai trò |
|---|---|---|
| `control-plane/00-configmap.yaml` | ConfigMap `gwm-config` | Biến env: DB host/port, Redis host, audit, APM. **Mặc định vĩnh viễn = DB170** (sửa 2026-09-12) — đổi DB chỉ cần patch + restart, không build lại |
| `control-plane/30-redis.yaml` | Deploy + Svc `gwm-redis` | Cache "Thử ngay" trên UI. Spring health fail-closed: thiếu Redis → pod không bao giờ Ready (bẫy #B4) |
| `control-plane/40-backend.yaml` | Deploy + Svc `gwm-backend` | App Spring Boot profile `control-plane` (CRUD). Có **startupProbe** chống slow-start Hibernate validate qua WAN (bẫy #B5) |
| `control-plane/50-frontend.yaml` | Deploy + Svc `gwm-frontend` | Angular UI (Nginx serve static) |
| `control-plane/60-ingress.yaml` | 2 Svc NodePort | UI **8837** / API **8051** — cửa ra ngoài (dải NodePort cụm 8000–10000) |
| `oracle-db/00-oracle-standalone.yaml` | Deploy + Svc `gwm-oracle` | Oracle Free 23.9 in-cluster — bàn đạp test trước khi cắt DB170 |
| `oracle-db/01-init-schema.sql` | ConfigMap `gwm-oracle-init-sql` | DDL 9 bảng (V1+V2 gộp) — Oracle in-cluster tự chạy lúc init |
| `data-plane/00-configmap.yaml` | ConfigMap `gwm-config` | **Sửa 3 biến trước khi apply (D3.0)**: `CONTROL_PLANE_BASE_URL`→`.14`, `TEAM_CODE`/`APM_ENVIRONMENT`→tên đội bạn. Team identity KHÔNG nằm ở đây — nằm ở team key trong secret (D1) |
| `data-plane/30-redis.yaml` | Deploy + Svc `gwm-redis` | **Bắt buộc nghiệp vụ**: cache-aside per-step + bộ đếm rate-limit (fail-open) |
| `data-plane/40-backend.yaml` | Deploy + Svc `gwm-backend` (replicas=2, antiAffinity, nodeAffinity) + ConfigMap nginx TLS | Profile `data-plane` (thực thi traffic) + nginx sidecar HTTPS 8443→8080 |
| `data-plane/60-ingress.yaml` | Svc NodePort 2 port | HTTP **8052** (migrate) + HTTPS **8053** (product chính thức) |

Kubeconfig dùng xuyên suốt:

```bash
CP_KC="E:\kubeconfigs\product-kubeconfig.yaml"        # context product-dev@testlab-c2, API 10.58.71.203:8443
DP  = mặc định kubectl (context dev-myviettel-context) # cụm myviettel
```

> **QUY ƯỚC SHELL CỦA TÀI LIỆU NÀY**: toàn bộ lệnh viết cho **bash** — chạy
> NGUYÊN DẠNG trong Git Bash (Windows), Linux, macOS. **Khuyến nghị: mở Git
> Bash** (đi kèm Git for Windows — Start menu gõ "Git Bash") và dùng nó cho
> toàn bộ buổi thực hành: copy từng khối lệnh nhiều dòng vào là chạy, không
> phải chuyển đổi gì. Pod Linux trong cụm cũng chạy `sh` bash-style nên 1 cú
> pháp dùng được cả 2 đầu.
>
> Nếu BẮT BUỘC phải dùng PowerShell (không có Git Bash), áp dụng quy tắc
> chuyển đổi cho TẤT CẢ lệnh kubectl trong tài liệu:
> 1. **Gộp về 1 dòng** — bỏ ký tự nối dòng `\` cuối dòng (PowerShell không
>    hiểu; muốn xuống dòng dùng backtick `` ` `` nhưng dễ dính space ẩn).
> 2. **`sh -c '...'` → giữ nháy đơn `'...'`** (PowerShell không biến dịch
>    bên trong, `$?`/`$var` của shell Linux trong pod được giữ nguyên). NHÁY
>    KÉP `"..."` là SAI — PowerShell thay `$?` bằng True/False của chính nó.
> 3. **Tham số `-D...` (maven) bọc nháy kép**: `"-DskipTests"` — không quote
>    thì PowerShell tách token tại dấu chấm → Maven báo
>    `Unknown lifecycle phase ".test.skip=true"`.
> 4. **`./mvnw.cmd` → `.\mvnw.cmd`, `cp` → `copy`, `cat` → `Get-Content`,
>    `grep` → `Select-String`, `curl` → `curl.exe -o NUL`** (curl thường trong
>    PowerShell là alias của Invoke-WebRequest — không hiểu cờ `-sk`).
> 5. **`$VAR` → `$env:VAR`** khi cần biến môi trường Windows (VD
>    `$USERPROFILE` → `$env:USERPROFILE`).

---

# PHẦN 1 — CONTROL PLANE trên testlab-c2

## Bước 0 — Kiểm tra sân trống

```bash
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product get all,cm,secret
```

**Để làm gì**: xác nhận không còn tài nguyên `gwm-*` nào sót (tránh xung đột
apply). Kỳ vọng: chỉ còn 5 deployment BCCS service hàng xóm (organization-resource,
product-catalog...), không có gì tên `gwm-`.

## Bước 1 — Build backend jar (máy build, chưa đụng k8s)

```bash
cd "E:\product_4.0_02082026-krakend-gateway-manager\product_4.0_02082026-krakend-gateway-manager\krakend-gateway-manager\backend"
./mvnw.cmd clean package -DskipTests -Dmaven.test.skip=true
cp target/gateway-manager.jar ../k8s-deploy/backend/gateway-manager.jar
```

(Chạy nguyên trong **Git Bash**. PowerShell thì: `.\mvnw.cmd clean package "-DskipTests" "-Dmaven.test.skip=true"` + `copy` — không quote `-D` là Maven báo `Unknown lifecycle phase ".test.skip=true"`, đã gặp thật 2026-09-12.)

**Để làm gì**:
- `clean package` = xoá output cũ + compile + đóng jar executable. Jar này là
  thứ duy nhất image backend cần (pattern "jar-in-image" — Dockerfile chỉ copy
  jar + JDK runtime, không build bên trong container nên nhanh).
- `-DskipTests` **BẮT BUỘC** (bẫy thực tế): repo còn file test stale fail
  compile — không skip là chết ở giai đoạn build, không liên quan code mới.
- Copy sang `k8s-deploy/backend/` vì đó là thư mục build context của
  `docker build` ở bước 3.

## Bước 2 — Build frontend

```bash
cd ../frontend
npm install
npm run build
xcopy /E /I dist\krakend-gateway-manager-ui\browser "..\k8s-deploy\frontend\browser"
```

(Bước này chạy trên máy build Windows — `xcopy` là lệnh cmd, chạy được cả
trong PowerShell lẫn Git Bash.)

**Để làm gì**: Angular compile ra static JS/HTML vào `dist/.../browser/` —
chính là cái Nginx trong image UI sẽ serve. Copy sang build context trước khi
docker build. (Lưu ý: `frontend/proxy.conf.json` cho `ng serve` dev còn trỏ
cổng 9000 cũ — KHÔNG ảnh hưởng build production.)

## Bước 3 — Build & push image lên registry (bẫy #B1)

```bash
cd ..\k8s-deploy\backend
docker build -t reg-hosted-lab.kcntt.net/bccs/gateway-manager:2.0.0-cp.2 .
docker push reg-hosted-lab.kcntt.net/bccs/gateway-manager:2.0.0-cp.2

cd ..\frontend
docker build -t reg-hosted-lab.kcntt.net/bccs/gateway-manager-ui:2.0.0-cp.2 .
docker push reg-hosted-lab.kcntt.net/bccs/gateway-manager-ui:2.0.0-cp.2
```

**Để làm gì / hiểu cặn kẽ**:
- Push lên `reg-hosted-lab.kcntt.net` (máy build PUSH được) nhưng **cụm
  testlab-c2 chỉ pull được tên `registry-lab.kcntt.net`** — DNS từ node không
  phân giải tên hosted. 2 registry **chung storage** (verify bằng so digest cùng
  tag trên 2 tên — giống nhau) → không cần push 2 lần, chỉ cần ghi đúng tên
  "cụm pull được" vào `image:` trong manifest (manifest gốc đã ghi đúng).
- Tag gợi ý `2.0.0-cp.2` để phân biệt lần build mới (bản cũ `2.0.0-cp.1` vẫn
  nằm trên registry — nếu code không đổi có thể tái dùng, khỏi build lại).
- Docker Desktop đôi khi mất pipe — lỗi kết nối thì thử lại là được.

## Bước 4 — Verify kết nối DB170 TRƯỚC (bắt buộc khi mặc định = DB170)

ConfigMap giờ mặc định DB170 từ đầu → bước này trở thành điều kiện đầu vào,
KHÔNG còn bàn đạp Oracle in-cluster để tách lỗi nữa:

> **Lỗi hay gặp `Error from server (AlreadyExists): pods "t" already exists`**:
> pod test tạm tên `t` còn sót từ lần chạy trước (Ctrl+C giữa chừng hoặc terminal
> đóng — `--rm` không kịp dọn dẹp). Xoá pod trần đó rồi chạy lại:
> ```powershell
> kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product delete pod t --force --grace-period=0
> ```
> (`delete pod t` xoá ĐÚNG 1 pod tên `t` — không ảnh hưởng pod khác; pod của
> Deployment thì k8s tự tái lập, pod trần kiểu này xoá là mất hẳn — đúng ý)

```bash
# 4.0 TCP từ trong cụm tới DB170 (máy trạm có thể không route, pod thì được)
# Git Bash: chạy nguyên khối. PowerShell: gộp 1 dòng, sh -c giữ NHÁY ĐƠN
# (nháy kép → PowerShell thay $? bằng True/False — SAI)
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" run t --rm -i --restart=Never \
  --image=registry-lab.kcntt.net/bccs/redis:7-alpine --command -- \
  sh -c 'nc -z -w5 10.207.222.170 1521; echo rc=$?'
#  rc=0 = THÔNG (cần VPN của user đang bật). rc=1 = DỪNG, nhờ mở route trước.

# 4.1 Schema DB170 đủ 9 bảng (thiếu = backend crash ORA-00942 lúc validate)
# Lệnh dài dễ gãy khi dán vào terminal (bị wrap thành 2 lệnh → "required
# flag(s) \"image\" not set"). CÁCH CHẮC CHẮN: script đã viết sẵn — chỉ chạy
# (Git Bash):
bash "E:/kubeconfigs/dbcheck.sh"
#  Script bên trong = kubectl run dbcheck ... sqlplus query
#  table_name IN (UPSTREAM_SERVICE, ENDPOINT_CONFIG, BACKEND_STEP,
#  BACKEND_STEP_ALLOW, BACKEND_STEP_DENY, BACKEND_STEP_MAPPING,
#  FIELD_MAPPING, ENDPOINT_CONFIG_VERSION, GWM_TEAM) của owner BCCS_PRODUCT.
#  Lần đầu pull image oracle-free ~1-2GB — đợi vài phút là bình thường.
#  KỲ VỌNG: đúng 9 dòng tên bảng + "9 rows selected.".
#  BẪY: 8/9 bảng KHÔNG có prefix GWM_ — nếu ai đó check bằng
#  LIKE 'GWM_%' chỉ thấy GWM_TEAM là ĐÚNG kết quả truy vấn, đừng kết luận thiếu bảng.
```

**Để làm gì**: DB170 chỉ reachable qua VPN; schema là của DBA quản. Kiểm 2
điều này TRƯỚC khi deploy giúp tránh chết ở bước cuối mới phát hiện.

> Bàn đạp Oracle in-cluster (`k8s/oracle-db/`) giữ nguyên trên repo như phương
> án dự phòng khi DB170 không reachable — lúc đó patch
> `DB_HOST=gwm-oracle / DB_PORT=1521 / DB_NAME=FREEPDB1` + secret GWM_RUNTIME,
> rồi apply `oracle-db/`. Trình tự dựng bàn đạp đó xem
> `control-plane/PRODUCTION_RUNBOOK.md` mục B2.

```bash
cd "E:\product_4.0_02082026-krakend-gateway-manager\product_4.0_02082026-krakend-gateway-manager\krakend-gateway-manager\k8s"
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product apply -f oracle-db/
```

(Bước này chỉ dùng khi quay lại bàn đạp Oracle in-cluster — mặc định DB170 thì
bỏ qua.)

**Để làm gì / các bẫy đã harden sẵn trong manifest**:
- Oracle Free 23.9-slim chạy trong pod, tự tạo PDB `FREEPDB1` + user
  `GWM_RUNTIME`, rồi chạy `01-init-schema.sql` (ConfigMap mount vào
  `/container-entrypoint-initdb.d/`) tạo đủ 9 bảng.
- Bẫy đã xử lý trong file (đừng "tối ưu" lại):
  - Tag phải `23.9-slim` — tag `:23-slim` mới = 23.26 crash `ORA-65012` khi restart.
  - `ORACLE_PASSWORD` (KHÔNG phải `ORACLE_PWD` như image chính chủ).
  - KHÔNG đặt `ORACLE_DATABASE` (image sẽ CREATE PDB đã tồn tại → crash).
  - `/dev/shm` mount emptyDir Memory 2Gi — node chỉ cấp 64MB mặc định, Oracle
    chết ngay lúc SGA init.
  - Healthcheck `/opt/oracle/healthcheck.sh` (không phải `/healthcheck`).
  - File init SQL bắt đầu bằng `ALTER SESSION SET CONTAINER=FREEPDB1;` —
    không thì bảng rơi vào SYS (`ORA-01435`).
- Init mất **2–4 phút**, pod có thể restart 1-2 lần đầu — bình thường.

Theo dõi:

```bash
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product get pods -w
# chờ gwm-oracle-... Running + Ready 1/1 rồi Ctrl+C
```

Verify 9 bảng đủ (đạt = đủ 9 dòng). PowerShell — 1 dòng, nháy đơn ngoài:

```bash
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product exec deploy/gwm-oracle -- bash -c 'sqlplus -s GWM_RUNTIME/<APP_USER_PASSWORD>@localhost/FREEPDB1 <<< "SELECT table_name FROM user_tables; EXIT;"'
```

## Bước 5 — Tạo secrets bằng kubectl (KHÔNG qua file yaml)

### 5.1 Lấy lại giá trị cũ từ backup (không phải sinh key mới)

```bash
# Xem 2 secret CP đã backup (base64 trong key "data"):
cat "E:\kubeconfigs\gwm-secrets-backup-20260912\cp-product-secrets.json"
#  (PowerShell: Get-Content "E:\kubeconfigs\gwm-secrets-backup-20260912\cp-product-secrets.json")

# Decode key thật từ base64 (Git Bash):
printf '%s' '<chuỗi-base64-của-GATEWAY_ADMIN_API_KEY>' | base64 -d
#  → in ra key gốc KfpGywi1r-... — CHỈ KEY NÀY MỚI là giá trị dán vào bước 5.2
```

> **BẪY #B11 — dán nhầm base64 thay vì key gốc (gặp thật 2026-09-13)**: trong
> file backup, key `"data"` của kubectl **luôn là base64** — đó là lớp đóng gói
> format, KHÔNG phải giá trị thật. Dán nguyên chuỗi base64 (nó còn nằm trong
> dấu nháy `"..."` trong JSON nên trông giống như giá trị thật) vào
> `--from-literal` thì secret vẫn tạo OK, pod vẫn chạy — và bạn chỉ phát hiện
> khi gọi API nhận **401 GW-UNAUTHORIZED**. Cách soi:
> `kubectl get secret gwm-secret -o jsonpath='{.data.GATEWAY_ADMIN_API_KEY}' | base64 -d | od -c`
> — nếu thấy byte đầu là `"` hoặc chuỗi dài bất thường (89 ký tự thay vì 64,
> vẫn còn đuôi `==`) là dán nhầm. Sửa: delete + create lại secret (SA
> dev-myviettel/team-sa không có quyền `get` secrets nên
> `--dry-run=client -o yaml | kubectl apply` sẽ Forbidden) + rollout restart.

Giá trị cần dùng lại từ backup: `GATEWAY_ADMIN_API_KEY` (platform-admin key
đăng nhập UI). Với mặc định DB170 thì `gwm-oracle-secret` KHÔNG cần nữa (chỉ
dùng khi quay lại bàn đạp Oracle in-cluster).

### 5.2 Tạo secret (chỉ 1 — backend)

```bash
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product create secret generic gwm-secret \
  --from-literal=DB_USER=BCCS_PRODUCT \
  --from-literal=DB_PASSWORD='BCCS_PRODUCT123' \
  --from-literal=GATEWAY_ADMIN_API_KEY='<key-ĐÃ-DECODE-dạng-KfpGywi1r-...>'
```

**Để làm gì / vì sao tách 2 secret**:
- `gwm-oracle-secret` = cho **container Oracle** đọc (tạo user APP lúc init).
- `gwm-secret` = cho **container backend** đọc (kết nối DB + platform-admin key).
- Vai trò platform-admin key (bẫy #B6 — 2 LỚP key trong `ApiKeyAuthFilter.java`):
  - platform-admin key → CHỈ mở `/api/teams/**` (Quản lý đội).
  - team key (nằm trong bảng GWM_TEAM, hệ thống sinh khi tạo đội) → mở mọi
    `/api/**` còn lại + là thứ Data Plane dùng để sync.
- Secret tạo bằng `kubectl` trực tiếp — **không tồn tại dạng file trong repo,
  không commit git**. Đó là lý do phải backup trước khi teardown.

**Nền tảng kiến thức — Secret trong k8s là gì**:
- Bản chất: 1 "hộp khóa" cặp key=value, value base64, sống trong cluster.
- Dòng chảy: Secret → `envFrom.secretRef` trong 40-backend.yaml (dòng 39-40) →
  kubelet bơm toàn bộ key thành **biến môi trường** lúc container start →
  Spring Boot đọc relaxed binding (`DB_PASSWORD` → `spring.datasource.password`).
- Vì sao tạo TRƯỚC Bước 6: Deployment khai báo "bơm secret gwm-secret" — thiếu
  nó pod kẹt `CreateContainerConfigError` (không crash, không tự hồi).
- Vì sao dùng lại giá trị CŨ: DB_USER/PASSWORD do DBA cấp (không thể sinh);
  GATEWAY_ADMIN_API_KEY nếu sinh mới = "đổi mật khẩu chung" — ai dùng UI key cũ
  phải cấp lại. Lấy từ backup = như chưa từng teardown.
- 3 giá trị hiện tại: DB_USER=BCCS_PRODUCT, DB_PASSWORD=BCCS_PRODUCT123 (DBA
  cấp), GATEWAY_ADMIN_API_KEY=KfpGywi1r-... (backup 20260912).
- LƯU Ý quyền: SA team-sa (testlab-c2) KHÔNG có `get` secrets — đọc secret bằng
  jsonpath như 5.3a vẫn được (get secret gwm-secret -o jsonpath) vì đó là API
  đọc 1 secret cụ thể... nếu Forbidden thì nhờ admin. Còn ghi (`create`) thì
  được — vì vậy khi THAY secret phải delete + create.

### 5.2b Production lần đầu — SINH key mới, KHÔNG "lấy lại từ backup"

Phân biệt 2 kịch bản (chọn SAI kịch bản thì hoặc là mất công rảnh, hoặc là
làm phiền người dùng):

| | Production lần đầu (trang trắng) | Teardown rồi dựng lại (thực hành này) |
|---|---|---|
| Key admin cũ | Chưa từng tồn tại | Đã có, đang bị người dùng dùng |
| Cách tạo | **Sinh key mới** | **Lấy lại từ backup** |
| Backup | Tạo NGAY sau khi sinh key (bước 3 dưới) | Đã có sẵn — chỉ đọc lại |

Trình tự chuẩn production lần đầu:

```bash
# 1. Sinh key ngẫu nhiên (32 byte = 256 bit, đủ mạnh)
openssl rand -base64 32

# 2. Tạo secret với key MỚI vừa sinh
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product create secret generic gwm-secret \
  --from-literal=DB_USER=BCCS_PRODUCT \
  --from-literal=DB_PASSWORD='BCCS_PRODUCT123' \
  --from-literal=GATEWAY_ADMIN_API_KEY='<key-vừa-sinh>'

# 3. BACKUP NGAY — đây là "sổ khóa" duy nhất của key này (không nằm trong DB nào)
mkdir -p "E:\kubeconfigs\gwm-secrets-backup-$(date +%Y%m%d)"
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product get secret gwm-secret -o json \
  > "E:\kubeconfigs\gwm-secrets-backup-$(date +%Y%m%d)\cp-product-secrets.json"
```

Quy tắc tổng quát:
- **Có người dùng đang phụ thuộc key** → giữ key cũ (restore từ backup).
- **Chưa có ai dùng / lần đầu** → sinh key mới + backup ngay.
- **Teardown mà KHÔNG có backup** → buộc sinh key mới (kịch bản 5.3b) — vẫn
  chạy được, chỉ tốn chi phí phát hành lại key cho người dùng.
- Backup KHÔNG phải việc "dành cho teardown" — nó là **bước thành lập của
  key**: vì admin key chỉ sống trong secret (không như team key nằm trong DB),
  không backup ngay thì lần teardown sau rơi thẳng vào 5.3b.

### 5.3 Vòng đời platform-admin key: quên/mất thì xử lý thế nào? (kiến thức vận hành)

Key nằm CHỈ trong Secret `gwm-secret` — KHÔNG nằm trong DB (khác team key nằm
plaintext cột `api_key` bảng GWM_TEAM trên DB170). 3 kịch bản:

**a) Quên nhưng secret còn trong cluster** — đọc lại 1 lệnh (không mất gì):

```bash
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product get secret gwm-secret \
  -o jsonpath='{.data.GATEWAY_ADMIN_API_KEY}' | base64 -d
```

**b) Mất cả secret (teardown + không backup)** — sinh key mới và thay thế:

```bash
# 1. Sinh key mới ngẫu nhiên
openssl rand -base64 32

# 2. Thay key mới vào secret (dry-run=client để không cần xóa-trước-tạo-sau)
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product create secret generic gwm-secret \
  --from-literal=DB_USER=BCCS_PRODUCT \
  --from-literal=DB_PASSWORD='BCCS_PRODUCT123' \
  --from-literal=GATEWAY_ADMIN_API_KEY='<KEY-MỚI>' \
  --dry-run=client -o yaml | kubectl apply -f -

# 3. Env trong pod KHÔNG tự reload — bắt buộc restart
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product rollout restart deploy/gwm-backend
```

Chi phí = "đổi mật khẩu chung": ai đang dùng UI với key cũ phải được cấp key
mới. KHÔNG mất data gì — config Endpoint/Upstream + team key đều nằm DB170,
không liên quan.

**c) Đối xứng với team key** (đội quên key của đội): admin đọc lại được từ DB
vì key đội lưu plaintext — `SELECT team_code, api_key FROM gwm_team;` trên
DB170. Phân biệt bản chất: admin key = cấu hình hạ tầng (secret, mất khi
teardown); team key = data nghiệp vụ (DB ngoài cluster, sống sót qua teardown).

**Bài học backup**: backup secret TRƯỚC teardown là bắt buộc —
`kubectl get secret -n product -o json > <file ngoài repo>`. Backup hiện có:
`E:\kubeconfigs\gwm-secrets-backup-20260912\`.

## Bước 6 — Apply Control Plane

```bash
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product apply -f control-plane/
```

**Để làm gì**: dựng configmap → redis → backend → frontend → 2 nodeport svc
(thứ tự trong tên file có chủ đích: backend ref `gwm-config` + `gwm-secret`
nên 2 cái đó phải có trước — secret đã tạo ở bước 5).

Theo dõi rollout (backend có thể chậm, startupProbe cho phép tới ~10 phút):

```bash
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product rollout status deploy/gwm-backend --timeout=600s
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product get pods -o wide
```

## Bước 7 — Đổi ConfigMap/Secret khi pod đang chạy → restart

```bash
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product rollout restart deploy/gwm-backend
```

**Để làm gì**: env trong pod **không tự reload** khi ConfigMap/Secret thay đổi.
Restart tạo pod mới đọc env mới. Đây là thao tác bắt buộc mỗi lần patch config
(bước 9 sẽ dùng tới).

> **Trong lượt dựng từ đầu thì Bước 7 KHÔNG cần chạy** — bạn tạo configmap +
> secret TRƯỚC khi pod sinh ra → pod đọc env đúng ngay từ khi mở mắt. Bước này
> có nghĩa từ lần nâng cấp config trở đi.

## Bước 8 — Verify Control Plane sống

```bash
# 8.1 Health tổng (đạt: {"status":"UP"} — DOWN = kiểm tra DB/Redis)
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product exec deploy/gwm-backend -- curl -s localhost:8080/actuator/health

# 8.2 Log khởi động sạch (Git Bash: grep nguyên dạng)
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product logs deploy/gwm-backend --tail=50 | grep -E 'Started GatewayManagerApplication|Da nap lai|ERROR'
#  (PowerShell: thay grep bằng Select-String -Pattern '...')
#  → "Started GatewayManagerApplication in N seconds" + "Da nap lai N Upstream Service vao cache", không ERROR

# 8.3 Auth 2 lớp — platform-admin key mở /api/teams
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product exec deploy/gwm-backend -- \
  curl -s -o /dev/null -w '%{http_code}\n' -H "X-Gateway-Admin-Key: <PLATFORM_KEY>" localhost:8080/api/teams
#  → 200
```

Test NodePort TỪ TRONG cluster (bẫy #B7 — máy trạm bị firewall chặn TCP tới
node testlab-c2, ping được vẫn vô nghĩa):

> **QUY ĐỊNH HẠ TẦNG (2026-09-13)**: đội chỉ được dùng node
> `worker-10029001014` ↔ `10.29.1.14` và `worker-10029001015` ↔ `10.29.1.15`
> (pattern tên node `worker-10029001xyz` ↔ IP `10.29.1.xyz`). **IP test chuẩn =
> 10.29.1.14** — đã verify `wget ... http://10.29.1.14:8051/actuator/health` →
> UP, rc=0. Đổi node cho CP xem mục H4 (nodeAffinity pin dải được cấp).
> Xác nhận node chứa pod hiện tại: `kubectl exec deploy/gwm-backend -- cat /proc/net/arp`
> (Calico proxy-arp lộ IP node chứa pod — bẫy #B9: SA không được `get nodes`).

```bash
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" run t --rm -i --restart=Never \
  --image=registry-lab.kcntt.net/bccs/redis:7-alpine --command -- \
  sh -c 'wget -q -T5 -O- http://10.29.1.14:8051/actuator/health; echo; echo rc=$?'
#  → {"status":"UP"} + rc=0 (Git Bash: nguyên khối. PowerShell: gộp 1 dòng, giữ nháy đơn)
#  Gặp "sh: curl: not found" = apk add curl thất bại thầm lặng (bẫy #B10) —
#  dùng bản wget ở trên (wget CÓ SẴN trong redis:7-alpine, không cần cài gì).
```

**Vì sao temp pod redis:7-alpine**: có sẵn `wget`/`nc` — KHÔNG cần `apk add
curl` (thất bại thầm lặn khi repo Alpine không tới được — gặp thật 2026-09-13,
bẫy #B10); image này đã có sẵn trong registry nội bộ — không pull Docker Hub
(bẫy #C3). Quirk kubectl: `sh -c` phải kèm `--command --`.

## Bổ sung 8A — Chuyển Redis ngoài (khi cần)

Redis hiện tại = pod `gwm-redis` trong namespace (từ 30-redis.yaml). Cắt sang
Redis ngoài (server riêng / cluster chung) — 2 trường hợp:

**Trường hợp A — Redis ngoài KHÔNG mật khẩu**: chỉ 2 việc (đúng như đoán):

```bash
# 1. Patch configmap (đổi REDIS_HOST/REDIS_PORT)
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product patch configmap gwm-config \
  -p '{"data":{"REDIS_HOST":"<IP-Redis-riêng>","REDIS_PORT":"6379"}}'

# 2. Restart (env không tự reload)
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product rollout restart deploy/gwm-backend

# Sau khi ổn định: scale redis in-cluster về 0 (giữ manifest để có thể trả về)
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product scale deploy/gwm-redis --replicas=0
```

**Trường hợp B — Redis ngoài CÓ mật khẩu**: thêm 1 bước — bỏ mật khẩu vào
**Secret** (không đụng code Java, không rebuild image):

```bash
# Thêm biến env mật khẩu vào secret hiện có (relaxed binding:
# SPRING_DATA_REDIS_PASSWORD → spring.data.redis.password — Spring Boot tự nhận,
# StringRedisTemplate dùng connection factory auto-configured nên password tự áp dụng)
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product delete secret gwm-secret
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product create secret generic gwm-secret \
  --from-literal=DB_USER=BCCS_PRODUCT \
  --from-literal=DB_PASSWORD='BCCS_PRODUCT123' \
  --from-literal=GATEWAY_ADMIN_API_KEY='<key>' \
  --from-literal=SPRING_DATA_REDIS_PASSWORD='<MẬT KHẨU REDIS>'
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product rollout restart deploy/gwm-backend
```

- **Ngoại lệ**: Redis **Cluster mode** (như Redis chung BCCS 10.58.71.184/.185/.240
  có password `Mbccs@2025`) thì `host/port` bị bỏ qua, thay bằng env
  `SPRING_DATA_REDIS_CLUSTER_NODES=10.58.71.184:8385,10.58.71.185:8386,10.58.71.240:8387`
  — vẫn chỉ là thêm biến env, không phải code. (Chưa test thật với cụm này.)
- **Khi nào mới phải sửa `application.yml`**: chỉ khi muốn password mặc định
  "baked" vào jar (chạy local docker-compose). Với k8s: env-based là chuẩn —
  mỗi môi trường một giá trị, không rebuild.
- **Luôn test TCP từ trong cluster trước khi cắt** (bài học DB170):
  `kubectl run t --rm -i --restart=Never --image=registry-lab.kcntt.net/bccs/redis:7-alpine --command -- sh -c 'nc -z -w5 <IP> 6379; echo rc=$?'`
- **Hệ quả khi cắt**: business code fail-open (Redis chết app vẫn sống — đã
  verify code), nhưng `/actuator/health` tổng hợp fail-closed → thấy redisDown
  (fix H0 ở Phần 3 tách Redis khỏi readiness).

## Bổ sung 8B — Nâng cấp code CP khi có version mới (routine, KHÔNG chạy lại các bước 0–6)

| | Dựng lần đầu (đang thực hành) | Nâng cấp code (routine) |
|---|---|---|
| Bước làm | Bước 0→11 toàn bộ | Chỉ 3 việc dưới |
| ConfigMap/Secret | Tạo mới | KHÔNG đụng |
| Redis/Frontend | Apply | KHÔNG đụng (trừ khi frontend cũng đổi) |

```bash
# 1. Build jar mới (máy build)
cd "E:\product_4.0_02082026-krakend-gateway-manager\product_4.0_02082026-krakend-gateway-manager\krakend-gateway-manager\backend"
./mvnw.cmd clean package -DskipTests -Dmaven.test.skip=true
cp target/gateway-manager.jar ../k8s-deploy/backend/gateway-manager.jar

# 2. Build + push image — ĐỔI TAG để phân biệt version (VD 2.0.0-cp.3)
cd ../k8s-deploy
docker build -t reg-hosted-lab.kcntt.net/bccs/gateway-manager:2.0.0-cp.3 .
docker push reg-hosted-lab.kcntt.net/bccs/gateway-manager:2.0.0-cp.3

# 3. Báo pod chạy image mới — rolling update tự diễn ra
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product set image deploy/gwm-backend backend=registry-lab.kcntt.net/bccs/gateway-manager:2.0.0-cp.3
```

- **Đổi code** → chỉ set image. **Đổi ConfigMap/Secret** → patch + `rollout restart`.
- **Đổi frontend** → build Angular (`npm run build`) + copy dist + build/push image
  UI + `set image deploy/gwm-frontend frontend=<tag-mới>` — tương tự.
- **Downtime**: replicas=1 thì có khoảng vài phút pod mới đang Hibernate validate
  (~6.5 phút "thuế startup" MỖI lần pod mới sinh ra — không riêng lần đầu) mà
  Service chưa có backend nhận request → đây chính là 1 lý do nâng replicas=2
  (H1, Phần 3).
- rollback nếu image mới lỗi: `set image` lại về tag cũ.

## Bước 9 — Đã KHÔNG cần nữa (mặc định DB170 từ đầu)

Cắt DB170 giờ nằm ngay trong `00-configmap.yaml` + Bước 5.2 — không còn bước
patch riêng. Lần trước (2026-09-10) cắt bằng `kubectl patch` vì file còn mặc
định Oracle in-cluster; giờ giá trị thật đã sống trong repo, `apply` một lần
là đúng.

## Bước 10 — Verify cuối Control Plane

```bash
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product exec deploy/gwm-backend -- curl -s localhost:8080/actuator/health   # {"status":"UP"} trên DB170
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product get pods   # gwm-backend Running
```

**Lưu ý lần đầu start trên DB170**: backend mất vài phút ở Hibernate validate
qua WAN (~6.5 phút đo được) — startupProbe chịu tới ~10 phút nên KHÔNG bị kill.
Nếu thấy CrashLoopBackOff exit 137, log cuối `HHH000489` → startupProbe thiếu
(bẫy #B5).

## Bước 11 — Kiểm tra/duy trì team cho Data Plane

Team cần thiết (trong bảng GWM_TEAM trên DB170, sống sót qua teardown vì nằm ở
DB ngoài):

```bash
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product exec deploy/gwm-backend -- curl -s -H "X-Gateway-Admin-Key: <PLATFORM_KEY>" localhost:8080/api/teams
#  → kỳ vọng thấy team "default" + "myviettel" (key myviettel = 4MNjPBcyM... đã dùng cho Data Plane)
```

**Để làm gì**: Data Plane (Phần 2) cần `team_code` + `api_key` của team
`myviettel` để sync config. Team sống trong DB170 nên teardown k8s KHÔNG mất
team — chỉ mất nếu ai đó xoá dòng trong DB.

---

# PHẦN 2 — DATA PLANE trên cụm myviettel

> Đổi vai: từ đây coi như "đội myviettel" tự triển khai bản của mình.
> Kubeconfig mặc định (`dev-myviettel-context`), namespace `myviettel`.
> Image + redis/nginx mirror ĐÃ CÓ trên `reg-hosted-lab.kcntt.net` (cụm này
> pull được tên hosted — khác testlab-c2, bẫy #C2).

## Bước D0 — Điều kiện đầu vào

- [ ] CP đã UP (Phần 1 xong), team của đội + key có trên CP.
- [ ] Route cụm → CP THÔNG (bẫy #C1 — test TRƯỚC khi apply, 5 lần/node):

```bash
kubectl -n myviettel get pods -o wide | grep Running    # chọn 1 pod/node (PowerShell: | Select-String Running)
kubectl -n myviettel exec <pod> -- sh -c \
  'for i in 1 2 3 4 5; do curl -s -o /dev/null -w "%{http_code} " --max-time 5 http://10.29.1.14:8051/actuator/health; done; echo'
#  → "200 200 200 200 200" = node đó THÔNG (1 lần 200 chưa đủ!)
#  IP CP = 10.29.1.14 (node được cấp, xem Bước 8) — KHÔNG còn dùng .21
#  (PowerShell: gộp 1 dòng, sh -c giữ nháy đơn)
```

**Hiểu đúng lệnh test** (quan trọng, từng mảnh):
- `exec <pod> -- sh -c '...'` = nhảy vào 1 pod đang chạy, mở shell chạy chuỗi
  lệnh bên trong. `<pod>` = tên pod THẬT lấy từ lệnh `get pods -o wide` ngay
  trên — **chọn pod trên node mà DP sẽ đậu** (đã xác nhận: worker-10207252016).
- `for i in 1 2 3 4 5; do ...; done` = gọi 5 lần liên tiếp. `-o /dev/null -w
  "%{http_code} "` = chỉ in mã HTTP (vứt body). `--max-time 5` = mỗi lần quá
  5s coi fail (không treo vòng lặp).
- **Ý nghĩa thật**: 1 pod KHÔNG có đường mạng riêng — nó thừa kế route của
  NODE chứa nó. Test bằng pod nào trên node đó cũng ra cùng kết quả. Vậy nên
  test này đang thử **node**, pod chỉ là "cửa sổ". Node nào test 5/5 mới được
  đưa vào nodeAffinity — test node nào pin node đó, KHÔNG test 1 node rồi suy
  ra cả cụm (bẫy #C1: node .80/.91 bị chặn hoàn toàn, chỉ 16/17/83 thông).

Node đã xác nhận thông: `worker-10207252016/.17/.83` (manifest đã pin
nodeAffinity về 3 node này — giữ nguyên cho tới khi hạ tầng mở route toàn node).

- [ ] NodePort 8052/8053 còn trống trong namespace:

```bash
kubectl -n myviettel get svc -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{range .spec.ports[*]}{.nodePort}{" "}{end}{"\n"}{end}'
#  → 8052/8053 KHÔNG xuất hiện trong output = còn trống.
#  LƯU Ý phân biệt: deploy CŨ 1.1.0 (gateway-manager-backend/ui, tên dài, KHÔNG
#  phải bộ gwm-*) vẫn chiếm NodePort 8051/8837 trên cụm này — khi test nhớ
#  8051=app cũ (trả 401 health UP), 8052/8053=DP 2.0 của mình.
```

## Bước D1 — Tạo đội trên CP + Secret sync key (SINH MỚI — không dùng backup)

> **Quy tắc production (chốt 2026-09-13)**: lần đầu lên production KHÔNG có
> key cũ để lấy lại — mọi key sinh mới. Team key là thứ **hệ thống tự sinh**
> khi tạo đội (không phải mình đặt), hiện đúng 1 LẦN trong response — chép
> ngay, mất là phải tạo team khác.

```bash
# 1. Tạo team mới trên CP (bằng PLATFORM-ADMIN key — duy nhất mở /api/teams/**):
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product exec deploy/gwm-backend -- \
  curl -s -X POST -H "X-Gateway-Admin-Key: <PLATFORM_KEY>" \
  -H "Content-Type: application/json" \
  -d '{"teamCode":"myviettel-prod","teamName":"Don vi myviettel (production)"}' \
  localhost:8080/api/teams
#  → response chứa "apiKey" — CHÉP NGAY (chỉ hiện 1 lần này)
#  /api/teams liệt kê CHỈ tên đội, không bao giờ trả lại key (đó là lý do
#  phải chép ngay — key đội sống trong bảng GWM_TEAM DB170 dạng plaintext
#  nhưng đọc lại bằng SQL chỉ khi có quyền DBA)

# 2. Tạo secret DP với TEAM KEY vừa nhận (KHÔNG phải platform key —
#    nhầm 2 cái này là sync 401 vô hạn):
kubectl -n myviettel create secret generic gwm-secret \
  --from-literal=CONTROL_PLANE_SYNC_API_KEY='<apiKey-vừa-chép>'
#  (PowerShell: gộp 1 dòng)
```

**Để làm gì**: Data Plane dùng team key gọi `GET $CP_URL/api/config/export`
mỗi 15s — ApiKeyAuthFilter khớp key với dòng GWM_TEAM → gắn
`CurrentTeamContext=<teamCode>` → CP chỉ trả cấu hình CỦA ĐỘI ĐÓ (multi-tenant).
Team mới = DB chưa có endpoint/upstream nào của đội → sync đầu sẽ báo
"0 upstream, 0 endpoint" là ĐÚNG, không phải lỗi.

## Bước D2 — Sinh TLS cert MỚI cho nginx sidecar (không dùng cert cũ)

```bash
# 1. Sinh cert mới (Git Bash, openssl có sẵn) — SAN phải chứa IP node vì
#    App/Client sẽ gọi https://<node-IP>:8053; sai SAN → client từ chối cert:
#    BẪY Git Bash (gặp thật 2026-09-13): KHÔNG có MSYS_NO_PATHCONV=1 thì Git
#    Bash "dịch" /CN thành đường dẫn Windows (C:/Users/.../Git/CN=...) →
#    openssl chết NGAY SAU KHI ghi tls.key, KHÔNG sinh tls.crt, exit code 0
#    (dòng ....+.....local' trong output chính là đuôi câu lỗi bị trộn vào
#    progress sinh key — nhìn như thành công). kubectl sau đó báo "Cannot
#    read file tls.crt". Luôn dùng tiền tố MSYS_NO_PATHCONV=1:
cd /tmp
rm -f tls.key tls.crt
MSYS_NO_PATHCONV=1 openssl req -x509 -nodes -newkey rsa:2048 -days 365 \
  -subj "/CN=gwm-dataplane.myviettel.local" \
  -addext "subjectAltName=IP:10.207.252.17,DNS:gwm-dataplane.myviettel.local" \
  -keyout tls.key -out tls.crt
ls -la tls.crt tls.key
#  ĐẠT: cả 2 file tồn tại (crt ~1.1KB, key ~1.7KB) — rồi mới chạy lệnh dưới

# 2. Tạo secret tls:
kubectl -n myviettel create secret tls gwm-tls --cert=tls.crt --key=tls.key
#  → secret/gwm-tls created

# 3. DỌN file tạm NGAY (cert/key không để lại trên đĩa):
rm tls.crt tls.key
```

**Để làm gì**: container `tls-proxy` (nginx sidecar) nghe HTTPS 8443, terminate
TLS, proxy về `127.0.0.1:8080`. Cert self-signed hạn 1 năm — **ghi lịch thay
trước khi hết hạn** (sinh ngày nào + 365). Khác CP (H2): cert CP SAN = IP node
testlab-c2 (10.29.1.14/15); cert DP SAN = IP node myviettel (10.207.252.17) —
2 cụm, 2 cert, đừng dùng chung.

> **CẤP CERT CHO HỆ THỐNG CẦN GỌI DP (mô hình trust self-signed)**:
>
> - **Chỉ cấp file `tls.crt`, TUYỆT ĐỐI KHÔNG `tls.key`** — key tư nhân là thứ
>   giả mạo được gateway; nó chỉ sống trong secret `gwm-tls` của cluster.
>   Người gọi chỉ cần cert công khai (nó trong handshake ai cũng thấy) để
>   import vào truststore của mình.
> - **Chuỗi cấp file**: D2 đã `rm` file tạm ngay → khi có hệ thống mới cần gọi,
>   xuất lại từ secret (secret là nguồn chân truth duy nhất):
>   ```bash
>   kubectl -n myviettel get secret gwm-tls -o jsonpath='{.data.tls-crt}' | base64 -d > tls.crt
>   #   giao tls.crt cho đội gọi + 3 thông tin: URL chuẩn
>   #   https://10.207.252.17:8053, ngày sinh (hạn = +365), cách import:
>   #   keytool -importcert -alias gwm-dp -file tls.crt -cacerts -storepass changeit
>   #   (nếu app dùng JVM truststore riêng thì import vào store đó thay -cacerts)
>   rm tls.crt   # dọn file tạm sau khi giao xong
>   ```
> - **2 lựa chọn phía hệ thống gọi** (khác nhau về mức an toàn):
>   - **A (chuẩn)**: import `tls.crt` vào truststore → full verification
>     (hostname + hạn). Đây là "hợp đồng" chuẩn giữa team gateway và team App.
>   - **B (tạm thời, thường gặp)**: app tự cấu hình trust-all SSLContext —
>     không cần file cert nhưng MẤT hostname verification + validity check,
>     MITM vô hình. Chỉ dùng giai đoạn đầu; đặt lịch chuyển sang A.
> - **Xoay cert sau này**: sinh cert mới (cùng SAN, ngày mới) → `kubectl create
>   secret tls gwm-tls --from-file=... --dry-run=client -o yaml | kubectl apply -f -`
>   (lưu ý SA có thể không đủ quyền get secret cho dạng apply này — nếu
>   Forbidden thì delete + create) → `rollout restart deploy/gwm-backend` →
>   cấp `tls.crt` MỚI cho các đội, họ import lại.

> **KHÔNG DÙNG TLS CÓ ĐƯỢC KHÔNG? — KHÔNG, và không phải vì lý do bạn nghĩ**:
> volume `gwm-tls` là POD-level (40-backend.yaml) — thiếu secret thì kubelet
> không dựng được pod → **cả container backend cũng không start** (kẹt
> `ContainerCreating`, event `MountVolume.SetUp failed: secret "gwm-tls" not
> found`), chết cả HTTP 8052 chứ không riêng HTTPS 8053. Phân biệt 2 chế độ hỏng:
> thiếu hẳn secret → ContainerCreating; secret sai kiểu/không đúng key
> `tls.crt`/`tls.key` → pod lên nhưng nginx không thấy cert → tls-proxy
> CrashLoop. Muốn chạy thuần HTTP thì phải SỬA manifest (bỏ container
> tls-proxy + 2 volumes + port https của 2 Service) — không khuyến khích:
> traffic nghiệp vụ đi plaintext + lệch hướng production (App migrate dần
> sang 8053).
>
> **Bẫy SAN**: NodePort mở trên MỌI node nhưng cert chỉ có SAN
> `IP:10.207.252.17` → App gọi đúng .17 thì qua; gọi .16/.83 (2 node còn lại
> được pin) fail hostname verification (`SSLHandshakeException`). Muốn gọi
> node khác → sinh lại cert thêm SAN. Quy ước: App luôn gọi .17.
>
> **"Ứng dụng nội bộ gọi nhau thì có cần TLS không?"** — Đúng là service-to-service
> nội bộ BCCS (org→catalog, cron→API nội bộ...) toàn HTTP, không ai bắt TLS, và
> chấp nhận được vì cùng trust domain + traffic chỉ lượn trong dải nội bộ. Nhưng
> DP là BIÊN chứ không phải 1 service trong đám đó: người gọi là App của team
> khác, traffic qua node network (cross-node), mang token/dữ liệu khách hàng, và
> quan trọng nhất — **URL cho App là hợp đồng**: bật TLS "sau" = đổi URL
> 8052→8053 = thông báo + sửa từng App; bật từ đầu = App cắm đúng URL đích luôn
> (chi phí ≈ 0: self-signed + import 1 file tls.crt). Vì vậy thiết kế giữ CẢ 2
> đường: 8052 HTTP cho App nội bộ tin dải mạng (không cần import gì), 8053 HTTPS
> cho App mới cắm URL chuẩn. Secret gwm-tls BẮT BUỘC tồn tại (pod cần để lên),
> nhưng không bắt buộc mọi App dùng 8053 — đó là quyết định policy, hạ tầng để
> sẵn cả 2.

## Bước D3 — Sửa ConfigMap rồi Apply Data Plane

### D3.0 — Sửa `data-plane/00-configmap.yaml` TRƯỚC khi apply (bắt buộc)

Mở `data-plane/00-configmap.yaml`, sửa 3 giá trị:

| Biến | Sửa từ → thành | Vì sao |
|---|---|---|
| `CONTROL_PLANE_BASE_URL` | `http://10.29.1.21:8051` → `http://10.29.1.14:8051` | **BẮT BUỘC** — .21 vi phạm quy định hạ tầng (chỉ được dùng node .14/.15, xem H4). Đây là URL DP gọi `GET /api/config/export` mỗi 15s — sai URL thì sync chết |
| `TEAM_CODE` | `"myviettel"` → `"myviettel-prod"` | **Nên sửa cho khớp team mới** — nhưng hiểu đúng: biến này KHÔNG dùng để sync. DP nhận diện đội bằng team KEY (CP tra ngược `api_key` → `teamCode`), không gửi team code đi đâu cả. `TEAM_CODE` chỉ bị ghép vào `SPRING_APPLICATION_NAME`/`ELASTIC_APM_SERVICE_NAME` (data-plane/40-backend.yaml) — sai nó chỉ làm tên service trên APM/monitoring nhầm đội, sync vẫn chạy |
| `APM_ENVIRONMENT` | `"myviettel"` → `"myviettel-prod"` | Cosmetic cùng nhóm trên |

> **Kiến thức gốc — team identity nằm ở KEY, không ở ConfigMap**:
> `RemoteConfigSyncService.sync()` chỉ đặt header `X-Gateway-Admin-Key: <team key>`
> khi gọi CP (không có header team code nào). CP `ApiKeyAuthFilter` tra
> `findByApiKey` trong `GWM_TEAM` → ra `teamCode` → scope mọi query theo đội.
> Đội nào cầm key đội đó → tự được config của đội đó. Sai key = 401, không
> có cơ chế "khai team code tay".

Vì DP chưa từng apply (teardown sạch) → sửa file trước rồi apply là sạch nhất,
không cần restart gì. (Nếu sau này sửa configmap trên bản ĐANG chạy: phải
`kubectl -n myviettel apply -f data-plane/00-configmap.yaml` + `kubectl -n
myviettel rollout restart deploy/gwm-backend` — env chỉ được inject lúc
container start, sửa configmap không tự ảnh hưởng pod đang chạy.)

### D3.1 — Apply

```bash
cd "E:\product_4.0_02082026-krakend-gateway-manager\product_4.0_02082026-krakend-gateway-manager\krakend-gateway-manager\k8s"
kubectl -n myviettel apply -f data-plane/
```

**Để làm gì / những gì manifest đã harden sẵn (giữ nguyên)**:
- `replicas: 2` + `maxUnavailable: 0` + `podAntiAffinity` (required, theo
  hostname) — rolling/restart 1 pod không mất traffic; 2 pod BẮT BUỘC 2 node.
- `nodeAffinity` pin 3 node có route sang CP thông (16/17/83) — biện pháp tạm
  thời, nhờ network mở route toàn node rồi XÓA affinity.
- ConfigMap `gwm-tls-nginx-conf` (gộp trong 40-backend.yaml) — nginx sidecar.
- **Bẫy k8s đã gặp**: Service nhiều port thì port ĐẦU TIÊN cũng phải có `name:`
  (file này đã đúng).
- Image redis PHẢI là `reg-hosted-lab.kcntt.net/bccs/redis:7-alpine` — Docker
  Hub bị chặn DNS trong cụm (bẫy #C3), manifest đã đúng.

```bash
kubectl -n myviettel rollout status deploy/gwm-redis --timeout=120s
kubectl -n myviettel rollout status deploy/gwm-backend --timeout=180s
kubectl -n myviettel get pods -o wide
#  ĐẠT: 2 pod gwm-backend (2/2 container) Ready + gwm-redis Running, 2 pod nằm 2 node khác nhau
```

## Bước D4 — Verify sync từ Control Plane

```bash
# 4.1 Log sync — dòng QUAN TRỌNG NHẤT (cả 2 pod phải có)
kubectl -n myviettel logs deploy/gwm-backend -c backend --tail=50 | grep 'Da dong bo'
#  (PowerShell: | Select-String 'Da dong bo')
#  → team MỚI (myviettel-prod) chưa khai gì: "0 upstream, 0 endpoint" là ĐÚNG
#    (số >0 chỉ xuất hiện sau khi khai Upstream/Endpoint ở D5)
#  → team CŨ còn data: "1 upstream, 1 endpoint"

# 4.2 Readiness gồm remoteConfigSync — 0/1 Ready kèm log 'Loi dong bo' lặp = không tới CP (bẫy #C1)
kubectl -n myviettel get pod -l app.kubernetes.io/component=data-plane

# 4.3 Health tổng
kubectl -n myviettel exec deploy/gwm-backend -c backend -- curl -s localhost:8080/actuator/health
#  → {"status":"UP"}
```

Đọc lỗi sync trong ngoặc: `401` = sai key (nhầm platform key / dán nhầm
base64 — bẫy #B11); `Request cancelled`/timeout = route chưa mở (bẫy #C1);
`404` = sai URL.

## Bước D5 — Khai báo Upstream/Endpoint trên CP (bắt buộc với team mới)

> **Với team mới (myviettel-prod)**: DB chưa có gì của đội → BƯỚC BẮT BUỘC
> trước khi test traffic D6 — không có Upstream/Endpoint nào thì DP không có
> gì để dispatch. (Với team cũ `myviettel` thì data nằm sẵn trong DB170 —
> 4 upstream + endpoint optionset — bỏ qua bước này.)
> Khai báo bằng **TEAM KEY** của đội mình (KHÔNG phải platform key — bẫy #B6):
> mọi POST tự động gắn team_code theo key → data thuộc đội bạn.

```bash
# Upstream (LƯU "id" trong response — bước sau cần)
curl -X POST http://10.29.1.14:8051/api/upstreams \
  -H "X-Gateway-Admin-Key: <TEAM_KEY>" -H "Content-Type: application/json" \
  -d '{
    "name": "product-catalog",
    "baseHost": "http://product-catalog-service.myviettel.svc.cluster.local:8001",
    "connectTimeoutMs": 2000, "readTimeoutMs": 8000,
    "circuitBreakerEnabled": true, "failureRateThreshold": 50,
    "retryEnabled": true, "maxConcurrentCalls": 20, "maxWaitDurationMs": 1000
  }'

# Endpoint composite 1 step (target: "data" = bóc wrapper StandardResponse)
curl -X POST http://10.29.1.14:8051/api/endpoints \
  -H "X-Gateway-Admin-Key: <TEAM_KEY>" -H "Content-Type: application/json" \
  -d '{
    "name": "optionset-demo",
    "path": "/v1/myviettel/optionset/{code}",
    "method": "GET", "sequential": true, "outputEncoding": "json",
    "idempotencyEnabled": false, "parallelExecution": false, "responseCacheEnabled": false,
    "steps": [{
      "stepOrder": 1, "name": "get-optionset", "method": "GET",
      "urlPattern": "/product-catalog-service/v1/optionset/getByCode/{code}",
      "upstreamServiceId": "<id-vừa-lấy>",
      "forwardOriginalBody": false, "cacheEnabled": false, "cacheTtlSeconds": 300,
      "target": "data"
    }],
    "mappings": []
  }'
```

(PowerShell không chạy được khối JSON nhiều dòng này — ghi 2 file
`upstream.json`/`endpoint.json` rồi
`curl.exe -X POST http://10.29.1.14:8051/api/upstreams -H "X-Gateway-Admin-Key: <TEAM_KEY>" -H "Content-Type: application/json" -d "@$env:TEMP\upstream.json"`.
Git Bash: chạy nguyên khối.)

**Bẫy thực chiến**: port Service backend KHÔNG đoán 8080 — thật: product-catalog
= **8001** (bẫy #C6). Dò path API backend thật bằng `/v3/api-docs` (không dùng
`/actuator/mappings` — bị catch-all của BCCS chặn 404, bẫy #C7). Optionset code
tồn tại thật: `STATUS`, `TYPE`.

## Bước D6 — Verify traffic thật end-to-end (sau 15–30s sync)

```bash
# 6.1 Từ trong pod (nhanh nhất) — path theo team_code ĐÃ KHAI ở D5:
#     team mới myviettel-prod → path bạn đặt khi khai endpoint (VD /v1/myviettel-prod/...)
#     team cũ myviettel → /v1/myviettel/optionset/STATUS
kubectl -n myviettel exec deploy/gwm-backend -c backend -- \
  curl -s --max-time 10 "http://localhost:8080/<path-endpoint-bạn-khai>"
#  → 200 + JSON dữ liệu thật (t~50-80ms)

# 6.2 Qua NodePort HTTPS — đường product mà App/Client sẽ đi (path theo endpoint đã khai)
curl -sk -o /dev/null -w '%{http_code} %{time_total}s\n' --max-time 10 \
  "https://10.207.252.17:8053/<path-endpoint-bạn-khai>"
#  → 200

# 6.3 Qua NodePort HTTP (kênh migrate)
curl -s -o /dev/null -w '%{http_code}\n' --max-time 10 \
  "http://10.207.252.17:8052/<path-endpoint-bạn-khai>"
#  → 200

# 6.4 Path không tồn tại → 404 GW-ENDPOINT-NOT-FOUND
curl -sk -o /dev/null -w '%{http_code}\n' "https://10.207.252.17:8053/v1/myviettel/khong-ton-tai"

# 6.5 Upstream lỗi → GW-UPSTREAM-4XX (KHÔNG crash gateway)
curl -sk "https://10.207.252.17:8053/v1/myviettel/optionset/XYZ-KHONG-CO"

# 6.6 Test chu kỳ sync: sửa config trên CP → đợi ≤20s → gọi lại 6.2 thấy đổi
```

(Git Bash: chạy nguyên. PowerShell: dùng `curl.exe` thay `curl` (alias
Invoke-WebRequest không hiểu cờ `-sk`), `-o NUL` thay `-o /dev/null`, `-w "%{http_code}"`
(nháy kép) thay nháy đơn — VD:
`curl.exe -sk -o NUL -w "%{http_code} %{time_total}s\n" --max-time 10 "https://10.207.252.17:8053/v1/myviettel/optionset/STATUS"`.)

Test HA tự phục hồi (đã chứng minh 2026-09-11): xoá 1 backend pod → pod mới tái
lập, traffic 200 liên tục (Service chỉ điều pod còn lại).

---

# SỔ TAY LỖI NHANH (tra cứu khi thực hành)

## Control Plane (testlab-c2, ns product)

| Triệu chứng | Gốc rễ | Xử lý |
|---|---|---|
| CrashLoopBackOff **exit 137**, log cuối `HHH000489`, chết ~2ph15s | Hibernate validate qua WAN ~6.5' > liveness 2ph15s → SIGKILL (KHÔNG phải OOM) | startupProbe failureThreshold 60 đã có trong manifest — nếu thiếu thì thêm (bẫy #B5) |
| CrashLoopBackOff exit 1, log `ORA-00942` | thiếu bảng (schema chưa V2) | chạy V2 migrate / init SQL |
| CrashLoopBackOff exit 1, `ORA-01017` | sai DB_USER/PASSWORD | patch secret `gwm-secret` + restart |
| Readiness 503 mãi, health DOWN, log `connection refused localhost:6379` | thiếu Redis (health fail-closed) | apply `30-redis.yaml` + restart (bẫy #B4) |
| `ImagePullBackOff` `lookup reg-hosted-lab... timeout` | cụm testlab-c2 chỉ pull được `registry-lab.kcntt.net` | sửa `image:` sang tên pull được (bẫy #B1) |
| 401 `GW-UNAUTHORIZED` khi gọi /api/endpoints bằng platform key | 2 lớp key | `/api/teams/**` = platform key; còn lại = team key (bẫy #B6) |
| UI 502 khi backend restart | nginx resolve DNS | FE phải dùng FQDN `gwm-backend.product.svc.cluster.local` + `resolver <kube-dns>` (bẫy #B6) |
| NodePort test từ máy trạm fail nhưng pod trong cụm OK | firewall chặn TCP tới node | test bằng temp pod trong cụm (bẫy #B7) |
| `AlreadyExists: pods "t" already exists` khi `kubectl run` | pod test cũ chưa dọn (--rm không kịp khi Ctrl+C) | `kubectl -n product delete pod t --force --grace-period=0` rồi chạy lại |
| 401 GW-UNAUTHORIZED khi gọi /api/teams **bằng platform key đúng** | dán nhầm base64 (kèm dấu nháy) vào secret — soi: jsonpath + `base64 -d \| od -c` thấy `"` đầu chuỗi / 89 ký tự / đuôi `==` | delete + create lại secret bằng key đã decode + `rollout restart` (bẫy #B11, gặp thật 2026-09-13) |
| `Forbidden ... cannot get resource "secrets"` khi `create secret --dry-run=client -o yaml \| kubectl apply` | SA team-sa/dev-myviettel không có quyền `get` secrets (apply cần get để diff) | delete + create lại secret trực tiếp |
| Lệnh kubectl dài dán vào terminal báo `required flag(s) "image" not set` / gãy giữa chừng | terminal wrap dòng khi dán → kubectl nhận lệnh bị cắt | viết lệnh vào file `.sh` rồi `bash file.sh` (đã làm với `E:\kubeconfigs\dbcheck.sh`) |
| `nc <ClusterIP> 8051` fail | NodePort KHÔNG mở trên ClusterIP | chỉ test qua IP node (bẫy #B8) |
| Đổi ConfigMap không có tác dụng | env không tự reload | `rollout restart deploy/gwm-backend` |
| Oracle crash `ORA-65012`/`ORA-01081` | tag `:23-slim` mới / đặt `ORACLE_DATABASE` | dùng `23.9-slim`, không đặt biến (bẫy #B2) |
| Oracle crash lúc "Starting Oracle Instances" | `/dev/shm` 64MB | emptyDir Memory 2Gi đã có trong manifest (bẫy #B2) |

## Data Plane (myviettel, ns myviettel)

| Triệu chứng | Gốc rễ | Xử lý |
|---|---|---|
| Sync WARN lặp `401 Unauthorized` | sai sync key | patch secret `gwm-secret` + restart |
| Sync WARN lặp `Request cancelled` | route liên cụm chặn (KHÔNG đồng nhất theo node) | test 5 lần/node (C1.2); nodeAffinity pin 16/17/83 đã có |
| Pod `0/1 Ready`, log `Loi dong bo` lặp | chưa sync được LẦN NÀO (fail-closed lần đầu là ĐÚNG) | sửa URL/key → pod tự Ready |
| tls-proxy CrashLoopBackOff | thiếu `gwm-tls` / `gwm-tls-nginx-conf` | tạo lại secret tls (bước D2) |
| `ImagePullBackOff` redis | Docker Hub bị chặn DNS | image phải là `reg-hosted-lab.kcntt.net/bccs/redis:7-alpine` (bẫy #C3) |
| Chỉ 1 backend pod Running (replicas=2) | không đủ node đạt nodeAffinity | mở rộng danh sách node hoặc bỏ affinity |
| 2 backend pod cùng 1 node | podAntiAffinity thiếu | đã có trong manifest — đừng xoá |
| Traffic 404 GW-ENDPOINT-NOT-FOUND dù đã khai báo | sync chưa tới chu kỳ | đợi 15–20s, check `grep 'Da dong bo'` (PowerShell: `Select-String 'Da dong bo'`) |
| Config sửa trên CP không hiệu lực | chu kỳ sync 15s | đợi ≤20s; resilience config invalidate tự động ≤15s (không cần restart) |

---

# Checklist hoàn tất thực hành (đánh dấu khi xong)

- [ ] P1-B1/2/3: build jar + FE + push image (tag `2.0.0-cp.2`)
- [ ] P1-B4: TCP tới DB170 thông + 9 bảng GWM_* đủ trên DB170
- [ ] P1-B5: secret `gwm-secret` tạo (dùng lại platform key từ backup)
- [ ] P1-B6/8: Control Plane UP + health UP (DB170) + test /api/teams = 200
- [ ] P1-B11: `/api/teams` thấy team `default` + `myviettel`
- [ ] P2-D0: route 3 node 16/17/83 → CP 5/5 lần 200
- [ ] P2-D1/D2: secret sync key + gwm-tls tạo
- [ ] P2-D3: apply data-plane, 2 pod Ready trên 2 node khác nhau
- [ ] P2-D4: log "Da dong bo cau hinh tu Control Plane: N upstream, M endpoint" trên cả 2 pod
- [ ] P2-D6: curl end-to-end qua 8052 (HTTP) + 8053 (HTTPS) + 2 case lỗi (404 / GW-UPSTREAM-4XX)
- [ ] P3-H0: readiness CP tách Redis (scale redis 0 → pod vẫn Ready)
- [ ] P3-H1: backend CP lên 2 replica trên 2 node
- [ ] P3-H2: TLS cho CP (sidecar + NodePort HTTPS mới)
- [ ] P3-H3: bật audit khi có ES

Tài liệu gốc chi tiết hơn (nếu cần đào sâu):
- `k8s/control-plane/PRODUCTION_RUNBOOK.md` — runbook CP product thật
- `k8s/data-plane/PRODUCTION_RUNBOOK.md` — runbook DP từng đội + tool audit resilience
- `k8s/README.md` — phân vai CP/DP (ADR-07)

---

# PHẦN 3 — NÂNG HA SAU KHI DỰNG XONG (bài nâng cao, LÀM SAU checklist Phần 1+2)

> Đánh giá production-readiness 2026-09-12: mức test hiện tại CHƯA phải
> production vì 3 điểm — (1) backend CP 1 replica = điểm gãy duy nhất,
> (2) CP expose HTTP trần 8051/8837 (key team đi qua kênh này mỗi 15s khi DP
> sync), (3) audit log đang tắt. Giải quyết lần lượt dưới đây.

## Bước H0 — Tách Redis khỏi readiness của CP (sửa ngữ nghĩa health, KHÔNG cần build image)

**Vấn đề**: code nghiệp vụ dùng Redis đều fail-open (GatewayCacheService catch→
cache-miss, RateLimitService catch→allow) — Redis chỉ phục vụ cache "Thử ngay"
trên UI, KHÔNG phải phụ thuộc sống còn. Nhưng Spring Boot tự đăng ký
RedisHealthIndicator vào `/actuator/health` tổng hợp (fail-closed) mà
readinessProbe của CP lại trỏ đúng endpoint tổng hợp đó → Redis chết = pod
Running nhưng không bao giờ Ready (bẫy #10). Health nói dối ngữ nghĩa nghiệp vụ.

**Fix bằng env var** (relaxed binding override cấu hình trong jar — không
rebuild image): sửa `control-plane/40-backend.yaml`:

```yaml
# thêm vào danh sách env của container backend:
- name: MANAGEMENT_ENDPOINT_HEALTH_GROUP_READINESS_INCLUDE
  value: "readinessState,db"        # readiness CHỈ phụ thuộc DB (phụ thuộc thật)

# đổi readinessProbe httpGet.path:
path: /actuator/health/readiness   # thay vì /actuator/health
```

**Kết quả**: Redis chết → pod vẫn Ready (đúng ngữ nghĩa fail-open), tổng hợp
`/actuator/health` vẫn hiện `redisDown` để quan sát. Giữ deployment Redis
(Thử ngay vẫn có cache). Về sau muốn CP thuần stateless thì xoá
`30-redis.yaml` — readiness không còn phụ thuộc nó nữa.

```bash
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product apply -f control-plane/40-backend.yaml
# Test: scale redis về 0 → pod backend vẫn Ready; health tổng hợp vẫn thấy redisDown
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product scale deploy/gwm-redis --replicas=0
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product get pods   # backend vẫn 1/1 (hoặc 2/2)
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product exec deploy/gwm-backend -- curl -s localhost:8080/actuator/health   # status DOWN nhưng có redisDown — pod vẫn Ready
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product scale deploy/gwm-redis --replicas=1   # trả lại
```

## Bước H1 — Backend CP replicas: 2 + podAntiAffinity + maxUnavailable: 0

Sửa `control-plane/40-backend.yaml` — copy pattern đã harden ở
`data-plane/40-backend.yaml`:

```yaml
spec:
  replicas: 2                 # thay replicas: 1
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 0       # rolling không gián đoạn CRUD
  ...
    spec:
      affinity:
        podAntiAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
            - labelSelector:
                matchLabels:
                  app.kubernetes.io/name: gateway-manager
                  app.kubernetes.io/component: control-plane
              topologyKey: kubernetes.io/hostname
```

**Để làm gì**: 2 pod nằm 2 node khác nhau — node down vẫn còn 1 pod phục vụ.
Không cần nodeAffinity cho CP (khác DP) vì CP không cần route liên cụm; k8s tự
xếp. Lưu ý trước khi apply: `kubectl patch` không thêm được struct lồng sâu —
sửa file rồi apply lại là cách đúng.

```bash
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product apply -f control-plane/40-backend.yaml
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product rollout status deploy/gwm-backend --timeout=600s
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product get pods -o wide
#  ĐẠT: 2/2 pod Ready, 2 node khác nhau
```

(Đây là bước sửa FILE manifest rồi apply lại — `kubectl patch` không thêm
được struct lồng sâu như affinity/strategy.)

## Bước H2 — TLS cho Control Plane (nginx sidecar, copy pattern DP)

Không thể dùng SSL bundle Spring (1 connector duy nhất sẽ biến 8080 thành
HTTPS, phá probe + phải build lại image) — dùng nginx sidecar như DP đã làm:

1. Sinh cert (SAN = 2 IP node được cấp + DNS) — chạy trong **Git Bash** (openssl
   có sẵn). BẮT BUỘC tiền tố `MSYS_NO_PATHCONV=1` — không có thì Git Bash dịch
   `/CN` thành đường dẫn Windows → openssl chết ngay sau khi ghi tls.key, KHÔNG
   sinh tls.crt (bẫy chi tiết ở D2, gặp thật 2026-09-13):
   ```bash
   cd /tmp
   rm -f tls.key tls.crt
   MSYS_NO_PATHCONV=1 openssl req -x509 -nodes -newkey rsa:2048 -days 365 \
     -subj "/CN=gwm-cp.product.local" \
     -addext "subjectAltName=IP:10.29.1.14,IP:10.29.1.15,DNS:gwm-cp.product.local" \
     -keyout tls.key -out tls.crt
   ls -la tls.crt tls.key   # ĐẠT: cả 2 file tồn tại
   kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product create secret tls gwm-tls \
     --cert=tls.crt --key=tls.key
   rm tls.crt tls.key       # dọn file tạm NGAY
   ```
2. Thêm vào `control-plane/40-backend.yaml`: container `tls-proxy` (image
   `registry-lab.kcntt.net/nginx:1.29-alpine`, 8443→127.0.0.1:8080) + ConfigMap
   `gwm-tls-nginx-conf` + mount Secret — y hệt cấu trúc trong
   `data-plane/40-backend.yaml` (chỉ khác `server_name`).
3. Thêm port https `8443→nodePort <mới>` vào `control-plane/60-ingress.yaml`
   (Service backend-nodeport sẽ có 2 port — **nhớ đặt `name:` cho port đầu tiên**,
   bẫy k8s đã gặp ở DP).
4. DP sau đó đổi `CONTROL_PLANE_BASE_URL` sang `https://...:nodePort-mới` +
   mount/qua bản cert (hoặc App tin `http` nội bộ trong khi chờ DNS).

## Bước H3 — Bật audit log khi có ES

testlab-c2 ns product hiện CHƯA có Elasticsearch. Khi có (hoặc trỏ qua ES chung):

```bash
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product patch configmap gwm-config \
  -p '{"data":{"GATEWAY_AUDIT_ENABLED":"true","GATEWAY_AUDIT_ES_HOST":"<es-host>","ES_PORT":"9200","GATEWAY_AUDIT_RETENTION_DAYS":"30"}}'
kubectl --kubeconfig "E:\kubeconfigs\product-kubeconfig.yaml" -n product rollout restart deploy/gwm-backend
```

(Git Bash: nguyên khối. PowerShell: JSON `-p '...'` nháy đơn chạy được nguyên
dạng vì bên trong chỉ có nháy kép — nhưng vẫn phải gộp 1 dòng.)

Kiểm tra index audit xuất hiện: `GET <es-host>:9200/_cat/indices` →
`gwm-hops-*` / `gwm-requests-*`.

## Bước H4 — Pin node theo quy ước hạ tầng (ĐÃ LÀM 2026-09-13, ghi lại để hiểu)

**Quy định đội hạ tầng**: đội Gateway Manager chỉ được dùng node
`worker-10029001014` (10.29.1.14) + `worker-10029001015` (10.29.1.15).

**Vì sao phải pin bằng nodeAffinity**: scheduler mặc định rải pod khắp cụm —
nó KHÔNG biết quy ước "đội nào dùng node nào" (gặp thật: sau teardown-dựng-lại,
redis đậu .17, backend .23, frontend .25 — tất cả ngoài dải được cấp). Quy ước
hạ tầng là "quy định mềm" từ phía người; phải **biểu diễn thành config k8s**
thì scheduler mới tôn trọng:

```yaml
affinity:
  nodeAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
      nodeSelectorTerms:
        - matchExpressions:
            - key: kubernetes.io/hostname
              operator: In
              values: [worker-10029001014, worker-10029001015]
```

Đã thêm vào cả 3 manifest (`30-redis`, `40-backend`, `50-frontend`) — một pod
quên pin là một pod rò rỉ ra ngoài dải.

**Hiệu lực**: apply → rollout thay pod (backend mất thêm 1 chu kỳ validate
6.5'). Đã verify: 3/3 pod gwm-* trên .14, health UP.

**Đánh đổi phải hiểu rõ**:
- 2 node phải luôn sống — 1 node down → pod của node đó Pending (không còn chỗ
  đậu hợp lệ). Hạ tầng mở rộng dải → thêm tên node vào list.
- Đây KHÔNG phải taint phía node — 5 BCCS service hàng xóm trong ns product
  vẫn đậu tự do trên node khác (không do mình quản). Câu hỏi để mở: quy ước
  ".14/.15" áp dụng cho cả namespace hay chỉ Gateway Manager? Nếu cả namespace
  → phải mang hỏi hạ tầng, vì mình không pin được pod của người khác.

**Phân biệt với nodeAffinity của DP (Phần 2)**: DP pin 3 node 16/17/83 cụm
myviettel vì lý do MẠNG (route liên cụm chỉ mở ở các node đó); CP pin .14/.15
vì lý do QUY ĐỊNH HẠ TẦNG. Cùng công cụ, khác động cơ.

## Việc nào KHÔNG cần làm cho CP (chấp nhận mức hiện tại)

- Redis HA/PVC — cache UI "Thử ngay", mất tự rebuild
- HPA — CRUD không có tải spike
- Oracle HA — thuộc hạ tầng chung DB170 (phụ trách DBA)
- Frontend replicas 2 — làm thêm được nếu muốn (stateless, rẻ), nhưng UI admin
  không phải đường truyền traffic thật → ưu tiên thấp nhất
