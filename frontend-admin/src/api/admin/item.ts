import { get, put, del } from '../index'

// 闲置物品信息
export interface Item {
  id: number
  userId: number
  type: number
  title: string
  description: string
  price: number
  images: string[]
  viewCount: number
  contactCount: number
  status: number
  userNickname: string
  userAvatar: string
  createdAt: string
}

// 闲置物品列表查询参数
export interface ItemQueryParams {
  page?: number
  size?: number
  type?: number
  status?: number
  userId?: number
  keyword?: string
  minPrice?: number
  maxPrice?: number
}

// 闲置物品分页结果
export interface ItemPageResult {
  records: Item[]
  total: number
  current: number
  size: number
}

// 获取闲置物品列表
export function getItemList(params: ItemQueryParams): Promise<ItemPageResult> {
  return get('/admin/items', params)
}

// 获取闲置物品详情
export function getItemDetail(itemId: number): Promise<Item> {
  return get(`/admin/items/${itemId}`)
}

// 删除闲置物品
export function deleteItem(itemId: number): Promise<void> {
  return del(`/admin/items/${itemId}`)
}

// 下架闲置物品
export function offlineItem(itemId: number): Promise<void> {
  return put(`/admin/items/${itemId}/offline`)
}

// 获取闲置物品统计
export function getItemStats(): Promise<{ total: number; selling: number; completed: number; offline: number }> {
  return get('/admin/items/stats')
}
