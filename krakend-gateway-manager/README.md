# KrakenD Gateway Manager

Web UI quan tri cau hinh cho **KrakenD API Gateway (Community Edition)**, chay hoan
toan bang Docker. Cho phep nguoi dung khong can tu tay viet `krakend.json`:
tao endpoint, khai bao composite API (goi tuan tu nhieu backend + chain field
giua cac step), preview JSON truoc khi ap dung, va deploy (ghi file + reload
container KrakenD) chi bang 1 nut bam tren giao dien.

## 1. Kien truc tong the

```
                         ┌─────────────────────────────────────────────────┐
                         │                     Browser                     │
                         └───────────────────────┬───────────────────────┬─┘
                                                  │ HTTP (UI)             │ HTTP (JSON API client goi thu)
                                                  v                       v
┌───────────────────────────────────────┐   port 4200            ┌──────────────┐
│ frontend (Angular 18 + Angular         │◄─────────────────────  │  End users   │
│ Material, standalone components)       │                        └──────────────┘
│ - build production, serve boi Nginx    │
│ - Nginx proxy "/api/**" -> backend     │
└───────────────────┬────────────────────┘
                     │ proxy_pass /api/**
                     v
┌────────────────────────────────────────────────────────┐        ┌──────────────┐
│ backend (Spring Boot 3 - Control Plane API) :9000       │        │  postgres    │
│                                                          │  JDBC  │  :5432       │
│  EndpointController / ConfigController                  │───────►│  (JPA/       │
│         │                                                │        │  Hibernate)  │
│         v                                                │        └──────────────┘
│  EndpointService  ──►  EndpointConfigRepository (DB)     │
│         │                                                │
│         v                                                │
│  KrakendConfigGenerator                                  │
│  (Model DB -> JSON krakend.json chuan "version": 3)       │
│         │                                                │
│    ┌────┴─────┐                                          │
│    v          v                                          │
│ ConfigFile   DockerReloadService                          │
│ WriterService  (goi Docker Engine API qua                │
│ (ghi atomic)   Unix socket /var/run/docker.sock:          │
│    │           POST /containers/krakend-gateway/restart)  │
└────┼──────────────────────┬──────────────────────────────┘
     │ ghi file              │ HTTP qua unix socket
     v                       v
┌─────────────────────────────────────────┐
│ Docker volume dung chung: krakend-config │
│  /etc/krakend/krakend.json               │
└───────────────┬───────────────────────────┘
                 │ doc file khi khoi dong / restart
                 v
       ┌───────────────────────┐
       │ krakend (KrakenD CE)  │  <-- client goi API that su qua day, port 8080
       │  container: krakend-gateway
       └───────────────────────┘
```

### Co che trigger reload (quan trong)

KrakenD Community Edition **khong co hot-reload built-in** (khong tu dong theo
doi thay doi file cau hinh). Khi nguoi dung bam **Deploy** tren UI:

1. `ConfigController.deploy()` goi `KrakendConfigGenerator.generateFullConfig()`
   de gom TAT CA endpoint dang luu trong Postgres thanh 1 file `krakend.json`
   (chuan `"version": 3`).
2. `ConfigFileWriterService` ghi file nay ra volume Docker dung chung
   (`krakend-config`) - **ghi atomic** (viet ra `.tmp` roi `Files.move()` voi
   `ATOMIC_MOVE`) de tranh KrakenD doc phai file dang ghi do (partial JSON).
3. `DockerReloadService` goi thang **Docker Engine API** qua Unix domain
   socket `/var/run/docker.sock` (dung `java.nio.channels.SocketChannel` +
   `UnixDomainSocketAddress` co san tu Java 16+, **khong can cai Docker CLI**
   trong image backend) de goi `POST /containers/krakend-gateway/restart`.
4. Container `krakend-gateway` restart, doc lai file JSON moi nhat tu volume.

