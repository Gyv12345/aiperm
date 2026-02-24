## API 开发流程（Orval 自动生成）

**前端绝对禁止手写 API 调用代码！**

必须遵循以下流程：

### 后端 API 开发流程

1. 设计并开发后端 Controller 掉用修改 API 接口
2. 启动后端服务：`./gradlew bootRun`
3. 运行 `pnpm run generate:api` 生成前端 API
4. 查看生成的 TypeScript 类型定义
5. 使用生成的 API 函数开发前端功能

### Orval 配置

Orval 配置文件位于 `frontend/orval.config.ts`，主要配置内容：

```typescript
import { defineConfig } from 'orval';

export default defineConfig({
  // 后端 API 文档地址
  input: 'http://localhost:8080/api/v3/api-docs',
  // 生成的 API 文件输出路径
  output: {
    mode: 'tags-split',
    target: 'src/api/generated.ts',
    schemasDir: 'src/models',
    // 请求拦截器配置
    hooks: {
    beforeRequest: {
      return {
        ...axiosConfig, // 使用自定义的 Axios 配置
      };
    },
  },
  // 其他配置...
});
```

### 使用示例

**错误做法（❌）：**
```typescript
// 不要这样！缺乏类型安全
const response = await axios.get('/api/users')
const users = response.data // 类型为 any
```

**正确做法（✅）：**
```typescript
// 1. 先生成 API
// cd frontend && pnpm run generate:api

// 2. 导入生成的类型和 API
import type { UserVO } from '@/models'
import { userControllerFindAll } from '@/api/generated'

// 3. 使用生成的 API（完全类型安全）
const { data } = await userControllerFindAll({ page: 1, pageSize: 10 })
// data 的类型自动推断为 UserVO[]
```

### 开发检查清单

在开发任何涉及 API 的功能前：

- [ ] 后端 API 是否已开发完成？
- [ ] 是否运行了 `pnpm run generate:api`？
- [ ] 是否查看了 `src/models/` 中的类型定义？
- [ ] 是否使用了生成的 API 函数？
- [ ] IDE 是否显示类型错误？

## 前端 API 生成命令

```bash
# 安装依赖
cd frontend
pnpm install

# 生成 API 客户端
pnpm run generate:api

# 启动开发服务器
pnpm run dev
```

### 常见问题

**Q: 生成后类型不匹配怎么办？**

A: 后端 API 已修改但前端未重新生成

**解决方法**：
```bash
cd frontend && pnpm run generate:api
```

**Q: 找不到生成的类型怎么办？**

A: 检查后端服务是否启动，检查 API 文档地址 http://localhost:8080/api/v3/api-docs

**Q: 历史数据怎么办？**

A: 检查 `frontend/src/models/` 目录，如果为空，说明 API 还未生成或后端未启动

## 参考资料

- [Orval 官方文档](https://orval.dev/)
- [项目 CLAUDE.md 中的 API 开发流程说明](../.claude/skills/aiperm/CLAUDE.md)
