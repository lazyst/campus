<template>
  <div class="login-page">
    <!-- Header with Back Button -->
    <NavBar title="登录" :left-arrow="true" @click-left="handleBack" />

    <!-- Content -->
    <div class="login-content">
      <!-- Header -->
      <div class="login-header">
        <h1 class="login-title">校园互助</h1>
        <p class="login-subtitle">让校园生活更美好</p>
      </div>

      <!-- Form -->
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

      <!-- Register Link -->
      <div class="login-footer">
        <span class="login-register-text">还没有账号？</span>
        <span class="login-register-link" @click="goToRegister">立即注册</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';
import NavBar from '@/components/navigation/NavBar.vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const form = reactive({
  phone: '',
  password: '',
});

const isFormValid = computed(() => {
  const phoneValid = /^1[3-9]\d{9}$/.test(form.phone);
  return form.phone.length > 0 && form.password.length > 0 && phoneValid;
});

function handleBack() {
  router.back();
}

function goToRegister() {
  router.replace('/register');
}

async function handleSubmit() {
  if (!isFormValid.value) return;

  try {
    await userStore.login(form);
    // 登录成功后跳转到 redirect 参数指定的页面，或默认首页
    const redirect = route.query.redirect as string;
    // 校验 redirect 参数，防止开放重定向
    const safeRedirect = (redirect && redirect.startsWith('/') && !redirect.startsWith('//'))
      ? redirect
      : '/';
    router.replace(safeRedirect);
  } catch (error) {
    // 错误已在 request.js 中通过 showToast 处理
    console.error('登录失败', error);
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  display: flex;
  flex-direction: column;
}

/* PC端样式增强 */
@media (min-width: 1024px) {
  .login-page {
    background: linear-gradient(135deg, #EFF6FF 0%, #F8FAFC 50%, #F0FDF4 100%);
    position: relative;
    overflow: hidden;
  }

  .login-page::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -20%;
    width: 60%;
    height: 200%;
    background: radial-gradient(ellipse, rgba(37, 99, 235, 0.08) 0%, transparent 70%);
    pointer-events: none;
  }

  .login-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: var(--space-8);
    max-width: 440px;
    margin: 0 auto;
    width: 100%;
  }

  .login-header {
    text-align: center;
    margin-bottom: var(--space-10);
  }

  .login-title {
    font-size: var(--text-3xl);
    font-weight: var(--font-weight-bold);
    background: linear-gradient(135deg, var(--color-primary-600) 0%, var(--color-primary-800) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin-bottom: var(--space-3);
  }

  .login-subtitle {
    font-size: var(--text-lg);
    color: var(--text-secondary);
  }

  .login-form {
    width: 100%;
    background: #FFFFFF;
    padding: var(--space-8);
    border-radius: var(--radius-xl);
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
    border: 1px solid rgba(0, 0, 0, 0.04);
  }

  .form-group {
    margin-bottom: var(--space-5);
  }

  .form-label {
    font-size: var(--text-base);
    margin-bottom: var(--space-2);
  }

  .input {
    height: 52px;
    font-size: var(--text-base);
    border-radius: var(--radius-lg);
    border: 1px solid var(--color-gray-200);
    transition: all var(--transition-fast);
  }

  .input:focus {
    border-color: var(--color-primary-500);
    box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
  }

  .login-submit {
    height: 56px;
    font-size: var(--text-lg);
    margin-top: var(--space-6);
    border-radius: var(--radius-lg);
  }

  .login-footer {
    margin-top: var(--space-8);
  }
}

.login-content {
  flex: 1;
  padding: calc(var(--nav-height) + var(--space-6)) var(--space-4) var(--space-4);
  display: flex;
  flex-direction: column;
}

.login-header {
  text-align: center;
  margin-bottom: var(--space-10);
}

.login-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary-700);
  margin: 0 0 var(--space-2) 0;
}

.login-subtitle {
  font-size: var(--text-base);
  color: var(--text-secondary);
  margin: 0;
}

.login-form {
  flex: 1;
  display: flex;
  flex-direction: column;
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
