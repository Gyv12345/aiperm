<script setup lang="ts">
import { computed } from 'vue'
import type { DictDataVO } from '@/api/system/dict'

/**
 * 字典标签展示组件（模仿若依）
 *
 * 用法：
 * <DictTag :options="sys_status" :value="row.status" />
 */
const props = defineProps<{
  /** 字典数据列表（由 useDict 提供） */
  options: DictDataVO[]
  /** 当前值（与 dictValue 比对） */
  value: string | number | null | undefined
}>()

// Element Plus Tag 预设类型
const PRESET_TYPES = ['default', 'primary', 'success', 'warning', 'danger', 'info']

const matched = computed(() =>
  props.options?.find(item => String(item.dictValue) === String(props.value))
)

const tagType = computed(() => {
  const cls = matched.value?.listClass || ''
  if (!cls || !PRESET_TYPES.includes(cls)) return ''
  return cls === 'default' ? '' : cls
})

const tagStyle = computed(() => {
  const cls = matched.value?.listClass || ''
  if (!cls || PRESET_TYPES.includes(cls)) return undefined
  // 自定义颜色（十六进制）
  return {
    backgroundColor: cls,
    borderColor: cls,
    color: '#fff',
  }
})
</script>

<template>
  <el-tag
    v-if="matched"
    :type="tagType as any"
    :style="tagStyle"
    size="small"
  >
    {{ matched.dictLabel }}
  </el-tag>
  <span v-else>{{ value ?? '' }}</span>
</template>
