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
  <div class="login-container">
    <!-- 左侧品牌区域 -->
    <div class="login-brand">
      <div class="brand-content">
        <div class="brand-logo">
          <img
            src="/logo.png"
            alt="爱编程"
            class="logo-img"
          >
        </div>
        <h1 class="brand-title">
          AIPerm 权限管理系统
        </h1>
        <p class="brand-subtitle">
          专业的企业级 RBAC 权限管理解决方案
        </p>
        <div class="brand-features">
          <div class="feature-item">
            <svg
              class="feature-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            </svg>
            <span>安全可靠</span>
          </div>
          <div class="feature-item">
            <svg
              class="feature-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
            </svg>
            <span>高效便捷</span>
          </div>
          <div class="feature-item">
            <svg
              class="feature-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <circle
                cx="12"
                cy="12"
                r="10"
              />
              <path d="M12 6v6l4 2" />
            </svg>
            <span>实时响应</span>
          </div>
        </div>
      </div>
      <!-- 装饰元素 -->
      <div class="brand-decoration">
        <div class="decoration-circle decoration-circle-1" />
        <div class="decoration-circle decoration-circle-2" />
        <div class="decoration-circle decoration-circle-3" />
      </div>
    </div>

    <!-- 右侧登录区域 -->
    <div class="login-form-wrapper">
      <div class="login-form-container">
        <!-- 移动端 Logo -->
        <div class="mobile-logo">
          <img
            src="/logo.png"
            alt="爱编程"
            class="logo-img-mobile"
          >
        </div>

        <div class="login-header">
          <h2 class="login-title">
            欢迎登录
          </h2>
          <p class="login-subtitle">
            请输入您的账号信息
          </p>
        </div>

        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          label-width="0"
          size="large"
          class="login-form"
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
            <div class="captcha-row">
              <el-input
                v-model="loginForm.captcha"
                placeholder="请输入验证码"
                :prefix-icon="Picture"
                clearable
                class="captcha-input"
              />
              <div
                class="captcha-box"
                title="点击刷新验证码"
                @click="fetchCaptcha"
              >
                <img
                  v-if="captchaData.captchaImage"
                  :src="captchaData.captchaImage"
                  alt="验证码"
                  class="captcha-img"
                >
                <div
                  v-else
                  class="captcha-loading"
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
              class="login-btn"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>

          <el-form-item>
            <el-button
              class="reset-btn"
              @click="resetForm"
            >
              重 置
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-tips">
          <span class="tips-label">默认账号：</span>
          <span class="tips-value">admin / admin123</span>
        </div>
      </div>

      <!-- 底部版权信息 -->
      <div class="login-footer">
        <p class="company-name">
          河南爱编程网络科技有限公司
        </p>
        <a
          href="https://beian.miit.gov.cn/"
          target="_blank"
          rel="noopener noreferrer"
          class="icp-link"
        >
          豫ICP备2024074107号-2
        </a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 左侧品牌区域 */
.login-brand {
  flex: 1;
  display: none;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.9) 0%, rgba(124, 58, 237, 0.9) 100%);
  color: white;
}

@media (min-width: 1024px) {
  .login-brand {
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.brand-content {
  position: relative;
  z-index: 10;
  text-align: center;
  padding: 3rem;
}

.brand-logo {
  margin-bottom: 2rem;
}

.logo-img {
  width: 80px;
  height: 80px;
  object-fit: contain;
  filter: brightness(0) invert(1);
}

.brand-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 0.75rem;
  letter-spacing: 0.025em;
}

.brand-subtitle {
  font-size: 1.125rem;
  opacity: 0.9;
  margin-bottom: 3rem;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.feature-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  font-size: 1rem;
}

.feature-icon {
  width: 24px;
  height: 24px;
  opacity: 0.9;
}

/* 装饰圆圈 */
.brand-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.decoration-circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
}

.decoration-circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  right: -50px;
}

.decoration-circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  right: 10%;
}

/* 右侧登录区域 */
.login-form-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
}

@media (min-width: 1024px) {
  .login-form-wrapper {
    max-width: 500px;
    flex: none;
  }
}

.login-form-container {
  width: 100%;
  max-width: 380px;
}

/* 移动端 Logo */
.mobile-logo {
  display: flex;
  justify-content: center;
  margin-bottom: 1.5rem;
}

@media (min-width: 1024px) {
  .mobile-logo {
    display: none;
  }
}

.logo-img-mobile {
  width: 60px;
  height: 60px;
  object-fit: contain;
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.login-title {
  font-size: 1.75rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.login-subtitle {
  font-size: 0.9375rem;
  color: #64748b;
}

.login-form {
  margin-bottom: 1.5rem;
}

.captcha-row {
  display: flex;
  width: 100%;
  gap: 0.75rem;
}

.captcha-input {
  flex: 1;
}

.captcha-box {
  width: 120px;
  height: 40px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.captcha-box:hover {
  border-color: #2563eb;
}

.captcha-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.captcha-loading {
  font-size: 0.75rem;
  color: #94a3b8;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 1rem;
  font-weight: 500;
}

.reset-btn {
  width: 100%;
  height: 40px;
}

.login-tips {
  text-align: center;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 8px;
  font-size: 0.875rem;
}

.tips-label {
  color: #64748b;
}

.tips-value {
  color: #2563eb;
  font-weight: 500;
}

/* 底部版权 */
.login-footer {
  margin-top: 2rem;
  text-align: center;
}

.company-name {
  font-size: 0.875rem;
  color: #64748b;
  margin-bottom: 0.25rem;
}

.icp-link {
  font-size: 0.75rem;
  color: #94a3b8;
  text-decoration: none;
  transition: color 0.2s ease;
}

.icp-link:hover {
  color: #2563eb;
}

/* Element Plus 样式覆盖 */
:deep(.el-input__wrapper) {
  padding: 0.25rem 0.75rem;
}

:deep(.el-input__inner) {
  height: 42px;
}
</style>
