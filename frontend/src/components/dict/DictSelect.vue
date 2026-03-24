<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {dictApi, type DictDataVO} from '@/api/system/dict'

/**
 * 字典下拉选择组件
 *
 * 用法：
 * <DictSelect v-model="form.status" dict-type="sys_status" />
 * <DictSelect v-model="form.status" dict-type="sys_status" placeholder="请选择状态" clearable />
 */
const props = defineProps<{
  /** 字典类型 */
  dictType: string
  /** 占位符 */
  placeholder?: string
  /** 是否可清空 */
  clearable?: boolean
  /** 是否禁用 */
  disabled?: boolean
}>()

const model = defineModel<string | number | null>()

// Element Plus Tag 预设类型
const PRESET_TYPES = ['default', 'primary', 'success', 'warning', 'danger', 'info']

const options = ref<DictDataVO[]>([])
const loading = ref(false)

function getTagStyle(listClass: string | undefined) {
  if (!listClass || PRESET_TYPES.includes(listClass)) return undefined
  return {
    backgroundColor: listClass,
    borderColor: listClass,
    color: '#fff',
  }
}

function getTagType(listClass: string | undefined): string {
  if (!listClass || !PRESET_TYPES.includes(listClass)) return ''
  return listClass === 'default' ? '' : listClass
}

onMounted(async () => {
  loading.value = true
  try {
    options.value = await dictApi.dataList(props.dictType)
  }
  finally {
    loading.value = false
  }
})
</script>

<template>
  <el-select
    v-model="model"
    :placeholder="placeholder ?? '请选择'"
    :clearable="clearable"
    :disabled="disabled"
    :loading="loading"
  >
    <el-option
      v-for="item in options"
      :key="item.dictValue"
      :label="item.dictLabel"
      :value="item.dictValue"
    >
      <div class="flex items-center gap-2">
        <el-tag
          :type="getTagType(item.listClass) as any"
          :style="getTagStyle(item.listClass)"
          size="small"
        >
          {{ item.dictLabel }}
        </el-tag>
      </div>
    </el-option>
  </el-select>
</template>
