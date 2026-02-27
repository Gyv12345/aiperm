# 功能模块开发流程

## 流程概览

```
需求分析 → 数据库设计 → 后端开发 → 前端开发 → 测试联调
    ↓           ↓           ↓           ↓           ↓
  PRD/设计   Flyway SQL   6层代码    API技能生成   E2E验证
```

---

## Phase 1: 需求分析

### 输入
- 产品需求文档 (PRD)
- UI/UX 设计稿
- 接口文档（如有）

### 输出
- 功能模块清单
- 数据模型设计
- 接口设计（REST API）

### 检查清单
- [ ] 明确业务场景和用户角色
- [ ] 确定 CRUD 操作范围
- [ ] 设计数据表结构和关联关系
- [ ] 规划 API 端点和权限标识

---

## Phase 2: 数据库设计

### 创建 Flyway 迁移脚本

**文件路径**: `backend/src/main/resources/db/migration/V{version}__{description}.sql`

**命名规则**:
- 版本号格式：`V1.0.1`、`V1.0.2`（递增）
- 描述使用下划线：`__create_sys_xxx.sql`

### 标准建表模板

```sql
CREATE TABLE IF NOT EXISTS `sys_xxx` (
    -- 主键
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',

    -- 业务字段（根据实际需求）
    `name`        VARCHAR(100) NOT NULL COMMENT '名称',
    `code`        VARCHAR(50)  DEFAULT NULL COMMENT '编码',
    `status`      INT          DEFAULT 0 COMMENT '状态:0正常,1停用',
    `sort`        INT          DEFAULT 0 COMMENT '排序号',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',

    -- 基础字段（必须）
    `deleted`     TINYINT      DEFAULT 0 COMMENT '删除标记',
    `version`     INT          DEFAULT 0 COMMENT '乐观锁版本',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`   VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`   VARCHAR(50)  DEFAULT NULL COMMENT '更新人',

    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='xxx表';
```

### 检查清单
- [ ] Flyway 版本号正确且递增
- [ ] 包含所有基础字段（deleted, version, create_time 等）
- [ ] 添加合适的索引和注释
- [ ] 表名使用 `sys_` 前缀（系统模块）

---

## Phase 3: 后端开发

### 代码分层结构

```
modules/{module}/
├── entity/          # 1. 实体类（继承 BaseEntity）
├── repository/      # 2. 数据访问层（继承 BaseRepository）
├── service/         # 3. 业务逻辑层
├── controller/      # 4. 控制器层
├── dto/             # 5. 数据传输对象（使用 @JsonView）
└── vo/              # 6. 视图对象
```

### 核心规范

1. **Entity**: 继承 `BaseEntity`，无需任何注解
2. **Repository**: 继承 `BaseRepository<SysXxx>`
3. **DTO**: 使用 `@JsonView` 分组验证（Views.Create/Update/Query）
4. **Controller**: 类级别 `@SaCheckLogin`，写操作加 `@Log` + `@SaCheckPermission`
5. **依赖注入**: 使用 `@RequiredArgsConstructor` + `private final`，**禁止 `@Autowired`**

### 参照模板

| 层 | 参照文件 |
|----|---------|
| Entity | `modules/system/entity/SysDictType.java` |
| Repository | `modules/system/repository/DictTypeRepository.java` |
| Service | `modules/system/service/DictTypeService.java` |
| Controller | `modules/system/controller/SysDictTypeController.java` |
| DTO | `modules/system/dto/DictTypeDTO.java` |
| VO | `modules/system/vo/DictTypeVO.java` |

### 检查清单
- [ ] Entity 继承 BaseEntity，无注解
- [ ] Repository 继承 BaseRepository
- [ ] DTO 使用 @JsonView 分组验证
- [ ] Controller 类级别 @SaCheckLogin
- [ ] 写操作加 @Log + @SaCheckPermission
- [ ] 依赖注入用 @RequiredArgsConstructor + private final

---

## Phase 4: 前端开发

### API 生成（禁止手写！）

使用 `aiperm-api-generator` 技能生成 API：

```
/aiperm-api-generator
```

### 模块映射规则

```
backend/modules/                 frontend/src/api/
├── auth/controller/            ├── auth.ts
├── system/controller/          └── system/
│   ├── SysUserController           ├── user.ts
│   ├── SysRoleController           ├── role.ts
│   └── SysDictTypeController       └── dict.ts
└── enterprise/controller/      └── enterprise/
    └── SysNoticeController         └── notice.ts
```

### API 使用示例

```typescript
// 导入
import { xxxApi, type XxxVO, type XxxDTO } from '@/api/system/xxx'

// 调用
const result = await xxxApi.list(params)
tableData.value = result.list || []  // 注意：是 list，不是 records！
```

### 检查清单
- [ ] 使用 aiperm-api-generator 技能生成 API
- [ ] API 文件路径与后端模块对应
- [ ] 类型定义与后端 DTO/VO 一致
- [ ] 使用 `result.list` 而非 `records`
- [ ] 使用 `result.pageNum` 而非 `page`

---

## Phase 5: 测试联调

### 后端测试

```bash
cd backend && ./gradlew build    # 编译
cd backend && ./gradlew test     # 单元测试
cd backend && ./gradlew bootRun  # 启动服务
```

### 接口测试

访问 Swagger UI：http://localhost:8080/api/swagger-ui.html

### 前端联调

```bash
cd frontend && pnpm run dev      # 启动前端
```

### 检查清单
- [ ] 后端编译无错误
- [ ] 后端单元测试通过
- [ ] Swagger UI 接口可访问
- [ ] 前端页面正常渲染
- [ ] CRUD 操作正常
- [ ] 权限控制生效

---

## 权限标识规范

```
{模块}:{功能}:{操作}

示例：
- system:xxx:list    # 列表查询
- system:xxx:query   # 详情查询
- system:xxx:create  # 新增
- system:xxx:update  # 更新
- system:xxx:delete  # 删除
```

## 常用命令

```bash
# 后端
cd backend && ./gradlew bootRun          # 启动后端
cd backend && ./gradlew build            # 编译
cd backend && ./gradlew test             # 测试

# 前端
cd frontend && pnpm run dev              # 启动前端
cd frontend && pnpm run build            # 构建
```
