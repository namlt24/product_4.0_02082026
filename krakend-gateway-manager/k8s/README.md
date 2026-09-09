# Bo manifest Kubernetes - Gateway Manager

YAML thuan (khong Helm), ap dung bang `kubectl apply`. Tach thanh **2 bo
RIENG BIET** tu 2026-09 (xem `SAD-Gateway-Manager.md` ADR-07) - khac han
truoc do (moi doi 1 bo day du, tu than day du CRUD+traffic):

| | `control-plane/` | `data-plane/` |
|---|---|---|
| Ai trien khai | Doi NEN TANG - **1 lan duy nhat** | **MOI doi BCCS** - tu trien khai rieng |
| Gom | UI Angular + backend (CRUD Endpoint/Upstream/Team) | Backend (thuc thi traffic that) + Redis |
| Oracle | Ket noi truc tiep (dung chung MOI doi) | KHONG can - dong bo qua HTTP |
| Redis | Tuy chon (chi phuc vu "Thu ngay") | Bat buoc (cache-aside, khong PersistentVolume) |

`SPRING_PROFILES_ACTIVE` chon vai tro cho **cung 1 image** (xem
`backend/src/main/resources/application-control-plane.yml`/
`application-data-plane.yml`) - khong phai 2 anh Docker khac nhau.

**Thu tu trien khai bat buoc**: `control-plane/` phai len TRUOC va co it
nhat 1 doi da duoc tao qua man hinh "Quan ly doi" (can `team_code`+`api_key`
truoc khi apply duoc `data-plane/`). Xem huong dan chi tiet tung buoc trong
`control-plane/README.md` va `data-plane/README.md`, hoac tong quan ca quy
trinh trong `../DEPLOYMENT_GUIDE.md`.
