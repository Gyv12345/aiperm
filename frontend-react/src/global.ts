/**
 * umi 全局脚本入口（早于 app.tsx 与 React 渲染执行）。
 *
 * 在最早时机注册 capture 阶段的 unhandledrejection 监听器，拦截业务失败错误
 * （responseInterceptor reject 的带 __silentBusinessError 标记的 Error），
 * 阻止 react-error-overlay 在 dev 环境显示全屏错误页。
 *
 * 原理：addEventListener 的 capture 阶段先于 bubble 阶段执行。react-error-overlay
 * 默认用 bubble 阶段监听，因此这里的 capture 监听器先执行，stopImmediatePropagation
 * 阻止后续监听器收到事件。业务提示已在 responseInterceptor 的 message.error 完成。
 * 真正的代码 bug（无标记的 rejection）不拦截，仍正常显示 overlay 便于排查。
 */
if (typeof window !== 'undefined') {
  window.addEventListener(
    'unhandledrejection',
    (event) => {
      const reason = event?.reason;
      if (
        reason &&
        typeof reason === 'object' &&
        (reason as any)?.__silentBusinessError
      ) {
        // 阻止 react-error-overlay 等 dev 工具显示全屏错误页
        event.preventDefault();
        event.stopImmediatePropagation();
      }
    },
    true, // capture 阶段，先于 react-error-overlay 的 bubble 监听器
  );
}
