/**
 * 通用卡片列表：下拉刷新 + 无限滚动加载更多。
 *
 * - fetchFn 接收 { pageNum, pageSize }，返回 { list, total }（PageResult 结构）。
 *   调用方负责把后端 service 参数包进 fetchFn（如 user 的 { dto:{page,...} }）。
 * - filter 变化时重置到第 1 页重新加载。
 * - 加载失败静默（业务错误已由请求拦截器 Toast 提示），保留已加载数据。
 *
 * 用法：
 * <MobileList
 *   fetchFn={(p) => userPage({ dto: { page: p.pageNum, pageSize: p.pageSize } })}
 *   keyField="id"
 *   renderItem={(u) => <div>{u.username}</div>}
 * />
 */
import { InfiniteScroll, PullToRefresh, List, SpinLoading } from 'antd-mobile';
import React, { useCallback, useEffect, useRef, useState } from 'react';

export type MobileListProps<T> = {
  /** 分页拉取函数，返回 PageResult（list/total） */
  fetchFn: (params: { pageNum: number; pageSize: number }) => Promise<{
    list?: T[];
    total?: number;
  }>;
  /** 单项渲染 */
  renderItem: (item: T, index: number) => React.ReactNode;
  /** key 字段名 */
  keyField: keyof T;
  /** 筛选条件，变化时重置重载 */
  filter?: Record<string, any>;
  pageSize?: number;
  header?: React.ReactNode;
  emptyText?: string;
};

function MobileListInner<T>(props: MobileListProps<T>) {
  const {
    fetchFn,
    renderItem,
    keyField,
    filter,
    pageSize = 10,
    header,
    emptyText = '暂无数据',
  } = props;

  const [data, setData] = useState<T[]>([]);
  const [hasMore, setHasMore] = useState(true);
  const [initialLoading, setInitialLoading] = useState(true);
  const pageRef = useRef(1);
  // filter 序列化用于依赖对比
  const filterKey = JSON.stringify(filter ?? {});

  const loadPage = useCallback(
    async (pageNum: number) => {
      const res = await fetchFn({ pageNum, pageSize });
      const list = res?.list ?? [];
      return { list, total: res?.total ?? 0 };
    },
    [fetchFn, pageSize],
  );

  const loadMore = useCallback(async () => {
    const { list, total } = await loadPage(pageRef.current);
    setData((prev) => {
      const merged = [...prev, ...list];
      setHasMore(merged.length < total);
      return merged;
    });
    pageRef.current += 1;
  }, [loadPage]);

  // filter 变化或首次：重置并加载第 1 页
  useEffect(() => {
    let cancelled = false;
    setInitialLoading(true);
    pageRef.current = 1;
    setData([]);
    setHasMore(true);
    (async () => {
      try {
        const { list, total } = await loadPage(1);
        if (cancelled) return;
        setData(list);
        setHasMore(list.length < total);
        pageRef.current = 2;
      } catch {
        // 静默
      } finally {
        if (!cancelled) setInitialLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterKey, loadPage]);

  const onRefresh = useCallback(async () => {
    pageRef.current = 1;
    try {
      const { list, total } = await loadPage(1);
      setData(list);
      setHasMore(list.length < total);
      pageRef.current = 2;
    } catch {
      // 静默
    }
  }, [loadPage]);

  if (initialLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: 32 }}>
        <SpinLoading />
      </div>
    );
  }

  return (
    <PullToRefresh onRefresh={onRefresh}>
      <List header={header}>
        {data.length === 0 ? (
          <List.Item>{emptyText}</List.Item>
        ) : (
          data.map((item, idx) => (
            <List.Item key={String(item[keyField])} aria-rowindex={idx}>
              {renderItem(item, idx)}
            </List.Item>
          ))
        )}
      </List>
      {hasMore && data.length > 0 && (
        <InfiniteScroll loadMore={loadMore} hasMore={hasMore} />
      )}
    </PullToRefresh>
  );
}

// 泛型组件用 forwardRef 包装导出
export const MobileList = React.memo(MobileListInner) as <T>(
  props: MobileListProps<T>,
) => React.ReactElement;

export default MobileList;
