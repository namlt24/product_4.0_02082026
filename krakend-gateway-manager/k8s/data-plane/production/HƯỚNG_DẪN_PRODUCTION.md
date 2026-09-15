# HƯỚNG DẪN TRIỂN KHAI DATA PLANE PRODUCTION (sizing 1000 RPS)

> Dùng kèm bộ manifest trong thư mục này (`README.md` — bảng khác biệt với bản lab).
> Cụm đích: cụm Data Plane của đội (namespace riêng của đội), Control Plane trung
> tâm chạy trên cụm testlab-c2 (namespace `product`, NodePort backend API 8051).
>
> Quy ước lệnh: viết theo bash/kubectl. Nếu dùng PowerShell thì thêm `MSYS_NO_PATHCONV=1`
> khi lệnh có `\` và đổi `\` thành backtick — hoặc chạy qua Git Bash cho đúng nguyên văn.

---

## 0. Checklist đầu vào — đủ thứ này mới bắt đầu

| # | Cần gì | Lấy ở đâu | Kiểm tra thế nào |
|---|--------|-----------|------------------|
| 1 | **Team key** (api_key của đội, cấp khi tạo team) | Đội nền tảng tạo team cho bạn qua UI Control Plane (`POST /api/teams`) — key hiện **đúng 1 lần** lúc tạo | Gọi thử: `curl -H "X-Gateway-Admin-Key: <team-key>" http://<ip-node-14>:8051/api/config/export` → trả JSON cấu hình (có thể rỗng) là key đúng |
| 2 | **NodePort trống** cho HTTP + HTTPS | Liên hệ quản trị cụm Data Plane | `kubectl get svc -A | grep <port-đề-xuất>` — phải trống trên toàn cụm |
| 3 | **3 worker node** đủ điều kiện pin | `kubectl get nodes` | Với từng node: `curl -m 5 http://<ip-node-14>:8051/api/actuator/health` từ node đó thông (route liên cụm mở). Nếu hạ tầng không chặn theo node → bỏ pin, xoá khối affinity |
| 4 | **Redis nội bộ cụm** (nếu cụm có Redis dùng chung) hoặc dùng `30-redis.yaml` trong bộ này | Quản trị cụm | `kubectl get svc redis...` — nếu dùng Redis ngoài, sửa `REDIS_HOST/PORT` trong 00-configmap |
| 5 | **Elasticsearch** cho audit (tuỳ chọn) | Quản trị cụm | Không có → `GATEWAY_AUDIT_ENABLED: "false"` |
| 6 | **TLS cert** — đề xuất tự tạo (production first-time = tạo mới, KHÔNG dùng lại cert cũ) | Tự tạo trên máy bạn, bước 4 | SAN chứa IP node sẽ exposed + hostname dữ liệu |
| 7 | **Image** | `reg-hosted-lab.kcntt.net/bccs/gateway-manager:2.0.0-cp.5` (đã có sẵn) — hoặc tự build theo `backend/Dockerfile` | `kubectl run --rm -it test --image=<image> --restart=Never -- ls /app/app.jar` — hoặc chỉ cần tin tag |

> **Quan trọng về team key**: key trong Secret chỉ là `CONTROL_PLANE_SYNC_API_KEY`.
> KHÔNG ĐỤNG vào key nền tảng (`l7ifSjJN+hqPxEyQOA8tBrN31j0JykJY0jp7zHfQTKA=`) —
> key đó là của Control Plane, không phải của đội Data Plane.

---

## 1. Tạo namespace + chuẩn bị file

```bash
# Tạo namespace riêng của đội (nếu chưa có)
kubectl create namespace <ns-cua-doi>

# Edit 00-configmap.yaml: điền 3 biến TEAM_CODE / CONTROL_PLANE_BASE_URL /
# APM_ENVIRONMENT (bảng ở đầu file liệt kê rõ).
# Edit 40-backend.yaml: 3 chỗ đánh dấu "SỬA" (node pin, server_name, comment cert).
# Edit 60-ingress.yaml: nodePort còn trống của cụm bạn.
```

---

## 2. Apply ConfigMap

```bash
kubectl apply -f 00-configmap.yaml -n <ns-cua-doi>
```

---

## 3. Tạo Secret team key — KHÔNG BAO GIỜ qua file YAML

```bash
kubectl create secret generic gwm-secret -n <ns-cua-doi> \
  --from-literal=CONTROL_PLANE_SYNC_API_KEY='<team-key-bước-1>'

# Xoá khỏi lịch sử shell ngay nếu muốn an toàn tuyệt đối
history -c   # (bash; PowerShell thì không cần vì -p không lưu vào file)
```

