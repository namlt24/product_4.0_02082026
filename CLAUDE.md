# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

This is a multi-service BCCS (Viettel) microservice platform containing 6 independent Spring Boot services, each in its own directory. All services are generated from the same `product-catalog-service` template and share the same architecture patterns.

### Services

| Service | Description |
|---------|-------------|
| `product-catalog-service` | Product catalog management (template source) |
| `product-policy-service` | Product policy configuration |
| `product-area-service` | Product area management |
| `product-price-service` | Product pricing |
| `organization-resource-service` | Organization resource management |
| `spec-common-service` | Common specifications |

Each service is an independent Git repository. There is no parent Maven POM at the root — each service has its own `pom.xml` and Maven Wrapper.

## Development Commands

Run from each service directory.

**Build and test:**
```bash
mvn clean install
mvn clean verify        # full verify including integration tests
mvn test                # unit tests only
```

**Run a specific test class:**
```powershell
.\mvnw.cmd "-Dtest=LayeredArchitectureTest" test
```

**Run locally (from service directory):**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

PowerShell on Windows:
```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

**Start local infrastructure (from service directory):**
```bash
docker compose -f docker-compose.local.yml --env-file .env up -d
```

Each service has its own `docker-compose.local.yml` and `.env.example`. Copy to `.env` and configure before starting Docker.

**Run with minimal dependencies (no Kafka, memory-only cache):**
```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--bccs.kafka.enabled=false --bccs.cache.mode=memory-only"
```

**Create a new service from template** (run from `product-catalog-service`):
```powershell
.\scripts\render-template.ps1 -ServiceName "my-service" -BasePackage "com.viettel.bccs" -ServicePackage "myservice" -OutputPath "..\my-service"
```
Linux/macOS:
```bash
./scripts/create-service.sh my-service com.viettel.bccs.myservice ../my-service
```

## Architecture

### Layered Package-by-Feature

Each service follows the same structure. Within `com.viettel.bccs.<package>`:

```
<feature>/
├── controller         # REST endpoints, validation, response wrapping
├── dto/request        # Request DTOs with validation annotations
├── dto/response       # Response DTOs
├── service            # Business logic, orchestration, @Transactional
├── repository         # Persistence access (wraps Spring Data JPA)
├── entity             # JPA entities
├── model              # Domain models (persistence-independent)
├── mapper             # Entity <-> Model <-> DTO conversions
├── client             # Outbound HTTP/SOAP calls
├── cache              # Caching logic via BccsCacheService
├── event              # Kafka publishers/consumers
└── metrics            # Custom business metrics
```

**Dependency rule: `controller -> service -> repository/client/cache/event/mapper`**

### Key Constraints

- **Use BCCS starters only.** Never declare Spring Boot starters, `spring-kafka`, Redis clients, Caffeine, Micrometer, OpenTelemetry, or HTTP clients directly. All are provided by `bccs-starter-service-base`, `bccs-starter-data`, `bccs-starter-kafka`, `bccs-starter-cache`, `bccs-starter-redis`.
- **No reactive code.** No `WebFlux`, `WebClient`, `Mono`, `Flux`, or reactive database drivers. The platform is strictly imperative/synchronous.
- **No raw exceptions.** Throw `BusinessException` or `SystemException` from `com.viettel.bccs.common.error.exception`, not raw `RuntimeException`.
- **Keep `@Transactional` on service methods**, not controllers.
- **Keep DTOs at the API boundary.** Never return JPA entities from controllers or expose them in responses.
- **`LayeredArchitectureTest`** (ArchUnit) enforces these rules — it must pass before merging.

### Platform APIs

- **API response wrapper:** Use `StandardResponse<T>` via `StandardResponses.success()` or `BccsResponseFactory`.
- **HTTP client:** Use `BccsHttpClient` or `BccsRestClientFactory` — do not use raw HTTP clients.
- **Cache:** Use `BccsCacheService` or `BccsRedisService` — do not inject `RedisTemplate` or native Redis clients.
- **Kafka:** Publish through `BccsEventPublisher`; keep Kafka-specific code in the `event` package.
- **Logging/tracing:** Use SLF4J with `@Slf4j`. Tracing and MDC are managed by the BCCS starter — do not add custom filters.
- **Configuration:** Use `bccs.config.team-code` (e.g., ORDER, POLICY) for centralized error code registry. Configure `bccs.config.service-name` to match `spring.application.name`.

### Error Handling

- Validation errors: use `@Valid` annotations on request DTOs — the BCCS starter handles conversion to `ErrorResponse`.
- Business errors: `throw new BusinessException("BCCS-TEAM-XXX", "message")`.
- Integration errors: `throw new IntegrationException(...)`.
- System errors: `throw new SystemException(...)` or let generic `RuntimeException` propagate.

### Cache Modes

Configure `bccs.cache.mode` via CLI arg, YAML, or env var:
- `memory-only` — local Caffeine cache, no Redis needed.
- `redis-only` — centralized Redis cache, requires Redis running.
- `two-level` — local cache backed by Redis.

### Kafka

Configure `bccs.kafka.enabled` — set to `false` for local dev without Kafka. The template provides a no-op publisher fallback.

## Repository Model

- **`bccs-platform`** (not cloned): Core BOM, parent POM, starters. Consumed from Nexus only.
- **`product-catalog-service`**: Template source for scaffolding new services.
- **Each service directory**: Independent service repo. Clone only the one you work on.
- **`bccs-reference-services`**: Reference implementations showing real business features.

## Prerequisites

- JDK 25+
- Maven 3.9+ (or Maven Wrapper included in each service)
- Docker for local infrastructure (Oracle, Redis, Kafka, Kafka UI)
- `~/.m2/settings.xml` pointing to `https://nexus.kcntt.net/repository/maven-group/` as mirror

## Service-Specific Files

Each service directory contains:
- `pom.xml` — inherits from `com.viettel.bccs:bccs-build-parent`
- `docker-compose.local.yml` — Oracle, Redis, Kafka, Kafka UI
- `.env.example` — copy to `.env` and configure before Docker startup
- `src/main/resources/application.yml` — default config
- `src/main/resources/application-local.yml` — local overrides
- `src/main/resources/bccs-error-codes-fallback.json` — error code fallback
- `src/main/java/com/viettel/bccs/<package>/` — Java source
- `src/test/java/com/viettel/bccs/<package>/` — tests including `LayeredArchitectureTest`

## Before First Commit (for a new service)

1. Rename artifact, name, description in `pom.xml`
2. Move code from `com.viettel.bccs.productcatalog` to your service package
3. Update `spring.application.name` and `bccs.config.service-name`
4. Rename main application class
5. Configure `bccs.config.team-code` for your team
6. Uncomment and update `LayeredArchitectureTest` rules
7. Remove all sample code (the `sampleorder`, `sampleredis`, `sampleerror`, `outboundsample` packages)
8. Run `mvn clean install` — must compile and pass all tests