# Coding Conventions

**Analysis Date:** 2025-03-25

## Naming Patterns

### Backend (Java)

**Files:**
- PascalCase for all classes: `SysUser.java`, `RoleService.java`, `UserDTO.java`
- Lowercase with underscores for SQL migrations: `V1.0.1__create_sys_user.sql`

**Classes:**
- Entities: `Sys{Resource}` (e.g., `SysUser`, `SysRole`)
- DTOs: `{Resource}DTO` (e.g., `UserDTO`, `RoleDTO`)
- VOs: `{Resource}VO` (e.g., `UserVO`, `RoleVO`)
- Services: `{Resource}Service` (e.g., `UserService`, `RoleService`)
- Repositories: `{Resource}Repository` (e.g., `UserRepository`, `RoleRepository`)
- Controllers: `Sys{Resource}Controller` (e.g., `SysUserController`)

**Methods:**
- camelCase: `findById`, `queryPage`, `create`, `update`, `delete`
- Query methods use `query`, `find`, `get` prefixes: `queryPage`, `findById`, `getMenuIds`

**Variables:**
- camelCase: `userId`, `roleName`, `createTime`
- Constants: UPPER_SNAKE_CASE: `DEFAULT_PAGE_SIZE`

**Packages:**
- lowercase: `com.devlovecode.aiperm.modules.system.entity`

### Frontend (TypeScript/Vue)

**Files:**
- PascalCase for components: `UserList.vue`, `MainLayout.vue`
- camelCase for utilities/composables: `useTable.ts`, `useForm.ts`, `api-mutator.ts`
- lowercase for stores: `user.ts`, `app.ts`, `permission.ts`

**Components:**
- PascalCase: `<TableToolbar>`, `<SelectionBar>`, `<DictSelect>`

**Functions/Variables:**
- camelCase: `fetchUserList`, `handleSubmit`, `tableData`, `dialogVisible`

**Composables:**
- `use` prefix: `useTable`, `useForm`, `useDict`, `useAuth`

**Types/Interfaces:**
- PascalCase: `UserVO`, `UserDTO`, `PageResult<T>`, `TableColumn`

## Code Style

### Backend

**Linting:**
- Tool: Not explicitly configured (standard Java conventions)
- Key patterns observed:
  - 4-space indentation
  - Line length: No strict limit observed
  - Opening braces on same line

**Formatting:**
- Lombok annotations reduce boilerplate: `@Data`, `@RequiredArgsConstructor`, `@EqualsAndHashCode(callSuper = true)`

### Frontend

**Formatting:**
- Tool: ESLint with `typescript-eslint` and `eslint-plugin-vue`
- Config: `frontend/eslint.config.js`
- Key settings:
  - `@typescript-eslint/no-unused-vars`: warn (args starting with `_` ignored)
  - `@typescript-eslint/no-explicit-any`: warn
  - `vue/multi-word-component-names`: off
  - `no-console`: off
  - `no-debugger`: warn

**Lint commands:**
```bash
pnpm run lint          # Check
pnpm run lint:fix      # Fix
pnpm run format        # Alias for lint:fix
```

**Run checks:**
```bash
pnpm run check         # type-check + lint + build
```

## Import Organization

### Backend

**Order:**
1. Java standard library
2. Third-party libraries (Spring, Lombok, etc.)
3. Project imports (grouped by module)

**Example:**
```java
import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.dto.RoleDTO;
import com.devlovecode.aiperm.modules.system.entity.SysRole;
import com.devlovecode.aiperm.modules.system.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
```

### Frontend

**Order:**
1. Vue imports
2. Third-party libraries (Element Plus, etc.)
3. Project imports (absolute with `@/` alias)
4. Type imports

**Path Aliases:**
- `@/` → `frontend/src/`

**Example:**
```typescript
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox, type FormInstance, type FormRules} from 'element-plus'
import {userApi, type UserDTO, type UserVO} from '@/api/system/user'
import {roleApi, type RoleVO} from '@/api/system/role'
import type {PageResult, TableColumn} from '@/types'
import {useDict} from '@/composables/useDict'
```

