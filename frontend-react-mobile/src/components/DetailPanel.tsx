/**
 * 详情字段面板：键值对展示（antd-mobile List）。
 * 用法：<DetailPanel fields={[{label:'用户名', key:'username'}]} data={user} />
 */
import { List } from 'antd-mobile';
import React from 'react';

export type DetailField = {
  label: string;
  key: string;
  /** 自定义渲染值，接收（值，整行数据） */
  render?: (value: any, row: any) => React.ReactNode;
};

export type DetailPanelProps = {
  fields: DetailField[];
  data?: Record<string, any>;
  header?: React.ReactNode;
};

const PLACEHOLDER = '-';

export const DetailPanel: React.FC<DetailPanelProps> = ({ fields, data, header }) => {
  return (
    <List header={header}>
      {fields.map((f) => {
        const raw = data?.[f.key];
        const value = f.render ? f.render(raw, data) : raw;
        const isEmpty = value === undefined || value === null || value === '';
        return (
          <List.Item key={f.key} description={isEmpty ? PLACEHOLDER : String(value)}>
            {f.label}
          </List.Item>
        );
      })}
    </List>
  );
};

export default DetailPanel;
