# Vue3 + Vant UI 校园社交平台开发最佳实践研究报告

## 1. 项目初始化和配置

### 安装Vant 4（Vue 3版本）

```bash
# 使用npm
npm install vant@latest

# 使用yarn
yarn add vant@latest
```

### 按需引入配置（推荐）

```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import styleImport from 'vite-plugin-style-import'

export default defineConfig({
  plugins: [
    vue(),
    styleImport({
      libs: [
        {
          libraryName: 'vant',
          esModule: true,
          resolveStyle: (name) => `vant/es/${name}/style/index.js`,
        },
      ],
    }),
  ],
})
```

### 全局引入（不推荐用于大型项目）

```typescript
// main.ts
import { createApp } from 'vue'
import Vant from 'vant'
import 'vant/lib/index.css'
import App from './App.vue'

const app = createApp(App)
app.use(Vant)
app.mount('#app')
```

### 项目结构

```
src/
├── api/                    # API 接口
│   ├── request.ts         # Axios 请求封装
│   ├── auth.ts            # 认证相关API
│   ├── user.ts            # 用户相关API
│   ├── forum.ts           # 论坛相关API
│   └── trade.ts           # 交易相关API
├── components/            # 公共组件
│   ├── ImageUploader.vue  # 图片上传组件
│   ├── SocialFeed.vue     # 社交动态Feed组件
│   ├── DynamicForm.vue    # 动态表单组件
│   └── UserCard.vue       # 用户卡片组件
├── views/                 # 页面
│   ├── login/            # 登录注册
│   │   ├── Login.vue
│   │   └── Register.vue
│   ├── home/             # 首页
│   ├── forum/            # 论坛
│   │   ├── ForumHome.vue
│   │   ├── PostList.vue
│   │   └── PostDetail.vue
│   ├── trade/            # 闲置交易
│   │   ├── TradeHome.vue
│   │   └── ItemDetail.vue
│   ├── profile/          # 个人中心
│   │   ├── Profile.vue
│   │   └── EditProfile.vue
│   └── messages/         # 消息
│       ├── MessageList.vue
│       └── ChatRoom.vue
├── router/                # 路由配置
├── stores/                # 状态管理 (Pinia)
│   ├── user.ts           # 用户状态
│   ├── forum.ts          # 论坛状态
│   └── message.ts        # 消息状态
├── utils/                 # 工具函数
│   ├── validators.ts     # 表单验证
│   ├── format.ts         # 格式化工具
│   └── constants.ts      # 常量定义
├── assets/                # 静态资源
└── styles/                # 样式文件
    ├── variables.scss    # 变量定义
    └── global.scss       # 全局样式
```

## 2. 用户认证流程（登录/注册）

### 登录页面组件

```vue
<!-- Login.vue -->
<template>
  <div class="login-container">
    <van-form @submit="handleLogin">
      <van-cell-group inset>
        <van-field
          v-model="form.username"
          name="用户名"
          label="用户名"
          placeholder="请输入用户名"
          :rules="[{ required: true, message: '请填写用户名' }]"
        />
        <van-field
          v-model="form.password"
          type="password"
          name="密码"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请填写密码' }]"
        />
      </van-cell-group>
      
      <div style="margin: 16px;">
        <van-button
          round
          block
          type="primary"
          native-type="submit"
          :loading="loading"
        >
          登录
        </van-button>
      </div>
      
      <div class="register-link">
        <van-button
          plain
          hairline
          type="primary"
          size="small"
          @click="goToRegister"
        >
          还没有账号？立即注册
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Toast } from 'vant'
import { loginApi } from '@/api/auth'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  loading.value = true
  try {
    const res = await loginApi(form)
    if (res.code === 200) {
      localStorage.setItem('token', res.data.token)
      Toast.success('登录成功')
      router.push('/home')
    } else {
      Toast.fail(res.message || '登录失败')
    }
  } catch (error) {
    Toast.fail('登录失败，请重试')
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-link {
  text-align: center;
  margin-top: 20px;
}
</style>
```

