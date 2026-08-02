# BCCS Service Template

## Package-by-feature layered architecture

Primary dependency flow:

```text
controller -> service -> repository / client / cache / event / mapper
```

```text
com.viettel.bccs.productcatalog
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
  -> SampleOrderRepository or CustomerClient
  -> SampleOrderMapper
  -> SampleOrderResponse
  -> StandardResponse
```

Controllers own transport, validation, security, and response wrapping only:

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

Services own business orchestration and transactions; repositories/mappers keep
entities internal; clients own downstream request construction:

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

To create a feature:

1. Create `<feature>/controller`, `<feature>/service`, and needed DTOs.
2. Make the controller inject only its same-feature service.
3. Add repository/entity/mapper, client, cache, or event only when used.
4. Put `@Transactional` on the public service unit of work.
5. Never return entities from REST or call repositories from controllers.
6. Add tests and run the architecture rules.

```powershell
.\mvnw.cmd "-Dtest=LayeredArchitectureTest" test
.\mvnw.cmd clean verify
```

See [`docs/layered-architecture-guide.md`](docs/layered-architecture-guide.md)
for complete conventions and migration notes from the previous Hexagonal layout.

This repository is a copyable starting point for a BCCS backend microservice. It contains a runnable Spring Boot service plus sample code that demonstrates the approved BCCS stacks through one `SampleOrder` flow.

The sample code is not production implementation. Keep the platform wiring, rename the service, then copy, modify, or delete the `SampleOrder` classes according to your service boundary.

## 1. What This Template Provides

- A Maven service using the BCCS parent and BCCS starters.
- A package layout for REST, application services, domain model, repository adapters, outbound clients, Kafka adapters, cache adapters, metrics, tests, and local configuration.
- Local Docker Compose dependencies for Oracle, Redis, Kafka, and Kafka UI.
- Detailed developer onboarding guide under `docs/developer-guide.md`.
- Sample OpenAPI under `docs/openapi/sample-order-api.yaml`.
- Scripts for rendering a new service, starting local dependencies, running tests, and checking the local environment.

Use the template to learn the approved wiring. Do not keep sample names, sample topics, sample data, or sample business behavior in a real service.

## 2. Create a New Service

### Windows PowerShell

Run this from `product-catalog-service`:

```powershell
.\scripts\render-template.ps1 -ServiceName "payment-service" -BasePackage "com.viettel.bccs" -ServicePackage "payment" -OutputPath "..\payment-service"
```

### Linux, macOS, or Git Bash

Run this from `product-catalog-service`:

```bash
./scripts/create-service.sh payment-service com.viettel.bccs.payment ../payment-service
```

Then build the generated service:

```bash
cd ../payment-service
mvn clean install
```

## 3. Rename Checklist

Check these items before the first commit of a generated service.

### artifactId

In `pom.xml`, replace the template artifact:

```xml
<artifactId>payment-service</artifactId>
<name>payment-service</name>
<description>Payment service</description>
```

### package

Move Java code from the template package to your service package:

```text
src/main/java/com/viettel/bccs/payment
src/test/java/com/viettel/bccs/payment
```

Then replace imports and package declarations:

```text
com.viettel.bccs.productcatalog -> com.viettel.bccs.payment
```

### application name

Set the Spring application name in `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: ${APP_NAME:payment-service}
```

Use the same name in `.env`, deployment values, logging dashboards, Kafka topic prefixes, cache key prefixes, and alert names.

### main application class

- Run `mvn clean install` to format code and run tests.
- Execute `ProductCatalogServiceApplication.java` from your IDE.

## Try the Redis Sample

A `SampleRedisController` is provided to demonstrate interacting with Redis using `BccsRedisService`.

Endpoints (make sure `bccs.template.sample.redis.enabled=true`):
- `POST /sample-redis/value/{key}`
- `GET /sample-redis/value/{key}`
- `POST /sample-redis/list/{key}/left`
- `GET /sample-redis/hash/{key}/{hashKey}`

See `SampleRedisController` for full capabilities.

For comprehensive guidelines on using Redis in BCCS (including configuration, TTL policies, and API usage), refer to the [Redis Enterprise Usage Guide](docs/redis.md).

Rename the main class to match the service:

```text
ProductCatalogServiceApplication.java -> PaymentServiceApplication.java
```

Keep it at the root of the service package so Spring component scanning covers every feature package.

## 4. Run Locally

### Build

```bash
mvn clean install
```

### Start local infrastructure

Copy the local environment file if needed:

```bash
cp .env.example .env
```

