# Data Plane Production — bộ manifest chuẩn production (sizing 1000 RPS)

Bộ manifest NÀY là bản chuẩn production cho Data Plane, tách khỏi bộ ở thư mục
trên (`../` — bản dùng cho thực hành/lab, giữ lại để luyện tập không đụng vào).

Khác biệt so với bộ lab:

| Hạng mục | Lab (`../`) | Production (thư mục này) |
|---|---|---|
| Số replicas | 2 | 3 (pin 3 node, anti-affinity 1 pod/node) |
| Backend resources | 250m/512Mi → 1CPU/1Gi | 1CPU/1Gi request → 2CPU/2.5Gi limit |
| Tomcat threads | 200 (mặc định) | 500 (Little's Law: 333 rps × mean 1.5s) |
| JVM heap | 25% limit (mặc định) | 55% của 2.5Gi (~1.4Gi) |
| Rate limit | 300/60s per IP | 3000/60s per IP |
| HTTP connection pool | không có (JDK không keep-alive) | HttpClient5 pool 600/550 (env chỉnh được) |
| Redis | 250m/256Mi limit | 1 CPU/512Mi limit (≥2000 ops/s) |
| PDB | không có | minAvailable 2 |
| Rollout | maxUnavailable 0 | maxUnavailable 1 / maxSurge 0 (anti-affinity) |
| startupProbe | không có | 36×5s = 3 phút |

## Danh sách file

- `00-configmap.yaml` — cấu hình team, Control Plane, rate limit, connection pool
- `01-secret.yaml.example` — MẪU tham khảo, KHÔNG apply. Secret tạo bằng kubectl
- `30-redis.yaml` — Redis cho cache-aside + rate limit
- `40-backend.yaml` — Deployment (backend + nginx TLS sidecar) + Service + PDB
- `60-ingress.yaml` — NodePort cho client gọi vào (HTTP 8052 + HTTPS 8053)
- `HƯỚNG_DẪN_PRODUCTION.md` — trình tự deploy từ đầu đến khi nghiệm thu

## Quy tắc khi dùng bộ này

1. **ĐỌC `HƯỚNG_DẪN_PRODUCTION.md` trước** — làm đúng thứ tự bước.
2. Giá trị cần sửa theo đội nằm ở `00-configmap.yaml` (3 biến: TEAM_CODE,
   CONTROL_PLANE_BASE_URL, APM_ENVIRONMENT) — mỗi đội 1 bản riêng, không
   dùng chung.
3. Secret (team key, TLS) KHÔNG BAO GIỜ nằm trong file yaml — tạo bằng
   `kubectl create secret` (xem HƯỚNG_DẪN_PRODUCTION.md bước 3).
4. Scale lên 2000 RPS sau này: KHÔNG phải sửa code — chỉ đổi (a) replicas
   3→6, (b) GATEWAY_HTTP_POOL_MAX_TOTAL 600→1200, (c) rate limit tăng theo,
   (d) bulkhead từng upstream trong UI Control Plane. Chi tiết ở mục 8 của
   hướng dẫn.
