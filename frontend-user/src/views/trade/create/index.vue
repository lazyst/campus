<template>
  <div class="create-item min-h-screen bg-gray-100">
    <NavBar title="发布闲置" :left-arrow="true" @click-left="onClickLeft" />
    
    <form @submit.prevent="onSubmit" class="p-4">
      <!-- Type Selection -->
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-2">类型</label>
        <div 
          class="w-full border border-gray-300 rounded-md px-4 py-3 bg-white cursor-pointer flex justify-between items-center"
          @click="showTypePicker = true"
        >
          <span class="text-gray-900">{{ form.type }}</span>
          <span class="text-gray-500">›</span>
        </div>
      </div>
      
      <!-- Title Input -->
      <div class="mb-4">
        <BaseInput
          v-model="form.title"
          label="标题"
          placeholder="请输入物品标题"
          :error="!form.title && attemptedSubmit"
          error-message="请输入标题"
        />
      </div>
      
      <!-- Price Input -->
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-2">价格</label>
        <div class="relative rounded-md shadow-sm">
          <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500">¥</span>
          <input
            v-model="form.price"
            type="number"
            placeholder="请输入价格"
            class="block w-full border border-gray-300 rounded-md pl-8 pr-4 py-3 text-base outline-none focus:border-primary focus:ring-1 focus:ring-primary"
            :class="{ 'border-danger': !form.price && attemptedSubmit }"
          />
        </div>
        <p v-if="!form.price && attemptedSubmit" class="text-danger text-sm mt-1">请输入价格</p>
      </div>
      
      <!-- Description Input -->
      <div class="mb-6">
        <label class="block text-sm font-medium text-gray-700 mb-2">描述</label>
        <textarea
          v-model="form.description"
          rows="4"
          placeholder="请输入物品描述"
          class="block w-full border border-gray-300 rounded-md px-4 py-3 text-base outline-none focus:border-primary focus:ring-1 focus:ring-primary resize-none"
        ></textarea>
      </div>
      
      <!-- Submit Button -->
      <BaseButton type="primary" block round @click="onSubmit">
        发布
      </BaseButton>
    </form>
    
    <!-- Simple Type Picker Modal -->
    <div v-if="showTypePicker" class="fixed inset-0 bg-black/50 z-50 flex items-end">
      <div class="w-full bg-white rounded-t-xl pb-safe">
        <div class="flex justify-between items-center p-4 border-b">
          <span class="text-gray-500" @click="showTypePicker = false">取消</span>
          <span class="font-medium">选择类型</span>
          <span class="text-primary" @click="onTypeConfirm">确定</span>
        </div>
        <div class="max-h-64 overflow-y-auto">
          <div 
            v-for="(type, index) in typeColumns" 
            :key="index"
            class="p-4 border-b last:border-b-0 cursor-pointer"
            :class="selectedTypeIndex === index ? 'bg-blue-50' : ''"
            @click="selectedTypeIndex = index"
          >
            {{ type }}
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
const showTypePicker = ref(false)
const attemptedSubmit = ref(false)
const typeColumns = ['出售', '收购']
const selectedTypeIndex = ref(0)

const form = ref({
  type: '出售',
  title: '',
  price: '',
  description: ''
})

function onClickLeft() {
  router.back()
}

function onTypeConfirm() {
  form.value.type = typeColumns[selectedTypeIndex.value]
  showTypePicker.value = false
}

function onSubmit() {
  attemptedSubmit.value = true
  
  if (!form.value.title || !form.value.price) {
    return
  }
  
  // TODO: Implement create item logic
  alert('发布功能开发中')
  router.back()
}
</script>