### 注册页面组件

```vue
<!-- Register.vue -->
<template>
  <div class="register-container">
    <van-form @submit="handleRegister" :show-error-message="false">
      <van-cell-group inset>
        <van-field
          v-model="form.username"
          name="用户名"
          label="用户名"
          placeholder="4-20位字母数字"
          :rules="usernameRules"
        />
        <van-field
          v-model="form.phone"
          name="手机号"
          label="手机号"
          placeholder="请输入手机号"
          :rules="phoneRules"
        />
        <van-field
          v-model="form.password"
          type="password"
          name="密码"
          label="密码"
          placeholder="6-20位字母数字"
          :rules="passwordRules"
        />
        <van-field
          v-model="form.confirmPassword"
          type="password"
          name="确认密码"
          label="确认密码"
          placeholder="请再次输入密码"
          :rules="confirmPasswordRules"
        />
        <van-field
          v-model="form.verificationCode"
          name="验证码"
          label="验证码"
          placeholder="请输入验证码"
          :rules="[{ required: true, message: '请填写验证码' }]"
        >
          <template #button>
            <van-button
              size="small"
              type="primary"
              :disabled="countdown > 0"
              @click="sendVerificationCode"
            >
              {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
            </van-button>
          </template>
        </van-field>
      </van-cell-group>
      
      <div style="margin: 16px;">
        <van-button
          round
          block
          type="primary"
          native-type="submit"
          :loading="loading"
        >
          注册
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { Toast } from 'vant'
import { sendVerificationCodeApi, registerApi } from '@/api/auth'

const loading = ref(false)
const countdown = ref(0)
const form = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
  verificationCode: ''
})

// 表单验证规则
const usernameRules = [
  { required: true, message: '请填写用户名' },
  { pattern: /^[a-zA-Z0-9]{4,20}$/, message: '用户名必须是4-20位字母数字' }
]

const phoneRules = [
  { required: true, message: '请填写手机号' },
  { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号' }
]

const passwordRules = [
  { required: true, message: '请填写密码' },
  { pattern: /^[a-zA-Z0-9]{6,20}$/, message: '密码必须是6-20位字母数字' }
]

const confirmPasswordRules = computed(() => [
  { required: true, message: '请确认密码' },
  {
    validator: (val: string) => val === form.password,
    message: '两次密码输入不一致'
  }
])

const sendVerificationCode = async () => {
  if (!form.phone) {
    Toast.fail('请先输入手机号')
    return
  }
  
  try {
    await sendVerificationCodeApi({ phone: form.phone })
    Toast.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    Toast.fail('发送失败，请重试')
  }
}

const handleRegister = async () => {
  loading.value = true
  try {
    const res = await registerApi(form)
    if (res.code === 200) {
      Toast.success('注册成功')
      // 跳转到登录页
    } else {
      Toast.fail(res.message || '注册失败')
    }
  } catch (error) {
    Toast.fail('注册失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>
```

## 3. 表单处理和验证

### 统一的表单验证模式

```typescript
// utils/validators.ts
import type { Rule } from 'vant/es/form'

// 自定义验证器
export const phoneValidator: Rule['validator'] = (val) => /^1[3-9]\d{9}$/.test(val)

export const emailValidator: Rule['validator'] = (val) => 
  /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val)

// 常用验证规则组合
export const commonRules = {
  required: { required: true, message: '此项为必填项' },
  phone: { validator: phoneValidator, message: '请输入有效的手机号' },
  email: { validator: emailValidator, message: '请输入有效的邮箱地址' },
  minLength: (min: number): Rule => ({
    min, message: `请至少输入${min}个字符`
  }),
  maxLength: (max: number): Rule => ({
    max, message: `请最多输入${max}个字符`
  })
}
```

### 动态表单组件