Vi KrakenD la **stateless**, restart chi mat khoang 1-2 giay. De dat
**zero-downtime that su** trong production, khuyen nghi: chay >= 2 replica
KrakenD phia sau 1 load balancer (hoac Docker Swarm/K8s rolling update) va
rolling-restart tung replica mot - Control Plane hien tai restart 1 container
duy nhat de giu vi du don gian, de trien khai bang `docker compose up -d`.

### Vi sao tach rieng "Control Plane" khoi KrakenD?

KrakenD CE chi doc file JSON tinh - khong co API de CRUD cau hinh. Control
Plane (Spring Boot) dong vai tro "nguon su that" (source of truth) luu trong
Postgres, va **generate** ra `krakend.json` moi khi deploy - dam bao file luon
duoc sinh nhat quan tu model, khong bao gio bi sua tay lech voi DB.

## 2. Cau truc thu muc

```
krakend-gateway-manager/
├── docker-compose.yml          # chay toan bo he thong
├── .env.example                 # bien moi truong (DB user/pass)
├── backend/                     # Control Plane API (Spring Boot)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/bccs/gatewaymanager/
│       ├── entity/               # EndpointConfig, BackendStep, FieldMapping
│       ├── dto/                  # Request/Response DTO
│       ├── repository/
│       ├── service/               # EndpointService, KrakendConfigGenerator,
│       │                          # ConfigFileWriterService, DockerReloadService
│       ├── controller/            # EndpointController, ConfigController
│       ├── exception/
│       └── config/                # CORS, DataSeeder (seed vi du mau)
├── frontend/                    # Web Management UI (Angular 18, standalone)
│   ├── Dockerfile / nginx.conf
│   └── src/app/
│       ├── models/endpoint.model.ts
│       ├── services/endpoint-api.service.ts
│       ├── pages/endpoint-list/    # danh sach + tim kiem + Deploy toan bo
│       ├── pages/endpoint-form/    # form tao/sua (steps, mapping, preview)
│       └── components/json-preview, preview-deploy-dialog
└── gateway/
    └── krakend.json              # seed bootstrap (chi dung lan dau khi volume con trong)
```

## 3. Chay he thong

```bash
cd krakend-gateway-manager
cp .env.example .env      # tuy chinh neu can
docker compose up -d --build
```

- Web UI: http://localhost:4200
- KrakenD Gateway (API that su): http://localhost:8080
- Control Plane API (Swagger khong bat kem, goi truc tiep REST): http://localhost:9000/api
- Postgres (debug ngoai): localhost:5433

Lan dau khoi dong, `DataSeeder` (backend) se tu dong tao san 1 endpoint mau
**GET /v1/user-orders** (composite, xem muc 5). Mo UI, vao **Endpoints**, bam
**Preview & Deploy toan bo** de ghi endpoint nay vao KrakenD lan dau (seed chi
tao ban ghi trong DB, KHONG tu dong deploy).

## 4. Model du lieu & logic sinh JSON

- `EndpointConfig` (1) → nhieu `BackendStep` (Step 1, Step 2, ...) + nhieu
  `FieldMapping` (khai bao chain field giua cac step).
- Neu `sequential = true` va co > 1 step, KrakenD goi backend **tuan tu**.
  **QUAN TRONG** (da xac nhan thuc te tren KrakenD v2.6.3, khong chi doc): co
  che nay PHAI duoc bat qua `"extra_config": {"proxy": {"sequential": true}}`
  tren endpoint - MOT FIELD TOP-LEVEL `"sequential": true` DON THUAN SE BI
  KrakenD AM THAM BO QUA (khong loi, nhung backend chay song song nhu binh
  thuong va cac placeholder `{respN_field}` khong bao gio duoc thay the, URL
  cuoi cung con nguyen dang Go-template chua render vi du `{{.Resp0_x}}`).
  `KrakendConfigGenerator` da tu dong ghi dung vi tri nay.
- Step sau tham chieu response step truoc qua placeholder **`{respN_field}`**
  (N = so thu tu step nguon, 0-based, field viet thuong, ho tro dot-notation
  cho object long nhau vi du `{resp0_user.hash}` - theo docs.krakend.io).
