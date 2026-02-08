import { get } from '../index'

// 获取首页统计数据
export function getDashboardStats() {
  return get('/api/admin/dashboard/stats')
}

// 获取近7天趋势数据
export function getDashboardTrend() {
  return get('/api/admin/dashboard/trend')
}

// 获取最近活跃数据
export function getDashboardRecent() {
  return get('/api/admin/dashboard/recent')
}

// 获取系统状态
export function getDashboardStatus() {
  return get('/api/admin/dashboard/status')
}

// 获取完整首页数据（聚合接口）
export function getDashboardOverview() {
  return get('/api/admin/dashboard/overview')
}
