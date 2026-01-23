<template>
  <div class="create-post min-h-screen bg-gray-100">
    <NavBar title="发布帖子" :left-arrow="true" @click-left="onClickLeft" />
    
    <form @submit.prevent="onSubmit" class="p-4">
      <!-- Board Selection -->
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-2">板块</label>
        <div 
          class="w-full border border-gray-300 rounded-md px-4 py-3 bg-white cursor-pointer flex justify-between items-center"
          :class="{ 'border-danger': !form.boardId }"
          @click="showBoardPicker = true"
        >
          <span :class="form.boardName ? 'text-gray-900' : 'text-gray-400'">
            {{ form.boardName || '请选择板块' }}
          </span>
          <span class="text-gray-500">›</span>
        </div>
        <p v-if="!form.boardId && attemptedSubmit" class="text-danger text-sm mt-1">请选择板块</p>
      </div>
      
      <!-- Title Input -->
      <div class="mb-4">
        <BaseInput
          v-model="form.title"
          label="标题"
          placeholder="请输入帖子标题"
          :error="!form.title && attemptedSubmit"
          error-message="请输入标题"
        />
      </div>
      
      <!-- Content Input -->
      <div class="mb-6">
        <label class="block text-sm font-medium text-gray-700 mb-2">内容</label>
        <textarea
          v-model="form.content"
          rows="6"
          placeholder="请输入帖子内容"
          class="w-full border rounded-md px-4 py-3 text-base outline-none transition-colors duration-200 resize-none"
          :class="[
            !form.content && attemptedSubmit ? 'border-danger focus:border-danger focus:ring-1 focus:ring-danger' : 'border-gray-300 focus:border-primary focus:ring-1 focus:ring-primary'
          ]"
        ></textarea>
        <p v-if="!form.content && attemptedSubmit" class="text-danger text-sm mt-1">请输入内容</p>
      </div>
      
      <!-- Submit Button -->
      <BaseButton type="primary" block round :loading="submitting" @click="onSubmit">
        发布帖子
      </BaseButton>
    </form>
    
    <!-- Simple Board Picker Modal -->
    <div v-if="showBoardPicker" class="fixed inset-0 bg-black/50 z-50 flex items-end">
      <div class="w-full bg-white rounded-t-xl pb-safe">
        <div class="flex justify-between items-center p-4 border-b">
          <span class="text-gray-500" @click="showBoardPicker = false">取消</span>
          <span class="font-medium">选择板块</span>
          <span class="text-primary" @click="onBoardConfirm">确定</span>
        </div>
        <div class="max-h-64 overflow-y-auto">
          <div 
            v-for="(board, index) in boardColumns" 
            :key="board.value"
            class="p-4 border-b last:border-b-0 cursor-pointer"
            :class="selectedBoardIndex === index ? 'bg-blue-50' : ''"
            @click="selectedBoardIndex = index"
          >
            {{ board.text }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '@/components/navigation/NavBar.vue'
import BaseInput from '@/components/base/Input.vue'
import BaseButton from '@/components/base/Button.vue'
import { getBoards, createPost, type Board } from '@/api/forum'

const router = useRouter()
const showBoardPicker = ref(false)
const submitting = ref(false)
const attemptedSubmit = ref(false)
const boards = ref<Board[]>([])
const boardColumns = ref<{text: string, value: number}[]>([])
const selectedBoardIndex = ref(0)

const form = ref({
  boardId: 0 as number,
  boardName: '',
  title: '',
  content: ''
})

function onClickLeft() {
  router.back()
}

function onBoardConfirm() {
  if (boardColumns.value[selectedBoardIndex.value]) {
    form.value.boardId = boardColumns.value[selectedBoardIndex.value].value
    form.value.boardName = boardColumns.value[selectedBoardIndex.value].text
  }
  showBoardPicker.value = false
}

async function onSubmit() {
  attemptedSubmit.value = true
  
  if (!form.value.boardId || !form.value.title || !form.value.content) {
    return
  }
  
  try {
    submitting.value = true
    await createPost({
      boardId: form.value.boardId,
      title: form.value.title,
      content: form.value.content
    })
    alert('发布成功')
    router.back()
  } catch (error: any) {
    console.error('发布帖子失败:', error)
    alert(error.message || '发布失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    boards.value = await getBoards()
    boardColumns.value = boards.value.map(b => ({ text: b.name, value: b.id }))
  } catch (error) {
    console.error('获取板块列表失败:', error)
  }
})
</script>
