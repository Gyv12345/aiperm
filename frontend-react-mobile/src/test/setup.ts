import '@testing-library/jest-dom/vitest';
import { afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';

// 每个测试后清理渲染的组件，避免泄漏
afterEach(() => {
  cleanup();
});
