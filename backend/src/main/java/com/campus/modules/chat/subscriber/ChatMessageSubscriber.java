package com.campus.modules.chat.subscriber;

import com.campus.modules.chat.dto.ChatMessageDTO;
import com.campus.modules.chat.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ChatMessageSubscriber implements MessageListener {

    private final RedisMessageListenerContainer container;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    public ChatMessageSubscriber(RedisMessageListenerContainer container,
                                 SimpMessagingTemplate messagingTemplate,
                                 ChatService chatService,
                                 ObjectMapper objectMapper) {
        this.container = container;
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.objectMapper = objectMapper;
    }

    private static final String CHAT_CHANNEL = "chat:message";
    private static final String QUEUE_MESSAGES = "/queue/messages";
    private static final String QUEUE_UNREAD_COUNT = "/queue/unread-count";

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        subscribe();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // message.getBody() 返回字节数组，需要手动反序列化
            byte[] body = message.getBody();
            if (body == null || body.length == 0) {
                log.warn("收到空的 Redis 消息");
                return;
            }

            // 将字节数组反序列化为 ChatMessageDTO
            ChatMessageDTO chatMessage = objectMapper.readValue(body, ChatMessageDTO.class);

            log.debug("收到 Redis 聊天消息: fromUser={}, toUser={}, content={}",
                    chatMessage.getSenderId(),
                    chatMessage.getReceiverId(),
                    chatMessage.getContent());

            // 通过WebSocket推送给目标用户（解决集群环境下跨服务器推送问题）
            pushMessageToUser(chatMessage);

        } catch (Exception e) {
            log.error("处理 Redis 聊天消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 通过 SimpMessagingTemplate 推送消息给目标用户
     */
    private void pushMessageToUser(ChatMessageDTO chatMessage) {
        try {
            // 推送聊天消息
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getReceiverId().toString(),
                    QUEUE_MESSAGES,
                    chatMessage
            );
            log.debug("Redis Subscriber 通过 WebSocket 推送消息给用户: {}", chatMessage.getReceiverId());

            // 推送未读数（实时更新）
            pushUnreadCountToUser(chatMessage.getReceiverId());
        } catch (Exception e) {
            log.error("推送消息给用户 {} 失败: {}", chatMessage.getReceiverId(), e.getMessage());
        }
    }

    /**
     * 推送未读数给目标用户
     */
    private void pushUnreadCountToUser(Long userId) {
        try {
            Integer totalUnread = chatService.getTotalUnreadCount(userId);
            Map<String, Object> unreadData = new HashMap<>();
            unreadData.put("type", "unread");
            unreadData.put("count", totalUnread != null ? totalUnread : 0);

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    QUEUE_UNREAD_COUNT,
                    unreadData
            );
            log.debug("Redis Subscriber 推送未读数给用户: {}, count: {}", userId, totalUnread);
        } catch (Exception e) {
            log.error("推送未读数给用户 {} 失败: {}", userId, e.getMessage());
        }
    }

    /**
     * 绑定到 Redis 频道
     */
    public void subscribe() {
        container.addMessageListener(this, new ChannelTopic(CHAT_CHANNEL));
        log.info("已订阅 Redis 频道: {}", CHAT_CHANNEL);
    }
}
