<template>
  <div class="forum-page">
    <van-nav-bar title="论坛" />
    <van-tabs v-model:active="activeBoard" @change="onBoardChange">
      <van-tab name="all" title="全部" />
      <van-tab 
        v-for="board in boards" 
        :key="board.id" 
        :name="board.id" 
        :title="board.name" 
      />
    </van-tabs>
    <ForumList :board-id="currentBoardId" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBoards, type Board } from '@/api/forum'
import ForumList from '@/views/forum/components/ForumList.vue'

const router = useRouter()
const activeBoard = ref('all')
const boards = ref<Board[]>([])

const currentBoardId = computed(() => {
  return activeBoard.value === 'all' ? undefined : activeBoard.value
})

function onBoardChange() {
  // 切换板块时刷新列表
}

onMounted(async () => {
  try {
    boards.value = await getBoards()
  } catch (error) {
    console.error('获取板块列表失败:', error)
  }
})
</script>

<style scoped>
.forum-page {
  min-height: 100vh;
  background: #f5f5f5;
}
</style>
