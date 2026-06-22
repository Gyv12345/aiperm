import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';

vi.mock('@umijs/max', () => ({
  history: { back: vi.fn(), push: vi.fn() },
}));

import { FormPage } from './FormPage';

describe('FormPage', () => {
  it('渲染标题与提交按钮', () => {
    render(
      <FormPage title="新增用户" onFinish={vi.fn()}>
        <div>表单字段</div>
      </FormPage>,
    );
    expect(screen.getByText('新增用户')).toBeInTheDocument();
    expect(screen.getByText('提交')).toBeInTheDocument();
  });

  it('点击提交触发 onFinish', async () => {
    const onFinish = vi.fn().mockResolvedValue(undefined);
    render(
      <FormPage title="t" onFinish={onFinish} submitText="保存">
        <div>x</div>
      </FormPage>,
    );
    fireEvent.click(screen.getByText('保存'));
    await waitFor(() => {
      expect(onFinish).toHaveBeenCalled();
    });
  });
});