Start Oracle, Redis, Kafka, and Kafka UI:

```bash
docker compose -f docker-compose.local.yml --env-file .env up -d
```

Kafka UI is available at:

```text
http://localhost:8085
```

### Run with Maven Spring Boot

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

On Windows PowerShell, use:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

### Try the sample REST API

```bash
curl -X POST http://localhost:8080/api/v1/sample-orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST-1","productCode":"PROD-A","quantity":1,"channel":"WEB"}'
```

```bash
curl http://localhost:8080/api/v1/sample-orders/order-123
```

These endpoints are sample endpoints only. Replace them with your service API before implementation starts.

## 5. Optional Configuration & Run Modes

The template supports local run modes for teams that do not need every infrastructure component on day one.

The examples below are local-only. They use `localhost` and the Docker Compose services in this repository. Do not put production URLs, passwords, tokens, or broker addresses in template files.

The sample `SampleOrder` flow still uses Oracle/JPA, so keep the local database running unless you remove the sample persistence code.

### Cache Modes

Configure `bccs.cache.mode` in `application.yml`, `application-local.yml`, environment variables, or Maven run arguments:

- `memory-only`: Uses local in-memory caching. Does not require Redis.
- `redis-only`: Uses centralized Redis caching. Redis must be running locally.
- `two-level`: Uses memory cache backed by Redis. Redis must be running locally.
- Cache disabled: the starter supports `bccs.cache.enabled=false`, but the template sample cache adapter injects `BccsCacheService`. To run with cache disabled, first remove the sample cache adapter and any cache usage from your service code.

Local memory cache:

```yaml
bccs:
  cache:
    enabled: true
    mode: memory-only
```

Local Redis cache:

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

bccs:
  cache:
    enabled: true
    mode: redis-only
```

Local two-level cache:

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

bccs:
  cache:
    enabled: true
    mode: two-level
    invalidation:
      enabled: true
      topic: ${BCCS_CACHE_INVALIDATION_TOPIC:${spring.application.name}.cache.invalidation}
```

Redis requirements:

- `redis-only` and `two-level` require Redis to be running before the app uses the cache.
- Required local properties are `REDIS_HOST` and `REDIS_PORT`; both default to `localhost` and `6379` in the local profile.
- Do not configure Redis passwords or cluster nodes in the template. Add environment-specific values outside source control.

### Kafka

Configure `bccs.kafka.enabled` in `application.yml`, `application-local.yml`, environment variables, or Maven run arguments:

- `bccs.kafka.enabled=false`: Disables the BCCS Kafka auto-configuration. The sample `NoopSampleOrderEventPublisher` handles the sample publish call without sending to Kafka. The sample listener is also conditional and will not start.
- `bccs.kafka.enabled=true`: Enables BCCS Kafka publishing and sample listener code. Kafka must be running locally.

Local Kafka disabled:

```yaml
bccs:
  kafka:
    enabled: false
```

Local Kafka enabled:

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

Kafka requirements:

- `KAFKA_BOOTSTRAP_SERVERS` is required only when `bccs.kafka.enabled=true`.
- When `bccs.kafka.enabled=false`, the sample code uses `NoopSampleOrderEventPublisher`; no event is published.
- If your real service disables Kafka, keep your own Kafka publishers/listeners conditional or remove them.

### Local Run Examples

PowerShell examples:

Run without Kafka:

```powershell
docker compose -f docker-compose.local.yml --env-file .env up -d database
mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--bccs.kafka.enabled=false --bccs.cache.mode=memory-only"
```

Run with memory cache:

```powershell
docker compose -f docker-compose.local.yml --env-file .env up -d database
mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--bccs.kafka.enabled=false --bccs.cache.enabled=true --bccs.cache.mode=memory-only"
```

Run with Redis cache:

```powershell
docker compose -f docker-compose.local.yml --env-file .env up -d database redis
mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--bccs.kafka.enabled=false --bccs.cache.enabled=true --bccs.cache.mode=redis-only --spring.data.redis.host=localhost --spring.data.redis.port=6379"
```

Run with Kafka enabled:

```powershell
docker compose -f docker-compose.local.yml --env-file .env up -d database redis kafka kafka-ui
mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--bccs.kafka.enabled=true --spring.kafka.bootstrap-servers=localhost:9092 --bccs.cache.mode=memory-only"
```

