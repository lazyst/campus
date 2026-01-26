<template>
  <div class="min-h-screen bg-gray-100">
    <!-- Navigation Bar -->
    <NavBar title="论坛" />
    
    <!-- Category Tabs -->
    <div class="flex gap-3 px-4 py-3 bg-white overflow-x-auto whitespace-nowrap">
      <span 
        :class="[
          'px-3 py-1.5 rounded-full text-sm cursor-pointer transition-colors flex-shrink-0',
          activeBoard === 'all' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
        ]"
        @click="activeBoard = 'all'"
      >
        全部
      </span>
      <span 
        v-for="board in boards" 
        :key="board.id"
        :class="[
          'px-3 py-1.5 rounded-full text-sm cursor-pointer transition-colors flex-shrink-0',
          activeBoard === board.id ? 'bg-primary text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
        ]"
        @click="activeBoard = board.id"
      >
        {{ board.name }}
      </span>
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
