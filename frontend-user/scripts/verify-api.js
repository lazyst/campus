// frontend-user/scripts/verify-api.js
/**
 * API验证脚本
 * 简单验证所有API函数都能正常工作（使用mock数据）
 */

import axios from 'axios'

// Mock axios
const mockAxiosInstance = {
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  delete: vi.fn(),
  interceptors: {
    request: { use: vi.fn() },
    response: { use: vi.fn() }
  }
}

vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => mockAxiosInstance)
  }
}))

// 统计信息
const results = {
  total: 0,
  passed: 0,
  failed: 0,
  errors: []
}

// 验证函数
async function verifyAPI(name, apiFunc, mockData, mockError = null) {
  results.total++

  try {
    // 设置mock响应
    if (mockError) {
      mockAxiosInstance.post.mockRejectedValue(mockError)
      mockAxiosInstance.get.mockRejectedValue(mockError)
      mockAxiosInstance.put.mockRejectedValue(mockError)
      mockAxiosInstance.delete.mockRejectedValue(mockError)
    } else {
      mockAxiosInstance.post.mockResolvedValue({ data: { code: 200, data: mockData } })
      mockAxiosInstance.get.mockResolvedValue({ data: { code: 200, data: mockData } })
      mockAxiosInstance.put.mockResolvedValue({ data: { code: 200, data: mockData } })
      mockAxiosInstance.delete.mockResolvedValue({ data: { code: 200, data: mockData } })
    }

    // 调用API
    await apiFunc()

    results.passed++
    console.log(`✓ ${name}`)
  } catch (error) {
    results.failed++
    results.errors.push({ name, error: error.message })
    console.log(`✗ ${name}: ${error.message}`)
  }
}

