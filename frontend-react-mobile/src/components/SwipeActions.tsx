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
