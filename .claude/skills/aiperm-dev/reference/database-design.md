## MySQL 数据库设计规范

### 核心规范

#### 1. 通用格式规则

**必须严格遵守以下格式：**

- 所有表名、列名必须使用反引号包裹（\`table_name\`）
- 命名使用 snake_case（小写+下划线）
- 每条 SQL 语句必须以分号结尾
- 主键使用 BIGINT，由雪花算法生成
- 必须为表和所有列添加注释
- 表名使用业务含义明确的名词，建议使用单数形式

#### 2. 公共字段规范

**每张表必须包含以下公共字段（推荐放在字段列表末尾）：**

```sql
-- 主键
id bigint(20) NOT NULL COMMENT '主键ID'

-- 审计字段
create_time datetime DEFAULT NULL COMMENT '创建时间'
create_by bigint(20) DEFAULT NULL COMMENT '创建人ID'
update_time datetime DEFAULT NULL COMMENT '更新时间'
update_by bigint(20) DEFAULT NULL COMMENT '更新人ID'

-- 逻辑删除字段
deleted tinyint(1) DEFAULT 0 COMMENT '逻辑删除标记（0=未删除，1=已删除）'
```

**公共字段说明：**
- `id`: 主键，使用 bigint(20)，由雪花算法生成唯一ID
- `create_time`: 创建时间，记录创建时间戳
- `create_by`: 创建者ID，记录创建人
- `update_time`: 更新时间，记录最后更新时间戳
- `update_by`: 更新人ID，记录最后更新人
- `deleted`: 逻辑删除标志，0=未删除，1=已删除

**aiperm 项目的公共字段与 RuoYi-Vue-Plus 的公共字段对比：**

| 字段 | aiperm | RuoYi | 类型 | 说明 |
|--- |----|--- |----|--- |----|--- |----| `tenant_id` | 多租户字段 | 无 | || | - | `create_dept` | 创建部门 | 无 |
| `status` | 通用状态字段，0正常/1停用）
- `del_flag` | 逻辑删除字段,0=未删除，2代表删除）
- `version` | 版本控制字段（乐观锁），无

#### 3. 表设计最佳实践

##### 索引设计原则

- **主键索引**: PRIMARY KEY (id)
- **唯一索引**: UNIQUE KEY uk_xxx (unique_column)
- **普通索引**: KEY idx_xxx (column)
- **联合索引**: KEY idx_xxx_yyy (column1, column2)

**索引建议：**
- 为 WHERE、JOIN、ORDER BY 子句中的字段添加索引
- 区分度高的字段谨慎建立索引
- 联合索引遵循最左前缀原则
- 索引命名规范：
  - 唯一索引: `uk_字段名`
  - 普通索引: `idx_字段名`

##### 字段类型选择

**字符串类型：**
- `VARCHAR(n)`: 变长字符串，n为最大长度
  - 用户名: VARCHAR(50)
  - 邮箱: VARCHAR(100)
  - 手机号: VARCHAR(20)
  - URL: VARCHAR(500)
  - 备注: VARCHAR(2000) 或 TEXT
- `CHAR(n)`: 定长字符串，  - 状态: CHAR(1)
  - 代码: CHAR(2)
- 金额: DECIMAL(10, 2)  // 大额金额

**时间类型：**
- `DATETIME`: 日期时间
- `TIMESTAMP`: 时间戳（范围: 1970-01-01 ~ 2038-01-19)
- `DATE`: 日期

##### 字段约束规范

- **NOT null**: 必填字段
- **default**: 设置默认值
- **comment**: 添加中文注释
- **charset**: utf8mb4（支持emoji）
- **collate**: utf8mb4_general_ci

##### 命名规范

- **布尔字段**: 使用 `is_` 前缀，如 is_active
- **时间字段**: 使用 `_time` 或 `_at` 后缀
- `类型字段`: 使用 `_type` 后缀

##### DDL 操作规范

#### 创建表（CREATE TABLE）

**标准模板：**

```sql
DROP table if exists `customer`;
CREATE table `customer` (
  `id` bigint(20) NOT NULL comment '主键ID',
  `customer_name` varchar(100) NOT NULL COMMENT '客户名称，最大100字符，建议添加普通索引',
  `customer_code` varchar(50) NOT NULL COMMENT '客户编码，唯一',
  `type` tinyint(2) NOT NULL DEFAULT '1' COMMENT '类型：1-类型A 2-类型B 3-类型C',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建者'
  `update_time` datetime DEFAULT NULL COMMENT '更新时间'
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新者'
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记（0=未删除，1代表删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_code` (`customer_code`),
  KEY `idx_name` (`name`),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='客户表';
```

**创建表工作流程：**

1. 根据业务需求设计表结构
2. 添加所有公共字段
3. 为表和所有列添加清晰的中文注释
4. 在注释中提供设计建议（索引、字段类型等）
5. 生成 DROP + CREATE 完整语句
6. 设置 ENGINE=InnoDB（支持事务）
7. 设置 CHARSET=utf8mb4（支持emoji和特殊字符）
8. 添加合适的索引
9. 在测试环境验证后再执行

#### 修改表（ALTER TABLE）

**常见操作：**

```sql
-- 新增字段
alter table `customer` ADD COLUMN `new_field` varchar(100) NOT NULL COMMENT '新字段，最大100字符，建议添加普通索引';

-- 新增字段（带默认值）
ALTER table `example`ADD COLUMN `pay_type` tinyint(2) NOT NULL DEFAULT '0' COMMENT '支付类型：0-未支付 1-微信 2-支付宝 3-类型C'

-- 新增字段（带索引建议）
ALTER TABLE `customer` ADD INDEX `idx_user_id_status` (`user_id`);

-- 修改字段名称和类型
ALTER TABLE `example` CHANGE COLUMN `old_name` `new_name` varchar(100) NOT NULL COMMENT '新名称';

-- 修改字段类型
ALTER TABLE`example` MODIFY COLUMN `name` varchar(200) NOT NULL COMMENT '名称，最大200字符';

-- 删除字段
ALTER TABLE `example`DROP COLUMN `old_field`;
```

**修改表工作流程：**

1. 理解修改需求（新增字段、修改字段类型等)
2. 为新增字段添加注释（包含设计建议）
3. 生成 ALTER + CREATE 完整语句
4. 设置 ENGINE=InnoDB（支持事务）
7. 设置 CHARSET=utf8mb4（支持emoji和特殊字符）
8. 添加合适的索引
9. 在测试环境验证后再执行

