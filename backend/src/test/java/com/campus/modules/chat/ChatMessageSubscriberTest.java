package com.campus.modules.chat;

import com.campus.modules.chat.service.ChatService;
import com.campus.modules.chat.subscriber.ChatMessageSubscriber;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessageSubscriber - 聊天消息订阅者")
class ChatMessageSubscriberTest {

    @Mock
    private RedisMessageListenerContainer container;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatService chatService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private ChatMessageSubscriber subscriber;

    @BeforeEach
    void setUp() {
        subscriber = new ChatMessageSubscriber(container, messagingTemplate, chatService, objectMapper);
    }

    @Test
    @DisplayName("subscribe - 绑定到Redis频道")
    void shouldSubscribeToChannel() {
        subscriber.subscribe();

        verify(container).addMessageListener(any(), any(ChannelTopic.class));
    }
}