// 主验证函数
async function verifyAllAPIs() {
  console.log('🚀 开始验证API层...\n')

  // 导入所有API模块
  const {
    login,
    register,
    logout
  } = await import('../src/api/modules/auth.js')

  const {
    getUserInfo,
    updateProfile,
    getUserPublicInfo,
    uploadAvatar
  } = await import('../src/api/modules/user.js')

  const {
    getBoards,
    getBoardById
  } = await import('../src/api/modules/board.js')

  const {
    getPosts,
    getPostById,
    getMyPosts,
    createPost,
    updatePost,
    deletePost,
    toggleLikePost,
    checkPostLiked,
    toggleCollectPost,
    checkPostCollected
  } = await import('../src/api/modules/post.js')

  const {
    getCommentsByPost,
    createComment,
    deleteComment
  } = await import('../src/api/modules/comment.js')

  const {
    getNotifications,
    markAsRead,
    markAllAsRead
  } = await import('../src/api/modules/notification.js')

  const {
    getItems,
    getItemById,
    getMyItems,
    createItem,
    updateItem,
    deleteItem,
    contactSeller,
    offlineItem,
    onlineItem,
    completeItem
  } = await import('../src/api/modules/item.js')

  const {
    toggleItemCollect,
    checkItemCollected,
    getCollectedItems
  } = await import('../src/api/modules/itemCollect.js')

  const {
    getConversations,
    createConversation
  } = await import('../src/api/modules/conversation.js')

  const {
    getMessages,
    getMessagesWithUser
  } = await import('../src/api/modules/message.js')

  // Auth模块
  console.log('📦 Auth模块')
  await verifyAPI('login', () => login({ phone: '13800138000', password: '123456' }), 'token')
  await verifyAPI('register', () => register({ phone: '13800138000', password: '123456', nickname: '测试' }), { user: {}, token: 'token' })
  await verifyAPI('logout', () => logout(), {})

  // User模块
  console.log('\n📦 User模块')
  await verifyAPI('getUserInfo', () => getUserInfo(), { id: 1, nickname: '测试' })
  await verifyAPI('updateProfile', () => updateProfile({ nickname: '新昵称' }), { id: 1, nickname: '新昵称' })
  await verifyAPI('getUserPublicInfo', () => getUserPublicInfo(1), { id: 1, nickname: '用户' })
  await verifyAPI('uploadAvatar', () => uploadAvatar(new File([''], 'test.jpg')), { url: 'avatar.jpg' })

  // Board模块
  console.log('\n📦 Board模块')
  await verifyAPI('getBoards', () => getBoards(), [{ id: 1, name: '交流' }])
  await verifyAPI('getBoardById', () => getBoardById(1), { id: 1, name: '交流' })

  // Post模块
  console.log('\n📦 Post模块')
  await verifyAPI('getPosts', () => getPosts({ page: 1, size: 10 }), { records: [], total: 0 })
  await verifyAPI('getPostById', () => getPostById(1), { id: 1, title: '测试' })
  await verifyAPI('getMyPosts', () => getMyPosts({ page: 1 }), { records: [], total: 0 })
  await verifyAPI('createPost', () => createPost({ boardId: 1, title: '测试', content: '内容' }), { id: 1 })
  await verifyAPI('updatePost', () => updatePost(1, { title: '新标题' }), { id: 1, title: '新标题' })
  await verifyAPI('deletePost', () => deletePost(1), { message: '删除成功' })
  await verifyAPI('toggleLikePost', () => toggleLikePost(1), { liked: true, likeCount: 1 })
  await verifyAPI('checkPostLiked', () => checkPostLiked(1), { liked: true })
  await verifyAPI('toggleCollectPost', () => toggleCollectPost(1), { collected: true, collectCount: 1 })
  await verifyAPI('checkPostCollected', () => checkPostCollected(1), { collected: true })

  // Comment模块
  console.log('\n📦 Comment模块')
  await verifyAPI('getCommentsByPost', () => getCommentsByPost(1, { page: 1 }), { records: [], total: 0 })
  await verifyAPI('createComment', () => createComment({ postId: 1, content: '评论' }), { id: 1 })
  await verifyAPI('deleteComment', () => deleteComment(1), { message: '删除成功' })

  // Notification模块
  console.log('\n📦 Notification模块')
  await verifyAPI('getNotifications', () => getNotifications({ page: 1 }), { records: [], total: 0 })
  await verifyAPI('markAsRead', () => markAsRead(1), { message: '标记成功' })
  await verifyAPI('markAllAsRead', () => markAllAsRead(), { message: '全部标记成功' })

  // Item模块
  console.log('\n📦 Item模块')
  await verifyAPI('getItems', () => getItems({ page: 1 }), { records: [], total: 0 })
  await verifyAPI('getItemById', () => getItemById(1), { id: 1, title: '物品' })
  await verifyAPI('getMyItems', () => getMyItems({ page: 1 }), { records: [], total: 0 })
  await verifyAPI('createItem', () => createItem({ type: 2, title: '物品', price: 100 }), { id: 1 })
  await verifyAPI('updateItem', () => updateItem(1, { title: '新标题' }), { id: 1 })
  await verifyAPI('deleteItem', () => deleteItem(1), { message: '删除成功' })
  await verifyAPI('contactSeller', () => contactSeller(1), { message: '已联系' })
  await verifyAPI('offlineItem', () => offlineItem(1), { id: 1, status: 3 })
  await verifyAPI('onlineItem', () => onlineItem(1), { id: 1, status: 1 })
  await verifyAPI('completeItem', () => completeItem(1), { id: 1, status: 2 })

  // ItemCollect模块
  console.log('\n📦 ItemCollect模块')
  await verifyAPI('toggleItemCollect', () => toggleItemCollect(1), { collected: true, collectCount: 1 })
  await verifyAPI('checkItemCollected', () => checkItemCollected(1), { collected: true })
  await verifyAPI('getCollectedItems', () => getCollectedItems({ page: 1 }), { records: [], total: 0 })

  // Conversation模块
  console.log('\n📦 Conversation模块')
  await verifyAPI('getConversations', () => getConversations(), [])
  await verifyAPI('createConversation', () => createConversation(2), { id: 1 })

  // Message模块
  console.log('\n📦 Message模块')
  await verifyAPI('getMessages', () => getMessages(1, { page: 1 }), { records: [], total: 0 })
  await verifyAPI('getMessagesWithUser', () => getMessagesWithUser(2, { page: 1 }), { records: [], total: 0 })

  // 打印结果
  console.log('\n' + '='.repeat(50))
  console.log(`✓ 通过: ${results.passed}`)
  console.log(`✗ 失败: ${results.failed}`)
  console.log(`📊 总计: ${results.total}`)
  console.log(`成功率: ${((results.passed / results.total) * 100).toFixed(1)}%`)
  console.log('='.repeat(50))

  if (results.errors.length > 0) {
    console.log('\n❌ 失败详情:')
    results.errors.forEach(({ name, error }) => {
      console.log(`  - ${name}: ${error}`)
    })
  }

  // 返回退出码
  process.exit(results.failed > 0 ? 1 : 0)
}

// 运行验证
verifyAllAPIs().catch(error => {
  console.error('验证脚本执行失败:', error)
  process.exit(1)
})
