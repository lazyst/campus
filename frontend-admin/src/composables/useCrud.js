/**
 * 通用 CRUD 操作 composable
 * 提取通用的增删改查确认逻辑
 */

import { ElMessage, ElMessageBox } from 'element-plus'

export function useCrud() {
  /**
   * 执行删除操作
   * @param {string} itemName - 操作对象名称
   * @param {Function} apiFunc - API函数
   * @param {object} apiParams - API参数
   * @param {Function} onSuccess - 成功回调
   */
  const handleDelete = async (itemName, apiFunc, apiParams, onSuccess) => {
    try {
      await ElMessageBox.confirm(
        `确定要删除 ${itemName} 吗？此操作不可恢复。`,
        '删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )

      const res = await apiFunc(apiParams)
      ElMessage.success('删除成功')
      onSuccess?.()
      return res
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error(error.message || '删除失败')
      }
      throw error
    }
  }

  /**
   * 执行禁用/启用操作
   * @param {string} itemName - 操作对象名称
   * @param {string} action - 操作类型: 'ban' | 'unban' | 'disable' | 'enable'
   * @param {Function} apiFunc - API函数
   * @param {object} apiParams - API参数
   * @param {Function} onSuccess - 成功回调
   */
  const handleToggleStatus = async (itemName, action, apiFunc, apiParams, onSuccess) => {
    const actionText = action === 'ban' || action === 'disable' ? '禁用' : '启用'
    const actionType = action === 'ban' || action === 'disable' ? 'warning' : 'success'

    try {
      await ElMessageBox.confirm(
        `确定要${actionText} ${itemName} 吗？`,
        `${actionText}确认`,
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: actionType
        }
      )

      const res = await apiFunc(apiParams)
      ElMessage.success(`${actionText}成功`)
      onSuccess?.()
      return res
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error(error.message || `${actionText}失败`)
      }
      throw error
    }
  }

  /**
   * 执行通用操作（带确认框）
   * @param {string} message - 确认消息
   * @param {string} title - 确认标题
   * @param {Function} apiFunc - API函数
   * @param {object} apiParams - API参数
   * @param {Function} onSuccess - 成功回调
   */
  const handleAction = async (message, title, apiFunc, apiParams, onSuccess) => {
    try {
      await ElMessageBox.confirm(message, title, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      const res = await apiFunc(apiParams)
      ElMessage.success('操作成功')
      onSuccess?.()
      return res
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error(error.message || '操作失败')
      }
      throw error
    }
  }

  return {
    handleDelete,
    handleToggleStatus,
    handleAction
  }
}
