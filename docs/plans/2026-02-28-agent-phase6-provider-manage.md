# AI Agent Phase 6: LLM 提供商管理

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 创建 LLM 提供商和 Agent 配置的后端接口及前端管理界面

**Architecture:** 后端 CRUD 接口 + 前端管理页面

**Tech Stack:** Spring Boot 3.5 + Vue 3 + Element Plus

---

## Task 1: 创建后端 DTO/VO

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/dto/LlmProviderDTO.java`
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/vo/LlmProviderVO.java`

**Step 1: 创建 LlmProviderDTO**

```java
package com.devlovecode.aiperm.modules.agent.dto;

import com.devlovecode.aiperm.common.domain.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LlmProviderDTO {
    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private Long id;

    @NotBlank(message = "提供商名称不能为空", groups = {Views.Create.class})
    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private String name;

    @NotBlank(message = "显示名称不能为空", groups = {Views.Create.class, Views.Update.class})
    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private String displayName;

    @NotBlank(message = "API Key不能为空", groups = {Views.Create.class, Views.Update.class})
    @JsonView({Views.Create.class, Views.Update.class})
    private String apiKey;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private String baseUrl;

    @NotBlank(message = "模型名称不能为空", groups = {Views.Create.class, Views.Update.class})
    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private String model;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private Boolean isDefault;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private Integer status;

    @JsonView({Views.Create.class, Views.Update.class, Views.Query.class})
    private Integer sort;

    @JsonView({Views.Create.class, Views.Update.class})
    private String remark;
}
```

**Step 2: 创建 LlmProviderVO**

```java
package com.devlovecode.aiperm.modules.agent.vo;

import lombok.Data;

@Data
public class LlmProviderVO {
    private Long id;
    private String name;
    private String displayName;
    private String baseUrl;
    private String model;
    private Boolean isDefault;
    private Integer status;
    private Integer sort;
    private String remark;
    private String createTime;
    private String updateTime;
}
```

**Step 3: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/dto/LlmProviderDTO.java
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/vo/LlmProviderVO.java
git commit -m "feat(agent): add LlmProvider DTO and VO"
```

---

## Task 2: 创建 LlmProviderService

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/LlmProviderService.java`

**Step 1: 创建 Service**