Quy tắc an toàn (bắt buộc):
- Key KHÔNG commit git, KHÔNG lưu file nào trong repo, KHÔNG quote vào chat/output.
- Backup duy nhất được phép: thư mục ngoài repo, ví dụ `E:\kubeconfigs\gwm-secrets-backup-*\`.
- Mất key → xin đội nền tảng cấp lại (tạo lại team key qua UI CP).

---

## 4. Tạo TLS Secret (self-signed 1 năm, SAN đủ cả IP + hostname)

```bash
# sinh cert+key vào thư mục tạm — xoá ngay sau bước tạo Secret
openssl req -x509 -nodes -newkey rsa:2048 -days 365 \
  -keyout /tmp/gwm-tls.key -out /tmp/gwm-tls.crt \
  -subj "/CN=gwm-dataplane.<doi-ban>.local" \
  -addext "subjectAltName=IP:<ip-node-chính>,DNS:gwm-dataplane.<doi-ban>.local"

kubectl create secret tls gwm-tls -n <ns-cua-doi> \
  --cert=/tmp/gwm-tls.crt --key=/tmp/gwm-tls.key

# XOÁ key khỏi máy ngay — tls.key KHÔNG bao giờ rời cụm sau bước này
rm /tmp/gwm-tls.key /tmp/gwm-tls.crt
```

> App/Client cần tin cậy: phân phát **chỉ file tls.crt** (lấy từ Secret:
> `kubectl get secret gwm-tls -n <ns> -o jsonpath='{.data.tls\.crt}' | base64 -d > tls.crt`),
> KHÔNG bao giờ phân phát tls.key.

---

## 5. Deploy Redis

```bash
kubectl apply -f 30-redis.yaml -n <ns-cua-doi>
```

(Đã sizing 1 CPU / 512Mi limit — Redis 1 thread, ≥2000 ops/s cần ~1 core.)

---

## 6. Deploy backend (Deployment + nginx conf + Service + PDB)

```bash
kubectl apply -f 40-backend.yaml -n <ns-cua-doi>
```

Giám sát rollout — **đợi đủ 3 pod Ready mới qua bước sau**:

```bash
kubectl rollout status deployment/gwm-backend -n <ns-cua-doi> --timeout=300s
kubectl get pods -n <ns-cua-doi> -l app.kubernetes.io/component=data-plane -o wide
```

Nếu pod kèm Ready quá 3 phút → xem log:

```bash
kubectl logs -n <ns-cua-doi> deploy/gwm-backend -c backend --tail=100
```

---

## 7. Mở NodePort cho client + nghiệm thu

```bash
kubectl apply -f 60-ingress.yaml -n <ns-cua-doi>
```

Nghiệm thu theo thứ tự:

```bash
# (a) Config đã sync từ Control Plane về chưa (bắt buộc trước khi nhận traffic)
kubectl logs -n <ns-cua-doi> deploy/gwm-backend -c backend --tail=50 | grep -i "sync"

# (b) Health
curl http://<ip-node>:<nodePort-http>/actuator/health/readiness

# (c) Gọi 1 endpoint composite thật của đội
curl http://<ip-node>:<nodePort-http>/v1/<endpoint-cua-doi>/...

# (d) HTTPS (cert self-signed thì thêm -k)
curl -k https://<ip-node>:<nodePort-https>/v1/<endpoint-cua-doi>/...

# (e) Đối chiếu bản dựng đang chạy
kubectl get deploy gwm-backend -n <ns-cua-doi> \
  -o jsonpath='{.spec.template.spec.containers[0].image}'
```

---

## 8. Bulkhead từng upstream — BẮT BUỘC trước khi nhận tải thật

`40-backend.yaml` chỉ mới phòng ngực Tomcat 500 thread/pod. Lớp chắn TỪNG
UPSTREAM là **bulkhead Resilience4j**, và giá trị của nó KHÔNG nằm trong manifest —
nó là **dữ liệu DB trên Control Plane** (cột `max_concurrent_calls` của
`upstream_service`, sửa qua UI Control Plane).

**Công thức**: `maxConcurrentCalls ≥ P99(giây) × rps-đi-lên-upstream-đó × 1.2` (per pod).
Kết quả chia cho số pod nếu đặt ở mức tổng — nhưng giữ nguyên đơn vị per-pod
vì bulkhead đếm trong 1 JVM.

Ví dụ: upstream nhận 100 rps/pod, p99 2s → `≥ 2 × 100 × 1.2 = 240`.

Trạng thái hiện tại: mặc định 20/concurrent/pod = chỉ đủ ~13 rps cho upstream
p99 1.5s (`20 / (1.5 × 1.2) ≈ 11`). **Trước 1000 RPS phân bổ lên nhiều
upstream, gần như chắc chắn phải tăng từng cái** — làm ngay sau khi khai báo
upstream trên UI:

1. Vào UI Control Plane → Upstream → sửa từng upstream.
2. Đặt `maxConcurrentCalls` theo công thức trên.
3. Lưu — Data Plane tự sync trong ≤15s, không cần deploy.

---

## 9. Kiểm tra giới hạn đã thật sự áp dụng

```bash
# Rate limit (gọi quá 3000/60s từ 1 IP → 429)
for i in $(seq 1 3100); do curl -s -o /dev/null -w "%{http_code}\n" \
  http://<ip-node>:<nodePort>/actuator/health/readiness; done | sort | uniq -c

