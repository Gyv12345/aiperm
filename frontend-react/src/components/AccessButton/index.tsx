/**
 * 权限按钮组件
 *
 * 基于 umi/max 的 Access，对按钮做权限包裹。
 * 用法：
 *   <AddButton perm="system:user:create" onClick={...}>新增</AddButton>
 *   若不传 perm，则默认已登录即可见。
 */
import { Access, useAccess } from '@umijs/max';
import { Button } from 'antd';
import React from 'react';

interface PermProps {
  /** 权限码，如 system:user:create；不传则仅需登录 */
  perm?: string;
}

const Wrap: React.FC<React.PropsWithChildren<PermProps>> = ({ perm, children }) => {
  const access = useAccess();
  const accessible = !perm || (access as any).canPermission?.(perm);
  if (!accessible) return null;
  return <>{children}</>;
};

export const AddButton: React.FC<
  React.PropsWithChildren<{ onClick?: () => void; perm?: string }>
> = ({ perm, ...rest }) => (
  <Wrap perm={perm}>
    <Button type="primary" {...rest} />
  </Wrap>
);

export const del = (perm?: string) => perm;

export const edit = (perm?: string) => perm;

export default AddButton;