- `KrakendConfigGenerator` doc `FieldMapping` va:
  - `targetType = PATH`: thay the token `{tenParam}` co san trong `url_pattern`
    cua step dich bang `{respN_field}`.
  - `targetType = QUERY`: noi them `key={respN_field}` vao query string cua
    `url_pattern` step dich.
  - `targetType = HEADER`: **KHONG duoc ghi vao krakend.json** - KrakenD
    Community Edition khong ho tro native chain field vao header cua backend
    ke tiep (chi ho tro trong `url_pattern`). Generator tra ve **warning**
    thay vi fail, de nguoi dung biet va tu quyet dinh (can KrakenD Enterprise
    plugin Martian/Lua, hoac 1 middleware rieng).
- `target`: "boc vo" 1 field cua response truoc khi mapping/allow/deny/group va
  truoc khi step sau trich xuat qua `{respN_field}` - RAT QUAN TRONG voi cac
  API kieu BCCS `StandardResponse` (`{"code":...,"data":{...}}`): khai bao
  `target: "data"` de KrakenD chi giu lai noi dung `data` lam response goc.
- Loc/gop du lieu truoc khi tra ve client dung dung tinh nang co san cua
  KrakenD: `allow` (whitelist field), `deny` (blacklist field), `mapping`
  (doi ten field), `group` (dat response cua 1 step vao 1 key rieng de tranh
  dung do ten field khi merge nhieu step).

## 5. Vi du mau: `GET /v1/user-orders`

Composite API goi tuan tu **Auth Service** (lay `user_id`) roi **Order
Service** (dung `user_id` do lam query param `userId`).

### Request tao endpoint (POST /api/endpoints)

```json
{
  "name": "User Orders (composite)",
  "description": "Goi tuan tu Auth Service -> Order Service, truyen userId trich xuat tu response Auth sang query cua Order.",
  "path": "/v1/user-orders/{userId}",
  "method": "GET",
  "sequential": true,
  "outputEncoding": "json",
  "steps": [
    {
      "stepOrder": 1,
      "name": "Auth Service - lay thong tin user",
      "method": "GET",
      "urlPattern": "/api/v1/users/{userId}",
      "hosts": ["http://auth-service:8081"],
      "group": "auth",
      "allowFields": ["id", "name", "email"],
      "denyFields": [],
      "fieldRenameMapping": { "id": "user_id" }
    },
    {
      "stepOrder": 2,
      "name": "Order Service - lay danh sach don hang",
      "method": "GET",
      "urlPattern": "/api/v1/orders",
      "hosts": ["http://order-service:8082"],
      "allowFields": [],
      "denyFields": ["internal_debug_info"],
      "fieldRenameMapping": {}
    }
  ],
  "mappings": [
    {
      "sourceStepOrder": 1,
      "sourceField": "user_id",
      "targetStepOrder": 2,
      "targetType": "QUERY",
      "targetParamName": "userId"
    }
  ]
}
```

