package com.campus.modules.chat.websocket.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.modules.chat.entity.Conversation;
import com.campus.modules.chat.entity.Message;
import com.campus.modules.chat.mapper.ConversationMapper;
import com.campus.modules.chat.mapper.MessageMapper;
import com.campus.modules.chat.websocket.ChatService;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天服务实现
 */
@Service
public class ChatServiceImpl extends ServiceImpl<MessageMapper, Message>
        implements ChatService {

    private final ConversationMapper conversationMapper;
    private final UserService userService;

    public ChatServiceImpl(ConversationMapper conversationMapper, UserService userService) {
        this.conversationMapper = conversationMapper;
        this.userService = userService;
    }

    @Override
    @Transactional
    public Message saveMessage(Long senderId, Long receiverId, String content) {
        // 获取或创建会话
        Conversation conversation = getOrCreateConversation(senderId, receiverId);

        // 创建消息
        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setType(1);  // 文本消息
        message.setSendTime(LocalDateTime.now());

        this.save(message);

        // 更新会话的最后消息
        conversation.setLastMessageId(message.getId());
        conversation.setLastMessageContent(content);
        conversation.setLastMessageTime(message.getCreatedAt());

        // 增加接收者的未读消息数量
        conversation.incrementUnreadCount(receiverId);

        conversationMapper.updateById(conversation);

        return message;
    }

    @Override
    public void clearUnreadCount(Long userId, Long conversationId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return;
        }

        // 判断用户是 userId1 还是 userId2
        if (conversation.getUserId1() != null && conversation.getUserId1().equals(userId)) {
            conversation.setUnreadCount1(0);
        } else if (conversation.getUserId2() != null && conversation.getUserId2().equals(userId)) {
            conversation.setUnreadCount2(0);
        }

        conversationMapper.updateById(conversation);
    }

    /**
     * 通过两个用户ID清除某个用户的未读消息数
     */
    @Override
    public void clearUnreadCountByUserIds(Long userId, Long otherUserId) {
        Conversation conversation = findConversation(userId, otherUserId);
        if (conversation == null) {
            return;
        }

        clearUnreadCount(userId, conversation.getId());
    }

    @Override
    public Integer getTotalUnreadCount(Long userId) {
        // 查询用户参与的所有会话
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getDeleted, false)
               .and(w -> w.eq(Conversation::getUserId1, userId)
                          .or()
                          .eq(Conversation::getUserId2, userId));

        List<Conversation> conversations = conversationMapper.selectList(wrapper);

        // 计算总未读数
        int totalUnread = 0;
        for (Conversation conversation : conversations) {
            totalUnread += conversation.getUnreadCount(userId);
        }

        return totalUnread;
    }

    @Override
    public List<Conversation> getConversations(Long userId) {
        // 查询用户参与的所有会话
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getDeleted, false)
               .and(w -> w.eq(Conversation::getUserId1, userId)
                          .or()
                          .eq(Conversation::getUserId2, userId))
               .orderByDesc(Conversation::getUpdatedAt);

        List<Conversation> conversations = conversationMapper.selectList(wrapper);

        // 填充会话信息
        for (Conversation conversation : conversations) {
            // 获取对方用户ID
            Long otherUserId = conversation.getUserId1().equals(userId)
                    ? conversation.getUserId2()
                    : conversation.getUserId1();

            // 设置对方用户ID
            conversation.setOtherUserId(otherUserId);

            // 获取对方用户信息
            User otherUser = userService.getById(otherUserId);
            if (otherUser != null) {
                conversation.setOtherUserNickname(otherUser.getNickname());
                conversation.setOtherUserAvatar(otherUser.getAvatar());
            }

            // 获取最新一条消息
            Message lastMessage = getLastMessage(conversation.getId());
            if (lastMessage != null) {
                conversation.setLastMessageContent(lastMessage.getContent());
                conversation.setLastMessageTime(lastMessage.getCreatedAt());
            }

            // 设置当前用户的未读消息数量
            conversation.setUnreadCount(conversation.getUnreadCount(userId));
        }

        return conversations;
    }

    /**
     * 获取会话的最后一条消息
     */
    private Message getLastMessage(Long conversationId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId)
               .eq(Message::getDeleted, false)
               .orderByDesc(Message::getCreatedAt)
               .last("LIMIT 1");

        return this.baseMapper.selectOne(wrapper);
    }

    @Override
    public List<Message> getMessages(Long conversationId, Integer page, Integer size) {
        Page<Message> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId)
               .eq(Message::getDeleted, false)
               .orderByAsc(Message::getCreatedAt);
        
        Page<Message> result = this.page(pageParam, wrapper);
        
        // 填充发送者信息
        for (Message message : result.getRecords()) {
            User sender = userService.getById(message.getSenderId());
            if (sender != null) {
                message.setSenderNickname(sender.getNickname());
                message.setSenderAvatar(sender.getAvatar());
            }
            message.setSendTime(message.getCreatedAt());
        }
        
        return result.getRecords();
    }

    @Override
    public List<Message> getMessagesWithUser(Long userId, Long otherUserId, Integer page, Integer size) {
        // 查找会话
        Conversation conversation = findConversation(userId, otherUserId);
        
        if (conversation == null) {
            return new ArrayList<>();
        }
        
        return getMessages(conversation.getId(), page, size);
    }

    @Override
    @Transactional
    public Conversation getOrCreateConversation(Long userId1, Long userId2) {
        // 先查找是否已存在会话
        Conversation existing = findConversation(userId1, userId2);
        if (existing != null) {
            return existing;
        }
        
        // 创建新会话
        Conversation conversation = new Conversation();
        conversation.setUserId1(userId1);
        conversation.setUserId2(userId2);
        conversation.setLastMessageId(0L);
        
        conversationMapper.insert(conversation);
        return conversation;
    }

    /**
     * 查找两个用户之间的会话
     */
    private Conversation findConversation(Long userId1, Long userId2) {
        // 查询: (userId1=A AND userId2=B) OR (userId1=B AND userId2=A)
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getDeleted, false)
               .and(wrapper1 -> 
                   wrapper1.eq(Conversation::getUserId1, userId1)
                           .eq(Conversation::getUserId2, userId2))
               .or(wrapper2 ->
                   wrapper2.eq(Conversation::getUserId1, userId2)
                           .eq(Conversation::getUserId2, userId1));
        
        return conversationMapper.selectOne(wrapper);
    }
}