```vue
<!-- DynamicForm.vue -->
<template>
  <van-form @submit="handleSubmit">
    <van-cell-group inset>
      <template v-for="field in formFields" :key="field.name">
        <van-field
          v-if="field.type === 'text' || field.type === 'textarea'"
          v-model="formData[field.name]"
          :name="field.label"
          :label="field.label"
          :placeholder="field.placeholder"
          :type="field.type"
          :rules="field.rules"
          :required="field.required"
          :disabled="field.disabled"
          :readonly="field.readonly"
          autosize
        />
        
        <van-field
          v-else-if="field.type === 'number'"
          v-model="formData[field.name]"
          type="number"
          :name="field.label"
          :label="field.label"
          :placeholder="field.placeholder"
          :rules="field.rules"
        />
        
        <van-field
          v-else-if="field.type === 'password'"
          v-model="formData[field.name]"
          type="password"
          :name="field.label"
          :label="field.label"
          :placeholder="field.placeholder"
          :rules="field.rules"
        />
        
        <van-field
          v-else-if="field.type === 'select'"
          v-model="formData[field.name]"
          :name="field.label"
          :label="field.label"
          :placeholder="`请选择${field.label}`"
          :rules="field.rules"
          readonly
          clickable
          @click="showSelectPopup[field.name] = true"
        />
      </template>
    </van-cell-group>
    
    <!-- 选择器弹窗 -->
    <van-popup
      v-model:show="showSelectPopup[field.name]"
      position="bottom"
      v-for="field in formFields.filter(f => f.type === 'select')"
      :key="field.name"
    >
      <van-picker
        :columns="field.options || []"
        @confirm="(val) => handleSelectConfirm(val, field.name)"
        @cancel="showSelectPopup[field.name] = false"
      />
    </van-popup>
    
    <div style="margin: 16px;">
      <van-button
        round
        block
        type="primary"
        native-type="submit"
        :loading="loading"
      >
        {{ submitText }}
      </van-button>
    </div>
  </van-form>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { Popup, Picker } from 'vant'

interface FormField {
  name: string
  label: string
  type: 'text' | 'textarea' | 'number' | 'password' | 'select'
  placeholder?: string
  rules?: any[]
  required?: boolean
  disabled?: boolean
  readonly?: boolean
  options?: { text: string; value: string | number }[]
}

const props = defineProps<{
  fields: FormField[]
  initialData?: Record<string, any>
  submitText?: string
}>()

const emit = defineEmits(['submit'])

const loading = ref(false)
const formData = reactive<Record<string, any>>({})
const showSelectPopup = reactive<Record<string, boolean>>({})

// 初始化表单数据
props.fields.forEach(field => {
  formData[field.name] = props.initialData?.[field.name] || ''
})

// 处理选择器确认
const handleSelectConfirm = (value: any, fieldName: string) => {
  formData[fieldName] = value
  showSelectPopup[fieldName] = false
}

// 表单提交
const handleSubmit = async () => {
  loading.value = true
  try {
    emit('submit', formData)
  } finally {
    loading.value = false
  }
}
</script>
```

## 4. 列表视图和卡片组件

### 社交动态Feed组件

