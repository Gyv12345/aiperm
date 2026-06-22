# React 移动端 Phase 1（通用组件 + 用户管理 CRUD）实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立移动端通用组件库（MobileList/FilterPopup/FormPage/DetailPanel/SwipeActions/PermButton）+ Vitest 测试基建，并用它重写用户管理为完整 CRUD（列表/筛选/详情/新增/编辑/删除），验证组件可复用性。

**Architecture:** 通用组件放 `src/components/`，每个组件职责单一、props 清晰、可独立测试。用户管理作为首个消费方，路由拆为 list / detail / form 三页。所有网络走 Phase 0 已就绪的 `requestErrorConfig`（R\<T\> 解包）+ OpenAPI 生成的 services，分页消费 `PageResult.list/pageNum`。

**Tech Stack:** UmiJS Max 4 · antd-mobile 5（InfiniteScroll/PullToRefresh/SwipeAction/Popup/Form）· Vitest · @testing-library/react · jsdom。

**关联 spec:** `docs/superpowers/specs/2026-06-22-react-mobile-design.md`（第 5 节交互体系）
**前置:** Phase 0（`docs/superpowers/plans/2026-06-22-react-mobile-phase0.md`）已完成。

---

## 关键约定（Phase 0 经验，必读）

- **umi 4 全局 layout 用 `<Outlet/>`**，不是 children（见 `src/layouts/index.tsx`）
- **用户 status 语义**：`0=禁用 1=正常`（后端 UserDTO 注释"0=正常 1=停用"是错的）
- **user service `page()` 参数**：`{ dto: { page, pageSize, ...筛选 } }`（注意是 `page` 不是 `pageNum`，且包在 `dto` 里）
- **OpenAPI 类型**：`API.UserVO` / `API.UserDTO` / `API.PageResultUserVO`（来自 `src/services/aiperm/typings.d.ts`）
- **权限码**：`system:user:list` / `system:user:query` / `system:user:create` / `system:user:update` / `system:user:delete`（若依风格，复用 `access.canPermission`）

---

## 文件结构总览

| 文件 | 处理 | 职责 |
|------|------|------|
| `frontend-react-mobile/package.json` | 改 | 加 vitest/@testing-library/react/jsdom |
| `frontend-react-mobile/vitest.config.ts` | 新 | Vitest 配置（jsdom 环境） |
| `frontend-react-mobile/src/test/setup.ts` | 新 | jest-dom 注册 |
| `frontend-react-mobile/src/components/PermButton.tsx` | 新 | 权限包裹按钮 |
| `frontend-react-mobile/src/components/PermButton.test.tsx` | 新 | 单测 |
| `frontend-react-mobile/src/components/DetailPanel.tsx` | 新 | 键值对详情展示 |
| `frontend-react-mobile/src/components/DetailPanel.test.tsx` | 新 | 单测 |
| `frontend-react-mobile/src/components/MobileList.tsx` | 新 | 卡片列表+下拉刷新+无限滚动 |
| `frontend-react-mobile/src/components/MobileList.test.tsx` | 新 | 单测 |
| `frontend-react-mobile/src/components/FilterPopup.tsx` | 新 | 筛选弹层 |
| `frontend-react-mobile/src/components/FilterPopup.test.tsx` | 新 | 单测 |
| `frontend-react-mobile/src/components/FormPage.tsx` | 新 | 表单页壳 |
| `frontend-react-mobile/src/components/FormPage.test.tsx` | 新 | 单测 |
| `frontend-react-mobile/src/components/SwipeActions.tsx` | 新 | 左滑操作（含权限过滤） |
| `frontend-react-mobile/src/components/SwipeActions.test.tsx` | 新 | 单测 |
| `frontend-react-mobile/src/services/options.ts` | 新 | 下拉数据源（部门/角色/岗位） |
| `frontend-react-mobile/src/pages/System/User/index.tsx` | 重写 | 列表 + 筛选 + 左滑 |
| `frontend-react-mobile/src/pages/System/User/Detail.tsx` | 新 | 详情页 |
| `frontend-react-mobile/src/pages/System/User/Form.tsx` | 新 | 新增/编辑表单页 |
| `frontend-react-mobile/config/routes.ts` | 改 | 加 detail/form 路由 |

---

## Task 1: Vitest 测试基础设施

**Files:**
- Modify: `frontend-react-mobile/package.json`
- Create: `frontend-react-mobile/vitest.config.ts`
- Create: `frontend-react-mobile/src/test/setup.ts`

