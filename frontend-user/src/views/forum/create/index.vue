<template>
  <div class="create-forum-page">
    <!-- 状态栏 -->
    <div class="create-status-bar">
      <button class="create-back-btn" @click="goBack">‹</button>
      <span class="create-page-title">发布帖子</span>
      <button 
        class="create-publish-btn" 
        @click="publish"
        :disabled="!isFormValid"
      >
        发布
      </button>
    </div>

    <!-- 表单内容 -->
    <div class="create-form-content">
      <!-- 标题输入 -->
      <div class="create-form-group">
        <label class="create-form-label">帖子标题</label>
        <input 
          v-model="form.title"
          type="text"
          class="create-form-input"
          placeholder="请输入标题（5-30字）"
        />
      </div>

      <!-- 版块选择 -->
      <div class="create-form-group">
        <label class="create-form-label">选择版块</label>
        <div class="create-selector" @click="showBoardPicker = true">
          <span :class="{ 'create-selector-placeholder': !form.boardName }">
            {{ form.boardName || '请选择' }}
          </span>
          <span class="create-selector-arrow">›</span>
        </div>
      </div>

      <!-- 内容输入 -->
      <div class="create-form-group">
        <label class="create-form-label">帖子内容</label>
        <textarea 
          v-model="form.content"
          class="create-form-textarea"
          placeholder="分享你的想法、经验或求助..."
          rows="8"
        ></textarea>
      </div>

      <!-- 图片上传 -->
      <div class="create-form-group">
        <label class="create-form-label">添加图片（最多9张）</label>
        <div class="create-image-upload">
          <div class="create-upload-btn">
            <span class="create-upload-icon">+</span>
            <span class="create-upload-text">点击上传图片</span>
          </div>
        </div>
      </div>

      <!-- 标签 -->
      <div class="create-form-group">
        <label class="create-form-label">添加标签</label>
        <input 
          v-model="form.tags"
          type="text"
          class="create-form-input"
          placeholder="输入标签后按回车"
        />
      </div>

      <!-- 提示 -->
      <div class="create-tips">
        提示：描述越详细，越容易获得帮助哦
      </div>
    </div>

    <!-- 板块选择器 -->
    <div v-if="showBoardPicker" class="create-picker-overlay" @click.self="showBoardPicker = false">
      <div class="create-picker-content">
        <div class="create-picker-header">
          <span class="create-picker-cancel" @click="showBoardPicker = false">取消</span>
          <span class="create-picker-title">选择板块</span>
          <span class="create-picker-confirm" @click="confirmBoard">确定</span>
        </div>
        <div class="create-picker-list">
          <div 
            v-for="(board, index) in boards" 
            :key="board.value"
            class="create-picker-item"
            :class="{ 'create-picker-item--active': selectedBoard === index }"
            @click="selectedBoard = index"
          >
            {{ board.text }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const showBoardPicker = ref(false);
const selectedBoard = ref(0);

const boards = [
  { value: 1, text: '交流' },
  { value: 2, text: '学习' },
  { value: 3, text: '搭子' },
  { value: 4, text: '闲置' },
];

const form = reactive({
  title: '',
  boardName: '',
  boardId: 0,
  content: '',
  tags: '',
});

const isFormValid = computed(() => {
  return form.title.length >= 5 && 
         form.title.length <= 30 && 
         form.boardId > 0 && 
         form.content.length > 0;
});

function goBack() {
  router.back();
}

function confirmBoard() {
  if (boards[selectedBoard.value]) {
    form.boardName = boards[selectedBoard.value].text;
    form.boardId = boards[selectedBoard.value].value;
  }
  showBoardPicker.value = false;
}

function publish() {
  console.log('发布帖子:', form);
  router.back();
}
</script>

<style scoped>
.create-forum-page {
  min-height: 100vh;
  background-color: var(--bg-secondary);
}

.create-status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-4);
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
}

.create-back-btn {
  font-size: 28px;
  color: var(--text-primary);
  background: none;
  border: none;
  cursor: pointer;
}

.create-back-btn:active {
  opacity: 0.7;
}

.create-page-title {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.create-publish-btn {
  width: 64px;
  height: 32px;
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-inverse);
  background-color: var(--color-primary-700);
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
}

.create-publish-btn:active {
  background-color: var(--color-primary-800);
}

.create-publish-btn:disabled {
  background-color: var(--color-gray-300);
  cursor: not-allowed;
}

.create-form-content {
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.create-form-group {
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-3);
}

.create-form-label {
  display: block;
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.create-form-input {
  width: 100%;
  border: none;
  background: transparent;
  font-size: var(--text-base);
  color: var(--text-primary);
  outline: none;
}

.create-form-input::placeholder {
  color: var(--text-tertiary);
}

.create-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}

.create-selector span:first-child {
  font-size: var(--text-base);
  color: var(--text-primary);
}

.create-selector-placeholder {
  color: var(--text-tertiary) !important;
}

.create-selector-arrow {
  font-size: var(--text-lg);
  color: var(--text-tertiary);
}

.create-form-textarea {
  width: 100%;
  border: none;
  background: transparent;
  font-size: var(--text-base);
  color: var(--text-primary);
  resize: none;
  outline: none;
  font-family: inherit;
  line-height: var(--line-height-relaxed);
}

.create-form-textarea::placeholder {
  color: var(--text-tertiary);
}

.create-image-upload {
  display: flex;
  gap: var(--space-3);
}

.create-upload-btn {
  width: 100px;
  height: 100px;
  border: 1px dashed var(--border-default);
  border-radius: var(--radius-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.create-upload-btn:active {
  border-color: var(--color-primary-400);
  background-color: var(--color-primary-50);
}

.create-upload-icon {
  font-size: 36px;
  color: var(--text-tertiary);
}

.create-upload-text {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin-top: var(--space-1);
}

.create-tips {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  padding: var(--space-2) 0;
}

.create-picker-overlay {
  position: fixed;
  inset: 0;
  background-color: var(--bg-overlay);
  display: flex;
  align-items: flex-end;
  z-index: var(--z-modal);
}

.create-picker-content {
  width: 100%;
  max-height: 60vh;
  background-color: var(--bg-card);
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
}

.create-picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.create-picker-cancel {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  cursor: pointer;
}

.create-picker-cancel:active {
  opacity: 0.7;
}

.create-picker-title {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.create-picker-confirm {
  font-size: var(--text-sm);
  color: var(--color-primary-700);
  cursor: pointer;
}

.create-picker-confirm:active {
  opacity: 0.7;
}

.create-picker-list {
  max-height: 400px;
  overflow-y: auto;
}

.create-picker-item {
  padding: var(--space-4);
  text-align: center;
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.create-picker-item:active {
  background-color: var(--bg-secondary);
}

.create-picker-item--active {
  color: var(--color-primary-700);
  font-weight: var(--font-weight-medium);
}
</style>