Bash examples use the same arguments without PowerShell quoting:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.arguments="--bccs.kafka.enabled=false --bccs.cache.mode=memory-only"
```

```bash
docker compose -f docker-compose.local.yml --env-file .env up -d database redis
mvn spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.arguments="--bccs.kafka.enabled=false --bccs.cache.mode=redis-only --spring.data.redis.host=localhost --spring.data.redis.port=6379"
```

```bash
docker compose -f docker-compose.local.yml --env-file .env up -d database redis kafka kafka-ui
mvn spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.arguments="--bccs.kafka.enabled=true --spring.kafka.bootstrap-servers=localhost:9092 --bccs.cache.mode=memory-only"
```

### Troubleshooting

- **Cannot connect to Redis**: You configured `bccs.cache.mode=redis-only` or `bccs.cache.mode=two-level`, but Redis is not running or `REDIS_HOST` / `REDIS_PORT` points to the wrong place. Start Redis with `docker compose -f docker-compose.local.yml --env-file .env up -d redis`, or switch to `bccs.cache.mode=memory-only`.
- **Kafka bootstrap missing**: You enabled Kafka but did not provide a reachable bootstrap server. Start Kafka with `docker compose -f docker-compose.local.yml --env-file .env up -d kafka kafka-ui` and use `--spring.kafka.bootstrap-servers=localhost:9092`, or run with `--bccs.kafka.enabled=false`.
- **No bean named BccsEventPublisher**: `bccs.kafka.enabled=false` disables the platform `BccsEventPublisher` bean. The template sample avoids this by using `NoopSampleOrderEventPublisher`. For real code, either remove Kafka publishers when Kafka is not needed, or make publisher adapters conditional with `@ConditionalOnProperty(prefix = "bccs.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)`.
- **Kafka listener starts even when disabled**: The sample listener is conditional on `bccs.kafka.enabled=true`. If your service adds a new `@KafkaListener`, apply the same conditional or remove the listener when Kafka is disabled.
- **Error registry Redis unavailable**: Keep `bccs.error.registry.fallback.enabled=true`. The service still resolves errors from the fallback file and static common definitions. Redis is needed only for live centralized snapshot sync, not for every error response.

## 6. Dependency Rules

Use BCCS starters only. Do not declare platform core libraries or hidden transitive dependencies directly in a service.

Allowed pattern:

```xml
<dependency>
    <groupId>com.viettel.bccs</groupId>
    <artifactId>bccs-starter-service-base</artifactId>
</dependency>
```

Current template starters:

- `bccs-starter-service-base` for web, validation/error handling, centralized error-code registry sync through `bccs-starter-web`, security, logging/tracing, and observability integration.
- `bccs-starter-data` for repository and transaction integration.
- `bccs-starter-kafka` for Kafka producer/consumer integration.
- `bccs-starter-cache` for BCCS cache integration.
- `bccs-starter-test` for test utilities and architecture rules.

Do not add direct dependencies such as:

- Spring Boot web/data/security/Kafka/cache starters.
- `spring-kafka`, `KafkaTemplate`, Redis clients, Caffeine, Micrometer, OpenTelemetry, HTTP clients, or JSON libraries.
- `WebFlux`, `WebClient`, `Mono`, `Flux`, or reactive database drivers.

If a required capability is not available through a BCCS starter, request a platform change instead of bypassing the starter boundary.

## 7. Sample Tech Stack Guide

All classes in `com.viettel.bccs.productcatalog` are sample code. Use them as examples, then replace them with business-specific names and behavior.

### REST API

Sample files:

- `sampleorder/controller/SampleOrderController.java`
- `sampleorder/dto/request/CreateSampleOrderRequest.java`
- `sampleorder/dto/response/SampleOrderResponse.java`

Pattern:

- Controllers expose versioned REST paths such as `/api/v1/...`.
- Controllers call application use cases, not repositories or infrastructure adapters.
- Responses use the platform wrapper `StandardResponse<T>` generated via the `StandardResponses.success()` helper.
- `GET` endpoints return `StandardResponse<T>` (implies HTTP 200).
- `POST` endpoints return `ResponseEntity<StandardResponse<T>>` with HTTP 201 Created.

Production action:

- Replace `SampleOrderController` with controllers for your business API.
- Keep request/response DTOs at the API boundary.
- Keep business rules out of controllers.

### validation/error

Sample file:

- `sampleorder/dto/request/CreateSampleOrderRequest.java`
- `sampleorder/exception/SampleErrorCode.java`

Pattern:

- Use `jakarta.validation` annotations such as `@NotBlank`, `@NotNull`, and `@Min`.
- Let the BCCS web/error stack convert validation and application errors into the standard `ErrorResponse` API response.
- Throw `BusinessException` with a domain-specific `ErrorCode` (e.g., `SampleErrorCode`) for business logic failures.
- Throw `SystemException` for unexpected technical failures.
- Do NOT throw raw `IllegalArgumentException` or `RuntimeException`.

Production action:

- Replace sample validation messages with business field validation.
- Define a central `ErrorCode` enum for your service.
- Do not create custom exception response formats or `ErrorResponse` manually in each controller.
- For a comprehensive guide on responses and exceptions, refer to the [BCCS API Response & Exception Guideline](../../docs/api-response-exception-guideline.md).

### sampleorder layered feature

Sample files:

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

- `model` contains persistence-independent business state.
- `service` owns business orchestration and transaction boundaries.
- `repository`, `client`, `cache`, and `event` contain dedicated technical responsibilities inside the feature.
- The concrete feature repository wraps Spring Data and maps entities to models.

Production action:

- Keep interfaces only for multiple implementations, configuration selection, or meaningful external boundaries.
- Map between models, persistence entities, and response DTOs in the feature mapper.
- Do not inject Spring Data repositories into controllers or expose entities.

### transaction

Sample file:

- `sampleorder/service/SampleOrderService.java`

Pattern:

- Put `@Transactional` on application service methods that own a business operation.
- Do not start transactions in REST controllers.

Production action:

- Keep transaction boundaries around one use case.
- Avoid long transactions that include remote HTTP calls unless the platform architecture explicitly approves the flow.

### cache

Sample file:

- `sampleorder/cache/SampleOrderCache.java`

Sample config:

- `bccs.cache` in `src/main/resources/application.yml`
- local override in `src/main/resources/application-local.yml`

Pattern:

- Use `BccsCacheService`.
- Use service-specific cache names and keys.
- Configure mode (`memory-only`, `redis-only`, `two-level`) and TTL through `bccs.cache`, not direct Redis or Caffeine code.
- Note: `redis-only` and `two-level` modes require a running Redis instance (use the local Docker Compose for development).

Production action:

- Replace `sample-orders` cache names and `order:*` keys.
- Remove cache code entirely if your service does not need caching.
- Do not inject `RedisTemplate`, native Redis clients, or Caffeine directly.

### outbound HTTP client

Sample files:

- `sampleorder/client/CustomerClient.java`
- `sampleorder/client/BccsCustomerClient.java`
- `sampleorder/client/CustomerResponse.java`

Sample config:

- `bccs.client.clients.customer-service` in `application-local.yml`

Pattern:

- Application code depends on a client port.
- Infrastructure code uses `BccsHttpClient`.
- The template is synchronous/blocking. It does not use `WebClient`, `Mono`, or `Flux`.

Production action:

- Rename `customer-service` to the real downstream service key.
- Replace sample URLs and DTOs.
- Do not use raw HTTP clients or reactive clients.

### Kafka

Sample files:

- `sampleorder/event/SampleOrderEventPublisher.java`
- `sampleorder/event/SampleOrderCreatedEvent.java`
- `sampleorder/event/KafkaSampleOrderEventPublisher.java`
- `sampleorder/event/SampleOrderEventConsumer.java`

Sample config:

- `bccs.kafka` in `application.yml`
- `bccs.sample-order.created` and `bccs.sample-order.dlq` in `application-local.yml`

Pattern:

- Publish through `BccsEventPublisher`.
- Keep Kafka-specific code in infrastructure adapters.
- Consumers use Spring Kafka listener annotations as shown by the sample.
- Topic names come from configuration.

Production action:

- Replace sample topic names before connecting to shared Kafka.
- Remove the sample consumer if your service only produces events.
- Remove the sample producer if your service only consumes events.
- Do not use raw Kafka clients in business logic.
- To completely disable Kafka, set `bccs.kafka.enabled=false`. The template provides a `NoopSampleOrderEventPublisher` fallback so the service can still start without requiring Kafka bootstrap servers.

### security

Sample files/config:

- `sampleorder/controller/SampleOrderController.java`
- `sampleorder/controller/SampleOrderControllerSecurityTest.java`
- `bccs.security` in `application.yml`

Pattern:

- Public paths are configured under `bccs.security.public-paths`.
- Method-level authorization can use Spring Security annotations such as `@PreAuthorize`.
- Local profile disables JWT by default for easier local development.

Production action:

- Replace `SAMPLE_ORDER_DELETE` with real service authorities.
- Enable and validate the production JWT/security configuration before deployment.
- Do not add service-specific security filters unless the platform team approves them.

### logging/tracing

Sample config:

- `logging.level` and `bccs.logging` in `application.yml`
- local request logging override in `application-local.yml`

Pattern:

- Use normal SLF4J logging in application code.
- Let the BCCS logging starter manage MDC, request context, masking, and request/response logging.
- Request body logging is disabled by default; keep it disabled unless explicitly approved for a safe local/debug scenario.

Production action:

- Do not log secrets, tokens, passwords, customer identifiers, or payloads containing sensitive data.
- Use `spring.application.name` consistently so logs, traces, metrics, and dashboards group correctly.

### observability/metrics

Sample files/config:

- `sampleorder/metrics/SampleOrderMetrics.java`
- `sampleorder/metrics/SampleOrderMetrics.java`
- `management.endpoints.web.exposure.include`
- `bccs.observability`

Pattern:

- Actuator endpoints expose health, info, metrics, and Prometheus according to configuration.
- Infrastructure adapters may use Micrometer through the platform starter.
- Application services depend on a metrics port instead of `MeterRegistry`.

Production action:

- Rename sample metric names to your service vocabulary.
- Keep cardinality low; do not tag metrics with unbounded IDs such as customer ID, order ID, token, or request payload.

### resilience/idempotency

There is no completed resilience or idempotency starter in this template.

Production action:

- Do not add direct Resilience4j, retry, circuit-breaker, or idempotency-lock dependencies.
- Do not document a service as idempotent just because a request has an ID field.
- If your service needs retries, circuit breakers, rate limits, deduplication, or idempotency keys, request a platform change.

### OpenAPI

Sample file:

- `docs/openapi/sample-order-api.yaml`

Pattern:

- Keep API contracts in `docs/openapi`.
- Keep examples aligned with controller paths and DTO fields.

Production action:

- Replace `sample-order-api.yaml` with your service contract.
- Remove sample paths before publishing API documentation.

### tests

Sample files:

- `src/test/java/com/viettel/bccs/template/sampleorder/controller/*Test.java`
- `src/test/java/com/viettel/bccs/template/sampleorder/service/*Test.java`
- `src/test/java/com/viettel/bccs/template/sampleorder/*Test.java`
- `src/test/java/com/viettel/bccs/template/architecture/LayeredArchitectureTest.java`

Pattern:

- Controller tests cover REST behavior and validation.
- Service tests cover business orchestration with focused collaborators.
- Repository, mapper, client, cache, event, and metrics tests cover their concrete boundaries.
- Architecture tests enforce layered direction, feature isolation, service transactions, approved Redis usage, and no WebFlux.

Production action:

- Rename tests with the service package.
- Keep architecture tests.
- Delete sample tests when deleting sample code.
- Add service-specific tests before replacing the sample flow.

Run tests:

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

## 8. Remove Before Real Implementation

Remove or replace all sample-only assets before the service is considered implementation-ready.

### sample package

Delete or replace:

```text
src/main/java/com/viettel/bccs/template
src/test/java/com/viettel/bccs/template
```

After rendering a new service, delete or replace the same copied `SampleOrder*` classes under your generated package.

### sample config

Delete or replace:

```yaml
bccs:
  sample-order:
    created: product-catalog-service.sample-order.created
    dlq: product-catalog-service.sample-order.dlq
```

Delete or replace the sample downstream client:

```yaml
bccs:
  client:
    clients:
      customer-service:
        base-url: http://localhost:8081
```

### sample topics

Replace:

```text
product-catalog-service.sample-order.created
product-catalog-service.sample-order.dlq
```

Use topic names approved for your service and environment.

### sample local data

Remove or replace local-only names and defaults:

- Sample API paths under `/api/v1/sample-orders`.
- Sample cache names such as `sample-orders`.
- Sample table names such as `sample_orders`.
- Sample metric names from `SampleOrderMetrics`.
- Sample OpenAPI paths in `docs/openapi/sample-order-api.yaml`.
- Sample local downstream URL `CUSTOMER_SERVICE_URL=http://localhost:8081`.

Do not commit real secrets into `.env`, YAML files, OpenAPI examples, logs, or test fixtures.

## 9. Request Platform Changes

Open a platform request when your service needs a capability that is not exposed by a BCCS starter.

Include:

- Business service name and owner.
- Business requirement.
- Missing platform capability.
- Proposed behavior and operational requirements.
- Security, logging, tracing, metrics, and deployment impact.
- Whether the capability is needed for local development, production runtime, tests, or CI/CD.

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

## 10. Outbound Client Samples

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

