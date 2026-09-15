# HƯỚNG DẪN TRIỂN KHAI CONTROL PLANE PRODUCTION

> Dùng kèm bộ manifest trong thư mục này (`README.md` — bảng khác biệt với bản lab).
> Control Plane là bản DUY NHẤT của nền tảng, chạy trên cụm testlab-c2, namespace
> `product` — các đội Data Plane gọi về đây để sync cấu hình.
>
> Quy ước lệnh: viết theo bash/kubectl. PowerShell: đổi `\` thành backtick, tiền tố
> `MSYS_NO_PATHCONV=1` cho lệnh openssl (bẫy B19: MSYS dịch `/CN` thành đường dẫn Windows).

---

## 0. Checklist đầu vào — đủ thứ này mới bắt đầu

| # | Cần gì | Kiểm tra thế nào |
|---|--------|------------------|
| 1 | **Oracle trung tâm reachable từ trong cụm** | `kubectl run t --rm -i --restart=Never --image=registry-lab.kcntt.net/bccs/redis:7-alpine --command -- sh -c "nc -z -w5 10.207.222.170 1521; echo rc=$?"` → `rc=0`. Máy trạm không route được DB170 không sao — pod trong cụm phải được |
| 2 | **Schema đã có 9 bảng** (UPSTREAM_SERVICE, ENDPOINT_CONFIG, BACKEND_STEP, BACKEND_STEP_ALLOW, BACKEND_STEP_DENY, BACKEND_STEP_MAPPING, FIELD_MAPPING, ENDPOINT_CONFIG_VERSION, GWM_TEAM) | `sqlplus BCCS_PRODUCT/<pass>@//10.207.222.170:1521/DB170` → `SELECT table_name FROM user_tables;` |
| 3 | **2 NodePort còn trống** (đề xuất UI 8837 / API 8051) | `kubectl get svc -A \| grep <port>` — range NodePort cụm 8000–10000 |
| 4 | **2 node pin** theo quy ước hạ tầng (.14/.15) | `kubectl get nodes` + xác nhận node tồn tại. Nếu quy ước thay đổi → sửa nodeAffinity ở CẢ 3 file 30/40/50 |
| 5 | **Mật khẩu DB + platform-admin key** | Sinh key mới: `openssl rand -base64 32`. KHÔNG dùng lại key của môi trường khác (production first-time = sinh mới toàn bộ) |
| 6 | **Image backend 2.0.0-cp.5** (+ UI cp.1) có sẵn trên registry | Có sẵn trên `registry-lab.kcntt.net` — cụm phân giải đúng tên này (KHÔNG đổi sang reg-hosted-lab) |

> Nếu schema CHƯA có (DB trống hoàn toàn): xem mục 5a — lần đầu deploy phải
> chạy 1 replica để Hibernate `ddl-auto=update` tạo schema, tránh 2 pod race.

---

## 1. Tạo namespace + chuẩn bị

```bash
kubectl create namespace product   # hoặc namespace nền tảng đã chỉ định

# Không cần edit manifest nào ngoài:
# - 00-configmap.yaml: DB_HOST/DB_PORT/DB_NAME nếu khác DB170
# - 30/40/50-backend: nodeAffinity nếu quy ước node khác .14/.15
# - 60-ingress.yaml: nodePort nếu 8837/8051 không còn trống
```

---

## 2. Apply ConfigMap + Redis

```bash
kubectl apply -f 00-configmap.yaml -n product
kubectl apply -f 30-redis.yaml -n product
kubectl rollout status deploy/gwm-redis -n product --timeout=60s
```

