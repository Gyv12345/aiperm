<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { profileApi, type ProfileVO, type ProfileDTO } from '@/api/profile'
import { useUserStore } from '@/stores/user'
import { useDict } from '@/composables/useDict'
import DictTag from '@/components/dict/DictTag.vue'

// 字典
const dictData = useDict('sys_status')
const sys_status = dictData.sys_status!

const activeTab = ref('info')
const userStore = useUserStore()

// 基本信息
const loading = ref(false)
const saving = ref(false)
const profile = ref<ProfileVO | null>(null)
const formRef = ref()
const profileForm = ref<ProfileDTO>({
  nickname: '',
  realName: '',
  email: '',
  phone: '',
  gender: 0,
  avatar: '',
})

const profileRules = {
  nickname: [{ max: 50, message: '昵称不能超过50个字符', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }],
  phone: [{ max: 20, message: '手机号码不能超过20个字符', trigger: 'blur' }],
}

// 修改密码
const passwordFormRef = ref()
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const passwordSaving = ref(false)

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

// 登录日志
const logsLoading = ref(false)
const logs = ref<any[]>([])

// 获取个人信息
async function fetchProfile() {
  loading.value = true
  try {
    const data = await profileApi.getInfo()
    profile.value = data
    profileForm.value = {
      nickname: data.nickname || '',
      realName: data.realName || '',
      email: data.email || '',
      phone: data.phone || '',
      gender: data.gender || 0,
      avatar: data.avatar || '',
    }
  } catch (error) {
    console.error('获取个人信息失败:', error)
  } finally {
    loading.value = false
  }
}

// 保存个人信息
async function handleSaveProfile() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await profileApi.updateInfo(profileForm.value)
    ElMessage.success('保存成功')
    fetchProfile()
    // 更新 userStore
    if (profileForm.value.nickname) {
      userStore.setUserInfo({
        ...userStore.userInfo,
        nickname: profileForm.value.nickname,
      })
    }
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

// 修改密码
async function handleChangePassword() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  passwordSaving.value = true
  try {
    await profileApi.updatePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
    })
    ElMessage.success('密码修改成功')
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    passwordFormRef.value?.resetFields()
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    passwordSaving.value = false
  }
}

// 获取登录日志
async function fetchLogs() {
  logsLoading.value = true
  try {
    const data = await profileApi.getLoginLogs({ page: 1, pageSize: 20 })
    logs.value = data?.list || []
  } catch (error) {
    console.error('获取登录日志失败:', error)
  } finally {
    logsLoading.value = false
  }
}

// Tab 切换时加载数据
function handleTabChange(tab: string) {
  if (tab === 'logs' && logs.value.length === 0) {
    fetchLogs()
  }
}

onMounted(() => {
  fetchProfile()
})
</script>

<template>
  <div class="profile-page p-6">
    <el-card shadow="never">
      <template #header>
        <div class="flex items-center gap-2">
          <el-icon class="text-blue-500">
            <User />
          </el-icon>
          <span class="font-semibold">个人中心</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 基本信息 Tab -->
        <el-tab-pane label="基本信息" name="info">
          <el-skeleton :loading="loading" animated>
            <el-form
              ref="formRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="100px"
              class="max-w-2xl mt-4"
            >
              <el-form-item label="用户名">
                <el-input :model-value="profile?.username" disabled />
              </el-form-item>

              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
              </el-form-item>

              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
              </el-form-item>

              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
              </el-form-item>

              <el-form-item label="手机号" prop="phone">
                <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
              </el-form-item>

              <el-form-item label="性别">
                <el-radio-group v-model="profileForm.gender">
                  <el-radio :value="0">未知</el-radio>
                  <el-radio :value="1">男</el-radio>
                  <el-radio :value="2">女</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="部门">
                <el-input :model-value="profile?.deptName || '-'" disabled />
              </el-form-item>

              <el-form-item label="岗位">
                <el-input :model-value="profile?.postName || '-'" disabled />
              </el-form-item>

              <el-form-item label="角色">
                <el-tag
                  v-for="role in profile?.roleNames"
                  :key="role"
                  class="mr-2"
                >
                  {{ role }}
                </el-tag>
                <span v-if="!profile?.roleNames?.length">-</span>
              </el-form-item>

              <el-form-item label="状态">
                <DictTag :options="sys_status" :value="profile?.status" />
              </el-form-item>

              <el-form-item label="创建时间">
                <span>{{ profile?.createTime || '-' }}</span>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" :loading="saving" @click="handleSaveProfile">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </el-skeleton>
        </el-tab-pane>

        <!-- 修改密码 Tab -->
        <el-tab-pane label="修改密码" name="password">
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            class="max-w-md mt-4"
          >
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入旧密码"
                show-password
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（6-20位）"
                show-password
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="passwordSaving" @click="handleChangePassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 登录日志 Tab -->
        <el-tab-pane label="登录日志" name="logs">
          <el-table :data="logs" v-loading="logsLoading" class="mt-4">
            <el-table-column prop="loginTime" label="登录时间" width="180" />
            <el-table-column prop="ip" label="登录IP" width="140" />
            <el-table-column prop="location" label="登录地点" />
            <el-table-column prop="browser" label="浏览器" />
            <el-table-column prop="os" label="操作系统" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="msg" label="提示消息" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.profile-page {
  background-color: var(--color-bg-page);
  min-height: calc(100vh - 64px);
}
</style>
