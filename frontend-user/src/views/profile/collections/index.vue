<template>
  <div class="profile-collections-page">
    <NavBar title="我的收藏" :left-arrow="true" @click-left="onClickLeft" />

    <div class="profile-collections__empty">
      <div class="profile-collections__empty-icon">藏</div>
      <div class="profile-collections__empty-text">暂无收藏</div>
      <div class="profile-collections__empty-hint">快去收藏一些内容吧</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import NavBar from '@/components/navigation/NavBar.vue'

const router = useRouter()

function onClickLeft() {
  router.back()
}
</script>

<style scoped>
.profile-collections-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.profile-collections__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-8);
}

.profile-collections__empty-icon {
  width: 80px;
  height: 80px;
  background-color: var(--color-primary-700);
  color: #FFFFFF;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--space-4);
}

.profile-collections__empty-text {
  font-size: var(--text-lg);
  color: var(--text-primary);
  font-weight: var(--font-weight-medium);
  margin-bottom: var(--space-2);
}

.profile-collections__empty-hint {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}
</style>
