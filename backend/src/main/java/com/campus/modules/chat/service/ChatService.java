package com.campus.modules.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.modules.chat.entity.Conversation;
import com.campus.modules.chat.entity.Message;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService extends IService<Message> {

    /**
     * 保存聊天消息
     */
    Message saveMessage(Long senderId, Long receiverId, String content);

    /**
     * 保存聊天消息（支持商品卡片）
     */
    Message saveMessage(Long senderId, Long receiverId, String content, Long itemId);

    /**
     * 保存聊天消息（支持消息类型）
     * @param type 消息类型：1-文本，2-图片，3-商品卡片
     */
    Message saveMessage(Long senderId, Long receiverId, String content, Integer type, Long itemId);

    /**
     * 获取会话列表
     */
    List<Conversation> getConversations(Long userId);

    /**
     * 获取会话消息
     */
    List<Message> getMessages(Long conversationId, Integer page, Integer size);

    /**
     * 获取与特定用户的聊天消息
     */
    List<Message> getMessagesWithUser(Long userId, Long otherUserId, Integer page, Integer size);

    /**
     * 获取或创建会话
     */
    Conversation getOrCreateConversation(Long userId, Long otherUserId);

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
