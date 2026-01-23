<template>
  <div class="create-item">
    <van-nav-bar title="发布闲置" left-arrow @click-left="onClickLeft" />
    
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.type"
          name="type"
          label="类型"
          readonly
          @click="showTypePicker = true"
        />
        <van-field
          v-model="form.title"
          name="title"
          label="标题"
          placeholder="请输入物品标题"
          :rules="[{ required: true, message: '请输入标题' }]"
        />
        <van-field
          v-model="form.price"
          name="price"
          label="价格"
          type="number"
          placeholder="请输入价格"
          prefix="¥"
          :rules="[{ required: true, message: '请输入价格' }]"
        />
        <van-field
          v-model="form.description"
          name="description"
          label="描述"
          type="textarea"
          rows="4"
          placeholder="请输入物品描述"
        />
      </van-cell-group>
      
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit">
          发布
        </van-button>
      </div>
    </van-form>
    
    <van-popup v-model:show="showTypePicker" position="bottom">
      <van-picker
        :columns="typeColumns"
        @confirm="onTypeConfirm"
        @cancel="showTypePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const showTypePicker = ref(false)
const typeColumns = ['出售', '收购']

const form = ref({
  type: '出售',
  title: '',
  price: '',
  description: ''
})

function onClickLeft() {
  router.back()
}

function onTypeConfirm({ selectedValues }: any) {
  form.value.type = typeColumns[selectedValues[0]]
  showTypePicker.value = false
}

function onSubmit() {
  // TODO: Implement create item logic
  router.back()
}
</script>

<style scoped>
.create-item { min-height: 100vh; background: #f5f5f5; }
</style>
