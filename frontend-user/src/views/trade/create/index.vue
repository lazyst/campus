<template>
  <div class="create-trade-page">
    <!-- 状态栏 -->
    <div class="create-status-bar">
      <button class="create-back-btn" @click="goBack">‹</button>
      <span class="create-page-title">发布闲置</span>
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
      <!-- 物品名称 -->
      <div class="create-form-group">
        <label class="create-form-label">物品名称</label>
        <input 
          v-model="form.title"
          type="text"
          class="create-form-input"
          placeholder="请输入物品名称"
        />
      </div>

      <!-- 价格 -->
      <div class="create-form-group">
        <label class="create-form-label">价格</label>
        <div class="create-price-row">
          <span class="create-price-symbol">¥</span>
          <input 
            v-model="form.price"
            type="number"
            class="create-price-input"
            placeholder="0"
          />
          <span v-if="form.originalPrice" class="create-original-price">
            原价 ¥{{ form.originalPrice }}
          </span>
        </div>
      </div>

      <!-- 分类 -->
      <div class="create-form-group">
        <label class="create-form-label">分类</label>
        <div class="create-selector" @click="showCategoryPicker = true">
          <span :class="{ 'create-selector-placeholder': !form.category }">
            {{ form.category || '请选择' }}
          </span>
          <span class="create-selector-arrow">›</span>
        </div>
      </div>

      <!-- 成色 -->
      <div class="create-form-group">
        <label class="create-form-label">成色</label>
        <div class="create-condition-options">
          <button 
            v-for="condition in conditions" 
            :key="condition"
            class="create-condition-btn"
            :class="{ 'create-condition-btn--active': form.condition === condition }"
            @click="form.condition = condition"
          >
            {{ condition }}
          </button>
        </div>
      </div>

      <!-- 描述 -->
      <div class="create-form-group">
        <label class="create-form-label">物品描述</label>
        <textarea 
          v-model="form.description"
          class="create-form-textarea"
          placeholder="描述物品的使用情况、新旧程度、入手渠道等..."
          rows="6"
        ></textarea>
      </div>

      <!-- 图片上传 -->
      <div class="create-form-group">
        <label class="create-form-label">上传图片（最多9张）</label>
        <div class="create-image-grid">
          <div class="create-upload-btn">
            <span class="create-upload-icon">+</span>
          </div>
          <div class="create-image-placeholder"></div>
          <div class="create-image-placeholder"></div>
        </div>
      </div>

      <!-- 交易地点 -->
      <div class="create-form-group">
        <label class="create-form-label">交易地点</label>
        <div class="create-selector" @click="showLocationPicker = true">
          <span :class="{ 'create-selector-placeholder': !form.location }">
            {{ form.location || '请选择' }}
          </span>
          <span class="create-selector-arrow">›</span>
        </div>
      </div>

      <!-- 提示 -->
      <div class="create-tips">
        提示：清晰的图片和详细的描述能更快卖出哦
      </div>
    </div>

    <!-- 分类选择器 -->
    <div v-if="showCategoryPicker" class="create-picker-overlay" @click.self="showCategoryPicker = false">
      <div class="create-picker-content">
        <div class="create-picker-header">
          <span class="create-picker-cancel" @click="showCategoryPicker = false">取消</span>
          <span class="create-picker-title">选择分类</span>
          <span class="create-picker-confirm" @click="confirmCategory">确定</span>
        </div>
        <div class="create-picker-list">
          <div 
            v-for="(category, index) in categories" 
            :key="category"
            class="create-picker-item"
            :class="{ 'create-picker-item--active': selectedCategory === index }"
            @click="selectedCategory = index"
          >
            {{ category }}
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

const showCategoryPicker = ref(false);
const showLocationPicker = ref(false);
const selectedCategory = ref(0);
const selectedLocation = ref(0);

const categories = ['电脑数码', '学习资料', '生活用品', '出行工具', '服装鞋包', '书籍教材', '其他'];
const conditions = ['全新', '99新', '95新', '9成新', '8成新'];
const locations = ['东校区', '西校区', '南校区', '北校区', '图书馆', '体育馆'];

const form = reactive({
  title: '',
  price: '',
  originalPrice: '',
  category: '',
  condition: '95新',
  description: '',
  location: '',
});

const isFormValid = computed(() => {
  return form.title.length > 0 && 
         form.price.length > 0 && 
         form.category.length > 0 && 
         form.description.length > 0;
});

function goBack() {
  router.back();
}

function confirmCategory() {
  form.category = categories[selectedCategory.value];
  showCategoryPicker.value = false;
}

function publish() {
  console.log('发布闲置:', form);
  router.back();
}
</script>

<style scoped>
.create-trade-page {
  min-height: 100vh;
  background-color: var(--bg-secondary);
  padding-bottom: var(--space-10);
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

.create-price-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.create-price-symbol {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-error-600);
}

.create-price-input {
  width: 100px;
  border: none;
  background: transparent;
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-error-600);
  outline: none;
}

.create-original-price {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  text-decoration: line-through;
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

.create-condition-options {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.create-condition-btn {
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);
  background-color: var(--bg-tertiary);
  color: var(--text-secondary);
}

.create-condition-btn:active {
  background-color: var(--color-primary-100);
}

.create-condition-btn--active {
  background-color: var(--color-primary-700);
  color: var(--text-inverse);
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

.create-image-grid {
  display: flex;
  gap: var(--space-3);
}

.create-upload-btn {
  width: 80px;
  height: 80px;
  border: 1px dashed var(--border-default);
  border-radius: var(--radius-md);
  display: flex;
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
  font-size: 32px;
  color: var(--text-tertiary);
}

.create-image-placeholder {
  width: 80px;
  height: 80px;
  background-color: var(--bg-tertiary);
  border-radius: var(--radius-md);
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
