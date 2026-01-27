<template>
  <div class="forum-page">
    <!-- Navigation Bar -->
    <NavBar title="论坛" />
    
    <!-- Category Tabs -->
    <div class="forum-tabs">
      <button
        class="forum-tab"
        :class="{ 'forum-tab--active': activeBoard === 'all' }"
        @click="activeBoard = 'all'"
      >
        全部
      </button>
      <button
        v-for="board in boards" 
        :key="board.id"
        class="forum-tab"
        :class="{ 'forum-tab--active': activeBoard === board.id }"
        @click="activeBoard = board.id"
      >
        {{ board.name }}
      </button>
    </div>
    
    <!-- Forum List -->
    <ForumList :board-id="currentBoardId" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getBoards } from '@/api/modules'
import ForumList from '@/views/forum/components/ForumList.vue'
import NavBar from '@/components/navigation/NavBar.vue'

const activeBoard = ref('all')
const boards = ref([])

const currentBoardId = computed(() => {
  return activeBoard.value === 'all' ? undefined : activeBoard.value
})

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
  background-color: var(--bg-page);
}

.forum-tabs {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-3) var(--page-padding);
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
  overflow-x: auto;
  white-space: nowrap;
  -webkit-overflow-scrolling: touch;
}

.forum-tabs::-webkit-scrollbar {
  display: none;
}

.forum-tab {
  flex-shrink: 0;
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  background-color: var(--bg-tertiary);
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.forum-tab:active {
  background-color: var(--color-primary-100);
}

.forum-tab--active {
  color: var(--text-inverse);
  background-color: var(--color-primary-700);
}

.forum-tab--active:active {
  background-color: var(--color-primary-800);
}
</style>