- [ ] **Step 1: 安装测试依赖**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm add -D vitest@^1.6.0 @testing-library/react@^16.0.0 @testing-library/jest-dom@^6.4.0 @testing-library/user-event@^14.5.0 jsdom@^24.0.0`
Expected: 安装成功，无 peer 报错。

- [ ] **Step 2: 写 `vitest.config.ts`**

Create `frontend-react-mobile/vitest.config.ts`:

```typescript
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import path from 'node:path';

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    include: ['src/**/*.test.{ts,tsx}'],
    css: false,
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
});
```

- [ ] **Step 3: 写 `src/test/setup.ts`**

Create `frontend-react-mobile/src/test/setup.ts`:

```typescript
import '@testing-library/jest-dom/vitest';
import { afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';

// 每个测试后清理渲染的组件，避免泄漏
afterEach(() => {
  cleanup();
});
```

- [ ] **Step 4: 加 `@vitejs/plugin-react` 依赖**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm add -D @vitejs/plugin-react@^4.3.0`
Expected: 安装成功。

- [ ] **Step 5: 在 `package.json` 加测试脚本**

Modify `frontend-react-mobile/package.json` 的 `scripts`，在 `"openapi"` 后加：

```json
    "test": "vitest run",
    "test:watch": "vitest"
```

- [ ] **Step 6: 验证 Vitest 可跑（无测试时应输出 No test files found）**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm test`
Expected: 输出 `No test files found` 或类似（此时还没有 .test.tsx），退出码 0。若报错（如找不到 vitest），检查安装。

- [ ] **Step 7: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/package.json frontend-react-mobile/pnpm-lock.yaml frontend-react-mobile/vitest.config.ts frontend-react-mobile/src/test/
git commit -m "feat(mobile): 引入 Vitest 测试基础设施"
```

---

## Task 2: PermButton 组件（权限包裹按钮）

**Files:**
- Create: `frontend-react-mobile/src/components/PermButton.tsx`
- Test: `frontend-react-mobile/src/components/PermButton.test.tsx`

- [ ] **Step 1: 写失败测试**

Create `frontend-react-mobile/src/components/PermButton.test.tsx`:

```typescript
import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';

// vi.hoisted 让 vi.mock 工厂能引用 canPermissionMock（vi.mock 会被提升到文件顶部）
const { canPermissionMock } = vi.hoisted(() => ({ canPermissionMock: vi.fn() }));

vi.mock('@umijs/max', () => ({
  useAccess: () => ({ canPermission: canPermissionMock }),
}));

import { PermButton } from './PermButton';

describe('PermButton', () => {
  beforeEach(() => {
    canPermissionMock.mockReset();
  });

  it('有权限时渲染按钮', () => {
    canPermissionMock.mockReturnValue(true);
    render(<PermButton perm="system:user:create">新增</PermButton>);
    expect(screen.getByText('新增')).toBeInTheDocument();
  });

  it('无权限时不渲染', () => {
    canPermissionMock.mockReturnValue(false);
    const { container } = render(
      <PermButton perm="system:user:create">新增</PermButton>,
    );
    expect(container).toBeEmptyDOMElement();
  });
});
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm test src/components/PermButton.test.tsx`
Expected: FAIL，提示找不到 `PermButton` 模块。

- [ ] **Step 3: 写实现**

Create `frontend-react-mobile/src/components/PermButton.tsx`:

```typescript
/**
 * 权限包裹按钮：无指定权限时不渲染。
 * 权限来源：src/access.ts 的 canPermission（超管直接放行）。
 *
 * 用法：<PermButton perm="system:user:create" color="primary" onClick={...}>新增</PermButton>
 */
import { useAccess } from '@umijs/max';
import { Button } from 'antd-mobile';
import type { ButtonProps } from 'antd-mobile/es/components/button';
import React from 'react';

export type PermButtonProps = {
  /** 权限码，如 system:user:create */
  perm: string;
  children: React.ReactNode;
} & Omit<ButtonProps, 'children'>;

export const PermButton: React.FC<PermButtonProps> = ({
  perm,
  children,
  ...rest
}) => {
  const access = useAccess();
  if (!access.canPermission(perm)) return null;
  return <Button {...rest}>{children}</Button>;
};

export default PermButton;
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm test src/components/PermButton.test.tsx`
Expected: PASS（2 个测试通过）。

- [ ] **Step 5: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/components/PermButton.tsx frontend-react-mobile/src/components/PermButton.test.tsx
git commit -m "feat(mobile): PermButton 权限包裹按钮组件 + 单测"
```

---

## Task 3: DetailPanel 组件（键值对详情）

**Files:**
- Create: `frontend-react-mobile/src/components/DetailPanel.tsx`
- Test: `frontend-react-mobile/src/components/DetailPanel.test.tsx`

- [ ] **Step 1: 写失败测试**

Create `frontend-react-mobile/src/components/DetailPanel.test.tsx`:

```typescript
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { DetailPanel, type DetailField } from './DetailPanel';

describe('DetailPanel', () => {
  it('渲染各字段键值对', () => {
    const fields: DetailField[] = [
      { label: '用户名', key: 'username' },
      { label: '昵称', key: 'nickname' },
    ];
    const data = { username: 'admin', nickname: '管理员' };
    render(<DetailPanel fields={fields} data={data} />);
    expect(screen.getByText('用户名')).toBeInTheDocument();
    expect(screen.getByText('admin')).toBeInTheDocument();
    expect(screen.getByText('昵称')).toBeInTheDocument();
    expect(screen.getByText('管理员')).toBeInTheDocument();
  });

  it('render 自定义函数生效', () => {
    const fields: DetailField[] = [
      { label: '状态', key: 'status', render: (v) => (v === 1 ? '正常' : '禁用') },
    ];
    render(<DetailPanel fields={fields} data={{ status: 1 }} />);
    expect(screen.getByText('正常')).toBeInTheDocument();
  });

  it('值为空时显示占位', () => {
    const fields: DetailField[] = [{ label: '邮箱', key: 'email' }];
    render(<DetailPanel fields={fields} data={{}} />);
    expect(screen.getByText('邮箱')).toBeInTheDocument();
    expect(screen.getByText('-')).toBeInTheDocument();
  });
});
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm test src/components/DetailPanel.test.tsx`
Expected: FAIL，找不到 `DetailPanel`。

- [ ] **Step 3: 写实现**

Create `frontend-react-mobile/src/components/DetailPanel.tsx`:

```typescript
/**
 * 详情字段面板：键值对展示（antd-mobile List）。
 * 用法：<DetailPanel fields={[{label:'用户名', key:'username'}]} data={user} />
 */
import { List } from 'antd-mobile';
import React from 'react';

export type DetailField = {
  label: string;
  key: string;
  /** 自定义渲染值，接收（值，整行数据） */
  render?: (value: any, row: any) => React.ReactNode;
};

export type DetailPanelProps = {
  fields: DetailField[];
  data?: Record<string, any>;
  header?: React.ReactNode;
};

const PLACEHOLDER = '-';

export const DetailPanel: React.FC<DetailPanelProps> = ({ fields, data, header }) => {
  return (
    <List header={header}>
      {fields.map((f) => {
        const raw = data?.[f.key];
        const value = f.render ? f.render(raw, data) : raw;
        const isEmpty = value === undefined || value === null || value === '';
        return (
          <List.Item key={f.key} description={isEmpty ? PLACEHOLDER : String(value)}>
            {f.label}
          </List.Item>
        );
      })}
    </List>
  );
};

export default DetailPanel;
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm test src/components/DetailPanel.test.tsx`
Expected: PASS（3 个测试）。

- [ ] **Step 5: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/components/DetailPanel.tsx frontend-react-mobile/src/components/DetailPanel.test.tsx
git commit -m "feat(mobile): DetailPanel 详情面板组件 + 单测"
```

---

## Task 4: MobileList 组件（卡片列表 + 下拉刷新 + 无限滚动）

**Files:**
- Create: `frontend-react-mobile/src/components/MobileList.tsx`
- Test: `frontend-react-mobile/src/components/MobileList.test.tsx`

- [ ] **Step 1: 写失败测试**

Create `frontend-react-mobile/src/components/MobileList.test.tsx`:

```typescript
import { render, screen, waitFor, act } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { MobileList } from './MobileList';

describe('MobileList', () => {
  it('首次加载渲染数据', async () => {
    const fetchFn = vi.fn().mockResolvedValue({
      list: [{ id: 1, name: '张三' }, { id: 2, name: '李四' }],
      total: 2,
      pageNum: 1,
      pageSize: 10,
      pages: 1,
    });
    render(
      <MobileList
        fetchFn={fetchFn}
        keyField="id"
        renderItem={(item: any) => <div>{item.name}</div>}
      />,
    );
    await waitFor(() => {
      expect(screen.getByText('张三')).toBeInTheDocument();
      expect(screen.getByText('李四')).toBeInTheDocument();
    });
    expect(fetchFn).toHaveBeenCalledWith({ pageNum: 1, pageSize: 10 });
  });

  it('filter 变化时重新加载（清空旧数据）', async () => {
    const fetchFn = vi.fn().mockResolvedValue({ list: [], total: 0 });
    const { rerender } = render(
      <MobileList
        fetchFn={fetchFn}
        keyField="id"
        filter={{ status: 0 }}
        renderItem={(item: any) => <div>{item.id}</div>}
      />,
    );
    await waitFor(() => {
      expect(fetchFn).toHaveBeenCalledWith({ pageNum: 1, pageSize: 10 });
    });
    // filter 变化触发重载
    rerender(
      <MobileList
        fetchFn={fetchFn}
        keyField="id"
        filter={{ status: 1 }}
        renderItem={(item: any) => <div>{item.id}</div>}
      />,
    );
    await waitFor(() => {
      expect(fetchFn).toHaveBeenLastCalledWith({ pageNum: 1, pageSize: 10 });
    });
    expect(fetchFn.mock.calls.length).toBeGreaterThanOrEqual(2);
  });

  it('fetchFn 失败时不崩溃（空列表）', async () => {
    const fetchFn = vi.fn().mockRejectedValue(new Error('网络错误'));
    const { container } = render(
      <MobileList fetchFn={fetchFn} keyField="id" renderItem={(i: any) => <div>{i.id}</div>} />,
    );
    await waitFor(() => {
      expect(container).toBeInTheDocument();
    });
  });
});
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm test src/components/MobileList.test.tsx`
Expected: FAIL，找不到 `MobileList`。

- [ ] **Step 3: 写实现**

Create `frontend-react-mobile/src/components/MobileList.tsx`:

```typescript
/**
 * 通用卡片列表：下拉刷新 + 无限滚动加载更多。
 *
 * - fetchFn 接收 { pageNum, pageSize }，返回 { list, total }（PageResult 结构）。
 *   调用方负责把后端 service 参数包进 fetchFn（如 user 的 { dto:{page,...} }）。
 * - filter 变化时重置到第 1 页重新加载。
 * - 加载失败静默（业务错误已由请求拦截器 Toast 提示），保留已加载数据。
 *
 * 用法：
 * <MobileList
 *   fetchFn={(p) => userPage({ dto: { page: p.pageNum, pageSize: p.pageSize } })}
 *   keyField="id"
 *   renderItem={(u) => <div>{u.username}</div>}
 * />
 */
import { InfiniteScroll, PullToRefresh, List, SpinLoading } from 'antd-mobile';
import React, { useCallback, useEffect, useRef, useState } from 'react';

export type MobileListProps<T> = {
  /** 分页拉取函数，返回 PageResult（list/total） */
  fetchFn: (params: { pageNum: number; pageSize: number }) => Promise<{
    list?: T[];
    total?: number;
  }>;
  /** 单项渲染 */
  renderItem: (item: T, index: number) => React.ReactNode;
  /** key 字段名 */
  keyField: keyof T;
  /** 筛选条件，变化时重置重载 */
  filter?: Record<string, any>;
  pageSize?: number;
  header?: React.ReactNode;
  emptyText?: string;
};

function MobileListInner<T>(props: MobileListProps<T>) {
  const {
    fetchFn,
    renderItem,
    keyField,
    filter,
    pageSize = 10,
    header,
    emptyText = '暂无数据',
  } = props;

  const [data, setData] = useState<T[]>([]);
  const [hasMore, setHasMore] = useState(true);
  const [initialLoading, setInitialLoading] = useState(true);
  const pageRef = useRef(1);
  // filter 序列化用于依赖对比
  const filterKey = JSON.stringify(filter ?? {});

  const loadPage = useCallback(
    async (pageNum: number) => {
      const res = await fetchFn({ pageNum, pageSize });
      const list = res?.list ?? [];
      return { list, total: res?.total ?? 0 };
    },
    [fetchFn, pageSize],
  );

  const loadMore = useCallback(async () => {
    const { list, total } = await loadPage(pageRef.current);
    setData((prev) => {
      const merged = [...prev, ...list];
      setHasMore(merged.length < total);
      return merged;
    });
    pageRef.current += 1;
  }, [loadPage]);

  // filter 变化或首次：重置并加载第 1 页
  useEffect(() => {
    let cancelled = false;
    setInitialLoading(true);
    pageRef.current = 1;
    setData([]);
    setHasMore(true);
    (async () => {
      try {
        const { list, total } = await loadPage(1);
        if (cancelled) return;
        setData(list);
        setHasMore(list.length < total);
        pageRef.current = 2;
      } catch {
        // 静默
      } finally {
        if (!cancelled) setInitialLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterKey, loadPage]);

  const onRefresh = useCallback(async () => {
    pageRef.current = 1;
    try {
      const { list, total } = await loadPage(1);
      setData(list);
      setHasMore(list.length < total);
      pageRef.current = 2;
    } catch {
      // 静默
    }
  }, [loadPage]);

  if (initialLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: 32 }}>
        <SpinLoading />
      </div>
    );
  }

  return (
    <PullToRefresh onRefresh={onRefresh}>
      <List header={header}>
        {data.length === 0 ? (
          <List.Item>{emptyText}</List.Item>
        ) : (
          data.map((item, idx) => (
            <List.Item key={String(item[keyField])} aria-rowindex={idx}>
              {renderItem(item, idx)}
            </List.Item>
          ))
        )}
      </List>
      {hasMore && data.length > 0 && (
        <InfiniteScroll loadMore={loadMore} hasMore={hasMore} />
      )}
    </PullToRefresh>
  );
}

// 泛型组件用 forwardRef 包装导出
export const MobileList = React.memo(MobileListInner) as <T>(
  props: MobileListProps<T>,
) => React.ReactElement;

export default MobileList;
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm test src/components/MobileList.test.tsx`
Expected: PASS（3 个测试）。若 InfiniteScroll/PullToRefresh 在 jsdom 报错，给测试加 `vi.mock('antd-mobile', ...)` 仅 mock 用到的子模块——但优先尝试真实渲染。

- [ ] **Step 5: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/components/MobileList.tsx frontend-react-mobile/src/components/MobileList.test.tsx
git commit -m "feat(mobile): MobileList 通用列表组件（下拉刷新+无限滚动）+ 单测"
```

---

## Task 5: FilterPopup 组件（筛选弹层）

**Files:**
- Create: `frontend-react-mobile/src/components/FilterPopup.tsx`
- Test: `frontend-react-mobile/src/components/FilterPopup.test.tsx`

- [ ] **Step 1: 写失败测试**

Create `frontend-react-mobile/src/components/FilterPopup.test.tsx`:

```typescript
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { FilterPopup } from './FilterPopup';

describe('FilterPopup', () => {
  it('visible 时渲染标题与字段', () => {
    render(
      <FilterPopup visible={true} onClose={vi.fn()} onConfirm={vi.fn()}>
        <div>用户名输入框</div>
      </FilterPopup>,
    );
    expect(screen.getByText('筛选')).toBeInTheDocument();
    expect(screen.getByText('用户名输入框')).toBeInTheDocument();
  });

  it('点击确定回调 onConfirm 并传值', async () => {
    const onConfirm = vi.fn();
    const onClose = vi.fn();
    render(
      <FilterPopup visible={true} onClose={onClose} onConfirm={onConfirm}>
        <div>x</div>
      </FilterPopup>,
    );
    fireEvent.click(screen.getByText('确定'));
    await waitFor(() => {
      expect(onConfirm).toHaveBeenCalled();
      expect(onClose).toHaveBeenCalled();
    });
  });

  it('点击重置清空表单', async () => {
    const onConfirm = vi.fn();
    render(
      <FilterPopup visible={true} onClose={vi.fn()} onConfirm={onConfirm} initial={{ username: 'a' }}>
        <div>x</div>
      </FilterPopup>,
    );
    fireEvent.click(screen.getByText('重置'));
    fireEvent.click(screen.getByText('确定'));
    await waitFor(() => {
      expect(onConfirm).toHaveBeenCalledWith(expect.not.objectContaining({ username: 'a' }));
    });
  });
});
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm test src/components/FilterPopup.test.tsx`
Expected: FAIL，找不到 `FilterPopup`。

- [ ] **Step 3: 写实现**

Create `frontend-react-mobile/src/components/FilterPopup.tsx`:

```typescript
/**
 * 筛选弹层：底部 Popup + Form + 重置/确定。
 *
 * - children 是 Form.Item 列表（调用方提供字段）。
 * - visible 变 true 时把 initial 灌入 form。
 * - 确定回调 onConfirm(values) 并关闭；重置清空表单。
 *
 * 用法：
 * <FilterPopup visible={v} onClose={...} onConfirm={(vals)=>setFilter(vals)} initial={filter}>
 *   <Form.Item name="username" label="用户名"><Input /></Form.Item>
 * </FilterPopup>
 */
import { Button, Form, Popup } from 'antd-mobile';
import React, { useEffect } from 'react';

export type FilterPopupProps = {
  visible: boolean;
  onClose: () => void;
  onConfirm: (values: Record<string, any>) => void;
  children: React.ReactNode;
  initial?: Record<string, any>;
  title?: string;
};

export const FilterPopup: React.FC<FilterPopupProps> = ({
  visible,
  onClose,
  onConfirm,
  children,
  initial,
  title = '筛选',
}) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (visible) {
      form.setFieldsValue(initial ?? {});
    }
  }, [visible, initial, form]);

  const handleConfirm = () => {
    onConfirm(form.getFieldsValue());
    onClose();
  };

  const handleReset = () => {
    form.resetFields();
  };

  return (
    <Popup
      visible={visible}
      onMaskClick={onClose}
      bodyStyle={{
        borderTopLeftRadius: 12,
        borderTopRightRadius: 12,
        minHeight: '50vh',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <div
        style={{
          padding: '16px 16px 8px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <span style={{ fontSize: 16, fontWeight: 600 }}>{title}</span>
        <span style={{ color: '#999', fontSize: 14 }} onClick={handleReset}>
          重置
        </span>
      </div>
      <div style={{ flex: 1, overflowY: 'auto', padding: '0 16px' }}>
        <Form form={form} layout="vertical">
          {children}
        </Form>
      </div>
      <div
        style={{
          padding: 12,
          display: 'flex',
          gap: 12,
          borderTop: '1px solid #eee',
        }}
      >
        <Button block onClick={onClose}>
          取消
        </Button>
        <Button block color="primary" onClick={handleConfirm}>
          确定
        </Button>
      </div>
    </Popup>
  );
};

export default FilterPopup;
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm test src/components/FilterPopup.test.tsx`
Expected: PASS（3 个测试）。

- [ ] **Step 5: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/components/FilterPopup.tsx frontend-react-mobile/src/components/FilterPopup.test.tsx
git commit -m "feat(mobile): FilterPopup 筛选弹层组件 + 单测"
```

---

## Task 6: FormPage 组件（表单页壳）

**Files:**
- Create: `frontend-react-mobile/src/components/FormPage.tsx`
- Test: `frontend-react-mobile/src/components/FormPage.test.tsx`

- [ ] **Step 1: 写失败测试**

Create `frontend-react-mobile/src/components/FormPage.test.tsx`:

```typescript
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';

vi.mock('@umijs/max', () => ({
  history: { back: vi.fn(), push: vi.fn() },
}));

import { FormPage } from './FormPage';

describe('FormPage', () => {
  it('渲染标题与提交按钮', () => {
    render(
      <FormPage title="新增用户" onFinish={vi.fn()}>
        <div>表单字段</div>
      </FormPage>,
    );
    expect(screen.getByText('新增用户')).toBeInTheDocument();
    expect(screen.getByText('提交')).toBeInTheDocument();
  });

  it('点击提交触发 onFinish', async () => {
    const onFinish = vi.fn().mockResolvedValue(undefined);
    render(
      <FormPage title="t" onFinish={onFinish} submitText="保存">
        <div>x</div>
      </FormPage>,
    );
    fireEvent.click(screen.getByText('保存'));
    await waitFor(() => {
      expect(onFinish).toHaveBeenCalled();
    });
  });
});
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm test src/components/FormPage.test.tsx`
Expected: FAIL，找不到 `FormPage`。

- [ ] **Step 3: 写实现**

Create `frontend-react-mobile/src/components/FormPage.tsx`:

```typescript
/**
 * 表单页壳：NavBar（标题 + 返回 + 提交）+ Form（垂直布局）+ 底部固定提交按钮。
 *
 * - onFinish 成功后自动 history.back() 返回。
 * - initialValues 用于编辑页灌数据。
 * - children 是 Form.Item 列表。
 *
 * 用法：
 * <FormPage title="新增用户" onFinish={async (v) => { await create({dto:v}); }}>
 *   <Form.Item name="username" label="用户名"><Input /></Form.Item>
 * </FormPage>
 */
import { Button, Form, NavBar, Toast } from 'antd-mobile';
import { history } from '@umijs/max';
import React, { useState } from 'react';

export type FormPageProps = {
  title: string;
  onFinish: (values: any) => Promise<void>;
  children: React.ReactNode;
  initialValues?: Record<string, any>;
  submitText?: string;
};

export const FormPage: React.FC<FormPageProps> = ({
  title,
  onFinish,
  children,
  initialValues,
  submitText = '提交',
}) => {
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);

  const handleFinish = async (values: any) => {
    setSubmitting(true);
    try {
      await onFinish(values);
      Toast.show({ icon: 'success', content: '操作成功' });
      history.back();
    } catch {
      // 业务错误已由请求拦截器 Toast 提示
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <NavBar onBack={() => history.back()}>{title}</NavBar>
      <div style={{ padding: 12 }}>
        <Form
          form={form}
          layout="vertical"
          initialValues={initialValues}
          onFinish={handleFinish}
          footer={
            <Button
              block
              color="primary"
              loading={submitting}
              onClick={() => form.submit()}
              type="submit"
            >
              {submitText}
            </Button>
          }
        >
          {children}
        </Form>
      </div>
    </>
  );
};

export default FormPage;
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm test src/components/FormPage.test.tsx`
Expected: PASS（2 个测试）。

- [ ] **Step 5: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/components/FormPage.tsx frontend-react-mobile/src/components/FormPage.test.tsx
git commit -m "feat(mobile): FormPage 表单页壳组件 + 单测"
```

---

## Task 7: SwipeActions 组件（左滑操作 + 权限过滤）

**Files:**
- Create: `frontend-react-mobile/src/components/SwipeActions.tsx`
- Test: `frontend-react-mobile/src/components/SwipeActions.test.tsx`

- [ ] **Step 1: 写失败测试**

Create `frontend-react-mobile/src/components/SwipeActions.test.tsx`:

```typescript
import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';

const { canPermissionMock } = vi.hoisted(() => ({ canPermissionMock: vi.fn() }));

vi.mock('@umijs/max', () => ({
  useAccess: () => ({ canPermission: canPermissionMock }),
}));

import { SwipeActions, type SwipeActionItem } from './SwipeActions';

describe('SwipeActions', () => {
  beforeEach(() => {
    canPermissionMock.mockReset();
    canPermissionMock.mockReturnValue(true);
  });

  it('有权限的操作渲染', () => {
    const actions: SwipeActionItem[] = [
      { key: 'edit', text: '编辑', color: 'primary', onClick: vi.fn() },
    ];
    render(
      <SwipeActions actions={actions}>
        <div>行内容</div>
      </SwipeActions>,
    );
    expect(screen.getByText('行内容')).toBeInTheDocument();
  });

  it('无权限的操作被过滤（canPermission 返回 false）', () => {
    canPermissionMock.mockImplementation((perm: string) => perm !== 'system:user:delete');
    const actions: SwipeActionItem[] = [
      { key: 'edit', text: '编辑', color: 'primary', onClick: vi.fn(), perm: 'system:user:update' },
      { key: 'del', text: '删除', color: 'danger', onClick: vi.fn(), perm: 'system:user:delete' },
    ];
    // SwipeAction 右滑操作在 DOM 中渲染（antd-mobile 把 rightActions 放 DOM）
    const { container } = render(
      <SwipeActions actions={actions}>
        <div>行</div>
      </SwipeActions>,
    );
    // 删除按钮（无权限）不应渲染；这里验证"删除"文字不出现
    expect(screen.queryByText('删除')).toBeNull();
  });

  it('无任何可见操作时，直接渲染 children（不包 SwipeAction）', () => {
    canPermissionMock.mockReturnValue(false);
    const { container } = render(
      <SwipeActions actions={[{ key: 'x', text: 'x', color: 'primary', onClick: vi.fn(), perm: 'p' }]}>
        <div>行</div>
      </SwipeActions>,
    );
    expect(screen.getByText('行')).toBeInTheDocument();
  });
});
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm test src/components/SwipeActions.test.tsx`
Expected: FAIL，找不到 `SwipeActions`。

- [ ] **Step 3: 写实现**

Create `frontend-react-mobile/src/components/SwipeActions.tsx`:

```typescript
/**
 * 左滑操作（包裹 antd-mobile SwipeAction）+ 权限过滤。
 *
 * - actions 每项可带 perm；无 perm 或有权限的操作才显示。
 * - 若过滤后无可见操作，直接渲染 children（不包 SwipeAction，避免空滑动手势）。
 *
 * 用法：
 * <SwipeActions actions={[
 *   { key:'edit', text:'编辑', color:'primary', onClick:()=>..., perm:'system:user:update' },
 *   { key:'del', text:'删除', color:'danger', onClick:()=>..., perm:'system:user:delete' },
 * ]}>
 *   <List.Item>...</List.Item>
 * </SwipeActions>
 */
import { SwipeAction } from 'antd-mobile';
import { useAccess } from '@umijs/max';
import React from 'react';

export type SwipeActionItem = {
  key: string;
  text: string;
  color: 'primary' | 'success' | 'warning' | 'default' | 'danger';
  onClick: () => void;
  /** 权限码；不传则始终可见 */
  perm?: string;
};

export type SwipeActionsProps = {
  actions: SwipeActionItem[];
  children: React.ReactNode;
};

export const SwipeActions: React.FC<SwipeActionsProps> = ({ actions, children }) => {
  const access = useAccess();
  const visible = actions.filter((a) => !a.perm || access.canPermission(a.perm));

  if (visible.length === 0) {
    return <>{children}</>;
  }

  return (
    <SwipeAction
      rightActions={visible.map((a) => ({
        key: a.key,
        text: a.text,
        color: a.color,
        onClick: a.onClick,
      }))}
    >
      {children}
    </SwipeAction>
  );
};

export default SwipeActions;
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm test src/components/SwipeActions.test.tsx`
Expected: PASS（3 个测试）。

- [ ] **Step 5: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/components/SwipeActions.tsx frontend-react-mobile/src/components/SwipeActions.test.tsx
git commit -m "feat(mobile): SwipeActions 左滑操作组件（含权限过滤）+ 单测"
```

---

## Task 8: 下拉数据源 service（options.ts）

**Files:**
- Create: `frontend-react-mobile/src/services/options.ts`

- [ ] **Step 1: 写实现**

Create `frontend-react-mobile/src/services/options.ts`:

```typescript
/**
 * 表单下拉数据源：部门 / 角色 / 岗位。
 * 统一返回 { label, value }[] 供 Selector/Picker 使用。
 */
import { list3 as deptList } from '@/services/aiperm/dept';
import { page1 as rolePage } from '@/services/aiperm/role';
import { list9 as postList } from '@/services/aiperm/post';

export type Option = { label: string; value: number };

/** 部门下拉 */
export async function getDeptOptions(): Promise<Option[]> {
  try {
    const res: any = await deptList();
    const list = Array.isArray(res) ? res : res?.list ?? [];
    return (list as any[]).map((d) => ({ label: d.deptName ?? d.name ?? '', value: d.id }));
  } catch {
    return [];
  }
}

/** 角色下拉（拉前 100 个） */
export async function getRoleOptions(): Promise<Option[]> {
  try {
    const res: any = await rolePage({ dto: { page: 1, pageSize: 100 } });
    const list = res?.list ?? [];
    return (list as any[]).map((r) => ({ label: r.roleName ?? r.name ?? '', value: r.id }));
  } catch {
    return [];
  }
}

/** 岗位下拉 */
export async function getPostOptions(): Promise<Option[]> {
  try {
    const res: any = await postList();
    const list = Array.isArray(res) ? res : res?.list ?? [];
    return (list as any[]).map((p) => ({ label: p.postName ?? p.name ?? '', value: p.id }));
  } catch {
    return [];
  }
}
```

- [ ] **Step 2: 验证生成的 service 导出存在**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && grep -E "export (async )?function (list3|page1|list9)" src/services/aiperm/dept.ts src/services/aiperm/role.ts src/services/aiperm/post.ts`
Expected: 三行输出（dept list3、role page1、post list9）。若函数名不同（生成可能变），按实际名修正 import。

- [ ] **Step 3: 类型检查**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm tsc`
Expected: 零错误（生成的 service 自带 @ts-ignore，options.ts 无类型错误）。

- [ ] **Step 4: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/services/options.ts
git commit -m "feat(mobile): 表单下拉数据源 options.ts（部门/角色/岗位）"
```

---

## Task 9: 用户管理 - 列表页重写（MobileList + FilterPopup + SwipeActions）

**Files:**
- Modify: `frontend-react-mobile/src/pages/System/User/index.tsx`（重写）
- Modify: `frontend-react-mobile/config/routes.ts`（加子路由占位）

- [ ] **Step 1: 更新路由（加 detail / form 子路由）**

Modify `frontend-react-mobile/config/routes.ts`，把 `/system/user` 那行替换为：

```typescript
  {
    path: '/system/user',
    routes: [
      { path: '/system/user', name: '用户管理', component: './System/User' },
      { path: '/system/user/detail/:id', name: '用户详情', component: './System/User/Detail' },
      { path: '/system/user/create', name: '新增用户', component: './System/User/Form' },
      { path: '/system/user/edit/:id', name: '编辑用户', component: './System/User/Form' },
    ],
  },
```

- [ ] **Step 2: 重写列表页 `src/pages/System/User/index.tsx`**

Create（覆盖）`frontend-react-mobile/src/pages/System/User/index.tsx`:

```typescript
/**
 * 用户管理 - 列表页（Phase 1）
 * 用 MobileList + FilterPopup + SwipeActions，左滑编辑/删除，点行进详情。
 */
import { FilterPopup } from '@/components/FilterPopup';
import { MobileList } from '@/components/MobileList';
import { SwipeActions } from '@/components/SwipeActions';
import { Dialog, Form, Input, NavBar, Tag } from 'antd-mobile';
import { history } from '@umijs/max';
import React, { useState } from 'react';
import { page as userPage, deleteUsingDelete } from '@/services/aiperm/user';

const statusTag = (status?: number) =>
  status === 1 ? (
    <Tag color="#00b578" fill="outline" style={{ '--border-radius': '4px' }}>
      正常
    </Tag>
  ) : (
    <Tag color="#ff3141" fill="outline" style={{ '--border-radius': '4px' }}>
      禁用
    </Tag>
  );

const UserList: React.FC = () => {
  const [filter, setFilter] = useState<Record<string, any>>({});
  const [filterVisible, setFilterVisible] = useState(false);
  // MobileList 的 reload key：删除后变化以触发重载
  const [reloadKey, setReloadKey] = useState(0);

  const handleDelete = async (id: number) => {
    const ok = await Dialog.confirm({ content: '确定删除该用户？', confirmText: '删除', cancelText: '取消' });
    if (!ok) return;
    try {
      await deleteUsingDelete({ id });
      setReloadKey((k) => k + 1);
    } catch {
      // 拦截器已提示
    }
  };

  return (
    <>
      <NavBar
        onBack={() => history.back()}
        right={<span onClick={() => setFilterVisible(true)}>筛选</span>}
      >
        用户管理
      </NavBar>

      <MobileList<API.UserVO>
        key={reloadKey}
        fetchFn={(p) =>
          userPage({ dto: { page: p.pageNum, pageSize: p.pageSize, ...filter } })
        }
        keyField="id"
        filter={filter}
        renderItem={(u) => (
          <SwipeActions
            actions={[
              {
                key: 'edit',
                text: '编辑',
                color: 'primary',
                onClick: () => history.push(`/system/user/edit/${u.id}`),
                perm: 'system:user:update',
              },
              {
                key: 'del',
                text: '删除',
                color: 'danger',
                onClick: () => handleDelete(u.id!),
                perm: 'system:user:delete',
              },
            ]}
          >
            <div
              onClick={() => history.push(`/system/user/detail/${u.id}`)}
              style={{ padding: '8px 0' }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span>{u.nickname || u.username}</span>
                {statusTag(u.status)}
              </div>
              <div style={{ fontSize: 12, color: '#999', marginTop: 4 }}>
                {u.username}
                {u.deptName ? ` · ${u.deptName}` : ''}
                {u.roleNames ? ` · ${u.roleNames}` : ''}
              </div>
            </div>
          </SwipeActions>
        )}
      />

      <FilterPopup
        visible={filterVisible}
        onClose={() => setFilterVisible(false)}
        onConfirm={(vals) => setFilter(vals)}
        initial={filter}
      >
        <Form.Item name="username" label="用户名">
          <Input placeholder="按用户名筛选" clearable />
        </Form.Item>
        <Form.Item name="phone" label="手机号">
          <Input placeholder="按手机号筛选" clearable />
        </Form.Item>
      </FilterPopup>
    </>
  );
};

export default UserList;
```

- [ ] **Step 3: 类型检查**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm tsc`
Expected: 零错误（Detail/Form 页未建，routes 引用会报"找不到模块"——记录但继续，Task 10/11 创建）。

- [ ] **Step 4: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/pages/System/User/index.tsx frontend-react-mobile/config/routes.ts
git commit -m "feat(mobile): 用户列表重写（MobileList+FilterPopup+SwipeActions 左滑）"
```

---

## Task 10: 用户管理 - 详情页（DetailPanel + getById）

**Files:**
- Create: `frontend-react-mobile/src/pages/System/User/Detail.tsx`

- [ ] **Step 1: 写实现**

Create `frontend-react-mobile/src/pages/System/User/Detail.tsx`:

```typescript
/**
 * 用户管理 - 详情页
 * 从路由参数取 id，getById 拉数据，DetailPanel 展示。
 */
import { DetailPanel, type DetailField } from '@/components/DetailPanel';
import { Button, NavBar, SpinLoading } from 'antd-mobile';
import { history, useParams } from '@umijs/max';
import React, { useEffect, useState } from 'react';
import { getById } from '@/services/aiperm/user';

const FIELDS: DetailField[] = [
  { label: '用户名', key: 'username' },
  { label: '昵称', key: 'nickname' },
  { label: '真实姓名', key: 'realName' },
  { label: '手机号', key: 'phone' },
  { label: '邮箱', key: 'email' },
  { label: '性别', key: 'gender', render: (v) => (v === 1 ? '男' : v === 2 ? '女' : '未知') },
  { label: '部门', key: 'deptName' },
  { label: '角色', key: 'roleNames' },
  { label: '状态', key: 'status', render: (v) => (v === 1 ? '正常' : '禁用') },
];

const UserDetail: React.FC = () => {
  const params = useParams();
  const id = Number(params.id);
  const [user, setUser] = useState<API.UserVO>();
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    (async () => {
      try {
        const res = await getById({ id });
        setUser(res);
      } catch {
        // 拦截器已提示
      } finally {
        setLoading(false);
      }
    })();
  }, [id]);

  return (
    <>
      <NavBar onBack={() => history.back()} right={user?.id ? <span onClick={() => history.push(`/system/user/edit/${user.id}`)}>编辑</span> : undefined}>
        用户详情
      </NavBar>
      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: 32 }}>
          <SpinLoading />
        </div>
      ) : (
        <div style={{ padding: 12 }}>
          <DetailPanel fields={FIELDS} data={user} header="基本信息" />
        </div>
      )}
    </>
  );
};

