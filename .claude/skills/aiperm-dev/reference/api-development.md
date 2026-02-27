## API 开发流程

### ⚠️ 前端绝对禁止手写 API 调用代码！

必须遵循以下流程：

### 开发流程

```
1. 后端开发 API
       ↓
2. 启动后端服务：cd backend && ./gradlew bootRun
       ↓
3. 访问 Swagger 确认接口：http://localhost:8080/api/swagger-ui.html
       ↓
4. 生成前端 API：cd frontend && pnpm run generate:api
       ↓
5. 查看 frontend/src/models/ 中的 TypeScript 类型定义
       ↓
6. 前端使用生成的 API 函数（完全类型安全）
```

### 使用示例

**❌ 错误做法：**
```typescript
// 不要手写 API 调用！缺乏类型安全
const response = await axios.get('/api/users')
const users = response.data // 类型为 any
```

**✅ 正确做法：**
```typescript
// 1. 先生成 API
// cd frontend && pnpm run generate:api

// 2. 导入生成的类型和 API
import type { UserVO } from '@/models'
import { userControllerFindAll } from '@/api/generated'

// 3. 使用生成的 API（完全类型安全）
const { data } = await userControllerFindAll({ page: 1, pageSize: 10 })
// data 的类型自动推断
```

### PageResult 字段说明（重要！）

后端 `PageResult` 返回的 JSON 格式：

```json
{
  "total": 100,
  "list": [...],      // 数据列表，不是 records！
  "pageNum": 1,       // 当前页码，不是 page！
  "pageSize": 10,
  "pages": 10         // 总页数
}
```

**前端必须使用相同的字段名：**
- ✅ `list` - 数据列表
- ✅ `pageNum` - 当前页码
- ❌ `records` - 错误！
- ❌ `page` - 错误！

### 开发检查清单

在开发任何涉及 API 的功能前：

- [ ] 后端 API 是否已开发完成？
- [ ] 是否运行了 `pnpm run generate:api`？
- [ ] 是否查看了 `src/models/` 中的类型定义？
- [ ] 是否使用了生成的 API 函数？
- [ ] IDE 是否显示类型错误？

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
