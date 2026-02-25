# AI 友好架构重构实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将 RBAC 系统从 7 层架构简化为 4 层，使用 JdbcClient 替代 MyBatis-Plus，提高 AI 代码生成效率。

**Architecture:** 使用 JdbcClient 替代 MyBatis-Plus，合并 Service 接口和实现，DTO 使用 Jackson 视图复用，保留 Sa-Token 权限控制。

**Tech Stack:** Spring Boot 3.5 + JdbcClient + Sa-Token + Redis + Jackson

**Design Doc:** `docs/plans/2026-02-24-ai-friendly-architecture-design.md`

---

## Phase 1: 基础设施准备

### Task 1: 创建 Jackson Views 视图类

**Files:**
- Create: `src/main/java/com/devlovecode/aiperm/common/domain/Views.java`

**Step 1: 创建 Views 类**

```java
package com.devlovecode.aiperm.common.domain;

/**
 * Jackson 视图定义，用于 DTO 多场景复用
 */
public class Views {
    /** 创建场景 */
    public interface Create {}
    /** 更新场景 */
    public interface Update {}
    /** 查询场景 */
    public interface Query {}
}
```

**Step 2: 验证编译**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add src/main/java/com/devlovecode/aiperm/common/domain/Views.java
git commit -m "feat: 添加 Jackson Views 视图类，用于 DTO 多场景复用"
```

---

### Task 2: 重构 BaseEntity（移除 MyBatis-Plus 注解）

**Files:**
- Modify: `src/main/java/com/devlovecode/aiperm/common/domain/BaseEntity.java`

**Step 1: 重构 BaseEntity**

将 MyBatis-Plus 注解替换为普通字段：

```java
package com.devlovecode.aiperm.common.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 所有实体类的父类，包含通用字段
 *
 * @author devlovecode
 */
@Data
@Schema(description = "基础实体类")
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "逻辑删除标记（0=未删除，1=已删除）")
    private Integer deleted;

    @Schema(description = "乐观锁版本号")
    private Integer version;
}
```

**Step 2: 验证编译**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL（会有其他文件的编译错误，先忽略）

**Step 3: Commit**

```bash
git add src/main/java/com/devlovecode/aiperm/common/domain/BaseEntity.java
git commit -m "refactor: 移除 BaseEntity 的 MyBatis-Plus 注解"
```

---

### Task 3: 重构 PageResult（移除 MyBatis-Plus 依赖）

**Files:**
- Modify: `src/main/java/com/devlovecode/aiperm/common/domain/PageResult.java`

**Step 1: 重构 PageResult**

移除 MyBatis-Plus 依赖：

```java
package com.devlovecode.aiperm.common.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页结果封装
 *
 * @param <T> 数据类型
 * @author devlovecode
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "数据列表")
    private List<T> list;

    @Schema(description = "当前页码")
    private Long pageNum;

    @Schema(description = "每页条数")
    private Long pageSize;

    @Schema(description = "总页数")
    private Long pages;

    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> of(Long total, List<T> list, Long pageNum, Long pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setList(list);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages(pageSize > 0 ? (total + pageSize - 1) / pageSize : 0L);
        return result;
    }

    /**
     * 构建空分页结果
     */
    public static <T> PageResult<T> empty(Long pageNum, Long pageSize) {
        return of(0L, Collections.emptyList(), pageNum, pageSize);
    }

    /**
     * 转换列表元素类型
     */
    public <R> PageResult<R> map(Function<T, R> mapper) {
        List<R> mappedList = this.list.stream()
                .map(mapper)
                .collect(Collectors.toList());
        return of(this.total, mappedList, this.pageNum, this.pageSize);
    }
}
```

**Step 2: 验证编译**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL（会有其他文件的编译错误，先忽略）

**Step 3: Commit**

```bash
git add src/main/java/com/devlovecode/aiperm/common/domain/PageResult.java
git commit -m "refactor: 移除 PageResult 的 MyBatis-Plus 依赖，添加 map 方法"
```

---

### Task 4: 创建 BaseRepository 通用基类

**Files:**
- Create: `src/main/java/com/devlovecode/aiperm/common/repository/BaseRepository.java`

**Step 1: 创建 BaseRepository**

```java
package com.devlovecode.aiperm.common.repository;

