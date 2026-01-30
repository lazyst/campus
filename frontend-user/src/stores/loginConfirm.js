import { ref } from 'vue'

// 全局登录确认状态
const loginDialogVisible = ref(false)
const loginCallback = ref(null)

/**
 * 显示登录确认对话框
 * @param callback 点击"去登录"后的回调
 */
export function showLoginConfirm(callback) {
  loginDialogVisible.value = true
  if (callback) {
    loginCallback.value = callback
  }
}

/**
 * 隐藏登录确认对话框
 */
export function hideLoginConfirm() {
  loginDialogVisible.value = false
  loginCallback.value = null
}

/**
 * 获取登录对话框可见状态
 */
export function getLoginDialogVisible() {
  return loginDialogVisible.value
}

/**
 * 执行登录回调
 */
export function executeLoginCallback() {
  if (loginCallback.value) {
    loginCallback.value()
  }
}
