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
