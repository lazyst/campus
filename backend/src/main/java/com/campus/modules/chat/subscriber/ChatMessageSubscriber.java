package com.campus.modules.chat.subscriber;

import com.campus.modules.chat.dto.ChatMessageDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * 聊天消息订阅者
 * 监听 Redis Pub/Sub 频道，收到消息后通过 WebSocket 推送给目标用户
 * 用于集群环境下跨服务器消息同步
 */
@Slf4j
@Component
public class ChatMessageSubscriber implements MessageListener {

    private final RedisMessageListenerContainer container;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageSubscriber(RedisMessageListenerContainer container, @Lazy SimpMessagingTemplate messagingTemplate) {
        this.container = container;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Redis 频道名称
     */
    private static final String CHAT_CHANNEL = "chat:message";

    /**
     * WebSocket 消息目标地址
     */
    private static final String QUEUE_MESSAGES = "/queue/messages";

    /**
     * Bean初始化后自动订阅Redis频道（延迟订阅以避免循环依赖）
     */
    @PostConstruct
    public void init() {
        // 延迟订阅，等待所有 Bean 初始化完成
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                subscribe();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("订阅线程被中断");
            }
        }).start();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // 反序列化消息
            Object body = message.getBody();
            if (body instanceof ChatMessageDTO) {
                ChatMessageDTO chatMessage = (ChatMessageDTO) body;
                log.debug("收到 Redis 聊天消息: fromUser={}, toUser={}, content={}",
                        chatMessage.getSenderId(),
                        chatMessage.getReceiverId(),
                        chatMessage.getContent());

                // 通过WebSocket推送给目标用户（解决集群环境下跨服务器推送问题）
                pushMessageToUser(chatMessage);
            }
        } catch (Exception e) {
            log.error("处理 Redis 聊天消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 通过 SimpMessagingTemplate 推送消息给目标用户
     */
    private void pushMessageToUser(ChatMessageDTO chatMessage) {
        try {
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getReceiverId().toString(),
                    QUEUE_MESSAGES,
                    chatMessage
            );
            log.debug("Redis Subscriber 通过 WebSocket 推送消息给用户: {}", chatMessage.getReceiverId());
        } catch (Exception e) {
            log.error("推送消息给用户 {} 失败: {}", chatMessage.getReceiverId(), e.getMessage());
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