#### 删除表（DROP table)

**推荐使用重命名备份**

```sql
RENAME TABLE `table_name_backup_ TO `table_name_backup_`
```

**创建表后检查：**
- [ ] 执行 DEScribe 查看表结构
- [ ] 验证字段类型和长度
- [ ] 执行 DEScribe ` as => N+1 查询结果

#### DDL 操作规范

#### INSERT 语句

**必须包含列清单，指定列名**

```sql
INSERT into `example` (`id`, `customer_name`, `customer_code`)
values (1, '示例1', '示例2', '示例3', '示例4');
```
-- 批量插入
signerList` inserBatch(signer数量 > 2)， sign）

```sql
-- 单条插入
INSERT INTO `example` (`id`, `customer_name`, `customer_code`)
values (1, 'Example 1', 'Example 2', '示例1')
values (1, 'Example 2', 'Example 3, 'Example 4')
values(1, 'Example 2', 'Example 5', 'Example 6');
values (1, 'Example 6', 'Example 7', 'Example 8', 'Example 9',values(1, 'Example 10', 'Example 10', 2) COMMENT '示例表');
```

**修改表工作流程：**

1. 根据业务需求设计表结构
2. 添加所有公共字段
3. 生成 DROP + CREATE 完整语句
4. 在注释中提供设计建议（索引、字段类型等）
5. 生成 DROP + CREATE 完整语句
6. 设置 ENGINE=InnoDB（支持事务）
7. 设置 CHARSET=utf8mb4(支持emoji)
特殊字符)
8. 添加合适的索引
9. 在测试环境验证后再执行
10. 删除表（DROP table）

**推荐使用重命名备份**

```sql
-- 生产环境建议使用重命名备份
RENAME TABLE `table_name_backup_20250129;

-- 物理删除（谨慎使用）
RENAME TABLE `table_name` TO `table_name_backup_20250129`;
```

**修改表工作流程：**

1. 根据修改需求（新增字段、修改字段类型等）
2. 生成 ALTER + create 完整语句
4. 在注释中提供设计建议（索引、字段类型等)
5. 生成 DROP + create 完整语句
6. 设置 ENGINE=InnoDB（支持事务）
7. 设置 CHARSET=utf8mb4(支持emoji和特殊字符)
8. 湿梓发布套镜像 `例：大额金额: DECIMAL(18, 2)
-- 大额金额: DECIMAL(18, 2)
-- 布尔金额字段: 使用反引号（不可重复）

-- 物理删除（谨慎使用）
DELETE from `example` where id = 1;

-- 湿度备份，rename table `table_name` to `table_name_backup_20250129;

