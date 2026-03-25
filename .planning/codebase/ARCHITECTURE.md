# Architecture

**Analysis Date:** 2026-03-25

## Pattern Overview

**Overall:** Layered MVC Architecture with Frontend-Backend Separation

**Key Characteristics:**
- Backend follows classic 3-tier architecture: Controller → Service → Repository → Entity
- Frontend uses Vue 3 Composition API with Pinia state management
- JWT-based authentication with role-based access control (RBAC)
- RESTful API design with OpenAPI/Swagger documentation
- Soft delete pattern for data integrity
- Database version control via Flyway migrations

## Layers

### Backend Layer Structure

**Controller Layer:**
- Purpose: HTTP request handling, authentication/authorization, request validation
- Location: `backend/src/main/java/com/devlovecode/aiperm/modules/*/controller/`
- Contains: REST endpoints with Sa-Token annotations
- Depends on: Service layer
- Used by: Frontend API clients

**Service Layer:**
- Purpose: Business logic, transaction management, data transformation
- Location: `backend/src/main/java/com/devlovecode/aiperm/modules/*/service/`
- Contains: Business rules, entity-to-VO mapping
- Depends on: Repository layer, common services
- Used by: Controller layer

**Repository Layer:**
- Purpose: Data access abstraction using Spring Data JPA
- Location: `backend/src/main/java/com/devlovecode/aiperm/modules/*/repository/`
- Contains: JPA repositories extending `BaseJpaRepository`
- Depends on: Entity classes, JPA/Hibernate
- Used by: Service layer

**Entity Layer:**
- Purpose: Database table mapping with soft delete support
- Location: `backend/src/main/java/com/devlovecode/aiperm/modules/*/entity/`
- Contains: JPA entities extending `BaseEntity`
- Depends on: Jakarta Persistence API
- Used by: Repository layer

### Frontend Layer Structure

**View Layer:**
- Purpose: UI components and page templates
- Location: `frontend/src/views/`, `frontend/src/components/`
- Contains: Vue 3 SFC components
- Depends on: Stores, API clients, Element Plus
- Used by: Router

**State Management Layer:**
- Purpose: Global state and caching
- Location: `frontend/src/stores/`
- Contains: Pinia stores (user, permission, app)
- Depends on: API clients, localStorage
- Used by: Components

**API Layer:**
- Purpose: Typed HTTP client functions
- Location: `frontend/src/api/`
- Contains: Auto-generated API functions from Orval
- Depends on: Axios, backend API
- Used by: Stores and components

**Router Layer:**
- Purpose: Navigation and route guards
- Location: `frontend/src/router/`
- Contains: Vue Router configuration
- Depends on: Stores
- Used by: App

## Data Flow

**Authentication Flow:**

1. User enters credentials in login page
2. Frontend calls `authApi.login()` → POST `/auth/login`
3. Backend validates credentials via `AuthService`
4. Sa-Token generates JWT and stores in Redis
5. Backend returns token and user info
6. Frontend stores token in Pinia + localStorage
7. Router guard redirects to dashboard

**Authorization Flow:**

1. Frontend includes token in request header (`Authorization`)
2. Backend `SaCheckLogin` validates token via Sa-Token
3. If `@SaCheckPermission` present, `StpInterfaceImpl` loads user permissions
4. Service layer executes business logic
5. Repository layer performs soft-delete-aware queries
6. Response wrapped in `R<T>` unified response format

**Dynamic Menu Flow:**

1. After login, `permissionStore.fetchMenus()` calls `/auth/menus`
2. Backend returns menu tree based on user roles
3. Frontend filters disabled menus
4. `permissionStore.generateRoutes()` converts menus to Vue Router routes
5. Routes dynamically added via `router.addRoute()`
6. Sidebar renders from menu tree

**CRUD Data Flow:**

1. Component calls API function (e.g., `dictApi.typeList(params)`)
2. Axios interceptor adds token
3. Request hits Controller (e.g., `SysDictTypeController.list()`)
4. Controller calls Service (e.g., `DictTypeService.queryPage()`)
5. Service calls Repository (e.g., `DictTypeRepository.queryPage()`)
6. Repository uses JPA Specification for dynamic queries
7. JPA returns `Page<T>`, converted to `PageResult<T>`
8. Service maps entities to VOs
9. Controller wraps in `R.ok(data)`
10. Frontend receives typed response

**State Management:**
- Authentication state: Token in localStorage via `pinia-plugin-persistedstate`
- User info: Cached in `useUserStore` with shallowRef for performance
- Permissions: Loaded on login, stored in memory
- Menus: Dynamically loaded and converted to routes

## Key Abstractions

**BaseEntity:**
- Purpose: Base entity class with common fields
- Examples: `backend/src/main/java/com/devlovecode/aiperm/common/domain/BaseEntity.java`
- Pattern: `@MappedSuperclass` with `@SQLRestriction("deleted = 0")` for automatic soft delete filtering
- Fields: `id`, `createTime`, `updateTime`, `createBy`, `updateBy`, `deleted`, `version`

**BaseJpaRepository:**
- Purpose: Base repository interface with soft delete enforcement
- Examples: `backend/src/main/java/com/devlovecode/aiperm/common/repository/BaseJpaRepository.java`
- Pattern: Extends `JpaRepository`, overrides `deleteById()` to throw exception, provides `softDelete()`
- Methods: `softDelete()`, `softDeleteByIds()`, `findAll()` with soft delete filter

