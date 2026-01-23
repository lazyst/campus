<template>
  <div class="create-post">
    <van-nav-bar title="发布帖子" left-arrow @click-left="onClickLeft" />
    
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.boardName"
          name="board"
          label="板块"
          readonly
          required
          @click="showBoardPicker = true"
        />
        <van-field
          v-model="form.title"
          name="title"
          label="标题"
          placeholder="请输入帖子标题"
          required
          :rules="[{ required: true, message: '请输入标题' }]"
        />
        <van-field
          v-model="form.content"
          name="content"
          label="内容"
          type="textarea"
          rows="6"
          placeholder="请输入帖子内容"
          required
          :rules="[{ required: true, message: '请输入内容' }]"
        />
      </van-cell-group>
      
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit" :loading="submitting">
          发布帖子
        </van-button>
      </div>
    </van-form>
    
    <van-popup v-model:show="showBoardPicker" position="bottom">
      <van-picker
        :columns="boardColumns"
        :default-index="0"
        @confirm="onBoardConfirm"
        @cancel="showBoardPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast, showSuccessToast } from 'vant'
import { getBoards, createPost, type Board } from '@/api/forum'

const router = useRouter()
const showBoardPicker = ref(false)
const submitting = ref(false)
const boards = ref<Board[]>([])
const boardColumns = ref<{text: string, value: number}[]>([])

const form = ref({
  boardId: 0 as number,
  boardName: '',
  title: '',
  content: ''
})

function onClickLeft() {
  router.back()
}

function onBoardConfirm({ selectedValues, selectedOptions }: any) {
  form.value.boardId = selectedValues[0]
  form.value.boardName = selectedOptions[0].text
  showBoardPicker.value = false
}

async function onSubmit() {
  if (!form.value.boardId) {
    showFailToast('请选择板块')
    return
  }
  
  try {
    submitting.value = true
    await createPost({
      boardId: form.value.boardId,
      title: form.value.title,
      content: form.value.content
    })
    showSuccessToast('发布成功')
    router.back()
  } catch (error: any) {
    console.error('发布帖子失败:', error)
    showFailToast(error.message || '发布失败')
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

<style scoped>
.create-post { min-height: 100vh; background: #f5f5f5; }
</style>
