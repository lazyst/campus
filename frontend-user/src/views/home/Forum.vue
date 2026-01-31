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
        <div class="tab-underline"></div>
      </button>
      <button
        v-for="board in boards"
        :key="board.id"
        class="forum-tab"
        :class="{ 'forum-tab--active': activeBoard === board.id }"
        @click="activeBoard = board.id"
      >
        {{ board.name }}
        <div class="tab-underline"></div>
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
  gap: var(--space-4);
  padding: var(--space-3) var(--page-padding);
  padding-bottom: 0;
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
  padding: var(--space-3) 0;
  font-size: var(--text-base);
  font-weight: var(--font-weight-medium);
  color: var(--text-secondary);
  background: transparent;
  border: none;
  cursor: pointer;
  transition: color var(--transition-fast);
  position: relative;
  z-index: 1;
}

.forum-tab:hover {
  color: var(--text-primary);
}

.forum-tab--active {
  color: var(--color-primary-700);
}

.tab-underline {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 32px;
  height: 2px;
  background-color: var(--color-primary-700);
  border-radius: 1px;
  transform: translateX(-50%) scaleX(0);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.forum-tab--active .tab-underline {
  transform: translateX(-50%) scaleX(1);
}
</style>
