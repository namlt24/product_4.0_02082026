# Data Plane - MOI DOI BCCS tu trien khai 1 ban RIENG cua chinh minh

Khac han `../control-plane/` (do doi nen tang trien khai 1 lan duy nhat, dung
chung MOI doi) - thu muc nay la **TEMPLATE**, moi doi copy/apply RIENG vao
namespace cua chinh minh. Xem huong dan day du tai `../../DEPLOYMENT_GUIDE.md`.

## Dieu kien tien quyet

Da co `team_code` + `api_key` do **doi nen tang** cap qua man hinh "Quan ly
doi" tren UI Control Plane trung tam (xem `../control-plane/README.md`) - can
2 gia tri nay TRUOC khi lam cac buoc duoi day.

## Thu tu apply

1. Sua gia tri trong `00-configmap.yaml` (dac biet `TEAM_CODE`,
   `CONTROL_PLANE_BASE_URL`) cho dung.
2. Tao Secret that (KHONG dung `01-secret.yaml.example` truc tiep):
   ```
   kubectl create secret generic gwm-secret -n <namespace-cua-doi> \
     --from-literal=CONTROL_PLANE_SYNC_API_KEY='<api_key-doi-nen-tang-da-cap>'
   ```
3. Sua `image:` trong `40-backend.yaml`.
4. Sua `host:` trong `60-ingress.yaml`.
5. Apply:
   ```
   kubectl apply -f . -n <namespace-cua-doi>
   ```
   (sau khi da tao Secret o buoc 2 - Secret KHONG nam trong thu muc nay o
   dang that).
6. Truy cap UI Control Plane trung tam de khai bao Upstream/Endpoint cho doi
   minh (Data Plane KHONG co man hinh quan tri nao - chi thuc thi traffic).

## Gia dinh ve ha tang

- **Oracle**: KHONG can, KHONG chay o day - Data Plane khong con ket noi DB
  nao ca, tu dong bo cau hinh qua HTTP (xem RemoteConfigSyncService).
- **Redis**: chay ngay trong cum (`30-redis.yaml`), KHONG can PersistentVolume
  (cache-aside + bo dem rate-limit deu fail-open).
- **Elasticsearch/APM**: tuy chon, tro qua ConfigMap - dat
  `GATEWAY_AUDIT_ENABLED=false` neu doi chua co Elasticsearch rieng.
- **Ingress**: gia dinh da co san NGINX Ingress Controller trong cum cua doi.

## Kiem tra sau khi len

```
kubectl get pods -n <namespace-cua-doi>              # backend + redis phai Running
kubectl logs -n <namespace-cua-doi> deploy/gwm-backend | grep "Da dong bo"
```
Neu chua thay dong log "Da dong bo cau hinh tu Control Plane" sau ~15-30s,
kiem tra lai `CONTROL_PLANE_BASE_URL`/`CONTROL_PLANE_SYNC_API_KEY` - xem muc
"Su co thuong gap" trong `../../DEPLOYMENT_GUIDE.md`.
