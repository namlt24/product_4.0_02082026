# Control Plane Production — bộ manifest chuẩn production

Bộ manifest NÀY là bản chuẩn production cho Control Plane, tách khỏi bộ ở
thư mục trên (`../` — bản dùng cho thực hành/lab, giữ lại để luyện tập).

Control Plane là nguồn cấu hình DUY NHẤT của MỌI Data Plane (mỗi DP sync về
mỗi 15s) — downtime CP không làm mất traffic thật (DP fail-open giữ cache cũ),
nhưng không đội nào thêm/sửa được endpoint. Vì vậy bản production thêm: 2
replicas + anti-affinity + PDB.

Khác biệt so với bộ lab:

| Hạng mục | Lab (`../`) | Production (thư mục này) |
|---|---|---|
| Replicas backend | 1 | 2 (pin 2 node .14/.15, anti-affinity 1 pod/node) |
| PDB | không có | minAvailable 1 |
| Rollout | mặc định | maxUnavailable 1 / maxSurge 0 (anti-affinity) |
| Image | 2.0.0-cp.1 | **2.0.0-cp.5** (ETag/304 sync + HttpClient5 — đồng bộ DP) |
| Redis/HPA resources | giữ nguyên | giữ nguyên (CP tải nhẹ — chỉ CRUD + sync ~1.3 rps/20 đội) |

Giữ nguyên từ bản lab (đã đúng production):
- startupProbe 60×10s ≈ 10 phút — Hibernate validate Oracle qua WAN mất ~6.5 phút (bẫy B5).
- readiness dùng `/actuator/health` tổng hợp (fail-closed với Oracle — đúng cho CP).
- Secret tạo bằng `kubectl create secret`, KHÔNG qua file YAML.

## Danh sách file

- `00-configmap.yaml` — DB170, Redis, audit, APM (1 bản duy nhất cho nền tảng)
- `01-secret.yaml.example` — MẪU tham khảo, KHÔNG apply. Secret tạo bằng kubectl
- `30-redis.yaml` — Redis CP (bắt buộc: health fail-closed)
- `40-backend.yaml` — Deployment 2 replicas + Service + PDB
- `50-frontend.yaml` — UI Angular (1 replica — UI mất vài phút không ảnh hưởng vận hành)
- `60-ingress.yaml` — 2 NodePort: UI 8837, API 8051
- `HƯỚNG_DẪN_PRODUCTION.md` — trình tự deploy từ đầu đến khi bàn giao đội đầu tiên

## Quy tắc khi dùng bộ này

1. **ĐỌC `HƯỚNG_DẪN_PRODUCTION.md` trước** — làm đúng thứ tự bước.
2. Secret (DB password, platform-admin key) KHÔNG BAO GIỜ nằm trong file yaml.
3. Đây là bản DUY NHẤT của nền tảng — các đội Data Plane KHÔNG copy bộ này;
   đội dùng `../data-plane/production/`.
4. Sau bất kỳ thay đổi nào lên ConfigMap/Secret: `kubectl rollout restart
   deploy/gwm-backend` (env không tự reload).