```vue
<!-- SocialFeed.vue -->
<template>
  <div class="feed-container">
    <van-pull-refresh
      v-model="refreshing"
      @refresh="onRefresh"
    >
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div
          v-for="post in postList"
          :key="post.id"
          class="post-card"
        >
          <!-- 用户信息头部 -->
          <div class="post-header">
            <van-image
              round
              :src="post.user.avatar"
              class="user-avatar"
              @click="goToUserProfile(post.user.id)"
            />
            <div class="user-info">
              <div class="user-name">{{ post.user.name }}</div>
              <div class="post-time">{{ formatTime(post.createdAt) }}</div>
            </div>
          </div>
          
          <!-- 帖子内容 -->
          <div class="post-content">
            <p>{{ post.content }}</p>
            
            <!-- 图片网格 -->
            <div
              v-if="post.images && post.images.length > 0"
              :class="['image-grid', `grid-${Math.min(post.images.length, 4)}`]"
            >
              <van-image
                v-for="(img, index) in post.images.slice(0, 4)"
                :key="index"
                :src="img"
                fit="cover"
                @click="previewImage(post.images, index)"
              />
            </div>
          </div>
          
          <!-- 互动按钮 -->
          <div class="post-actions">
            <van-button
              plain
              hairline
              :type="post.isLiked ? 'danger' : 'default'"
              :icon="post.isLiked ? 'like' : 'like-o'"
              @click="handleLike(post)"
            >
              {{ post.likeCount }}
            </van-button>
            <van-button
              plain
              hairline
              icon="comment-o"
              @click="goToComments(post.id)"
            >
              {{ post.commentCount }}
            </van-button>
            <van-button
              plain
              hairline
              icon="share-o"
              @click="handleShare(post)"
            >
              分享
            </van-button>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ImagePreview, Toast } from 'vant'
import { getFeedApi, likePostApi } from '@/api/posts'

interface User {
  id: number
  name: string
  avatar: string
}

interface Post {
  id: number
  user: User
  content: string
  images: string[]
  createdAt: string
  likeCount: number
  commentCount: number
  isLiked: boolean
}

const postList = ref<Post[]>([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = ref(10)

// 加载更多
const onLoad = async () => {
  if (finished.value) return
  
  try {
    const res = await getFeedApi({ page: page.value, pageSize: pageSize.value })
    if (res.code === 200) {
      postList.value = [...postList.value, ...res.data.list]
      if (res.data.list.length < pageSize.value) {
        finished.value = true
      } else {
        page.value++
      }
    }
  } catch (error) {
    Toast.fail('加载失败')
  } finally {
    loading.value = false
  }
}

// 下拉刷新
const onRefresh = async () => {
  page.value = 1
  finished.value = false
  postList.value = []
  
  try {
    const res = await getFeedApi({ page: 1, pageSize: pageSize.value })
    if (res.code === 200) {
      postList.value = res.data.list
    }
  } catch (error) {
    Toast.fail('刷新失败')
  } finally {
    refreshing.value = false
  }
}

// 点赞处理
const handleLike = async (post: Post) => {
  try {
    const res = await likePostApi({ postId: post.id })
    if (res.code === 200) {
      post.isLiked = !post.isLiked
      post.likeCount += post.isLiked ? 1 : -1
    }
  } catch (error) {
    Toast.fail('操作失败')
  }
}

// 图片预览
const previewImage = (images: string[], index: number) => {
  ImagePreview({
    images,
    startPosition: index,
  })
}

// 时间格式化
 (time: string) => {
  const date = newconst formatTime = Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return `${date.getMonth() + 1}月${date.getDate()}日`
}
</script>

<style scoped>
.feed-container {
  background: #f5f5f5;
  min-height: 100vh;
}

.post-card {
  background: white;
  margin-bottom: 10px;
  padding: 15px;
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.user-avatar {
  width: 44px;
  height: 44px;
  margin-right: 12px;
}

.user-info {
  flex: 1;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-content {
  margin-bottom: 12px;
}

.post-content p {
  margin: 0 0 12px;
  line-height: 1.5;
}

.image-grid {
  display: grid;
  gap: 8px;
}

.grid-1 {
  grid-template-columns: 1fr;
}

.grid-2, .grid-3, .grid-4 {
  grid-template-columns: repeat(2, 1fr);
}

.image-grid van-image {
  aspect-ratio: 1;
  border-radius: 8px;
}

.post-actions {
  display: flex;
  justify-content: space-around;
  padding-top: 12px;
  border-top: 1px solid #eee;
}
</style>
```

## 5. 图片上传和显示模式

### 图片上传组件