**R<T> Unified Response:**
- Purpose: Standardized API response wrapper
- Examples: `backend/src/main/java/com/devlovecode/aiperm/common/domain/R.java`
- Pattern: Generic wrapper with `code`, `message`, `data`
- Usage: `R.ok(data)`, `R.fail(message)`, `R.fail(errorCode)`

**PageResult<T>:**
- Purpose: Pagination wrapper with JPA Page conversion
- Examples: `backend/src/main/java/com/devlovecode/aiperm/common/domain/PageResult.java`
- Pattern: Converts JPA's 0-based pages to frontend's 1-based pages
- Fields: `total`, `list`, `pageNum`, `pageSize`, `pages`
- Methods: `fromJpaPage()`, `map()`

**DTO with @JsonView:**
- Purpose: Separate validation groups for different operations
- Examples: `backend/src/main/java/com/devlovecode/aiperm/modules/system/dto/DictTypeDTO.java`
- Pattern: `Views.Create`, `Views.Update`, `Views.Query` for validation grouping
- Usage: `@Validated({Default.class, Views.Create.class})`

## Entry Points

**Backend Main Entry:**
- Location: `backend/src/main/java/com/devlovecode/aiperm/AipermApplication.java`
- Triggers: Spring Boot application startup
- Responsibilities: Component scanning, auto-configuration, embedded Tomcat server
- Annotations: `@SpringBootApplication`, `@EnableAsync`

**Frontend Main Entry:**
- Location: `frontend/src/main.ts`
- Triggers: Browser loads the app
- Responsibilities: Vue app initialization, Pinia setup, Element Plus registration, router setup
- Key steps: Create app → Register Pinia → Register Element Plus → Register global components → Setup router → Mount

**API Entry Points (Backend Controllers):**

| Controller | Path | Purpose |
|------------|------|---------|
| `AuthController` | `/auth/*` | Login, logout, user info, menus |
| `SysUserController` | `/system/user/*` | User CRUD |
| `SysRoleController` | `/system/role/*` | Role CRUD |
| `SysMenuController` | `/system/menu/*` | Menu tree management |
| `SysDictTypeController` | `/system/dict/type/*` | Dictionary type CRUD |
| `SysDeptController` | `/system/dept/*` | Department management |
| `SysPostController` | `/system/post/*` | Position management |
| `SysNoticeController` | `/enterprise/notice/*` | Notice management |
| `SysJobController` | `/enterprise/job/*` | Scheduled job management |
| `MfaController` | `/mfa/*` | Multi-factor authentication |
| `CaptchaController` | `/captcha/*` | CAPTCHA generation/validation |

**Frontend Route Entry Points:**

| Route | Component | Purpose |
|-------|-----------|---------|
| `/login` | `views/login/index.vue` | Login page |
| `/` | `components/layout/MainLayout.vue` | Main layout (dashboard shell) |
| `/dashboard` | `views/dashboard/index.vue` | Dashboard home |
| `/system/user` | `views/system/user/index.vue` | User management |
| `/system/role` | `views/system/role/index.vue` | Role management |
| `/system/menu` | `views/system/menu/index.vue` | Menu management |
| `/system/dict` | `views/system/dict/index.vue` | Dictionary management |
| `/profile` | `views/profile/index.vue` | User profile |

## Error Handling

**Strategy:** Global exception handler with custom BusinessException

**Patterns:**
- **Business Exception:** Thrown for business logic errors, caught by `GlobalExceptionHandler`
- **Validation:** Jakarta Bean Validation with `@Validated` and custom groups
- **Soft Delete Enforcement:** Repository throws `UnsupportedOperationException` for hard deletes
- **401 Handling:** Axios interceptor redirects to login on token expiry
- **Permission Denied:** Sa-Token throws `NotPermissionException`, handled globally

**Cross-Cutting Concerns:**

**Logging:**
- Framework: SLF4J + Logback
- Operation Log: `@Log` aspect records all write operations to `sys_oper_log` table
- Login Log: `AuthService` records login attempts to `sys_login_log` table

**Validation:**
- Request validation: Jakarta Bean Validation with `@Validated`
- DTO grouping: `@JsonView` for create/update/query separation
- Custom validators: Can be added for specific business rules

**Authentication:**
- Provider: Sa-Token (JWT-based)
- Storage: Redis for token validation and session management
- Token header: `Authorization`
- Timeout: 30 days (configurable)

**Authorization:**
- Framework: Sa-Token permission annotations
- Levels: Login check (`@SaCheckLogin`) → Role check (`@SaCheckRole`) → Permission check (`@SaCheckPermission`)
- Permission format: `模块:资源:操作` (e.g., `system:user:create`)
- Implementation: `StpInterfaceImpl` loads permissions from database

**Data Scoping:**
- Service: `DataScopeService` for row-level security
- Pattern: Filter data based on user's department hierarchy
- Usage: Applied in service layer queries

**Transaction Management:**
- Annotation: `@Transactional` on service methods
- Propagation: Required (default)
- Rollback: On `RuntimeException` and `BusinessException`

---

*Architecture analysis: 2026-03-25*
