/**
 * API 统一导出
 * 所有 API 模块的统一入口
 *
 * 注意：禁止使用 Orval 自动生成
 * 所有 API 均根据后端 Controller 手动编写
 */

// 认证模块
export * from './auth'

// 文件管理
export * from './oss'

// 操作日志
export * from './log'

// 系统管理模块
export * from './system/user'
export * from './system/role'
export * from './system/menu'
export * from './system/post'
export * from './system/dept'
export * from './system/dict'
export * from './system/imConfig'
export * from './system/approvalScene'

// 企业管理模块
export * from './enterprise/notice'
export * from './enterprise/message'
export * from './enterprise/job'
export * from './enterprise/config'
export * from './enterprise/messageTemplate'
export * from './enterprise/messageLog'

// 审批中心
export * from './approval'