## Error Handling

### Backend

**Patterns:**
- Custom exception: `BusinessException` for business logic errors
- Global handler: `GlobalExceptionHandler` catches and formats errors
- Throw `BusinessException` with message: `throw new BusinessException("用户不存在")`
- Use `Optional.orElseThrow()` for missing data: `findById(id).orElseThrow(() -> new BusinessException("..."))`

**Example:**
```java
public SysRole findById(Long id) {
    return roleRepo.findById(id)
            .orElseThrow(() -> new BusinessException("角色不存在"));
}
```

### Frontend

**Patterns:**
- Try-catch with user feedback via `ElMessage.error()`
- Console errors for debugging: `console.error('Failed to fetch:', error)`
- Check for cancellation (`error !== 'cancel'`) in MessageBox confirms
- Silent failures in logout (ignore errors)

**Example:**
```typescript
try {
  await userApi.create(submitData)
  ElMessage.success('创建成功')
} catch (error) {
  if (error !== false) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}
```

## Logging

### Backend

**Framework:** SLF4J (Spring Boot default)

**Patterns:**
- Use `@Log` annotation for operation logging (custom aspect)
- Log levels: DEBUG, INFO, WARN, ERROR
- Operation logging via `@Log(title = "xxx", operType = OperType.CREATE)`

**Example:**
```java
@Log(title = "用户管理", operType = OperType.CREATE)
public R<Long> create(@RequestBody @Validated UserDTO dto) {
    return R.ok(userService.create(dto));
}
```

### Frontend

**Framework:** `console`

**Patterns:**
- `console.error()` for failures in catch blocks
- No formal logging framework
- Errors shown to users via `ElMessage.error()`

## Comments

### Backend

**When to Comment:**
- Javadoc on public APIs
- Complex business logic explanation
- `@param`, `@return`, `@throws` for public methods

**Example:**
```java
/**
 * 分页查询
 */
public PageResult<SysRole> queryPage(RoleDTO dto) {
    // ...
}

/**
 * 获取角色的菜单ID列表
 */
public List<Long> getMenuIds(Long roleId) {
    // ...
}
```

### Frontend

**When to Comment:**
- Section dividers in templates: `<!-- 搜索区域 -->`, `<!-- 表格区域 -->`
- Complex logic explanation
- CSS class purposes

**JSDoc/TSDoc:**
- Used for exported functions in composables
- Type comments for complex generics

**Example:**
```typescript
export interface TableOptions<T> {
  fetchData: (params: { page: number; pageSize: number }) => Promise<PageResult<T>>
  defaultPageSize?: number
}
```

## Function Design

### Backend

**Size:**
- Services: 100-200 lines typical (RoleService: 155 lines)
- Controllers: 50-150 lines typical
- Repositories: Varies by complexity

**Parameters:**
- DTOs for complex input (vs many individual parameters)
- `@Validated` with View groups for validation

**Return Values:**
- `R<T>` wrapper for API responses
- `PageResult<T>` for paginated data
- Entities for internal methods
- VOs for external responses

### Frontend

**Size:**
- Composables: 60-70 lines typical
- Vue SFC: Can be large (user/index.vue: 1348 lines - includes template)
- Utility functions: < 50 lines

**Parameters:**
- Options objects for composables: `TableOptions<T>`, `FormOptions<T>`
- Destructured props in components

**Return Values:**
- Composables return reactive state and methods
- API functions return typed data (unwrapped by request utility)

## Module Design

### Backend

**Exports:**
- Public classes: Entities, DTOs, VOs, Services, Controllers
- Repositories typically package-private visibility

**Barrel Files:**
- Not used (Java package structure)

### Frontend

**Exports:**
- Named exports from composables: `export function useTable()`
- Default exports from stores: `export const useUserStore = defineStore(...)`
- API modules export both functions and types: `export { userApi, type UserVO }`

**Barrel Files:**
- `composables/index.ts` - exports all composables
- `api/generated.ts` - exports all generated API functions

---

*Convention analysis: 2025-03-25*
