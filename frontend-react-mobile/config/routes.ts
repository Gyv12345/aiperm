/**
 * 移动端路由
 *
 * - 登录守卫由 src/app.tsx 的 onRouteChange 处理（未登录访问非 /user/* → 跳登录）。
 * - TabBar 显隐由 src/layouts/index.tsx 按路径决定（仅 /dashboard /apps /message /profile 显示 TabBar）。
 * - 不使用 layout: false（那是 layout 插件概念，本项目不开该插件）。
 * - access 字段留到 Phase 1 细粒度权限时启用。
 *
 * 文档：https://umijs.org/docs/guides/routes
 */
export default [
  { path: '/user/login', name: '登录', component: './User/Login' },
  { path: '/dashboard', name: '首页', component: './Dashboard' },
  { path: '/apps', name: '应用', component: './Apps' },
  { path: '/message', name: '消息', component: './Message' },
  { path: '/profile', name: '我的', component: './Profile' },
  { path: '/system/user', name: '用户管理', component: './System/User' },
  { path: '/', redirect: '/dashboard' },
  { path: '*', component: './404' },
];
