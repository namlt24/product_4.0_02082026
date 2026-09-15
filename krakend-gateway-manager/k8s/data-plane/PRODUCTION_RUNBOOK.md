# RUNBOOK TRIỂN KHAI DATA PLANE — HẠ TẦNG PRODUCT THẬT (MỖI ĐỘI 1 BẢN)

> Tài liệu "cầm tay chỉ việc" cho **mỗi đội BCCS**: triển khai Data Plane của
> chính đội mình lên 1 cụm Kubernetes **môi trường thật** — người dùng thật,
> request thật (App/Client của đội gọi vào endpoint composite qua gateway này).
> Biên soạn 2026-09-10, dựa trên deploy thực tế ĐÃ THÀNH CÔNG lần đầu tiên trên
> cụm myviettel tích hợp với Control Plane chạy trên cụm testlab-c2 (mọi lỗi/bẫy
> ghi trong tài liệu đều đã gặp THẬT).
>
> Phân vai: đội nền tảng đã (1) chạy Control Plane trung tâm theo
> `../control-plane/PRODUCTION_RUNBOOK.md` và (2) cấp cho đội bạn 2 giá trị
> `team_code` + `api_key`. **Từ đây chỉ đội BCCS làm.**

---

## 0. Điều kiện đầu vào (kiểm tra TRƯỚC khi làm)

Đánh dấu ✅ từng mục, thiếu mục nào xử lý mục đó, KHÔNG bỏ qua:

- [ ] **2 giá trị do đội nền tảng cấp**: `team_code` (vd `myviettel`) +
      `api_key` (hiện DUNG 1 LẦN lúc tạo đội — nếu đã làm mất, đội nền tảng
      phải xoá đội và tạo lại). KHÔNG có 2 giá trị này thì DỪNG — Data Plane
      không thể đồng bộ cấu hình.
- [ ] **URL của Control Plane trung tâm** (đội nền tảng cung cấp): dạng
      `http://<host>:<port>` trỏ tới API backend CP (`/api/config/export`).
      VD đã dùng thật: `http://10.29.1.21:8051` (IP node + NodePort CP).
