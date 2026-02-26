<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock, Picture } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { authApi, type LoginRequest, type CaptchaVO } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const loginForm = reactive<LoginRequest>({
  username: '',
  password: '',
  captcha: '',
  captchaKey: '',
})

// 验证码数据
const captchaData = ref<CaptchaVO>({
  captchaKey: '',
  captchaImage: '',
})

// 加载状态
const loading = ref(false)

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
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 4, max: 4, message: '验证码为 4 位字符', trigger: 'blur' },
  ],
}))

// 获取验证码
async function fetchCaptcha() {
  try {
    const captchaInfo = await authApi.captcha()
    if (captchaInfo) {
      captchaData.value = {
        captchaKey: captchaInfo.captchaKey || '',
        captchaImage: captchaInfo.captchaImage || '',
      }
      loginForm.captchaKey = captchaInfo.captchaKey || ''
    }
  }
  catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败，请刷新页面')
  }
}

// 处理登录
async function handleLogin() {
  if (!formRef.value)
    return

  try {
    await formRef.value.validate()
    loading.value = true

    const loginData = await authApi.login(loginForm)

    if (loginData) {
      // 保存 token
      userStore.setToken(loginData.token || '')

      // 保存用户信息
      if (loginData.nickname) {
        userStore.setUserInfo({
          id: 0,
          username: loginData.username || '',
          nickname: loginData.nickname || '',
          roles: ['admin'],
          permissions: ['*'],
        })
      }

      ElMessage.success('登录成功')
      router.push('/')
    }
  }
  catch (error: any) {
    console.error('登录失败:', error)
    // 刷新验证码
    fetchCaptcha()
    loginForm.captcha = ''
  }
  finally {
    loading.value = false
  }
}

// 重置表单
function resetForm() {
  formRef.value?.resetFields()
  fetchCaptcha()
}

// 页面加载时获取验证码
onMounted(() => {
  fetchCaptcha()
})
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
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="captcha">
          <div class="flex w-full gap-2">
            <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              :prefix-icon="Picture"
              clearable
              class="flex-1"
            />
            <div
              class="captcha-box cursor-pointer"
              @click="fetchCaptcha"
            >
              <img
                v-if="captchaData.captchaImage"
                :src="captchaData.captchaImage"
                alt="验证码"
                class="h-10 w-24 object-contain"
              >
              <div
                v-else
                class="h-10 w-24 bg-gray-100 flex items-center justify-center text-xs text-gray-400"
              >
                点击刷新
              </div>
            </div>
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

      <div class="text-center text-xs text-gray-400 mt-4">
        默认账号: admin / admin123
      </div>
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

.captcha-box {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
}

.captcha-box:hover {
  border-color: #409eff;
}
</style>
