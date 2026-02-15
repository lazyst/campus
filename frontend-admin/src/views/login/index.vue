<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo-icon">管</div>
        <h2>校园互助平台</h2>
        <p class="subtitle">管理后台</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username" class="input-item">
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item prop="password" class="input-item">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-form-item class="btn-item">
          <button type="button" class="login-btn" :class="{ loading }" @click="handleLogin" :disabled="loading">
            <span class="btn-text">{{ loading ? '登录中...' : '登录' }}</span>
          </button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span class="version">v1.0.0</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { login } from '@/api/admin/auth'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await login(form)
      localStorage.setItem('admin_token', res.data.token)
      localStorage.setItem('admin_info', JSON.stringify(res.data))
      ElMessage.success('登录成功')
      router.push('/')
    } catch (error: any) {
      ElMessage.error(error.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #F5F7FA;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.login-box {
  width: 420px;
  padding: 50px 45px;
  background: #FFFFFF;
  border-radius: 16px;
  border: 1px solid #E5E7EB;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1E3A8A;
  border-radius: 12px;
  color: white;
  font-size: 24px;
  font-weight: 600;
}

.login-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px;
}

.subtitle {
  font-size: 14px;
  color: #6B7280;
  margin: 0;
}

.input-item {
  margin-bottom: 20px;
}

.btn-item {
  margin-top: 28px;
  margin-bottom: 0;
}

:deep(.el-input) {
  width: 100%;
}

:deep(.el-input__wrapper) {
  width: 100%;
  box-shadow: none !important;
  background: #F9FAFB !important;
  border: 1px solid #E5E7EB !important;
  border-radius: 10px !important;
  padding: 4px 15px !important;
  height: auto !important;
  min-height: 48px !important;
}

:deep(.el-input__wrapper:hover) {
  border-color: #D1D5DB !important;
  background: #FFFFFF !important;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #1E3A8A !important;
  background: #FFFFFF !important;
  box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.1) !important;
}

:deep(.el-input__inner) {
  color: #111827 !important;
  font-size: 15px !important;
  height: auto !important;
  line-height: 1.5 !important;
}

:deep(.el-input__inner::placeholder) {
  color: #9CA3AF !important;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
  padding: 0;
}

:deep(.el-form-item__content) {
  margin-left: 0 !important;
  width: 100%;
}

.login-btn {
  width: 100%;
  padding: 14px 32px;
  background: #1E3A8A;
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: #1E40AF;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(30, 58, 138, 0.25);
  }

  &:active {
    transform: translateY(0);
  }

  &.loading {
    pointer-events: none;
    opacity: 0.8;
  }
}

.login-footer {
  text-align: center;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #E5E7EB;
}

.version {
  font-size: 12px;
  color: #9CA3AF;
}

@media (max-width: 480px) {
  .login-box {
    width: calc(100% - 32px);
    padding: 40px 30px;
  }
}
</style>