import com.devlovecode.aiperm.common.domain.PageResult;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository 通用基类
 * 提供基础 CRUD 操作
 *
 * @param <T> 实体类型
 */
public abstract class BaseRepository<T> {

    protected final JdbcClient db;
    protected final String tableName;
    protected final Class<T> entityClass;

    protected BaseRepository(JdbcClient db, String tableName, Class<T> entityClass) {
        this.db = db;
        this.tableName = tableName;
        this.entityClass = entityClass;
    }

    /**
     * 根据 ID 查询（排除已删除）
     */
    public Optional<T> findById(Long id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = :id AND deleted = 0";
        return db.sql(sql)
                .param("id", id)
                .query(entityClass)
                .optional();
    }

    /**
     * 查询所有（排除已删除）
     */
    public List<T> findAll() {
        String sql = "SELECT * FROM " + tableName + " WHERE deleted = 0 ORDER BY create_time DESC";
        return db.sql(sql).query(entityClass).list();
    }

    /**
     * 软删除
     */
    public int deleteById(Long id) {
        String sql = "UPDATE " + tableName + " SET deleted = 1, update_time = :updateTime WHERE id = :id";
        return db.sql(sql)
                .param("id", id)
                .param("updateTime", LocalDateTime.now())
                .update();
    }

    /**
     * 统计总数（排除已删除）
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE deleted = 0";
        Long count = db.sql(sql).query(Long.class).single();
        return count != null ? count : 0L;
    }

    /**
     * 检查是否存在（排除已删除）
     */
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE id = :id AND deleted = 0";
        Integer count = db.sql(sql).param("id", id).query(Integer.class).single();
        return count != null && count > 0;
    }

    /**
     * 通用分页查询
     */
    protected PageResult<T> queryPage(String whereClause, List<Object> params, int pageNum, int pageSize) {
        // 查总数
        String countSql = "SELECT COUNT(*) FROM " + tableName + " WHERE deleted = 0" + whereClause;
        Long total = db.sql(countSql).params(params).query(Long.class).single();

        if (total == null || total == 0) {
            return PageResult.empty((long) pageNum, (long) pageSize);
        }

        // 查列表
        String listSql = "SELECT * FROM " + tableName + " WHERE deleted = 0" + whereClause +
                " ORDER BY create_time DESC LIMIT :limit OFFSET :offset";
        List<Object> listParams = new java.util.ArrayList<>(params);
        listParams.add(pageSize);
        listParams.add((pageNum - 1) * pageSize);

        List<T> list = db.sql(listSql)
                .params(listParams)
                .query(entityClass)
                .list();

        return PageResult.of(total, list, (long) pageNum, (long) pageSize);
    }
}
```

**Step 2: 验证编译**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add src/main/java/com/devlovecode/aiperm/common/repository/BaseRepository.java
git commit -m "feat: 添加 BaseRepository 通用基类，提供基础 CRUD 操作"
```

---

### Task 5: 创建 SqlBuilder 动态 SQL 工具

**Files:**
- Create: `src/main/java/com/devlovecode/aiperm/common/repository/SqlBuilder.java`

**Step 1: 创建 SqlBuilder**

