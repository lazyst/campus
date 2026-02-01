import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './styles/index.scss'

// 抑制 Element Plus 内部警告
const originalWarn = console.warn
console.warn = (...args) => {
  const message = args.join(' ')
  if (message.includes('ElementPlus') ||
      message.includes('ElPagination') ||
      message.includes('Deprecated') ||
      message.includes('Invalid prop')) {
    return
  }
  originalWarn.apply(console, args)
}

const app = createApp(App)

// 抑制 Element Plus 内部错误警告
const originalErrorHandler = app.config.errorHandler
app.config.errorHandler = (err, instance, info) => {
  if (err.message && (
    err.message.includes('ElementPlus') ||
    err.message.includes('ElPagination') ||
    err.message.includes('Deprecated') ||
    err.message.includes('Invalid prop')
  )) {
    return
  }
  if (originalErrorHandler) {
    originalErrorHandler(err, instance, info)
  }
}

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
  size: 'default'
})

app.mount('#app')
