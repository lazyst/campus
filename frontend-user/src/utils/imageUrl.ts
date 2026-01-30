export function getApiBaseUrl(): string {
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL
  }
  const { protocol, hostname } = window.location
  return `${protocol}//${hostname}:8080`
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
