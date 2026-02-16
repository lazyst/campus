<template>
  <div class="forum-page">
    <NavBar title="论坛" :left-arrow="true" @click-left="onClickLeft" />
    <ResponsiveContainer>
      <ForumList class="forum-page__list" />
    </ResponsiveContainer>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import NavBar from '@/components/navigation/NavBar.vue'
import ForumList from './components/ForumList.vue'
import ResponsiveContainer from '@/components/layout/ResponsiveContainer.vue'

const router = useRouter()

function onClickLeft() {
  router.back()
}
</script>

<style scoped>
.forum-page {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  height: 100%;
  background: var(--bg-page);
}

.forum-page__list {
  flex: 1;
  padding-bottom: calc(var(--tabbar-height) + var(--page-safe-bottom, 16px));
  overflow-y: auto;
}

@media (min-width: 1024px) {
  .forum-page__list {
    padding-bottom: var(--space-6);
  }
}
</style>