```java
package com.devlovecode.aiperm.common.repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态 SQL 构建器
 * 简化条件查询的 SQL 拼接
 */
public class SqlBuilder {

    private final StringBuilder sql;
    private final List<Object> params;

    public SqlBuilder() {
        this.sql = new StringBuilder();
        this.params = new ArrayList<>();
    }

    /**
     * 添加 WHERE 条件
     */
    public SqlBuilder where(String condition, Object... args) {
        if (sql.length() > 0) {
            sql.append(" AND ");
        } else {
            sql.append(" WHERE ");
        }
        sql.append(condition);
        for (Object arg : args) {
            params.add(arg);
        }
        return this;
    }

    /**
     * 添加 WHERE 条件（条件为 true 时）
     */
    public SqlBuilder whereIf(boolean condition, String clause, Object arg) {
        if (condition) {
            where(clause, arg);
        }
        return this;
    }

    /**
     * 添加 LIKE 条件（自动添加通配符）
     */
    public SqlBuilder likeIf(boolean condition, String column, String value) {
        if (condition && value != null && !value.isBlank()) {
            where(column + " LIKE ?", "%" + value + "%");
        }
        return this;
    }

    /**
     * 添加 ORDER BY
     */
    public SqlBuilder orderBy(String clause) {
        sql.append(" ORDER BY ").append(clause);
        return this;
    }

    /**
     * 获取 WHERE 子句
     */
    public String getWhereClause() {
        return sql.toString();
    }

    /**
     * 获取参数列表
     */
    public List<Object> getParams() {
        return params;
    }

    /**
     * 清空构建器
     */
    public void clear() {
        sql.setLength(0);
        params.clear();
    }
}
```

**Step 2: 验证编译**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add src/main/java/com/devlovecode/aiperm/common/repository/SqlBuilder.java
git commit -m "feat: 添加 SqlBuilder 动态 SQL 构建工具"
```

---

### Task 6: 更新 build.gradle（移除 MyBatis-Plus 和 MapStruct-Plus）

**Files:**
- Modify: `build.gradle`

**Step 1: 移除依赖**

删除以下依赖：
```groovy
// 删除这些行
implementation "com.baomidou:mybatis-plus-spring-boot3-starter:${mybatisPlusVersion}"
implementation "io.github.linpeilie:mapstruct-plus-spring-boot-starter:${mapStructPlusVersion}"
annotationProcessor "io.github.linpeilie:mapstruct-plus-processor:${mapStructPlusVersion}"
```

删除版本变量：
```groovy
// 删除这些行
set('mybatisPlusVersion', '3.5.9')
set('mapStructPlusVersion', '1.4.8')
```

**Step 2: 添加 JDBC 依赖（如果没有）**

确保有以下依赖（Spring Boot 内置，无需额外添加版本）：
```groovy
implementation 'org.springframework.boot:spring-boot-starter-jdbc'
```

**Step 3: 验证编译**

Run: `./gradlew compileJava`
Expected: 会有编译错误（旧代码依赖 MP），这是预期的

**Step 4: Commit**

```bash
git add build.gradle
git commit -m "refactor: 移除 MyBatis-Plus 和 MapStruct-Plus 依赖"
```

---

## Phase 2: 迁移字典模块（SysDictType）

### Task 7: 重构 SysDictType Entity

**Files:**
- Modify: `src/main/java/com/devlovecode/aiperm/modules/system/entity/SysDictType.java`

**Step 1: 移除 MyBatis-Plus 注解**

```java
package com.devlovecode.aiperm.modules.system.entity;

import com.devlovecode.aiperm.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典类型")
public class SysDictType extends BaseEntity {

    @Schema(description = "字典名称")
    private String dictName;