```java
package com.devlovecode.aiperm.modules.agent.service;

import com.devlovecode.aiperm.common.domain.PageResult;
import com.devlovecode.aiperm.common.exception.BusinessException;
import com.devlovecode.aiperm.modules.agent.dto.LlmProviderDTO;
import com.devlovecode.aiperm.modules.agent.entity.SysLlmProvider;
import com.devlovecode.aiperm.modules.agent.repository.LlmProviderRepository;
import com.devlovecode.aiperm.modules.agent.vo.LlmProviderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmProviderService {

    private final LlmProviderRepository providerRepo;

    public List<LlmProviderVO> listAll() {
        return providerRepo.findAll().stream()
            .map(this::toVO)
            .collect(Collectors.toList());
    }

    public LlmProviderVO findById(Long id) {
        return providerRepo.findById(id)
            .map(this::toVO)
            .orElseThrow(() -> new BusinessException("提供商不存在"));
    }

    @Transactional
    public Long create(LlmProviderDTO dto) {
        // 检查名称是否已存在
        if (providerRepo.findByName(dto.getName()).isPresent()) {
            throw new BusinessException("提供商名称已存在");
        }

        SysLlmProvider entity = new SysLlmProvider();
        entity.setName(dto.getName());
        entity.setDisplayName(dto.getDisplayName());
        entity.setApiKey(dto.getApiKey());
        entity.setBaseUrl(dto.getBaseUrl() != null ? dto.getBaseUrl() : getDefaultBaseUrl(dto.getName()));
        entity.setModel(dto.getModel());
        entity.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        entity.setSort(dto.getSort() != null ? dto.getSort() : 0);
        entity.setRemark(dto.getRemark());

        providerRepo.insert(entity);

        // 如果设为默认，清除其他默认
        if (Boolean.TRUE.equals(entity.getIsDefault())) {
            providerRepo.clearDefault();
            providerRepo.setDefault(entity.getId());
        }

        return entity.getId();
    }

    @Transactional
    public void update(Long id, LlmProviderDTO dto) {
        SysLlmProvider entity = providerRepo.findById(id)
            .orElseThrow(() -> new BusinessException("提供商不存在"));

        entity.setDisplayName(dto.getDisplayName());
        if (dto.getApiKey() != null) {
            entity.setApiKey(dto.getApiKey());
        }
        entity.setBaseUrl(dto.getBaseUrl());
        entity.setModel(dto.getModel());
        entity.setStatus(dto.getStatus());
        entity.setSort(dto.getSort());
        entity.setRemark(dto.getRemark());

        providerRepo.update(entity);

        // 处理默认设置
        if (Boolean.TRUE.equals(dto.getIsDefault()) && !Boolean.TRUE.equals(entity.getIsDefault())) {
            providerRepo.clearDefault();
            providerRepo.setDefault(id);
        }
    }

    @Transactional
    public void delete(Long id) {
        SysLlmProvider provider = providerRepo.findById(id)
            .orElseThrow(() -> new BusinessException("提供商不存在"));

        if (Boolean.TRUE.equals(provider.getIsDefault())) {
            throw new BusinessException("不能删除默认提供商");
        }

        providerRepo.deleteById(id);
    }

    @Transactional
    public void setDefault(Long id) {
        if (providerRepo.findById(id).isEmpty()) {
            throw new BusinessException("提供商不存在");
        }

        providerRepo.clearDefault();
        providerRepo.setDefault(id);
    }

    private String getDefaultBaseUrl(String name) {
        return switch (name) {
            case "deepseek" -> "https://api.deepseek.com";
            case "qwen" -> "https://dashscope.aliyuncs.com/compatible-mode/v1";
            case "openai" -> "https://api.openai.com";
            default -> null;
        };
    }

    private LlmProviderVO toVO(SysLlmProvider entity) {
        LlmProviderVO vo = new LlmProviderVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDisplayName(entity.getDisplayName());
        vo.setBaseUrl(entity.getBaseUrl());
        vo.setModel(entity.getModel());
        vo.setIsDefault(entity.getIsDefault());
        vo.setStatus(entity.getStatus());
        vo.setSort(entity.getSort());
        vo.setRemark(entity.getRemark());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (entity.getCreateTime() != null) {
            vo.setCreateTime(entity.getCreateTime().format(formatter));
        }
        if (entity.getUpdateTime() != null) {
            vo.setUpdateTime(entity.getUpdateTime().format(formatter));
        }

        return vo;
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/service/LlmProviderService.java
git commit -m "feat(agent): add LlmProviderService"
```

---

## Task 3: 创建 LlmProviderController

**Files:**
- Create: `backend/src/main/java/com/devlovecode/aiperm/modules/agent/controller/LlmProviderController.java`

**Step 1: 创建 Controller**

```java
package com.devlovecode.aiperm.modules.agent.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.agent.dto.LlmProviderDTO;
import com.devlovecode.aiperm.modules.agent.service.LlmProviderService;
import com.devlovecode.aiperm.modules.agent.vo.LlmProviderVO;
import com.devlovecode.aiperm.common.annotation.Log;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "LLM提供商管理")
@RestController
@RequestMapping("/agent/provider")
@SaCheckLogin
@RequiredArgsConstructor
public class LlmProviderController {

    private final LlmProviderService providerService;

    @Operation(summary = "查询所有提供商")
    @SaCheckPermission("agent:provider:list")
    @GetMapping
    public R<List<LlmProviderVO>> list() {
        return R.ok(providerService.listAll());
    }

    @Operation(summary = "查询提供商详情")
    @SaCheckPermission("agent:provider:query")
    @GetMapping("/{id}")
    public R<LlmProviderVO> getById(@PathVariable Long id) {
        return R.ok(providerService.findById(id));
    }

    @Operation(summary = "新增提供商")
    @SaCheckPermission("agent:provider:create")
    @Log(title = "LLM提供商管理", operType = OperType.CREATE)
    @PostMapping
    public R<Long> create(@RequestBody @Validated LlmProviderDTO dto) {
        return R.ok(providerService.create(dto));
    }

    @Operation(summary = "更新提供商")
    @SaCheckPermission("agent:provider:update")
    @Log(title = "LLM提供商管理", operType = OperType.UPDATE)
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody @Validated LlmProviderDTO dto) {
        providerService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除提供商")
    @SaCheckPermission("agent:provider:delete")
    @Log(title = "LLM提供商管理", operType = OperType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        providerService.delete(id);
        return R.ok();
    }

    @Operation(summary = "设为默认")
    @SaCheckPermission("agent:provider:update")
    @Log(title = "LLM提供商管理", operType = OperType.UPDATE)
    @PutMapping("/{id}/default")
    public R<Void> setDefault(@PathVariable Long id) {
        providerService.setDefault(id);
        return R.ok();
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/main/java/com/devlovecode/aiperm/modules/agent/controller/LlmProviderController.java
git commit -m "feat(agent): add LlmProviderController"
```

