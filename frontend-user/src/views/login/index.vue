<template>
  <div class="login-page">
    <div class="header">
      <van-icon name="arrow-left" class="back-button" @click="goBack" />
      <h1>校园互助</h1>
      <p>登录账号</p>
    </div>

    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.phone"
          name="phone"
          label="手机号"
          placeholder="请输入手机号"
          :rules="[{ required: true, message: '请填写手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }]"
        />
        <van-field
          v-model="form.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请填写密码' }]"
        />
      </van-cell-group>

      <div class="btn-wrap">
        <van-button round block type="primary" native-type="submit" :loading="loading">
          登录
        </van-button>
      </div>
    </van-form>

    <div class="footer">
      <span>还没有账号？</span>
      <router-link to="/register">立即注册</router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { Toast } from 'vant'

const userStore = useUserStore()
const router = useRouter()

const loading = ref(false)
const form = reactive({
  phone: '',
  password: ''
})

async function onSubmit() {
  loading.value = true
  try {
    await userStore.login(form)
    Toast.success('登录成功')
    router.push('/')
  } catch (error: any) {
    Toast.fail(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/')
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  background: #fff;
  padding: 40px 20px;

  .header {
    position: relative;
    text-align: center;
    margin-bottom: 40px;

    .back-button {
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      font-size: 24px;
      color: #1989fa;
      cursor: pointer;
      padding: 8px;
      border-radius: 4px;
      transition: background-color 0.3s;

      &:hover {
        background-color: #f0f0f0;
      }
    }

    h1 {
      font-size: 28px;
      color: #1989fa;
      margin-bottom: 10px;
    }

    p {
      color: #999;
    }
  }

  .btn-wrap {
    margin-top: 30px;
    padding: 0 16px;
  }

  .footer {
    text-align: center;
    margin-top: 30px;
    color: #999;

    a {
      color: #1989fa;
      margin-left: 5px;
    }
  }
}
</style>
