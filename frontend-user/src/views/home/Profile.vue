<template>
  <div class="my-profile">
    <div class="profile-header" @click="goToProfile">
      <div class="avatar">我</div>
      <div class="user-info">
        <div class="nickname">点击登录</div>
        <div class="phone">登录后查看完整信息</div>
      </div>
      <van-icon name="arrow" />
    </div>
    
    <div class="menu-section">
      <div class="menu-item" @click="goTo('/profile/edit')">
        <van-icon name="user-o" />
        <span>编辑资料</span>
      </div>
      <div class="menu-item" @click="goTo('/profile/posts')">
        <van-icon name="comment-o" />
        <span>我的帖子</span>
      </div>
      <div class="menu-item" @click="goTo('/profile/items')">
        <van-icon name="shop-o" />
        <span>我的闲置</span>
      </div>
      <div class="menu-item" @click="goTo('/profile/collections')">
        <van-icon name="star-o" />
        <span>我的收藏</span>
      </div>
      <div class="menu-item" @click="goTo('/profile/messages')">
        <van-icon name="bell-o" />
        <span>消息通知</span>
      </div>
    </div>
    
    <div class="action-section">
      <van-button type="primary" block @click="goToLogin">
        登录/注册
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goToProfile() {
  router.push('/profile')
}

function goTo(path: string) {
  router.push(path)
}

function goToLogin() {
  router.push('/login')
}
</script>

<style scoped>
.my-profile {
  min-height: calc(100vh - 50px);
  background: #f5f5f5;
  padding: 16px;
}
.profile-header {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}
.avatar {
  width: 60px;
  height: 60px;
  background: #1989fa;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}
.user-info {
  flex: 1;
}
.nickname {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 4px;
}
.phone {
  font-size: 14px;
  color: #999;
}
.menu-section {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 16px;
}
.menu-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
}
.menu-item:last-child {
  border-bottom: none;
}
.menu-item .van-icon {
  margin-right: 12px;
  font-size: 20px;
  color: #1989fa;
}
.menu-item span {
  flex: 1;
}
.action-section {
  padding: 0 16px;
}
</style>
