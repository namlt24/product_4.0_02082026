# Bo manifest Kubernetes - Gateway Manager

YAML thuan (khong Helm), ap dung bang `kubectl apply`. Xem huong dan day du
tai `../DEPLOYMENT_GUIDE.md`. Tom tat thu tu:

1. Sua gia tri trong `00-configmap.yaml` cho dung ha tang cua doi.
2. Tao Secret that (KHONG dung `01-secret.yaml.example` truc tiep):
   ```
   kubectl create secret generic gwm-secret -n <namespace-cua-doi> \
     --from-literal=DB_USER='...' \
     --from-literal=DB_PASSWORD='...' \
     --from-literal=GATEWAY_ADMIN_API_KEY='...'
   ```
3. Sua `image:` trong `40-backend.yaml` va `50-frontend.yaml` thanh dung
   registry/tag noi bo cua doi.
4. Sua 2 `host:` trong `60-ingress.yaml` thanh dung domain noi bo cua doi.
5. Apply toan bo (thu tu ten file da dam bao dung thu tu phu thuoc):
   ```
   kubectl apply -f 00-configmap.yaml -n <namespace-cua-doi>
   kubectl apply -f 30-redis.yaml -n <namespace-cua-doi>
   kubectl apply -f 40-backend.yaml -n <namespace-cua-doi>
   kubectl apply -f 50-frontend.yaml -n <namespace-cua-doi>
   kubectl apply -f 60-ingress.yaml -n <namespace-cua-doi>
   ```
   (hoac gon hon: `kubectl apply -f . -n <namespace-cua-doi>` sau khi da tao
   Secret o buoc 2 - Secret KHONG nam trong thu muc nay o dang that.)

## Gia dinh ve ha tang

- **Oracle 19c+**: KHONG chay trong cum k8s nay - `DB_HOST` trong ConfigMap
  tro toi Oracle co san cua doi (trong mang noi bo hoac qua 1
  `ExternalName` Service, ngoai pham vi bo manifest nay). Schema da duoc DBA
  tao san qua ban giao DDL, xem `../backend/src/main/resources/db/team-schema/`
  va `../DEPLOYMENT_GUIDE.md` muc 2 - **KHONG co migration/init-job nao chay
  tu dong trong cum**.
- **Redis**: chay ngay trong cum (`30-redis.yaml`), KHONG can PersistentVolume
  (cache-aside + bo dem rate-limit, ca 2 deu fail-open - xem
  `GatewayCacheService`/`RateLimitService`) - mat du lieu Redis khi Pod restart
  KHONG lam gian doan traffic that, chi lam nguoi/rong cache tam thoi.
- **Elasticsearch/APM**: tuy chon, tro qua ConfigMap - dat
  `GATEWAY_AUDIT_ENABLED=false` neu doi chua co Elasticsearch rieng.
- **Ingress**: gia dinh da co san NGINX Ingress Controller trong cum (cai dat
  no khong nam trong pham vi bo manifest nay - xem huong dan chinh thuc cua
  NGINX Ingress Controller cho cach cai vao cum cua doi).
