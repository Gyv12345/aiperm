import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'uno.css'

import App from './App.vue'
import { setupRouter } from './router'
import permissionDirectives from './directives/permission'

const app = createApp(App)

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 创建 Pinia 实例并配置持久化插件
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

app.use(pinia)
app.use(ElementPlus)

// 注册权限指令
app.directive('permission', permissionDirectives.permission)
app.directive('role', permissionDirectives.role)

setupRouter(app)

app.mount('#app')
