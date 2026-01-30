<template>
  <div class="register-page">
    <!-- Header with Back Button -->
    <NavBar title="注册" :left-arrow="true" @click-left="handleBack" />
    
    <!-- Content -->
    <div class="register-content">
      <!-- Header -->
      <div class="register-header">
        <h1 class="register-title">校园互助</h1>
        <p class="register-subtitle">注册账号</p>
      </div>
      
      <!-- Register Form -->
      <form @submit.prevent="handleSubmit" class="register-form">
        <div class="form-fields">
          <div class="form-field">
            <label class="form-label">手机号</label>
            <input 
              v-model="form.phone"
              type="tel"
              class="input"
              placeholder="请输入手机号"
            />
          </div>
          
          <div class="form-field">
            <label class="form-label">昵称</label>
            <input 
              v-model="form.nickname"
              type="text"
              class="input"
              placeholder="请输入昵称(2-20位)"
            />
          </div>
          
          <div class="form-field">
            <label class="form-label">密码</label>
            <input 
              v-model="form.password"
              type="password"
              class="input"
              placeholder="请输入密码(6-20位)"
            />
          </div>
          
          <div class="form-field">
            <label class="form-label">确认密码</label>
            <input 
              v-model="form.confirmPassword"
              type="password"
              class="input"
              placeholder="请再次输入密码"
            />
          </div>
        </div>
        
        <button 
          type="submit"
          class="btn btn-primary register-submit"
          :disabled="!isFormValid"
        >
          注册
        </button>
      </form>
      
      <!-- Footer -->
      <div class="register-footer">
        <span class="register-login-text">已有账号？</span>
        <span class="register-login-link" @click="goToLogin">立即登录</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/navigation/NavBar.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const form = reactive({
  phone: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

// Validation
const isPhoneValid = computed(() => {
  return /^1[3-9]\d{9}$/.test(form.phone)
})

const isNicknameValid = computed(() => {
  return form.nickname.length >= 2 && form.nickname.length <= 20
})

const isPasswordValid = computed(() => {
  return form.password.length >= 6 && form.password.length <= 20
})

const isConfirmPasswordValid = computed(() => {
  return form.confirmPassword === form.password && form.confirmPassword.length > 0
})

const isFormValid = computed(() => {
  return form.phone.length > 0 && 
         form.nickname.length > 0 && 
         form.password.length > 0 && 
         form.confirmPassword.length > 0 &&
         isPhoneValid.value &&
         isNicknameValid.value &&
         isPasswordValid.value &&
         isConfirmPasswordValid.value
})

function handleBack() {
  router.back();
}

function goToLogin() {
  router.replace('/login');
}

async function handleSubmit() {
  if (!isFormValid.value) {
    return
  }

  loading.value = true
  try {
    await userStore.register({
      phone: form.phone,
      password: form.password,
      nickname: form.nickname
    })
    router.replace('/login')
  } catch (error) {
    console.error('注册失败', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  display: flex;
  flex-direction: column;
}

.register-content {
  flex: 1;
  padding: calc(var(--nav-height) + var(--space-6)) var(--space-4) var(--space-4);
  display: flex;
  flex-direction: column;
}

.register-header {
  text-align: center;
  margin-bottom: var(--space-10);
}

.register-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary-700);
  margin: 0 0 var(--space-2) 0;
}

.register-subtitle {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: 0;
}

.register-form {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.form-fields {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.form-field {
  display: flex;
  flex-direction: column;
}

.form-label {
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.register-submit {
  width: 100%;
  height: 48px;
  font-size: var(--text-base);
  font-weight: var(--font-weight-semibold);
  border-radius: var(--radius-lg);
  margin-top: var(--space-6);
}

.register-footer {
  text-align: center;
  margin-top: var(--space-8);
  padding-bottom: var(--space-4);
}

.register-login-text {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.register-login-link {
  color: var(--color-primary-700);
  font-weight: var(--font-weight-medium);
  text-decoration: none;
  margin-left: var(--space-1);
}

.register-login-link:active {
  opacity: 0.7;
}
</style>
