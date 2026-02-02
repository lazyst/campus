package com.campus.modules.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.modules.chat.dto.ChatMessageDTO;
import com.campus.modules.chat.entity.Conversation;
import com.campus.modules.chat.entity.Message;
import com.campus.modules.chat.mapper.ConversationMapper;
import com.campus.modules.chat.mapper.MessageMapper;
import com.campus.modules.chat.publisher.ChatMessagePublisher;
import com.campus.modules.chat.service.ChatService;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatServiceImpl extends ServiceImpl<MessageMapper, Message>
        implements ChatService {

    private final ConversationMapper conversationMapper;
    private final UserService userService;
    private final ChatMessagePublisher chatMessagePublisher;

    public ChatServiceImpl(ConversationMapper conversationMapper,
                          UserService userService,
                          ChatMessagePublisher chatMessagePublisher) {
        this.conversationMapper = conversationMapper;
        this.userService = userService;
        this.chatMessagePublisher = chatMessagePublisher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message saveMessage(Long senderId, Long receiverId, String content) {
        Conversation conversation = getOrCreateConversation(senderId, receiverId);

        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setType(1);
        message.setSendTime(LocalDateTime.now());

        this.save(message);

        conversation.setLastMessageId(message.getId());
        conversation.setLastMessageContent(content);
        conversation.setLastMessageTime(message.getCreatedAt());
        conversation.incrementUnreadCount(receiverId);

        conversationMapper.updateById(conversation);

        // 发布到 Redis Pub/Sub（用于集群环境下的消息同步）
        publishMessageToRedis(message, conversation.getId());

        return message;
    }

    /**
     * 将聊天消息发布到 Redis Pub/Sub 频道
     * 这样集群中的其他服务器也能收到消息并推送给对应用户
     */
    private void publishMessageToRedis(Message message, Long conversationId) {
        ChatMessageDTO dto = ChatMessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .conversationId(conversationId)
                .content(message.getContent())
                .type(message.getType())
                .sendTime(message.getSendTime())
                .createdAt(message.getCreatedAt())
                .build();

        chatMessagePublisher.publish(dto);
    }

    @Override
    public void clearUnreadCount(Long userId, Long conversationId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return;
        }

        if (conversation.getUserId1() != null && conversation.getUserId1().equals(userId)) {
            conversation.setUnreadCount1(0);
        } else if (conversation.getUserId2() != null && conversation.getUserId2().equals(userId)) {
            conversation.setUnreadCount2(0);
        }

        conversationMapper.updateById(conversation);
    }

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
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getDeleted, false)
               .and(w -> w.eq(Conversation::getUserId1, userId)
                          .or()
                          .eq(Conversation::getUserId2, userId));

        List<Conversation> conversations = conversationMapper.selectList(wrapper);

        int totalUnread = 0;
        for (Conversation conversation : conversations) {
            totalUnread += conversation.getUnreadCount(userId);
        }

        return totalUnread;
    }

    @Override
    public List<Conversation> getConversations(Long userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getDeleted, false)
               .and(w -> w.eq(Conversation::getUserId1, userId)
                          .or()
                          .eq(Conversation::getUserId2, userId))
               .orderByDesc(Conversation::getUpdatedAt);

        List<Conversation> conversations = conversationMapper.selectList(wrapper);

        for (Conversation conversation : conversations) {
            Long otherUserId = conversation.getUserId1().equals(userId)
                    ? conversation.getUserId2()
                    : conversation.getUserId1();

            conversation.setOtherUserId(otherUserId);

            User otherUser = userService.getById(otherUserId);
            if (otherUser != null) {
                conversation.setOtherUserNickname(otherUser.getNickname());
                conversation.setOtherUserAvatar(otherUser.getAvatar());
            }

            Message lastMessage = getLastMessage(conversation.getId());
            if (lastMessage != null) {
                conversation.setLastMessageContent(lastMessage.getContent());
                conversation.setLastMessageTime(lastMessage.getCreatedAt());
            }

            conversation.setUnreadCount(conversation.getUnreadCount(userId));
        }

        return conversations;
    }

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
        Conversation conversation = findConversation(userId, otherUserId);
        
        if (conversation == null) {
            return new ArrayList<>();
        }
        
        return getMessages(conversation.getId(), page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Conversation getOrCreateConversation(Long userId1, Long userId2) {
        Conversation existing = findConversation(userId1, userId2);
        if (existing != null) {
            return existing;
        }
        
        Conversation conversation = new Conversation();
        conversation.setUserId1(userId1);
        conversation.setUserId2(userId2);
        conversation.setLastMessageId(0L);
        
        conversationMapper.insert(conversation);
        return conversation;
    }

    private Conversation findConversation(Long userId1, Long userId2) {
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
