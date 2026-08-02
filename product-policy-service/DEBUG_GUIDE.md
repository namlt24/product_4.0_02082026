# Hướng Dẫn Chạy và Debug product-policy-service

## 1. Thiết Lập Maven (Quan Trọng - Bước Đầu Tiên)

Trước khi chạy service, cần **disable proxy** trong Maven settings vì proxy corporate `10.207.156.52:3128` làm MITM SSL interception.

**File:** `C:\Users\namlt24\.m2\settings.xml`

```xml
<proxies>
  <proxy>
    <id>http</id>
    <active>false</active>
    <protocol>http</protocol>
    <host>10.207.156.52</host>
    <port>3128</port>
  </proxy>
  <proxy>
    <id>https</id>
    <active>false</active>
    <protocol>https</protocol>
    <host>10.207.156.52</host>
    <port>3128</port>
  </proxy>
</proxies>
```

Xem file hiện tại bằng:
```bash
notepad C:\Users\namlt24\.m2\settings.xml
```

## 2. Chạy Service Từ CLI (Command Line)

### 2.1. Vào thư mục service
```bash
cd E:\product_4.0_new\product-policy-service
```

### 2.2. Chạy với profile local (khuyến nghị - không cần Kafka/Redis đầy đủ)
```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

PowerShell:
```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

### 2.3. Chạy với memory-only cache (nhanh nhất - không cần Redis)
```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--bccs.kafka.enabled=false --bccs.cache.mode=memory-only"
```

### 2.4. Kiểm tra service đã chạy chưa
```bash
curl http://localhost:8002/actuator/health
```

Service chạy trên port **8002**.

### 2.5. Kill process nếu cần
```bash
# Tìm PID đang chiếm port 8002
netstat -ano | findstr :8002

# Kill process (thay PID bằng số thực tế)
taskkill /PID <PID> /F
```

## 3. Enable DEBUG Logging (Để Xem Chi Tiết)

File cấu hình: `src/main/resources/application-local.yml`

Đảm bảo các level sau được set:
```yaml
logging:
  level:
    root: INFO
    com.viettel.bccs.policy.mapactiveinfo: DEBUG
    com.viettel.bccs.policy.reason: DEBUG
    com.viettel.bccs.policy.discountpromotion: DEBUG
    com.viettel.bccs.client.core.BccsHttpClient: DEBUG
    com.fasterxml.jackson: DEBUG
    com.viettel.bccs: DEBUG
bccs:
  web:
    include-stacktrace: true
  error:
    include-stacktrace: true
```

**Tại sao cần `include-stacktrace: true`?**
- Khi có lỗi, `BccsGlobalExceptionHandler` (từ `bccs-starter-web`) catch mọi exception và wrap thành `SystemException` với code `SYSTEM_ERROR`
- Mặc định, stack trace bị ẩn. Bật `include-stacktrace: true` để thấy root cause thực sự trong log.

## 4. Cách Đọc Log và Debug

### 4.1. Log output trực tiếp khi chạy `mvn spring-boot:run`
- Mọi log `log.info`, `log.debug`, `log.error` hiển thị ngay trên terminal
- Dùng `Ctrl+C` để dừng

### 4.2. Các log pattern quan trọng để track
```
[validateWithOutMapActiveInfo] START     -> Entry point của method
[validateWithOutMapActiveInfo] END OK     -> Thành công
[validateWithOutMapActiveInfo] CompletionException -> Lỗi từ async task
[AsyncTask] getListReason START/END       -> Async task 1 chạy thế nào
[AsyncTask] getPromotionList START/END    -> Async task 2 chạy thế nào
```

### 4.3. Timeout configuration
```yaml
app:
  async:
    task-timeout-ms: 5000      # Mỗi task async tối đa 5s
    total-timeout-ms: 10000    # Tổng thời gian tối đa 10s
```

## 5. Test API Endpoint Bằng Curl

### 5.1. Lưu request body vào file JSON (KHUYẾN NGHỊ)
**Đây là cách tránh lỗi UTF-8 encoding với tiếng Việt**

Tạo file request: `E:\product_4.0_new\product-policy-service\request.json`
```json
{
  "actionCode": "ACTION_SUB_SIM",
  "telServiceId": 3,
  "payType": "POST_PAID",
  "regReasonId": 2,
  "promotionCode": "KHUYEN_MAI_01",
  "productOfferType": "NORMAL",
  "lstBusinessNo": ["123456"],
  "staffDTO": {
    "shopName": "Cua hang A",
    "staffName": "Tran Van A",
    "shopAddress": "123 Duong ABC, Quan 1, TP HCM"
  }
}
```

