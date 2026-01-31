<template>
  <div class="create-trade-page">
    <!-- 状态栏 -->
    <div class="create-status-bar">
      <button class="create-back-btn" @click="goBack">‹</button>
      <span class="create-page-title">发布闲置</span>
      <button 
        class="create-publish-btn" 
        @click="publish"
        :disabled="!isFormValid || isPublishing"
      >
        {{ isPublishing ? '发布中...' : '发布' }}
      </button>
    </div>

    <!-- 表单内容 -->
    <div class="create-form-content">
      <!-- 物品名称 -->
      <div class="create-form-group">
        <label class="create-form-label">物品名称</label>
        <input 
          v-model="title"
          type="text"
          class="create-form-input"
          placeholder="请输入物品名称"
        />
      </div>

      <!-- 闲置类型 -->
      <div class="create-form-group">
        <label class="create-form-label">闲置类型</label>
        <div class="create-type-options">
          <button
            class="create-type-btn"
            :class="{ 'create-type-btn--active': itemType === 2 }"
            @click="itemType = 2"
          >
            出售
          </button>
          <button
            class="create-type-btn"
            :class="{ 'create-type-btn--active': itemType === 1 }"
            @click="itemType = 1"
          >
            求购
          </button>
        </div>
      </div>

      <!-- 价格 -->
      <div class="create-form-group">
        <label class="create-form-label">价格</label>
        <div class="create-price-row">
          <span class="create-price-symbol">¥</span>
          <input 
            v-model="price"
            type="number"
            class="create-price-input"
            placeholder="0"
          />
          <span v-if="originalPrice" class="create-original-price">
            原价 ¥{{ originalPrice }}
          </span>
        </div>
      </div>

      <!-- 分类 -->
      <div class="create-form-group">
        <label class="create-form-label">分类</label>
        <div class="create-category-options">
          <button
            v-for="cat in categories"
            :key="cat"
            class="create-category-btn"
            :class="{ 'create-category-btn--active': category === cat }"
            @click="category = cat"
          >
            {{ cat }}
          </button>
        </div>
      </div>

      <!-- 成色 -->
      <div class="create-form-group">
        <label class="create-form-label">成色</label>
        <div class="create-condition-options">
          <button 
            v-for="item in conditions" 
            :key="item"
            class="create-condition-btn"
            :class="{ 'create-condition-btn--active': condition === item }"
            @click="condition = item"
          >
            {{ item }}
          </button>
        </div>
      </div>

      <!-- 描述 -->
      <div class="create-form-group">
        <label class="create-form-label">物品描述</label>
        <textarea 
          v-model="description"
          class="create-form-textarea"
          placeholder="描述物品的使用情况、新旧程度、入手渠道等..."
          rows="6"
        ></textarea>
      </div>

      <!-- 图片上传 -->
      <div class="create-form-group">
        <label class="create-form-label">上传图片（最多9张）</label>
        <div class="create-image-upload">
          <!-- 有图片时显示图片预览列表 -->
          <template v-if="previewImages.length > 0">
            <div 
              v-for="(url, index) in previewImages" 
              :key="index" 
              class="create-image-preview"
            >
              <img :src="url" :alt="'图片' + (index + 1)" />
              <button class="create-image-remove" @click="removeImage(index)">×</button>
            </div>
          </template>
          
          <!-- 无图片时显示商品描述预览 -->
          <div v-else class="create-no-image-placeholder">
            <div class="create-no-image-content">
              <span class="create-no-image-label">商品描述</span>
              <p class="create-no-image-description">{{ description || '暂无描述' }}</p>
            </div>
          </div>
          
          <!-- 上传按钮 -->
          <div 
            v-if="previewImages.length < 9" 
            class="create-upload-btn" 
            @click="triggerFileInput"
          >
            <span class="create-upload-icon">+</span>
          </div>
        </div>
        <input 
          ref="fileInput"
          type="file" 
          accept="image/*" 
          multiple 
          style="display: none"
          @change="handleFileChange"
        />
      </div>

      <!-- 交易地点 -->
      <div class="create-form-group">
        <label class="create-form-label">交易地点</label>
        <input 
          v-model="location"
          type="text"
          class="create-form-input"
          placeholder="请输入交易地点（如：东校区图书馆门口）"
        />
      </div>

      <!-- 提示 -->
      <div class="create-tips">
        提示：清晰的图片和详细的描述能更快卖出哦
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { createItem } from '@/api/modules/item';
import { uploadImages } from '@/api/modules/upload';
import { showToast } from '@/services/toastService';

