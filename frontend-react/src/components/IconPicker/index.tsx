/**
 * 图标选择器
 *
 * 用于菜单等需要在表单中选择 antd 图标的场景。
 * - 从 @ant-design/icons 动态枚举所有 *Outlined 图标，无需手动维护列表。
 * - 后端约定：存储不带 Outlined 后缀的 PascalCase 名称（如 Setting、Odometer），
 *   这里对选中值做 strip 处理，与 app.tsx 的 resolveMenuIcon 保持一致。
 * - 通过 Popover 弹出可搜索的图标网格，底部支持清空。
 */
import * as AllIcons from '@ant-design/icons';
import { Button, Empty, Input, Popover, Space } from 'antd';
import React, { useMemo, useState } from 'react';

/** 所有 Outlined 图标，按名称排序；格式为去掉 Outlined 后缀的 key */
const ICON_KEYS: string[] = Object.keys(AllIcons)
  .filter((k) => k.endsWith('Outlined'))
  .map((k) => k.replace(/Outlined$/, ''))
  .sort((a, b) => a.localeCompare(b));

interface IconPickerProps {
  /** 当前选中的图标名（不带 Outlined 后缀） */
  value?: string;
  /** 值变化回调，回传不带 Outlined 后缀的 PascalCase 名称 */
  onChange?: (value: string | undefined) => void;
  /** 占位提示 */
  placeholder?: string;
}

const IconPicker: React.FC<IconPickerProps> = ({ value, onChange, placeholder }) => {
  const [open, setOpen] = useState(false);
  const [keyword, setKeyword] = useState('');

  // 关键字过滤（大小写不敏感，按显示名匹配）
  const filtered = useMemo(() => {
    const kw = keyword.trim().toLowerCase();
    if (!kw) return ICON_KEYS;
    return ICON_KEYS.filter((k) => k.toLowerCase().includes(kw));
  }, [keyword]);

  const CurrentIcon = value ? (AllIcons as any)[`${value}Outlined`] : null;

  const content = (
    <div style={{ width: 320 }}>
      <Input.Search
        allowClear
        placeholder="搜索图标名称"
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
        style={{ marginBottom: 8 }}
      />
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(6, 1fr)',
          gap: 8,
          maxHeight: 280,
          overflow: 'auto',
        }}
      >
        {filtered.length === 0 && (
          <div style={{ gridColumn: '1 / -1' }}>
            <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="无匹配图标" />
          </div>
        )}
        {filtered.map((key) => {
          const IconComp = (AllIcons as any)[`${key}Outlined`];
          if (!IconComp) return null;
          const selected = key === value;
          return (
            <Button
              key={key}
              title={key}
              type={selected ? 'primary' : 'text'}
              onClick={() => {
                onChange?.(key);
                setOpen(false);
                setKeyword('');
              }}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                height: 40,
                fontSize: 18,
              }}
            >
              <IconComp />
            </Button>
          );
        })}
      </div>
      {value && (
        <Button
          size="small"
          type="link"
          danger
          style={{ padding: '4px 0 0' }}
          onClick={() => {
            onChange?.(undefined);
          }}
        >
          清除已选
        </Button>
      )}
    </div>
  );

  return (
    <Popover
      open={open}
      onOpenChange={setOpen}
      trigger="click"
      placement="bottomLeft"
      content={content}
    >
      {/* 受控的只读展示框，点击触发 Popover；样式贴近 antd Input */}
      <div
        className="ant-input"
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          cursor: 'pointer',
          minHeight: 32,
          paddingInline: 11,
        }}
      >
        <Space>
          {CurrentIcon ? (
            <>
              <CurrentIcon />
              <span style={{ fontSize: 14 }}>{value}</span>
            </>
          ) : (
            <span style={{ color: '#bfbfbf' }}>{placeholder ?? '点击选择图标'}</span>
          )}
        </Space>
        <span style={{ color: '#bfbfbf', fontSize: 12 }}>选择</span>
      </div>
    </Popover>
  );
};

export default IconPicker;

export type { IconPickerProps };
