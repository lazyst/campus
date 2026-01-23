<template>
  <div class="profile-edit">
    <van-nav-bar title="编辑资料" left-arrow @click-left="onClickLeft" />
    
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.nickname"
          name="nickname"
          label="昵称"
          placeholder="请输入昵称"
          :rules="[{ required: true, message: '请输入昵称' }]"
        />
        <van-field
          v-model="form.gender"
          name="gender"
          label="性别"
          readonly
          @click="showGenderPicker = true"
        />
        <van-field
          v-model="form.bio"
          name="bio"
          label="个人简介"
          type="textarea"
          rows="3"
          placeholder="请输入个人简介"
        />
      </van-cell-group>
      
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit">
          保存
        </van-button>
      </div>
    </van-form>
    
    <van-popup v-model:show="showGenderPicker" position="bottom">
      <van-picker
        :columns="genderColumns"
        @confirm="onGenderConfirm"
        @cancel="showGenderPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const showGenderPicker = ref(false)
const genderColumns = ['未知', '男', '女']

const form = ref({
  nickname: '测试用户',
  gender: '男',
  bio: ''
})

function onClickLeft() {
  router.back()
}

function onGenderConfirm({ selectedValues }: any) {
  form.value.gender = genderColumns[selectedValues[0]]
  showGenderPicker.value = false
}

function onSubmit() {
  // Save logic here
  router.back()
}
</script>

<style scoped>
.profile-edit {
  min-height: 100vh;
  background: #f5f5f5;
}
</style>
