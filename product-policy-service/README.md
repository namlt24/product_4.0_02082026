# BCCS Service Template

## Kiến trúc package-by-feature phân lớp

Luồng phụ thuộc chính:

```text
controller -> service -> repository / client / cache / event / mapper
```

```text
com.viettel.bccs.policy
├── sampleorder
│   ├── controller, dto/request, dto/response, service
│   ├── repository, entity, model, mapper
│   └── client, cache, event, metrics
├── sampleredis/controller, service
├── outboundsample/controller, dto, service, client
├── optionsetdemo/controller, dto, service, mapper
└── sampleerror/controller, dto, service
```

```text
HTTP request
  -> SampleOrderController
  -> SampleOrderService
  -> SampleOrderRepository hoặc CustomerClient
  -> SampleOrderMapper
  -> SampleOrderResponse
  -> StandardResponse
```

Controller chỉ xử lý transport, validation, security và response wrapper:

```java
@PostMapping
public ResponseEntity<StandardResponse<SampleOrderResponse>> createOrder(
        @Valid @RequestBody CreateSampleOrderRequest request) {
    SampleOrder order = sampleOrderService.createOrder(
            request.getCustomerId(), request.getProductCode(),
            request.getQuantity(), request.getChannel());
    return ResponseEntity.status(HttpStatus.CREATED).body(
            responseFactory.success(
                    "SMS_SEND_SUCCESS", mapper.toResponse(order), order.getCustomerId()));
}
```

Service giữ business orchestration và transaction; repository/mapper giữ entity
bên trong persistence; client chuyên trách tạo request hệ thống ngoài:

```java
@Service
public class SampleOrderService {
    @Transactional
    public SampleOrder createOrder(...) {
        SampleOrder saved = sampleOrderRepository.save(order);
        sampleOrderEventPublisher.publishOrderCreatedEvent(saved);
        return saved;
    }
}

@Repository
public class SampleOrderRepository {
    public SampleOrder save(SampleOrder order) {
        return mapper.toModel(jpaRepository.save(mapper.toEntity(order)));
    }
}

@Component
public class OutboundSampleClient {
    private final BccsRestClientFactory restClientFactory;
}
```

Tạo feature mới:

1. Tạo `<feature>/controller`, `<feature>/service` và DTO cần thiết.
2. Controller chỉ inject service cùng feature.
3. Chỉ thêm repository/entity/mapper, client, cache hoặc event khi thực sự dùng.
4. Đặt `@Transactional` tại public service method xác định unit of work.
5. Không trả entity từ REST và không gọi repository từ controller.
6. Thêm tests và chạy architecture rules.

```powershell
.\mvnw.cmd "-Dtest=LayeredArchitectureTest" test
.\mvnw.cmd clean verify
```

Xem [`docs/layered-architecture-guide.md`](docs/layered-architecture-guide.md)
để biết quy ước đầy đủ và hướng dẫn migrate từ Hexagonal Architecture cũ.

Repository này là template để sinh ra một microservice backend BCCS. Chứa một service Spring Boot sẵn sàng chạy được cùng code mẫu minh họa luồng `SampleOrder`.

Code mẫu không phải implementation production. Giữ nguyên phần quy định nền tảng, đổi tên service, sau đó sao chép, chỉnh sửa hoặc xóa các class `SampleOrder` theo ranh giới service của bạn.

## 1. Template cung cấp những gì

- Một service Maven sử dụng BCCS parent và BCCS starters.
- Cấu trúc package cho REST, application services, domain model, repository adapters, outbound clients, Kafka adapters, cache adapters, metrics, tests và cấu hình local.
- OpenAPI mẫu tại `docs/openapi/sample-order-api.yaml`.
- Scripts để render service mới, khởi động dependencies local, chạy tests và kiểm tra môi trường local.

Dùng template để học cách wiring được phê duyệt. Không giữ tên mẫu, topic mẫu, dữ liệu mẫu hoặc hành vi nghiệp vụ mẫu trong service thực tế.

## 2. Tạo service mới

### Windows PowerShell

Chạy từ thư mục `product-policy-service`:

```powershell
.\scripts\render-template.ps1 -ServiceName "payment-service" -BasePackage "com.viettel.bccs" -ServicePackage "payment" -OutputPath "..\payment-service"
```

### Linux, macOS hoặc Git Bash

Chạy từ thư mục `product-policy-service`:

```bash
./scripts/create-service.sh payment-service com.viettel.bccs.payment ../payment-service
```

Cấu hình maven trỏ đến nexus của trung tâm

```bash
Sao chép file settings.xml trong `deploy/settings.xml` vào thư mục C:\Users\{USER}\.m2\settings.xml
```

Sau đó build service vừa tạo:

