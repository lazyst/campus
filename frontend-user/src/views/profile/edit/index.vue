<template>
  <div class="profile-edit-page">
    <!-- 状态栏 -->
    <div class="status-bar">
      <span class="back-btn" @click="goBack">‹</span>
      <span class="page-title">编辑资料</span>
      <span class="save-btn" @click="save">保存</span>
    </div>

    <!-- 头像区域 -->
    <div class="avatar-section">
      <div class="avatar-wrapper">
        <div class="avatar">
          <span>{{ form.nickname.charAt(0) || '用' }}</span>
        </div>
        <span class="avatar-action">点击更换头像</span>
      </div>
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

      <!-- 所在校区 -->
      <div class="form-row">
        <label class="form-label">所在校区</label>
        <div class="selector">
          <span>{{ form.campus || '东校区' }}</span>
          <span class="selector-arrow">›</span>
        </div>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-section">
      <button class="logout-btn" @click="logout">退出登录</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const genders = ['男', '女'];

const form = reactive({
  nickname: '校园小助手',
  gender: '男',
  bio: '热爱学习，喜欢交朋友的在校大学生~',
  grade: '2023级',
  major: '计算机科学与技术',
  campus: '东校区',
});

function goBack() {
  router.back();
}

function save() {
  console.log('保存资料:', form);
  // TODO: 实现保存功能
  router.back();
}

function logout() {
  console.log('退出登录');
  // TODO: 实现退出登录
  router.push('/login');
}
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
}

.avatar-action {
  font-size: 13px;
  color: #6366F1;
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
