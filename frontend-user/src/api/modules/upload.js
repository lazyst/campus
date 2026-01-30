// frontend-user/src/api/modules/upload.js

import request from '../request'

/**
 * 上传单张图片
 * @param {File} file - 图片文件
 * @returns {Promise<string>} 图片URL
 */
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post('/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    loadingText: '上传中...'
  })
}

/**
 * 上传多张图片
 * @param {File[]} files - 图片文件数组
 * @returns {Promise<string[]>} 图片URL数组
 */
export function uploadImages(files) {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  
  return request.post('/upload/images', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    loadingText: '上传中...'
  })
}
