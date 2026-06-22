/**
 * 用户管理 - 新增/编辑表单页
 * - 路由 /system/user/create：新增（含密码必填）
 * - 路由 /system/user/edit/:id：编辑（getById 灌数据，密码留空不修改）
 *
 * 字段映射说明：
 * - 后端 UserDTO.postId 为单数（提交用）；UserVO.postIds 为复数（展示用）。
 * - 编辑回填时把 VO 的 postIds[0] 映射到表单 postId，保证岗位正确回显。
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
          // VO.postIds（数组）→ 表单 postId（单数），取第一个
          setInitialValues({
            ...u,
            postId: u.postIds && u.postIds.length > 0 ? u.postIds[0] : undefined,
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