```bash
cd ../payment-service
mvn clean install -U
```

## 3. Checklist khởi tạo service mới từ template

Chạy script render rồi thực hiện đầy đủ các bước sau. Mỗi mục đều phải pass trước khi coi là service sẵn sàng implement.

### 3.1. artifactId, name, description

Trong `pom.xml`, thay đổi các giá trị template:

```xml
<artifactId>payment-service</artifactId>
<name>payment-service</name>
<description>Payment service</description>
```

### 3.2. package (thay đổi tên package Java)

Di chuyển toàn bộ code từ package template sang package service mới:

```text
src/main/java/com/viettel/bccs/policy  ->  src/main/java/com/viettel/bccs/payment
src/test/java/com/viettel/bccs/policy  ->  src/test/java/com/viettel/bccs/payment
```

Sau đó thay tất cả `com.viettel.bccs.policy` → `com.viettel.bccs.payment` trong mọi file `.java` và `.yml`.

### 3.3. application name

```yaml
spring:
  application:
    name: ${APP_NAME:payment-service}
```

Dùng cùng tên trong `.env`, giá trị deploy, logging dashboards, tiền tố Kafka topic, tiền tố cache key và tên alert.

### 3.4. main application class

Đổi tên và package declaration:

```text
ProductPolicyServiceApplication.java  ->  PaymentServiceApplication.java
```

Giữ ở root của service package để Spring component scanning bao phủ toàn bộ feature.

### 3.5. bccs.config team-code và service-name

Trong `application.yml` và `application-local.yml`:

```yaml
bccs:
  config:
    team-code: PAYMENT          # đổi tên team, không dùng ORDER mặc định
    service-name: payment-service  # khớp với spring.application.name
```

### 3.6. Xóa toàn bộ code mẫu

Sau khi render, xóa tất cả các module mẫu (không phải code nghiệp vụ thực tế):

```
# Main source
src/main/java/com/viettel/bccs/<package>/sampleorder/       # xóa
src/main/java/com/viettel/bccs/<package>/sampleredis/       # xóa
src/main/java/com/viettel/bccs/<package>/sampleerror/       # xóa
src/main/java/com/viettel/bccs/<package>/outboundsample/    # xóa
src/main/java/com/viettel/bccs/<package>/openapi/           # xóa

# Nếu có optionsetdemo (chỉ là demo, không phải nghiệp vụ): xóa luôn
# Nếu optionset là code nghiệp vụ: đổi tên thành optionset (bỏ "demo")
# src/main/java/com/viettel/bccs/<package>/optionsetdemo/   ->  optionset/

# Test source
src/test/java/com/viettel/bccs/<package>/sampleorder/       # xóa
src/test/java/com/viettel/bccs/<package>/sampleredis/       # xóa
src/test/java/com/viettel/bccs/<package>/sampleerror/       # xóa
src/test/java/com/viettel/bccs/<package>/outboundsample/    # xóa
src/test/java/com/viettel/bccs/<package>/sampleredis/       # xóa

# Xóa test sampleorder liên quan đến StartupMatrixTest (Kafka disabled/enabled tests)
src/test/java/com/viettel/bccs/<package>/StartupMatrixTest.java  # xóa

# OpenAPI mẫu
docs/openapi/sample-order-api.yaml                          # xóa
```

### 3.7. Xóa cấu hình mẫu trong YAML

Trong `application-local.yml`, xóa các config mẫu:

```yaml
# XÓA các dòng sau:
bccs:
  sample-order:
    created: product-policy-service.sample-order.created
    dlq: product-policy-service.sample-order.dlq

  template:
    sample:
      redis:
        enabled: true
```

### 3.8. Uncomment và chạy LayeredArchitectureTest

Mở `src/test/java/com/viettel/bccs/<package>/architecture/LayeredArchitectureTest.java`:
- Bỏ comment toàn bộ file (file template để comment sẵn)
- Cập nhật rule `option_set_demo_is_independent` — xóa hoặc đổi tên nếu `optionsetdemo` đã được xử lý
- Xóa các rule cho feature mẫu đã xóa (`sample_order_is_independent`, `sample_redis_is_independent`, `outbound_sample_is_independent`, `sample_error_is_independent`)
- Giữ lại: `option_set_demo_is_independent` (nếu optionset là code nghiệp vụ, đổi tên thành `option_set_is_independent`)

```java
@ArchTest
static final ArchRule option_set_is_independent = featureDoesNotDependOnOthers(
        "optionset", "sampleorder", "sampleredis", "outboundsample", "sampleerror");
```

### 3.9. Cập nhật springdoc paths-to-exclude

Trong `application.yml`, xóa các sample paths khỏi springdoc exclusion:

