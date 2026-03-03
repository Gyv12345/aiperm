# Approval Todo Usability Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 重构 `审批中心/待我审批` 为小白可用的引导与诊断工作台，新增后端聚合接口，完成管理员/普通用户分层可见。

**Architecture:** 后端在 `approval` 模块新增只读聚合服务与接口，统一计算用户下一步动作和管理员诊断摘要。前端 `approval/todo` 页面只消费一个聚合 API，渲染步骤引导、快捷动作、管理员排查区和飞书权限申请清单，避免多接口拼装带来的不一致。

**Tech Stack:** Spring Boot 4 + Sa-Token + JdbcClient + Vue 3 + TypeScript + Element Plus

---

### Task 1: 后端决策模型（NextStep）先测后写

**Files:**
- Create: `backend/src/test/java/com/devlovecode/aiperm/modules/approval/service/ApprovalTodoDecisionTest.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/approval/service/ApprovalTodoDecision.java`

**Step 1: Write the failing test**

```java
@Test
void should_return_bind_oauth_when_platform_enabled_but_not_bound() {
    ApprovalTodoDecision.Input input = new ApprovalTodoDecision.Input(true, false, 2);
    ApprovalTodoDecision.Output output = ApprovalTodoDecision.resolve(input);
    assertEquals("BIND_OAUTH", output.nextStep());
}
```

**Step 2: Run test to verify it fails**

Run: `cd backend && ./gradlew test --tests "*ApprovalTodoDecisionTest" --rerun-tasks`  
Expected: FAIL（`ApprovalTodoDecision` 不存在）

**Step 3: Write minimal implementation**

```java
public final class ApprovalTodoDecision {
    public static Output resolve(Input input) { /* 按 enabled/bound/sceneCount 计算 */ }
}
```

**Step 4: Run test to verify it passes**

Run: `cd backend && ./gradlew test --tests "*ApprovalTodoDecisionTest"`  
Expected: PASS

**Step 5: Commit**

```bash
git add backend/src/test/java/com/devlovecode/aiperm/modules/approval/service/ApprovalTodoDecisionTest.java backend/src/main/java/com/devlovecode/aiperm/modules/approval/service/ApprovalTodoDecision.java
git commit -m "test(approval): add todo next-step decision tests and implementation"
```

### Task 2: 后端聚合 VO 与仓储查询能力

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/approval/vo/ApprovalTodoOverviewVO.java`
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/approval/repository/ApprovalInstanceRepository.java`
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/approval/repository/ApprovalSceneRepository.java`
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/im/repository/ImConfigRepository.java`
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/notification/repository/MessageLogRepository.java`

**Step 1: Write the failing test**

```java
@Test
void should_build_platform_check_with_missing_fields() {
    // 给定缺失 appId/appSecret/callbackToken 的配置
    // 期望 missingFields 包含这三个字段
}
```

**Step 2: Run test to verify it fails**

Run: `cd backend && ./gradlew test --tests "*ApprovalTodoOverviewServiceTest" --rerun-tasks`  
Expected: FAIL（仓储方法/VO 不存在）

**Step 3: Write minimal implementation**

```java
// repository 新增：
// - findAllEnabledByPlatform / findLatestByPlatform
// - countEnabledByPlatform / findOneEnabledByPlatform
// VO 增加 viewer/userGuide/quickActions/adminDiagnostics
```

**Step 4: Run test to verify it passes**

Run: `cd backend && ./gradlew test --tests "*ApprovalTodoOverviewServiceTest"`  
Expected: PASS

**Step 5: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/approval/vo/ApprovalTodoOverviewVO.java backend/src/main/java/com/devlovecode/aiperm/modules/approval/repository/ApprovalInstanceRepository.java backend/src/main/java/com/devlovecode/aiperm/modules/approval/repository/ApprovalSceneRepository.java backend/src/main/java/com/devlovecode/aiperm/modules/im/repository/ImConfigRepository.java backend/src/main/java/com/devlovecode/aiperm/modules/notification/repository/MessageLogRepository.java
git commit -m "feat(approval): add todo overview vo and repository query support"
```

