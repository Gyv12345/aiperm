/**
 * 筛选弹层：底部 Popup + Form + 重置/确定。
 *
 * - children 是 Form.Item 列表（调用方提供字段）。
 * - visible 变 true 时把 initial 灌入 form。
 * - 确定回调 onConfirm(values) 并关闭；重置清空表单。
 *
 * 用法：
 * <FilterPopup visible={v} onClose={...} onConfirm={(vals)=>setFilter(vals)} initial={filter}>
 *   <Form.Item name="username" label="用户名"><Input /></Form.Item>
 * </FilterPopup>
 */
import { Button, Form, Popup } from 'antd-mobile';
import React, { useEffect } from 'react';

export type FilterPopupProps = {
  visible: boolean;
  onClose: () => void;
  onConfirm: (values: Record<string, any>) => void;
  children: React.ReactNode;
  initial?: Record<string, any>;
  title?: string;
};

export const FilterPopup: React.FC<FilterPopupProps> = ({
  visible,
  onClose,
  onConfirm,
  children,
  initial,
  title = '筛选',
}) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (visible) {
      form.setFieldsValue(initial ?? {});
    }
  }, [visible, initial, form]);

  const handleConfirm = () => {
    onConfirm(form.getFieldsValue());
    onClose();
  };

  const handleReset = () => {
    form.resetFields();
  };

  return (
    <Popup
      visible={visible}
      onMaskClick={onClose}
      bodyStyle={{
        borderTopLeftRadius: 12,
        borderTopRightRadius: 12,
        minHeight: '50vh',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <div
        style={{
          padding: '16px 16px 8px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <span style={{ fontSize: 16, fontWeight: 600 }}>{title}</span>
        <span style={{ color: '#999', fontSize: 14 }} onClick={handleReset}>
          重置
        </span>
      </div>
      <div style={{ flex: 1, overflowY: 'auto', padding: '0 16px' }}>
        <Form form={form} layout="vertical">
          {children}
        </Form>
      </div>
      <div
        style={{
          padding: 12,
          display: 'flex',
          gap: 12,
          borderTop: '1px solid #eee',
        }}
      >
        <Button block onClick={onClose}>
          取消
        </Button>
        <Button block color="primary" onClick={handleConfirm}>
          确定
        </Button>
      </div>
    </Popup>
  );
};

export default FilterPopup;