export default UserDetail;
```

- [ ] **Step 2: 类型检查**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm tsc`
Expected: 零错误（Form 页未建，仍可能报"找不到 ./Form"，Task 11 创建）。

- [ ] **Step 3: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/pages/System/User/Detail.tsx
git commit -m "feat(mobile): 用户详情页（DetailPanel + getById）"
```

---

## Task 11: 用户管理 - 新增/编辑表单页（FormPage）

**Files:**
- Create: `frontend-react-mobile/src/pages/System/User/Form.tsx`

- [ ] **Step 1: 写实现**

Create `frontend-react-mobile/src/pages/System/User/Form.tsx`:

```typescript
/**
 * 用户管理 - 新增/编辑表单页
 * - 路由 /system/user/create：新增（含密码必填）
 * - 路由 /system/user/edit/:id：编辑（getById 灌数据，密码留空不修改）
 */
import { FormPage } from '@/components/FormPage';
import { Form, Input, Selector } from 'antd-mobile';
import { useParams } from '@umijs/max';
import React, { useEffect, useState } from 'react';
import { create, getById, update } from '@/services/aiperm/user';
import { getDeptOptions, getPostOptions, getRoleOptions, type Option } from '@/services/options';

const UserForm: React.FC = () => {
  const params = useParams();
  const id = params.id ? Number(params.id) : undefined;
  const isEdit = !!id;

  const [initialValues, setInitialValues] = useState<Record<string, any>>();
  const [deptOptions, setDeptOptions] = useState<Option[]>([]);
  const [roleOptions, setRoleOptions] = useState<Option[]>([]);
  const [postOptions, setPostOptions] = useState<Option[]>([]);

  useEffect(() => {
    (async () => {
      const [depts, roles, posts] = await Promise.all([
        getDeptOptions(),
        getRoleOptions(),
        getPostOptions(),
      ]);
      setDeptOptions(depts);
      setRoleOptions(roles);
      setPostOptions(posts);
      if (isEdit && id) {
        try {
          const u = await getById({ id });
          setInitialValues({
            ...u,
            roleIds: u.roleIds ?? [],
          });
        } catch {
          // 拦截器已提示
        }
      } else {
        setInitialValues({ status: 1, gender: 0 });
      }
    })();
  }, [id, isEdit]);

  const handleFinish = async (values: any) => {
    const payload = { ...values };
    // 编辑时密码留空则不传
    if (isEdit && !payload.password) {
      delete payload.password;
    }
    if (isEdit && id) {
      await update({ id }, payload);
    } else {
      await create(payload);
    }
  };

  // initialValues 未就绪时不渲染（避免表单空）
  if (initialValues === undefined) {
    return <div style={{ padding: 32, textAlign: 'center', color: '#999' }}>加载中…</div>;
  }

  return (
    <FormPage
      title={isEdit ? '编辑用户' : '新增用户'}
      initialValues={initialValues}
      onFinish={handleFinish}
      submitText={isEdit ? '保存' : '新增'}
    >
      <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
        <Input placeholder="用户名" clearable disabled={isEdit} />
      </Form.Item>
      <Form.Item
        name="password"
        label="密码"
        rules={isEdit ? [] : [{ required: true, message: '请输入密码' }]}
        help={isEdit ? '留空则不修改' : undefined}
      >
        <Input type="password" placeholder="密码" clearable />
      </Form.Item>
      <Form.Item name="nickname" label="昵称">
        <Input placeholder="昵称" clearable />
      </Form.Item>
      <Form.Item name="phone" label="手机号">
        <Input placeholder="手机号" clearable />
      </Form.Item>
      <Form.Item name="email" label="邮箱">
        <Input placeholder="邮箱" clearable />
      </Form.Item>
      <Form.Item name="gender" label="性别">
        <Selector
          options={[
            { label: '未知', value: 0 },
            { label: '男', value: 1 },
            { label: '女', value: 2 },
          ]}
        />
      </Form.Item>
      <Form.Item name="deptId" label="部门">
        <Selector options={deptOptions.map((o) => ({ label: o.label, value: o.value }))} />
      </Form.Item>
      <Form.Item name="postId" label="岗位">
        <Selector options={postOptions.map((o) => ({ label: o.label, value: o.value }))} />
      </Form.Item>
      <Form.Item name="roleIds" label="角色">
        <Selector
          multiple
          options={roleOptions.map((o) => ({ label: o.label, value: o.value }))}
        />
      </Form.Item>
      <Form.Item name="status" label="状态">
        <Selector
          options={[
            { label: '正常', value: 1 },
            { label: '禁用', value: 0 },
          ]}
        />
      </Form.Item>
    </FormPage>
  );
};

