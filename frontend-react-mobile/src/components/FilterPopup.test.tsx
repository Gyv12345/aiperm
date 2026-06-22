import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { FilterPopup } from './FilterPopup';

describe('FilterPopup', () => {
  it('visible 时渲染标题与字段', () => {
    render(
      <FilterPopup visible={true} onClose={vi.fn()} onConfirm={vi.fn()}>
        <div>用户名输入框</div>
      </FilterPopup>,
    );
    expect(screen.getByText('筛选')).toBeInTheDocument();
    expect(screen.getByText('用户名输入框')).toBeInTheDocument();
  });

  it('点击确定回调 onConfirm 并传值', async () => {
    const onConfirm = vi.fn();
    const onClose = vi.fn();
    render(
      <FilterPopup visible={true} onClose={onClose} onConfirm={onConfirm}>
        <div>x</div>
      </FilterPopup>,
    );
    fireEvent.click(screen.getByText('确定'));
    await waitFor(() => {
      expect(onConfirm).toHaveBeenCalled();
      expect(onClose).toHaveBeenCalled();
    });
  });

  it('点击重置清空表单', async () => {
    const onConfirm = vi.fn();
    render(
      <FilterPopup visible={true} onClose={vi.fn()} onConfirm={onConfirm} initial={{ username: 'a' }}>
        <div>x</div>
      </FilterPopup>,
    );
    fireEvent.click(screen.getByText('重置'));
    fireEvent.click(screen.getByText('确定'));
    await waitFor(() => {
      expect(onConfirm).toHaveBeenCalledWith(expect.not.objectContaining({ username: 'a' }));
    });
  });
});
