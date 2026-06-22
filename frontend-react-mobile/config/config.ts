/**
 * Umi Max 主配置（移动端）
 *
 * 关键差异（vs 桌面端）：
 * - 不开 antd / layout 插件 → 不打包 antd 与 ProLayout，UI 全用 antd-mobile。
 * - 登录守卫用 umi core 的 onRouteChange（见 src/app.tsx），不依赖 layout 插件。
 * - postcss-px-to-viewport 做 vw 适配（375 设计稿基准）。
 *
 * 文档：https://umijs.org/docs/api/config
 */
import { defineConfig } from '@umijs/max';
import pxToViewport from 'postcss-px-to-viewport-8-plugin';
import proxy from './proxy';
import routes from './routes';

export default defineConfig({
  // 运行时插件：鉴权模型 / 共享状态 / 初始状态 / 请求。不开 antd、layout。
  access: {},
  model: {},
  initialState: {},
  request: {},

  routes,

  proxy,

  npmClient: 'pnpm',

  outputPath: 'dist',

  hash: true,

  favicons: ['/logo.svg'],

  title: 'AIPerm',

  // 移动端 viewport：禁止缩放，viewport-fit=cover 适配刘海屏
  metas: [
    {
      name: 'viewport',
      content:
        'width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1, user-scalable=no, viewport-fit=cover',
    },
  ],

  // vw 适配：设计稿宽 375，1px 不会被转换（minPixelValue:1）
  extraPostCSSPlugins: [
    pxToViewport({
      viewportWidth: 375,
      unitPrecision: 5,
      viewportUnit: 'vw',
      selectorBlackList: [],
      minPixelValue: 1,
      mediaQuery: false,
    }),
  ],
});
