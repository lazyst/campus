<template>
  <div class="min-h-screen bg-white px-5 py-10">
    <!-- Header with Back Button -->
    <NavBar title="登录" :left-arrow="true" @click-left="handleBack" />
    
    <!-- Content -->
    <div class="mt-8">
      <!-- Header -->
      <div class="text-center mb-10">
        <h1 class="text-2xl text-primary font-bold mb-2.5">校园互助</h1>
        <p class="text-gray-400">登录账号</p>
      </div>
      
      <!-- Login Form -->
      <form @submit.prevent="handleSubmit">
        <div class="space-y-4">
          <BaseInput 
            v-model="form.phone" 
            label="手机号" 
            placeholder="请输入手机号"
            :error="!isPhoneValid && form.phone.length > 0"
          />
          
          <BaseInput 
            v-model="form.password" 
            type="password" 
            label="密码" 
            placeholder="请输入密码"
          />
        </div>
        
        <div class="mt-8">
          <BaseButton 
            block 
            :loading="loading"
            :disabled="!isFormValid"
          >
            登录
          </BaseButton>
        </div>
      </form>
      
      <!-- Footer -->
      <div class="text-center mt-8 text-gray-400">
        还没有账号？
        <router-link to="/register" @click.native="goToRegister" class="text-primary ml-1">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/navigation/NavBar.vue'
import BaseInput from '@/components/base/Input.vue'
import BaseButton from '@/components/base/Button.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const form = reactive({ 
  phone: '', 
  password: '' 
})

// Phone validation
const isPhoneValid = computed(() => {
  return /^1[3-9]\d{9}$/.test(form.phone)
})

// Form validation
const isFormValid = computed(() => {
  return form.phone.length > 0 && 
         form.password.length > 0 && 
         isPhoneValid.value
})

function handleBack() {
  router.back()
}

function goToRegister() {
  event.preventDefault()
  router.replace('/register')
}

async function handleSubmit() {
  if (!isFormValid.value) {
    alert('请填写正确的手机号和密码')
    return
  }
  
  loading.value = true
  try {
    await userStore.login(form)
    alert('登录成功')
    router.replace('/')
  } catch (error: any) {
    alert(error.message || '登录失败，请检查手机号和密码')
  } finally {
    loading.value = false
  }
}
</script>
