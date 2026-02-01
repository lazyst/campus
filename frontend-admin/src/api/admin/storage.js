import { get, del, post } from '../index'

/**
 * 文件管理 API
 */

/**
 * 获取文件列表（分页）
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} [params.dateDir] - 日期目录
 * @param {string} [params.keyword] - 关键词搜索
 */
export function getFileList(params) {
  return get('/upload/list', params)
}

/**
 * 获取所有日期目录
 */
export function getDateDirs() {
  return get('/upload/dates')
}

/**
 * 获取未使用的图片
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 */
export function getUnusedImages(params) {
  return get('/upload/unused', params)
}

/**
 * 清理未使用的图片
 */
export function cleanUnusedImages() {
  return del('/upload/unused/clean')
}

/**
 * 删除单个图片
 * @param {string} url - 图片URL
 */
export function deleteImage(url) {
  return del('/upload/image?url=' + encodeURIComponent(url))
}

/**
 * 批量删除图片
 * @param {string[]} urls - 图片URL数组
 */
export function deleteImages(urls) {
  return del('/upload/images/batch', urls)
}

/**
 * 获取存储统计信息
 */
export function getStorageStats() {
  return get('/upload/stats')
}
