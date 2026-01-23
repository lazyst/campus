<template>
  <div class="min-h-screen bg-white px-5 py-10">
    <!-- Header -->
    <div class="text-center mb-10">
      <h1 class="text-2xl text-primary font-bold mb-2.5">校园互助</h1>
      <p class="text-gray-400">注册账号</p>
    </div>
    
    <!-- Register Form -->
    <form @submit.prevent="handleSubmit">
      <div class="space-y-4">
        <BaseInput 
          v-model="form.phone" 
          label="手机号" 
          placeholder="请输入手机号"
          :error="!isPhoneValid && form.phone.length > 0"
        />
        
        <BaseInput 
          v-model="form.nickname" 
          label="昵称" 
          placeholder="请输入昵称(2-20位)"
          :error="!isNicknameValid && form.nickname.length > 0"
        />
        
        <BaseInput 
          v-model="form.password" 
          type="password" 
          label="密码" 
          placeholder="请输入密码(6-20位)"
          :error="!isPasswordValid && form.password.length > 0"
        />
        
        <BaseInput 
          v-model="form.confirmPassword" 
          type="password" 
          label="确认密码" 
          placeholder="请再次输入密码"
          :error="!isConfirmPasswordValid && form.confirmPassword.length > 0"
        />
      </div>
      
      <div class="mt-8">
        <BaseButton 
          block 
          :loading="loading"
          :disabled="!isFormValid"
        >
          注册
        </BaseButton>
      </div>
    </form>
    
    <!-- Footer -->
    <div class="text-center mt-8 text-gray-400">
      已有账号？
      <router-link to="/login" class="text-primary ml-1">立即登录</router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import BaseInput from '@/components/base/Input.vue'
import BaseButton from '@/components/base/Button.vue'

const userStore = useUserStore()
const router = useRouter()

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

async function handleSubmit() {
  if (!isFormValid.value) {
    alert('请填写完整且正确的注册信息')
    return
  }
  
  loading.value = true
  try {
    await userStore.register({
      phone: form.phone,
      password: form.password,
      nickname: form.nickname
    })
    alert('注册成功')
    router.push('/')
  } catch (error: any) {
    alert(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>
