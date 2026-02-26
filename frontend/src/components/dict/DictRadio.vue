<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { dictApi, type DictDataVO } from '@/api/system/dict'

/**
 * 字典单选组件（用于表单，选项 ≤ 4 项时使用）
 *
 * 用法：
 * <DictRadio v-model="form.status" dict-type="sys_status" />
 */
const props = defineProps<{
  /** 字典类型标识 */
  dictType: string
  /** 是否禁用 */
  disabled?: boolean
}>()

const model = defineModel<string | number | null>()
const options = ref<DictDataVO[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    options.value = await dictApi.dataList(props.dictType)
  }
  catch {
    options.value = []
  }
  finally {
    loading.value = false
  }
})
</script>

<template>
  <el-radio-group
    v-model="model"
    v-loading="loading"
    :disabled="disabled"
  >
    <el-radio
      v-for="item in options"
      :key="item.dictValue"
      :value="item.dictValue"
    >
      {{ item.dictLabel }}
    </el-radio>
  </el-radio-group>
</template>