---

## Task 4: 创建前端 API

**Files:**
- Create: `frontend/src/api/agent/provider.ts`

**Step 1: 创建 API**

```typescript
// frontend/src/api/agent/provider.ts

import { request } from '@/utils/request'

const BASE_URL = '/agent/provider'

export interface LlmProvider {
  id: number
  name: string
  displayName: string
  baseUrl: string
  model: string
  isDefault: boolean
  status: number
  sort: number
  remark: string
  createTime: string
  updateTime: string
}

export interface LlmProviderDTO {
  id?: number
  name: string
  displayName: string
  apiKey?: string
  baseUrl?: string
  model: string
  isDefault?: boolean
  status?: number
  sort?: number
  remark?: string
}

export function listProviders() {
  return request.get<LlmProvider[]>(BASE_URL)
}

export function getProvider(id: number) {
  return request.get<LlmProvider>(`${BASE_URL}/${id}`)
}

export function createProvider(data: LlmProviderDTO) {
  return request.post<number>(BASE_URL, data)
}

export function updateProvider(id: number, data: LlmProviderDTO) {
  return request.put(`${BASE_URL}/${id}`, data)
}

export function deleteProvider(id: number) {
  return request.delete(`${BASE_URL}/${id}`)
}

export function setDefaultProvider(id: number) {
  return request.put(`${BASE_URL}/${id}/default`)
}
```

**Step 2: Commit**

```bash
git add frontend/src/api/agent/provider.ts
git commit -m "feat(agent): add LLM provider API"
```

---

## Task 5: 创建前端管理页面

**Files:**
- Create: `frontend/src/views/agent/ProviderManage.vue`

**Step 1: 创建管理页面**

