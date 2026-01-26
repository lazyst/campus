<template>
  <div class="my-items min-h-screen bg-gray-100 pt-2.5">
    <NavBar title="我的闲置" :left-arrow="true" @click-left="onClickLeft" />

    <div class="p-2.5">
      <!-- Loading State -->
      <div v-if="loading && items.length === 0" class="text-center py-8">
        <span class="text-gray-400">加载中...</span>
      </div>

      <!-- Empty State -->
      <div v-else-if="items.length === 0" class="text-center py-8">
        <span class="text-gray-400">暂无物品</span>
      </div>

      <!-- Item List -->
      <div
        v-for="item in items"
        :key="item.id"
        class="item-row flex bg-white rounded-lg p-3 mb-2.5"
        @click="onItemClick(item)"
      >
        <div class="item-image w-20 h-20 bg-primary text-white rounded-lg flex items-center justify-center mr-3">
          物
        </div>
        <div class="item-info flex-1">
          <div class="item-title text-base font-bold mb-2">{{ item.title }}</div>
          <div class="item-price text-lg text-red-500 mb-2">¥{{ item.price }}</div>
          <div class="item-status">
            <span
              :class="[
                'text-xs px-2 py-0.5 rounded',
                getStatusClass(item.status)
              ]"
            >
              {{ getStatusText(item.status) }}
            </span>
          </div>
        </div>
      </div>

      <!-- Loading More -->
      <div v-if="loading && items.length > 0" class="text-center py-4">
        <span class="text-gray-400">加载中...</span>
      </div>

      <!-- Finished -->
      <div v-if="finished && items.length > 0" class="text-center py-4">
        <span class="text-gray-400 text-sm">没有更多了</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyItems } from '@/api/modules'
import NavBar from '@/components/navigation/NavBar.vue'

const router = useRouter()
const items = ref([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)

async function loadItems() {
  if (loading.value || finished.value) return

  try {
    loading.value = true
    const { records, total } = await getMyItems({
      page: page.value,
      size: 10
    })
    items.value.push(...records)
    page.value++

    if (items.value.length >= total) {
      finished.value = true
    }
  } catch (error) {
    console.error('获取物品列表失败:', error)
  } finally {
    loading.value = false
  }
}

function onItemClick(item) {
  router.push(`/trade/${item.id}`)
}

function onClickLeft() {
  router.back()
}

function getStatusText(status) {
  const statusMap = {
    0: '在售',
    1: '已下架',
    2: '已完成'
  }
  return statusMap[status] || '未知'
}

function getStatusClass(status) {
  const classMap = {
    0: 'bg-green-100 text-green-600',
    1: 'bg-yellow-100 text-yellow-600',
    2: 'bg-gray-100 text-gray-600'
  }
  return classMap[status] || 'bg-gray-100 text-gray-600'
}

onMounted(() => {
  loadItems()
})
</script>
