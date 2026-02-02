package com.campus.modules.chat.publisher;

import com.campus.modules.chat.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

/**
 * 聊天消息发布者
 * 将聊天消息发布到 Redis Pub/Sub 频道
 * 用于集群环境下跨服务器消息同步
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis 频道名称
     */
    private static final String CHAT_CHANNEL = "chat:message";

    /**
     * 发布聊天消息到 Redis 频道
     *
     * @param message 聊天消息DTO
     */
    public void publish(ChatMessageDTO message) {
        try {
            redisTemplate.convertAndSend(CHAT_CHANNEL, message);
            log.debug("聊天消息已发布到 Redis 频道: {}", CHAT_CHANNEL);
        } catch (Exception e) {
            log.error("发布聊天消息到 Redis 失败: {}", e.getMessage(), e);
        }
    }
}
