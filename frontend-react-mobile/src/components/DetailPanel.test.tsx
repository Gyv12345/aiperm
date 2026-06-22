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
