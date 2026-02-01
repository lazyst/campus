# 校园互助平台用户前端开发规范指南

## 一、项目结构规范

### 1.1 目录结构

用户前端项目采用 Vue 3 技术栈，目录结构按照功能类型进行组织。所有代码位于 `frontend-user/src/` 目录下。

```
frontend-user/src/
├── api/                           # API 接口定义
│   ├── auth.js                    # 认证相关接口
│   ├── user.js                    # 用户相关接口
│   ├── post.js                    # 帖子相关接口
│   ├── comment.js                 # 评论相关接口
│   ├── item.js                    # 闲置相关接口
│   ├── message.js                 # 消息相关接口
│   └── notification.js            # 通知相关接口
├── components/                    # 通用组件
│   ├── form/                      # 表单组件
│   │   ├── InputField.vue         # 输入框组件
│   │   ├── TextareaField.vue      # 文本域组件
│   │   └── SelectField.vue        # 选择器组件
│   ├── list/                      # 列表组件
│   │   ├── PostList.vue           # 帖子列表组件
│   │   ├── ItemList.vue           # 闲置列表组件
│   │   └── UserList.vue           # 用户列表组件
│   ├── modal/                     # 弹窗组件
│   │   └── ConfirmModal.vue       # 确认弹窗
│   └── common/                    # 其他通用组件
│       ├── Avatar.vue             # 头像组件
│       ├── Badge.vue              # 徽章组件
│       └── EmptyState.vue         # 空状态组件
├── views/                         # 页面组件
│   ├── home/                      # 首页模块
│   │   ├── index.vue              # 首页
│   │   └── components/            # 首页子组件
│   ├── post/                      # 帖子模块
│   │   ├── index.vue              # 帖子列表页
│   │   ├── detail.vue             # 帖子详情页
│   │   └── create.vue             # 发布帖子页
│   ├── item/                      # 闲置模块
│   │   ├── index.vue              # 闲置列表页
│   │   ├── detail.vue             # 闲置详情页
│   │   └── create.vue             # 发布闲置页
│   ├── message/                   # 消息模块
│   │   ├── index.vue              # 消息列表页
│   │   └── chat.vue               # 聊天页面
│   ├── profile/                   # 个人中心模块
│   │   ├── index.vue              # 个人主页
│   │   ├── edit.vue               # 编辑资料
│   │   └── settings.vue           # 设置页面
│   └── auth/                      # 认证模块
│       ├── login.vue              # 登录页
│       └── register.vue           # 注册页
├── router/                        # 路由配置
│   └── index.js                   # 路由定义
├── stores/                        # Pinia 状态管理
│   ├── user.js                    # 用户状态
│   ├── auth.js                    # 认证状态
│   └── message.js                 # 消息状态
├── utils/                         # 工具函数
│   ├── request.js                 # axios 请求封装
│   ├── format.js                  # 格式化工具
│   └── validate.js                # 校验工具
├── styles/                        # 样式文件
│   ├── main.css                   # 主样式文件
│   └── tailwind.css               # Tailwind CSS 配置
├── App.vue                        # 根组件
└── main.js                        # 入口文件
```

### 1.2 文件命名规则

组件文件使用 PascalCase 命名法，以 `.vue` 为后缀。文件名应该清晰表达组件的功能和用途。页面组件放置在 `views/` 目录下，通用组件放置在 `components/` 目录下。

```
# 页面组件示例
PostList.vue      # 帖子列表组件
PostDetail.vue    # 帖子详情组件
UserProfile.vue   # 用户资料组件

# 组件文件示例
PrimaryButton.vue  # 主要按钮组件
SearchBar.vue      # 搜索栏组件
PullRefresh.vue    # 下拉刷新组件
```

## 二、组件开发规范

### 2.1 组件设计原则

组件设计遵循单一职责原则，每个组件只负责一个明确的功能。组件应该具有高内聚、低耦合的特点，内部实现对外部透明。组件通过 props 接收配置参数，通过事件与父组件通信，通过插槽实现内容分发。

**组件分类**：

- **基础组件**：封装最底层的 UI 元素，如按钮、输入框、图标等
- **业务组件**：封装可复用的业务逻辑，如帖子卡片、用户卡片等
- **页面组件**：组合各种组件实现完整的页面功能

### 2.2 Props 定义

组件的 props 应该定义明确的类型、默认值和校验规则。使用 TypeScript 的类型定义增强代码可维护性。

