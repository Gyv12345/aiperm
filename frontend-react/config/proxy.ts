/**
 * 开发环境请求代理
 *
 * 后端运行在 http://localhost:8080（Sa-Token，无 context-path）。
 * 前端 baseURL 统一为 /api，代理时 rewrite 移除 /api 前缀：
 *   /api/auth/login -> http://localhost:8080/auth/login
 *
 * 注意：umi/max 的 proxy 直接导出扁平对象，key 是路径前缀，
 * 不要再包一层 { dev: {...} }。
 *
 * 文档：https://umijs.org/docs/guides/proxy
 */
export default {
  '/api/': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    // 移除 /api 前缀
    pathRewrite: { '^/api': '' },
  },
};
