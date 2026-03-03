<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { imConfigApi, type ImConfigVO } from '@/api/system/imConfig'

const loading = ref(false)
const saving = ref(false)
const list = ref<ImConfigVO[]>([])

async function fetchList() {
  loading.value = true
  try {
    list.value = await imConfigApi.list()
  } finally {
    loading.value = false
  }
}

async function handleSave(row: ImConfigVO) {
  saving.value = true
  try {
    await imConfigApi.update(row.platform, {
      enabled: row.enabled,
      appId: row.appId,
      appSecret: row.appSecret,
      corpId: row.corpId,
      callbackToken: row.callbackToken,
      callbackAesKey: row.callbackAesKey,
      extraConfig: row.extraConfig,
    })
    ElMessage.success('保存成功')
    fetchList()
  } finally {
    saving.value = false
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="p-4">
    <el-card>
      <template #header>
        <div class="font-semibold">
          IM 平台配置
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="list"
        border
      >
        <el-table-column
          prop="platform"
          label="平台"
          width="120"
        />
        <el-table-column
          label="启用"
          width="90"
        >
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              :active-value="1"
              :inactive-value="0"
            />
          </template>
        </el-table-column>
        <el-table-column
          prop="appId"
          label="应用ID"
        >
          <template #default="{ row }">
            <el-input
              v-model="row.appId"
              placeholder="App ID"
            />
          </template>
        </el-table-column>
        <el-table-column
          prop="corpId"
          label="企业ID"
        >
          <template #default="{ row }">
            <el-input
              v-model="row.corpId"
              placeholder="Corp ID"
            />
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="120"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :loading="saving"
              @click="handleSave(row)"
            >
              保存
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