export default UserForm;
```

- [ ] **Step 2: 类型检查**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm tsc`
Expected: 零错误（所有页面已建）。

- [ ] **Step 3: Commit**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git add frontend-react-mobile/src/pages/System/User/Form.tsx
git commit -m "feat(mobile): 用户新增/编辑表单页（FormPage + 部门/角色/岗位下拉）"
```

---

## Task 12: 路由入口 + 端到端验证 + 收尾

**Files:**
- Modify: `frontend-react-mobile/src/pages/Apps/index.tsx`（应用宫格加"新增用户"快捷入口可选）

- [ ] **Step 1: 全量单测**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm test`
Expected: 6 个组件测试文件全部 PASS（PermButton/DetailPanel/MobileList/FilterPopup/FormPage/SwipeActions）。

- [ ] **Step 2: 类型检查**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm type-check`
Expected: 零错误。

- [ ] **Step 3: 生产构建**

Run: `cd /Users/shichenyang/IdeaProjects/aiperm/frontend-react-mobile && pnpm build`
Expected: 构建成功，产出 dist/，含 `p__System__User__Detail` / `p__System__User__Form` chunk。

- [ ] **Step 4: E2E 手动验证清单（宿主执行，dev server 已在端口 8001）**

- [ ] 登录后「应用」→「用户管理」→ 列表用 MobileList 渲染、上拉加载更多、下拉刷新
- [ ] 点右上「筛选」→ 弹 FilterPopup → 输入用户名确定 → 列表按筛选重载
- [ ] 点某行 → 进 `/system/user/detail/:id`，DetailPanel 展示字段
- [ ] 详情右上「编辑」→ 进 `/system/user/edit/:id`，表单灌数据，改昵称保存 → 返回列表更新
- [ ] 列表左滑某行 → 「编辑」「删除」操作（无权限的操作不显示）
- [ ] 左滑删除 → Dialog 确认 → 列表移除该项
- [ ] 列表页无「新增」入口（Phase 1 暂不做浮动按钮，Phase 2 补），通过 `/system/user/create` URL 可访问新增表单
- [ ] 新增表单：用户名/密码必填、部门/角色/岗位下拉、提交成功返回列表

- [ ] **Step 5: 收尾提交（若有遗漏）**

```bash
cd /Users/shichenyang/IdeaProjects/aiperm
git status
git add -A frontend-react-mobile/
git commit -m "chore(mobile): Phase 1 收尾" || echo "无遗漏改动"
```

---

## Phase 1 完成标准

✅ 6 个通用组件（PermButton/DetailPanel/MobileList/FilterPopup/FormPage/SwipeActions）实现 + 单测通过
✅ Vitest 基础设施就绪（`pnpm test` 跑全部组件单测）
✅ 用户管理完整 CRUD：列表（筛选+下拉刷新+无限滚动+左滑）/ 详情 / 新增 / 编辑 / 删除
✅ `pnpm type-check` + `pnpm build` 零错误
✅ E2E 全链路通过（宿主验证）
✅ 每个 Task 独立 commit

完成后进入 **Phase 2**：其余系统管理模块（角色/菜单/部门/岗位/字典/文件）用通用组件快速铺开。
