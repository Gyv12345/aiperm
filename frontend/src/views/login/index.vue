<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loginForm = reactive({
  username: '',
  password: '',
})

const loading = ref(false)

function handleLogin() {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true

  // TODO: 实现登录逻辑
  setTimeout(() => {
    localStorage.setItem('access_token', 'demo-token')
    ElMessage.success('登录成功')
    router.push('/dashboard')
    loading.value = false
  }, 1000)
}
</script>

<template>
  <div class="login-container flex items-center justify-center h-screen bg-gray-100">
    <el-card class="w-96">
      <template #header>
        <div class="text-center text-xl font-bold">
          AIPerm RBAC 系统
        </div>
      </template>

      <el-form :model="loginForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="w-full"
            @click="handleLogin"
          >
            登录
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
</style>
