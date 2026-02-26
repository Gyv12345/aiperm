import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

export interface FormOptions<T> {
  defaultValues?: Partial<T>
  onSubmit?: (values: T) => Promise<void>
  rules?: FormRules
}

export function useForm<T extends Record<string, any>>(options: FormOptions<T> = {}) {
  const { defaultValues = {}, onSubmit, rules = {} } = options

  const formRef = ref<FormInstance>()
  const loading = ref(false)
  const formData = reactive<T>({ ...defaultValues } as T)

  const validate = async (): Promise<boolean> => {
    if (!formRef.value) return true
    try {
      await formRef.value.validate()
      return true
    } catch {
      return false
    }
  }

  const handleSubmit = async () => {
    const isValid = await validate()
    if (!isValid) return false

    if (!onSubmit) return true

    loading.value = true
    try {
      await onSubmit({ ...formData } as T)
      return true
    } catch (error) {
      console.error('Form submit error:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  const resetForm = () => {
    formRef.value?.resetFields()
    Object.assign(formData, defaultValues)
  }

  const setFormData = (values: Partial<T>) => {
    Object.assign(formData, values)
  }

  return {
    formRef,
    formData,
    loading,
    rules,
    validate,
    handleSubmit,
    resetForm,
    setFormData
  }
}
