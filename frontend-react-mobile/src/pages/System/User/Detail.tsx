/**
 * 用户管理 - 详情页
 * 从路由参数取 id，getById 拉数据，DetailPanel 展示。
 */
import { DetailPanel, type DetailField } from '@/components/DetailPanel';
import { NavBar, SpinLoading } from 'antd-mobile';
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
      <NavBar
        onBack={() => history.back()}
        right={
          user?.id ? (
            <span onClick={() => history.push(`/system/user/edit/${user.id}`)}>编辑</span>
          ) : undefined
        }
      >
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
