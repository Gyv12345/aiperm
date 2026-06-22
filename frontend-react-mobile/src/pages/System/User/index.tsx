/**
 * 用户管理 - 移动端示范列表页（Phase 0）
 *
 * - 调用生成的 page() 拉用户分页，antd-mobile List 卡片化展示。
 * - PullToRefresh 下拉刷新。
 * - 完整筛选/详情/编辑/删除在 Phase 1（通用组件 MobileList/FilterPopup/FormPage）实现。
 *
 * 字段以生成的 API.UserVO 为准；status: 0=禁用 1=正常。
 */
import { List, NavBar, PullToRefresh, Tag, Toast } from 'antd-mobile';
import React, { useCallback, useEffect, useState } from 'react';
import { history } from '@umijs/max';
import { page as userPage } from '@/services/aiperm/user';

const UserList: React.FC = () => {
  const [users, setUsers] = useState<API.UserVO[]>([]);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res: API.PageResultUserVO = await userPage({
        dto: { page: 1, pageSize: 20 },
      });
      setUsers(res?.list ?? []);
    } catch {
      // 业务错误已由请求拦截器 Toast 提示
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <>
      <NavBar onBack={() => history.back()}>用户管理</NavBar>
      <PullToRefresh onRefresh={load}>
        <List header={`共 ${users.length} 条（Phase 0 仅前 20 条）`}>
          {users.map((u) => (
            <List.Item
              key={u.id}
              description={
                <span style={{ fontSize: 12, color: '#999' }}>
                  {u.deptName || '未分配部门'}
                  {u.roleNames ? ` · ${u.roleNames}` : ''}
                </span>
              }
              extra={
                <Tag
                  color={u.status === 1 ? '#00b578' : '#ff3141'}
                  fill="outline"
                  style={{ '--border-radius': '4px' }}
                >
                  {u.status === 1 ? '正常' : '停用'}
                </Tag>
              }
            >
              {u.nickname || u.username}{' '}
              <span style={{ fontSize: 12, color: '#999' }}>{u.username}</span>
            </List.Item>
          ))}
        </List>
      </PullToRefresh>
    </>
  );
};

export default UserList;
