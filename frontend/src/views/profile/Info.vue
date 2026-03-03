<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { profileApi, type ProfileVO, type ProfileDTO } from '@/api/profile'
import { useDict } from '@/composables/useDict'
import DictTag from '@/components/dict/DictTag.vue'

// 字典
const dictData = useDict('sys_status')
const sys_status = dictData.sys_status!

const loading = ref(false)
const saving = ref(false)
const profile = ref<ProfileVO | null>(null)
const formRef = ref()

const form = ref<ProfileDTO>({
  nickname: '',
  realName: '',
  email: '',
  phone: '',
  gender: 0,
  avatar: '',
})

const rules = {
  nickname: [{ max: 50, message: '昵称不能超过50个字符', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }],
  phone: [{ max: 20, message: '手机号码不能超过20个字符', trigger: 'blur' }],
}

// 获取个人信息
const fetchProfile = async () => {
  loading.value = true
  try {
    const data = await profileApi.getInfo()
    profile.value = data
    form.value = {
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
const handleSave = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await profileApi.updateInfo(form.value)
    ElMessage.success('保存成功')
    fetchProfile()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchProfile()
})
</script>

<template>
  <div class="profile-info p-6">
    <el-card
      shadow="never"
      class="max-w-3xl"
    >
      <template #header>
        <div class="flex items-center gap-2">
          <el-icon class="text-blue-500">
            <UserFilled />
          </el-icon>
          <span class="font-semibold">基本信息</span>
        </div>
      </template>

      <el-skeleton
        :loading="loading"
        animated
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          class="mt-4"
        >
          <el-form-item label="用户名">
            <el-input
              :model-value="profile?.username"
              disabled
            />
          </el-form-item>

          <el-form-item
            label="昵称"
            prop="nickname"
          >
            <el-input
              v-model="form.nickname"
              placeholder="请输入昵称"
            />
          </el-form-item>

          <el-form-item
            label="真实姓名"
            prop="realName"
          >
            <el-input
              v-model="form.realName"
              placeholder="请输入真实姓名"
            />
          </el-form-item>

          <el-form-item
            label="邮箱"
            prop="email"
          >
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱"
            />
          </el-form-item>

          <el-form-item
            label="手机号"
            prop="phone"
          >
            <el-input
              v-model="form.phone"
              placeholder="请输入手机号"
            />
          </el-form-item>

          <el-form-item label="性别">
            <el-radio-group v-model="form.gender">
              <el-radio :value="0">
                未知
              </el-radio>
              <el-radio :value="1">
                男
              </el-radio>
              <el-radio :value="2">
                女
              </el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="部门">
            <el-input
              :model-value="profile?.deptName || '-'"
              disabled
            />
          </el-form-item>

          <el-form-item label="岗位">
            <el-input
              :model-value="profile?.postName || '-'"
              disabled
            />
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
            <DictTag
              :options="sys_status"
              :value="profile?.status"
            />
          </el-form-item>

          <el-form-item label="创建时间">
            <span>{{ profile?.createTime || '-' }}</span>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="saving"
              @click="handleSave"
            >
              保存修改
            </el-button>
          </el-form-item>
        </el-form>
      </el-skeleton>
    </el-card>
  </div>
</template>

<style scoped>
.profile-info {
  background-color: var(--color-bg-page);
}
</style>
