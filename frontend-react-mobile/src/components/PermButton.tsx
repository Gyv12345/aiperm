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
