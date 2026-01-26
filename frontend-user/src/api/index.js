// frontend-user/src/api/index.js

/**
 * API模块统一导出
 * 所有API模块从这里导出
 */

// 认证相关
export * from './modules/auth'

// 用户相关
export * from './modules/user'

// 板块相关
export * from './modules/board'

// 帖子相关
export * from './modules/post'

// 评论相关
export * from './modules/comment'

// 通知相关
export * from './modules/notification'

// 交易相关
export * from './modules/item'
export * from './modules/itemCollect'

// 聊天相关
export * from './modules/conversation'
export * from './modules/message'

// 核心request
export { default as request } from './request'