    @Schema(description = "字典类型（唯一标识，如 sys_gender）")
    private String dictType;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
```

**Step 2: 验证编译**

Run: `./gradlew compileJava`

**Step 3: Commit**

```bash
git add src/main/java/com/devlovecode/aiperm/modules/system/entity/SysDictType.java
git commit -m "refactor(dict): 移除 SysDictType 的 MyBatis-Plus 注解"
```

---

### Task 8: 创建 DictTypeRepository

**Files:**
- Create: `src/main/java/com/devlovecode/aiperm/modules/system/repository/DictTypeRepository.java`

**Step 1: 创建 Repository**

```java
package com.devlovecode.aiperm.modules.system.repository;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.repository.BaseRepository;
import com.devlovecode.aiperm.common.repository.SqlBuilder;
import com.devlovecode.aiperm.modules.system.entity.SysDictType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class DictTypeRepository extends BaseRepository<SysDictType> {

    public DictTypeRepository(JdbcClient db) {
        super(db, "sys_dict_type", SysDictType.class);
    }

    /**
     * 插入字典类型
     */
    public void insert(SysDictType entity) {
        String sql = """
            INSERT INTO sys_dict_type (dict_name, dict_type, status, remark, deleted, version, create_time, create_by)
            VALUES (:dictName, :dictType, :status, :remark, 0, 0, :createTime, :createBy)
            """;
        db.sql(sql)
                .param("dictName", entity.getDictName())
                .param("dictType", entity.getDictType())
                .param("status", entity.getStatus())
                .param("remark", entity.getRemark())
                .param("createTime", LocalDateTime.now())
                .param("createBy", entity.getCreateBy())
                .update();
    }

    /**
     * 更新字典类型
     */
    public int update(SysDictType entity) {
        String sql = """
            UPDATE sys_dict_type
            SET dict_name = :dictName, status = :status, remark = :remark,
                update_time = :updateTime, update_by = :updateBy
            WHERE id = :id AND deleted = 0
            """;
        return db.sql(sql)
                .param("dictName", entity.getDictName())
                .param("status", entity.getStatus())
                .param("remark", entity.getRemark())
                .param("updateTime", LocalDateTime.now())
                .param("updateBy", entity.getUpdateBy())
                .param("id", entity.getId())
                .update();
    }

    /**
     * 根据字典类型查询
     */
    public Optional<SysDictType> findByDictType(String dictType) {
        String sql = "SELECT * FROM sys_dict_type WHERE dict_type = :dictType AND deleted = 0";
        return db.sql(sql).param("dictType", dictType).query(SysDictType.class).optional();
    }

    /**
     * 检查字典类型是否存在
     */
    public boolean existsByDictType(String dictType) {
        String sql = "SELECT COUNT(*) FROM sys_dict_type WHERE dict_type = :dictType AND deleted = 0";
        Integer count = db.sql(sql).param("dictType", dictType).query(Integer.class).single();
        return count != null && count > 0;
    }

    /**
     * 检查字典类型是否存在（排除指定ID）
     */
    public boolean existsByDictTypeExcludeId(String dictType, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM sys_dict_type WHERE dict_type = :dictType AND id != :id AND deleted = 0";
        Integer count = db.sql(sql)
                .param("dictType", dictType)
                .param("id", excludeId)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    /**
     * 分页查询
     */
    public PageResult<SysDictType> queryPage(String dictName, String dictType, Integer status, int pageNum, int pageSize) {
        SqlBuilder sb = new SqlBuilder();
        sb.likeIf(dictName != null && !dictName.isBlank(), "dict_name", dictName)
          .likeIf(dictType != null && !dictType.isBlank(), "dict_type", dictType)
          .whereIf(status != null, "status = ?", status);

        return queryPage(sb.getWhereClause(), sb.getParams(), pageNum, pageSize);
    }

    /**
     * 查询启用的字典类型列表
     */
    public List<SysDictType> findAllEnabled() {
        String sql = "SELECT * FROM sys_dict_type WHERE status = 1 AND deleted = 0 ORDER BY create_time DESC";
        return db.sql(sql).query(SysDictType.class).list();
    }
}
```

**Step 2: 创建 repository 目录**

Run: `mkdir -p src/main/java/com/devlovecode/aiperm/modules/system/repository`

**Step 3: 验证编译**

Run: `./gradlew compileJava`

**Step 4: Commit**

```bash
git add src/main/java/com/devlovecode/aiperm/modules/system/repository/DictTypeRepository.java
git commit -m "feat(dict): 创建 DictTypeRepository，使用 JdbcClient"
```

---

### Task 9: 创建 DictTypeDTO（合并多个 Request）

**Files:**
- Create: `src/main/java/com/devlovecode/aiperm/modules/system/dto/DictTypeDTO.java`

**Step 1: 创建 DTO**

```java
package com.devlovecode.aiperm.modules.system.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "字典类型数据")
public class DictTypeDTO {

