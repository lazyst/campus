<template>
  <div class="profile-edit-page">
    <!-- 状态栏 -->
    <div class="status-bar">
      <span class="back-btn" @click="goBack">‹</span>
      <span class="page-title">编辑资料</span>
      <span class="save-btn" :class="{ disabled: saving }" @click="save">{{ saving ? '保存中...' : '保存' }}</span>
    </div>

    <!-- 头像区域 -->
    <div class="avatar-section">
      <div class="avatar-wrapper" @click="triggerAvatarInput">
        <div class="avatar">
          <img v-if="avatarUrl" :src="avatarUrl" alt="头像" />
          <span v-else>{{ form.nickname.charAt(0) || '用' }}</span>
        </div>
        <span class="avatar-action">点击更换头像</span>
      </div>
      <!-- 隐藏的文件输入 -->
      <input
        ref="avatarInput"
        type="file"
        accept="image/*"
        class="hidden-input"
        @change="onAvatarChange"
      />
    </div>

    <!-- 表单区域 -->
    <div class="form-section">
      <!-- 昵称 -->
      <div class="form-row">
        <label class="form-label">昵称</label>
        <input 
          v-model="form.nickname"
          type="text"
          class="form-input"
          placeholder="请输入昵称"
        />
      </div>

      <!-- 性别 -->
      <div class="form-row">
        <label class="form-label">性别</label>
        <div class="gender-options">
          <button 
            v-for="gender in genders" 
            :key="gender"
            class="gender-btn"
            :class="{ active: form.gender === gender }"
            @click="form.gender = gender"
          >
            {{ gender }}
          </button>
        </div>
      </div>

      <!-- 个人简介 -->
      <div class="form-row bio-row">
        <label class="form-label">个人简介</label>
        <textarea 
          v-model="form.bio"
          class="form-textarea"
          placeholder="介绍下自己吧..."
          rows="4"
        ></textarea>
        <span class="char-count">{{ form.bio.length }}/50</span>
      </div>

      <!-- 年级 -->
      <div class="form-row">
        <label class="form-label">年级</label>
        <div class="selector">
          <span>{{ form.grade || '2023级' }}</span>
          <span class="selector-arrow">›</span>
        </div>
      </div>

      <!-- 专业 -->
      <div class="form-row">
        <label class="form-label">专业</label>
        <input 
          v-model="form.major"
          type="text"
          class="form-input"
          placeholder="请输入专业"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { showToast } from '@/services/toastService';
import { updateProfile, uploadAvatar } from '@/api/modules/user';
import { getImageUrl } from '@/utils/imageUrl';

const router = useRouter();
const userStore = useUserStore();
const saving = ref(false);
const uploadingAvatar = ref(false);

const avatarInput = ref<HTMLInputElement | null>(null);
const avatarUrl = ref('');

const genders = ['男', '女'];

const form = reactive({
  nickname: '',
  gender: '',
  bio: '',
  grade: '',
  major: '',
});

// 触发文件选择
function triggerAvatarInput() {
  avatarInput.value?.click();
}

// 处理头像选择
async function onAvatarChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  // 验证文件大小 (2MB)
  if (file.size > 2 * 1024 * 1024) {
    showToast('图片大小不能超过2MB', 'error');
    input.value = '';
    return;
  }

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    showToast('请选择图片文件', 'error');
    input.value = '';
    return;
  }

  try {
    uploadingAvatar.value = true;
    const url = await uploadAvatar(file);
    avatarUrl.value = getImageUrl(url);
    showToast('头像上传成功', 'success');
  } catch (error: any) {
    console.error('头像上传失败:', error);
    showToast(error.message || '头像上传失败', 'error');
  } finally {
    uploadingAvatar.value = false;
    input.value = '';
  }
}

function goBack() {
  router.back();
}

async function save() {
  if (!form.nickname.trim()) {
    showToast('请输入昵称');
    return;
  }

  try {
    saving.value = true;
    
    // 性别转换：男/女 -> 1/2
    const genderMap: Record<string, number> = {
      '男': 1,
      '女': 2
    };
    
    await updateProfile({
      nickname: form.nickname,
      gender: genderMap[form.gender] || 0,
      bio: form.bio,
      avatar: avatarUrl.value || undefined,
      grade: form.grade,
      major: form.major
    });
    
    // 立即刷新用户信息到 store
    await userStore.fetchUserInfo();
    
    showToast('保存成功', 'success');
    router.back();
  } catch (error: any) {
    console.error('保存失败:', error);
    showToast(error.message || '保存失败', 'error');
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  // 从 Store 获取用户信息初始化表单
  if (userStore.userInfo) {
    form.nickname = userStore.userInfo.nickname || '';
    form.gender = userStore.userInfo.gender === 1 ? '男' : userStore.userInfo.gender === 2 ? '女' : '';
    form.bio = userStore.userInfo.bio || '';
    form.grade = userStore.userInfo.grade || '';
    form.major = userStore.userInfo.major || '';
    // 初始化头像
    if (userStore.userInfo.avatar) {
      avatarUrl.value = getImageUrl(userStore.userInfo.avatar);
    }
  }
});
</script>

<style scoped>
.profile-edit-page {
  min-height: 100vh;
  background: #F8FAFC;
}

.status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: white;
}

.back-btn {
  font-size: 28px;
  color: #1E293B;
  cursor: pointer;
}

.page-title {
  font-size: 17px;
  font-weight: 600;
  color: #1E293B;
}

.save-btn {
  font-size: 15px;
  font-weight: 500;
  color: #6366F1;
  cursor: pointer;
}

.save-btn.disabled {
  color: #94A3B8;
  cursor: not-allowed;
}

.avatar-section {
  display: flex;
  justify-content: center;
  padding: 24px;
  background: white;
}

.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #64748B;
  overflow: hidden;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-action {
  font-size: 13px;
  color: #6366F1;
}

.avatar-wrapper {
  cursor: pointer;
}

.avatar-wrapper:active {
  opacity: 0.8;
}

.hidden-input {
  display: none;
}

.form-section {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 1px;
  background: white;
}

.form-row {
  display: flex;
  align-items: center;
  height: 56px;
  border-bottom: 1px solid #F1F5F9;
}

.form-label {
  width: 60px;
  font-size: 15px;
  color: #1E293B;
}

.form-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 15px;
  color: #1E293B;
  outline: none;
}

.gender-options {
  display: flex;
  gap: 24px;
}

.gender-btn {
  padding: 8px 24px;
  border: none;
  border-radius: 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.gender-btn:not(.active) {
  background: #F1F5F9;
  color: #64748B;
}

.gender-btn.active {
  background: #EEF2FF;
  color: #6366F1;
}

.bio-row {
  flex-direction: column;
  align-items: flex-start;
  height: auto;
  padding: 16px 0;
}

.bio-row .form-label {
  margin-bottom: 8px;
}

.form-textarea {
  width: 100%;
  border: none;
  background: transparent;
  font-size: 15px;
  color: #1E293B;
  resize: none;
  outline: none;
  font-family: inherit;
}

.char-count {
  align-self: flex-end;
  font-size: 13px;
  color: #94A3B8;
  margin-top: 4px;
}

.selector {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}

.selector span:first-child {
  font-size: 15px;
  color: #1E293B;
}

.selector-arrow {
  font-size: 16px;
  color: #CBD5E1;
}

.logout-section {
  padding: 200px 16px 0;
}

.logout-btn {
  width: 100%;
  height: 48px;
  background: #FEE2E2;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 500;
  color: #EF4444;
  cursor: pointer;
}
</style>