const router = useRouter();
const fileInput = ref<HTMLInputElement | null>(null);

const isPublishing = ref(false);
const previewImages = ref<string[]>([]);
const uploadedImages = ref<string[]>([]);

const categories = ['电脑数码', '学习资料', '生活用品', '出行工具', '服装鞋包', '书籍教材', '其他'];
const conditions = ['全新', '99新', '95新', '9成新', '8成新'];

// 使用独立的 ref 变量确保响应式更新正确工作
const title = ref('');
const itemType = ref(2); // 1=求购, 2=出售
const price = ref('');
const originalPrice = ref('');
const category = ref('');
const condition = ref('95新');
const description = ref('');
const location = ref('');

// 统一的表单对象用于验证
const form = reactive({
  title,
  price,
  originalPrice,
  category,
  condition,
  description,
  location,
});

const isFormValid = computed(() => {
  const titleValid = title.value.length > 0;
  const priceValid = price.value && price.value.toString().length > 0;
  const categoryValid = category.value.length > 0;
  const descriptionValid = description.value.length > 0;
  const result = titleValid && priceValid && categoryValid && descriptionValid;
  
  console.log('Form validation:', {
    title: title.value,
    titleValid,
    price: price.value,
    priceValid,
    category: category.value,
    categoryValid,
    description: description.value,
    descriptionValid,
    isPublishing: isPublishing.value,
    result
  });
  
  return result;
});

function goBack() {
  router.back();
}

function triggerFileInput() {
  if (fileInput.value) {
    fileInput.value.click();
  }
}

async function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const files = input.files;
  
  if (!files || files.length === 0) return;

  // 检查总数量限制
  const remaining = 9 - previewImages.value.length;
  const filesToUpload = Array.from(files).slice(0, remaining);

  // 本地预览
  for (const file of filesToUpload) {
    const reader = new FileReader();
    reader.onload = (e) => {
      previewImages.value.push(e.target?.result as string);
    };
    reader.readAsDataURL(file);
  }

  // 上传到服务器
  try {
    const urls = await uploadImages(filesToUpload);
    uploadedImages.value.push(...urls);
    showToast(`成功上传 ${filesToUpload.length} 张图片`);
  } catch (error) {
    console.error('上传失败:', error);
    showToast('图片上传失败，请重试', 'error');
  }

  // 清空input以便再次选择相同文件
  input.value = '';
}

function removeImage(index: number) {
  previewImages.value.splice(index, 1);
  uploadedImages.value.splice(index, 1);
}

async function publish() {
  if (!isFormValid.value || isPublishing.value) return;
  
  isPublishing.value = true;
  
  try {
    await createItem({
      title: title.value,
      description: description.value,
      price: parseFloat(price.value),
      type: itemType.value,
      category: category.value,
      images: JSON.stringify(uploadedImages.value),
      location: location.value
    });
    
    showToast('发布成功！');
    router.back();
  } catch (error: any) {
    console.error('发布失败:', error);
    showToast(error.message || '发布失败，请重试');
  } finally {
    isPublishing.value = false;
  }
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

.create-category-options {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.create-category-btn {
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm);
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);
  background-color: var(--bg-tertiary);
  color: var(--text-secondary);
}

.create-category-btn:active {
  background-color: var(--color-primary-100);
}

.create-category-btn--active {
  background-color: var(--color-primary-700);
  color: var(--text-inverse);
}

.create-type-options {
  display: flex;
  gap: var(--space-3);
}

.create-type-btn {
  flex: 1;
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  border: 2px solid var(--border-light);
  border-radius: var(--radius-lg);
  background-color: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.create-type-btn:active {
  background-color: var(--bg-secondary);
}

.create-type-btn--active {
  border-color: var(--color-primary-700);
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

.create-image-upload {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.create-image-preview {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-card);
}

.create-image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.create-image-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.create-image-remove:active {
  background-color: rgba(0, 0, 0, 0.8);
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

/* 无图片时的占位符样式 */
.create-no-image-placeholder {
  width: 80px;
  height: 80px;
  border: 1px dashed var(--border-default);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-tertiary);
  overflow: hidden;
}

.create-no-image-content {
  width: 100%;
  height: 100%;
  padding: var(--space-2);
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: var(--space-1);
}

.create-no-image-label {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.create-no-image-description {
  font-size: var(--text-xs);
  color: var(--text-secondary);
  line-height: var(--line-height-tight);
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
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