```vue
<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  // 必填属性
  title: {
    type: String,
    required: true
  },
  // 带默认值的属性
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  },
  // 布尔类型属性
  disabled: {
    type: Boolean,
    default: false
  },
  // 对象类型属性
  user: {
    type: Object,
    default: () => ({
      id: 0,
      name: ''
    })
  },
  // 数组类型属性
  tags: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['click', 'change', 'update:modelValue'])
</script>
```

### 2.3 事件定义

组件通过事件向父组件传递信息。事件名称使用 kebab-case 格式，遵循 DOM 事件命名惯例。

```vue
<script setup>
import { defineEmits } from 'vue'

const emit = defineEmits(['click', 'change', 'update:modelValue'])

// 触发点击事件
const handleClick = () => {
  emit('click')
}

// 触发值变更事件
const handleChange = (value) => {
  emit('change', value)
}
</script>
```

### 2.4 插槽使用

插槽用于实现组件的内容分发。具名插槽用于分发不同位置的内容，作用域插槽用于向父组件暴露子组件的数据。

```vue
<template>
  <div class="card">
    <div class="card-header">
      <slot name="header">
        <span class="default-header">{{ title }}</span>
      </slot>
    </div>
    <div class="card-body">
      <slot :user="user">
        <p>默认内容</p>
      </slot>
    </div>
    <div class="card-footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>
```

## 三、API 调用规范

### 3.1 接口模块组织

API 接口按照业务模块组织在 `api/` 目录下，每个模块对应一个独立的文件。接口文件使用统一的请求封装函数发起 HTTP 请求。

```javascript
// api/post.js
import { request } from '@/utils/request'

export function getPostList(params) {
  return request.get('/api/post', { params })
}

export function getPostDetail(id) {
  return request.get(`/api/post/${id}`)
}

export function createPost(data) {
  return request.post('/api/post', data)
}

export function updatePost(id, data) {
  return request.put(`/api/post/${id}`, data)
}

export function deletePost(id) {
  return request.delete(`/api/post/${id}`)
}

export function likePost(id) {
  return request.post(`/api/post/${id}/like`)
}

export function unlikePost(id) {
  return request.delete(`/api/post/${id}/like`)
}
```

### 3.2 请求拦截

请求拦截器用于统一处理请求参数、添加认证信息、处理请求错误。

```javascript
// utils/request.js
import axios from 'axios'
import { showToast } from 'vant'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加认证 Token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    // 显示错误提示
    showToast(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    // 处理认证错误
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      // 跳转到登录页
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default request
```

### 3.3 响应处理

API 响应采用统一的封装格式，响应处理遵循以下规范：

```javascript
// 组件中使用 API
import { getPostDetail } from '@/api/post'
import { ref } from 'vue'

const post = ref(null)
const loading = ref(false)

const fetchPost = async (id) => {
  loading.value = true
  try {
    const res = await getPostDetail(id)
    post.value = res.data
  } catch (error) {
    console.error('获取帖子详情失败:', error)
  } finally {
    loading.value = false
  }
}
```

## 四、状态管理规范

### 4.1 Store 设计

使用 Pinia 进行状态管理，每个业务域对应一个独立的 Store。Store 使用组合式 API 风格定义。

```javascript
// stores/user.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserProfile, updateUserProfile, uploadAvatar } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // 状态
  const userInfo = ref(null)
  const isLoggedIn = computed(() => !!userInfo.value)

  // 获取用户信息
  async function fetchUserInfo() {
    const res = await getUserProfile()
    userInfo.value = res.data
  }

  // 更新用户信息
  async function updateProfile(data) {
    const res = await updateUserProfile(data)
    userInfo.value = { ...userInfo.value, ...res.data }
  }

  // 上传头像
  async function uploadAvatarImage(file) {
    const res = await uploadAvatar(file)
    if (userInfo.value) {
      userInfo.value.avatar = res.data.url
    }
  }

  // 登出
  function logout() {
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    userInfo,
    isLoggedIn,
    fetchUserInfo,
    updateProfile,
    uploadAvatarImage,
    logout
  }
})
```

### 4.2 状态更新

状态更新遵循单向数据流原则。Store 中的状态通过 Action 进行更新，组件通过调用 Store Action 触发状态变化。

```vue
<script setup>
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const userStore = useUserStore()
// 使用 storeToRefs 保持响应式
const { userInfo, isLoggedIn } = storeToRefs(userStore)

// 调用 Action 更新状态
const handleLogout = () => {
  userStore.logout()
}
</script>
```

### 4.3 持久化

敏感信息存储在内存中，常用数据可以通过 Pinia 插件实现持久化存储。

```javascript
// stores/user.js 持久化示例
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo')) || null)

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo
  }
})
```

