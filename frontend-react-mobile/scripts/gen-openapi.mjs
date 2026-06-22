// OpenAPI → services 生成脚本
// 运行：node scripts/gen-openapi.mjs
// 需 ./openapi.json 存在（由后端 /v3/api-docs 拉取，见 README）
//
// 本脚本做了两项定制：
// 1. customClassName：把中文 @Tag 映射为英文文件名（后端 Tag 是中文）。
// 2. afterOpenApiDataInited：把响应里的 R<T> 解包为内层 T，
//    使得生成代码的返回类型与 src/requestErrorConfig.ts 自动解包 data 后的运行时值一致。
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { generateService } from '@umijs/openapi';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(__dirname, '..');

/** 中文 Tag → 英文文件名映射（与后端模块对齐） */
const TAG_TO_EN = {
  用户管理: 'user',
  角色管理: 'role',
  岗位管理: 'post',
  菜单管理: 'menu',
  部门管理: 'dept',
  字典类型管理: 'dictType',
  字典数据管理: 'dictData',
  'OAuth配置管理': 'oauthConfig',
  '2FA策略管理': 'mfaPolicy',
  'IM平台配置': 'imConfig',
  '验证码配置管理': 'captchaConfig',
  审批场景管理: 'approvalScene',
  审批实例: 'approvalInstance',
  审批回调: 'approvalCallback',
  待我审批总览: 'approvalTodo',
  个人中心: 'profile',
  公告通知管理: 'notice',
  消息中心管理: 'message',
  定时任务管理: 'job',
  系统配置管理: 'config',
  文件管理: 'oss',
  '双因素认证（2FA）': 'mfa',
  验证码: 'captcha',
  认证管理: 'auth',
  第三方OAuth: 'oauth',
  服务监控: 'monitorServer',
  在线用户监控: 'monitorOnline',
  登录日志管理: 'loginLog',
  任务日志管理: 'jobLog',
  缓存监控: 'monitorCache',
  操作日志管理: 'operLog',
  首页仪表盘: 'dashboard',
};

await generateService({
  schemaPath: path.join(root, 'openapi.json'),
  serversPath: path.join(root, 'src', 'services'),
  requestLibPath: "import { request } from '@umijs/max'",
  projectName: 'aiperm',
  hook: {
    // 响应 R<T> → 内层 T，对齐拦截器的自动解包；
    // 同时把中文 tags 替换为英文，使生成的文件名为英文
    afterOpenApiDataInited: (openAPIData) => {
      // 1) 替换 operation 的中文 tag 为英文（在工具拼音化之前生效）
      Object.values(openAPIData.paths || {}).forEach((methods) => {
        Object.values(methods).forEach((op) => {
          if (Array.isArray(op.tags)) {
            op.tags = op.tags.map((t) => {
              const en = TAG_TO_EN[t];
              if (!en) console.warn(`[openapi] 未映射的 Tag: ${t}`);
              return en || t;
            });
          }
        });
      });

      const schemas = openAPIData.components?.schemas || {};
      // 收集所有 R<XXX> 包装类型，记录其内层 data 指向的真实 schema 名
      const rWrapperToInner = {};
      Object.entries(schemas).forEach(([name, schema]) => {
        // 匹配 R 开头的包装：RUserVO、RListMenuVO、RVoid、RPageResultUserVO 等
        const m = /^R(.+)$/.exec(name);
        const dataRef = schema?.properties?.data?.$ref;
        if (m && dataRef) {
          const innerName = dataRef.split('/').pop();
          rWrapperToInner[name] = innerName;
        }
      });
      // 遍历所有响应，把对 R 包装的引用替换为内层类型引用
      Object.values(openAPIData.paths || {}).forEach((methods) => {
        Object.values(methods).forEach((op) => {
          const ok = op?.responses?.['200'];
          const ref = ok?.content?.['*/*']?.schema?.$ref;
          if (ref) {
            const refName = ref.split('/').pop();
            if (rWrapperToInner[refName]) {
              ok.content['*/*'].schema = {
                $ref: `#/components/schemas/${rWrapperToInner[refName]}`,
              };
            }
          }
        });
      });
      return openAPIData;
    },
  },
});

console.log('✓ services 已生成到 src/services/aiperm');