    // ========== 查询参数 ==========

    @JsonView(Views.Query.class)
    @Schema(description = "字典名称（模糊查询）")
    private String dictName;

    @JsonView(Views.Query.class)
    @Schema(description = "字典类型（模糊查询）")
    private String dictType;

    @JsonView(Views.Query.class)
    @Schema(description = "状态")
    private Integer status;

    @JsonView(Views.Query.class)
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @JsonView(Views.Query.class)
    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    // ========== 创建/更新参数 ==========

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "字典名称")
    @NotBlank(message = "字典名称不能为空", groups = Views.Create.class)
    @Size(max = 100, message = "字典名称不能超过100个字符")
    private String dictName;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    @Schema(description = "字典类型")
    @NotBlank(message = "字典类型不能为空", groups = Views.Create.class)
    @Size(max = 100, message = "字典类型不能超过100个字符")
    private String dictType;

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "备注")
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
```

**Step 2: 验证编译**

Run: `./gradlew compileJava`

**Step 3: Commit**

```bash
git add src/main/java/com/devlovecode/aiperm/modules/system/dto/DictTypeDTO.java
git commit -m "feat(dict): 创建 DictTypeDTO，使用 Jackson 视图复用"
```

---

### Task 10: 创建 DictTypeService（无接口）

**Files:**
- Create: `src/main/java/com/devlovecode/aiperm/modules/system/service/DictTypeService.java`

**Step 1: 创建 Service**

```java
package com.devlovecode.aiperm.modules.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.system.dto.DictTypeDTO;
import com.devlovecode.aiperm.modules.system.entity.SysDictType;
import com.devlovecode.aiperm.modules.system.repository.DictTypeRepository;
import com.devlovecode.aiperm.modules.system.vo.DictTypeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DictTypeService {

    private final DictTypeRepository dictTypeRepo;

    /**
     * 分页查询
     */
    public PageResult<DictTypeVO> queryPage(DictTypeDTO dto) {
        PageResult<SysDictType> result = dictTypeRepo.queryPage(
                dto.getDictName(), dto.getDictType(), dto.getStatus(),
                dto.getPage(), dto.getPageSize()
        );
        return result.map(this::toVO);
    }

    /**
     * 查询详情
     */
    public DictTypeVO findById(Long id) {
        return dictTypeRepo.findById(id)
                .map(this::toVO)
                .orElseThrow(() -> new BusinessException("字典类型不存在"));
    }

    /**
     * 创建
     */
    @Transactional
    public Long create(DictTypeDTO dto) {
        // 校验字典类型是否重复
        if (dictTypeRepo.existsByDictType(dto.getDictType())) {
            throw new BusinessException("字典类型已存在");
        }

        SysDictType entity = new SysDictType();
        entity.setDictName(dto.getDictName());
        entity.setDictType(dto.getDictType());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        entity.setRemark(dto.getRemark());
        entity.setCreateBy(getCurrentUsername());

        dictTypeRepo.insert(entity);

        // 获取自增ID
        return dictTypeRepo.findByDictType(dto.getDictType())
                .map(SysDictType::getId)
                .orElse(null);
    }

    /**
     * 更新
     */
    @Transactional
    public void update(Long id, DictTypeDTO dto) {
        SysDictType entity = dictTypeRepo.findById(id)
                .orElseThrow(() -> new BusinessException("字典类型不存在"));

        // 校验字典类型是否重复
        if (dictTypeRepo.existsByDictTypeExcludeId(dto.getDictType(), id)) {
            throw new BusinessException("字典类型已存在");
        }

        entity.setDictName(dto.getDictName());
        entity.setDictType(dto.getDictType());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        entity.setUpdateBy(getCurrentUsername());

        dictTypeRepo.update(entity);
    }

    /**
     * 删除
     */
    @Transactional
    public void delete(Long id) {
        if (!dictTypeRepo.existsById(id)) {
            throw new BusinessException("字典类型不存在");
        }
        dictTypeRepo.deleteById(id);
    }

    /**
     * 查询所有启用的字典类型
     */
    public List<DictTypeVO> findAllEnabled() {
        return dictTypeRepo.findAllEnabled().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    // ========== 私有方法 ==========

    private DictTypeVO toVO(SysDictType entity) {
        DictTypeVO vo = new DictTypeVO();
        vo.setId(entity.getId());
        vo.setDictName(entity.getDictName());
        vo.setDictType(entity.getDictType());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private String getCurrentUsername() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return "system";
        }
    }
}
```

**Step 2: 验证编译**

Run: `./gradlew compileJava`

**Step 3: Commit**

```bash
git add src/main/java/com/devlovecode/aiperm/modules/system/service/DictTypeService.java
git commit -m "feat(dict): 创建 DictTypeService（无接口）"
```

---

### Task 11: 重构 DictTypeController

**Files:**
- Modify: `src/main/java/com/devlovecode/aiperm/modules/system/controller/SysDictTypeController.java`

**Step 1: 重构 Controller**

```java
package com.devlovecode.aiperm.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.domain.Views;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.system.dto.DictTypeDTO;
import com.devlovecode.aiperm.modules.system.service.DictTypeService;
import com.devlovecode.aiperm.modules.system.vo.DictTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/dict/type")
@Tag(name = "字典类型管理")
@RequiredArgsConstructor
public class SysDictTypeController {