Luu y quan trong: `{userId}` trong `url_pattern` cua Step 1 PHAI trung ten voi
mot path-param da khai bao tren CHINH endpoint gateway (`path` = "/v1/user-
orders/{userId}"). Neu path endpoint khong co `{userId}`, KrakenD se tu choi
parse config voi loi `undefined output param 'userId'` - day la co che KrakenD
dung de biet token nao duoc client truyen vao qua URL (forward tu dong sang
backend dau tien) khac voi token `{respN_field}` (chi ap dung cho backend thu 2
tro di, lay tu response backend truoc).

Client goi gateway bang: `GET http://localhost:8080/v1/user-orders/42`

### JSON duoc `KrakendConfigGenerator` sinh ra (preview / sau khi deploy)

```json
{
  "endpoint": "/v1/user-orders/{userId}",
  "method": "GET",
  "output_encoding": "json",
  "extra_config": {
    "proxy": { "sequential": true }
  },
  "backend": [
    {
      "url_pattern": "/api/v1/users/{userId}",
      "method": "GET",
      "encoding": "json",
      "host": ["http://auth-service:8081"],
      "group": "auth",
      "allow": ["id", "name", "email"],
      "mapping": { "id": "user_id" }
    },
    {
      "url_pattern": "/api/v1/orders?userId={resp0_user_id}",
      "method": "GET",
      "encoding": "json",
      "host": ["http://order-service:8082"],
      "deny": ["internal_debug_info"]
    }
  ]
}
```

Giai thich:
- `extra_config.proxy.sequential: true` → KrakenD goi Step 1 truoc, cho phan
  hoi (**bat buoc phai nam trong `extra_config`**, xem canh bao o muc 4 -
  1 field `"sequential": true` o top-level se bi KrakenD am tham bo qua).
- Step 1 tra ve field `id`, duoc **rename** thanh `user_id` (`mapping`) va
  chi giu 3 field trong `allow`.
- Step 2 tham chieu `resp0_user_id` (resp**0** = step dau tien, 0-based) trong
  query string → KrakenD tu dong thay bang gia tri `user_id` lay tu response
  Step 1 truoc khi goi Order Service.
- Response cuoi cung tra ve client la ket qua **merge** cua ca 2 step (field
  cua Step 1 nam trong key `"auth"` nho `group`, field cua Step 2 nam o root,
  da loai bo `internal_debug_info`).

## 6. Vi du thuc te da kiem chung: BCCS `organization-resource-service`

Composite API goi tuan tu 2 API THAT cua `organization-resource-service`
(chay local tren `host.docker.internal:8004`, response deu boc trong
`StandardResponse{ code, data, ... }`):

1. `GET /organization-resource-service/v1/staff/getActiveById/{staffId}` →
   lay `data.staffCode`.
2. `GET /organization-resource-service/v1/staff/getStaffShopFullInfo/{staffCode}`
   → tra ve full thong tin staff + shop (ket qua cuoi cung).

Request tao endpoint (rut gon phan quan trong):

```json
{
  "path": "/v1/staff/full-info/{staffId}",
  "method": "GET",
  "sequential": true,
  "steps": [
    {
      "stepOrder": 1,
      "urlPattern": "/organization-resource-service/v1/staff/getActiveById/{staffId}",
      "hosts": ["http://host.docker.internal:8004"],
      "target": "data",
      "allowFields": ["staffCode"]
    },
    {
      "stepOrder": 2,
      "urlPattern": "/organization-resource-service/v1/staff/getStaffShopFullInfo/{staffCode}",
      "hosts": ["http://host.docker.internal:8004"],
      "target": "data"
    }
  ],
  "mappings": [
    { "sourceStepOrder": 1, "sourceField": "staffCode", "targetStepOrder": 2,
      "targetType": "PATH", "targetParamName": "staffCode" }
  ]
}
```

`target: "data"` tren CA HAI step la diem mau chot: neu khong "boc vo", field
`staffCode` KHONG the trich xuat duoc (no nam trong `data.staffCode`, khong
phai o root cua response Step 1), va response cuoi cung tra ve client se van
con nguyen wrapper `{code, message, data, ...}` thay vi du lieu that.

Test qua gateway (da chay va xac nhan tra ve 200 voi day du staff + shop):

```bash
curl -s http://localhost:8080/v1/staff/full-info/102137 | jq
```

## 7. Gioi han da biet (Community Edition)

- Khong ho tro hot-reload - Control Plane phai restart container KrakenD.
- Chain field vao **header** cua step ke tiep khong duoc ho tro native - can
  KrakenD Enterprise (plugin Martian/Lua) hoac middleware rieng.
- Cac API kieu BCCS `StandardResponse` (boc du lieu trong `data`) BAT BUOC
  phai khai bao `target: "data"` tren tung step, neu khong `{respN_field}`
  se khong tim thay field can chain.
- Auth/rate-limit/circuit-breaker chua co UI cau hinh (co the mo rong `extra_config`
  trong `KrakendConfigGenerator` sau nay).