```vue
<!-- frontend/src/views/agent/ProviderManage.vue -->
<template>
  <div class="provider-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>LLM 提供商管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
        </div>
      </template>

      <el-table :data="providers" v-loading="loading" stripe>
        <el-table-column prop="displayName" label="名称" width="150" />
        <el-table-column prop="name" label="标识" width="120" />
        <el-table-column prop="model" label="模型" width="150" />
        <el-table-column prop="baseUrl" label="API 地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="默认" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button
              link
              type="primary"
              @click="handleSetDefault(row)"
              :disabled="row.isDefault"
            >
              设为默认
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(row)"
              :disabled="row.isDefault"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editId ? '编辑提供商' : '新增提供商'"
      width="500px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标识" prop="name" v-if="!editId">
          <el-select v-model="form.name" placeholder="选择提供商" @change="handleProviderChange">
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="通义千问" value="qwen" />
            <el-option label="OpenAI" value="openai" />
          </el-select>
        </el-form-item>
        <el-form-item label="显示名称" prop="displayName">
          <el-input v-model="form.displayName" placeholder="显示名称" />
        </el-form-item>
        <el-form-item label="API Key" prop="apiKey">
          <el-input
            v-model="form.apiKey"
            type="password"
            placeholder="API Key"
            show-password
          />
        </el-form-item>
        <el-form-item label="API 地址" prop="baseUrl">
          <el-input v-model="form.baseUrl" placeholder="API 地址" />
        </el-form-item>
        <el-form-item label="模型" prop="model">
          <el-input v-model="form.model" placeholder="模型名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="0" :inactive-value="1" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  listProviders,
  createProvider,
  updateProvider,
  deleteProvider,
  setDefaultProvider,
  type LlmProvider,
  type LlmProviderDTO
} from '@/api/agent/provider'

const loading = ref(false)
const providers = ref<LlmProvider[]>([])
const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<LlmProviderDTO>({
  name: '',
  displayName: '',
  apiKey: '',
  baseUrl: '',
  model: '',
  isDefault: false,
  status: 0,
  remark: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请选择提供商', trigger: 'change' }],
  displayName: [{ required: true, message: '请输入显示名称', trigger: 'blur' }],
  apiKey: [{ required: true, message: '请输入 API Key', trigger: 'blur' }],
  model: [{ required: true, message: '请输入模型名称', trigger: 'blur' }]
}

const providerDefaults: Record<string, { displayName: string; baseUrl: string; model: string }> = {
  deepseek: { displayName: 'DeepSeek', baseUrl: 'https://api.deepseek.com', model: 'deepseek-chat' },
  qwen: { displayName: '通义千问', baseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1', model: 'qwen-plus' },
  openai: { displayName: 'OpenAI', baseUrl: 'https://api.openai.com', model: 'gpt-4o-mini' }
}

const handleProviderChange = (name: string) => {
  const defaults = providerDefaults[name]
  if (defaults) {
    form.displayName = defaults.displayName
    form.baseUrl = defaults.baseUrl
    form.model = defaults.model
  }
}

const loadProviders = async () => {
  loading.value = true
  try {
    const res = await listProviders()
    providers.value = res.data
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editId.value = null
  Object.assign(form, {
    name: '',
    displayName: '',
    apiKey: '',
    baseUrl: '',
    model: '',
    isDefault: false,
    status: 0,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: LlmProvider) => {
  editId.value = row.id
  Object.assign(form, {
    name: row.name,
    displayName: row.displayName,
    apiKey: '',
    baseUrl: row.baseUrl,
    model: row.model,
    isDefault: row.isDefault,
    status: row.status,
    remark: row.remark
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (editId.value) {
        await updateProvider(editId.value, form)
        ElMessage.success('更新成功')
      } else {
        await createProvider(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadProviders()
    } catch (e: any) {
      ElMessage.error(e.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleSetDefault = async (row: LlmProvider) => {
  try {
    await ElMessageBox.confirm(`确定将 "${row.displayName}" 设为默认提供商？`, '提示')
    await setDefaultProvider(row.id)
    ElMessage.success('设置成功')
    loadProviders()
  } catch {
    // 用户取消
  }
}

const handleDelete = async (row: LlmProvider) => {
  try {
    await ElMessageBox.confirm(`确定删除 "${row.displayName}"？`, '警告', {
      type: 'warning'
    })
    await deleteProvider(row.id)
    ElMessage.success('删除成功')
    loadProviders()
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  loadProviders()
})
</script>

<style scoped>
.provider-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
```

**Step 2: Commit**

```bash
git add frontend/src/views/agent/ProviderManage.vue
git commit -m "feat(agent): add LLM provider management page"
```

---

## Task 6: 添加路由和菜单

**Files:**
- Modify: `frontend/src/router/index.ts`
- Modify: 后端菜单数据

**Step 1: 添加路由**

```typescript
// 在路由配置中添加
{
  path: '/agent',
  component: Layout,
  meta: { title: '智能助手', icon: 'Cpu' },
  children: [
    {
      path: 'provider',
      name: 'AgentProvider',
      component: () => import('@/views/agent/ProviderManage.vue'),
      meta: { title: 'LLM提供商', icon: 'Connection' }
    }
  ]
}
```

**Step 2: 添加菜单 (通过数据库或管理界面)**

```sql
-- 添加 Agent 管理菜单
INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, sort, status)
VALUES (0, '智能助手', '/agent', NULL, NULL, 'M', 5, 0);

-- 获取刚插入的菜单 ID，假设为 100
INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, sort, status)
VALUES (100, 'LLM提供商', '/agent/provider', 'agent/ProviderManage', 'agent:provider:list', 'C', 1, 0);
```

**Step 3: Commit**

```bash
git add frontend/src/router/
git commit -m "feat(agent): add agent management routes"
```

---

## Completion Checklist

- [ ] LlmProviderDTO/VO 已创建
- [ ] LlmProviderService 已创建
- [ ] LlmProviderController 已创建
- [ ] 前端 API 已创建
- [ ] 前端管理页面已创建
- [ ] 路由和菜单已添加
- [ ] 编译通过

---

## Next Phase

继续执行 Phase 7: 语义缓存实现
