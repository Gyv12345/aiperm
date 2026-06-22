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
