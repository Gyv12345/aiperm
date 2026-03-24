<script setup lang="ts">
import {ref} from 'vue'
import {ElMessage} from 'element-plus'
import {type PasswordDTO, profileApi} from '@/api/profile'

const saving = ref(false)
const formRef = ref()

const form = ref<PasswordDTO>({
  oldPassword: '',
  newPassword: '',
})

const confirmPassword = ref('')

const rules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度必须在6-100个字符之间', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== form.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

// 修改密码
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await profileApi.updatePassword(form.value)
    ElMessage.success('密码修改成功')
    // 清空表单
    form.value = { oldPassword: '', newPassword: '' }
    confirmPassword.value = ''
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="profile-password p-6">
    <el-card
      shadow="never"
      class="max-w-xl"
    >
      <template #header>
        <div class="flex items-center gap-2">
          <el-icon class="text-orange-500">
            <Lock />
          </el-icon>
          <span class="font-semibold">修改密码</span>
        </div>
      </template>

      <el-alert
        type="warning"
        title="密码安全提示"
        :closable="false"
        class="mb-6"
      >
        <p>1. 密码长度至少6个字符</p>
        <p>2. 建议使用字母、数字、特殊字符组合</p>
        <p>3. 新密码不能与旧密码相同</p>
      </el-alert>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item
          label="旧密码"
          prop="oldPassword"
        >
          <el-input
            v-model="form.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            show-password
          />
        </el-form-item>

        <el-form-item
          label="新密码"
          prop="newPassword"
        >
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item
          label="确认密码"
          prop="confirmPassword"
        >
          <el-input
            v-model="confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="saving"
            @click="handleSubmit"
          >
            确认修改
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.profile-password {
  background-color: var(--color-bg-page);
}
</style>
