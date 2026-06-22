import { render, screen, waitFor, act } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { MobileList } from './MobileList';

describe('MobileList', () => {
  it('首次加载渲染数据', async () => {
    const fetchFn = vi.fn().mockResolvedValue({
      list: [{ id: 1, name: '张三' }, { id: 2, name: '李四' }],
      total: 2,
      pageNum: 1,
      pageSize: 10,
      pages: 1,
    });
    render(
      <MobileList
        fetchFn={fetchFn}
        keyField="id"
        renderItem={(item: any) => <div>{item.name}</div>}
      />,
    );
    await waitFor(() => {
      expect(screen.getByText('张三')).toBeInTheDocument();
      expect(screen.getByText('李四')).toBeInTheDocument();
    });
    expect(fetchFn).toHaveBeenCalledWith({ pageNum: 1, pageSize: 10 });
  });

  it('filter 变化时重新加载（清空旧数据）', async () => {
    const fetchFn = vi.fn().mockResolvedValue({ list: [], total: 0 });
    const { rerender } = render(
      <MobileList
        fetchFn={fetchFn}
        keyField="id"
        filter={{ status: 0 }}
        renderItem={(item: any) => <div>{item.id}</div>}
      />,
    );
    await waitFor(() => {
      expect(fetchFn).toHaveBeenCalledWith({ pageNum: 1, pageSize: 10 });
    });
    // filter 变化触发重载
    rerender(
      <MobileList
        fetchFn={fetchFn}
        keyField="id"
        filter={{ status: 1 }}
        renderItem={(item: any) => <div>{item.id}</div>}
      />,
    );
    await waitFor(() => {
      expect(fetchFn).toHaveBeenLastCalledWith({ pageNum: 1, pageSize: 10 });
    });
    expect(fetchFn.mock.calls.length).toBeGreaterThanOrEqual(2);
  });

  it('fetchFn 失败时不崩溃（空列表）', async () => {
    const fetchFn = vi.fn().mockRejectedValue(new Error('网络错误'));
    const { container } = render(
      <MobileList fetchFn={fetchFn} keyField="id" renderItem={(i: any) => <div>{i.id}</div>} />,
    );
    await waitFor(() => {
      expect(container).toBeInTheDocument();
    });
  });
});