### 5.2. Gọi curl với file JSON
```bash
curl -X POST "http://localhost:8002/product-policy-service/v1/map-active-info/validateWithoutMapActiveInfo" \
  -H "Content-Type: application/json" \
  -d @E:/product_4.0_new/product-policy-service/request.json
```

**Lưu ý quan trọng:**
- URL endpoint là **`validateWithoutMapActiveInfo`** (không phải `validateWithOutMapActiveInfo`)
- Tiếng Việt (Trần, Cửa hàng, Hồ Chí Minh) phải đặt trong file JSON với encoding UTF-8, KHÔNG truyền trực tiếp trong command - bash không xử lý đúng UTF-8 cho ký tự có dấu

### 5.3. Các endpoint khác thường dùng
```bash
# Check có cần map active info không
curl -X POST "http://localhost:8002/product-policy-service/v1/map-active-info/isCheckMapActiveInfo" \
  -H "Content-Type: application/json" \
  -d @request.json

# Validate input
curl -X POST "http://localhost:8002/product-policy-service/v1/map-active-info/validateInputMapActiveInfo" \
  -H "Content-Type: application/json" \
  -d @request.json

# Health check
curl http://localhost:8002/actuator/health
```

## 6. Các Lỗi Thường Gặp

### Lỗi 1: `PKIX path building failed` (certificate_unknown)
- **Nguyên nhân:** Proxy MITM SSL
- **Cách fix:** Disable proxy trong `~/.m2/settings.xml` (bước 1)

### Lỗi 2: `NoResourceFoundException` - HTTP 404
- **Nguyên nhân:** URL endpoint sai
- **Cách fix:** Kiểm tra đúng endpoint - vd: `validateWithoutMapActiveInfo` (không có chữ O hoa)

### Lỗi 3: `Invalid UTF-8 middle byte` - SYSTEM_ERROR
- **Nguyên nhân:** Tiếng Việt trong JSON body không encode đúng UTF-8 qua bash/curl
- **Cách fix:** Lưu request body vào file JSON rồi dùng `-d @filename`

### Lỗi 4: `SYSTEM_ERROR` không hiểu root cause
- **Nguyên nhân:** `BccsGlobalExceptionHandler` wrap tất cả exception
- **Cách fix:** Bật `bccs.error.include-stacktrace: true` trong config

### Lỗi 5: Kafka connection error khi khởi động
- **Nguyên nhân:** Kafka server `10.207.252.169:8859` không reachable
- **Cách fix:** Thêm arg `--bccs.kafka.enabled=false` khi chạy

## 7. Rebuild và Restart Nhanh

```bash
# Kill service (Ctrl+C hoặc taskkill)

# Clean + compile
cd E:\product_4.0_new\product-policy-service
mvn clean compile

# Chạy lại
mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--bccs.kafka.enabled=false --bccs.cache.mode=memory-only"
```

## 8. Quick Reference

| Thông tin | Giá trị |
|-----------|---------|
| Thư mục service | `E:\product_4.0_new\product-policy-service` |
| Port chạy | **8002** |
| Profile | `local` |
| Maven settings | `C:\Users\namlt24\.m2\settings.xml` |
| Config file | `src/main/resources/application-local.yml` |
| Main endpoint | `/product-policy-service/v1/map-active-info/validateWithoutMapActiveInfo` |
| Actuator health | `http://localhost:8002/actuator/health` |

## 9. Debug Flow Khi Gặp Lỗi

1. **Bật stack trace** trong `application-local.yml`
2. **Chạy service** với `mvn spring-boot:run`
3. **Gọi curl** với request body từ file JSON
4. **Đọc log** trên terminal - tìm `ERROR` hoặc `CompletionException`
5. **Xem root cause** từ stack trace được log ra
6. **Fix code** nếu cần, rebuild, restart

## 10. Các File Quan Trọng Trong Service

```
src/main/java/com/viettel/bccs/policy/
├── mapactiveinfo/
│   ├── controller/MapActiveInfoController.java    # REST endpoint
│   ├── service/MapActiveInfoService.java          # Business logic (async parallel execution)
│   ├── dto/request/*.java                          # Request DTOs
│   └── dto/response/*.java                         # Response DTOs
├── reason/service/ReasonService.java              # Gọi DB lấy reason
├── discountpromotion/service/DiscountPromotionService.java  # Gọi DB lấy promotion
├── config/AsyncConfig.java                        # Custom async executor config
└── client/                                         # Outbound HTTP calls (OptionSet, Staff, etc.)
```