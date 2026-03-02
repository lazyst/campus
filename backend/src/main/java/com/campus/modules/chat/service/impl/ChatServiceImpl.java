package com.campus.modules.chat.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
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
import com.campus.modules.trade.entity.Item;
import com.campus.modules.trade.mapper.ItemMapper;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@DS("slave")
public class ChatServiceImpl extends ServiceImpl<MessageMapper, Message>
        implements ChatService {

    private final ConversationMapper conversationMapper;
    private final UserService userService;
    private final ChatMessagePublisher chatMessagePublisher;
    private final ItemMapper itemMapper;

    public ChatServiceImpl(ConversationMapper conversationMapper,
                          UserService userService,
                          ChatMessagePublisher chatMessagePublisher,
                          ItemMapper itemMapper) {
        this.conversationMapper = conversationMapper;
        this.userService = userService;
        this.chatMessagePublisher = chatMessagePublisher;
        this.itemMapper = itemMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DS("master")
    public Message saveMessage(Long senderId, Long receiverId, String content) {
        return saveMessage(senderId, receiverId, content, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DS("master")
    public Message saveMessage(Long senderId, Long receiverId, String content, Long itemId) {
        Conversation conversation = getOrCreateConversation(senderId, receiverId);

        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setType(itemId != null ? 3 : 1);
        message.setItemId(itemId);
        message.setSendTime(LocalDateTime.now());

        // 如果是商品卡片消息，填充商品信息
        if (itemId != null) {
            Item item = itemMapper.selectById(itemId);
            if (item != null) {
                message.setItemTitle(item.getTitle());
                message.setItemPrice(item.getPrice());
                message.setItemImage(item.getImages());
                message.setItemType(item.getType());
                
                // 查询商品原始所有者信息（不是发送消息的用户）
                User itemOwner = userService.getById(item.getUserId());
                if (itemOwner != null) {
                    message.setItemUserNickname(itemOwner.getNickname());
                    message.setItemUserAvatar(itemOwner.getAvatar());
                }
            }
        }

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
                .itemId(message.getItemId())
                .itemTitle(message.getItemTitle())
                .itemPrice(message.getItemPrice())
                .itemImage(message.getItemImage())
                .itemType(message.getItemType())
                .itemUserNickname(message.getItemUserNickname())
                .itemUserAvatar(message.getItemUserAvatar())
                .sendTime(message.getSendTime())
                .createdAt(message.getCreatedAt())
                .build();

        chatMessagePublisher.publish(dto);
    }

    @Override
    @DS("master")
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
        // 使用SQL的SUM函数直接计算未读总数，避免Java循环
        return conversationMapper.sumUnreadCount(userId);
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

        if (conversations.isEmpty()) {
            return conversations;
        }

        // 收集所有需要的userId和conversationId，使用批量查询避免N+1问题
        Set<Long> otherUserIds = conversations.stream()
                .map(c -> c.getUserId1().equals(userId) ? c.getUserId2() : c.getUserId1())
                .collect(Collectors.toSet());

        Set<Long> conversationIds = conversations.stream()
                .map(Conversation::getId)
                .collect(Collectors.toSet());

        // 批量查询用户信息
        Map<Long, User> userMap = userService.listByIds(otherUserIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 批量查询最后消息
        Map<Long, Message> lastMessageMap = getLastMessages(conversationIds);

        // 填充会话信息
        for (Conversation conversation : conversations) {
            Long otherUserId = conversation.getUserId1().equals(userId)
                    ? conversation.getUserId2()
                    : conversation.getUserId1();

            conversation.setOtherUserId(otherUserId);

            User otherUser = userMap.get(otherUserId);
            if (otherUser != null) {
                conversation.setOtherUserNickname(otherUser.getNickname());
                conversation.setOtherUserAvatar(otherUser.getAvatar());
            }

            Message lastMessage = lastMessageMap.get(conversation.getId());
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

    /**
     * 批量查询每个会话的最后一条消息
     */
    private Map<Long, Message> getLastMessages(Set<Long> conversationIds) {
        if (conversationIds == null || conversationIds.isEmpty()) {
            return Map.of();
        }

        // 将Set转换为List，MyBatis需要List或数组
        List<Long> conversationIdList = new ArrayList<>(conversationIds);

        // 查询每个会话的最新消息
        List<Message> messages = this.baseMapper.selectLastMessagesByConversationIds(conversationIdList);

        return messages.stream()
                .collect(Collectors.toMap(Message::getConversationId, msg -> msg));
    }

    @Override
    public List<Message> getMessages(Long conversationId, Integer page, Integer size) {
        Page<Message> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId)
               .eq(Message::getDeleted, false)
               .orderByAsc(Message::getCreatedAt);

        Page<Message> result = this.page(pageParam, wrapper);

        List<Message> messages = result.getRecords();
        if (!messages.isEmpty()) {
            // 批量查询发送者信息，避免 N+1 问题
            Set<Long> senderIds = messages.stream()
                    .map(Message::getSenderId)
                    .collect(Collectors.toSet());

            Map<Long, User> userMap = userService.listByIds(senderIds).stream()
                    .collect(Collectors.toMap(User::getId, user -> user));

            // 批量查询商品信息（只查询商品卡片消息）
            Set<Long> itemIds = messages.stream()
                    .filter(m -> m.getType() != null && m.getType() == 3 && m.getItemId() != null)
                    .map(Message::getItemId)
                    .collect(Collectors.toSet());

            Map<Long, Item> itemMap = null;
            if (!itemIds.isEmpty()) {
                itemMap = itemMapper.selectBatchIds(itemIds).stream()
                        .collect(Collectors.toMap(Item::getId, item -> item));
            }

            for (Message message : messages) {
                // 填充发送者信息
                User sender = userMap.get(message.getSenderId());
                if (sender != null) {
                    message.setSenderNickname(sender.getNickname());
                    message.setSenderAvatar(sender.getAvatar());
                }
                message.setSendTime(message.getCreatedAt());

                // 填充商品信息（如果是商品卡片消息）
                if (message.getType() != null && message.getType() == 3 && message.getItemId() != null) {
                    Item item = itemMap != null ? itemMap.get(message.getItemId()) : null;
                    if (item != null) {
                        message.setItemTitle(item.getTitle());
                        message.setItemPrice(item.getPrice() != null ? item.getPrice().doubleValue() : null);
                        message.setItemImage(item.getImages());
                        message.setItemType(item.getType());
                        
                        // 查询商品所有者信息
                        User itemOwner = userMap.get(item.getUserId());
                        if (itemOwner != null) {
                            message.setItemUserNickname(itemOwner.getNickname());
                            message.setItemUserAvatar(itemOwner.getAvatar());
                        }
                    }
                }
            }
        }

        return messages;
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
    @DS("master")
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
