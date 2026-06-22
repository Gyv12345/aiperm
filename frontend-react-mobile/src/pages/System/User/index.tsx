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
