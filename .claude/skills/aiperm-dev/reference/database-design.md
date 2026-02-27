## MySQL 数据库设计规范

### 核心规范

#### 1. 通用格式规则

- 所有表名、列名必须使用反引号包裹（\`table_name\`）
- 命名使用 snake_case（小写+下划线）
- 每条 SQL 语句必须以分号结尾
- 主键使用 BIGINT AUTO_INCREMENT
- 必须为表和所有列添加注释
- 表名使用业务含义明确的名词，建议使用单数形式

#### 2. 公共字段规范

**每张表必须包含以下公共字段（放在字段列表末尾）：**

```sql
`deleted`     TINYINT   DEFAULT 0 COMMENT '逻辑删除标记（0=未删除，1=已删除）',
`version`     INT       DEFAULT 0 COMMENT '版本号（乐观锁）',
`create_time` DATETIME  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`create_by`   VARCHAR(50) DEFAULT NULL COMMENT '创建人',
`update_time` DATETIME  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`update_by`   VARCHAR(50) DEFAULT NULL COMMENT '更新人',
PRIMARY KEY (`id`)
```

**公共字段说明：**
- `id`: 主键，使用 BIGINT AUTO_INCREMENT
- `deleted`: 逻辑删除标志，0=未删除，1=已删除
- `version`: 版本控制字段（乐观锁）
- `create_time`: 创建时间
- `create_by`: 创建者用户名
- `update_time`: 更新时间
- `update_by`: 更新者用户名

#### 3. 建表模板

```sql
CREATE TABLE IF NOT EXISTS `sys_xxx` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '名称',
    `status`      INT         DEFAULT 0 COMMENT '状态（0=正常，1=停用）',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted`     TINYINT     DEFAULT 0 COMMENT '逻辑删除标记（0=未删除，1=已删除）',
    `version`     INT         DEFAULT 0 COMMENT '版本号',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`   VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`   VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='xxx表';
```

#### 4. 索引设计原则

- **主键索引**: PRIMARY KEY (id)
- **唯一索引**: UNIQUE KEY uk_xxx (unique_column)
- **普通索引**: KEY idx_xxx (column)
- **联合索引**: KEY idx_xxx_yyy (column1, column2)

**索引命名规范：**
- 唯一索引: `uk_字段名`
- 普通索引: `idx_字段名`

#### 5. 字段类型选择

| 用途 | 类型 | 示例 |
|------|------|------|
| 用户名 | VARCHAR(50) | username |
| 邮箱 | VARCHAR(100) | email |
| 手机号 | VARCHAR(20) | phone |
| URL | VARCHAR(500) | avatar |
| 备注 | VARCHAR(500) 或 TEXT | remark |
| 状态 | INT | status |
| 时间 | DATETIME | create_time |
| 金额 | DECIMAL(10, 2) | price |

#### 6. Flyway 迁移脚本规范

- **位置**: `backend/src/main/resources/db/migration/`
- **命名**: `Vx.x.x__描述.sql`（如 `V2.1.0__add_dict_tables.sql`）
- **版本号**: 必须比现有最高版本大
- **禁止**: 直接修改已执行的脚本

#### 7. 常用 ALTER 语句

```sql
-- 新增字段
ALTER TABLE `sys_xxx` ADD COLUMN `new_field` VARCHAR(100) DEFAULT NULL COMMENT '新字段';

-- 修改字段类型
ALTER TABLE `sys_xxx` MODIFY COLUMN `name` VARCHAR(200) NOT NULL COMMENT '名称';

-- 添加索引
ALTER TABLE `sys_xxx` ADD INDEX `idx_create_time` (`create_time`);

-- 删除字段
ALTER TABLE `sys_xxx` DROP COLUMN `old_field`;
```