```vue
<!-- ImageUploader.vue -->
<template>
  <div class="image-uploader">
    <div class="upload-grid">
      <div
        v-for="(image, index) in fileList"
        :key="index"
        class="upload-item"
      >
        <van-image
          :src="image.content"
          fit="cover"
          @click="previewImage(index)"
        />
        <van-icon
          name="clear"
          class="delete-btn"
          @click="removeImage(index)"
        />
      </div>
      
      <div
        v-if="fileList.length < maxCount"
        class="upload-btn"
        @click="handleChooseImage"
      >
        <van-icon name="plus" size="32" color="#ccc" />
        <span class="upload-text">{{ fileList.length }}/{{ maxCount }}</span>
      </div>
    </div>
    
    <!-- 图片预览 -->
    <van-image-preview
      v-model:show="previewShow"
      :images="previewImages"
      :start-position="previewIndex"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ImagePreview, Toast } from 'vant'

interface FileItem {
  content: string  // Base64或URL
  file?: File
}

const props = defineProps<{
  maxCount?: number
  maxSize?: number  // 单位MB
}>()

const emit = defineEmits(['update:modelValue'])

const fileList = ref<FileItem[]>([])
const previewShow = ref(false)
const previewIndex = ref(0)

const maxCount = props.maxCount || 9
const maxSize = props.maxSize || 5

const previewImages = computed(() => 
  fileList.value.map(item => item.content)
)

const handleChooseImage = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.multiple = maxCount - fileList.value.length > 1
  
  input.onchange = (e: any) => {
    const files = Array.from(e.target.files).slice(0, maxCount - fileList.value.length)
    
    files.forEach(file => {
      if (file.size > maxSize * 1024 * 1024) {
        Toast.fail(`图片大小不能超过${maxSize}MB`)
        return
      }
      
      const reader = new FileReader()
      reader.onload = (event) => {
        fileList.value.push({
          content: event.target?.result as string,
          file: file
        })
        emit('update:modelValue', fileList.value)
      }
      reader.readAsDataURL(file)
    })
  }
  
  input.click()
}

const removeImage = (index: number) => {
  fileList.value.splice(index, 1)
  emit('update:modelValue', fileList.value)
}

const previewImage = (index: number) => {
  previewIndex.value = index
  previewShow.value = true
}
</script>

<style scoped>
.image-uploader {
  padding: 10px;
}

.upload-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.upload-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
}

.upload-item van-image {
  width: 100%;
  height: 100%;
}

.delete-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  font-size: 18px;
  color: #f00;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  padding: 2px;
}

.upload-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  aspect-ratio: 1;
  border: 2px dashed #ddd;
  border-radius: 8px;
  background: #fafafa;
}

.upload-text {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
```

## 6. REST API 集成

### API 封装层

```typescript
// api/request.ts
import axios, { AxiosRequestConfig, AxiosResponse } from 'axios'
import { Toast } from 'vant'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
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
  (response: AxiosResponse) => {
    const { code, message, data } = response.data
    
    if (code === 200) {
      return { code, message, data }
    } else {
      Toast.fail(message || '请求失败')
      return Promise.reject(new Error(message))
    }
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          Toast.fail('没有权限')
          break
        case 404:
          Toast.fail('请求的资源不存在')
          break
        case 500:
          Toast.fail('服务器错误')
          break
        default:
          Toast.fail(data?.message || '请求失败')
      }
    } else if (error.request) {
      Toast.fail('网络连接失败')
    } else {
      Toast.fail('请求失败')
    }
    
    return Promise.reject(error)
  }
)

export default request
```

### API 服务模块

```typescript
// api/auth.ts
import request from './request'

export const loginApi = (data: { username: string; password: string }) => {
  return request.post('/auth/login', data)
}

export const registerApi = (data: any) => {
  return request.post('/auth/register', data)
}

export const sendVerificationCodeApi = (data: { phone: string }) => {
  return request.post('/auth/send-verification-code', data)
}

export const logoutApi = () => {
  return request.post('/auth/logout')
}

// posts.ts
export const getFeedApi = (params: { page: number; pageSize: number }) => {
  return request.get('/posts/feed', { params })
}

export const likePostApi = (data: { postId: number }) => {
  return request.post('/posts/like', data)
}

export const createPostApi = (data: FormData) => {
  return request.post('/posts', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
```

