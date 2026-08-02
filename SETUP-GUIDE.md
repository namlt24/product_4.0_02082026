# Hướng dẫn cài đặt & chạy BCCS Microservice Platform

Tài liệu này hướng dẫn máy mới pull toàn bộ code về và chạy 6 service trên BCCS Platform.

---

## Mục lục

1. [Yêu cầu hệ thống](#1-yêu-cầu-hệ-thống)
2. [Cấu hình Git Credentials](#2-cấu-hình-git-credentials)
3. [Pull toàn bộ service](#3-pull-toàn-bộ-service)
4. [Cấu hình Maven](#4-cấu-hình-maven)
5. [Chạy infrastructure (Docker)](#5-chạy-infrastructure-docker)
6. [Build service](#6-build-service)
7. [Chạy service](#7-chạy-service)
8. [Kiểm tra service đã chạy](#8-kiểm-tra-service-đã-chạy)
9. [Chạy nhanh không cần Kafka/Redis (dev)](#9-chạy-nhanh-không-cần-kafkaredis-dev)
10. [Xử lý lỗi thường gặp](#10-xử-lý-lỗi-thường-gặp)

---

## 1. Yêu cầu hệ thống

| Phần mềm | Phiên bản tối thiểu | Ghi chú |
|---|---|---|
| JDK | 25+ | `java -version` kiểm tra |
| Maven | 3.9+ | `mvn -version` kiểm tra |
| Docker Desktop | latest | Cần enable WSL2 (Windows) |
| Git | 2.x | |

**Lưu ý:** Nếu máy không đủ RAM cho Docker (Oracle + Kafka + Redis), xem mục [9. Chạy nhanh không cần Kafka/Redis](#9-chạy-nhanh-không-cần-kafkaredis-dev).

---

## 2. Cấu hình Git Credentials

Các repo nằm trên GitLab nội bộ `http://10.255.60.6`. Cần lưu credentials để không phải nhập lại.

### Cách 1: Credential Helper (khuyến nghị)

```bash
git config --global credential.helper store
```

Sau đó clone lần đầu sẽ hỏi user/pass, thông tin sẽ được lưu lại:

```bash
git clone http://namlt:ancuttaoy9@10.255.60.6/bccs/product/product-catalog-service.git
```

### Cách 2: NetRC

Thêm vào `~/.netrc` (Windows: `C:\Users\<username>\.netrc` hoặc `~/.netrc` qua Git Bash):

```
machine 10.255.60.6
login namlt
password ancuttaoy9
```

---

## 3. Pull toàn bộ service

Clone tất cả 6 service về cùng một thư mục cha:

```bash
# Tạo thư mục chứa project
mkdir bccs-platform
cd bccs-platform

# Clone lần lượt
git clone http://namlt:ancuttaoy9@10.255.60.6/bccs/product/product-catalog-service.git
git clone http://namlt:ancuttaoy9@10.255.60.6/bccs/product/product-policy-service.git
git clone http://namlt:ancuttaoy9@10.255.60.6/bccs/product/product-area-service.git
git clone http://namlt:ancuttaoy9@10.255.60.6/bccs/product/product-price-service.git
git clone http://namlt:ancuttaoy9@10.255.60.6/bccs/product/organization-resource-service.git
git clone http://namlt:ancuttaoy9@10.255.60.6/bccs/product/spec-common-service.git
```

Sau khi clone xong, cấu trúc thư mục như sau:

```
bccs-platform/
├── product-catalog-service/
├── product-policy-service/
├── product-area-service/
├── product-price-service/
├── organization-resource-service/
└── spec-common-service/
```

Mỗi service là một repo độc lập. **Không có parent POM** ở thư mục gốc.

---

## 4. Cấu hình Maven

Maven cần trỏ vào Nexus nội bộ của Viettel để download dependencies.

Kiểm tra file `~/.m2/settings.xml`:

```bash
cat ~/.m2/settings.xml
```

Nếu chưa có hoặc chưa đúng, tạo file `~/.m2/settings.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <mirrors>
    <mirror>
      <id>viettel-nexus</id>
      <name>Viettel Nexus</name>
      <url>https://nexus.kcntt.net/repository/maven-group/</url>
      <mirrorOf>*</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

**Lưu ý:** Nếu Nexus yêu cầu authentication, thêm `<server>` vào settings.xml:

```xml
  <servers>
    <server>
      <id>viettel-nexus</id>
      <username>TÀI KHOẢN_NEXUS</username>
      <password>MẬT_KHẨU_NEXUS</password>
    </server>
  </servers>
```

---

## 5. Chạy infrastructure (Docker)

Mỗi service đi kèm file `docker-compose.local.yml` và `.env.example`. Docker chạy Oracle (database), Redis (cache), Kafka + Kafka UI.

### 5.1. Cài đặt Docker Desktop

1. Tải Docker Desktop từ https://www.docker.com/products/docker-desktop/
2. Enable WSL2 integration (Windows)
3. Khởi động Docker Desktop, chờ "Docker Desktop is running"

### 5.2. Cấu hình env cho từng service

Mỗi service cần file `.env`. Các service chia sẻ cùng Oracle DB (`10.207.222.170:1521:DB170`) và Redis cluster, nên không cần chạy Docker local cho database — chỉ cần khi muốn chạy độc lập.

```bash
# Vào thư mục service, copy và chỉnh sửa .env
cd product-catalog-service
cp .env.example .env
# Chỉnh sửa .env nếu cần thay đổi port/service name
```

### 5.3. Khởi động Docker infrastructure (chạy nền)

```bash
# Chạy Oracle + Redis + Kafka + Kafka UI
docker compose -f docker-compose.local.yml --env-file .env up -d

# Kiểm tra các container đang chạy
docker compose -f docker-compose.local.yml ps

# Xem logs
docker compose -f docker-compose.local.yml logs -f
```

Sau khi Oracle khởi động xong (khoảng 2-3 phút lần đầu), các service sẽ kết nối được.

**Danh sách cổng mặc định:**

| Service | Port |
|---|---|
| Oracle | 1521 |
| Redis | 6379 |
| Kafka | 9092 |
| Kafka UI | 8085 |

---

## 6. Build service

Build tất cả 6 service:

```bash
# Build tất cả service (từ thư mục cha chứa 6 service)
for svc in product-catalog-service product-policy-service product-area-service product-price-service organization-resource-service spec-common-service; do
  echo "Building $svc..."
  cd "$svc"
  ./mvnw clean install -DskipTests
  cd ..
done
```

Hoặc build từng service riêng lẻ:

```bash
cd product-catalog-service
./mvnw clean install -DskipTests
```

**Build với tests:**

```bash
./mvnw clean verify
```

---

## 7. Chạy service

### 7.1. Chạy trên Windows (PowerShell)

```powershell
# Chạy service với profile local
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

### 7.2. Chạy trên Linux/macOS

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### 7.3. Chạy nhiều service cùng lúc

Mỗi service cần một terminal riêng. Nếu chạy nhiều service trên cùng máy, phải đổi port trong `application-local.yml`:

```yaml
server:
  port: 8080  # thay đổi: 8081, 8082, ...
```

Mặc định mỗi service chạy port riêng (đã cấu hình sẵn trong mỗi service). Kiểm tra port trong `src/main/resources/application.yml`:

```bash
grep -r "server.port" */src/main/resources/application.yml
```

### 7.4. Chạy service từ IDE (IntelliJ IDEA)

1. Mở project: File → Open → chọn thư mục service (VD: `product-catalog-service`)
2. IntelliJ sẽ nhận diện là Maven project → Import
3. Chạy: chuột phải vào class `*ServiceApplication.java` → Run
4. Chỉnh profile: Run Configuration → VM Options: `-Dspring-boot.run.profiles=local`
5. Hoặc tạo Run Configuration mới với:

```
Main class: com.viettel.bccs.productcatalog.ProductCatalogServiceApplication
VM options: -Dspring-boot.run.profiles=local
```

---

## 8. Kiểm tra service đã chạy

### 8.1. Health check

```bash
curl http://localhost:8080/actuator/health
```

Kết quả mong đợi:

```json
{
  "status": "UP"
}
```

### 8.2. Swagger/OpenAPI

```bash
curl http://localhost:8080/swagger-ui.html
# hoặc
curl http://localhost:8080/v3/api-docs
```

### 8.3. Kafka UI

Mở trình duyệt: http://localhost:8085

---

## 9. Chạy nhanh không cần Kafka/Redis (dev)

Nếu máy không chạy được Docker hoặc muốn dev nhanh không phụ thuộc hạ tầng:

```bash
# Tắt Kafka, dùng Redis local (Caffeine cache)
./mvnw spring-boot:run \
  "-Dspring-boot.run.profiles=local" \
  "-Dspring-boot.run.arguments=--bccs.kafka.enabled=false --bccs.cache.mode=memory-only"
```

Cấu hình `bccs.cache.mode`:

| Mode | Mô tả | Yêu cầu |
|---|---|---|
| `memory-only` | Local Caffeine cache, không cần Redis | Không cần Redis |
| `redis-only` | Centralized Redis cache | Cần Redis chạy |
| `two-level` | Local cache backed by Redis | Cần Redis chạy |

Cấu hình `bccs.kafka.enabled=false` sẽ dùng no-op publisher, không cần Kafka chạy.

---

## 10. Xử lý lỗi thường gặp

### 10.1. Lỗi "Could not resolve dependencies"

```
Could not resolve dependencies for project com.viettel.bccs:...
```

**Nguyên nhân:** Maven chưa trỏ đúng vào Nexus nội bộ.

**Khắc phục:**

```bash
# Kiểm tra settings.xml
cat ~/.m2/settings.xml

# Xem Maven trỏ đúng chưa
./mvnw dependency:resolve -U
```

### 10.2. Lỗi "Connection refused" đến Oracle/Redis

**Nguyên nhân:** Database/Redis chưa khởi động hoặc sai địa chỉ.

**Khắc phục:**

```bash
# Kiểm tra Docker containers
docker ps

# Kiểm tra Oracle đã healthy chưa
docker compose -f docker-compose.local.yml ps database

# Xem logs
docker compose -f docker-compose.local.yml logs database

# Thử kết nối trực tiếp (nếu dùng Docker local)
sqlplus bccs/bccs@localhost:1521/FREEPDB1
```

### 10.3. Lỗi "Port already in use"

```
Bind for [::]:8080 failed: Address already in use
```

**Khắc phục:**

```bash
# Tìm process chiếm port
netstat -ano | findstr :8080   # Windows
# hoặc
lsof -i :8080                   # Linux/macOS

# Kill process
taskkill /PID <PID> /F          # Windows
kill -9 <PID>                   # Linux/macOS

# Hoặc đổi port trong application-local.yml
```

### 10.4. Lỗi "Permission denied" khi chạy mvnw

```bash
# Linux/macOS: cấp quyền execute
chmod +x mvnw
```

### 10.5. Lỗi "LF will be replaced by CRLF"

Cảnh báo Git về line ending, không ảnh hưởng đến chạy. Nếu muốn tắt:

```bash
git config --global core.autocrlf input
```

### 10.6. Oracle Docker chậm hoặc không start

Oracle Free image khởi động lần đầu mất 2-5 phút. Kiểm tra:

```bash
docker compose -f docker-compose.local.yml logs database | tail -50
```

Nếu Oracle không health check sau 30 lần thử, restart:

```bash
docker compose -f docker-compose.local.yml restart database
```

### 10.7. Kafka Docker không start đúng

```bash
# Xem logs chi tiết
docker compose -f docker-compose.local.yml logs kafka

# Restart Kafka
docker compose -f docker-compose.local.yml restart kafka
```

---

## Danh sách endpoint các service

Sau khi chạy thành công, kiểm tra Swagger UI của từng service theo port tương ứng:

| Service | URL Swagger | Ghi chú |
|---|---|---|
| product-catalog | http://localhost:8080/swagger-ui.html | Template source |
| product-policy | http://localhost:8081/swagger-ui.html | |
| product-area | http://localhost:8082/swagger-ui.html | |
| product-price | http://localhost:8083/swagger-ui.html | |
| organization-resource | http://localhost:8084/swagger-ui.html | |
| spec-common | http://localhost:8085/swagger-ui.html | Port trùng Kafka UI |

**Lưu ý:** Port có thể khác tùy cấu hình trong `application-local.yml`. Kiểm tra trực tiếp trong file cấu hình của từng service.

---

## Liên hệ & Hỗ trợ

- GitLab: http://10.255.60.6/bccs/product
- Maven Nexus: https://nexus.kcntt.net/repository/maven-group/
- Kafka UI (local): http://localhost:8085