```yaml
springdoc:
  paths-to-exclude: /actuator/**,/error
  # KHÔNG liệt kê /samples/**, /sample-redis/**, /demo/**, /sample-errors/** ở đây
```

### 3.10. Dọn secrets trong YAML (nếu có)

Đảm bảo password không hardcoded trong source control. Dùng biến môi trường:

```yaml
# ĐÚNG — không có default value hoặc placeholder rõ ràng
password: ${DB_PASSWORD:}

# SAI — hardcoded password
password: ${DB_PASSWORD:Viettel#2025}
```

### 3.11. Chạy verify cuối cùng

```powershell
mvn clean install -U
```

Phải đạt:
- ✅ Compile thành công (0 lỗi)
- ✅ Tất cả tests pass (LayeredArchitectureTest enforce dependency rules)
- ✅ Không còn class/import/reference đến `sampleorder`, `sampleredis`, `sampleerror`, `outboundsample`
- ✅ Không còn endpoint mẫu (`/api/v1/sample-*`, `/sample-redis`, `/api/v1/sample-errors`)

## 4. Chạy local

### Build

```bash
mvn clean install
```

### Chạy local

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

Để biết hướng dẫn chi tiết về Redis trong BCCS (cấu hình, chính sách TTL, cách dùng API), tham khảo [Redis Enterprise Usage Guide](docs/redis.md).

## 5. Cấu hình tùy chọn và chế độ chạy

Template hỗ trợ các chế độ chạy local cho team chưa cần mọi thành phần hạ tầng ngay từ đầu.

Các ví dụ dưới đây chỉ dùng cho local. Không đặt URL production, mật khẩu, token hoặc địa chỉ broker vào file template.

Luồng `SampleOrder` mẫu vẫn dùng Oracle/JPA, nên giữ database local chạy trừ khi bạn xóa code persistence mẫu.

### Chế độ Cache

Cấu hình `bccs.cache.mode` trong `application.yml`, `application-local.yml`, biến môi trường hoặc tham số chạy Maven:

- `memory-only`: Dùng cache in-memory local. Không cần Redis.
- `redis-only`: Dùng cache Redis tập trung. Nếu run local thì redis phải chạy local hoặc trỏ redis test
- `two-level`: Dùng cache memory được Redis hỗ trợ. Nếu run local thì redis phải chạy local hoặc trỏ redis test
- Tắt cache: starter hỗ trợ `bccs.cache.enabled=false`, nhưng cache adapter mẫu trong template inject `BccsCacheService`. Để chạy khi tắt cache, trước tiên xóa cache adapter mẫu và mọi chỗ dùng cache trong code service.

Cache memory local:

```yaml
bccs:
  cache:
    enabled: true
    mode: memory-only
```

Cache Redis local:

```yaml
spring:
  data:
     redis:
        cluster:
           nodes:
              - 127.0.0.1:6379
              - 127.0.0.1:6379
              - 127.0.0.1:6379
              - 127.0.0.1:6379
              - 127.0.0.1:6379
              - 127.0.0.1:6379
              - 127.0.0.1:6379
              - 127.0.0.1:6379
              - 127.0.0.1:6379
           max-redirects: 3
        username: ${REDIS_USERNAME:example-user}
        password: ${REDIS_PASSWORD:}
        timeout: 3000ms
        lettuce:
           pool:
              enabled: true
              max-active: 32
              max-idle: 16
              min-idle: 4
              max-wait: 3000ms
           shutdown-timeout: 100ms

bccs:
  cache:
    enabled: true
    mode: redis-only
```

Cache two-level local:

```yaml
spring:
  data:
     redis:
        cluster:
           nodes:
              - 127.0.0.1:6379
              - 127.0.0.1:6379
              - 127.0.0.1:6379
              - 127.0.0.1:6379
              - 127.0.0.1:6379
              - 127.0.0.1:6379
              - 127.0.0.1:6379
              - 127.0.0.1:6379
              - 127.0.0.1:6379
           max-redirects: 3
        username: ${REDIS_USERNAME:example-user}
        password: ${REDIS_PASSWORD:}
        timeout: 3000ms
        lettuce:
           pool:
              enabled: true
              max-active: 32
              max-idle: 16
              min-idle: 4
              max-wait: 3000ms
           shutdown-timeout: 100ms

bccs:
  cache:
    enabled: true
    mode: two-level
    invalidation:
      # Giữ false cho chạy local thông thường. Tính năng này dùng Redis Pub/Sub.
      enabled: ${BCCS_CACHE_INVALIDATION_ENABLED:false}
      topic: ${BCCS_CACHE_INVALIDATION_TOPIC:${spring.application.name}.cache.invalidation}
```

Yêu cầu Redis:

- `redis-only` và `two-level` cần Redis chạy trước khi app dùng cache.
- Thuộc tính local bắt buộc là `REDIS_HOST` và `REDIS_PORT`; cả hai mặc định `localhost` và `6379` trong profile local.
- Cache invalidation là tùy chọn với `two-level`. Chỉ bật khi user Redis có quyền Pub/Sub channel cho topic đã cấu hình.
- Không cấu hình mật khẩu Redis hoặc cluster nodes trong template. Thêm giá trị theo môi trường bên ngoài source control.

### Kafka

Cấu hình `bccs.kafka.enabled` trong `application.yml`, `application-local.yml`, biến môi trường hoặc tham số chạy Maven:

- `bccs.kafka.enabled=false`: Tắt auto-configuration Kafka BCCS. `NoopSampleOrderEventPublisher` mẫu xử lý lệnh publish mẫu mà không gửi lên Kafka. Listener mẫu cũng có điều kiện và sẽ không khởi động.
- `bccs.kafka.enabled=true`: Bật publish Kafka BCCS và code listener mẫu. Kafka phải chạy local.

Tắt Kafka ở local:

```yaml
bccs:
  kafka:
    enabled: false
```

Bật kafka ở local:

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

bccs:
  kafka:
    enabled: true
    consumer:
      group-id: ${BCCS_KAFKA_CONSUMER_GROUP_ID:${spring.application.name}-local}
```

Yêu cầu Kafka:

- `KAFKA_BOOTSTRAP_SERVERS` chỉ bắt buộc khi `bccs.kafka.enabled=true`.
- Khi `bccs.kafka.enabled=false`, code mẫu dùng `NoopSampleOrderEventPublisher`; không có event nào được publish.
- Nếu service thực tế tắt Kafka, giữ publisher/listener Kafka của bạn có điều kiện hoặc xóa chúng.

### Ví dụ chạy local

Ví dụ PowerShell:

Chạy không Kafka:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--bccs.kafka.enabled=false --bccs.cache.mode=memory-only"
```

Chạy với cache memory:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--bccs.kafka.enabled=false --bccs.cache.enabled=true --bccs.cache.mode=memory-only"
```

Chạy với cache Redis:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--bccs.kafka.enabled=false --bccs.cache.enabled=true --bccs.cache.mode=redis-only --spring.data.redis.host=localhost --spring.data.redis.port=6379"
```

Chạy với cache two-level không dùng Redis Pub/Sub invalidation:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--bccs.kafka.enabled=false --bccs.cache.enabled=true --bccs.cache.mode=two-level --bccs.cache.invalidation.enabled=false --spring.data.redis.host=localhost --spring.data.redis.port=6379"
```

Chạy với Kafka bật:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--bccs.kafka.enabled=true --spring.kafka.bootstrap-servers=localhost:9092 --bccs.cache.mode=memory-only"
```

