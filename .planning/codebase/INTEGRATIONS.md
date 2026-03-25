# External Integrations

**Analysis Date:** 2026-03-25

## APIs & External Services

**Email Sending:**
- Spring Boot Mail - Transactional email delivery
  - Implementation: `spring-boot-starter-mail`
  - Config: SMTP settings via Spring Mail properties

**SMS Sending:**
- sms4j (3.3.3) - Multi-provider SMS service
  - Supported providers: Aliyun, Tencent Cloud, and others
  - Config: Provider-specific credentials in application config
  - Implementation: `modules/captcha/service/SmsCaptchaService.java`

**Object Storage:**
- Aliyun OSS (Alibaba Cloud Object Storage Service) - Optional file storage
  - SDK/Client: `aliyun-sdk-oss:3.18.1`
  - Auth: `oss.aliyun.access-key-id`, `oss.aliyun.access-key-secret`
  - Config: `oss.storage-type` (local/aliyun), endpoint, bucket name
  - Implementation: `modules/oss/service/impl/AliyunOssServiceImpl.java`
  - Fallback: Local filesystem storage (`./uploads` directory)

## Data Storage

**Databases:**
- MySQL 8.x - Primary relational database
  - Connection: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars
  - Default: `jdbc:mysql://localhost:3306/aiperm`
  - Client: Spring Data JPA + Hibernate ORM
  - Migrations: Flyway (scripts in `backend/src/main/resources/db/migration/`)
  - Connection pooling: HikariCP (Spring Boot default)

**File Storage:**
- Local filesystem (default) - Files stored in `./uploads` directory
- Aliyun OSS (optional) - Cloud object storage
  - Config: `application-dev.yaml` or `application-prod.yaml`
  - Access URL prefix configurable

**Caching:**
- Redis 7.x - Session storage, permission caching, CAPTCHA
  - Connection: `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`, `REDIS_DATABASE`
  - Default: `localhost:6379`
  - Client: Spring Data Redis with Lettuce
  - Pooling: Apache Commons Pool2
  - Used by: Sa-Token for session management

## Authentication & Identity

**Auth Provider:**
- Sa-Token (Custom implementation) - RBAC authentication and authorization
  - Implementation: `modules/auth/`, Spring Boot 3 starter
  - Features:
    - Login/logout with JWT-style tokens
    - Role-based access control (RBAC)
    - Permission annotations (`@SaCheckPermission`)
    - Multi-device login support
    - Session timeout and active timeout
    - Redis-backed session storage
  - Token storage: Redis
  - Token name: `Authorization` (configurable)
  - Token timeout: 30 days (configurable)
  - Active timeout: 30 minutes (configurable)

**Multi-Factor Authentication (MFA):**
- Custom MFA implementation - TOTP (Time-based One-Time Password)
  - Implementation: `modules/mfa/`
  - Supported: TOTP apps (Google Authenticator, Authy, etc.)

**OAuth 2.0 Integration:**
- Custom OAuth implementation - Third-party login support
  - Implementation: `modules/oauth/`
  - Supported providers: Extensible (Gitea, GitHub, etc.)

## Monitoring & Observability

**Error Tracking:**
- None (no external service integration)

**Logs:**
- Approach: Logback with file rolling
  - Development: Console logging with debug level
  - Production: File logging to `/var/log/aiperm/aiperm.log`
  - Rolling policy: Daily, max 100MB per file, 14 days retention, 2GB total cap
  - Trace/Span ID support: Distributed tracing context in logs

**Metrics:**
- Prometheus - Metrics export
  - Endpoint: `/actuator/prometheus`
  - Registry: `micrometer-registry-prometheus`
  - Config: `management.otlp.metrics.export.enabled` env var

**Distributed Tracing:**
- OpenTelemetry - OTLP export support
  - Sampling: Configurable probability (dev: 0%, prod: 5%)
  - Config: `MANAGEMENT_OTLP_TRACING_ENABLED` env var
  - Export: OTLP protocol (disabled by default)

**Health Checks:**
- Spring Boot Actuator - Application health monitoring
  - Endpoints: `/actuator/health`, `/actuator/info`, `/actuator/metrics`
  - Kubernetes probes: readiness and liveness enabled

## CI/CD & Deployment

**Hosting:**
- Self-hosted (no cloud provider lock-in)
- Supports containerization via GraalVM native image

**CI Pipeline:**
- GitHub Actions - `.github/workflows/ci.yml`
  - Purpose: Automated testing and build verification

## Environment Configuration

**Required env vars:**

**Database:**
- `DB_URL` - MySQL connection URL (default: `jdbc:mysql://localhost:3306/aiperm?...`)
- `DB_USERNAME` - Database user (default: `root`)
- `DB_PASSWORD` - Database password

**Redis:**
- `REDIS_HOST` - Redis host (default: `localhost`)
- `REDIS_PORT` - Redis port (default: `6379`)
- `REDIS_PASSWORD` - Redis password (default: empty)
- `REDIS_DATABASE` - Redis database number (default: `0`)

**Spring Profile:**
- `SPRING_PROFILES_ACTIVE` - Active profile (dev/prod, default: `dev`)

**Flyway (optional):**
- `FLYWAY_URL` - Override database URL for migrations
- `FLYWAY_USER` - Override database user for migrations
- `FLYWAY_PASSWORD` - Override database password for migrations

**Monitoring (optional):**
- `MANAGEMENT_TRACING_SAMPLING` - Tracing sampling probability
- `MANAGEMENT_OTLP_TRACING_ENABLED` - Enable OTLP tracing export
- `MANAGEMENT_OTLP_METRICS_ENABLED` - Enable OTLP metrics export

**Secrets location:**
- Environment variables (recommended for production)
- Application YAML files (for development only)
- No hardcoded secrets in codebase

## Webhooks & Callbacks

**Incoming:**
- None (no webhook endpoints configured)

**Outgoing:**
- None (no external webhook calls)

## API Documentation

**Swagger/OpenAPI:**
- SpringDoc OpenAPI 3.0.1 - Interactive API documentation
  - UI: `http://localhost:8080/swagger-ui.html`
  - OpenAPI JSON: `http://localhost:8080/v3/api-docs`
  - Used by: Orval for frontend API client generation

---

*Integration audit: 2026-03-25*
