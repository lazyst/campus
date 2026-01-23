<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="avatar-section">
        <van-uploader :max-count="1" :after-read="handleAvatarUpload">
          <van-image
            round
            width="80px"
            height="80px"
            :src="userInfo?.avatar || defaultAvatar"
          />
        </van-uploader>
      </div>
      <div class="info-section">
        <h2>{{ userInfo?.nickname || '未登录' }}</h2>
        <p>{{ userInfo?.phone || '请先登录' }}</p>
      </div>
    </div>

    <div class="profile-menu">
      <van-cell-group>
        <van-cell title="编辑资料" is-link to="/profile/edit" />
        <van-cell title="我的帖子" is-link to="/profile/posts" />
        <van-cell title="我的闲置" is-link to="/profile/items" />
        <van-cell title="我的收藏" is-link to="/profile/collections" />
        <van-cell title="消息通知" is-link to="/profile/messages" />
      </van-cell-group>
    </div>

    <div class="logout-section">
      <van-button type="primary" block @click="handleLogout">退出登录</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { Toast } from 'vant'

const userStore = useUserStore()
const router = useRouter()
const defaultAvatar = 'https://img.yzcdn.cn/vant/cat.jpeg'

const userInfo = ref(userStore.userInfo)

onMounted(async () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  await userStore.fetchUserInfo()
  userInfo.value = userStore.userInfo
})

function handleAvatarUpload(file: any) {
  Toast.upload('头像上传功能开发中')
}

async function handleLogout() {
  try {
    await userStore.logout()
    Toast.success('已退出登录')
    router.push('/login')
  } catch (error) {
    Toast.fail('退出失败')
  }
}
</script>

<style scoped lang="scss">
.profile-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.profile-header {
  background: #fff;
  padding: 30px 20px;
  display: flex;
  align-items: center;

  .avatar-section {
    margin-right: 20px;
  }

  .info-section {
    h2 {
      font-size: 20px;
      margin-bottom: 5px;
    }

    p {
      color: #999;
      font-size: 14px;
    }
  }
}

.profile-menu {
  margin-top: 20px;
}

.logout-section {
  margin-top: 40px;
  padding: 0 20px;
}
</style>