    private final DictTypeService dictTypeService;

    @GetMapping
    @Operation(summary = "分页查询字典类型")
    @SaCheckPermission("system:dict:list")
    @Log(title = "字典类型管理", operType = OperType.QUERY)
    public R<PageResult<DictTypeVO>> list(DictTypeDTO dto) {
        return R.ok(dictTypeService.queryPage(dto));
    }

    @GetMapping("/all")
    @Operation(summary = "查询所有启用的字典类型")
    @SaCheckPermission("system:dict:list")
    public R<List<DictTypeVO>> all() {
        return R.ok(dictTypeService.findAllEnabled());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询字典类型详情")
    @SaCheckPermission("system:dict:list")
    public R<DictTypeVO> detail(@PathVariable Long id) {
        return R.ok(dictTypeService.findById(id));
    }

    @PostMapping
    @Operation(summary = "创建字典类型")
    @SaCheckPermission("system:dict:create")
    @Log(title = "字典类型管理", operType = OperType.CREATE)
    public R<Long> create(@RequestBody @Validated({Default.class, Views.Create.class}) DictTypeDTO dto) {
        return R.ok(dictTypeService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新字典类型")
    @SaCheckPermission("system:dict:update")
    @Log(title = "字典类型管理", operType = OperType.UPDATE)
    public R<Void> update(@PathVariable Long id,
                          @RequestBody @Validated({Default.class, Views.Update.class}) DictTypeDTO dto) {
        dictTypeService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除字典类型")
    @SaCheckPermission("system:dict:delete")
    @Log(title = "字典类型管理", operType = OperType.DELETE)
    public R<Void> delete(@PathVariable Long id) {
        dictTypeService.delete(id);
        return R.ok();
    }
}
```

**Step 2: 验证编译**

Run: `./gradlew compileJava`

**Step 3: Commit**

```bash
git add src/main/java/com/devlovecode/aiperm/modules/system/controller/SysDictTypeController.java
git commit -m "refactor(dict): 重构 DictTypeController，使用新的 Service 和 DTO"
```

---

### Task 12: 删除旧的字典类型相关文件

**Files:**
- Delete: `src/main/java/com/devlovecode/aiperm/modules/system/mapper/SysDictTypeMapper.java`
- Delete: `src/main/java/com/devlovecode/aiperm/modules/system/service/ISysDictTypeService.java`
- Delete: `src/main/java/com/devlovecode/aiperm/modules/system/service/impl/SysDictTypeServiceImpl.java`
- Delete: `src/main/java/com/devlovecode/aiperm/modules/system/dto/request/DictTypeCreateRequest.java`
- Delete: `src/main/java/com/devlovecode/aiperm/modules/system/dto/request/DictTypeUpdateRequest.java`
- Delete: `src/main/resources/mapper/system/SysDictTypeMapper.xml`（如果存在）

**Step 1: 删除文件**

```bash
rm -f src/main/java/com/devlovecode/aiperm/modules/system/mapper/SysDictTypeMapper.java
rm -f src/main/java/com/devlovecode/aiperm/modules/system/service/ISysDictTypeService.java
rm -f src/main/java/com/devlovecode/aiperm/modules/system/service/impl/SysDictTypeServiceImpl.java
rm -f src/main/java/com/devlovecode/aiperm/modules/system/dto/request/DictTypeCreateRequest.java
rm -f src/main/java/com/devlovecode/aiperm/modules/system/dto/request/DictTypeUpdateRequest.java
rm -f src/main/resources/mapper/system/SysDictTypeMapper.xml
```

**Step 2: 验证编译**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL（字典模块编译通过）

**Step 3: Commit**

```bash
git add -A
git commit -m "refactor(dict): 删除旧的 Mapper、Service接口、Request DTO 文件"
```

---

### Task 13: 启动服务验证

**Step 1: 启动后端服务**

Run: `./gradlew bootRun`

**Step 2: 验证字典类型 API**

```bash
# 获取 Token（使用 admin 账号登录）
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.data.token')

# 查询字典类型列表
curl -s http://localhost:8080/api/system/dict/type \
  -H "Authorization: Bearer $TOKEN" | jq
```

**Step 3: 验证成功后 Commit**

```bash
git add -A
git commit -m "test(dict): 验证字典类型模块重构完成"
```

---

## Phase 3: 迁移其他模块

字典类型模块迁移完成后，按相同模式迁移其他模块：

### 迁移顺序

| Task | 模块 | 复杂度 |
|------|------|--------|
| 14-20 | SysDictData | 简单 |
| 21-27 | SysPost | 简单 |
| 28-34 | SysDept | 中等（树形） |
| 35-41 | SysMenu | 中等（权限关联） |
| 42-48 | SysRole | 复杂（多对多） |
| 49-55 | SysUser | 复杂（关联最多） |

每个模块的迁移步骤相同：
1. 重构 Entity（移除 MP 注解）
2. 创建 Repository
3. 创建 DTO（Jackson 视图）
4. 创建 Service（无接口）
5. 重构 Controller
6. 删除旧文件
7. 验证测试

---

## Phase 4: 清理

### Task 56: 删除 MyBatis-Plus 配置类

**Files:**
- Delete: `src/main/java/com/devlovecode/aiperm/config/MybatisPlusConfig.java`
- Delete: `src/main/java/com/devlovecode/aiperm/config/MyMetaObjectHandler.java`

### Task 57: 删除所有 Mapper XML

```bash
rm -rf src/main/resources/mapper/
```

### Task 58: 删除所有 Converter 文件

```bash
rm -rf src/main/java/com/devlovecode/aiperm/modules/*/converter/
```

### Task 59: 删除所有 Service 接口文件

```bash
rm -f src/main/java/com/devlovecode/aiperm/modules/*/service/I*.java
```

### Task 60: 更新 CLAUDE.md 文档

更新开发规范文档，反映新的架构约定。

### Task 61: 全量测试

Run: `./gradlew test`

### Task 62: 最终 Commit

```bash
git add -A
git commit -m "refactor: 完成 AI 友好架构重构

- 使用 JdbcClient 替代 MyBatis-Plus
- 合并 Service 接口和实现
- DTO 使用 Jackson 视图复用
- 移除 MapStruct-Plus
- 保留权限控制、操作审计、缓存、分页能力"
```

---

## 验收标准

- [ ] 所有模块编译通过
- [ ] 所有 API 接口正常工作
- [ ] 权限控制正常
- [ ] 操作日志正常记录
- [ ] 分页查询正常
- [ ] 前端 API 生成正常（`cd frontend && pnpm run generate:api`）
