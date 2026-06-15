import { defineConfig } from '@umijs/max';
import proxy from './proxy';
import routes from './routes';

/**
 * Umi Max 主配置
 * 文档：https://umijs.org/docs/api/config
 */
export default defineConfig({
  /**
   * @umijs/max 插件
   */
  antd: {},
  access: {},
  model: {},
  initialState: {},
  request: {},
  layout: {
    title: 'AIPerm · 权限结构总览',
    logo: '/logo.svg',
    navTheme: 'light',
    colorPrimary: '#0060a9',
    layout: 'mix',
    contentWidth: 'Fluid',
    fixedHeader: true,
    fixSiderbar: true,
    // 关闭自带菜单，由 BasicLayout 通过 menuDataRender 渲染后端菜单
    menu: {
      locale: false,
    },
    // 不自动注入，自定义渲染
    splitMenus: false,
  },

  /**
   * 路由配置（从 routes.ts 引入）
   * access 字段对接 src/access.ts
   */
  routes,

  /**
   * 开发环境请求代理
   */
  proxy: proxy,

  /**
   * npm 客户端
   */
  npmClient: 'pnpm',

  /**
   * 构建产物目录
   */
  outputPath: 'dist',

  /**
   * hash 文件名，便于缓存控制
   */
  hash: true,

  /**
   * 站点图标
   */
  favicons: ['/logo.svg'],
});