Ví dụ Bash dùng cùng tham số:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.arguments="--bccs.kafka.enabled=false --bccs.cache.mode=memory-only"
```

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.arguments="--bccs.kafka.enabled=false --bccs.cache.mode=redis-only --spring.data.redis.host=localhost --spring.data.redis.port=6379"
```

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.arguments="--bccs.kafka.enabled=true --spring.kafka.bootstrap-servers=localhost:9092 --bccs.cache.mode=memory-only"
```

### Xử lý sự cố

- **Không kết nối được Redis**: Bạn cấu hình `bccs.cache.mode=redis-only` hoặc `bccs.cache.mode=two-level`, nhưng Redis chưa chạy hoặc `REDIS_HOST` / `REDIS_PORT` trỏ sai. Khởi động Redis bằng `docker compose -f docker-compose.local.yml --env-file .env up -d redis`, hoặc chuyển sang `bccs.cache.mode=memory-only`.
- **Redis NOPERM cho cache invalidation channel**: `bccs.cache.invalidation.enabled=true` khởi động `invalidationMessageListenerContainer`, subscribe topic Redis đã cấu hình. Nếu Redis trả về `NOPERM No permissions to access a channel`, chạy local với `--bccs.cache.invalidation.enabled=false` hoặc cấp quyền Pub/Sub cho user Redis trước khi bật invalidation.
- **Thiếu Kafka bootstrap**: Bạn bật Kafka nhưng chưa cung cấp bootstrap server khả dụng. Khởi động Kafka bằng `docker compose -f docker-compose.local.yml --env-file .env up -d kafka kafka-ui` và dùng `--spring.kafka.bootstrap-servers=localhost:9092`, hoặc chạy với `--bccs.kafka.enabled=false`.
- **Không có bean tên BccsEventPublisher**: `bccs.kafka.enabled=false` tắt bean `BccsEventPublisher` của nền tảng. Template mẫu tránh lỗi này bằng `NoopSampleOrderEventPublisher`. Với code thực tế, hoặc xóa Kafka publisher khi không cần Kafka, hoặc làm adapter publisher có điều kiện với `@ConditionalOnProperty(prefix = "bccs.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)`.
- **Kafka listener vẫn khởi động khi đã tắt**: Listener mẫu có điều kiện `bccs.kafka.enabled=true`. Nếu service thêm `@KafkaListener` mới, áp dụng cùng điều kiện hoặc xóa listener khi Kafka tắt.

## 6. Quy tắc dependency

Chỉ dùng BCCS starters. Không khai báo trực tiếp thư viện core nền tảng hoặc transitive dependencies ẩn trong service.

Mẫu được phép:

```xml
<dependency>
    <groupId>com.viettel.bccs</groupId>
    <artifactId>bccs-starter-service-base</artifactId>
</dependency>
```

Các starter hiện có trong template:

- `bccs-starter-service-base` cho web, validation/xử lý lỗi, security, logging/tracing và tích hợp observability.
- `bccs-starter-data` cho repository và transaction.
- `bccs-starter-kafka` cho Kafka producer/consumer.
- `bccs-starter-cache` cho tích hợp cache BCCS.
- `bccs-starter-test` cho tiện ích test và quy tắc kiến trúc.

Các dependency nếu thêm trực tiếp thì cần xem lại tính tương thích với project, cần thận trong khi sử dụng:

- Spring Boot web/data/security/Kafka/cache starters.
- `spring-kafka`, `KafkaTemplate`, Redis clients, Caffeine, Micrometer, OpenTelemetry, HTTP clients hoặc thư viện JSON.
- `WebFlux`, `WebClient`, `Mono`, `Flux` hoặc reactive database drivers.

## 7. Hướng dẫn tech stack mẫu

Mọi class trong `com.viettel.bccs.policy` đều là code mẫu. Dùng làm ví dụ, sau đó thay bằng tên và hành vi theo nghiệp vụ.

### REST API

File mẫu:

- `sampleorder/controller/SampleOrderController.java`
- `sampleorder/dto/request/CreateSampleOrderRequest.java`
- `sampleorder/dto/response/SampleOrderResponse.java`

Pattern:

- Controller expose REST path có version như `/api/v1/...`.
- Controller gọi application use case, không gọi repository hoặc infrastructure adapter.
- Response dùng wrapper nền tảng `StandardResponse<T>` qua helper `StandardResponses.success()`.
- Endpoint `GET` trả về `StandardResponse<T>` (tương ứng HTTP 200).
- Endpoint `POST` trả về `ResponseEntity<StandardResponse<T>>` với HTTP 201 Created.

Hành động khi đưa vào production:

- Thay `SampleOrderController` bằng controller cho API nghiệp vụ.
- Giữ request/response DTO ở biên API.
- Không đặt business rules trong controller.

### validation/error

File mẫu:

- `sampleorder/dto/request/CreateSampleOrderRequest.java`
- `sampleorder/exception/SampleErrorCode.java`

Pattern:

- Dùng annotation `jakarta.validation` như `@NotBlank`, `@NotNull` và `@Min`.
- Để stack web/error BCCS chuyển lỗi validation và application thành response API chuẩn `ErrorResponse`.
- Ném `BusinessException` với `ErrorCode` theo domain (ví dụ `SampleErrorCode`) cho lỗi logic nghiệp vụ.
- Ném `SystemException` cho lỗi kỹ thuật không mong đợi.
- KHÔNG ném trực tiếp `IllegalArgumentException` hoặc `RuntimeException`.

Hành động khi đưa vào production:

- Thay message validation mẫu bằng validation field nghiệp vụ.
- Định nghĩa enum `ErrorCode` tập trung cho service.
- Không tự tạo format exception response hoặc `ErrorResponse` thủ công trong từng controller.
- Để biết hướng dẫn đầy đủ về response và exception, tham khảo [BCCS API Response & Exception Guideline](../../docs/api-response-exception-guideline.md).

### feature sampleorder phân lớp

File mẫu:

- `sampleorder/controller/SampleOrderController.java`
- `sampleorder/service/SampleOrderService.java`
- `sampleorder/dto/request/CreateSampleOrderRequest.java`
- `sampleorder/dto/response/SampleOrderResponse.java`
- `sampleorder/model/SampleOrder.java`
- `sampleorder/model/SampleOrderStatus.java`
- `sampleorder/entity/SampleOrderEntity.java`
- `sampleorder/repository/SampleOrderJpaRepository.java`
- `sampleorder/mapper/SampleOrderMapper.java`

Pattern:

- `model` chứa trạng thái nghiệp vụ không phụ thuộc persistence.
- `service` điều phối nghiệp vụ và sở hữu transaction boundary.
- `repository`, `client`, `cache` và `event` giữ từng trách nhiệm kỹ thuật trong feature.
- Feature repository bao Spring Data và map entity sang model.

Hành động khi đưa vào production:

- Chỉ giữ interface cho nhiều implementation, lựa chọn theo config hoặc boundary hệ thống ngoài.
- Map model, persistence entity và response DTO trong feature mapper.
- Không inject Spring Data repository trực tiếp vào controller hoặc domain object.

### transaction

File mẫu:

- `sampleorder/service/SampleOrderService.java`

Pattern:

- Đặt `@Transactional` trên method application service sở hữu một thao tác nghiệp vụ.
- Không mở transaction trong REST controller.

Hành động khi đưa vào production:

- Giữ ranh giới transaction quanh một use case.
- Tránh transaction dài bao gồm HTTP call từ xa, trừ khi kiến trúc nền tảng phê duyệt rõ luồng đó.

### cache

File mẫu:

- `sampleorder/cache/SampleOrderCache.java`

Cấu hình mẫu:

- `bccs.cache` trong `src/main/resources/application.yml`
- override local trong `src/main/resources/application-local.yml`

Pattern:

- Dùng `BccsCacheService`.
- Dùng tên cache và key theo service.
- Cấu hình mode (`memory-only`, `redis-only`, `two-level`) và TTL qua `bccs.cache`, không viết code Redis hoặc Caffeine trực tiếp.
- Lưu ý: mode `redis-only` và `two-level` cần instance Redis đang chạy (dùng Docker Compose local khi phát triển).

Hành động khi đưa vào production:

- Thay tên cache `sample-orders` và key `order:*`.
- Xóa hoàn toàn code cache nếu service không cần cache.
- Không inject `RedisTemplate`, Redis client native hoặc Caffeine trực tiếp.

### outbound HTTP client

File mẫu:

- `sampleorder/client/CustomerClient.java`
- `sampleorder/client/BccsCustomerClient.java`
- `sampleorder/client/CustomerResponse.java`

Cấu hình mẫu:

- `bccs.client.clients.customer-service` trong `application-local.yml`

Pattern:

- Code application phụ thuộc client port.
- Code infrastructure dùng `BccsHttpClient`.
- Template đồng bộ/blocking. Không dùng `WebClient`, `Mono` hoặc `Flux`.

Hành động khi đưa vào production:

- Đổi `customer-service` thành key service downstream thực tế.
- Thay URL và DTO mẫu.
- Không dùng HTTP client thô hoặc reactive client.

### Kafka

File mẫu:

- `sampleorder/event/SampleOrderEventPublisher.java`
- `sampleorder/event/SampleOrderCreatedEvent.java`
- `sampleorder/event/KafkaSampleOrderEventPublisher.java`
- `sampleorder/event/SampleOrderEventConsumer.java`

Cấu hình mẫu:

- `bccs.kafka` trong `application.yml`
- `bccs.sample-order.created` và `bccs.sample-order.dlq` trong `application-local.yml`

Pattern:

- Publish qua `BccsEventPublisher`.
- Giữ code Kafka-specific trong infrastructure adapter.
- Consumer dùng annotation Spring Kafka listener như trong mẫu.
- Tên topic lấy từ cấu hình.

Hành động khi đưa vào production:

- Thay tên topic mẫu trước khi kết nối Kafka dùng chung.
- Xóa consumer mẫu nếu service chỉ produce event.
- Xóa producer mẫu nếu service chỉ consume event.
- Không dùng Kafka client thô trong business logic.
- Để tắt hoàn toàn Kafka, đặt `bccs.kafka.enabled=false`. Template cung cấp fallback `NoopSampleOrderEventPublisher` để service vẫn khởi động mà không cần Kafka bootstrap servers.

### security

File/cấu hình mẫu:

- `sampleorder/controller/SampleOrderController.java`
- `sampleorder/controller/SampleOrderControllerSecurityTest.java`
- `bccs.security` trong `application.yml`

Pattern:

- Public path cấu hình dưới `bccs.security.public-paths`.
- Phân quyền cấp method có thể dùng annotation Spring Security như `@PreAuthorize`.
- Profile local mặc định tắt JWT để phát triển local dễ hơn.

Hành động khi đưa vào production:

- Thay `SAMPLE_ORDER_DELETE` bằng authority thực tế của service.
- Bật và xác thực cấu hình JWT/security production trước khi deploy.
- Không thêm security filter riêng của service trừ khi team nền tảng phê duyệt.

### logging/tracing

Cấu hình mẫu:

- `logging.level` và `bccs.logging` trong `application.yml`
- override request logging local trong `application-local.yml`

Pattern:

- Dùng logging SLF4J thông thường trong application code.
- Để BCCS logging starter quản lý MDC, request context, masking và request/response logging.
- Log request body mặc định tắt; giữ tắt trừ khi được phê duyệt rõ cho local/debug an toàn.
- Sử dụng annotation lombok @Slf4j không cần khai báo logger

Hành động khi đưa vào production:

- Không log secret, token, mật khẩu, định danh khách hàng hoặc payload chứa dữ liệu nhạy cảm.
- Dùng `spring.application.name` nhất quán để log, trace, metric và dashboard nhóm đúng.

### observability/metrics

File/cấu hình mẫu:

- `sampleorder/metrics/SampleOrderMetrics.java`
- `sampleorder/metrics/SampleOrderMetrics.java`
- `management.endpoints.web.exposure.include`
- `bccs.observability`

Pattern:

- Actuator endpoint expose health, info, metrics và Prometheus theo cấu hình.
- Infrastructure adapter có thể dùng Micrometer qua platform starter.
- Application service phụ thuộc metrics port thay vì `MeterRegistry`.

Hành động khi đưa vào production:

- Đổi tên metric mẫu theo từ vựng service.
- Giữ cardinality thấp; không tag metric bằng ID không giới hạn như customer ID, order ID, token hoặc request payload.

### resilience/idempotency

Template này chưa có starter resilience hoặc idempotency hoàn chỉnh.

Hành động khi đưa vào production:

- Không thêm trực tiếp Resilience4j, retry, circuit-breaker hoặc idempotency-lock dependency.
- Không mô tả service là idempotent chỉ vì request có field ID.
- Nếu service cần retry, circuit breaker, rate limit, deduplication hoặc idempotency key, yêu cầu thay đổi nền tảng.

### OpenAPI

File mẫu:

- `docs/openapi/sample-order-api.yaml`

Pattern:

- Giữ API contract trong `docs/openapi`.
- Giữ ví dụ khớp với path controller và field DTO.

Hành động khi đưa vào production:

- Thay `sample-order-api.yaml` bằng contract service.
- Xóa path mẫu trước khi publish tài liệu API.

### tests

File mẫu:

- `src/test/java/com/viettel/bccs/template/sampleorder/controller/*Test.java`
- `src/test/java/com/viettel/bccs/template/sampleorder/service/*Test.java`
- `src/test/java/com/viettel/bccs/template/sampleorder/*Test.java`
- `src/test/java/com/viettel/bccs/template/architecture/LayeredArchitectureTest.java`

Pattern:

- Controller test cover hành vi REST và validation.
- Service test cover business orchestration với collaborator phù hợp.
- Repository, mapper, client, cache, event và metrics test cover boundary cụ thể.
- Architecture test enforce ranh giới dependency như không hidden core dependency, không dùng Redis client trực tiếp và không WebFlux.

Hành động khi đưa vào production:

- Đổi tên test theo package service.
- Giữ architecture test.
- Xóa test mẫu khi xóa code mẫu.
- Thêm test theo service trước khi thay luồng mẫu.

Chạy tests:

```bash
mvn clean test
```

- `FORBIDDEN` ("BCCS-AUTH-SEC-0002"): Access denied.
- `VALIDATION_ERROR` ("BCCS-SYS-VAL-0001"): Failed bean validation (e.g. `@Valid`).
- `INTERNAL_ERROR` ("BCCS-SYS-TECH-0001"): Unknown server error mapped from `RuntimeException`.
- `INTEGRATION_ERROR` ("BCCS-SYS-INT-0001"): Downstream or infrastructure failure.
- `INTEGRATION_TIMEOUT` ("BCCS-SYS-INT-0002"): Downstream timeout.
- `RATE_LIMIT_EXCEEDED` ("BCCS-SYS-RATE-0001"): Rate limit breach.

### Throwing Exceptions
To trigger an error, use the specific exception subclasses in `com.viettel.bccs.common.error.exception`:

1. **Business Error**:
   ```java
   throw new BusinessException("BCCS-ORD-BIZ-0001", "Invalid state for operation");
   ```
2. **Integration Error**:
   ```java
   throw new IntegrationException(CommonErrorCode.INTEGRATION_TIMEOUT.getCode(), "Upstream service timeout");
   ```
3. **Validation Error**:
   Rely on `@Valid` annotations in your controller.
4. **System Error**:
   Thrown explicitly via `SystemException` or implicitly when a generic `RuntimeException` occurs.

### Example Error Response
All errors yield an `ErrorResponse` structured consistently with successes, containing `errors[]` for validation. Raw system exception messages and stacktraces are deliberately scrubbed from the response payload to prevent information leakage.

If a validation error occurs, the client automatically receives:
```json
{
  "success": false,
  "code": "BCCS-SYS-VAL-0001",
  "message": "validation error",
  "traceId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
  "path": "/api/v1/sample-orders",
  "timestamp": "2026-07-04T12:00:00.000Z",
  "data": null,
  "errors": [
    {
      "field": "productCode",
      "code": "NotBlank",
      "message": "must not be blank",
      "rejectedValue": null
    }
  ]
}
```

### Configuration & Response Codes

1. This template uses ccs-config-service for response-code registry.

3. Each service must configure ccs.config.team-code (e.g., ORDER).
4. Kafka (ccs.config-events.v1) only notifies version changes.
5. Service pulls snapshot in background periodically via scheduler or when notified by Kafka.
6. Runtime response resolution uses local memory cache only.
7. Missing code returns message = code as the default behavior.
8. Message template supports interpolation using varargs array.
9. No fallback JSON file is used or needed.



### Checking Error Logs & traceId
The generated `traceId` in the JSON response exactly matches the `traceId` injected into MDC context and printed in the backend logs.
To trace a specific issue, locate the `traceId` from the client response, and query it in your centralized log viewer (e.g. EFK stack).

**Sample Log Format:**
```text
Error - traceId: 9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d, serviceName: sample-service, errorCode: BCCS-SYS-VAL-0001, exceptionType: MethodArgumentNotValidException, httpStatus: 400, method: POST, path: /api/v1/sample-orders, message: Validation failed for argument...
```

You can test these scenarios by invoking the sample endpoints exposed locally in `SampleErrorController.java`.

## 8. Yêu cầu thay đổi nền tảng

Mở yêu cầu nền tảng khi service cần khả năng chưa được expose qua BCCS starter.

Bao gồm:

- Tên service nghiệp vụ và owner.
- Yêu cầu nghiệp vụ.
- Khả năng nền tảng còn thiếu.
- Hành vi đề xuất và yêu cầu vận hành.
- Tác động security, logging, tracing, metrics và deployment.
- Khả năng cần cho phát triển local, runtime production, tests hay CI/CD.

Do not ship direct hidden dependencies while waiting for platform approval. The platform team should add or update the starter, publish the artifact, and then service teams consume the approved version through the BCCS parent/starter model.
### Dynamic Client Configuration (CLIENT_CONFIG)

The service automatically supports **dynamic outbound client configuration** without requiring a service restart or @RefreshScope. 

**Key Concepts:**
- **Bootstrap Config:** ccs.config.client.* connects exclusively to the ccs-config-service to download configuration snapshots.
- **Fallback Config:** ccs.client.clients.* acts as the static fallback outbound client configuration. **Only this section is dynamically updatable.**
- **Runtime Behavior:** The service does **not** perform blocking API calls to ccs-config-service during active business logic. It relies on its local memory cache of RestClient instances.
- **Eviction:** When a new configuration snapshot is published (via Kafka or /version scheduler polling), the cache is evicted, and the next request effortlessly reconstructs the client with updated settings (aseUrl, connectTimeout, etc.).

**Supported Dynamic Fields:**
- enabled
- aseUrl
- connectTimeout
- eadTimeout
- esponseTimeout

*Note: Security/Authentication modes, resilience properties, and connection pools are static by design and cannot be dynamically injected via  ccs-config-service.*

If a client is missing in a dynamic snapshot, it uses the application file fallback (missing-client.mode: use-file-config). If it's missing in both, it safely fails fast to prevent ambiguous routing.

## 9. Outbound Client Samples

The template provides sample controllers mapping to common BCCS external integrations. These samples show how to cleanly call outbound systems using `BccsRestClientFactory` and dynamic configurations without hardcoding IPs or credentials.

- **sms-vas**: Maps to the SMS VAS HTTP POST pattern (query parameters, custom headers, Basic Auth, no body). Custom `username/password` headers come from `defaultHeaders`.
- **push-transaction**: Maps to the Push Transaction pattern (JSON body, Basic Auth).
- **bccs-soap**: Maps to the BCCS SOAP pattern (raw XML body, Basic Auth).

**Important Security Concepts:**
- Basic Auth tokens are automatically generated by the platform config. Business code never manually intercepts or builds `Authorization` headers.
- No real credentials should ever be committed to Java files or test configurations.
- For local development, fallback placeholders are located in `application-local.yml` under `bccs.client.clients.*`.
- For real environments, configure these values through the DB dynamic `CLIENT_CONFIG` or environment variables.
- If dynamic `CLIENT_CONFIG` is enabled, DB snapshot overrides the fallback YAML safely at runtime.

