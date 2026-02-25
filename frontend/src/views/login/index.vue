<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false,
})

// 加载状态
const loading = ref(false)

// 密码可见性
const passwordVisible = ref(false)

// 表单验证规则
const rules = computed<FormRules>(() => ({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度为 2-20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 个字符', trigger: 'blur' },
  ],
}))

// 处理登录
async function handleLogin() {
  if (!formRef.value)
    return

  try {
    await formRef.value.validate()
    loading.value = true

    // TODO: 调用实际的登录 API
    // const { authControllerLogin } = getScrmApi()
    // const res = await authControllerLogin(loginForm)
    // userStore.setToken(res.data.token)
    // userStore.setUserInfo(res.data.userInfo)

    // Mock 登录逻辑（待替换为真实 API）
    await mockLogin()
    ElMessage.success('登录成功')
    router.push('/dashboard')
  }
  catch (error) {
    console.error('登录失败:', error)
  }
  finally {
    loading.value = false
  }
}

// Mock 登录（开发阶段使用）
function mockLogin() {
  return new Promise<void>((resolve) => {
    setTimeout(() => {
      userStore.setToken('mock-token-' + Date.now())
      userStore.setUserInfo({
        id: 1,
        username: loginForm.username,
        nickname: '管理员',
        roles: ['admin'],
        permissions: ['*'],
      })
      resolve()
    }, 800)
  })
}

// 重置表单
function resetForm() {
  formRef.value?.resetFields()
}
</script>

<template>
  <div class="login-container flex items-center justify-center h-screen">
    <el-card class="login-card w-96">
      <template #header>
        <div class="text-center">
          <h1 class="text-2xl font-bold text-gray-800">
            AIPerm RBAC 系统
          </h1>
          <p class="text-sm text-gray-500 mt-2">
            权限管理系统
          </p>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        label-width="0"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            :type="passwordVisible ? 'text' : 'password'"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            @change="passwordVisible = !passwordVisible"
          />
        </el-form-item>

        <el-form-item>
          <div class="flex justify-between w-full">
            <el-checkbox v-model="loginForm.rememberMe">
              记住我
            </el-checkbox>
            <el-link type="primary" :underline="false">
              忘记密码？
            </el-link>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="w-full"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button
            class="w-full"
            @click="resetForm"
          >
            重 置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-container {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}
</style>