### Task 3: 后端聚合 Service + Controller 接口

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/approval/service/ApprovalTodoOverviewService.java`
- Create: `backend/src/test/java/com/devlovecode/aiperm/modules/approval/service/ApprovalTodoOverviewServiceTest.java`
- Modify: `backend/src/main/java/com/devlovecode/aiperm/modules/approval/controller/ApprovalController.java`

**Step 1: Write the failing test**

```java
@Test
void should_hide_admin_diagnostics_for_non_admin() {
    // 模拟无管理员权限
    // 断言 adminDiagnostics == null
}
```

**Step 2: Run test to verify it fails**

Run: `cd backend && ./gradlew test --tests "*ApprovalTodoOverviewServiceTest" --rerun-tasks`  
Expected: FAIL（service 未实现）

**Step 3: Write minimal implementation**

```java
@GetMapping("/todo/overview")
@SaCheckPermission("approval:todo:list")
public R<ApprovalTodoOverviewVO> todoOverview(@RequestParam(required = false) String platform) { ... }
```

**Step 4: Run test to verify it passes**

Run: `cd backend && ./gradlew test --tests "*ApprovalTodoOverviewServiceTest" --tests "*ApprovalTodoDecisionTest"`  
Expected: PASS

**Step 5: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/approval/service/ApprovalTodoOverviewService.java backend/src/test/java/com/devlovecode/aiperm/modules/approval/service/ApprovalTodoOverviewServiceTest.java backend/src/main/java/com/devlovecode/aiperm/modules/approval/controller/ApprovalController.java
git commit -m "feat(approval): expose todo overview aggregation endpoint"
```

### Task 4: 前端 API 对接与类型定义

**Files:**
- Modify: `frontend/src/api/approval.ts`

**Step 1: Write the failing test**

```ts
// type-check 失败预期：页面引用了不存在的 approvalApi.todoOverview
```

**Step 2: Run test to verify it fails**

Run: `cd frontend && pnpm run type-check`  
Expected: FAIL（todoOverview 未定义）

**Step 3: Write minimal implementation**

```ts
todoOverview: (platform?: string) =>
  request.get<ApprovalTodoOverviewVO>('/approval/todo/overview', { params: { platform } })
```

**Step 4: Run test to verify it passes**

Run: `cd frontend && pnpm run type-check`  
Expected: PASS

**Step 5: Commit**

```bash
git add frontend/src/api/approval.ts
git commit -m "feat(frontend): add approval todo overview api and types"
```

### Task 5: 重构待我审批页面为引导+诊断工作台

**Files:**
- Modify: `frontend/src/views/approval/todo/index.vue`

**Step 1: Write the failing test**

```ts
// 先让页面使用 overview 数据字段（userGuide/adminDiagnostics）但暂不实现，触发类型错误
```

**Step 2: Run test to verify it fails**

Run: `cd frontend && pnpm run type-check`  
Expected: FAIL（字段缺失或类型不匹配）

**Step 3: Write minimal implementation**

```vue
<!-- 引导步骤 + 快捷按钮 + 分层诊断区 + 飞书权限申请清单 + 复制申请文案 -->
```

**Step 4: Run test to verify it passes**

Run: `cd frontend && pnpm run type-check`  
Expected: PASS

**Step 5: Commit**

```bash
git add frontend/src/views/approval/todo/index.vue
git commit -m "feat(frontend): redesign approval todo page with guided diagnostics"
```

### Task 6: 端到端回归验证

**Files:**
- Modify: `docs/plans/2026-03-03-approval-todo-usability-impl-plan.md`（补充执行结果）

**Step 1: Run backend verification**

Run: `cd backend && ./gradlew test --tests "*ApprovalTodoDecisionTest" --tests "*ApprovalTodoOverviewServiceTest"`  
Expected: PASS

**Step 2: Run frontend verification**

Run: `cd frontend && pnpm run type-check && pnpm run lint && pnpm run build`  
Expected: PASS

**Step 3: Manual smoke checklist**

```text
1) 普通用户看不到 adminDiagnostics
2) 管理员可见 4 张诊断卡
3) 未绑定场景下 nextStep= BIND_OAUTH
4) 飞书权限申请文案可复制
```

**Step 4: Commit**

```bash
git add .
git commit -m "feat(approval): complete todo usability overhaul with overview diagnostics"
```

