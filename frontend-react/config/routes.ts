/**
 * 路由配置
 *
 * 说明：
 * - 路径需与后端 /auth/menus 返回的菜单 path 对齐（拼接后形如
 *   /system/user、/system/role、/enterprise/notice 等）。
 * - /welcome 是固定首页（后端菜单不含）。
 * - 监控类路由（/monitor/*、/log/*）后端菜单未包含，但接口存在，
 *   这里补充路由，便于通过 URL 或前端补充菜单访问。
 * - access 字段对接 src/access.ts。
 *
 * 文档：https://umijs.org/docs/guides/routes
 */
export default [
  {
    path: '/user',
    layout: false,
    routes: [
      {
        name: '登录',
        path: '/user/login',
        component: './User/Login',
      },
    ],
  },
  // 固定首页（后端菜单不含）
  {
    path: '/welcome',
    name: '工作台',
    icon: 'dashboard',
    access: 'canUser',
    component: './Dashboard',
  },
  {
    path: '/system',
    access: 'canUser',
    routes: [
      { path: '/system/user', name: '用户管理', component: './System/User', access: 'canUser' },
      { path: '/system/role', name: '角色管理', component: './System/Role', access: 'canUser' },
      { path: '/system/menu', name: '菜单管理', component: './System/Menu', access: 'canUser' },
      { path: '/system/dept', name: '部门管理', component: './System/Dept', access: 'canUser' },
      { path: '/system/post', name: '岗位管理', component: './System/Post', access: 'canUser' },
      { path: '/system/dict', name: '字典管理', component: './System/Dict', access: 'canUser' },
      { path: '/system/im-config', name: 'IM平台配置', component: './System/ImConfig', access: 'canUser' },
      { path: '/system/approval-scene', name: '审批场景管理', component: './System/ApprovalScene', access: 'canUser' },
    ],
  },
  {
    path: '/enterprise',
    access: 'canUser',
    routes: [
      { path: '/enterprise/notice', name: '公告通知', component: './Enterprise/Notice', access: 'canUser' },
      { path: '/enterprise/message', name: '消息中心', component: './Enterprise/Message', access: 'canUser' },
      { path: '/enterprise/job', name: '定时任务', component: './Enterprise/Job', access: 'canUser' },
      { path: '/enterprise/config', name: '参数配置', component: './Enterprise/Config', access: 'canUser' },
    ],
  },
  // 监控类（后端菜单未包含，补充路由）
  {
    path: '/monitor',
    name: '系统监控',
    access: 'canUser',
    routes: [
      { path: '/monitor/online', name: '在线用户', component: './Monitor/Online', access: 'canUser' },
      { path: '/monitor/server', name: '服务监控', component: './Monitor/Server', access: 'canUser' },
      { path: '/monitor/cache', name: '缓存监控', component: './Monitor/Cache', access: 'canUser' },
      { path: '/monitor/login-log', name: '登录日志', component: './Monitor/LoginLog', access: 'canUser' },
      { path: '/monitor/job-log', name: '任务日志', component: './Monitor/JobLog', access: 'canUser' },
      { path: '/monitor/oper-log', name: '操作日志', component: './Monitor/OperLog', access: 'canUser' },
    ],
  },
  {
    path: '/profile',
    name: '个人中心',
    access: 'canUser',
    component: './Profile',
  },
  // 默认重定向到工作台
  { path: '/', redirect: '/welcome' },
  {
    path: '*',
    layout: false,
    component: './404',
  },
];
