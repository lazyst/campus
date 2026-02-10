export function getApiBaseUrl(): string {
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL
  }
  // 使用当前域名（不拼接端口），通过 nginx 代理访问后端 API
  const { protocol, hostname } = window.location
  return `${protocol}//${hostname}`
}

export function getImageUrl(img: string): string {
  if (!img) return ''
  if (img.startsWith('http')) return img
  const baseUrl = getApiBaseUrl()
  if (img.startsWith('/')) {
    return `${baseUrl}${img}`
  }
  return `${baseUrl}/${img}`
}
