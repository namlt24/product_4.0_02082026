# Control Plane - 1 bo manifest DUY NHAT, trien khai 1 LAN, dung chung MOI doi

Do **doi nen tang** (chung ta) trien khai va van hanh - KHONG phai tung doi
BCCS tu trien khai (xem `../data-plane/README.md` cho phan cua tung doi).
Gom: UI Angular + backend `SPRING_PROFILES_ACTIVE=control-plane` (CRUD
Endpoint/Upstream/Team, doc/ghi truc tiep Oracle trung tam). Xem huong dan
day du tai `../../DEPLOYMENT_GUIDE.md`.

## Thu tu apply

1. Sua gia tri trong `00-configmap.yaml` cho dung Oracle trung tam + ES/APM
   (neu co).
2. Tao Secret that (KHONG dung `01-secret.yaml.example` truc tiep):
   ```
   kubectl create secret generic gwm-secret -n <namespace-control-plane> \
     --from-literal=DB_USER='...' \
     --from-literal=DB_PASSWORD='...' \
     --from-literal=GATEWAY_ADMIN_API_KEY='...'
   ```
3. Sua `image:` trong `40-backend.yaml` va `50-frontend.yaml`.
4. Sua 2 `host:` trong `60-ingress.yaml`.
5. Apply:
   ```
   kubectl apply -f . -n <namespace-control-plane>
   ```
   (sau khi da tao Secret o buoc 2 - Secret KHONG nam trong thu muc nay o
   dang that).

## Sau khi len - buoc BAT BUOC truoc khi bat ky doi nao dung duoc

Truy cap UI (`gwm-ui.<domain>`), dang nhap bang platform-admin key
(`GATEWAY_ADMIN_API_KEY` vua tao) vao man hinh **"Quan ly doi"**, tao 1 dong
cho MOI doi BCCS se dung Gateway Manager (moi dong sinh ra 1 `team_code` +
`api_key` rieng, hien **DUNG 1 LAN** - luu lai ngay) - gui 2 gia tri nay cho
doi do de ho dien vao `../data-plane/`.

## Gia dinh ve ha tang

- **Oracle 19c+**: KHONG chay trong cum k8s nay - trung tam, dung chung cho
  MOI doi. Schema da duoc DBA cua doi nen tang tao san qua ban giao DDL, xem
  `../../backend/src/main/resources/db/team-schema/` (V1__baseline.sql +
  V2__team_code.sql) va `../../DEPLOYMENT_GUIDE.md`.
- **Redis**: TUY CHON, chi phuc vu cache tam khi dung "Thu ngay"/"Thu nhanh"
  tren UI - fail-open, khong co van chay binh thuong.
- **Ingress**: gia dinh da co san NGINX Ingress Controller trong cum.