# Bulkhead đầy (429/503 ngay lập tức thay vì xếp hàng chờ timeout)
# — chỉ có khi upstream thật sự chậm; theo dõi:
kubectl logs -n <ns-cua-doi> deploy/gwm-backend -c backend --tail=100 \
  | grep -iE "bulkhead|circuitbreaker|rate.?limit" | tail -20

# Số thread Tomcat thực tế đang dùng (vào pod):
kubectl exec -n <ns-cua-doi> deploy/gwm-backend -c backend -- \
  sh -c 'curl -s http://localhost:8080/actuator/metrics/tomcat.threads.busy'
```

---

## 10. Rollback

```bash
# Xem lịch sử
kubectl rollout history deployment/gwm-backend -n <ns-cua-doi>

# Lùi 1 revision
kubectl rollout undo deployment/gwm-backend -n <ns-cua-doi>

# Lùi về 1 revision cụ thể
kubectl rollout undo deployment/gwm-backend --to-revision=<N> -n <ns-cua-doi>

# Quan trọng: cấu hình NGHIỆP VỤ (endpoint/upstream) KHÔNG rollback theo pod —
# nó nằm trên Control Plane. Muốn quay lại cấu hình cũ: dùng Endpoint Version
# (UI Control Plane có màn hình version, restore) — không phải kubectl.
```

---

## 11. Scale lên 2000 RPS sau này — KHÔNG phải build lại image

Toàn bộ là thay đổi cấu hình:

1. **replicas 3 → 6** trong `40-backend.yaml` (đồng thời nới nodeAffinity/pdb —
   PDB `minAvailable: 2` đổi thành 4, hoặc đổi thành percentage `50%`).
2. **`GATEWAY_HTTP_POOL_MAX_TOTAL: 600 → 1200`** và
   **`GATEWAY_HTTP_POOL_MAX_PER_ROUTE: 550 → 1100`** trong `00-configmap.yaml`.
3. **Rate limit tăng theo** (`GATEWAY_RATE_LIMIT_MAX_REQUESTS`) — đếm theo IP,
   tăng gấp đôi nếu số IP client không đổi.
4. **Redis**: 1 CPU còn đủ cho ~15-25k ops/s; 2000 rps × 2 ops = 4000 ops/s
   → vẫn dư, không phải đụng. Nếu nâng cao hơn nữa mới xem Redis Cluster.
5. **Bulkhead từng upstream** trong UI Control Plane — công thức mục 8,
   số rps mới = rps-đến-upstream × 2.
6. **Tomcat 500/pod giữ nguyên** (333 rps/pod không đổi khi scale ngang).

Apply:

```bash
kubectl apply -f 00-configmap.yaml -n <ns-cua-doi>
kubectl apply -f 40-backend.yaml -n <ns-cua-doi>
kubectl rollout status deployment/gwm-backend -n <ns-cua-doi> --timeout=300s
```

maxUnavailable=1 + maxSurge=0 với 6 pod vẫn rollout lần lượt 1 pod/lượt an toàn.

---

## 12. Những giới hạn đã biết (ghi lại để không ngạc nhiên)

| Hiện tượng | Nguyên nhân | Khi nào quan tâm |
|---|---|---|
| Audit log mất sự kiện khi >~500 sự kiện/s | Hàng đợi audit 5000 phần tử, flush 500/1s — đầy thì DROP (fail-open) | Chỉ khi cần audit 100% sự kiện ở 1000 rps — phải sửa code tăng queue/flush |
| Đổi timeout/bulkhead upstream không ăn ngay | Resilience4j registry cache config từ lần init đầu | Restart deployment sau khi đổi cài đặt Resilience của upstream |
| `STEP_RESPONSE_ARRAY_AGGREGATE` trả JSON string | Đặc tả hiện tại | OK với hầu hết backend (Jackson lenient) |
| Steps chạy tuần tự | Chưa có parallel step | Nếu 1 endpoint có nhiều step độc lập — tổng latency = tổng từng step |
| Control Plane / DB170 down | Sync fail-open: Data Plane giữ cache cũ chạy tiếp | Chỉ không thêm/sửa được endpoint mới — không mất traffic |

---

## 13. Bảo mật — tóm tắt các điều BẮT BUỘC

1. **Secret không qua git**: team key bằng `kubectl create secret` (bước 3),
   TLS bằng `kubectl create secret tls` (bước 4). File `01-secret.yaml.example`
   chỉ là mẫu đọc, không apply.
2. **tls.key không rời cụm** sau khi tạo Secret — chỉ phân phát `tls.crt` cho App.
3. **Backup secret** chỉ vào thư mục ngoài repo (`E:\kubeconfigs\gwm-secrets-backup-*\`).
4. **Production first-time = tạo mới toàn bộ** (key, cert) — không khôi phục
   backup của môi trường khác vào production.