- [ ] **Kết nối từ cụm của đội → Control Plane THÔNG**: bắt buộc test TRƯỚC
      khi apply (bẫy #C1):
      ```bash
      kubectl exec -n <NS> <1-pod-bất-kỳ-có-curl> -- \
        curl -s -o /dev/null -w '%{http_code}\n' --max-time 8 \
        http://<CP-host>:<CP-port>/actuator/health
      # → 200 là THÔNG. 000/timeout = route chưa mở, nhờ network mở trước.
      ```
- [ ] **Cụm k8s** có namespace riêng của đội, kubeconfig với quyền:
      deploy/svc/configmap/secret/pod trong namespace đó.
- [ ] **Registry Docker nội bộ** mà cụm pull được (bẫy #C2 — tên registry cụm
      pull được CÓ THỂ khác tên registry máy build push).
- [ ] **1 redis image trong registry nội bộ** (bẫy #C3 — Docker Hub gần như
      chắc chắn bị chặn DNS trong cụm product; kiểm tra
      `curl -sk https://<REGISTRY>/v2/_catalog | grep redis`).
- [ ] **Elasticsearch cho audit log** (TUỲ CHỌN — chưa có thì bật
      `GATEWAY_AUDIT_ENABLED=false`, không ảnh hưởng traffic).
- [ ] **Port truy cập từ mạng App/Client**: xác nhận cụm dùng NodePort hay
      Ingress. Nếu NodePort: xin admin dải NodePort + **firewall mở TCP tới
      IP node từ đúng mạng mà App/Client gọi vào** (ping node được KHÔNG có
      nghĩa TCP mở).
- [ ] **Danh sách dịch vụ backend thật** mà endpoint composite sẽ gọi: ít nhất
      1 Service k8s trong namespace đội (hoặc host ngoài) + 1 path GET không
      cần auth để làm demo verify (bước 7).

Giá trị cần điền trước (dùng xuyên suốt tài liệu):

| Biến | Ví dụ (đã chạy thật 2026-09-10) | Ghi chú |
|---|---|---|
| `NS` | `myviettel` | namespace của đội |
| `TEAM_CODE` | `myviettel` | do đội nền tảng cấp |
| `TEAM_KEY` | (secret) | api_key do đội nền tảng cấp — chỉ đi vào Secret |
| `CP_URL` | `http://10.29.1.21:8051` | Control Plane trung tâm |
| `REGISTRY` | `reg-hosted-lab.kcntt.net/bccs` | registry cụm PULL được |
| NodePort traffic | `8052` | port để App/Client gọi vào gateway; kiểm tra trống |

Kiểm tra NodePort còn trống (SA thường không được list service toàn cụm —
kiểm tra trong namespace mình + hỏi admin dải đã cấp):

```bash
kubectl get svc -n $NS -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{range .spec.ports[*]}{.nodePort}{" "}{end}{"\n"}{end}'
```

---

## 1. Tạo Secret (làm TRƯỚC khi apply — deployment ref thiếu = pod không start)

Secret KHÔNG ghi vào file YAML, KHÔNG commit — chỉ dùng lệnh:

```bash
kubectl create secret generic gwm-secret -n $NS \
  --from-literal=CONTROL_PLANE_SYNC_API_KEY='<TEAM_KEY-đội-nền-tảng-cấp>'
```

> Key này là **api_key của đội** trong bảng GWM_TEAM ở DB trung tâm. Data
> Plane dùng nó gọi `GET $CP_URL/api/config/export` mỗi 15s — hệ thống tự lọc
> cấu hình theo đội của key, đội nào không thấy cấu hình đội nào khác
> (multi-tenant). **Không cần khai báo team_code ở đây** — nó chỉ nằm trong
> ConfigMap để đặt tên service/APM.

---

## 2. Soạn cấu hình (sửa file manifest trước khi apply)

Sửa trong thư mục `k8s/data-plane/`:

### 2.1 `00-configmap.yaml` — cấu hình không bảo mật

| Khóa | Giá trị product | Lưu ý |
|---|---|---|
| `TEAM_CODE` | `<TEAM_CODE>` | PHẪI khớp team do nền tảng cấp |
| `CONTROL_PLANE_BASE_URL` | `<CP_URL>` | KHÔNG kèm `/api/config/export` — sync tự nối |
| `CONTROL_PLANE_SYNC_INTERVAL_SECONDS` | `15` | config mới trên CP có hiệu lực DP sau tối đa chu kỳ này |
| `REDIS_HOST/REDIS_PORT` | `gwm-redis / 6379` | giữ nguyên — phải chạy `30-redis.yaml` cùng namespace |
| `GATEWAY_RATE_LIMIT_ENABLED` | `true` | 300 req/phút/endpoint mặc định; chỉnh `MAX_REQUESTS` theo tải thật |
| `GATEWAY_AUDIT_ENABLED` | `false` nếu CHƯA có ES; `true` khi có | bật sau, chỉ cần sửa ConfigMap + restart |
| `GATEWAY_AUDIT_ES_HOST/ES_PORT` | host ES của đội | audit là tùy chọn, fail-open |
| `GATEWAY_AUDIT_RETENTION_DAYS` | `30` (product) | quá hạn tự xóa index mỗi 24h; `<=0` = giữ vĩnh viễn |
| `APM_*` | theo hạ tầng APM | agent tự tắt nếu không kết nối được |

### 2.2 `40-backend.yaml` — image, HA, TLS sidecar

```yaml
image: <REGISTRY>/gateway-manager:2.0.0-cp.1
```

(Tên image do đội nền tảng build sẵn và push vào registry — đội BCCS KHÔNG
cần build, chỉ đổi tên registry nếu cụm pull được tên khác — bẫy #C2.)

**HA — giữ nguyên mặc định của manifest (đã cấu hình sẵn):**
- `replicas: 2` + `strategy.rollingUpdate.maxUnavailable: 0` — restart/rolling
  1 pod không mất traffic; pod còn lại gánh trong lúc pod kia start (~30s).
- `podAntiAffinity` (required, theo hostname) — 2 replica BẮT BUỘC nằm 2 node
  khác nhau, nếu không replicas=2 vô nghĩa khi node down.
- Rate-limit và cache đếm qua **Redis dùng chung** — 2 pod không phá hành vi
  rate limit/cache.

**nodeAffinity — CHỈ thêm khi cụm có chặn route một chiều theo node** (bẫy
#C1: cụm myviettel chỉ 3/5 node ra được Control Plane). Cách làm đúng:

1. Test từ pod trên TỪNG node (xem lệnh ở bẫy #C1.2) — node nào 5/5 lần 200
   mới tính THÔNG.
2. Pin deployment về các node đó bằng `requiredDuringSchedulingIgnoredDuringExecution`
   (đủ node cho 2 replica đặt ở 2 node khác nhau).
3. **Nhờ network mở route cho toàn bộ node, rồi XÓA affinity** — pin node là
   biện pháp tạm thời, node bị pin down = pod chết.

**TLS chặng client→gateway — nginx sidecar (đã cấu hình sẵn trong manifest):**

Container `tls-proxy` (image nginx) nghe HTTPS 8443, terminate TLS, proxy về
`127.0.0.1:8080` (backend HTTP nội bộ pod). Chọn sidecar thay vì SSL bundle
Spring vì: không phải build lại image; HTTP 8052 vẫn sống song song để
App/Client migrate dần; probe k8s không đổi.

Chuẩn bị TLS (bắt buộc TRƯỚC khi apply — pod tls-proxy thiếu cert sẽ CrashLoop):

```bash
# 2.2.1 Sinh cert (self-signed 1 năm, SAN = IP node NodePort sẽ mở + 1 DNS nội bộ).
#       Nếu đội có CA nội bộ / cert thật thì dùng cert đó thay thế — tốt hơn.
openssl req -x509 -nodes -newkey rsa:2048 -keyout tls.key -out tls.crt \
  -days 365 \
  -subj "/CN=gwm-dataplane.<team>.local" \
  -addext "subjectAltName=IP:<node-ip>,DNS:gwm-dataplane.<team>.local"

# 2.2.2 Tạo Secret k8s (KHÔNG commit file cert/key vào git)
kubectl create secret tls gwm-tls -n $NS --cert=tls.crt --key=tls.key
```

Sửa trong `40-backend.yaml` + ConfigMap `gwm-tls-nginx-conf` (cuối file):
`server_name` và SAN cert khớp DNS đội tự chọn; giới hạn body
`client_max_body_size 10m` chỉnh theo payload thật.

> **App/Client cần tin cậy cert**: gửi `tls.crt` cho đội App để import vào
> truststore (self-signed). Đến hạn cert (1 năm): tạo Secret mới + restart
> deployment — nên đưa vào lịch vận hành.

### 2.3 `30-redis.yaml` — đổi image

```yaml
image: <REGISTRY>/redis:7-alpine     # KHÔNG dùng redis:7-alpine từ Docker Hub (bẫy #C3)
```

Redis ở đây là bắt buộc nghiệp vụ (cache-aside per-step + bộ đếm rate-limit),
nhưng cả 2 đều **fail-open**: mất Redis traffic không dừng, chỉ chậm hơn và
mất rate limit. Không cần PersistentVolume.

### 2.4 `60-ingress.yaml` — expose traffic thật, chọn 1 trong 2

- **Cụm dùng NodePort** (đa số cụm BCCS nội bộ): giữ file hiện tại — Service
  NodePort `gwm-backend-nodeport` có 2 port: HTTP (VD thật: `8052`, chưa mã
  hóa — dùng khi migrate hoặc mạng tin cậy hoàn toàn) và HTTPS `8443`→
  `nodePort:` (VD thật: `8053` — TLS terminate tại nginx sidecar). Sửa 2 giá
  trị `nodePort:` cho khớp dải đã xin; **chuẩn product: chỉ công bố port
  HTTPS cho App/Client, giữ HTTP nội bộ để so sánh/migrate**.
- **Cụm có NGINX Ingress Controller**: thay file bằng Ingress host
  `gwm-api.<domain-đội>` trỏ `service.name: gwm-backend`, `port.number: 8443`
  + `tls:` trỏ Secret `gwm-tls` (khi đó có thể bỏ sidecar).

Traffic thật của đội = `https://<node-ip>:<nodePort-https>/<path-endpoint-composite>`.

---

## 3. Apply theo thứ tự

```bash
export KUBECONFIG=<kubeconfig-cụm-đội>

# 3.1 Secret TRƯỚC (đã làm ở bước 1)

# 3.2 Toàn bộ manifest còn lại
kubectl apply -f k8s/data-plane/ -n $NS

# 3.3 Theo dõi rollout
kubectl rollout status deploy/gwm-redis   -n $NS --timeout=120s
kubectl rollout status deploy/gwm-backend -n $NS --timeout=180s   # data-plane KHÔNG có DB → start nhanh (~30s)
kubectl get pods -n $NS -o wide
```

**Đạt** khi: 2 pod `gwm-backend` (2/2 container: backend + tls-proxy) +
`gwm-redis` `Running`, backend `2/2 Ready`, 2 backend pod nằm 2 node khác nhau
(`-o wide` cột NODE khác nhau — podAntiAffinity). Pod tls-proxy CrashLoop =
thiếu Secret `gwm-tls`/ConfigMap `gwm-tls-nginx-conf` (bước 2.2).

---

## 4. Verify đồng bộ cấu hình từ Control Plane (checklist)

```bash
# 4.1 Log sync — dòng QUAN TRỌNG NHẤT sau khi pod Ready (cả 2 pod phải có)
kubectl logs deploy/gwm-backend -n $NS -c backend --tail=50 | grep 'Da dong bo'
#   → "Da dong bo cau hinh tu Control Plane: N upstream, M endpoint."
#   N=M=0 vẫn ĐẠT nếu đội chưa khai báo gì trên CP (lần sync đầu).

# 4.2 Readiness gồm remoteConfigSync — pod không Ready nếu chưa sync được lần nào
kubectl get pod -n $NS -l component=data-plane
#   → 2/2 Ready  (0/1 kèm log 'Loi dong bo cau hinh' lặp = không tới được CP → bẫy #C1)

# 4.3 Health tổng
kubectl exec -n $NS deploy/gwm-backend -c backend -- curl -s localhost:8080/actuator/health
#   → {"status":"UP"}
```

Sai `CONTROL_PLANE_BASE_URL`/key là lỗi phổ biến nhất — log sync sẽ lặp WARN
`Loi dong bo cau hinh tu Control Plane (giu nguyen cache cu...)` mỗi 15s. Đọc
lý do trong ngoặc: `401 Unauthorized` = sai key; `Request cancelled`/timeout =
route chưa mở (bẫy #C1); `404` = sai URL.

---

## 5. Khai báo cấu hình đội lên Control Plane (UI trung tâm, KHÔNG phải ở đây)

Data Plane KHÔNG có màn hình quản trị — mọi Upstream/Endpoint khai báo qua UI
hoặc API của Control Plane trung tâm, bằng **team key của đội**:

```bash
# 5.1 Upstream = 1 backend thật (Service trong namespace đội / host ngoài)
curl -X POST http://<CP-host>:<CP-port>/api/upstreams \
  -H "X-Gateway-Admin-Key: $TEAM_KEY" -H "Content-Type: application/json" \
  -d '{
    "name": "product-catalog",
    "description": "backend product-catalog của đội",
    "baseHost": "http://product-catalog-service.<NS>.svc.cluster.local:8001",
    "connectTimeoutMs": 2000, "readTimeoutMs": 8000,
    "circuitBreakerEnabled": true, "failureRateThreshold": 50,
    "retryEnabled": true, "maxConcurrentCalls": 20, "maxWaitDurationMs": 1000
  }'
# → LƯU "id" trong response — bước 5.2 cần nó.

# 5.2 Endpoint composite (VD 1 step GET)
curl -X POST http://<CP-host>:<CP-port>/api/endpoints \
  -H "X-Gateway-Admin-Key: $TEAM_KEY" -H "Content-Type: application/json" \
  -d '{
    "name": "optionset-demo",
    "path": "/v1/<team>/optionset/{code}",
    "method": "GET", "sequential": true, "outputEncoding": "json",
    "idempotencyEnabled": false, "parallelExecution": false, "responseCacheEnabled": false,
    "steps": [{
      "stepOrder": 1, "name": "get-optionset", "method": "GET",
      "urlPattern": "/product-catalog-service/v1/optionset/getByCode/{code}",
      "upstreamServiceId": "<id-từ-bước-5.1>",
      "forwardOriginalBody": false, "cacheEnabled": false, "cacheTtlSeconds": 300,
      "target": "data"
    }],
    "mappings": []
  }'
```

Lưu ý thực chiến:

- **Header key đúng tên**: `X-Gateway-Admin-Key` dùng cho CẢ platform-admin
  key (chỉ `/api/teams/**`) lẫn team key (mọi `/api/**` còn lại). Sai loại key
  nhận `401 GW-UNAUTHORIZED` không phân biệt thêm (bẫy #B6 runbook CP).
- **Port Service backend**: KHÔNG đoán 8080 — `kubectl get svc -n $NS` xem cột
  port (thật: product-catalog = **8001**). Port trong `baseHost` = port Service.
- **Dò path API của backend thật**: thử `GET /v3/api-docs` hoặc
  `/swagger-ui/index.html` trên service đó; path theo pattern
  `<tên-service>/<v1>/...`. Actuator/mappings thường bị catch-all chặn 404.
- **`target: "data"`** = bóc wrapper `StandardResponse` của BCCS (trả thẳng
  object `data` cho client). Không có wrapper thì bỏ field này.
- 2 đội KHÔNG được trùng (path, method) — unique theo `(TEAM_CODE, PATH)`.

---

## 6. Verify traffic thật end-to-end (checklist chốt trước khi bàn giao)

Sau 15–30s (một chu kỳ sync), bắn traffic:

```bash
# 6.1 Từ trong namespace (nhanh nhất)
kubectl exec -n $NS deploy/gwm-backend -c backend -- \
  curl -s --max-time 10 "http://localhost:8080/v1/<team>/optionset/<code-co-du-lieu>"
#   → 200 + JSON dữ liệu thật (đã bóc "data" nếu đặt target)

# 6.2 Qua NodePort HTTPS — ĐÚNG ĐƯỜNG product mà App/Client sẽ đi
curl -sk -o /dev/null -w '%{http_code} %{time_total}s\n' \
  --max-time 10 "https://<node-ip>:<nodePort-https>/v1/<team>/optionset/<code>"
#   → 200, thời gian phản hồi hợp lý (<1s cho 1 step; lần đầu chậm hơn do handshake)

# 6.3 Qua NodePort HTTP (kênh cũ — chỉ để so sánh/migrate; product KHÔNG công bố)
curl -s -o /dev/null -w '%{http_code}\n' \
  --max-time 10 "http://<node-ip>:<nodePort-http>/v1/<team>/optionset/<code>"
#   → 200

# 6.4 Case lỗi: path không tồn tại trong cache định tuyến
curl -sk -o /dev/null -w '%{http_code}\n' "https://<node-ip>:<nodePort-https>/v1/<team>/khong-ton-tai"
#   → 404 GW-ENDPOINT-NOT-FOUND

# 6.5 Case lỗi: upstream trả lỗi (code sai)
curl -sk "https://<node-ip>:<nodePort-https>/v1/<team>/optionset/XYZ-KHONG-CO"
#   → GW-UPSTREAM-4XX kèm message gốc của backend (KHÔNG crash gateway)

# 6.6 Đổi cấu hình có hiệu lực không (test chu kỳ sync)
#   Trên CP: sửa endpoint (vd thêm denyFields) → đợi 20s → gọi lại 6.2 thấy thay đổi
```

Cuối cùng test từ **máy thật của App/Client** (không phải máy build/kubectl) —
nếu fail từ đây mà 6.2 pass thì là firewall/network, không phải gateway (bẫy
#C5). Nếu App bắt lỗi cert (self-signed) → gửi `tls.crt` cho đội App import
vào truststore (2.2.2).

---

## 7. Vận hành hằng ngày / xử lý sự cố nhanh

| Triệu chứng | Kiểm tra | Xử lý |
|---|---|---|
| Sync WARN lặp `401 Unauthorized` | key sai | sửa Secret `gwm-secret`, `rollout restart deploy/gwm-backend` |
| Sync WARN lặp `Request cancelled`/timeout | route cụm → CP | test theo bẫy #C1; nếu chặn theo node → nodeAffinity (2.2) |
| Pod `0/1 Ready`, log sync lỗi lặp | chưa sync được LẦN NÀO | readiness fail-closed lần đầu là ĐÚNG — sửa URL/key/sync rồi pod tự Ready |
| Pod tls-proxy `CrashLoopBackOff` | `logs -c tls-proxy --previous` | thiếu Secret `gwm-tls` hoặc ConfigMap `gwm-tls-nginx-conf` — xem 2.2 |
| Pod backend `CrashLoopBackOff` exit 1 | `logs -c backend --previous` | thiếu biến bắt buộc (Secret/ConfigMap ref sai tên) |
| `ImagePullBackOff` | event `lookup ... i/o timeout` | đổi `image:` sang registry cụm pull được (bẫy #C2); redis/nginx cũng vậy (bẫy #C3) |
| Readiness 503, health DOWN kèm `redisDown` | thiếu Redis | apply `30-redis.yaml` cùng namespace |
| Chỉ 1 backend pod Running (replicas=2) | `get events` | không đủ node đạt nodeAffinity → mở rộng danh sách node hoặc bỏ affinity |
| 2 backend pod cùng 1 node | `-o wide` cột NODE | podAntiAffinity thiếu/sai — xem 2.2 |
| Traffic HTTPS handshake fail từ App | cert/App truststore | App chưa import `tls.crt` (self-signed) hoặc SAN không khớp host gọi |
| Traffic 404 GW-ENDPOINT-NOT-FOUND dù đã khai báo | sync chưa tới chu kỳ | đợi 15–20s; kiểm tra `grep 'Da dong bo'` thấy N/M tăng |
| Traffic trả `GW-UPSTREAM-4XX/5XX` | backend thật lỗi | xem message gốc trong response — đây là lỗi backend, không phải gateway |
| Rate limit 429 bất thường | `MAX_REQUESTS` thấp quá | sửa ConfigMap + restart |
| Config sửa trên CP không có hiệu lực | chu kỳ sync | đợi ≤15s; vẫn không thì kiểm tra log sync |

Câu lệnh tra cứu nhanh:

```bash
kubectl get pods -n $NS -o wide
kubectl logs deploy/gwm-backend -n $NS -c backend --tail=100 -f
kubectl logs deploy/gwm-backend -n $NS -c tls-proxy --tail=20        # lỗi TLS/handshake
kubectl logs deploy/gwm-backend -n $NS -c backend --tail=100 | grep 'Da dong bo' | tail -3
kubectl exec -n $NS deploy/gwm-backend -c backend -- curl -s localhost:8080/actuator/health
```

---

## 8. SỔ TAY BẪY (tất cả đã gặp THẬT ngày 2026-09-10 trên cụm myviettel)

**C1. Route cụm đội → Control Plane bị chặn — và chặn KHÔNG ĐỒNG NHẤT giữa
các node.**
- Triệu chứng: pod backend `0/1 Ready`, log sync lặp
  `ResourceAccessException: Request cancelled` mỗi 15s; curl từ 1 pod bất kỳ
  thì 200 nên tưởng đã thông.
- Gốc rễ thật: route myviettel → testlab-c2 (10.29.1.x) chỉ mở ở 1 số node
  (worker-...016/.17/.83: 5/5 lần 200; worker-...080/.91: timeout). Cùng 1
  node vẫn có pod 000 pod 200 → flaky, không thuần node-level. Pod bị chặn
  VẪN tới được pod IP nội cụm + NodePort node khác → chỉ chặn dải 10.29.1.x.
- **C1.1 Test điều kiện đầu vào**: bắt buộc curl tới CP từ pod TRƯỚC khi apply.
- **C1.2 Test theo TỪNG node** (1 lần 200 chưa đủ):
  ```bash
  kubectl get pods -n $NS -o wide | grep Running          # chọn 1 pod/node
  kubectl exec -n $NS <pod> -- sh -c \
    'for i in 1 2 3 4 5; do curl -s -o /dev/null -w "%{http_code} " --max-time 5 http://<CP-host>:<CP-port>/actuator/health; done; echo'
  ```
- **C1.3 Xử lý ngắn hạn**: nodeAffinity pin về node đã xác nhận 5/5 (mẫu trong
  `40-backend.yaml`). **Cần nhờ network mở route toàn bộ node rồi XÓA affinity**
  — nếu không, node đó down = pod không xếp được nơi khác.

**C2. Registry cụm pull được khác tên registry máy build push.**
`ImagePullBackOff` + event `lookup reg-hosted... i/o timeout` = cụm không phân
giải được tên đó. Xem deployment dịch vụ khác trong namespace đội để biết tên
registry cụm DÙNG THẬT, đổi `image:` theo. (Đã xác nhận thật: myviettel pull
được `reg-hosted-lab.kcntt.net`, trong khi testlab-c2 chỉ pull được
`registry-lab.kcntt.net` — 2 cụm khác nhau có thể cần 2 tên khác nhau.)

**C3. Docker Hub bị chặn DNS trong cụm product.**
`redis:7-alpine` → `ImagePullBackOff`, event `lookup registry-1.docker.io ...
i/o timeout`. Xử lý: tìm mirror trong registry nội bộ
(`curl -sk https://<REGISTRY>/v2/_catalog | grep redis`) — đã có sẵn
`bccs/redis:7-alpine`; nếu chưa có thì nhờ đội nền tảng push 1 bản.

**C4. Redis thiếu = readiness 503 (health fail-closed), và ErrImagePull redis
khiến backend đứng theo.**
Spring Boot health gồm Redis → DOWN → pod không bao giờ Ready. Lưu ý chuỗi
gây nhiễu: redis ErrImagePull khiến backend (đã lên) readiness 503 mãi —
đừng nhầm là lỗi backend. Fix image redis (C3) là backend tự Ready.

**C5. Firewall một chiều từ mạng App/Client tới node (NodePort).**
Ping node được ≠ TCP mở. Test từ máy App/Client thật trước khi bàn giao; test
trong cụm pass mà ngoài fail = firewall hạ tầng, không phải gateway.

**C6. Port Service backend thật không phải 8080.**
`baseHost` sai port → mọi request `GW-UPSTREAM-4XX/5XX` hoặc connect timeout.
Luôn `kubectl get svc -n $NS` (thật: product-catalog = 8001). DNS trong cùng
namespace dùng short-name được, nhưng ghi FQDN
`<svc>.<NS>.svc.cluster.local` an toàn hơn khi copy config giữa các môi trường.

**C7. Dò API backend thật bị catch-all đánh lừa.**
`/actuator/mappings` trả 404 `ENDPOINT_NOT_FOUND` (catch-all của chính BCCS
service chặn) — không phải service không có API. Dùng `/v3/api-docs` +
`/swagger-ui/index.html`. Kiểm chứng nhanh option set có data thật trước khi
khai báo endpoint demo (code tồn tại thật: `STATUS`, `TYPE`; nhiều code hợp
lệ về mặt nghiệp vụ nhưng DB không có → đừng chọn làm demo).

---

## 8.1. Kiểm soát vòng đời cấu hình resilience (định kỳ, không phải làm 1 lần)

P99 và rps đo **ban đầu** sẽ LỆCH dần khi: thêm composite mới trỏ vào cùng
upstream (rps cộng dồn), backend chậm dần theo data, traffic theo mùa. Cấu
hình resilience khóa theo **upstream** (không theo composite) và **đã đổi
được lúc chạy thật** — sync phát hiện config đổi → invalidate registry
Resilience4j trong ≤15s (đã xác nhận code + chạy thật 2026-09-11). Vì vậy
quy trình là **vòng lặp**: đo → so → sửa → đo lại.

Công thức chuẩn (mục 5) + 5 qui tắc tự động trong tool
`tools/audit_vs_config.py`:

| Qui tắc | Điều kiện báo | Ý nghĩa |
|---|---|---|
| R1 | `circuitBreakerEnabled=false` | vi phạm chuẩn — yêu cầu đội bật lại |
| R2 | P99 ≥ `readTimeoutMs` | timeout ảo — đo lại P99, tăng timeout hợp lý |
| R3 | bulkhead cần `P99 × rps × 1.2` lệch > 30% với đang set | trần quá thấp (đang tự chặn traffic) hoặc quá cao (không bảo vệ) |
| R4 | `retryEnabled=on` khi P99 > 2s | bão tải ×3 vào upstream đang chậm |
| R5 | Circuit breaker `OPEN` lúc chạy | upstream đang bị ngắt — kiểm tra ngay |

Chạy (Python 3.8+, không cần cài gì, CHỈ ĐỌC):

```bash
python3 tools/audit_vs_config.py \
  --es-url http://elasticsearch.<NS>.svc.cluster.local:9200 \
  --cp-url http://<CP-host>:<CP-port> \
  --team-key '<TEAM_KEY>' \
  --days 7 --ns $NS
# Exit 1 = có cảnh báo → hook được vào cron/CI để báo thức.
```

Đề xuất nhịp: **cron thứ 2 sáng hàng tuần** (tổng hợp 7 ngày), cộng thêm
chạy tay khi CB OPEN hoặc thêm composite lớn mới trỏ vào upstream đang tải
cao. Nguồn số liệu là chính audit ES — đó là lý do audit phải bật ở product.

---

## 9. Bản đồ cấu hình cuối cùng (để đối soát)

```
Namespace $NS (của đội):
  Secret    gwm-secret                CONTROL_PLANE_SYNC_API_KEY      (kubectl create secret — KHÔNG commit)
  Secret    gwm-tls (kubernetes.io/tls) tls.crt/tls.key — cert HTTPS sidecar (KHÔNG commit)
  ConfigMap gwm-config               TEAM_CODE, CONTROL_PLANE_BASE_URL, REDIS_*,
                                     GATEWAY_RATE_LIMIT_*, GATEWAY_AUDIT_*, APM_*
  ConfigMap gwm-tls-nginx-conf       nginx sidecar HTTPS 8443 → 127.0.0.1:8080
  Deploy    gwm-backend (data-plane, replicas=2, antiAffinity theo node)
              ├─ container backend    :8080 (HTTP nội bộ + probe k8s)
              ├─ container tls-proxy  :8443 (TLS termination, nginx sidecar)
              → Service gwm-backend :8080 + :8443 (ClusterIP)
  Deploy    gwm-redis    (data-plane-redis) → Service gwm-redis :6379
  Expose    gwm-backend-nodeport :8080 → nodePort <http>   (HTTP — chỉ dùng migrate/test)
                                  :8443 → nodePort <https>  (HTTPS — port CÔNG BỐ cho App/Client)
            (hoặc Ingress gwm-api.<domain> + tls: gwm-tls)

  KHÔNG có: Oracle, PersistenceVolume, UI — Data Plane hoàn toàn stateless
  (cache Redis mất là rebuild, cấu hình sống ở Control Plane trung tâm).

Control Plane trung tâm (đội nền tảng vận hành):
  1 dòng trong GWM_TEAM: team_code + api_key (key đội giữ trong Secret ở trên)
  Cấu hình đội: N Upstream + M Endpoint (khai qua UI/API bằng team key)
  Chu kỳ sync: Data Plane GET /api/config/export mỗi 15s, fail-open giữ cache cũ
```

Liên hệ đội nền tảng khi: cần tạo lại team key (đã mất), cần mở route network
cụm → Control Plane, cần build/push image version mới vào registry.
