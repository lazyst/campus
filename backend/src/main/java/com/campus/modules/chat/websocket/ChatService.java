package com.campus.modules.chat.websocket;

import org.springframework.stereotype.Service;

/**
 * WebSocket聊天服务接口
 */
public interface ChatService {
    
    /**
     * 保存聊天消息
     */
    com.campus.modules.chat.entity.Message saveMessage(Long senderId, Long receiverId, String content);
    
    /**
     * 获取会话列表
     */
    java.util.List<com.campus.modules.chat.entity.Conversation> getConversations(Long userId);
    
    /**
     * 获取会话消息
     */
    java.util.List<com.campus.modules.chat.entity.Message> getMessages(Long conversationId, Integer page, Integer size);
    
    /**
     * 获取与特定用户的聊天消息
     */
    java.util.List<com.campus.modules.chat.entity.Message> getMessagesWithUser(Long userId, Long otherUserId, Integer page, Integer size);
    
    /**
     * 获取或创建会话
     */
    com.campus.modules.chat.entity.Conversation getOrCreateConversation(Long userId, Long otherUserId);

    /**
     * 清空会话的未读消息数
     */
    void clearUnreadCount(Long userId, Long conversationId);

    /**
     * 通过用户ID清除与某个用户的未读消息数
     */
    void clearUnreadCountByUserIds(Long userId, Long otherUserId);

    /**
     * 获取用户的总未读消息数
     */
    Integer getTotalUnreadCount(Long userId);
}
