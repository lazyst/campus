import { get, put, del } from '../index'

// 获取闲置物品列表
export function getItemList(params) {
  return get('/api/admin/items', params)
}

// 获取闲置物品详情
export function getItemDetail(itemId) {
  return get(`/api/admin/items/${itemId}`)
}

// 删除闲置物品
export function deleteItem(itemId) {
  return del(`/api/admin/items/${itemId}`)
}

// 下架闲置物品
export function offlineItem(itemId) {
  return put(`/api/admin/items/${itemId}/offline`)
}

// 上架闲置物品
export function onlineItem(itemId) {
  return put(`/api/admin/items/${itemId}/online`)
}

// 获取闲置物品统计
export function getItemStats() {
  return get('/api/admin/items/stats')
}
