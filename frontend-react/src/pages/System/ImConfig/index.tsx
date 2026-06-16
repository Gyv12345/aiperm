/**
 * IM 平台配置
 *
 * - 卡片网格展示各审批平台（飞书/企微/钉钉）的配置状态（/system/im-config GET）。
 * - 编辑：ModalForm 更新单平台配置（/system/im-config/{platform} PUT）。
 *
 * 状态语义：enabled 1=启用，0=禁用。
 */
import { list10 as listConfigs, update5 as updateConfig } from '@/services/aiperm/imConfig';
import { PageContainer } from '@ant-design/pro-components';
import {
  ModalForm,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
} from '@ant-design/pro-components';
import { Button, Card, Col, Descriptions, Empty, Form, Row, Spin, Tag, message } from 'antd';
import React, { useCallback, useEffect, useState } from 'react';

/** 扩展配置的默认 JSON 模板 */
const DEFAULT_EXTRA_CONFIG = '{\n  "simulationMode": true,\n  "todoUrl": ""\n}';

const ImConfig: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [configList, setConfigList] = useState<API.ImConfigVO[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [current, setCurrent] = useState<API.ImConfigVO>();
  const [form] = Form.useForm<API.ImConfigDTO>();

  const loadConfigs = useCallback(async () => {
    setLoading(true);
    try {
      const res: any = await listConfigs();
      // RListImConfigVO 为数组包装类型，拦截器已解包到数组本身
      const list: API.ImConfigVO[] = Array.isArray(res) ? res : (res?.data ?? []);
      setConfigList(list);
    } catch {
      // 业务失败已在拦截器统一提示，这里静默兜底避免页面崩溃
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadConfigs();
  }, [loadConfigs]);

  const enabledCount = configList.filter((item) => item.enabled === 1).length;

  const openEdit = (row: API.ImConfigVO) => {
    setCurrent(row);
    // 异步数据需手动回填（ModalForm initialValues 仅挂载时读取一次）
    form.setFieldsValue({
      enabled: row.enabled ?? 0,
      appId: row.appId ?? '',
      appSecret: '',
      corpId: row.corpId ?? '',
      callbackToken: row.callbackToken ?? '',
      callbackAesKey: row.callbackAesKey ?? '',
      extraConfig: row.extraConfig || DEFAULT_EXTRA_CONFIG,
      remark: row.remark ?? '',
    });
    setModalOpen(true);
  };

  return (
    <PageContainer>
      <Card style={{ marginBottom: 16 }}>
        <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', gap: 12 }}>
          <div>
            <div style={{ fontSize: 16, fontWeight: 600 }}>IM 平台配置</div>
            <div style={{ marginTop: 4, color: '#999', fontSize: 13 }}>
              当前已启用 {enabledCount} / {configList.length} 个审批平台。extraConfig 支持 simulationMode 与 todoUrl。
            </div>
          </div>
          <Button onClick={loadConfigs} loading={loading}>
            刷新
          </Button>
        </div>
      </Card>

      <Spin spinning={loading}>
        {configList.length === 0 && !loading ? (
          <Empty description="暂无平台配置" />
        ) : (
          <Row gutter={[16, 16]}>
            {configList.map((item) => (
              <Col xs={24} sm={12} lg={8} key={item.platform}>
                <Card
                  styles={{ body: { minHeight: 280 } }}
                  title={
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <span style={{ fontWeight: 600 }}>{item.platform}</span>
                      <Tag color={item.enabled === 1 ? 'success' : 'default'}>
                        {item.enabled === 1 ? '已启用' : '未启用'}
                      </Tag>
                      <Tag color={item.configReady ? 'success' : 'warning'}>
                        {item.configReady ? '配置完整' : '待补齐'}
                      </Tag>
                    </div>
                  }
                  extra={
                    <Button type="primary" size="small" onClick={() => openEdit(item)}>
                      编辑
                    </Button>
                  }
                >
                  <Descriptions column={1} size="small">
                    <Descriptions.Item label="App ID">{item.appId || '-'}</Descriptions.Item>
                    <Descriptions.Item label="Corp ID">{item.corpId || '-'}</Descriptions.Item>
                    <Descriptions.Item label="缺失字段">
                      {item.missingFields?.length ? (
                        <span>
                          {item.missingFields.map((field) => (
                            <Tag color="error" key={field} style={{ marginBottom: 4 }}>
                              {field}
                            </Tag>
                          ))}
                        </span>
                      ) : (
                        '-'
                      )}
                    </Descriptions.Item>
                    <Descriptions.Item label="备注">{item.remark || '-'}</Descriptions.Item>
                  </Descriptions>
                </Card>
              </Col>
            ))}
          </Row>
        )}
      </Spin>

      <ModalForm
        form={form}
        title={`编辑 ${current?.platform ?? ''} 配置`}
        width={680}
        open={modalOpen}
        onOpenChange={setModalOpen}
        modalProps={{ destroyOnClose: true }}
        onFinish={async (values) => {
          try {
            // 空字符串字段转 undefined，避免覆盖后端已有值
            const payload: API.ImConfigDTO = {
              enabled: values.enabled,
              appId: values.appId || undefined,
              appSecret: values.appSecret || undefined,
              corpId: values.corpId || undefined,
              callbackToken: values.callbackToken || undefined,
              callbackAesKey: values.callbackAesKey || undefined,
              extraConfig: values.extraConfig || undefined,
              remark: values.remark || undefined,
            };
            await updateConfig({ platform: current?.platform ?? '' } as any, payload);
            message.success('配置已保存');
            loadConfigs();
            return true;
          } catch {
            return false;
          }
        }}
      >
        <ProFormSelect
          name="enabled"
          label="启用状态"
          rules={[{ required: true, message: '请选择启用状态' }]}
          options={[
            { label: '启用', value: 1 },
            { label: '禁用', value: 0 },
          ]}
        />
        <ProFormText name="appId" label="App ID" placeholder="请输入平台 App ID" />
        <ProFormText.Password
          name="appSecret"
          label="App Secret"
          placeholder="保留现值可直接提交"
        />
        <ProFormText name="corpId" label="Corp ID" placeholder="企业ID（企微等平台需要）" />
        <ProFormText
          name="callbackToken"
          label="Callback Token"
          placeholder="请输入回调校验 Token"
        />
        <ProFormText.Password
          name="callbackAesKey"
          label="Callback AES Key"
          placeholder="请输入回调 AES Key"
        />
        <ProFormTextArea
          name="extraConfig"
          label="扩展配置 JSON"
          fieldProps={{ rows: 8 }}
          placeholder='例如：{ "simulationMode": true, "todoUrl": "https://..." }'
        />
        <ProFormTextArea
          name="remark"
          label="备注"
          fieldProps={{ rows: 3 }}
          placeholder="记录平台对接说明、回调地址约定等"
        />
      </ModalForm>
    </PageContainer>
  );
};

export default ImConfig;
