import { get, put, del } from '../index'

// 获取闲置物品列表
export function getItemList(params) {
  return get('/admin/items', params)
}

// 获取闲置物品详情
export function getItemDetail(itemId) {
  return get(`/admin/items/${itemId}`)
}

// 删除闲置物品
export function deleteItem(itemId) {
  return del(`/admin/items/${itemId}`)
}

// 下架闲置物品
export function offlineItem(itemId) {
  return put(`/admin/items/${itemId}/offline`)
}

// 获取闲置物品统计
export function getItemStats() {
  return get('/admin/items/stats')
}
