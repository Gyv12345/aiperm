import {reactive, ref, toRefs} from 'vue'
import {dictApi, type DictDataVO} from '@/api/system/dict'

/**
 * 字典数据组合式函数（模仿若依）
 *
 * 用法：
 * const { sys_status } = useDict('sys_status')
 * const { sys_status, sys_user_sex } = useDict('sys_status', 'sys_user_sex')
 */
export function useDict(...dictTypes: string[]) {
  const res = reactive<Record<string, DictDataVO[]>>(
    Object.fromEntries(dictTypes.map(type => [type, []]))
  )

  for (const dictType of dictTypes) {
    dictApi.dataList(dictType)
      .then((data) => {
        res[dictType] = data
      })
      .catch(() => {
        res[dictType] = []
      })
  }

  return toRefs(res) as Record<string, ReturnType<typeof ref<DictDataVO[]>>>
}