Redis phải Ready trước backend — health indicator của CP gồm Redis
(fail-closed): không có Redis thì backend không bao giờ Ready (bẫy #10).

---

## 3. Tạo Secret — KHÔNG BAO GIỜ qua file YAML

```bash
# platform-admin key: sinh mới (production first-time)
openssl rand -base64 32

kubectl create secret generic gwm-secret -n product \
  --from-literal=DB_USER='BCCS_PRODUCT' \
  --from-literal=DB_PASSWORD='<mật-khậu-thật-DB170>' \
  --from-literal=GATEWAY_ADMIN_API_KEY='<key-vừa-sinh>'
```

Quy tắc an toàn (bắt buộc):
- Secret KHÔNG commit git, KHÔNG lưu file trong repo.
- Backup duy nhất: thư mục ngoài repo —
  `kubectl get secret gwm-secret -n product -o json > E:\kubeconfigs\gwm-secrets-backup-YYYYMMDD\cp-product-secrets.json`
- **Bẫy B11 khi khôi phục từ backup**: file backup chứa giá trị base64 — phải
  `base64 -d` TRƯỚC khi dán vào `--from-literal`. Dán nguyên chuỗi base64 (hoặc
  kèm dấu nháy JSON) → secret trông OK nhưng mọi API call 401 GW-UNAUTHORIZED.
- Verify key sau tạo:
  `kubectl get secret gwm-secret -n product -o jsonpath='{.data.GATEWAY_ADMIN_API_KEY}' | base64 -d | od -c`
  → 44 ký tự, không có `"` ở đầu/cuối.

---

## 4. Deploy backend + frontend

```bash
kubectl apply -f 40-backend.yaml -n product
kubectl apply -f 50-frontend.yaml -n product
kubectl rollout status deploy/gwm-backend -n product --timeout=600s
```

**Đợi rollout tối đa 10 phút** — Hibernate validate schema qua WAN mất ~6.5
phút là BÌNH THƯỜNG (bẫy B5: đã bắt bằng startupProbe 60×10s; rút ngắn con số
này = quay lại exit 137 CrashLoopBackOff).

Log đáng mong đợi khi thành công:

```bash
kubectl logs -n product deploy/gwm-backend --tail=50 | grep -E "Started|HHH000489"
# "Started GatewayManagerApplication in ~390-400 seconds" là OK
```

---

### 5a. Trường hợp đặc biệt — DB trống hoàn toàn (lần đầu tiên có schema mới)

Hibernate `ddl-auto=update` tự tạo 9 bảng khi start. 2 pod chạy ĐỒNG THỜI có
thể race tạo schema → lần đầu:

```bash
kubectl scale deploy/gwm-backend -n product --replicas=1
# ... đợi pod Ready (mất 6-7 phút, schema được tạo lúc này) ...
kubectl scale deploy/gwm-backend -n product --replicas=2
```

Những lần deploy sau (schema đã có) apply thẳng 2 replicas như file là được.

---

## 5. Apply NodePort + nghiệm thu

```bash
kubectl apply -f 60-ingress.yaml -n product
```

Nghiệm thu theo thứ tự:

```bash
# (a) Health tổng hợp (gồm Oracle + Redis — cả 2 phải UP)
# NodePort KHÔNG truy cập được từ máy trạm trên cụm testlab-c2 → test trong cluster:
kubectl run t --rm -i --restart=Never --image=curlimages/curl --command -- \
  curl -s http://gwm-backend.product.svc.cluster.local:8080/actuator/health

# (b) Platform key hoạt động (2-lớp key: key này CHỈ mở /api/teams/**)
kubectl run t --rm -i --restart=Never --image=curlimages/curl --command -- \
  curl -s -H "X-Gateway-Admin-Key: <platform-key>" \
  http://gwm-backend.product.svc.cluster.local:8080/api/teams

# (c) UI mở từ browser máy trạm qua NodePort 8837 (UI port thường được mở firewall)
#     http://10.29.1.14:8837

# (d) API NodePort 8051 từ trong cụm (máy trạm bị chặn firewall — bẫy B7):
kubectl run t --rm -i --restart=Never --image=curlimages/curl --command -- \
  curl -s -H "X-Gateway-Admin-Key: <platform-key>" http://10.29.1.14:8051/api/teams
```

---

## 6. Bàn giao đội Data Plane đầu tiên (tạo team)

Quy trình chuẩn (làm qua UI hoặc curl — dùng platform-admin key):

```bash
# Tạo team qua API (key hiển thị ĐÚNG 1 LẦN — đội phải lưu ngay)
kubectl run t --rm -i --restart=Never --image=curlimages/curl --command -- \
  curl -s -X POST -H "X-Gateway-Admin-Key: <platform-key>" \
  -H "Content-Type: application/json" \
  -d '{"teamCode":"<ma-doi>","teamName":"<ten-hien-thi>"}' \
  http://10.29.1.14:8051/api/teams
```

Trả về `apiKey` (Base64url 32 byte) → giao cho đội đó để họ:
1. Đưa vào Secret `gwm-secret` phía Data Plane (`CONTROL_PLANE_SYNC_API_KEY`).
2. Dùng làm header `X-Gateway-Admin-Key` khi CRUD endpoint/upstream của mình.

> **Mất team key**: không mất data — admin đọc lại plaintext cột `api_key`
> bảng `GWM_TEAM` trên DB170 (`SELECT api_key FROM gwm_team WHERE team_code='...'`).
> **Mất platform-admin key**: xem runbook vòng đời key (backup ngoài repo /
> sinh key mới + `rollout restart` + cấp lại cho mọi bên dùng UI).

Sau đó đội tự triển khai Data Plane theo
`../data-plane/production/HƯỚNG_DẪN_PRODUCTION.md`.

---

## 7. Bảo trì hằng ngày

```bash
# Đổi ConfigMap/Secret → BẮT BUỘC restart (env không tự reload):
kubectl rollout restart deploy/gwm-backend -n product
# Lưu ý: restart qua WAN = 6-7 phút startupProbe lại — làm giờ thấp điểm.

# Kiểm tra 2 pod luôn đứng 2 node khác nhau:
kubectl get pods -n product -l app.kubernetes.io/component=control-plane -o wide

# Theo dõi sync của các Data Plane (log CP không ghi sync — xem phía DP).
```

### Rollback

```bash
kubectl rollout history deploy/gwm-backend -n product
kubectl rollout undo deploy/gwm-backend -n product [--to-revision=<N>]
```

Cấu hình nghiệp vụ của các đội (endpoint/upstream) KHÔNG nằm ở CP manifest —
nó nằm DB170; đội tự rollback qua Endpoint Version trên UI.

---

## 8. Những giới hạn / rủi ro đã biết

| Hiện tượng | Nguyên nhân | Xử lý |
|---|---|---|
| Startup 6-7 phút mỗi lần restart | Hibernate validate metadata Oracle qua WAN (bẫy B5) | Chấp nhận — đã có startupProbe 10 phút. Làm giờ thấp điểm |
| Cả 2 CP pod rơi readiness cùng lúc khi WAN chập chờn | readiness fail-closed với Oracle | Hành vi ĐÚNG — không nhận CRUD khi không chắc dữ liệu. DP phía dưới không bị ảnh hưởng (fail-open) |
| NodePort không vào được từ máy trạm | Firewall cụm testlab-c2 chặn TCP tới node (bẫy B7/B15) | Verify trong cluster; UI 8837 thường được mở sẵn |
| 2 pod race tạo schema | `ddl-auto=update` | Chỉ lần đầu DB trống: deploy 1 replica trước (mục 5a) |
| DB170 là SPOF | Oracle trung tâm duy nhất | Chấp nhận cho giai đoạn này; DP vẫn phục vụ traffic khi DB down |
| SA không có `get nodes`/`get secrets` | RBAC hạn chế | Tìm node IP qua `/proc/net/arp` trong pod (bẫy B14); thay secret = delete + create (bẫy B12) |
