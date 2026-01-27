<template>
  <div class="login-page">
    <!-- Logo区域 -->
    <div class="login-header">
      <div class="login-logo">
        <span>校</span>
      </div>
      <h1 class="login-title">校园互助</h1>
      <p class="login-subtitle">让校园生活更美好</p>
    </div>

    <!-- 表单区域 -->
    <form class="login-form" @submit.prevent="handleSubmit">
      <div class="form-group">
        <label class="form-label">手机号</label>
        <input 
          v-model="form.phone"
          type="tel"
          class="input"
          placeholder="请输入手机号"
        />
      </div>

      <div class="form-group">
        <div class="form-label-row">
          <label class="form-label">密码</label>
          <span class="forgot-link">忘记密码？</span>
        </div>
        <input 
          v-model="form.password"
          type="password"
          class="input"
          placeholder="请输入密码"
        />
      </div>

      <button 
        type="submit"
        class="btn btn-primary login-submit"
        :disabled="!isFormValid"
      >
        登录
      </button>
    </form>

    <!-- 注册链接 -->
    <div class="login-footer">
      <span class="login-register-text">还没有账号？</span>
      <router-link to="/register" class="login-register-link">立即注册</router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

const form = reactive({
  phone: '',
  password: '',
});

const isFormValid = computed(() => {
  const phoneValid = /^1[3-9]\d{9}$/.test(form.phone);
  return form.phone.length > 0 && form.password.length > 0 && phoneValid;
});

async function handleSubmit() {
  if (!isFormValid.value) return;

  try {
    await userStore.login(form);
    router.replace('/');
  } catch (error) {
    console.error('登录失败', error);
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-page);
  padding: var(--space-6) var(--space-4);
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 0 0 auto;
  margin-bottom: var(--space-12);
}

.login-logo {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-lg);
  background-color: var(--color-primary-100);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-4);
}

.login-logo span {
  font-size: 36px;
  font-weight: var(--font-weight-bold);
  color: var(--color-primary-700);
}

.login-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
}

.login-subtitle {
  font-size: var(--text-base);
  color: var(--text-secondary);
  margin: 0;
}

.login-form {
  flex: 1 0 auto;
}

.form-group {
  margin-bottom: var(--space-4);
}

.form-label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-2);
}

.form-label {
  display: block;
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.forgot-link {
  font-size: var(--text-sm);
  color: var(--color-primary-700);
  cursor: pointer;
}

.login-submit {
  width: 100%;
  height: 52px;
  font-size: var(--text-base);
  font-weight: var(--font-weight-semibold);
  border-radius: var(--radius-lg);
  margin-top: var(--space-4);
}

.login-footer {
  flex: 0 0 auto;
  text-align: center;
  margin-top: var(--space-8);
  padding-bottom: var(--space-4);
}

.login-register-text {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.login-register-link {
  color: var(--color-primary-700);
  font-weight: var(--font-weight-medium);
  text-decoration: none;
  margin-left: var(--space-1);
}

.login-register-link:active {
  opacity: 0.7;
}
</style>
