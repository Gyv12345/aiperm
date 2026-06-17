/**
 * 文件管理
 *
 * - ProTable 分页查询 /system/file，支持按文件名/类型搜索。
 * - 上传：antd Upload（customRequest 手写 FormData 调 /system/file/upload）。
 * - 图片预览：文件名列用 antd Image 组件。
 * - 下载：window.open(fileUrl)。
 * - 删除：Popconfirm + try/catch。
 */
import { deleteFile, pageFile, uploadFile } from '@/services/aiperm/file';
import { ProColumns, ProTable } from '@ant-design/pro-components';
import { FileOutlined, UploadOutlined } from '@ant-design/icons';
import { Button, Popconfirm, Upload, message } from 'antd';
import React, { useRef, useState } from 'react';

/** 文件大小格式化：字节 → B/KB/MB/GB */
function formatSize(bytes?: number): string {
  if (!bytes) return '-';
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  let val = bytes;
  let i = 0;
  while (val >= 1024 && i < units.length - 1) {
    val /= 1024;
    i++;
  }
  return `${val.toFixed(i === 0 ? 0 : 2)} ${units[i]}`;
}

/** 判断是否图片类型（用于预览） */
function isImage(fileType?: string): boolean {
  return !!fileType && fileType.startsWith('image/');
}

const FileList: React.FC = () => {
  const actionRef = useRef<any>();
  const [uploading, setUploading] = useState(false);

  const columns: ProColumns<API.SysFile>[] = [
    {
      title: '文件名',
      dataIndex: 'originalName',
      ellipsis: true,
      render: (_, r) =>
        isImage(r.fileType) ? (
          <a href={r.fileUrl} target="_blank" rel="noreferrer">
            {r.originalName || r.fileName}
          </a>
        ) : (
          <span>
            <FileOutlined style={{ marginRight: 6, color: '#8c8c8c' }} />
            {r.originalName || r.fileName}
          </span>
        ),
    },
    {
      title: '预览',
      dataIndex: 'fileUrl',
      width: 72,
      align: 'center',
      hideInSearch: true,
      render: (_, r) =>
        isImage(r.fileType) ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img src={r.fileUrl} alt="" style={{ width: 40, height: 40, objectFit: 'cover', borderRadius: 4 }} />
        ) : (
          <FileOutlined style={{ fontSize: 20, color: '#bfbfbf' }} />
        ),
    },
    { title: '文件类型', dataIndex: 'fileType', width: 140, ellipsis: true, hideInSearch: true },
    { title: '大小', dataIndex: 'fileSize', width: 100, hideInSearch: true, render: (_, r) => formatSize(r.fileSize) },
    {
      title: '存储',
      dataIndex: 'storageType',
      width: 80,
      hideInSearch: true,
      render: (_, r) => (r.storageType === 'aliyun' ? '阿里云' : '本地'),
    },
    { title: '上传人', dataIndex: 'createBy', width: 100, hideInSearch: true },
    { title: '上传时间', dataIndex: 'createTime', valueType: 'dateTime', width: 170, hideInSearch: true },
    {
      title: '操作',
      valueType: 'option',
      width: 140,
      render: (_, record) => [
        <a key="download" onClick={() => window.open(record.fileUrl, '_blank')}>
          下载
        </a>,
        <Popconfirm
          key="del"
          title="确认删除该文件？"
          onConfirm={async () => {
            try {
              await deleteFile({ id: record.id! });
              message.success('删除成功');
              actionRef.current?.reload();
            } catch {
              // 业务失败已在拦截器统一提示，吞掉避免 Unhandled Rejection
            }
          }}
        >
          <a style={{ color: '#ff4d4f' }}>删除</a>
        </Popconfirm>,
      ],
    },
  ];

  /** 自定义上传：手写 FormData 调 /system/file/upload */
  const handleUpload = async (options: any) => {
    const { file, onSuccess, onError } = options;
    setUploading(true);
    try {
      await uploadFile(file as File);
      message.success('上传成功');
      onSuccess?.({}, new XMLHttpRequest());
      actionRef.current?.reload();
    } catch (e) {
      message.error('上传失败');
      onError?.(e);
    } finally {
      setUploading(false);
    }
  };

  return (
    <ProTable<API.SysFile>
      rowKey="id"
      actionRef={actionRef}
      columns={columns}
      search={{ labelWidth: 'auto' }}
      request={async (params) => {
        const { current, pageSize, ...rest } = params;
        try {
          const res: any = await pageFile({
            dto: { page: current, pageSize, ...rest } as API.FileDTO,
          });
          const data: API.PageResultSysFile = res || {};
          return {
            data: data.list || [],
            total: data.total || 0,
            success: true,
          };
        } catch {
          return { data: [], total: 0, success: false };
        }
      }}
      toolBarRender={() => [
        <Upload key="upload" showUploadList={false} customRequest={handleUpload} multiple>
          <Button type="primary" loading={uploading} icon={<UploadOutlined />}>
            上传文件
          </Button>
        </Upload>,
      ]}
    />
  );
};

export default FileList;
