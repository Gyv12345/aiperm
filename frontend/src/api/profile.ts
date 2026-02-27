import request from '@/utils/request'
import type { PageResult } from '@/types'

export interface ProfileVO {
  id: number
  username: string
  nickname: string
  realName: string
  email: string
  phone: string
  gender: number
  avatar: string
  deptId: number
  deptName: string
  postName: string
  roleNames: string[]
  status: number
  lastLoginIp: string
  lastLoginTime: string
  createTime: string
}

export interface ProfileDTO {
  nickname: string
  realName: string
  email: string
  phone: string
  gender: number
  avatar: string
}

export interface PasswordDTO {
  oldPassword: string
  newPassword: string
}

export interface LoginLogVO {
  id: number
  ip: string
  location: string
  browser: string
  os: string
  status: number
  msg: string
  loginTime: string
}

export const profileApi = {
  // 获取个人信息
  getInfo: () => request.get<ProfileVO>('/profile/info'),

  // 修改个人信息
  updateInfo: (data: ProfileDTO) => request.put('/profile/info', data),

  // 修改密码
  updatePassword: (data: PasswordDTO) => request.put('/profile/password', data),

  // 获取登录日志
  getLogs: (pageNum: number = 1, pageSize: number = 10) =>
    request.get<PageResult<LoginLogVO>>('/profile/logs', {
      params: { pageNum, pageSize },
    }),
}
