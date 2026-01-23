<template>
  <div class="profile-edit min-h-screen bg-gray-100">
    <NavBar title="编辑资料" :left-arrow="true" @click-left="onClickLeft" />
    
    <form @submit.prevent="onSubmit" class="p-4">
      <!-- Nickname Input -->
      <div class="mb-4">
        <BaseInput
          v-model="form.nickname"
          label="昵称"
          placeholder="请输入昵称"
          :error="!form.nickname && attemptedSubmit"
          error-message="请输入昵称"
        />
      </div>
      
      <!-- Gender Selection -->
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-2">性别</label>
        <div 
          class="w-full border border-gray-300 rounded-md px-4 py-3 bg-white cursor-pointer flex justify-between items-center"
          @click="showGenderPicker = true"
        >
          <span class="text-gray-900">{{ form.gender }}</span>
          <span class="text-gray-500">›</span>
        </div>
      </div>
      
      <!-- Bio Input -->
      <div class="mb-6">
        <label class="block text-sm font-medium text-gray-700 mb-2">个人简介</label>
        <textarea
          v-model="form.bio"
          rows="3"
          placeholder="请输入个人简介"
          class="block w-full border border-gray-300 rounded-md px-4 py-3 text-base outline-none focus:border-primary focus:ring-1 focus:ring-primary resize-none"
        ></textarea>
      </div>
      
      <!-- Submit Button -->
      <BaseButton type="primary" block round @click="onSubmit">
        保存
      </BaseButton>
    </form>
    
    <!-- Simple Gender Picker Modal -->
    <div v-if="showGenderPicker" class="fixed inset-0 bg-black/50 z-50 flex items-end">
      <div class="w-full bg-white rounded-t-xl pb-safe">
        <div class="flex justify-between items-center p-4 border-b">
          <span class="text-gray-500" @click="showGenderPicker = false">取消</span>
          <span class="font-medium">选择性别</span>
          <span class="text-primary" @click="onGenderConfirm">确定</span>
        </div>
        <div class="max-h-64 overflow-y-auto">
          <div 
            v-for="(gender, index) in genderColumns" 
            :key="index"
            class="p-4 border-b last:border-b-0 cursor-pointer"
            :class="selectedGenderIndex === index ? 'bg-blue-50' : ''"
            @click="selectedGenderIndex = index"
          >
            {{ gender }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '@/components/navigation/NavBar.vue'
import BaseInput from '@/components/base/Input.vue'
import BaseButton from '@/components/base/Button.vue'

const router = useRouter()
const showGenderPicker = ref(false)
const attemptedSubmit = ref(false)
const genderColumns = ['未知', '男', '女']
const selectedGenderIndex = ref(1)

const form = ref({
  nickname: '测试用户',
  gender: '男',
  bio: ''
})

function onClickLeft() {
  router.back()
}

function onGenderConfirm() {
  form.value.gender = genderColumns[selectedGenderIndex.value]
  showGenderPicker.value = false
}

function onSubmit() {
  attemptedSubmit.value = true
  
  if (!form.value.nickname) {
    return
  }
  
  // Save logic here
  alert('保存功能开发中')
  router.back()
}
</script>
