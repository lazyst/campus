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

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/modules'
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

function goToRegister(event) {
  event.preventDefault()
  router.replace('/register')
}

async function handleSubmit() {
  if (!isFormValid.value) {
    return
  }

  loading.value = true
  try {
    // 使用新的API层登录
    // 新API会自动：显示Toast、保存Token、显示Loading
    await login(form)

    // 登录成功后，更新userStore状态
    // 注意：userStore可能需要调整以适配新的API
    router.replace('/')
  } catch (error) {
    // 错误已被新API层自动处理并显示Toast
    console.error('登录失败', error)
  } finally {
    loading.value = false
  }
}
</script>