## 7. 移动优先社交功能最佳实践

### 关键最佳实践

1. **移动端适配**
```css
/* 使用 viewport 适配 */
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">

/* CSS 变量定义 */
:root {
  --primary-color: #1989fa;
  --success-color: #07c160;
  --danger-color: #ee0a24;
  --text-primary: #333333;
  --text-secondary: #999999;
  --bg-color: #f5f5f5;
}
```

2. **性能优化**
```typescript
// 路由懒加载
const routes = [
  {
    path: '/home',
    component: () => import('@/views/home/index.vue')
  }
]

// 图片懒加载
<van-image
  :src="imageUrl"
  lazy-load
/>
```

3. **状态管理（Pinia）**
```typescript
// stores/user.ts
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    token: localStorage.getItem('token') || ''
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    userId: (state) => state.userInfo?.id
  },
  
  actions: {
    setToken(token: string) {
      this.token = token
      localStorage.setItem('token', token)
    },
    
    async fetchUserInfo() {
      const res = await request.get('/user/profile')
      this.userInfo = res.data
    },
    
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
    }
  }
})
```

4. **错误处理**
```typescript
// 统一错误处理
const handleError = (error: any) => {
  const message = error.response?.data?.message || error.message || '未知错误'
  Toast.fail(message)
  console.error('Error:', error)
}

// 在API调用中使用
try {
  await someApiCall()
} catch (error) {
  handleError(error)
}
```

## 8. 常用组件使用示例

### Tab标签页

```vue
<template>
  <van-tabs v-model:active="active" @change="onTabChange">
    <van-tab title="交流">交流内容</van-tab>
    <van-tab title="学习">学习内容</van-tab>
    <van-tab title="兴趣">兴趣内容</van-tab>
  </van-tabs>
</template>

<script setup>
import { ref } from 'vue'

const active = ref(0)

const onTabChange = (index) => {
  console.log('切换到标签', index)
}
</script>
```

### 列表组件

```vue
<template>
  <van-list
    v-model:loading="loading"
    :finished="finished"
    finished-text="没有更多了"
    @load="onLoad"
  >
    <div v-for="item in list" :key="item.id">
      {{ item.title }}
    </div>
  </van-list>
</template>

<script setup>
import { ref } from 'vue'

const list = ref([])
const loading = ref(false)
const finished = ref(false)

const onLoad = async () => {
  // 加载数据
}
</script>
```

### 弹窗组件

```vue
<template>
  <van-popup
    v-model:show="show"
    position="bottom"
    round
  >
    <van-picker
      title="选择板块"
      :columns="columns"
      @confirm="onConfirm"
      @cancel="show = false"
    />
  </van-popup>
</template>

<script setup>
import { ref } from 'vue'

const show = ref(false)
const columns = [
  { text: '交流', value: '1' },
  { text: '学习', value: '2' },
  { text: '兴趣', value: '3' }
]

const onConfirm = (value) => {
  console.log('选择', value)
  show.value = false
}
</script>
```

## 总结

本研究报告涵盖了Vue3 + Vant UI构建校园社交平台的最佳实践：

1. **项目初始化**：Vite + Vue3 + Vant UI配置
2. **用户认证**：完整的登录注册流程和表单验证
3. **表单处理**：动态表单和统一验证规则
4. **列表视图**：社交动态Feed和无限滚动
5. **图片处理**：图片上传和预览组件
6. **API集成**：Axios请求封装和错误处理
7. **移动优化**：性能优化和状态管理

这些实践基于Vant UI官方文档和Vue 3最佳实践，提供了生产级别的移动端开发指导。