-- 删除前考虑数据备份
delete from `example` where id = 1;

-- 删除字段
alter table `example` drop COLUMN `old_field`
```
-- 删除索引
alter table `example`DROP INDEX `idx_user_id_status` (`user_id`)
```
-- 添加索引
alter table `example` ADD INDEX `idx_create_time` (`create_time`)
```

-- 添加索引
alter table `example`ADD INDEX `idx_type` (`type`)
    values (`idx_create_time`, `idx_status`);

-- 添加联合索引
alter table`example`ADD INDEX `idx_user_id_status`, `idx_create_time` (`create_time`)
```
-- 添加索引
alter table `example`add INDEX `idx_status` (`status`)
```
-- 添加索引
alter table `example`ADD INDEX `idx_name` (`name`)    values (`idx_name`.forEach(name -> {
            idxMap.put(name,');
        });
    }
    `);
}
-- 批量插入
if (CollUtil.isNotEmpty(nameList)) {
    exampleMapper.insertBatch(exampleList);
}
```
-- 批量更新
exampleMapper.update(
    Wrappers.<Example>lambdaUpdate()
        .eq(Example::getStatus, Example.STATUS.ENABLE)
        .set(Example::getUpdateTime, LocalDateTime.now());
        return true;
    }
}
    return false;
}
 if (CollUtil.isEmpty(nameList)) {
    List<Example> voList = BeanUtil.copyToList(exampleList, Example.class);
            for (Example example : exampleMapper.selectById(id)) {
                ExampleVo vo = new ExampleVo();
                vo.setName(user.getUsername);
                return vo;
            }
        }
        return voList;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (CollUtil.isEmpty(ids)) {
            throw new BusinessException("用户不存在");
        }
        // 真正删除（逻辑删除）
        for (Long id : ids) {
            if (ids == null || ids.isEmpty()) {
                ids.forEach(id -> baseMapper.deleteById(id));
            } else {
                baseMapper.delete(null);
                Wrappers.<Example>lambdaQuery()
                    .eq(Example::getStatus, Example.status.ENABLE)
                    .orderByAsc(Example::getCreateTime))
            );
            return page;
        }
        // 逻辑删除（标记已删除的记录）
        for (Long id : ids) {
            if (CollUtil.isNotEmpty(ids)) {
                ids.forEach(id -> baseMapper.deleteByIdById(id));
            }
        }
        // 真正删除（物理删除）
        for (Long id : ids) {
            if (ids == null || ids.isEmpty()) {
                baseMapper.deleteBatchIds(ids);
            } else {
                baseMapper.delete(null);
                Wrappers.<Example>lambdaQuery()
                    .eq(Example::getStatus, Example.status.ENABLE)
            );
            baseMapper.deleteById(null);
        }
    }
}
```