## 五、样式规范

### 5.1 Tailwind CSS 使用

用户前端使用 Tailwind CSS 框架进行样式开发。Tailwind 提供了丰富的工具类，可以组合使用构建任意样式。

**常用工具类**：

```vue
<template>
  <div class="p-4 m-4 bg-white rounded-lg shadow-md">
    <h1 class="text-xl font-bold text-gray-800">标题</h1>
    <p class="mt-2 text-gray-600">内容描述</p>
    <button class="px-4 py-2 mt-4 text-white bg-blue-500 rounded hover:bg-blue-600">
      按钮
    </button>
  </div>
</template>
```

**响应式设计**：

```vue
<template>
  <div class="text-sm md:text-base lg:text-lg">
    响应式文本
  </div>
  <div class="w-full md:w-1/2 lg:w-1/3">
    响应式宽度
  </div>
</template>
```

### 5.2 设计令牌

项目定义了设计令牌（Design Tokens）用于保持视觉一致性。设计令牌在 `src/styles/tailwind.css` 或 `tailwind.config.js` 中配置。

```javascript
// tailwind.config.js
module.exports = {
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eff6ff',
          100: '#dbeafe',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8'
        },
        gray: {
          50: '#f9fafb',
          100: '#f3f4f6',
          500: '#6b7280',
          800: '#1f2937',
          900: '#111827'
        }
      },
      spacing: {
        18: '4.5rem',
        22: '5.5rem'
      }
    }
  }
}
```

### 5.3 自定义样式

对于 Tailwind 工具类无法覆盖的样式，可以在组件的 `<style>` 标签中编写自定义 CSS。

```vue
<template>
  <div class="custom-component">
    内容
  </div>
</template>

<style scoped>
.custom-component {
  /* 自定义样式 */
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 16px;
}

.custom-component::before {
  /* 伪元素 */
  content: '';
  position: absolute;
  top: 0;
  left: 0;
}
</style>
```

## 六、错误处理规范

### 6.1 错误处理原则

所有 API 调用必须包含错误处理逻辑，禁止留空 catch 块。错误处理遵循以下原则：

- 记录错误日志便于排查
- 向用户展示友好的错误提示
- 引导用户进行下一步操作

```javascript
// 错误处理示例
try {
  const res = await fetchUserData(userId)
  userData.value = res.data
} catch (error) {
  console.error('获取用户数据失败:', error)
  showToast('加载失败，请重试')
}
```

### 6.2 统一错误处理

通过请求拦截器统一处理认证错误和系统错误。

```javascript
// utils/request.js 统一错误处理
request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    const { status, data } = error.response

    switch (status) {
      case 401:
        // Token 过期或无效
        handleUnauthorized()
        break
      case 403:
        // 无权限
        showToast('您没有权限进行此操作')
        break
      case 404:
        // 资源不存在
        showToast('请求的资源不存在')
        break
      case 500:
        // 服务器错误
        showToast('服务器错误，请稍后重试')
        break
      default:
        showToast(data?.message || '请求失败')
    }

    return Promise.reject(error)
  }
)
```

## 七、测试规范

### 7.1 测试文件组织

测试文件放置在组件同目录下的 `__tests__/` 目录中，或以 `.spec.js` 或 `.test.js` 为后缀。

```
src/
├── components/
│   └── Button/
│       ├── Button.vue
│       └── __tests__/Button.spec.js
└── views/
    └── Home/
        ├── index.vue
        └── __tests__/index.spec.js
```

### 7.2 组件测试示例

```javascript
// __tests__/Button.spec.js
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Button from '../Button.vue'

describe('Button', () => {
  it('renders properly', () => {
    const wrapper = mount(Button, {
      props: {
        label: 'Click me'
      }
    })
    expect(wrapper.text()).toContain('Click me')
  })

  it('emits click event', async () => {
    const wrapper = mount(Button)
    await wrapper.trigger('click')
    expect(wrapper.emitted()).toHaveProperty('click')
  })

  it('applies correct variant classes', () => {
    const wrapper = mount(Button, {
      props: {
        variant: 'primary'
      }
    })
    expect(wrapper.classes('bg-blue-500')).toBe(true)
  })
})
```

### 7.3 运行测试

```bash
# 运行所有测试
npm run test

# 运行指定测试文件
npm run test src/components/Button/__tests__/Button.spec.js

# 运行测试并生成覆盖率报告
npm run test -- --coverage
```

---

**文档版本**：1.0

**最后更新**：2026年2月

**维护团队**：校园互助平台开发团队
