# Hướng dẫn setup môi trường cho máy ngoài (không nằm trong mạng nội bộ Viettel)

`SETUP-GUIDE.md` được viết cho máy nằm trong mạng nội bộ Viettel (có GitLab `10.255.60.6`, Nexus `nexus.kcntt.net`, Oracle/Redis/Kafka nội bộ). Tài liệu này dành cho **máy ngoài** — không truy cập được các hệ thống nội bộ đó — chỉ có Internet công khai. Toàn bộ các bước dưới đây đã được thực hiện và xác minh chạy được (5/6 service, xem mục 9).

---

## Mục lục

1. [Khác biệt so với máy nội bộ](#1-khác-biệt-so-với-máy-nội-bộ)
2. [Yêu cầu hệ thống](#2-yêu-cầu-hệ-thống)
3. [Lấy source code](#3-lấy-source-code)
4. [Cài JDK 25](#4-cài-jdk-25)
5. [Lấy dependency nội bộ com.viettel.bccs cho Maven](#5-lấy-dependency-nội-bộ-comviettelbccs-cho-maven)
6. [Hạ tầng Docker dùng chung (Oracle + Elasticsearch)](#6-hạ-tầng-docker-dùng-chung-oracle--elasticsearch)
7. [Trỏ datasource của từng service về local](#7-trỏ-datasource-của-từng-service-về-local)
8. [Build & chạy service](#8-build--chạy-service)
9. [Kiểm tra đã chạy đúng chưa](#9-kiểm-tra-đã-chạy-đúng-chưa)
10. [Redis / Kafka — không bắt buộc phải có](#10-redis--kafka--không-bắt-buộc-phải-có)
11. [Xử lý lỗi thường gặp riêng cho máy ngoài](#11-xử-lý-lỗi-thường-gặp-riêng-cho-máy-ngoài)

---

## 1. Khác biệt so với máy nội bộ

| | Máy nội bộ (`SETUP-GUIDE.md`) | Máy ngoài (tài liệu này) |
|---|---|---|
| Git source | Clone qua GitLab nội bộ `10.255.60.6` | Copy/kéo code sẵn có từ máy nội bộ ra (không có GitLab nội bộ để clone) |
| Maven mirror | Bắt buộc trỏ Nexus `nexus.kcntt.net` | **Không cần** — Maven resolve thẳng Maven Central qua Internet công khai |
| Dependency `com.viettel.bccs:*` | Tải từ Nexus | Nexus không tới được → phải copy sẵn từ `~/.m2/repository/com/viettel/bccs/` của máy nội bộ sang |
| Oracle DB | Dùng cụm Oracle nội bộ `10.207.222.170:1521:DB170` | Dựng Oracle **local bằng Docker** (`db-local/docker-compose.yml`), 1 container dùng chung cho cả 5 service |
| Elasticsearch | Chưa có cụm thật | Dựng ES **local bằng Docker** (cùng file compose), phục vụ cache `MAP_ACTIVE_INFO` |
| Redis / Kafka | Cụm nội bộ, hoạt động | Không tới được từ máy ngoài — **không sao**, service vẫn chạy được (xem mục 10) |

**Đừng làm theo mục 2 và mục 4 của `SETUP-GUIDE.md`** (git credentials nội bộ, Nexus mirror) — không áp dụng được và không cần thiết trên máy ngoài.

---

## 2. Yêu cầu hệ thống

| Phần mềm | Phiên bản | Ghi chú |
|---|---|---|
| JDK | 25+ | Xem mục 4 nếu máy chưa có sẵn |
| Maven | dùng Maven Wrapper (`mvnw`/`mvnw.cmd`) đi kèm mỗi service | Không cần cài Maven riêng |
| Docker Desktop | mới nhất, bật WSL2 (Windows) | Chạy Oracle + Elasticsearch local |
| Internet công khai | bắt buộc | Để Maven tải Spring Boot/Kafka client/... từ Maven Central |

---

## 3. Lấy source code

Máy ngoài không truy cập được GitLab nội bộ `10.255.60.6`, nên không clone trực tiếp được. Cách đã dùng: **copy toàn bộ thư mục code** (5 service + `db-local/` + tài liệu gốc) từ máy nội bộ sang máy ngoài qua ổ USB/mạng chia sẻ/v.v., giữ nguyên cấu trúc thư mục cha:

```
product_4.0_new/
├── product-catalog-service/
├── product-policy-service/
├── product-area-service/
├── product-price-service/
├── organization-resource-service/
└── db-local/
```

Lưu ý: `spec-common-service` hiện chưa có trong bộ copy này (chưa được tạo/scaffold) — chỉ 5 service trên đang hoạt động.

Nếu muốn quản lý bằng git riêng trên máy ngoài (không liên quan gì tới GitLab nội bộ), có thể `git init` một repo mới tại thư mục gốc và tự chọn remote (xem `.gitignore` đã có sẵn ở gốc repo loại trừ `target/`, `.idea/`, `.env`, v.v.).

---

## 4. Cài JDK 25

Dùng bản portable (không cần quyền admin để cài), ví dụ Eclipse Temurin:

1. Tải JDK 25 (ví dụ `jdk-25.0.3+9`) bản zip/portable cho Windows, giải nén ra một thư mục bất kỳ (VD: `C:\Users\<user>\Downloads\jdk-25.0.3+9`).
2. Set biến môi trường ở **User scope** (không cần đổi System scope, tránh ảnh hưởng JDK khác đang cài máy):
   - `JAVA_HOME` = đường dẫn thư mục JDK vừa giải nén
   - Thêm `%JAVA_HOME%\bin` vào đầu `PATH`
3. Mở **terminal mới** rồi kiểm tra:
   ```powershell
   java -version
   ```
   Nếu terminal đang mở từ trước lúc set biến môi trường, nó sẽ không thấy JDK mới — phải mở terminal mới, hoặc set tạm trong phiên hiện tại:
   ```powershell
   $env:JAVA_HOME = "C:\Users\<user>\Downloads\jdk-25.0.3+9"
   $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
   ```

---

## 5. Lấy dependency nội bộ `com.viettel.bccs` cho Maven

Đây là bước **quan trọng nhất và khác biệt lớn nhất** so với máy nội bộ. Các artifact `com.viettel.bccs:bccs-build-parent`, `bccs-starter-service-base`, `bccs-starter-data`, `bccs-starter-kafka`, `bccs-starter-cache`, `bccs-starter-redis`, v.v. **không có trên Maven Central** — chúng chỉ tồn tại trên Nexus nội bộ Viettel, mà máy ngoài không tới được.

Cách xử lý: copy sẵn các artifact này từ `~/.m2/repository/` của một máy **có** quyền truy cập Nexus:

1. Trên máy nội bộ, xác định thư mục:
   ```
   ~/.m2/repository/com/viettel/bccs/
   ```
2. Copy toàn bộ thư mục con này (giữ nguyên cấu trúc `groupId/artifactId/version`) sang máy ngoài, đặt vào đúng vị trí:
   ```
   C:\Users\<user>\.m2\repository\com\viettel\bccs\
   ```
3. **Không cần** cấu hình Nexus mirror trong `~/.m2/settings.xml` — để trống hoặc dùng mirror mặc định của Maven Central. Nếu file `settings.xml` đã có sẵn mirror trỏ Nexus nội bộ (`nexus.kcntt.net`) từ trước, hãy xoá/comment lại — bản thân Nexus đó cũng không tới được từ máy ngoài, để nguyên sẽ khiến toàn bộ dependency khác (Spring Boot, Kafka...) build lỗi timeout thay vì rơi về Maven Central.
4. Mọi dependency khác (Spring Boot, Spring Kafka, Micrometer, OpenTelemetry, ...) sẽ tự tải từ Maven Central qua Internet công khai như bình thường — không cần thao tác gì thêm.

**Nếu về sau `bccs-platform` có version mới**: phải lặp lại bước copy `.m2` này từ máy nội bộ, không có cách nào tự động hoá được nếu máy ngoài không có đường ra Nexus.

---

## 6. Hạ tầng Docker dùng chung (Oracle + Elasticsearch)

Máy ngoài không có Oracle/Elasticsearch nội bộ để trỏ vào, nên dựng **1 bộ hạ tầng Docker dùng chung cho cả 5 service** tại `db-local/docker-compose.yml` (khác với `docker-compose.local.yml` riêng của từng service, vốn dựng Oracle/Redis/Kafka/Kafka UI theo kiểu máy nội bộ — **không dùng file đó trên máy ngoài**).

```powershell
cd "product_4.0_new"
docker compose -f db-local/docker-compose.yml up -d
```

File này chạy 2 container:

| Container | Image | Port | Ghi chú |
|---|---|---|---|
| `bccs-oracle` | `gvenzl/oracle-free:23-slim` | 1521 | schema/user `BCCS_PRODUCT` / `BCCS_PRODUCT123`, PDB `FREEPDB1`. Schema + dữ liệu mẫu tự chạy lần đầu từ `db-local/init/01_schema.sql` và `02_sample_data.sql` |
| `bccs-elasticsearch` | `docker.elastic.co/elasticsearch/elasticsearch:8.15.3` | 9200 | single-node, không bật security. Phục vụ cache `MAP_ACTIVE_INFO` của `product-policy-service` |

**Lưu ý quan trọng về image Elasticsearch**: phải dùng đúng `8.15.3` — bản `8.4.3` (dù đúng version client Java đang khai trong `pom.xml`) bị crash lúc khởi động trên môi trường Docker Desktop/WSL2 (`NullPointerException` trong `CgroupV2Subsystem`, lỗi tương thích JDK/cgroup-v2 đã biết). Client Java 8.4.3 vẫn gọi được server 8.15.3 bình thường (Elastic client forward-compatible), nên không cần đổi version dependency trong `pom.xml`.

Oracle lần đầu khởi động mất 2-3 phút. Kiểm tra:

```powershell
docker compose -f db-local/docker-compose.yml ps
curl http://localhost:9200/_cluster/health
```

Dữ liệu Oracle/ES được lưu ở Docker volume (`bccs_oracle_local_data`, `bccs_es_local_data`) — không mất khi `docker compose down` (không có `-v`). Chỉ dùng `down -v` khi thực sự muốn xoá sạch và re-init từ đầu.

---

## 7. Trỏ datasource của từng service về local

Mỗi service có `src/main/resources/application-local.yml`. Trên máy ngoài, sửa `spring.datasource.url` của **cả 5 service** thành:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521/FREEPDB1
    username: BCCS_PRODUCT
    password: BCCS_PRODUCT123
```

(thay vì địa chỉ Oracle nội bộ `10.207.222.170:1521:DB170` mà file này mang theo mặc định khi copy từ máy nội bộ sang). Đây là điểm hay bị "trôi" lại về địa chỉ nội bộ nếu code được đồng bộ lại từ máy nội bộ — luôn kiểm tra lại giá trị này sau mỗi lần đồng bộ code.

Riêng `product-policy-service` cần thêm cấu hình Elasticsearch trong `application-local.yml` (đã có sẵn nếu copy đúng code đã tích hợp ES):

```yaml
app:
  elasticsearch:
    hosts: [localhost:9200]
  mapactiveinfo:
    elasticsearch-cache-enabled: true
```

---

## 8. Build & chạy service

Không cần Docker riêng của từng service (`docker-compose.local.yml`/`.env.example` — bỏ qua trên máy ngoài, chỉ dùng `db-local/docker-compose.yml` ở mục 6).

```powershell
cd product-catalog-service
.\mvnw.cmd clean install -DskipTests
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Lặp lại cho từng service (mỗi service một terminal riêng), theo đúng thứ tự port cố định sẵn trong `application.yml`:

| Service | Port |
|---|---|
| product-catalog-service | 8001 |
| product-policy-service | 8002 |
| product-area-service | 8003 |
| organization-resource-service | 8004 |
| product-price-service | 8005 |

Không cần đổi port thủ công như hướng dẫn trong `SETUP-GUIDE.md` (mục 7.3) — 5 service này đã được cấu hình port riêng biệt sẵn.

Có thể chạy từ IntelliJ IDEA thay vì terminal — chỉ cần đảm bảo JDK 25 được chọn làm Project SDK và VM/Program option có `-Dspring-boot.run.profiles=local`.

---

## 9. Kiểm tra đã chạy đúng chưa

Gọi thử một endpoint thật có dữ liệu mẫu, không chỉ xem log "Started ... Application":

```powershell
curl http://localhost:8004/v1/shop/getActiveById/100001
curl http://localhost:8001/v1/product/getByProductCode?productCode=MIMAX100
curl http://localhost:8002/v1/map-active-info/findById/1000001
curl http://localhost:8003/v1/area/getByAreaCode/01
curl http://localhost:8005/actuator/health
```

`product-price-service` hiện chưa có nghiệp vụ nào được build (chỉ còn `/actuator/health` trả `db: UP` là đủ xác nhận kết nối DB đúng).

---

## 10. Redis / Kafka — không bắt buộc phải có

`application-local.yml` mặc định (copy từ máy nội bộ) vẫn trỏ Redis/Kafka về địa chỉ nội bộ (`10.58.71.18x:838x`, `10.207.252.169:8859`) — máy ngoài **không** kết nối được tới các địa chỉ này. Đây **không phải lỗi chặn khởi động**: Spring context vẫn start đầy đủ, app vẫn phục vụ request bình thường, chỉ có `/actuator/health` báo Redis là `DOWN`.

Nếu muốn tắt hẳn cảnh báo/kết nối thử tới Redis/Kafka, chạy với override:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--bccs.kafka.enabled=false --bccs.cache.mode=memory-only"
```

---

## 11. Xử lý lỗi thường gặp riêng cho máy ngoài

**"Could not resolve dependencies for project com.viettel.bccs:..."**
Không phải do thiếu Nexus mirror (máy ngoài không có Nexus để trỏ tới). Nguyên nhân thật: thiếu artifact `com.viettel.bccs:*` trong `~/.m2/repository` local. Quay lại [mục 5](#5-lấy-dependency-nội-bộ-comviettelbccs-cho-maven) — copy đúng và đủ artifact còn thiếu từ máy nội bộ.

**Build treo lâu / timeout khi resolve dependency**
Kiểm tra `~/.m2/settings.xml` có đang trỏ mirror về Nexus nội bộ (`nexus.kcntt.net`) không — nếu có, xoá/comment lại, máy ngoài không tới được địa chỉ đó nên mọi request sẽ timeout thay vì fallback về Maven Central.

**`ORA-...` hoặc kết nối Oracle bị từ chối**
Kiểm tra container `bccs-oracle` đã healthy chưa (`docker compose -f db-local/docker-compose.yml ps`), và `application-local.yml` đã trỏ đúng `localhost:1521/FREEPDB1` (không phải địa chỉ nội bộ `10.207.222.170`) — xem [mục 7](#7-trỏ-datasource-của-từng-service-về-local).

**Elasticsearch container crash ngay khi start**
Kiểm tra đúng image `8.15.3` trong `db-local/docker-compose.yml`, không phải `8.4.3` — xem lưu ý ở [mục 6](#6-hạ-tầng-docker-dùng-chung-oracle--elasticsearch).

**`IndexOutOfBoundsException` khi gọi `validateMapActiveInfo`**
Bug đã được fix trong code (`MapActiveInfoService.getUniqueMapActiveInfo`) — chỉ cần đảm bảo đang chạy bản code mới nhất (rebuild/restart service nếu vừa đồng bộ code cũ từ máy nội bộ sang).
