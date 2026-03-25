# Technology Stack

**Analysis Date:** 2026-03-25

## Languages

**Primary:**
- Java 25 - Backend application logic, Spring Boot framework
- TypeScript 5.9.x - Frontend application logic, type safety

**Secondary:**
- SQL - Database migrations (Flyway)
- Vue SFC (.vue) - Frontend component templates

## Runtime

**Environment:**
- Backend: Java 25 (with virtual threads enabled)
- Frontend: Node.js (via pnpm), Vite dev server

**Package Manager:**
- Backend: Gradle 8.14.4
- Frontend: pnpm (lockfile present: `pnpm-lock.yaml`)

## Frameworks

**Core:**
- Spring Boot 4.0.3 - Backend REST API framework
- Vue 3.5.25 - Frontend UI framework
- Sa-Token 1.44.0 - Authentication and authorization (RBAC)
- Spring Data JPA - ORM for database operations
- Hibernate - JPA implementation

**Testing:**
- JUnit Platform (via `spring-boot-starter-test`)
- Spring Boot Test

**Build/Dev:**
- Vite 7.3.1 - Frontend build tool and dev server
- Gradle - Backend build automation
- Flyway 11.7.2 - Database migration management
- GraalVM Native Image 0.10.6 - Native image compilation support

## Key Dependencies

**Critical:**
- Sa-Token (1.44.0) - Permission authentication, session management, Redis integration
- Spring Data JPA - Database access layer
- Hutool 5.8.34 - Java utility library
- Element Plus 2.13.2 - Vue 3 UI component library
- Pinia 3.0.4 - Vue state management
- Vue Router 5.0.3 - Frontend routing
- Orval 7.13.2 - OpenAPI client generation for frontend
- Axios 1.13.5 - HTTP client

**Infrastructure:**
- MySQL Connector J 9.6.0 - MySQL database driver
- Spring Data Redis - Redis integration for caching/sessions
- Apache Commons Pool2 - Redis connection pooling
- Flyway Core & MySQL - Database schema migrations
- SpringDoc OpenAPI 3.0.1 - API documentation (Swagger UI)
- Spring Boot Actuator - Application monitoring and health checks
- Micrometer Prometheus - Metrics export
- Spring Boot Starter Mail - Email sending
- sms4j 3.3.3 - SMS sending (multi-provider: Aliyun, Tencent Cloud)
- Aliyun OSS SDK 3.18.1 - Object storage integration

**Frontend Build:**
- UnoCSS 66.6.0 - Atomic CSS framework
- TypeScript ESLint 8.56.1 - Linting
- Vue TSC 3.1.5 - TypeScript type checking for Vue
- @ai-sdk/vue 2.0.141 - AI SDK integration
- page-agent 1.6.1 - Page agent framework
- zod 4.3.6 - Schema validation

**Utilities:**
- VueUse 14.2.1 - Vue composition utilities
- pinia-plugin-persistedstate 4.7.1 - Pinia state persistence
- @element-plus/icons-vue 2.3.2 - Element Plus icons

## Configuration

**Environment:**
- Backend: Spring profiles (dev, prod) via `application.yaml`, `application-dev.yaml`, `application-prod.yaml`
- Frontend: Vite environment variables, no `.env` files (defaults in `vite.config.ts`)
- Environment variable overrides supported for database and Redis connections

**Key configs required:**
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` - Database connection
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`, `REDIS_DATABASE` - Redis connection
- `SPRING_PROFILES_ACTIVE` - Active profile (dev/prod)

**Build:**
- Backend: `backend/build.gradle` - Gradle build configuration
- Frontend: `frontend/vite.config.ts` - Vite build configuration
- Frontend: `frontend/tsconfig.json` - TypeScript configuration
- Frontend: `frontend/orval.config.ts` - API client generation config
- Frontend: `frontend/eslint.config.js` - ESLint configuration
- Frontend: `frontend/uno.config.ts` - UnoCSS configuration

## Platform Requirements

**Development:**
- Java 25+ with toolchain support
- Node.js 18+ (for pnpm)
- MySQL 8.x
- Redis 7.x
- pnpm package manager

**Production:**
- Linux server (e.g., Ubuntu, CentOS)
- Java 25 runtime environment
- MySQL 8.x database
- Redis 7.x for session caching
- Optional: Aliyun OSS for file storage
- Optional: Prometheus for metrics collection
- Optional: OTLP-compatible tracing backend

---

*Stack analysis: 2026-03-25*
